package com.estoquecentral.shared.tenant;

import org.springframework.jdbc.datasource.lookup.AbstractRoutingDataSource;

/**
 * TenantRoutingDataSource - Dynamic DataSource routing based on tenant context
 *
 * <p>This class extends Spring's {@link AbstractRoutingDataSource} to route
 * database connections to the appropriate tenant schema based on the current
 * {@link TenantContext}.
 *
 * <p>How it works:
 * <ol>
 *   <li>HTTP request arrives with X-Tenant-ID header</li>
 *   <li>{@link TenantInterceptor} sets tenant ID in {@link TenantContext}</li>
 *   <li>When database connection is needed, Spring calls {@link #determineCurrentLookupKey()}</li>
 *   <li>Method returns schema name based on tenant ID</li>
 *   <li>Spring routes connection to correct schema</li>
 * </ol>
 *
 * <p><strong>Schema Naming Convention:</strong>
 * <ul>
 *   <li>Public schema: "public" (contains only tenants table)</li>
 *   <li>Tenant schema: "tenant_{uuid}" (e.g., tenant_a1b2c3d4e5f6)</li>
 * </ul>
 *
 * <p><strong>Important:</strong> PostgreSQL uses the same physical database connection
 * but switches the search_path to the appropriate schema. This is achieved via:
 * <pre>{@code SET search_path TO tenant_xyz, public;}</pre>
 *
 * @see TenantContext
 * @see TenantInterceptor
 * @see DataSourceConfig
 */
public class TenantRoutingDataSource extends AbstractRoutingDataSource {

    /**
     * Determines the current lookup key for routing the DataSource.
     *
     * <p>This method is called by Spring's AbstractRoutingDataSource whenever
     * a database connection is requested. It returns the schema name that should
     * be used for the current request.
     *
     * <p>Routing logic:
     * <ul>
     *   <li>If {@link TenantContext#getTenantId()} is null → returns "public"</li>
     *   <li>If tenant ID is set → returns "tenant_{tenantId}" (schema name)</li>
     * </ul>
     *
     * @return the schema name to route to ("public" or "tenant_{uuid}")
     */
    @Override
    protected Object determineCurrentLookupKey() {
        String tenantId = TenantContext.getTenantId();

        if (tenantId == null) {
            // No tenant context set - use public schema
            // This is normal for:
            // - Tenant creation endpoint (POST /api/tenants)
            // - Health check endpoint
            // - Actuator endpoints
            return "public";
        }

        // Return schema name for the current tenant
        // Schema naming convention: tenant_{uuid_without_hyphens}
        String schemaName = "tenant_" + tenantId.replace("-", "");

        logger.trace("Routing to schema: " + schemaName + " for tenant ID: " + tenantId);

        return schemaName;
    }
}
