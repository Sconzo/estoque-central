package com.estoquecentral.auth.application;

import com.estoquecentral.auth.adapter.out.TenantRepository;
import com.estoquecentral.auth.domain.Tenant;
import org.flywaydb.core.Flyway;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.sql.DataSource;
import java.util.List;
import java.util.UUID;

/**
 * TenantService - Application service for tenant management
 *
 * <p>Handles tenant lifecycle operations:
 * <ul>
 *   <li>Create new tenant (schema + metadata)</li>
 *   <li>List all tenants</li>
 *   <li>Activate/deactivate tenants</li>
 * </ul>
 *
 * <p><strong>Tenant Creation Process:</strong>
 * <ol>
 *   <li>Generate UUID for tenant ID</li>
 *   <li>Generate schema name: tenant_{uuid_without_hyphens}</li>
 *   <li>Insert metadata into public.tenants table</li>
 *   <li>Execute DDL: CREATE SCHEMA tenant_{uuid}</li>
 *   <li>Run Flyway migrations on new schema (V002__create_tenant_schema.sql)</li>
 * </ol>
 *
 * @see Tenant
 * @see TenantRepository
 * @see com.estoquecentral.shared.tenant.config.FlywayMultiTenantConfig
 */
@Service
public class TenantService {

    private static final Logger logger = LoggerFactory.getLogger(TenantService.class);

    private final TenantRepository tenantRepository;
    private final JdbcTemplate jdbcTemplate;
    private final DataSource dataSource;

    @Autowired
    public TenantService(
            TenantRepository tenantRepository,
            JdbcTemplate jdbcTemplate,
            DataSource dataSource) {
        this.tenantRepository = tenantRepository;
        this.jdbcTemplate = jdbcTemplate;
        this.dataSource = dataSource;
    }

    /**
     * Creates a new tenant with isolated schema.
     *
     * <p>This method performs multiple operations atomically:
     * <ol>
     *   <li>Validates tenant doesn't already exist (by email)</li>
     *   <li>Generates UUID and schema name</li>
     *   <li>Inserts tenant metadata into public.tenants</li>
     *   <li>Creates PostgreSQL schema</li>
     *   <li>Runs Flyway migrations on new schema</li>
     * </ol>
     *
     * @param nome  tenant business name
     * @param email tenant contact email
     * @return the created Tenant entity
     * @throws IllegalArgumentException if tenant with email already exists
     * @throws RuntimeException         if schema creation or migration fails
     */
    @Transactional
    public Tenant createTenant(String nome, String email) {
        logger.info("Creating new tenant: nome={}, email={}", nome, email);

        // Validate tenant doesn't exist
        if (tenantRepository.findByEmail(email).isPresent()) {
            throw new IllegalArgumentException("Tenant with email " + email + " already exists");
        }

        // Generate UUID and schema name
        UUID tenantId = UUID.randomUUID();
        String schemaName = "tenant_" + tenantId.toString().replace("-", "");

        logger.debug("Generated tenant ID: {}, schema name: {}", tenantId, schemaName);

        // Create tenant entity
        Tenant tenant = new Tenant(tenantId, nome, schemaName, email);

        // Save metadata to public.tenants table
        tenant = tenantRepository.save(tenant);
        logger.info("Tenant metadata saved to public.tenants: {}", tenant);

        try {
            // Create PostgreSQL schema
            createSchema(schemaName);

            // Run Flyway migrations on new schema
            migrateTenantSchema(schemaName);

            logger.info("Tenant created successfully: id={}, schema={}", tenantId, schemaName);
            return tenant;

        } catch (Exception e) {
            // If schema creation or migration fails, rollback transaction
            logger.error("Failed to create tenant schema or run migrations: {}", schemaName, e);
            throw new RuntimeException("Failed to create tenant: " + e.getMessage(), e);
        }
    }

    /**
     * Retrieves all active tenants.
     *
     * @return list of active tenants
     */
    public List<Tenant> getAllActiveTenants() {
        return tenantRepository.findAllActive();
    }

    /**
     * Retrieves all schema names (for Flyway multi-tenant config).
     *
     * @return list of schema names
     */
    public List<String> getAllSchemaNames() {
        return tenantRepository.findAllSchemaNames();
    }

    /**
     * Creates a PostgreSQL schema for the tenant.
     *
     * <p><strong>IMPORTANT:</strong> Schema name is sanitized (UUID format)
     * to prevent SQL injection.
     *
     * @param schemaName the schema name to create
     * @throws RuntimeException if schema creation fails
     */
    private void createSchema(String schemaName) {
        logger.debug("Creating PostgreSQL schema: {}", schemaName);

        // Validate schema name format (must be tenant_{uuid})
        if (!schemaName.matches("^tenant_[a-f0-9]{32}$")) {
            throw new IllegalArgumentException("Invalid schema name format: " + schemaName);
        }

        String sql = String.format("CREATE SCHEMA IF NOT EXISTS %s", schemaName);
        jdbcTemplate.execute(sql);

        logger.info("Schema created successfully: {}", schemaName);
    }

    /**
     * Runs Flyway migrations on a tenant schema.
     *
     * <p>Applies migration V002__create_tenant_schema.sql to the new schema,
     * creating all business tables (produtos, vendas, etc.).
     *
     * @param schemaName the schema name to migrate
     * @throws RuntimeException if migration fails
     */
    private void migrateTenantSchema(String schemaName) {
        logger.debug("Running Flyway migrations on schema: {}", schemaName);

        Flyway flyway = Flyway.configure()
                .dataSource(dataSource)
                .schemas(schemaName)
                .locations("classpath:db/migration/tenant")
                .baselineOnMigrate(true)
                .load();

        int migrationsApplied = flyway.migrate().migrationsExecuted;
        logger.info("Applied {} migrations to schema: {}", migrationsApplied, schemaName);
    }
}
