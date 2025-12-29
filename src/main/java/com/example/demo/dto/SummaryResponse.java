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

    public void setSummary(String summary) {
        this.summary = summary;
    }

    public List<String> getKeyFindings() {
        return keyFindings;
    }

    public void setKeyFindings(List<String> keyFindings) {
        this.keyFindings = keyFindings;
    }
}
