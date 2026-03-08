package com.taxbridge.assessment.ratelimit;

import com.taxbridge.assessment.exception.RateLimitExceededException;
import com.taxbridge.assessment.security.JwtUtil;

import jakarta.servlet.http.HttpServletRequest;

import org.aspectj.lang.ProceedingJoinPoint;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RateLimitingAspectTest {

    @Mock
    private FixedWindowRateLimiter rateLimiter;

    @Mock
    private TenantRateLimitConfigService configService;

    @Mock
    private JwtUtil jwtUtil;

    @Mock
    private ProceedingJoinPoint joinPoint;

    @Mock
    private HttpServletRequest request;

    @InjectMocks
    private RateLimitingAspect aspect;

    private final UUID tenantId =
            UUID.fromString("a1000000-0000-0000-0000-000000000001");


    /**
     * Setup a fake HTTP request for RequestContextHolder
     */
    @BeforeEach
    void setupRequest() {

        when(request.getHeader("Authorization"))
                .thenReturn("Bearer token");

        ServletRequestAttributes attributes =
                new ServletRequestAttributes(request);

        RequestContextHolder.setRequestAttributes(attributes);
    }


    /**
     * Test 1
     * Request allowed → proceed() should be called
     */
    @Test
    void allowed_requestProceeds() throws Throwable {

        when(jwtUtil.extractTenantId(any()))
                .thenReturn(tenantId);

        TenantRateLimitConfig config = new TenantRateLimitConfig();
        config.setMaxRequests(10);
        config.setWindowSeconds(60);

        when(configService.getConfig(tenantId))
                .thenReturn(config);

        when(rateLimiter.isAllowed(any(), anyInt(), anyInt()))
                .thenReturn(new RateLimitResult(true, 0, 1, 10));

        aspect.rateLimit(joinPoint);

        verify(joinPoint).proceed();
    }


    /**
     * Test 2
     * Rate limit exceeded → exception thrown
     */
    @Test
    void denied_requestThrowsException() {

        when(jwtUtil.extractTenantId(any()))
                .thenReturn(tenantId);

        TenantRateLimitConfig config = new TenantRateLimitConfig();
        config.setMaxRequests(10);
        config.setWindowSeconds(60);

        when(configService.getConfig(tenantId))
                .thenReturn(config);

        when(rateLimiter.isAllowed(any(), anyInt(), anyInt()))
                .thenReturn(new RateLimitResult(false, 30, 11, 10));

        assertThrows(
                RateLimitExceededException.class,
                () -> aspect.rateLimit(joinPoint)
        );
    }
}