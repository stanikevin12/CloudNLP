package com.example.demo.dto;

import java.util.List;

public class KeywordResponse {
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
