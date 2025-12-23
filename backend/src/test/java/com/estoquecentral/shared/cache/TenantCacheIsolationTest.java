package com.estoquecentral.shared.cache;

import com.estoquecentral.shared.tenant.TenantCacheKeyGenerator;
import com.estoquecentral.shared.tenant.TenantContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Service;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

import java.util.UUID;

import static org.assertj.core.api.Assertions.*;

/**
 * Integration tests for tenant-isolated caching
 *
 * <p>Tests verify that:
 * <ul>
 *   <li>Cache entries are isolated per tenant</li>
 *   <li>Tenant A cannot access Tenant B's cached data</li>
 *   <li>Cache invalidation only affects current tenant</li>
 *   <li>TenantCacheKeyGenerator generates correct keys</li>
 * </ul>
 *
 * <p>This test uses mock services to simulate cacheable operations.
 */
@SpringJUnitConfig(TenantCacheIsolationTest.TestConfig.class)
@DisplayName("Tenant Cache Isolation Tests")
class TenantCacheIsolationTest {

    private final CacheManager cacheManager;
    private final TestCacheableService cacheableService;
    private final TenantCacheKeyGenerator keyGenerator;

    private UUID tenantA;
    private UUID tenantB;

    TenantCacheIsolationTest(
            CacheManager cacheManager,
            TestCacheableService cacheableService,
            TenantCacheKeyGenerator keyGenerator) {
        this.cacheManager = cacheManager;
        this.cacheableService = cacheableService;
        this.keyGenerator = keyGenerator;
    }

    @BeforeEach
    void setUp() {
        tenantA = UUID.randomUUID();
        tenantB = UUID.randomUUID();

        // Clear all caches before each test
        cacheManager.getCacheNames().forEach(cacheName -> {
            var cache = cacheManager.getCache(cacheName);
            if (cache != null) {
                cache.clear();
            }
        });
    }

    @AfterEach
    void tearDown() {
        TenantContext.clear();
    }

    @Test
    @DisplayName("Should generate tenant-prefixed cache keys")
    void shouldGenerateTenantPrefixedCacheKeys() {
        // Given
        TenantContext.setTenantId(tenantA.toString());

        // When
        String key = TenantCacheKeyGenerator.generateKey("products", "product-123");

        // Then
        assertThat(key).startsWith("tenant:" + tenantA);
        assertThat(key).contains(":products:");
        assertThat(key).contains(":product-123");
    }

    @Test
    @DisplayName("Should generate public-prefixed keys when no tenant context")
    void shouldGeneratePublicPrefixedKeysWhenNoTenantContext() {
        // Given - no tenant context set

        // When
        String key = TenantCacheKeyGenerator.generateKey("products", "product-123");

        // Then
        assertThat(key).startsWith("public:");
        assertThat(key).contains(":products:");
    }

    @Test
    @DisplayName("Should isolate cache between different tenants")
    void shouldIsolateCacheBetweenDifferentTenants() {
        // Given - Tenant A caches data
        TenantContext.setTenantId(tenantA.toString());
        String resultA1 = cacheableService.getProduct("product-1");
        assertThat(resultA1).isEqualTo("Tenant A: product-1");

        // When - Tenant B requests same product
        TenantContext.setTenantId(tenantB.toString());
        String resultB1 = cacheableService.getProduct("product-1");

        // Then - Tenant B gets its own result (not cached from Tenant A)
        assertThat(resultB1).isEqualTo("Tenant B: product-1");
        assertThat(resultB1).isNotEqualTo(resultA1);

        // Verify cache hit counts
        assertThat(cacheableService.getCallCount()).isEqualTo(2); // Both tenants called actual method
    }

    @Test
    @DisplayName("Should return cached data for same tenant")
    void shouldReturnCachedDataForSameTenant() {
        // Given
        TenantContext.setTenantId(tenantA.toString());

        // When - First call (cache miss)
        String result1 = cacheableService.getProduct("product-1");
        // Second call (cache hit)
        String result2 = cacheableService.getProduct("product-1");

        // Then
        assertThat(result1).isEqualTo(result2);
        assertThat(cacheableService.getCallCount()).isEqualTo(1); // Only 1 actual method call
    }

    @Test
    @DisplayName("Should invalidate cache only for current tenant")
    void shouldInvalidateCacheOnlyForCurrentTenant() {
        // Given - Both tenants cache data
        TenantContext.setTenantId(tenantA.toString());
        cacheableService.getProduct("product-1");

        TenantContext.setTenantId(tenantB.toString());
        cacheableService.getProduct("product-1");

        int callsBeforeEviction = cacheableService.getCallCount();

        // When - Invalidate cache for Tenant A only
        TenantContext.setTenantId(tenantA.toString());
        var cache = cacheManager.getCache("test-products");
        assertThat(cache).isNotNull();
        cache.clear();

        // Then - Tenant A cache is cleared
        cacheableService.getProduct("product-1"); // Cache miss, calls method again
        assertThat(cacheableService.getCallCount()).isEqualTo(callsBeforeEviction + 1);

        // But Tenant B cache is still intact
        TenantContext.setTenantId(tenantB.toString());
        cacheableService.getProduct("product-1"); // Cache hit, no method call
        assertThat(cacheableService.getCallCount()).isEqualTo(callsBeforeEviction + 1); // No increase
    }

    @Test
    @DisplayName("Should generate correct cache key pattern for eviction")
    void shouldGenerateCorrectCacheKeyPatternForEviction() {
        // Given
        TenantContext.setTenantId(tenantA.toString());

        // When
        String pattern = TenantCacheKeyGenerator.generatePattern("products");

        // Then
        assertThat(pattern).isEqualTo("tenant:" + tenantA + ":products:*");
    }

    @Test
    @DisplayName("Should generate wildcard pattern for all tenant caches")
    void shouldGenerateWildcardPatternForAllTenantCaches() {
        // Given
        TenantContext.setTenantId(tenantA.toString());

        // When
        String pattern = TenantCacheKeyGenerator.generatePattern("*");

        // Then
        assertThat(pattern).isEqualTo("tenant:" + tenantA + ":*");
    }

    /**
     * Test configuration with mock cacheable service
     */
    @Configuration
    @EnableCaching
    static class TestConfig {

        @Bean
        public TestCacheableService testCacheableService() {
            return new TestCacheableService();
        }

        @Bean
        public TenantCacheKeyGenerator tenantCacheKeyGenerator() {
            return new TenantCacheKeyGenerator();
        }
    }

    /**
     * Mock service with cacheable method for testing
     */
    @Service
    static class TestCacheableService {

        private int callCount = 0;

        @Cacheable(value = "test-products", keyGenerator = "tenantCacheKeyGenerator")
        public String getProduct(String productId) {
            callCount++;
            String tenantId = TenantContext.getTenantId();
            return (tenantId != null ? "Tenant " + tenantId.substring(0, 8) : "Public") + ": " + productId;
        }

        public int getCallCount() {
            return callCount;
        }

        public void resetCallCount() {
            callCount = 0;
        }
    }
}
