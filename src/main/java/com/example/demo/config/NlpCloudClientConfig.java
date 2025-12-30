package com.example.demo.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

import java.util.Objects;

@Configuration
public class NlpCloudClientConfig {

    private static final Logger log = LoggerFactory.getLogger(NlpCloudClientConfig.class);

    @Bean
    public RestTemplate nlpCloudRestTemplate(NlpCloudProperties properties, RestTemplateBuilder builder) {
        String model = sanitize(properties.getModel());

        validateModel(model);

        log.info("Configuring NLP Cloud client with base URL '{}' and model '{}'", properties.getBaseUrl(), model);

        return builder
                .rootUri(properties.getBaseUrl() + "/" + model)
                .setConnectTimeout(properties.getTimeout())
                .setReadTimeout(properties.getTimeout())
                .build();
    }

    private String sanitize(String value) {
        return value == null ? null : value.trim();
    }

    private void validateModel(String model) {
        if (model == null || model.isBlank()) {
            throw new IllegalStateException("NLP Cloud model is missing. Please configure 'nlpcloud.model' with an active model.");
        }
    }
}
