package com.example.demo.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;
import java.util.UUID;

@Schema(description = "Standard API envelope containing metadata and response payload")
public class ApiResult<T> {

    @Schema(description = "Unique request identifier", example = "c1db6b9e-5bfa-4db3-9b65-1234567890ab")
    private String requestId;

    @Schema(description = "Timestamp when the response was generated", example = "2024-01-01T12:00:00Z")
    private Instant timestamp;

    @Schema(description = "Processing duration in milliseconds", example = "123")
    private long processingTimeMs;

    private T data;

    public ApiResult() {
    }

    public ApiResult(String requestId, Instant timestamp, long processingTimeMs, T data) {
        this.requestId = requestId;
        this.timestamp = timestamp;
        this.processingTimeMs = processingTimeMs;
        this.data = data;
    }

    public static <T> ApiResult<T> fromPayload(T payload, long startTimeMs) {
        Instant now = Instant.now();
        long elapsed = Math.max(0, System.currentTimeMillis() - startTimeMs);
        return new ApiResult<>(UUID.randomUUID().toString(), now, elapsed, payload);
    }

    public String getRequestId() {
        return requestId;
    }

    public Instant getTimestamp() {
        return timestamp;
    }

    public long getProcessingTimeMs() {
        return processingTimeMs;
    }

    public T getData() {
        return data;
    }
}
