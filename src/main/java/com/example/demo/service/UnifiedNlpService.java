package com.example.demo.service;

import com.example.demo.config.NlpCloudProperties;
import com.example.demo.dto.ClinicalNoteRequest;
import com.example.demo.dto.EntityExtractionResponse;
import com.example.demo.dto.GrammarResponse;
import com.example.demo.dto.KeywordResponse;
import com.example.demo.dto.SummaryResponse;
import com.example.demo.exception.UpstreamServiceException;
import com.example.demo.mapper.NlpCloudMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.TimeoutException;
import java.util.function.Supplier;
import java.util.stream.Collectors;

@Service
public class UnifiedNlpService {

    private static final Logger log = LoggerFactory.getLogger(UnifiedNlpService.class);
    private static final String SAFE_UPSTREAM_MESSAGE = "Unable to process NLP request at this time. Please try again later.";

    private final RestTemplate nlpCloudRestTemplate;
    private final NlpCloudMapper mapper;
    private final NlpCloudProperties properties;

    public UnifiedNlpService(RestTemplate nlpCloudRestTemplate, NlpCloudMapper mapper, NlpCloudProperties properties) {
        this.nlpCloudRestTemplate = nlpCloudRestTemplate;
        this.mapper = mapper;
        this.properties = properties;
    }

    public GrammarResponse checkGrammar(ClinicalNoteRequest request) {
        String prompt = grammarPrompt(request.getNote(), request.getPatientContext());
        String payload = callSummarization(prompt);
        return new GrammarResponse(mapper.readSummaryText(payload), Collections.emptyList());
    }

    public SummaryResponse summarize(ClinicalNoteRequest request) {
        String prompt = summarizationPrompt(request.getNote(), request.getPatientContext());
        String payload = callSummarization(prompt);
        return mapper.toSummaryResponse(payload);
    }

    public KeywordResponse keywords(ClinicalNoteRequest request) {
        String prompt = keywordPrompt(request.getNote(), request.getPatientContext());
        String payload = callSummarization(prompt);
        return new KeywordResponse(parseKeywords(mapper.readSummaryText(payload)));
    }

    public EntityExtractionResponse extractEntities(ClinicalNoteRequest request) {
        String prompt = entityPrompt(request.getNote(), request.getPatientContext());
        String responseBody = callSummarization(prompt);
        return mapper.toEntityExtractionResponse(responseBody);
    }

    private String callSummarization(String text) {
        Map<String, String> payload = Map.of("text", text);


        return executeWithRetry(summarizationPath(), () -> {
            HttpEntity<Map<String, String>> requestEntity = new HttpEntity<>(payload, authorizationHeaders());


            ResponseEntity<String> response = nlpCloudRestTemplate.postForEntity(summarizationPath(), requestEntity, String.class);
            log.info("NLP Cloud response status → {}", response.getStatusCode());
            log.info("NLP Cloud response body → {}", response.getBody());
            return Objects.requireNonNullElse(response.getBody(), "");



        });
    }

    private <T> T executeWithRetry(String path, Supplier<T> action) {
        int attempts = Math.min(Math.max(properties.getMaxRetries(), 1), 3);
        for (int attempt = 1; attempt <= attempts; attempt++) {
            try {
                return action.get();
            } catch (Exception ex) {
                boolean retryable = attempt < attempts && isRetryable(ex);
                log.warn("Attempt {}/{} failed calling {}: {}", attempt, attempts, path, ex.getClass().getSimpleName());
                if (!retryable) {
                    throw mapUpstreamError(path, ex);
                }
                try {
                    Thread.sleep(300);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    throw mapUpstreamError(path, ex);
                }
            }
        }
        throw new UpstreamServiceException(SAFE_UPSTREAM_MESSAGE, null);
    }

    private boolean isRetryable(Throwable throwable) {
        if (throwable instanceof TimeoutException || throwable instanceof IOException) {
            return true;
        }
        if (throwable instanceof ResourceAccessException && throwable.getCause() instanceof IOException) {
            return true;
        }
        if (throwable instanceof RestClientResponseException responseException) {
            return responseException.getStatusCode().is5xxServerError();
        }
        return false;
    }

