package com.estoquecentral.shared.tenant;

/**
 * TenantContext - Thread-safe storage for current tenant ID
 *
 * <p>This class uses ThreadLocal to store the tenant ID for the current request.
 * Each HTTP request thread has its own isolated tenant context, preventing
 * data leakage between concurrent requests.
 *
 * <p><strong>CRITICAL:</strong> Always call {@link #clear()} after request completion
 * to prevent memory leaks. This is typically handled by {@link TenantInterceptor}.
 *
 * <p>Usage example:
 * <pre>{@code
 * // Set tenant ID (typically in interceptor)
 * TenantContext.setTenantId("a1b2c3d4-e5f6-7890-abcd-ef1234567890");
 *
 * // Get tenant ID (anywhere in request processing)
 * String tenantId = TenantContext.getTenantId();
 *
 * // Clear context (always in finally or afterCompletion)
 * TenantContext.clear();
 * }</pre>
 *
 * @see TenantInterceptor
 * @see TenantRoutingDataSource
 */
public final class TenantContext {

    private static final ThreadLocal<String> CURRENT_TENANT = new ThreadLocal<>();

    /**
     * Private constructor to prevent instantiation.
     * This is a utility class with only static methods.
     */
    private TenantContext() {
        throw new UnsupportedOperationException("TenantContext is a utility class and cannot be instantiated");
    }

    /**
     * Sets the tenant ID for the current thread.
     *
     * @param tenantId the tenant UUID (not null)
     * @throws IllegalArgumentException if tenantId is null or blank
     */
    public static void setTenantId(String tenantId) {
        if (tenantId == null || tenantId.isBlank()) {
            throw new IllegalArgumentException("Tenant ID cannot be null or blank");
        }
        CURRENT_TENANT.set(tenantId);
    }

    /**
     * Gets the tenant ID for the current thread.
     *
     * @return the tenant ID, or null if not set
     */
    public static String getTenantId() {
        return CURRENT_TENANT.get();
    }

    /**
     * Clears the tenant ID from the current thread.
     *
     * <p><strong>CRITICAL:</strong> Must be called after each request to prevent
     * memory leaks and tenant ID bleeding between requests.
     *
     * <p>This method is idempotent - calling it multiple times is safe.
     */
    public static void clear() {
        CURRENT_TENANT.remove();
    }
}
