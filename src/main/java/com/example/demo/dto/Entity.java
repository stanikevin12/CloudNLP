package com.example.demo.dto;

public class Entity {
    private String type;
    private String text;
    private int start;
    private int end;
    private double confidence;

    public Entity() {
    }

    public Entity(String type, String text, int start, int end, double confidence) {
        this.type = type;
        this.text = text;
        this.start = start;
        this.end = end;
        this.confidence = confidence;
    }

    public String getType() {
        return type;
    }

    public String getText() {
        return text;
    }

    public int getStart() {
        return start;
    }

    public int getEnd() {
        return end;
    }

    public double getConfidence() {
        return confidence;
    }
}
