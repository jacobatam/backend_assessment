package com.taxbridge.assessment.ratelimit;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface TenantRateLimitConfigRepository
        extends JpaRepository<TenantRateLimitConfig, UUID> {

    Optional<TenantRateLimitConfig> findByTenantId(UUID tenantId);

}