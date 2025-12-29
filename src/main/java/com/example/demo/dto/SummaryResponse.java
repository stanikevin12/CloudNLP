package com.example.demo.dto;

import java.util.List;

public class SummaryResponse {
    private String summary;
    private List<String> keyFindings;

    public SummaryResponse() {
    }

    public SummaryResponse(String summary, List<String> keyFindings) {
        this.summary = summary;
        this.keyFindings = keyFindings;
    }

    public String getSummary() {
        return summary;
    }

    public List<String> getKeyFindings() {
        return keyFindings;
    }
}
