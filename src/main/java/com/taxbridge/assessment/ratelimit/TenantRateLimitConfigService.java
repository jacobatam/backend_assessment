package com.taxbridge.assessment.ratelimit;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.taxbridge.assessment.exception.TenantNotFoundException;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Service
public class TenantRateLimitConfigService {

    private final TenantRateLimitConfigRepository repository;
    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;

    public TenantRateLimitConfigService(
            TenantRateLimitConfigRepository repository,
            StringRedisTemplate redisTemplate,
            ObjectMapper objectMapper
    ) {
        this.repository = repository;
        this.redisTemplate = redisTemplate;
        this.objectMapper = objectMapper;
    }

    public TenantRateLimitConfig getConfig(UUID tenantId) {

        String key = "tenant_config:" + tenantId;

        // 1️⃣ Check Redis cache
        String cached = redisTemplate.opsForValue().get(key);

        if (cached != null) {
            try {
                return objectMapper.readValue(cached, TenantRateLimitConfig.class);
            } catch (JsonProcessingException e) {
                throw new RuntimeException("Failed to deserialize cached config", e);
            }
        }

        // 2️⃣ Load from database
        TenantRateLimitConfig config = repository
                .findByTenantId(tenantId)
                .orElseThrow(() -> new TenantNotFoundException(
                        "Tenant not found: " + tenantId
                ));

        // 3️⃣ Store in Redis for 5 minutes
        try {
            String json = objectMapper.writeValueAsString(config);

            redisTemplate.opsForValue().set(
                    key,
                    json,
                    5,
                    TimeUnit.MINUTES
            );

        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to serialize tenant config", e);
        }

        return config;
    }

    public void evictCache(UUID tenantId) {

        String key = "tenant_config:" + tenantId;

        redisTemplate.delete(key);
    }
}