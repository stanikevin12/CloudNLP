package com.example.demo.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Suggestion returned by grammar correction")
public class Suggestion {
    @Schema(description = "Type of correction", example = "spelling")
    private String type;

    @Schema(description = "Original text span", example = "Ths")
    private String text;

    @Schema(description = "Suggested replacement", example = "This")
    private String suggestion;

    @Schema(description = "Start offset of the suggestion", example = "0")
    private int start;

    @Schema(description = "End offset of the suggestion", example = "3")
    private int end;

    public Suggestion() {
    }

    public Suggestion(String type, String text, String suggestion, int start, int end) {
        this.type = type;
        this.text = text;
        this.suggestion = suggestion;
        this.start = start;
        this.end = end;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getSuggestion() {
        return suggestion;
    }

    public void setSuggestion(String suggestion) {
        this.suggestion = suggestion;
    }

    public int getStart() {
        return start;
    }

    public void setStart(int start) {
        this.start = start;
    }

    public int getEnd() {
        return end;
    }

    public void setEnd(int end) {
        this.end = end;
    }
}
