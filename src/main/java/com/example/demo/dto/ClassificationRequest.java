package com.example.demo.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;

import java.util.List;

@Schema(description = "Request body for text classification")
public class ClassificationRequest {
    @NotBlank(message = "Text is required for classification")
    @Size(max = 5000, message = "Text must be 5,000 characters or fewer")
    @Schema(description = "Free text to classify", example = "The rover touched down on Mars this morning")
    private String text;

    @NotEmpty(message = "At least one label must be provided")
    @Schema(description = "Candidate labels for the zero-shot classifier", example = "[\"space\", \"science\", \"technology\"]")
    private List<String> labels;

    @Schema(description = "Allow multiple labels to be returned", example = "true")
    private boolean multi_class;

    public ClassificationRequest() {
    }

    public ClassificationRequest(String text, List<String> labels, boolean multi_class) {
        this.text = text;
        this.labels = labels;
        this.multi_class = multi_class;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public List<String> getLabels() {
        return labels;
    }

    public void setLabels(List<String> labels) {
        this.labels = labels;
    }

    public boolean isMulti_class() {
        return multi_class;
    }

    public void setMulti_class(boolean multi_class) {
        this.multi_class = multi_class;
    }
}
