package com.estoquecentral.auth.adapter.out;

import com.estoquecentral.auth.domain.Tenant;
import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * TenantRepository - Data access for Tenant entities
 *
 * <p>This repository operates on the PUBLIC schema (not tenant schemas).
 * It manages metadata about all tenants in the system.
 *
 * <p>Spring Data JDBC provides automatic CRUD operations.
 * Custom queries are defined using @Query annotation.
 *
 * @see Tenant
 * @see com.estoquecentral.auth.application.TenantService
 */
@Repository
public interface TenantRepository extends CrudRepository<Tenant, UUID> {

    /**
     * Finds a tenant by schema name.
     *
     * @param schemaName the schema name (e.g., "tenant_a1b2c3d4...")
     * @return Optional containing the tenant if found
     */
    Optional<Tenant> findBySchemaName(String schemaName);

    /**
     * Finds a tenant by email.
     *
     * @param email the tenant email
     * @return Optional containing the tenant if found
     */
    Optional<Tenant> findByEmail(String email);

    /**
     * Finds all active tenants.
     *
     * @return list of active tenants
     */
    @Query("SELECT * FROM public.tenants WHERE ativo = true ORDER BY data_criacao DESC")
    List<Tenant> findAllActive();

    /**
     * Retrieves all schema names for active tenants.
     *
     * <p>This is used by Flyway multi-tenant configuration to discover
     * which schemas need migrations.
     *
     * @return list of schema names (e.g., ["tenant_abc123", "tenant_def456"])
     */
    @Query("SELECT schema_name FROM public.tenants WHERE ativo = true")
    List<String> findAllSchemaNames();

    /**
     * Checks if a schema name already exists.
     *
     * @param schemaName the schema name to check
     * @return true if exists, false otherwise
     */
    @Query("SELECT COUNT(*) > 0 FROM public.tenants WHERE schema_name = :schemaName")
    boolean existsBySchemaName(String schemaName);
}
