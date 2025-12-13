package com.estoquecentral.shared.tenant;

import org.springframework.jdbc.datasource.DelegatingDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * TenantAwareDataSource - Wraps a DataSource to set PostgreSQL search_path based on TenantContext
 *
 * <p>This class ensures that every connection obtained from the pool has the correct
 * search_path set based on the current {@link TenantContext}.
 *
 * <p><strong>How it works:</strong>
 * <ol>
 *   <li>Request arrives with X-Tenant-ID header</li>
 *   <li>TenantInterceptor sets tenant ID in TenantContext</li>
 *   <li>When connection is requested, this class gets it from delegate DataSource</li>
 *   <li>Executes: SET search_path TO tenant_{uuid}, public;</li>
 *   <li>Returns connection with correct schema</li>
 * </ol>
 *
 * @see TenantContext
 * @see TenantRoutingDataSource
 */
public class TenantAwareDataSource extends DelegatingDataSource {

    private static final Logger logger = LoggerFactory.getLogger(TenantAwareDataSource.class);

    public TenantAwareDataSource(DataSource targetDataSource) {
        super(targetDataSource);
    }

    @Override
    public Connection getConnection() throws SQLException {
        Connection connection = super.getConnection();
        setSearchPath(connection);
        return connection;
    }

    @Override
    public Connection getConnection(String username, String password) throws SQLException {
        Connection connection = super.getConnection(username, password);
        setSearchPath(connection);
        return connection;
    }

    /**
     * Sets the PostgreSQL search_path based on current TenantContext.
     *
     * <p>If tenant ID is set in context, sets search_path to "tenant_{uuid}, public"
     * <p>If no tenant ID is set, sets search_path to "public" only
     *
     * @param connection the database connection
     * @throws SQLException if SET search_path fails
     */
    private void setSearchPath(Connection connection) throws SQLException {
        String tenantId = TenantContext.getTenantId();

        String searchPath;
        if (tenantId == null || tenantId.isBlank()) {
            // No tenant context - use public schema
            searchPath = "public";
            logger.trace("Setting search_path to: public (no tenant context)");
        } else {
            // Tenant context exists - set search_path to tenant schema + public fallback
            String schemaName = "tenant_" + tenantId.replace("-", "");
            searchPath = schemaName + ", public";
            logger.trace("Setting search_path to: {} for tenant: {}", searchPath, tenantId);
        }

        try (Statement statement = connection.createStatement()) {
            statement.execute("SET search_path TO " + searchPath);
        } catch (SQLException e) {
            logger.error("Failed to set search_path to: {}", searchPath, e);
            throw e;
        }
    }
}
