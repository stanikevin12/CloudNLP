package com.example.demo.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;

@Schema(description = "Standardized error payload for API responses")
public class ErrorResponse {

    @Schema(description = "Timestamp of when the error occurred", example = "2024-01-01T12:00:00Z")
    private Instant timestamp;

    @Schema(description = "HTTP status code of the error", example = "400")
    private int status;

    @Schema(description = "Human-readable error description", example = "Clinical note must be between 20 and 8,000 characters to ensure sufficient clinical context")
    private String message;

    @Schema(description = "Request path where the error occurred", example = "/api/nlp/grammar")
    private String path;

    @Schema(description = "Medical safety disclaimer for all responses", example = "This tool does not provide diagnosis.")
    private String disclaimer;

    public ErrorResponse() {
    }

    public ErrorResponse(Instant timestamp, int status, String message, String path, String disclaimer) {
        this.timestamp = timestamp;
        this.status = status;
        this.message = message;
        this.path = path;
        this.disclaimer = disclaimer;
    }

    public Instant getTimestamp() {
        return timestamp;
    }

    public int getStatus() {
        return status;
    }

    public String getMessage() {
        return message;
    }

    public String getPath() {
        return path;
    }

    public String getDisclaimer() {
        return disclaimer;
    }
}