    private UpstreamServiceException mapUpstreamError(String path, Throwable throwable) {
        if (throwable instanceof UpstreamServiceException upstream) {
            return upstream;
        }
        if (throwable instanceof RestClientResponseException responseException) {
            log.error("NLP Cloud returned {} for {}. Body: {}", responseException.getStatusCode().value(), path, responseException.getResponseBodyAsString());
            return new UpstreamServiceException(SAFE_UPSTREAM_MESSAGE, responseException);
        }
        if (throwable instanceof TimeoutException || throwable instanceof ResourceAccessException) {
            log.error("NLP Cloud timeout or resource access issue for {}: {}", path, throwable.getMessage());
            return new UpstreamServiceException(SAFE_UPSTREAM_MESSAGE, throwable);
        }
        return new UpstreamServiceException(SAFE_UPSTREAM_MESSAGE, throwable);
    }

    private HttpHeaders authorizationHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set(HttpHeaders.AUTHORIZATION, "Token " + resolveApiKey());
        return headers;
    }

    private String resolveApiKey() {
        String apiKey = sanitize(properties.getApiKey());
        if (apiKey == null || apiKey.isBlank() || Objects.equals(apiKey, "***redacted***")) {
            throw new UpstreamServiceException("NLP Cloud API key is missing. Please configure 'nlpcloud.api-key'.");
        }
        return apiKey;
    }

    private String summarizationPath() {
        String model = sanitize(properties.getSummarizationModel());
        if (model == null || model.isBlank()) {
            throw new UpstreamServiceException("NLP Cloud summarization model is missing. Please configure 'nlpcloud.summarization-model'.");
        }
        String endpoint = sanitize(properties.getSummarizationEndpoint());
        if (endpoint == null || endpoint.isBlank()) {
            throw new UpstreamServiceException("NLP Cloud summarization endpoint is missing. Please configure 'nlpcloud.summarization-endpoint'.");
        }
        if (!endpoint.startsWith("/")) {
            endpoint = "/" + endpoint;
        }
        return "/" + model + endpoint;
    }

    private String sanitize(String value) {
        return value == null ? null : value.trim();
    }

    private String grammarPrompt(String note, String context) {
        StringBuilder builder = new StringBuilder();
        builder.append("You are a medical writing assistant. Correct grammar, spelling, and clarity in the following note. Return only the corrected text without explanations.\n\n");
        if (context != null && !context.isBlank()) {
            builder.append("Clinical context: ").append(context.trim()).append("\n\n");
        }
        builder.append("Note: ").append(note);
        return builder.toString();
    }

    private String entityPrompt(String note, String context) {
        StringBuilder builder = new StringBuilder();
        builder.append("Extract medical entities from the note and respond as JSON with an 'entities' array where each object contains entity, text, start, end, and confidence fields.\n\n");
        if (context != null && !context.isBlank()) {
            builder.append("Clinical context: ").append(context.trim()).append("\n\n");
        }
        builder.append("Note: ").append(note);
        return builder.toString();
    }

    private String summarizationPrompt(String note, String context) {
        StringBuilder builder = new StringBuilder();
        builder.append("Summarize the following clinical information in 3-4 sentences highlighting key findings and recommendations.\n\n");
        if (context != null && !context.isBlank()) {
            builder.append("Clinical context: ").append(context.trim()).append("\n\n");
        }
        builder.append("Note: ").append(note);
        return builder.toString();
    }

    private String keywordPrompt(String note, String context) {
        StringBuilder builder = new StringBuilder();
        builder.append("Extract concise medical keywords or keyphrases from the following note. Respond with a comma-separated list only.\n\n");
        if (context != null && !context.isBlank()) {
            builder.append("Clinical context: ").append(context.trim()).append("\n\n");
        }
        builder.append("Note: ").append(note);
        return builder.toString();
    }

    private List<String> parseKeywords(String summaryText) {
        return Arrays.stream(summaryText.split(",|\n"))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .distinct()
                .collect(Collectors.toList());
    }
}
