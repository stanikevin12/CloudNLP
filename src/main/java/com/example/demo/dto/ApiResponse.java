package com.example.demo.dto;

import java.time.Instant;
import java.util.UUID;

public class ApiResponse<T> {

    private String requestId;
    private Instant timestamp;
    private long processingTimeMs;
    private T data;

    public ApiResponse() {
    }

    public ApiResponse(String requestId, Instant timestamp, long processingTimeMs, T data) {
        this.requestId = requestId;
        this.timestamp = timestamp;
        this.processingTimeMs = processingTimeMs;
        this.data = data;
    }

    public static <T> ApiResponse<T> fromPayload(T payload, long startTimeMs) {
        Instant now = Instant.now();
        long elapsed = Math.max(0, System.currentTimeMillis() - startTimeMs);
        return new ApiResponse<>(UUID.randomUUID().toString(), now, elapsed, payload);
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
