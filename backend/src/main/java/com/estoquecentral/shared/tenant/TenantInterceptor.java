package com.estoquecentral.shared.tenant;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.lang.NonNull;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * TenantInterceptor - Extracts tenant ID from HTTP request and sets it in TenantContext
 *
 * <p>This interceptor runs before every HTTP request and extracts the tenant identifier
 * using one of two strategies:
 * <ol>
 *   <li><strong>Header-based</strong>: Reads X-Tenant-ID header (preferred for APIs)</li>
 *   <li><strong>Subdomain-based</strong>: Extracts from hostname (e.g., tenant1.app.com → tenant1)</li>
 * </ol>
 *
 * <p>Once extracted, the tenant ID is stored in {@link TenantContext} (ThreadLocal),
 * making it available throughout the request lifecycle.
 *
 * <p><strong>CRITICAL:</strong> The {@link #afterCompletion} method ALWAYS clears
 * the TenantContext to prevent memory leaks and tenant ID bleeding between requests.
 *
 * <p>Usage example:
 * <pre>{@code
 * // API request with header
 * curl http://localhost:8080/api/produtos \
 *   -H "X-Tenant-ID: a1b2c3d4-e5f6-7890-abcd-ef1234567890"
 *
 * // Subdomain-based (future feature)
 * curl http://tenant123.estoque-central.com/api/produtos
 * }</pre>
 *
 * @see TenantContext
 * @see TenantRoutingDataSource
 * @see WebMvcConfig
 */
public class TenantInterceptor implements HandlerInterceptor {

    private static final Logger logger = LoggerFactory.getLogger(TenantInterceptor.class);

    /**
     * HTTP header name for tenant identification.
     */
    private static final String TENANT_HEADER_NAME = "X-Tenant-ID";

    /**
     * Regex pattern to extract tenant ID from subdomain.
     * Example: tenant123.estoque-central.com → captures "tenant123"
     */
    private static final Pattern SUBDOMAIN_PATTERN = Pattern.compile("^([a-zA-Z0-9-]+)\\.");

    /**
     * Pre-handle method - extracts tenant ID and sets it in TenantContext.
     *
     * <p>Extraction priority:
     * <ol>
     *   <li>Check X-Tenant-ID header (if present, use it)</li>
     *   <li>Check subdomain (if header absent, extract from hostname)</li>
     *   <li>If both absent, leave TenantContext empty (defaults to "public" schema)</li>
     * </ol>
     *
     * @param request  the HTTP request
     * @param response the HTTP response
     * @param handler  the chosen handler
     * @return true to continue request processing
     */
    @Override
    public boolean preHandle(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull Object handler) {

        // Strategy 1: Extract from X-Tenant-ID header (preferred)
        String tenantId = request.getHeader(TENANT_HEADER_NAME);

        // Strategy 2: Extract from subdomain (fallback)
        if (tenantId == null || tenantId.isBlank()) {
            tenantId = extractTenantFromSubdomain(request.getServerName());
        }

        // Set tenant context if found
        if (tenantId != null && !tenantId.isBlank()) {
            TenantContext.setTenantId(tenantId);
            logger.debug("Tenant context set: {} (URI: {})", tenantId, request.getRequestURI());
        } else {
            logger.trace("No tenant ID found in request (URI: {}), using public schema", request.getRequestURI());
        }

        return true; // Continue request processing
    }

    /**
     * After-completion method - ALWAYS clears TenantContext.
     *
     * <p>This method is called after request completion, even if an exception occurred.
     * It ensures that the ThreadLocal is cleaned up to prevent memory leaks.
     *
     * @param request   the HTTP request
     * @param response  the HTTP response
     * @param handler   the chosen handler
     * @param ex        any exception thrown during handler execution (may be null)
     */
    @Override
    public void afterCompletion(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull Object handler,
            Exception ex) {

        // CRITICAL: Always clear TenantContext to prevent memory leaks
        TenantContext.clear();
        logger.trace("Tenant context cleared (URI: {})", request.getRequestURI());
    }

    /**
     * Extracts tenant ID from hostname subdomain.
     *
     * <p>Examples:
     * <ul>
     *   <li>tenant123.estoque-central.com → "tenant123"</li>
     *   <li>acme-corp.app.com → "acme-corp"</li>
     *   <li>localhost → null</li>
     *   <li>192.168.1.100 → null</li>
     * </ul>
     *
     * @param hostname the server hostname from the request
     * @return the tenant ID extracted from subdomain, or null if not found
     */
    private String extractTenantFromSubdomain(String hostname) {
        if (hostname == null || hostname.isBlank()) {
            return null;
        }

        // Skip subdomain extraction for localhost and IP addresses
        if (hostname.equals("localhost") || hostname.matches("^\\d+\\.\\d+\\.\\d+\\.\\d+$")) {
            return null;
        }

        // Extract first segment before the first dot
        Matcher matcher = SUBDOMAIN_PATTERN.matcher(hostname);
        if (matcher.find()) {
            String subdomain = matcher.group(1);
            logger.trace("Extracted tenant '{}' from subdomain: {}", subdomain, hostname);
            return subdomain;
        }

        return null;
    }
}
