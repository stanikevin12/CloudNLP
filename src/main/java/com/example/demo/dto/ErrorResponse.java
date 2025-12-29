package com.example.demo.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Error payload nested inside the API response envelope")
public class ErrorResponse {

    @Schema(description = "Human-readable error description", example = "Clinical note must be between 20 and 8,000 characters to ensure sufficient clinical context")
    private String message;

    public ErrorResponse() {
    }

    public ErrorResponse(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }
}
