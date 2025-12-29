package com.example.demo.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;

@Schema(description = "Standardized error payload for API responses")
public class ErrorResponse {

    @Schema(description = "Machine-readable error code", example = "VALIDATION_ERROR")
    private String error;

    @Schema(description = "Human-readable error description", example = "Clinical note is required")
    private String message;

    @Schema(description = "Timestamp of when the error occurred", example = "2024-01-01T12:00:00Z")
    private Instant timestamp;

    public ErrorResponse() {
    }

    public ErrorResponse(String error, String message, Instant timestamp) {
        this.error = error;
        this.message = message;
        this.timestamp = timestamp;
    }

    public String getError() {
        return error;
    }

    public String getMessage() {
        return message;
    }

    public Instant getTimestamp() {
        return timestamp;
    }
}
