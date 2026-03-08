package com.taxbridge.assessment.exception;

/**
 * Thrown when a tenant configuration cannot be found
 * in the database.
 */
public class TenantNotFoundException extends RuntimeException {

    public TenantNotFoundException(String message) {
        super(message);
    }
}