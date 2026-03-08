package com.taxbridge.assessment.ratelimit;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FixedWindowRateLimiterTest {

    @Mock
    private StringRedisTemplate redisTemplate;

    @Mock
    private ValueOperations<String, String> valueOperations;

    @InjectMocks
    private FixedWindowRateLimiter rateLimiter;

    @BeforeEach
    void setup() {
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
    }

    /**
     * Test 1
     * First request in window -> allowed
     * increment() returns 1
     * expire() must be called
     */
    @Test
    void firstRequest_allowed() {

        when(valueOperations.increment(anyString())).thenReturn(1L);
        when(redisTemplate.getExpire(anyString(), eq(TimeUnit.SECONDS))).thenReturn(55L);

        RateLimitResult result =
                rateLimiter.isAllowed("tenant1", 10, 60);

        verify(redisTemplate).expire(anyString(), eq(60L), eq(TimeUnit.SECONDS));

        assertTrue(result.allowed());
        assertEquals(1, result.currentCount());
    }

    /**
     * Test 2
     * Under limit -> allowed
     * increment() returns 5
     * expire() must NOT be called
     */
    @Test
    void underLimit_allowed_notFirstRequest() {

        when(valueOperations.increment(anyString())).thenReturn(5L);

        RateLimitResult result =
                rateLimiter.isAllowed("tenant1", 10, 60);

        verify(redisTemplate, never())
                .expire(anyString(), anyLong(), any());

        assertTrue(result.allowed());
        assertEquals(5, result.currentCount());
    }

    /**
     * Test 3
     * Limit exceeded -> denied
     */
    @Test
    void atLimit_denied() {

        when(valueOperations.increment(anyString())).thenReturn(11L);
        when(redisTemplate.getExpire(anyString(), eq(TimeUnit.SECONDS))).thenReturn(30L);

        RateLimitResult result =
                rateLimiter.isAllowed("tenant1", 10, 60);

        assertFalse(result.allowed());
        assertEquals(30, result.retryAfterSeconds());
    }

    /**
     * Test 4
     * TTL fallback when Redis returns -1
     */
    @Test
    void nullTTL_fallbackToWindowSeconds() {

        when(valueOperations.increment(anyString())).thenReturn(11L);
        when(redisTemplate.getExpire(anyString(), eq(TimeUnit.SECONDS))).thenReturn(-1L);

        RateLimitResult result =
                rateLimiter.isAllowed("tenant1", 10, 60);

        assertEquals(60, result.retryAfterSeconds());
    }
}