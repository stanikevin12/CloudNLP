package com.example.demo.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;

@ConfigurationProperties(prefix = "nlpcloud")
public class NlpCloudProperties {

    private String apiKey;
    private String baseUrl = "https://api.nlpcloud.io/v1";
    private Duration timeout = Duration.ofSeconds(5);
    private int maxRetries = 2;
    private Models models = new Models();

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

    public Models getModels() {
        return models;
    }

    public void setModels(Models models) {
        this.models = models;
    }

    public static class Models {

        private String grammar;
        private String entities;
        private String summarize;
        private String keywords;
        private String classification;

        public String getGrammar() {
            return grammar;
        }

        public void setGrammar(String grammar) {
            this.grammar = grammar;
        }

        public String getEntities() {
            return entities;
        }

        public void setEntities(String entities) {
            this.entities = entities;
        }

        public String getSummarize() {
            return summarize;
        }

        public void setSummarize(String summarize) {
            this.summarize = summarize;
        }

        public String getKeywords() {
            return keywords;
        }

        public void setKeywords(String keywords) {
            this.keywords = keywords;
        }

        public String getClassification() {
            return classification;
        }

        public void setClassification(String classification) {
            this.classification = classification;
        }
    }
}
