package com.taxbridge.assessment.ratelimit;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

@Component
public class FixedWindowRateLimiter {

    private final StringRedisTemplate redisTemplate;

    public FixedWindowRateLimiter(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public RateLimitResult isAllowed(String tenantId,
                                     int maxRequests,
                                     int windowSeconds) {

        // Step 1 — calculate window start
        long windowStart =
                (System.currentTimeMillis() / 1000L)
                        / windowSeconds * windowSeconds;

        // Step 2 — build redis key
        String key = "rate_limit:" + tenantId + ":" + windowStart;

        // Step 3 — increment request counter
        Long count = redisTemplate.opsForValue().increment(key);

        if (count == null) {
            count = 1L;
        }

        // Step 4 — set TTL only for first request
        if (count == 1L) {
            redisTemplate.expire(key, windowSeconds, TimeUnit.SECONDS);
        }

        // Step 5 — get remaining TTL
        Long ttl = redisTemplate.getExpire(key, TimeUnit.SECONDS);

        int retryAfter =
                (ttl == null || ttl < 0)
                        ? windowSeconds
                        : ttl.intValue();

        // Step 6 — allow or deny
        if (count <= maxRequests) {
            return new RateLimitResult(true, 0, count, maxRequests);
        } else {
            return new RateLimitResult(false, retryAfter, count, maxRequests);
        }
    }
}