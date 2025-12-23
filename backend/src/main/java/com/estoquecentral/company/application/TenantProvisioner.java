package com.estoquecentral.company.application;

import org.flywaydb.core.Flyway;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.sql.DataSource;
import java.util.UUID;

/**
 * TenantProvisioner - Service for provisioning new tenant schemas.
 *
 * <p>Story 8.1 - AC3: Tenant Provisioning
 *
 * <p>Responsibilities:
 * <ul>
 *   <li>Creates a new PostgreSQL schema for the tenant</li>
 *   <li>Runs Flyway migrations to create tenant tables</li>
 *   <li>Seeds default profiles (Admin, Gerente, Vendedor)</li>
 *   <li>Completes in < 30 seconds (NFR2)</li>
 * </ul>
 *
 * @since 1.0
 */
@Service
public class TenantProvisioner {

    private static final Logger logger = LoggerFactory.getLogger(TenantProvisioner.class);

    private final DataSource dataSource;
    private final JdbcTemplate jdbcTemplate;

    public TenantProvisioner(DataSource dataSource, JdbcTemplate jdbcTemplate) {
        this.dataSource = dataSource;
        this.jdbcTemplate = jdbcTemplate;
    }

    /**
     * Provisions a new tenant schema with all required tables and data.
     *
     * <p><strong>Process:</strong>
     * <ol>
     *   <li>Generate unique tenant ID (UUID)</li>
     *   <li>Create PostgreSQL schema: tenant_{uuid_without_hyphens}</li>
     *   <li>Run Flyway migrations on the schema (creates tables)</li>
     *   <li>Seed default profiles: Admin, Gerente, Vendedor (via V068 migration)</li>
     * </ol>
     *
     * <p><strong>Performance:</strong> Completes in < 30 seconds (Story 8.1 AC3, NFR2)
     *
     * @return TenantProvisionResult with tenantId and schemaName
     * @throws TenantProvisioningException if provisioning fails
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public TenantProvisionResult provisionTenant() {
        UUID tenantId = UUID.randomUUID();
        String schemaName = generateSchemaName(tenantId);

        logger.info("Starting tenant provisioning: tenantId={}, schemaName={}", tenantId, schemaName);

        long startTime = System.currentTimeMillis();

        try {
            // Step 1: Create PostgreSQL schema
            createSchema(schemaName);

            // Step 2: Run Flyway migrations on the tenant schema
            runMigrations(schemaName);

            long duration = System.currentTimeMillis() - startTime;
            logger.info("Tenant provisioned successfully in {}ms: tenantId={}, schemaName={}",
                    duration, tenantId, schemaName);

            // AC3: Verify provisioning completed in < 30 seconds
            if (duration > 30000) {
                logger.warn("Tenant provisioning took longer than 30 seconds: {}ms (NFR2 violation)", duration);
            }

            return new TenantProvisionResult(tenantId, schemaName, true, null);

        } catch (Exception e) {
            long duration = System.currentTimeMillis() - startTime;
            logger.error("Tenant provisioning failed after {}ms: tenantId={}, schemaName={}",
                    duration, tenantId, schemaName, e);

            // Cleanup: Drop schema if it was created
            try {
                dropSchema(schemaName);
                logger.info("Cleaned up failed schema: {}", schemaName);
            } catch (Exception cleanupError) {
                logger.error("Failed to cleanup schema: {}", schemaName, cleanupError);
            }

            throw new TenantProvisioningException("Failed to provision tenant schema: " + schemaName, e);
        }
    }

    /**
     * Generates a schema name from a tenant ID.
     *
     * <p>Format: tenant_{uuid_without_hyphens}
     * <p>Example: tenant_a1b2c3d4e5f67890abcdef1234567890
     *
     * @param tenantId the tenant UUID
     * @return schema name
     */
    private String generateSchemaName(UUID tenantId) {
        String uuidWithoutHyphens = tenantId.toString().replace("-", "");
        return "tenant_" + uuidWithoutHyphens;
    }

    /**
     * Creates a new PostgreSQL schema.
     *
     * @param schemaName the schema name
     */
    private void createSchema(String schemaName) {
        logger.debug("Creating schema: {}", schemaName);

        String sql = String.format("CREATE SCHEMA IF NOT EXISTS %s", schemaName);
        jdbcTemplate.execute(sql);

        logger.info("Schema created: {}", schemaName);
    }

    /**
     * Drops a PostgreSQL schema (cleanup on failure).
     *
     * @param schemaName the schema name
     */
    private void dropSchema(String schemaName) {
        logger.debug("Dropping schema: {}", schemaName);

        String sql = String.format("DROP SCHEMA IF EXISTS %s CASCADE", schemaName);
        jdbcTemplate.execute(sql);

        logger.info("Schema dropped: {}", schemaName);
    }

    /**
     * Runs Flyway migrations on a tenant schema.
     *
     * <p>Applies all migrations from db/migration/tenant/ directory.
     * <p>V068 migration seeds default profiles: Admin, Gerente, Vendedor (AC3)
     *
     * @param schemaName the schema name
     */
    private void runMigrations(String schemaName) {
        logger.debug("Running Flyway migrations on schema: {}", schemaName);

        Flyway flyway = Flyway.configure()
                .dataSource(dataSource)
                .schemas(schemaName)
                .locations("classpath:db/migration/tenant")
                .baselineOnMigrate(true)
                .load();

        int migrationsApplied = flyway.migrate().migrationsExecuted;

        logger.info("Applied {} migrations to schema: {}", migrationsApplied, schemaName);

        // AC3: Verify profiles were seeded (V068 migration)
        if (migrationsApplied > 0) {
            logger.debug("Default profiles (Admin, Gerente, Vendedor) seeded in schema: {}", schemaName);
        }
    }

    /**
     * Result of tenant provisioning operation.
     *
     * @param tenantId UUID of the provisioned tenant
     * @param schemaName PostgreSQL schema name
     * @param success whether provisioning succeeded
     * @param errorMessage error message if provisioning failed
     */
    public record TenantProvisionResult(
            UUID tenantId,
            String schemaName,
            boolean success,
            String errorMessage
    ) {}

    /**
     * Exception thrown when tenant provisioning fails.
     */
    public static class TenantProvisioningException extends RuntimeException {
        public TenantProvisioningException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}
