package com.estoquecentral.shared.tenant.config;

import com.estoquecentral.shared.tenant.TenantRoutingDataSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import javax.sql.DataSource;
import java.util.HashMap;
import java.util.Map;

/**
 * DataSourceConfig - Configures multi-tenant DataSource routing
 *
 * <p>This configuration class sets up the {@link TenantRoutingDataSource}
 * which dynamically routes database connections to different schemas
 * based on the current tenant context.
 *
 * <p><strong>Important:</strong> In schema-per-tenant strategy, all tenants
 * share the SAME physical database connection pool, but each connection
 * has its search_path set to the appropriate schema.
 *
 * <p>Schema structure:
 * <ul>
 *   <li><strong>public</strong>: Contains only the "tenants" table (metadata)</li>
 *   <li><strong>tenant_{uuid}</strong>: Contains all business tables for that tenant</li>
 * </ul>
 *
 * @see TenantRoutingDataSource
 * @see com.estoquecentral.shared.tenant.TenantContext
 */
@Configuration
public class DataSourceConfig {

    private final DataSourceProperties dataSourceProperties;

    @Autowired
    public DataSourceConfig(DataSourceProperties dataSourceProperties) {
        this.dataSourceProperties = dataSourceProperties;
    }

    /**
     * Creates the default DataSource bean.
     *
     * <p>This DataSource is used for:
     * <ul>
     *   <li>Public schema operations (tenant creation, metadata)</li>
     *   <li>Flyway migrations on public schema</li>
     *   <li>Health checks</li>
     * </ul>
     *
     * @return the default DataSource pointing to the configured database
     */
    @Bean(name = "defaultDataSource")
    public DataSource defaultDataSource() {
        return DataSourceBuilder.create()
                .url(dataSourceProperties.getUrl())
                .username(dataSourceProperties.getUsername())
                .password(dataSourceProperties.getPassword())
                .driverClassName(dataSourceProperties.getDriverClassName())
                .build();
    }

    /**
     * Creates the tenant routing DataSource bean.
     *
     * <p>This is the PRIMARY DataSource used by Spring Data JDBC.
     * It delegates to {@link TenantRoutingDataSource} which routes
     * connections based on {@link com.estoquecentral.shared.tenant.TenantContext}.
     *
     * <p><strong>Routing mechanism:</strong>
     * <ol>
     *   <li>Request arrives with X-Tenant-ID header</li>
     *   <li>TenantInterceptor sets tenant ID in TenantContext</li>
     *   <li>When query executes, Spring asks TenantRoutingDataSource for connection</li>
     *   <li>TenantRoutingDataSource reads TenantContext and returns schema name</li>
     *   <li>Connection's search_path is set to that schema</li>
     * </ol>
     *
     * <p><strong>Note:</strong> Target datasources are configured with the same
     * physical connection pool. The only difference is the search_path setting.
     *
     * @param defaultDataSource the default DataSource
     * @return the configured TenantRoutingDataSource
     */
    @Primary
    @Bean(name = "dataSource")
    public DataSource dataSource(DataSource defaultDataSource) {
        TenantRoutingDataSource routingDataSource = new TenantRoutingDataSource();

        // Set up target datasources map
        // NOTE: In schema-per-tenant, we use the SAME datasource for all schemas
        // The routing happens via PostgreSQL search_path, not separate connections
        Map<Object, Object> targetDataSources = new HashMap<>();
        targetDataSources.put("public", defaultDataSource);

        // Additional tenant datasources will be added dynamically
        // For now, all tenants use the same datasource with different search_path
        // This is handled by TenantRoutingDataSource.determineCurrentLookupKey()

        routingDataSource.setTargetDataSources(targetDataSources);
        routingDataSource.setDefaultTargetDataSource(defaultDataSource);

        // Initialize the datasource
        routingDataSource.afterPropertiesSet();

        return routingDataSource;
    }
}
