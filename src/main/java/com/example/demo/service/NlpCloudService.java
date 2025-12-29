package com.example.demo.service;

import com.example.demo.config.NlpCloudProperties;
import com.example.demo.dto.ClassificationRequest;
import com.example.demo.dto.ClassificationResponse;
import com.example.demo.exception.UpstreamServiceException;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.client.ResourceAccessException;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.TimeoutException;

@Service
public class NlpCloudService {

    private final RestTemplate nlpCloudRestTemplate;
    private final NlpCloudProperties properties;

    public NlpCloudService(RestTemplate nlpCloudRestTemplate, NlpCloudProperties properties) {
        this.nlpCloudRestTemplate = nlpCloudRestTemplate;
        this.properties = properties;
    }

    public ClassificationResponse classify(String text) {

        ClassificationRequest requestBody = new ClassificationRequest(
                text,
                List.of("space", "sport", "business", "journalism", "politics"),
                true
        );

        return executeWithRetry(() -> {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<ClassificationRequest> requestEntity = new HttpEntity<>(requestBody, headers);

            ResponseEntity<ClassificationResponse> response = nlpCloudRestTemplate.postForEntity(
                    "/classification",
                    requestEntity,
                    ClassificationResponse.class
            );

            return response.getBody();
        });
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
                    "Upstream service responded with status " + responseException.getStatusCode(),
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
