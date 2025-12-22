package com.estoquecentral.tenant.application;

import org.flywaydb.core.Flyway;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.sql.DataSource;
import java.util.UUID;

/**
 * TenantProvisioner - Service for dynamic tenant schema provisioning.
 *
 * <p>This service handles the creation and setup of new tenant schemas following
 * the schema-per-tenant isolation strategy. It creates a dedicated PostgreSQL schema
 * for each tenant and applies all necessary migrations.</p>
 *
 * <p><strong>Provisioning Process:</strong></p>
 * <ol>
 *   <li>Generate unique schema name: tenant_{uuid_without_hyphens}</li>
 *   <li>Create PostgreSQL schema</li>
 *   <li>Apply Flyway migrations from db/migration/tenant/</li>
 *   <li>Seed default data (profiles, roles)</li>
 * </ol>
 *
 * <p><strong>Performance Requirements (NFR2):</strong></p>
 * <ul>
 *   <li>Complete provisioning in < 30 seconds</li>
 *   <li>Success rate > 99% (NFR1)</li>
 * </ul>
 *
 * @since 1.0
 */
@Service
public class TenantProvisioner {

    private static final Logger logger = LoggerFactory.getLogger(TenantProvisioner.class);
    private static final String SCHEMA_PREFIX = "tenant_";

    private final DataSource dataSource;
    private final JdbcTemplate jdbcTemplate;

    public TenantProvisioner(DataSource dataSource, JdbcTemplate jdbcTemplate) {
        this.dataSource = dataSource;
        this.jdbcTemplate = jdbcTemplate;
    }

    /**
     * Provisions a new tenant schema with all necessary tables and seed data.
     *
     * <p>This method is atomic - if any step fails, the entire operation is rolled back.</p>
     *
     * @param tenantId Tenant UUID (from companies table)
     * @return Schema name created (e.g., "tenant_a1b2c3d4...")
     * @throws TenantProvisioningException if provisioning fails
     */
    @Transactional
    public String provisionTenantSchema(UUID tenantId) {
        long startTime = System.currentTimeMillis();

        try {
            logger.info("Starting tenant schema provisioning for tenant: {}", tenantId);

            // Step 1: Generate schema name
            String schemaName = generateSchemaName(tenantId);
            logger.debug("Generated schema name: {}", schemaName);

            // Step 2: Create PostgreSQL schema
            createSchema(schemaName);
            logger.debug("Created PostgreSQL schema: {}", schemaName);

            // Step 3: Apply Flyway migrations
            int migrationsApplied = applyMigrations(schemaName);
            logger.debug("Applied {} migrations to schema: {}", migrationsApplied, schemaName);

            // Step 4: Seed default data (profiles will be seeded by migration)
            // Additional seeding can be done here if needed

            long durationMs = System.currentTimeMillis() - startTime;
            logger.info("Successfully provisioned tenant schema: {} in {}ms", schemaName, durationMs);

            // NFR2 validation: should complete in < 30 seconds
            if (durationMs > 30000) {
                logger.warn("Tenant provisioning exceeded 30s threshold: {}ms for schema: {}",
                    durationMs, schemaName);
            }

            return schemaName;

        } catch (Exception e) {
            long durationMs = System.currentTimeMillis() - startTime;
            logger.error("Failed to provision tenant schema for tenant: {} after {}ms",
                tenantId, durationMs, e);
            throw new TenantProvisioningException(
                "Failed to provision tenant schema for tenant: " + tenantId, e);
        }
    }

    /**
     * Generates a unique schema name from tenant UUID.
     *
     * <p>Format: tenant_{uuid_without_hyphens}</p>
     * <p>Example: tenant_a1b2c3d4e5f67890abcdef1234567890</p>
     *
     * @param tenantId Tenant UUID
     * @return Schema name
     */
    private String generateSchemaName(UUID tenantId) {
        String uuidWithoutHyphens = tenantId.toString().replace("-", "");
        return SCHEMA_PREFIX + uuidWithoutHyphens;
    }

    /**
     * Creates a new PostgreSQL schema.
     *
     * <p>Uses CREATE SCHEMA IF NOT EXISTS to be idempotent.</p>
     *
     * @param schemaName Schema name to create
     */
    private void createSchema(String schemaName) {
        try {
            String sql = "CREATE SCHEMA IF NOT EXISTS " + schemaName;
            jdbcTemplate.execute(sql);
            logger.debug("Executed: {}", sql);
        } catch (Exception e) {
            throw new TenantProvisioningException(
                "Failed to create schema: " + schemaName, e);
        }
    }

    /**
     * Applies Flyway migrations to the tenant schema.
     *
     * <p>Migrations are loaded from classpath:db/migration/tenant/</p>
     *
     * @param schemaName Target schema name
     * @return Number of migrations applied
     */
    private int applyMigrations(String schemaName) {
        try {
            Flyway flyway = Flyway.configure()
                    .dataSource(dataSource)
                    .schemas(schemaName)
                    .locations("classpath:db/migration/tenant")
                    .baselineOnMigrate(true)
                    .load();

            int migrationsApplied = flyway.migrate().migrationsExecuted;

            if (migrationsApplied == 0) {
                logger.warn("No migrations applied to new schema: {}. Check migration files.", schemaName);
            }

            return migrationsApplied;

        } catch (Exception e) {
            throw new TenantProvisioningException(
                "Failed to apply migrations to schema: " + schemaName, e);
        }
    }

    /**
     * Drops a tenant schema (for testing or tenant deletion).
     *
     * <p><strong>WARNING:</strong> This is a destructive operation. Use with caution.</p>
     *
     * @param schemaName Schema to drop
     */
    @Transactional
    public void dropTenantSchema(String schemaName) {
        logger.warn("Dropping tenant schema: {}", schemaName);

        try {
            String sql = "DROP SCHEMA IF EXISTS " + schemaName + " CASCADE";
            jdbcTemplate.execute(sql);
            logger.info("Successfully dropped schema: {}", schemaName);
        } catch (Exception e) {
            logger.error("Failed to drop schema: {}", schemaName, e);
            throw new TenantProvisioningException(
                "Failed to drop schema: " + schemaName, e);
        }
    }

    /**
     * Exception thrown when tenant provisioning fails.
     */
    public static class TenantProvisioningException extends RuntimeException {
        public TenantProvisioningException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}
