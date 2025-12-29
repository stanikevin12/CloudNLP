package com.example.demo.service;

import com.example.demo.config.NlpCloudProperties;
import com.example.demo.dto.*;
import com.example.demo.exception.UpstreamServiceException;
import com.example.demo.mapper.NlpCloudMapper;
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

@Service
public class MedicalNlpService {

    private final RestTemplate nlpCloudRestTemplate;
    private final NlpCloudMapper mapper;
    private final NlpCloudProperties properties;

    public MedicalNlpService(RestTemplate nlpCloudRestTemplate, NlpCloudMapper mapper, NlpCloudProperties properties) {
        this.nlpCloudRestTemplate = nlpCloudRestTemplate;
        this.mapper = mapper;
        this.properties = properties;
    }

    public GrammarResponse checkGrammar(ClinicalNoteRequest request) {
        return postForResponse("/grammar", request, mapper::toGrammarResponse);
    }

    public EntityExtractionResponse extractEntities(ClinicalNoteRequest request) {
        return postForResponse("/entities", request, mapper::toEntityExtractionResponse);
    }

    public SummaryResponse summarize(ClinicalNoteRequest request) {
        return postForResponse("/summarize", request, mapper::toSummaryResponse);
    }

    public KeywordResponse keywords(ClinicalNoteRequest request) {
        return postForResponse("/keywords", request, mapper::toKeywordResponse);
    }

    private <T> T postForResponse(String path,
                                  ClinicalNoteRequest request,
                                  Function<String, T> mapperFunction) {
        Map<String, String> payload = new HashMap<>();
        payload.put("text", request.getNote());
        if (request.getPatientContext() != null && !request.getPatientContext().isEmpty()) {
            payload.put("context", request.getPatientContext());
        }

        String responseBody = executeWithRetry(() -> {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<Map<String, String>> requestEntity = new HttpEntity<>(payload, headers);

            ResponseEntity<String> response = nlpCloudRestTemplate.postForEntity(path, requestEntity, String.class);
            return response.getBody();
        });

        return mapperFunction.apply(responseBody);
    }

    private <T> T executeWithRetry(SupplierWithException<T> action) {
        int attempts = properties.getMaxRetries() + 1;
        for (int attempt = 1; attempt <= attempts; attempt++) {
            try {
                return action.get();
            } catch (Exception ex) {
                if (attempt == attempts || !isRetryable(ex)) {
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
        throw new UpstreamServiceException("Upstream NLP service did not respond after retries", null);
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
                    "NLP service returned an unexpected response",
                    responseException);
        }
        if (throwable instanceof TimeoutException || throwable instanceof ResourceAccessException) {
            return new UpstreamServiceException("Timed out calling upstream NLP service", throwable);
        }
        return new UpstreamServiceException("Failed to reach upstream NLP service", throwable);
    }

    @FunctionalInterface
    private interface SupplierWithException<T> {
        T get() throws Exception;
    }
}
