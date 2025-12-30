package com.example.demo.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

@Configuration
public class NlpCloudClientConfig {

    private static final Logger log = LoggerFactory.getLogger(NlpCloudClientConfig.class);

    @Bean
    public RestTemplate nlpCloudRestTemplate(NlpCloudProperties properties, RestTemplateBuilder builder) {
        String baseUrl = normalizeBaseUrl(properties.getBaseUrl());

        log.info("Configuring NLP Cloud client with base URL '{}' and task models {}", baseUrl, describeModels(properties));

        return builder
                .rootUri(baseUrl)
                .setConnectTimeout(properties.getTimeout())
                .setReadTimeout(properties.getTimeout())
                .build();
    }

    private String normalizeBaseUrl(String baseUrl) {
        if (baseUrl == null) {
            return "";
        }
        String sanitized = baseUrl.trim();
        if (sanitized.endsWith("/")) {
            sanitized = sanitized.substring(0, sanitized.length() - 1);
        }
        if (!sanitized.endsWith("/v1")) {
            sanitized = sanitized + "/v1";
        }
        return sanitized;
    }

    private String describeModels(NlpCloudProperties properties) {
        NlpCloudProperties.Models models = properties.getModels();
        return String.format("{grammar=%s, entities=%s, summarize=%s, keywords=%s, classification=%s}",
                safeValue(models.getGrammar()),
                safeValue(models.getEntities()),
                safeValue(models.getSummarize()),
                safeValue(models.getKeywords()),
                safeValue(models.getClassification()));
    }

    private String safeValue(String value) {
        return value == null ? "" : value.trim();
    }
}
