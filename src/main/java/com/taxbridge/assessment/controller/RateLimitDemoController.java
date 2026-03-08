package com.taxbridge.assessment.controller;

import com.taxbridge.assessment.common.ApiResponse;
import com.taxbridge.assessment.ratelimit.RateLimited;
import com.taxbridge.assessment.security.JwtUtil;

import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/demo")
public class RateLimitDemoController {

    private final JwtUtil jwtUtil;

    public RateLimitDemoController(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    @RateLimited
    @GetMapping("/ping")
    public ApiResponse<Map<String, Object>> ping(HttpServletRequest request) {

        String authHeader = request.getHeader("Authorization");

        UUID tenantId = jwtUtil.extractTenantId(authHeader);

        return ApiResponse.success(
                "pong",
                Map.of("tenantId", tenantId)
        );
    }
}