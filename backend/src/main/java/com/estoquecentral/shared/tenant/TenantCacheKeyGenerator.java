package com.estoquecentral.shared.tenant;

import org.springframework.cache.interceptor.KeyGenerator;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;

/**
 * TenantCacheKeyGenerator - Generates cache keys with tenant isolation
 *
 * <p>This key generator automatically prefixes all cache keys with the current tenant ID
 * to ensure complete cache isolation between tenants.
 *
 * <p><strong>Key Format:</strong>
 * <pre>
 * tenant:{tenantId}:{cacheName}:{methodName}:{args}
 * </pre>
 *
 * <p><strong>Examples:</strong>
 * <ul>
 *   <li>tenant:123e4567:products:findById:550e8400</li>
 *   <li>tenant:123e4567:users:findByEmail:admin@company.com</li>
 *   <li>tenant:123e4567:dashboard:getMetrics:2024-01-15</li>
 * </ul>
 *
 * <p><strong>Public Schema Fallback:</strong>
 * If no tenant context is set (e.g., for tenant creation endpoint), uses "public" prefix:
 * <pre>
 * public:{cacheName}:{methodName}:{args}
 * </pre>
 *
 * <p><strong>Usage:</strong>
 * <pre>{@code
 * @Cacheable(value = "products", keyGenerator = "tenantCacheKeyGenerator")
 * public Product findById(UUID productId) {
 *     // Method implementation
 * }
 * }</pre>
 *
 * @see TenantContext
 * @see CacheConfig
 */
@Component("tenantCacheKeyGenerator")
public class TenantCacheKeyGenerator implements KeyGenerator {

    /**
     * Generates a cache key with tenant isolation.
     *
     * <p>The generated key follows the pattern:
     * <pre>tenant:{tenantId}:{cacheName}:{methodName}:{arg1}:{arg2}:...</pre>
     *
     * <p>If multiple arguments are provided, they are joined with colons.
     * If no arguments are provided, only tenant:cacheName:methodName is used.
     *
     * @param target the target instance
     * @param method the method being called
     * @param params the method parameters (cache key arguments)
     * @return the generated cache key with tenant prefix
     */
    @Override
    public Object generate(Object target, Method method, Object... params) {
        // Get current tenant ID from context
        String tenantId = TenantContext.getTenantId();

        // Use "public" prefix if no tenant context (e.g., tenant creation endpoints)
        String tenantPrefix = (tenantId != null && !tenantId.isBlank())
                ? "tenant:" + tenantId
                : "public";

        // Build cache key: tenant:{id}:{className}:{methodName}:{args}
        StringBuilder keyBuilder = new StringBuilder(tenantPrefix);
        keyBuilder.append(":");
        keyBuilder.append(target.getClass().getSimpleName());
        keyBuilder.append(":");
        keyBuilder.append(method.getName());

        // Append method parameters as part of the key
        if (params != null && params.length > 0) {
            for (Object param : params) {
                keyBuilder.append(":");
                keyBuilder.append(param != null ? param.toString() : "null");
            }
        }

        return keyBuilder.toString();
    }

    /**
     * Generates a simple cache key for a specific cache name.
     * Useful for programmatic cache operations.
     *
     * @param cacheName the cache name
     * @param keys the key components
     * @return the generated cache key with tenant prefix
     */
    public static String generateKey(String cacheName, Object... keys) {
        String tenantId = TenantContext.getTenantId();
        String tenantPrefix = (tenantId != null && !tenantId.isBlank())
                ? "tenant:" + tenantId
                : "public";

        StringBuilder keyBuilder = new StringBuilder(tenantPrefix);
        keyBuilder.append(":");
        keyBuilder.append(cacheName);

        if (keys != null && keys.length > 0) {
            for (Object key : keys) {
                keyBuilder.append(":");
                keyBuilder.append(key != null ? key.toString() : "null");
            }
        }

        return keyBuilder.toString();
    }

    /**
     * Generates a pattern for cache eviction (supports wildcards).
     * Used for invalidating multiple cache entries at once.
     *
     * <p>Examples:
     * <pre>
     * tenant:123e4567:products:*  (all product cache entries for tenant)
     * tenant:123e4567:*           (all cache entries for tenant)
     * </pre>
     *
     * @param cacheName the cache name (or "*" for all caches)
     * @return the cache key pattern with tenant prefix
     */
    public static String generatePattern(String cacheName) {
        String tenantId = TenantContext.getTenantId();
        String tenantPrefix = (tenantId != null && !tenantId.isBlank())
                ? "tenant:" + tenantId
                : "public";

        return tenantPrefix + ":" + cacheName + ":*";
    }
}
