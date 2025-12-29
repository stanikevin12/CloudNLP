package com.example.demo.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;

@Schema(description = "Standard API envelope containing metadata and response payload")
public class ApiResult<T> {

    public static final String MEDICAL_DISCLAIMER = "This tool does not provide diagnosis.";

    @Schema(description = "Timestamp when the response was generated", example = "2024-01-01T12:00:00Z")
    private Instant timestamp;

    @Schema(description = "HTTP status code of the response", example = "200")
    private int status;

    @Schema(description = "Request path", example = "/api/nlp/grammar")
    private String path;

    @Schema(description = "Payload for successful responses")
    private T data;

    @Schema(description = "Error details when a request fails")
    private ErrorResponse error;

    @Schema(description = "Medical safety disclaimer", example = "This tool does not provide diagnosis.")
    private String medicalDisclaimer;

    public ApiResult() {
    }

    private ApiResult(Instant timestamp, int status, String path, T data, ErrorResponse error, String medicalDisclaimer) {
        this.timestamp = timestamp;
        this.status = status;
        this.path = path;
        this.data = data;
        this.error = error;
        this.medicalDisclaimer = medicalDisclaimer;
    }

    public static <T> ApiResult<T> success(int status, String path, T payload) {
        return new ApiResult<>(Instant.now(), status, path, payload, null, MEDICAL_DISCLAIMER);
    }

    public static <T> ApiResult<T> error(int status, String path, String message) {
        return new ApiResult<>(Instant.now(), status, path, null, new ErrorResponse(message), MEDICAL_DISCLAIMER);
    }

    public Instant getTimestamp() {
        return timestamp;
    }

    public int getStatus() {
        return status;
    }

    public String getPath() {
        return path;
    }

    public String getDisclaimer() {
        return medicalDisclaimer;
    }

    public T getData() {
        return data;
    }

    public ErrorResponse getError() {
        return error;
    }

    public String getMedicalDisclaimer() {
        return medicalDisclaimer;
    }
}
