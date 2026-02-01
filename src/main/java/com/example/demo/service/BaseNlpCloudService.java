package com.example.demo.service;

import com.example.demo.config.NlpCloudProperties;
import com.example.demo.exception.UpstreamServiceException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

import java.util.Map;
import java.util.function.Supplier;

public abstract class BaseNlpCloudService {

    private static final Logger log = LoggerFactory.getLogger(BaseNlpCloudService.class);

    protected final RestTemplate restTemplate;
    protected final NlpCloudProperties properties;

    protected BaseNlpCloudService(RestTemplate restTemplate, NlpCloudProperties properties) {
        this.restTemplate = restTemplate;
        this.properties = properties;
    }

    protected HttpEntity<Map<String, ?>> buildRequest(Map<String, ?> payload) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set(HttpHeaders.AUTHORIZATION, "Token " + properties.getApiKey());
        return new HttpEntity<>(payload, headers);
    }

    /**
     * Executes an NLP Cloud call with safe retry semantics.
     *
     * Retry rules:
     * - NEVER retry HTTP 429 (rate limit)
     * - Retry only transient failures (network / 5xx)
     */
   protected <T> T executeWithRetry(Supplier<T> supplier) {
    int maxRetries = Math.max(properties.getMaxRetries(), 0);
    long delayMs = 500;

    for (int attempt = 0; attempt <= maxRetries; attempt++) {
        try {
            return supplier.get();

        } catch (HttpStatusCodeException ex) {

            var status = ex.getStatusCode(); // HttpStatusCode

            // 429 = hard stop (rate limit)
            if (status.value() == 429) {
                log.warn("NLP Cloud rate limit exceeded (HTTP 429). No retry will be attempted.");
                throw new UpstreamServiceException(
                        "NLP Cloud rate limit exceeded. Please retry later.", ex
                );
            }

            // Retry only on 5xx
            if (status.is5xxServerError() && attempt < maxRetries) {
                log.warn(
                    "Transient NLP Cloud error ({}). Retrying attempt {}/{} after {}ms",
                    status.value(), attempt + 1, maxRetries, delayMs
                );
                sleep(delayMs);
                delayMs *= 2;
                continue;
            }

            // All other HTTP errors → fail fast
            throw new UpstreamServiceException(
                    "NLP Cloud request failed with status " + status.value(), ex
            );

        } catch (ResourceAccessException ex) {

            // Network / timeout errors
            if (attempt < maxRetries) {
                log.warn(
                    "NLP Cloud network error. Retrying attempt {}/{} after {}ms",
                    attempt + 1, maxRetries, delayMs
                );
                sleep(delayMs);
                delayMs *= 2;
                continue;
            }

            throw new UpstreamServiceException(
                    "Unable to reach NLP Cloud service. Please try again later.", ex
            );
        }
    }

    throw new UpstreamServiceException("NLP Cloud request failed after retries.");
}


    private void sleep(long delayMs) {
        try {
            Thread.sleep(delayMs);
        } catch (InterruptedException ie) {
            Thread.currentThread().interrupt();
        }
    }
}
