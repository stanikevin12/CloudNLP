package com.example.demo.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import jakarta.validation.constraints.NotBlank;
import java.time.Duration;

@Validated
@ConfigurationProperties(prefix = "nlpcloud")
public class NlpCloudProperties {

    // ===== Common =====

    @NotBlank
    private String apiKey;

    private String baseUrl = "https://api.nlpcloud.io/v1";

    private Duration timeout = Duration.ofSeconds(5);

    private int maxRetries = 3;

    // Keywords
    private String keywordModel;
    private String keywordEndpoint;

    // Grammar
    private String grammarModel;
    private String grammarEndpoint;

    // ===== Summarization =====
    @NotBlank
    private String summarizationModel;

    private String summarizationEndpoint = "/summarization";

    // ===== Entity Extraction (NER) =====

    @NotBlank
    private String entityModel;

    private String entityEndpoint = "/entities";

    // ===== Getters / Setters =====

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

    public String getKeywordModel() {
        return keywordModel;
    }

    public void setKeywordModel(String keywordModel) {
        this.keywordModel = keywordModel;
    }

    public String getKeywordEndpoint() {
        return keywordEndpoint;
    }

    public void setKeywordEndpoint(String keywordEndpoint) {
        this.keywordEndpoint = keywordEndpoint;
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

    public String getEntityModel() {
        return entityModel;
    }

    public void setEntityModel(String entityModel) {
        this.entityModel = entityModel;
    }

    public String getEntityEndpoint() {
        return entityEndpoint;
    }

    public void setEntityEndpoint(String entityEndpoint) {
        this.entityEndpoint = entityEndpoint;
    }

    public String getGrammarModel() {
        return grammarModel;
    }

    public void setGrammarModel(String grammarModel) {
        this.grammarModel = grammarModel;
    }

    public String getGrammarEndpoint() {
        return grammarEndpoint;
    }

    public void setGrammarEndpoint(String grammarEndpoint) {
        this.grammarEndpoint = grammarEndpoint;
    }
}
