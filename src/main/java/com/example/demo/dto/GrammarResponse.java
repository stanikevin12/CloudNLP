package com.example.demo.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "Grammar correction output")
public class GrammarResponse {
    @Schema(description = "Text after grammar correction", example = "This is the corrected text.")
    private String correctedText;

    private List<Suggestion> suggestions;

    public GrammarResponse() {
    }

    public GrammarResponse(String correctedText, List<Suggestion> suggestions) {
        this.correctedText = correctedText;
        this.suggestions = suggestions;
    }

    public String getCorrectedText() {
        return correctedText;
    }

    public void setCorrectedText(String correctedText) {
        this.correctedText = correctedText;
    }

    public List<Suggestion> getSuggestions() {
        return suggestions;
    }

    public void setSuggestions(List<Suggestion> suggestions) {
        this.suggestions = suggestions;
    }
}
