package com.taxbridge.assessment.ratelimit;

/**
 * Represents the result of a rate limit check.
 *
 * This record contains information about whether the request
 * is allowed and details about the current rate limit window.
 */
public record RateLimitResult(

        // Whether the request is allowed
        boolean allowed,

        // Seconds remaining until the current window expires
        int retryAfterSeconds,

        // Current number of requests made in the window
        long currentCount,

        // Maximum allowed requests for the tenant
        long limit

) {}