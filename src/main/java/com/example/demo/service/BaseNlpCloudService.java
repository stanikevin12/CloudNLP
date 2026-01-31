package com.example.demo.service;

import com.example.demo.config.NlpCloudProperties;
import com.example.demo.exception.UpstreamServiceException;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;

import java.util.Map;
import java.util.function.Supplier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

    protected <T> T executeWithRetry(Supplier<T> supplier) {
        int maxRetries = 5;
        long delay = 3000; // 1 second
        for (int i = 1; i <= maxRetries; i++) {
            try {
                return supplier.get();
            } catch (HttpStatusCodeException ex) {
                if (ex.getStatusCode() == HttpStatus.TOO_MANY_REQUESTS) {

                    log.info("Rate limit hit, retrying in {}ms (attempt {}/{})", delay, i, maxRetries);
                    try {
                        Thread.sleep(delay);
                    } catch (InterruptedException ignored) {
                    }
                    delay *= 2; // exponential backoff
                } else {
                    throw ex;
                }
            }
        }
        throw new RuntimeException("Exceeded max retries due to rate limiting");
    }
}