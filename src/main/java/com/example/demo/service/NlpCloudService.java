package com.example.demo.service;

import com.example.demo.config.NlpCloudProperties;
import com.example.demo.dto.ClassificationRequest;
import com.example.demo.dto.ClassificationResponse;
import com.example.demo.exception.UpstreamServiceException;
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
import java.util.List;
import java.util.concurrent.TimeoutException;

@Service
public class NlpCloudService {

    private static final Logger log = LoggerFactory.getLogger(NlpCloudService.class);
    private static final String SAFE_UPSTREAM_MESSAGE = "Unable to process NLP request at this time. Please try again later.";

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
        int attempts = Math.min(properties.getMaxRetries(), 2) + 1;
        for (int attempt = 1; attempt <= attempts; attempt++) {
            try {
                return action.get();
            } catch (Exception ex) {
                boolean retryable = attempt < attempts && isRetryable(ex);
                log.warn("Attempt {}/{} failed calling /classification: {}", attempt, attempts, ex.getClass().getSimpleName());
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
}
