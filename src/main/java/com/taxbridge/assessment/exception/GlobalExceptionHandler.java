package com.taxbridge.assessment.exception;

import com.taxbridge.assessment.common.ApiResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletResponse;

/**
 * Handles all application exceptions and converts them
 * to the standard API response envelope.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * Handle rate limit exceeded.
     */
    @ExceptionHandler(RateLimitExceededException.class)
    public ResponseEntity<ApiResponse<Object>> handleRateLimitExceeded(
            RateLimitExceededException ex,
            HttpServletResponse response
    ) {

        response.setHeader("Retry-After", String.valueOf(ex.getRetryAfterSeconds()));

        ApiResponse<Object> body =
                ApiResponse.error(
                        ex.getMessage(),
                        "RATE_LIMIT_EXCEEDED"
                );

        return ResponseEntity
                .status(HttpStatus.TOO_MANY_REQUESTS)
                .body(body);
    }

    /**
     * Handle tenant not found.
     */
    @ExceptionHandler(TenantNotFoundException.class)
    public ResponseEntity<ApiResponse<Object>> handleTenantNotFound(
            TenantNotFoundException ex
    ) {

        ApiResponse<Object> body =
                ApiResponse.error(
                        ex.getMessage(),
                        "TENANT_NOT_FOUND"
                );

        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(body);
    }

    /**
     * Handle unauthorized requests.
     */
    @ExceptionHandler(UnauthorizedException.class)
    public ResponseEntity<ApiResponse<Object>> handleUnauthorized(
            UnauthorizedException ex
    ) {

        ApiResponse<Object> body =
                ApiResponse.error(
                        ex.getMessage(),
                        "UNAUTHORIZED"
                );

        return ResponseEntity
                .status(HttpStatus.UNAUTHORIZED)
                .body(body);
    }

    /**
     * Handle all unexpected exceptions.
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Object>> handleGeneric(
            Exception ex
    ) {

        ApiResponse<Object> body =
                ApiResponse.error(
                        "An unexpected error occurred",
                        "INTERNAL_SERVER_ERROR"
                );

        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(body);
    }
}