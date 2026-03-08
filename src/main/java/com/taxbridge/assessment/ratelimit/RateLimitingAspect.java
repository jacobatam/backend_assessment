package com.taxbridge.assessment.ratelimit;

import com.taxbridge.assessment.exception.RateLimitExceededException;
import com.taxbridge.assessment.exception.UnauthorizedException;
import com.taxbridge.assessment.security.JwtUtil;

import jakarta.servlet.http.HttpServletRequest;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.*;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.UUID;

@Aspect
@Component
public class RateLimitingAspect {

    private final FixedWindowRateLimiter rateLimiter;
    private final TenantRateLimitConfigService configService;
    private final JwtUtil jwtUtil;

    public RateLimitingAspect(
            FixedWindowRateLimiter rateLimiter,
            TenantRateLimitConfigService configService,
            JwtUtil jwtUtil
    ) {
        this.rateLimiter = rateLimiter;
        this.configService = configService;
        this.jwtUtil = jwtUtil;
    }

    @Around("@annotation(com.taxbridge.assessment.ratelimit.RateLimited)")
    public Object rateLimit(ProceedingJoinPoint joinPoint) throws Throwable {

        /*
         Step 1 — Retrieve HTTP request using RequestContextHolder
         (Assignment explicitly requires this method)
        */
        HttpServletRequest request =
                ((ServletRequestAttributes) RequestContextHolder
                        .currentRequestAttributes())
                        .getRequest();

        /*
         Step 2 — Read Authorization header
        */
        String authHeader = request.getHeader("Authorization");

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new UnauthorizedException("Missing or invalid Authorization header");
        }

        /*
         Step 3 — Extract tenantId from JWT
        */
        UUID tenantId = jwtUtil.extractTenantId(authHeader);

        /*
         Step 4 — Load tenant rate limit configuration
         (Redis cache → PostgreSQL fallback)
        */
        TenantRateLimitConfig config =
                configService.getConfig(tenantId);

        int maxRequests = config.getMaxRequests();
        int windowSeconds = config.getWindowSeconds();

        /*
         Step 5 — Call the Redis fixed window rate limiter
        */
        RateLimitResult result =
                rateLimiter.isAllowed(
                        tenantId.toString(),
                        maxRequests,
                        windowSeconds
                );

        /*
         Step 6 — If rate limit exceeded
        */
        if (!result.allowed()) {

            throw new RateLimitExceededException(
                    "Rate limit exceeded. Try again in "
                            + result.retryAfterSeconds()
                            + " seconds.",
                    result.retryAfterSeconds()
            );
        }

        /*
         Step 7 — If request allowed
         Continue execution
        */
        return joinPoint.proceed();
    }
}