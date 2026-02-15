package com.estoquecentral.shared.cache;

import com.estoquecentral.shared.tenant.TenantCacheKeyGenerator;
import org.redisson.api.RedissonClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.CacheManager;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import java.util.Objects;

/**
 * CacheInvalidationService - Handles cache eviction and invalidation
 * Story 7.5: Redis Cache with Tenant Isolation
 *
 * <p>Provides methods to invalidate cache entries at various granularities:
 * <ul>
 *   <li>Single cache entry by key</li>
 *   <li>All entries in a specific cache</li>
 *   <li>All cache entries for current tenant (pattern-based)</li>
 *   <li>All cache entries across all tenants (admin operation)</li>
 * </ul>
 *
 * <p><strong>Tenant Isolation:</strong>
 * All invalidation operations respect tenant context via {@link TenantCacheKeyGenerator}.
 * Invalidating "products" cache will only clear products for the current tenant.
 *
 * <p><strong>Usage Examples:</strong>
 * <pre>{@code
 * // Invalidate specific product
 * cacheInvalidationService.evictCacheEntry("products", productId);
 *
 * // Invalidate all products for current tenant
 * cacheInvalidationService.evictCache("products");
 *
 * // Invalidate all caches for current tenant
 * cacheInvalidationService.evictAllTenantCaches();
 * }</pre>
 *
 * @see TenantCacheKeyGenerator
 * @see com.estoquecentral.config.CacheConfig
 */
@Service
@ConditionalOnProperty(name = "redis.enabled", havingValue = "true")
public class CacheInvalidationService {

    private static final Logger logger = LoggerFactory.getLogger(CacheInvalidationService.class);

    private final CacheManager cacheManager;
    private final RedissonClient redissonClient;

    public CacheInvalidationService(CacheManager cacheManager, RedissonClient redissonClient) {
        this.cacheManager = cacheManager;
        this.redissonClient = redissonClient;
    }

    /**
     * Evicts a single cache entry by key.
     *
     * <p>Only evicts the entry for the current tenant (tenant context is used).
     *
     * @param cacheName the cache name (e.g., "products", "users")
     * @param key the cache key (e.g., productId, userId)
     */
    public void evictCacheEntry(String cacheName, Object key) {
        var cache = cacheManager.getCache(cacheName);
        if (cache != null) {
            // Generate tenant-aware key
            String tenantKey = TenantCacheKeyGenerator.generateKey(cacheName, key);
            cache.evict(tenantKey);
            logger.debug("Evicted cache entry: {} -> {}", cacheName, tenantKey);
        } else {
            logger.warn("Cache '{}' not found, cannot evict entry: {}", cacheName, key);
        }
    }

    /**
     * Evicts all entries in a specific cache for the current tenant.
     *
     * <p>Uses pattern-based eviction: tenant:{tenantId}:{cacheName}:*
     *
     * @param cacheName the cache name to clear
     */
    public void evictCache(String cacheName) {
        var cache = cacheManager.getCache(cacheName);
        if (cache != null) {
            // Clear cache for current tenant only
            String pattern = TenantCacheKeyGenerator.generatePattern(cacheName);
            long deletedKeys = deleteKeysByPattern(pattern);
            logger.info("Evicted cache '{}' for current tenant (deleted {} keys)", cacheName, deletedKeys);
        } else {
            logger.warn("Cache '{}' not found, cannot evict", cacheName);
        }
    }

    /**
     * Evicts all cache entries for the current tenant across all caches.
     *
     * <p>Uses pattern: tenant:{tenantId}:*
     *
     * <p><strong>Use with caution:</strong> This clears ALL cached data for the tenant.
     */
    public void evictAllTenantCaches() {
        String pattern = TenantCacheKeyGenerator.generatePattern("*");
        long deletedKeys = deleteKeysByPattern(pattern);
        logger.info("Evicted ALL caches for current tenant (deleted {} keys)", deletedKeys);
    }

    /**
     * Evicts all cache entries across all tenants.
     *
     * <p><strong>ADMIN OPERATION:</strong> This clears the entire cache.
     * Use only for maintenance or emergency situations.
     */
    public void evictAllCaches() {
        cacheManager.getCacheNames().forEach(cacheName -> {
            var cache = cacheManager.getCache(cacheName);
            if (cache != null) {
                cache.clear();
            }
        });
        logger.warn("Evicted ALL caches across ALL tenants (ADMIN OPERATION)");
    }

    /**
     * Deletes all Redis keys matching a pattern.
     *
     * <p>Uses Redisson's keys API to find and delete matching keys.
     *
     * @param pattern the Redis key pattern (supports wildcards)
     * @return the number of deleted keys
     */
    private long deleteKeysByPattern(String pattern) {
        try {
            Iterable<String> keys = redissonClient.getKeys().getKeysByPattern(pattern);
            long count = 0;
            for (String key : keys) {
                redissonClient.getBucket(key).delete();
                count++;
            }
            return count;
        } catch (Exception e) {
            logger.error("Failed to delete keys by pattern: {}", pattern, e);
            return 0;
        }
    }

    /**
     * Checks if a cache entry exists for a given key.
     *
     * @param cacheName the cache name
     * @param key the cache key
     * @return true if cache entry exists, false otherwise
     */
    public boolean isCached(String cacheName, Object key) {
        var cache = cacheManager.getCache(cacheName);
        if (cache != null) {
            String tenantKey = TenantCacheKeyGenerator.generateKey(cacheName, key);
            return cache.get(tenantKey) != null;
        }
        return false;
    }

    /**
     * Gets cache statistics for monitoring.
     *
     * <p>Counts the number of cached entries for the current tenant.
     *
     * @param cacheName the cache name
     * @return the number of cached entries for current tenant
     */
    public long getCacheEntryCount(String cacheName) {
        String pattern = TenantCacheKeyGenerator.generatePattern(cacheName);
        try {
            Iterable<String> keys = redissonClient.getKeys().getKeysByPattern(pattern);
            long count = 0;
            for (String ignored : keys) {
                count++;
            }
            return count;
        } catch (Exception e) {
            logger.error("Failed to count cache entries for: {}", cacheName, e);
            return -1;
        }
    }
}
