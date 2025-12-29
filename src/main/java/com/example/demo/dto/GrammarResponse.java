package com.example.demo.dto;

import java.util.List;

public class GrammarResponse {
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
