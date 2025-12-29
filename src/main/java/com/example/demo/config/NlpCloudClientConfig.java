package com.example.demo.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.web.client.RestTemplate;

@Configuration
public class NlpCloudClientConfig {

    @Bean
    public RestTemplate nlpCloudRestTemplate(NlpCloudProperties properties, RestTemplateBuilder builder) {
        return builder
                .rootUri(properties.getBaseUrl() + "/" + properties.getModel())
                .defaultHeader(HttpHeaders.AUTHORIZATION, "Token " + properties.getApiKey())
                .setConnectTimeout(properties.getTimeout())
                .setReadTimeout(properties.getTimeout())
                .build();
    }
}
