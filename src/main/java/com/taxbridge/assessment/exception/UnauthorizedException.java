package com.taxbridge.assessment.exception;

/**
 * Thrown when a request does not contain a valid JWT token
 * or authorization header.
 */
public class UnauthorizedException extends RuntimeException {

    public UnauthorizedException(String message) {
        super(message);
    }
}