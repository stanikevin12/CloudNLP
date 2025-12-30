package com.example.demo.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;

@ConfigurationProperties(prefix = "nlpcloud")
public class NlpCloudProperties {

    private String apiKey;
    private String baseUrl = "https://api.nlpcloud.io";
    private Duration timeout = Duration.ofSeconds(5);
    private int maxRetries = 3;
    private String summarizationModel;
    private String summarizationEndpoint = "/summarization";

    public String getApiKey() {
        return apiKey;
    }

    public void setApiKey(String apiKey) {
        this.apiKey = apiKey;
    }

    public String getBaseUrl() {
        return baseUrl;
    }

    public void setBaseUrl(String baseUrl) {
        this.baseUrl = baseUrl;
    }

    public Duration getTimeout() {
        return timeout;
    }

    public void setTimeout(Duration timeout) {
        this.timeout = timeout;
    }

    public int getMaxRetries() {
        return maxRetries;
    }

    public void setMaxRetries(int maxRetries) {
        this.maxRetries = maxRetries;
    }

    public String getSummarizationModel() {
        return summarizationModel;
    }

    public void setSummarizationModel(String summarizationModel) {
        this.summarizationModel = summarizationModel;
    }

    public String getSummarizationEndpoint() {
        return summarizationEndpoint;
    }

    public void setSummarizationEndpoint(String summarizationEndpoint) {
        this.summarizationEndpoint = summarizationEndpoint;
    }
}
