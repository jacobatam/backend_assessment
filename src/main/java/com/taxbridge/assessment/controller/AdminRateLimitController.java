package com.taxbridge.assessment.controller;

import com.taxbridge.assessment.common.ApiResponse;
import com.taxbridge.assessment.ratelimit.TenantRateLimitConfig;
import com.taxbridge.assessment.ratelimit.TenantRateLimitConfigRepository;
import com.taxbridge.assessment.ratelimit.TenantRateLimitConfigService;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/admin/rate-limits")
public class AdminRateLimitController {

    private final TenantRateLimitConfigRepository repository;
    private final TenantRateLimitConfigService configService;

    public AdminRateLimitController(
            TenantRateLimitConfigRepository repository,
            TenantRateLimitConfigService configService
    ) {
        this.repository = repository;
        this.configService = configService;
    }

    @GetMapping("/{tenantId}")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ApiResponse<TenantRateLimitConfig> getRateLimitConfig(
            @PathVariable UUID tenantId
    ) {

        TenantRateLimitConfig config =
                repository.findByTenantId(tenantId)
                        .orElseThrow();

        return ApiResponse.success(
                "Rate limit config retrieved",
                config
        );
    }

    @PutMapping("/{tenantId}")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ApiResponse<TenantRateLimitConfig> updateRateLimitConfig(
            @PathVariable UUID tenantId,
            @Valid @RequestBody TenantRateLimitConfig request
    ) {

        TenantRateLimitConfig config =
                repository.findByTenantId(tenantId)
                        .orElseThrow();

        config.setMaxRequests(request.getMaxRequests());
        config.setWindowSeconds(request.getWindowSeconds());
        config.setPlanName(request.getPlanName());

        TenantRateLimitConfig updated =
                repository.save(config);

        // Evict Redis cache
        configService.evictCache(tenantId);

        return ApiResponse.success(
                "Rate limit config updated",
                updated
        );
    }
}