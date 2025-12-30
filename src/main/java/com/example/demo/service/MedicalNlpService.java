package com.example.demo.service;

import com.example.demo.config.NlpCloudProperties;
import com.example.demo.dto.*;
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
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeoutException;
import java.util.function.Function;
import java.util.Objects;

@Service
public class MedicalNlpService {

    private static final Logger log = LoggerFactory.getLogger(MedicalNlpService.class);
    private static final String SAFE_UPSTREAM_MESSAGE = "Unable to process NLP request at this time. Please try again later.";

    private final RestTemplate nlpCloudRestTemplate;
    private final NlpCloudMapper mapper;
    private final NlpCloudProperties properties;

    public MedicalNlpService(RestTemplate nlpCloudRestTemplate, NlpCloudMapper mapper, NlpCloudProperties properties) {
        this.nlpCloudRestTemplate = nlpCloudRestTemplate;
        this.mapper = mapper;
        this.properties = properties;
    }

    public GrammarResponse checkGrammar(ClinicalNoteRequest request) {
        return postForResponse(modelPath(properties.getModels().getGrammar(), "grammar"), request, mapper::toGrammarResponse);
    }

    public EntityExtractionResponse extractEntities(ClinicalNoteRequest request) {
        return postForResponse(modelPath(properties.getModels().getEntities(), "entities"), request, mapper::toEntityExtractionResponse);
    }

    public SummaryResponse summarize(ClinicalNoteRequest request) {
        return postForResponse(modelPath(properties.getModels().getSummarize(), "summarize"), request, mapper::toSummaryResponse);
    }

    public KeywordResponse keywords(ClinicalNoteRequest request) {
        return postForResponse(modelPath(properties.getModels().getKeywords(), "keywords"), request, mapper::toKeywordResponse);
    }

    private <T> T postForResponse(String path,
                                  ClinicalNoteRequest request,
                                  Function<String, T> mapperFunction) {
        Map<String, String> payload = new HashMap<>();
        payload.put("text", request.getNote());
        if (request.getPatientContext() != null && !request.getPatientContext().isEmpty()) {
            payload.put("context", request.getPatientContext());
        }

        HttpHeaders headers = authorizationHeaders();

        String responseBody = executeWithRetry(path, () -> {
            HttpEntity<Map<String, String>> requestEntity = new HttpEntity<>(payload, headers);

            ResponseEntity<String> response = nlpCloudRestTemplate.postForEntity(path, requestEntity, String.class);
            return response.getBody();
        });

        return mapperFunction.apply(responseBody);
    }

    private <T> T executeWithRetry(String path, SupplierWithException<T> action) {
        int attempts = Math.min(properties.getMaxRetries(), 2) + 1;
        for (int attempt = 1; attempt <= attempts; attempt++) {
            try {
                return action.get();
            } catch (Exception ex) {
                boolean retryable = attempt < attempts && isRetryable(ex);
                log.warn("Attempt {}/{} failed calling {}: {}", attempt, attempts, path, ex.getClass().getSimpleName());
                if (!retryable) {
                    throw mapUpstreamError(ex);
                }
                try {
                    Thread.sleep(300);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    throw mapUpstreamError(ex);
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

    private UpstreamServiceException mapUpstreamError(Throwable throwable) {
        if (throwable instanceof UpstreamServiceException upstream) {
            return upstream;
        }
        if (throwable instanceof RestClientResponseException responseException) {
            return new UpstreamServiceException(
                    SAFE_UPSTREAM_MESSAGE,
                    responseException);
        }
        if (throwable instanceof TimeoutException || throwable instanceof ResourceAccessException) {
            return new UpstreamServiceException(SAFE_UPSTREAM_MESSAGE, throwable);
        }
        return new UpstreamServiceException(SAFE_UPSTREAM_MESSAGE, throwable);
    }

    @FunctionalInterface
    private interface SupplierWithException<T> {
        T get() throws Exception;
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

    private String modelPath(String model, String endpoint) {
        String sanitizedModel = sanitize(model);
        if (sanitizedModel == null || sanitizedModel.isBlank()) {
            throw new UpstreamServiceException(String.format("NLP Cloud model for %s is missing. Please configure 'nlpcloud.models.%s'.", endpoint, endpoint));
        }
        return "/" + sanitizedModel + "/" + endpoint;
    }

    private String sanitize(String value) {
        return value == null ? null : value.trim();
    }
}
