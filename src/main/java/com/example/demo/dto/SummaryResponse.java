package com.example.demo.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "Summarization output")
public class SummaryResponse {
    @Schema(description = "Summary text returned by the model", example = "This is a concise summary of the report.")
    private String summary;

    @Schema(description = "Optional key findings extracted during summarization", example = "[\"Finding one\", \"Finding two\"]")
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
