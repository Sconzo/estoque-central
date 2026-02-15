package com.estoquecentral.config;

import org.redisson.api.RedissonClient;
import org.redisson.spring.cache.RedissonSpringCacheManager;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;

/**
 * CacheConfig - Spring Cache configuration with Redisson and tenant isolation
 * Story 7.5: Redis Cache with Tenant Isolation
 *
 * <p>Configures Spring Cache abstraction using Redisson as the cache provider.
 * Works in conjunction with {@link com.estoquecentral.shared.tenant.TenantCacheKeyGenerator}
 * to provide tenant-isolated caching.
 *
 * <p><strong>Cache Names and TTL:</strong>
 * <ul>
 *   <li><strong>products</strong>: 30 minutes - Product catalog data</li>
 *   <li><strong>users</strong>: 15 minutes - User profile data</li>
 *   <li><strong>profiles</strong>: 60 minutes - RBAC profiles (rarely change)</li>
 *   <li><strong>roles</strong>: 120 minutes - RBAC roles (rarely change)</li>
 *   <li><strong>dashboard</strong>: 5 minutes - Dashboard metrics</li>
 *   <li><strong>tenants</strong>: 60 minutes - Tenant metadata</li>
 * </ul>
 *
 * <p><strong>Key Format:</strong>
 * All cache keys are automatically prefixed with tenant ID via TenantCacheKeyGenerator:
 * <pre>tenant:{tenantId}:{cacheName}:{key}</pre>
 *
 * <p><strong>Eviction Policy:</strong>
 * Redis is configured with LRU (Least Recently Used) eviction when memory limit is reached.
 *
 * @see com.estoquecentral.shared.tenant.TenantCacheKeyGenerator
 * @see RedissonConfig
 */
@Configuration
@EnableCaching
@ConditionalOnProperty(name = "redis.enabled", havingValue = "true")
public class CacheConfig {

    /**
     * Creates CacheManager bean using Redisson.
     *
     * <p>Configures individual caches with specific TTL (Time To Live) values
     * based on data volatility and access patterns.
     *
     * @param redissonClient the Redisson client
     * @return configured CacheManager
     */
    @Bean
    public CacheManager cacheManager(RedissonClient redissonClient) {
        // Configure cache TTL (in milliseconds)
        Map<String, org.redisson.spring.cache.CacheConfig> cacheConfigs = new HashMap<>();

        // Products cache: 30 minutes (frequently accessed, moderate volatility)
        cacheConfigs.put("products", new org.redisson.spring.cache.CacheConfig(
                30 * 60 * 1000,  // TTL: 30 minutes
                15 * 60 * 1000   // Max idle time: 15 minutes
        ));

        // Users cache: 15 minutes (user data changes moderately)
        cacheConfigs.put("users", new org.redisson.spring.cache.CacheConfig(
                15 * 60 * 1000,  // TTL: 15 minutes
                10 * 60 * 1000   // Max idle time: 10 minutes
        ));

        // Profiles cache: 60 minutes (RBAC profiles rarely change)
        cacheConfigs.put("profiles", new org.redisson.spring.cache.CacheConfig(
                60 * 60 * 1000,  // TTL: 60 minutes
                30 * 60 * 1000   // Max idle time: 30 minutes
        ));

        // Roles cache: 120 minutes (RBAC roles very rarely change)
        cacheConfigs.put("roles", new org.redisson.spring.cache.CacheConfig(
                120 * 60 * 1000, // TTL: 120 minutes
                60 * 60 * 1000   // Max idle time: 60 minutes
        ));

        // Dashboard cache: 5 minutes (real-time metrics, high volatility)
        cacheConfigs.put("dashboard", new org.redisson.spring.cache.CacheConfig(
                5 * 60 * 1000,   // TTL: 5 minutes
                2 * 60 * 1000    // Max idle time: 2 minutes
        ));

        // Tenants cache: 60 minutes (tenant metadata rarely changes)
        cacheConfigs.put("tenants", new org.redisson.spring.cache.CacheConfig(
                60 * 60 * 1000,  // TTL: 60 minutes
                30 * 60 * 1000   // Max idle time: 30 minutes
        ));

        // Stock cache: 10 minutes (inventory changes frequently)
        cacheConfigs.put("stock", new org.redisson.spring.cache.CacheConfig(
                10 * 60 * 1000,  // TTL: 10 minutes
                5 * 60 * 1000    // Max idle time: 5 minutes
        ));

        // Marketplace sync cache: 15 minutes (external API data)
        cacheConfigs.put("marketplace", new org.redisson.spring.cache.CacheConfig(
                15 * 60 * 1000,  // TTL: 15 minutes
                10 * 60 * 1000   // Max idle time: 10 minutes
        ));

        // Create RedissonSpringCacheManager with configured caches
        return new RedissonSpringCacheManager(redissonClient, cacheConfigs);
    }
}
