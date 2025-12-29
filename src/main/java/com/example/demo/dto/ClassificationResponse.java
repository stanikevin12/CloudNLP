package com.example.demo.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "Zero-shot classification output")
public class ClassificationResponse {
    @Schema(description = "Labels returned by the classifier", example = "[\"space\", \"science\", \"technology\"]")
    private List<String> labels;

    @Schema(description = "Confidence scores for each label", example = "[0.83, 0.10, 0.07]")
    private List<Double> scores;

    public ClassificationResponse() {
    }

    public List<String> getLabels() {
        return labels;
    }

    public void setLabels(List<String> labels) {
        this.labels = labels;
    }

    public List<Double> getScores() {
        return scores;
    }

    public void setScores(List<Double> scores) {
        this.scores = scores;
    }
}
