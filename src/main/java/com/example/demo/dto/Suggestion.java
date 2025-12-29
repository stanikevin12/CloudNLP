package com.example.demo.dto;

public class Suggestion {
    private String type;
    private String text;
    private String suggestion;
    private int start;
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

    public String getText() {
        return text;
    }

    public String getSuggestion() {
        return suggestion;
    }

    public int getStart() {
        return start;
    }

    public int getEnd() {
        return end;
    }
}
