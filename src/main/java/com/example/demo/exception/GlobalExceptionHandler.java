package com.example.demo.exception;

import com.example.demo.dto.ApiResult;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.client.RestClientException;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);
    private static final String GENERIC_UPSTREAM_MESSAGE = "Unable to process NLP request at this time. Please try again later.";

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResult<Void>> handleValidation(MethodArgumentNotValidException ex, HttpServletRequest request) {
        String message = ex.getBindingResult().getAllErrors().stream()
                .map(DefaultMessageSourceResolvable::getDefaultMessage)
                .collect(Collectors.joining("; "));
        log.warn("Validation failed for {}: {}", request.getRequestURI(), message);
        return build(HttpStatus.BAD_REQUEST, message, request);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ApiResult<Void>> handleConstraintViolation(ConstraintViolationException ex, HttpServletRequest request) {
        String message = ex.getConstraintViolations().stream()
                .map(violation -> violation.getPropertyPath() + ": " + violation.getMessage())
                .collect(Collectors.joining("; "));
        log.warn("Constraint violation for {}: {}", request.getRequestURI(), message);
        return build(HttpStatus.BAD_REQUEST, message, request);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiResult<Void>> handleUnreadable(HttpMessageNotReadableException ex, HttpServletRequest request) {
        log.warn("Unreadable request body at {}: {}", request.getRequestURI(), ex.getClass().getSimpleName());
        return build(HttpStatus.BAD_REQUEST, "Request body could not be parsed", request);
    }

    @ExceptionHandler(UpstreamServiceException.class)
    public ResponseEntity<ApiResult<Void>> handleUpstream(UpstreamServiceException ex, HttpServletRequest request) {
        log.warn("Upstream service error at {}: {}", request.getRequestURI(), ex.getMessage());
        return build(HttpStatus.BAD_GATEWAY, ex.getMessage(), request);
    }

    @ExceptionHandler(RestClientException.class)
    public ResponseEntity<ApiResult<Void>> handleRestClient(RestClientException ex, HttpServletRequest request) {
        log.warn("Rest client exception at {}: {}", request.getRequestURI(), ex.getClass().getSimpleName());
        return build(HttpStatus.BAD_GATEWAY, GENERIC_UPSTREAM_MESSAGE, request);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResult<Void>> handleGeneric(Exception ex, HttpServletRequest request) {
        log.warn("Unexpected exception at {}: {}", request.getRequestURI(), ex.getClass().getSimpleName());
        return build(HttpStatus.INTERNAL_SERVER_ERROR, "An unexpected error occurred", request);
    }

    private ResponseEntity<ApiResult<Void>> build(HttpStatus status, String message, HttpServletRequest request) {
        String path = request != null ? request.getRequestURI() : "N/A";
        ApiResult<Void> body = ApiResult.error(status.value(), path, message);
        return ResponseEntity.status(status).body(body);
    }
}
