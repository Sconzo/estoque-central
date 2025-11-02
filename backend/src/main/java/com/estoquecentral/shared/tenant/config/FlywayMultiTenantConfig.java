package com.estoquecentral.shared.tenant.config;

import com.estoquecentral.auth.adapter.out.TenantRepository;
import jakarta.annotation.PostConstruct;
import org.flywaydb.core.Flyway;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;

import javax.sql.DataSource;
import java.util.List;

/**
 * FlywayMultiTenantConfig - Manages Flyway migrations for multi-tenant schemas
 *
 * <p>This configuration class runs Flyway migrations on all tenant schemas
 * automatically during application startup.
 *
 * <p><strong>Migration Strategy:</strong>
 * <ol>
 *   <li><strong>Public Schema</strong>: Spring Boot's default Flyway runs V001 on public schema</li>
 *   <li><strong>Tenant Schemas</strong>: This class discovers all tenant schemas and runs V002 on each</li>
 * </ol>
 *
 * <p><strong>Directory Structure:</strong>
 * <pre>
 * db/migration/
 * ├── V001__create_tenants_table.sql       (applied to public schema)
 * └── tenant/
 *     └── V002__create_tenant_schema.sql   (applied to each tenant schema)
 * </pre>
 *
 * <p><strong>Important:</strong> This configuration depends on Spring Boot's
 * default Flyway having already run (to create public.tenants table).
 *
 * @see TenantRepository
 * @see com.estoquecentral.auth.application.TenantService
 */
@Configuration
@DependsOn("flywayInitializer") // Ensure public schema migrations run first
public class FlywayMultiTenantConfig {

    private static final Logger logger = LoggerFactory.getLogger(FlywayMultiTenantConfig.class);

    private final DataSource dataSource;
    private final TenantRepository tenantRepository;

    @Autowired
    public FlywayMultiTenantConfig(DataSource dataSource, TenantRepository tenantRepository) {
        this.dataSource = dataSource;
        this.tenantRepository = tenantRepository;
    }

    /**
     * Runs Flyway migrations on all tenant schemas after application startup.
     *
     * <p>This method is called automatically by Spring after bean initialization.
     * It discovers all active tenant schemas from public.tenants table and
     * applies migrations from db/migration/tenant/ to each schema.
     *
     * <p><strong>Execution Flow:</strong>
     * <ol>
     *   <li>Wait for default Flyway to create public.tenants table</li>
     *   <li>Query all schema names from public.tenants WHERE ativo=true</li>
     *   <li>For each schema:
     *     <ul>
     *       <li>Configure Flyway with locations=classpath:db/migration/tenant</li>
     *       <li>Run migrate() to apply V002__create_tenant_schema.sql</li>
     *     </ul>
     *   </li>
     * </ol>
     *
     * <p><strong>Note:</strong> If a tenant schema already has migrations applied,
     * Flyway will skip them (idempotent).
     */
    @PostConstruct
    public void migrateTenantSchemas() {
        logger.info("Starting multi-tenant Flyway migrations...");

        try {
            // Retrieve all active tenant schema names
            List<String> schemaNames = tenantRepository.findAllSchemaNames();

            if (schemaNames.isEmpty()) {
                logger.info("No tenant schemas found. Skipping tenant migrations.");
                return;
            }

            logger.info("Found {} tenant schemas to migrate: {}", schemaNames.size(), schemaNames);

            // Migrate each tenant schema
            int successCount = 0;
            int skipCount = 0;
            int failCount = 0;

            for (String schemaName : schemaNames) {
                try {
                    int migrationsApplied = migrateSingleTenantSchema(schemaName);

                    if (migrationsApplied > 0) {
                        successCount++;
                    } else {
                        skipCount++;
                    }

                } catch (Exception e) {
                    failCount++;
                    logger.error("Failed to migrate tenant schema: {}", schemaName, e);
                    // Continue with other schemas instead of failing completely
                }
            }

            logger.info("Multi-tenant Flyway migrations completed: {} migrated, {} skipped, {} failed",
                    successCount, skipCount, failCount);

            if (failCount > 0) {
                logger.warn("Some tenant schemas failed to migrate. Check logs above for details.");
            }

        } catch (Exception e) {
            logger.error("Failed to run multi-tenant Flyway migrations", e);
            throw new RuntimeException("Multi-tenant migration failed", e);
        }
    }

    /**
     * Runs Flyway migrations on a single tenant schema.
     *
     * @param schemaName the schema name (e.g., "tenant_a1b2c3d4...")
     * @return number of migrations applied
     */
    private int migrateSingleTenantSchema(String schemaName) {
        logger.debug("Migrating tenant schema: {}", schemaName);

        Flyway flyway = Flyway.configure()
                .dataSource(dataSource)
                .schemas(schemaName)
                .locations("classpath:db/migration/tenant")
                .baselineOnMigrate(true)
                .load();

        int migrationsApplied = flyway.migrate().migrationsExecuted;

        if (migrationsApplied > 0) {
            logger.info("Applied {} migrations to schema: {}", migrationsApplied, schemaName);
        } else {
            logger.debug("No new migrations for schema: {} (already up-to-date)", schemaName);
        }

        return migrationsApplied;
    }
}
