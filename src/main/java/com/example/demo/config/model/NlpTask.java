package com.example.demo.config.model;

public enum NlpTask {
    GRAMMAR("grammar"),
    ENTITIES("entities"),
    SUMMARIZE("summarize"),
    KEYWORDS("keywords"),
    CLASSIFICATION("classification");

    private final String endpoint;

    NlpTask(String endpoint) {
        this.endpoint = endpoint;
    }

    public String endpoint() {
        return endpoint;
    }
}
