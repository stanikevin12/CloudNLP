package com.example.demo.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Named entity recognized in the text")
public class Entity {
    @Schema(description = "Entity label or type", example = "person")
    private String entity;

    @Schema(description = "Text span that was matched", example = "John Doe")
    private String text;

    @Schema(description = "Start offset of the entity", example = "0")
    private int start;

    @Schema(description = "End offset of the entity", example = "8")
    private int end;

    @Schema(description = "Confidence score for the match", example = "0.98")
    private double confidence;

    public Entity() {
    }

    public Entity(String entity, String text, int start, int end, double confidence) {
        this.entity = entity;
        this.text = text;
        this.start = start;
        this.end = end;
        this.confidence = confidence;
    }

    public String getEntity() {
        return entity;
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
