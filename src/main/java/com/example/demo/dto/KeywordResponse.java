package com.example.demo.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "Keyword extraction output")
public class KeywordResponse {
    @Schema(description = "List of extracted keywords", example = "[\"nlp\", \"cloud\", \"api\"]")
    private List<String> keywords;

    public KeywordResponse() {
    }

    public KeywordResponse(List<String> keywords) {
        this.keywords = keywords;
    }

    public List<String> getKeywords() {
        return keywords;
    }

    public void setKeywords(List<String> keywords) {
        this.keywords = keywords;
    }
}
