package com.example.demo.service;

import com.example.demo.config.NlpCloudProperties;
import com.example.demo.exception.UpstreamServiceException;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.client.RestTemplate;

import java.util.Map;
import java.util.function.Supplier;

public abstract class BaseNlpCloudService {

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

    protected <T> T executeWithRetry(Supplier<T> action) {
        int attempts = Math.min(Math.max(properties.getMaxRetries(), 1), 3);
        RuntimeException last = null;

        for (int i = 1; i <= attempts; i++) {
            try {
                return action.get();
            } catch (RuntimeException ex) {
                last = ex;
                try { Thread.sleep(300); } catch (InterruptedException ignored) {}
            }
        }
        throw new UpstreamServiceException("Unable to process NLP request", last);
    }
}
