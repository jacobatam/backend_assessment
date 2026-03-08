package com.taxbridge.assessment.common;

import java.time.Instant;

/**
 * Generic API response wrapper used to standardize all HTTP responses
 * returned by the application.
 *
 * This ensures that every API response follows the same structure.
 *
 * Success Example:
 * {
 *   "success": true,
 *   "message": "Operation completed successfully",
 *   "data": {...},
 *   "timestamp": "2025-12-25T10:00:00Z"
 * }
 *
 * Error Example:
 * {
 *   "success": false,
 *   "message": "Rate limit exceeded",
 *   "errorCode": "RATE_LIMIT_EXCEEDED",
 *   "timestamp": "2025-12-25T10:00:00Z"
 * }
 */
public class ApiResponse<T> {

    private boolean success;
    private String message;
    private T data;
    private String errorCode;
    private Instant timestamp;

    /**
     * Default constructor sets timestamp automatically.
     */
    public ApiResponse() {
        this.timestamp = Instant.now();
    }

    /**
     * Creates a successful API response.
     */
    public static <T> ApiResponse<T> success(String message, T data) {

        ApiResponse<T> response = new ApiResponse<>();

        response.success = true;
        response.message = message;
        response.data = data;

        return response;
    }

    /**
     * Creates an error API response.
     */
    public static <T> ApiResponse<T> error(String message, String errorCode) {

        ApiResponse<T> response = new ApiResponse<>();

        response.success = false;
        response.message = message;
        response.errorCode = errorCode;

        return response;
    }

    public boolean isSuccess() {
        return success;
    }

    public String getMessage() {
        return message;
    }

    public T getData() {
        return data;
    }

    public String getErrorCode() {
        return errorCode;
    }

    public Instant getTimestamp() {
        return timestamp;
    }
}