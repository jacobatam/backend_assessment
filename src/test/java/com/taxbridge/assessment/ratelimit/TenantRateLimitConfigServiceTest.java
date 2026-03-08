package com.taxbridge.assessment.ratelimit;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.taxbridge.assessment.exception.TenantNotFoundException;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TenantRateLimitConfigServiceTest {

    @Mock
    private TenantRateLimitConfigRepository repository;

    @Mock
    private StringRedisTemplate redisTemplate;

    @Mock
    private ValueOperations<String, String> valueOperations;

    @Mock
    private ObjectMapper objectMapper;

    @InjectMocks
    private TenantRateLimitConfigService service;

    private final UUID tenantId =
            UUID.fromString("a1000000-0000-0000-0000-000000000001");


    /**
     * Test 1 — Cache hit
     * Redis returns JSON so repository should NOT be called
     */
    @Test
    void cacheHit_repositoryNotCalled() throws Exception {

        String json = "{\"tenantId\":\"a1000000-0000-0000-0000-000000000001\"}";
        TenantRateLimitConfig config = new TenantRateLimitConfig();

        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get(anyString())).thenReturn(json);
        when(objectMapper.readValue(json, TenantRateLimitConfig.class))
                .thenReturn(config);

        TenantRateLimitConfig result = service.getConfig(tenantId);

        verify(repository, never()).findByTenantId(any());

        assertEquals(config, result);
    }


    /**
     * Test 2 — Cache miss
     * Repository should be called and result cached
     */
    @Test
    void cacheMiss_repositoryCalled() throws Exception {

        TenantRateLimitConfig config = new TenantRateLimitConfig();

        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get(anyString())).thenReturn(null);

        when(repository.findByTenantId(tenantId))
                .thenReturn(Optional.of(config));

        when(objectMapper.writeValueAsString(config))
                .thenReturn("json");

        TenantRateLimitConfig result = service.getConfig(tenantId);

        verify(repository).findByTenantId(tenantId);
        verify(valueOperations)
                .set(anyString(), anyString(), anyLong(), any());

        assertEquals(config, result);
    }


    /**
     * Test 3 — Tenant not found
     */
    @Test
    void tenantNotFound_exceptionThrown() {

        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get(anyString())).thenReturn(null);

        when(repository.findByTenantId(tenantId))
                .thenReturn(Optional.empty());

        assertThrows(
                TenantNotFoundException.class,
                () -> service.getConfig(tenantId)
        );
    }


    /**
     * Test 4 — Cache eviction
     */
    @Test
    void evictCache_deletesRedisKey() {

        service.evictCache(tenantId);

        verify(redisTemplate)
                .delete("tenant_config:" + tenantId);
    }
}