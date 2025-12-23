package com.estoquecentral.auth.adapter.out;

import com.estoquecentral.auth.domain.Profile;
import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * ProfileRepository - Data access for Profile entities
 *
 * <p>This repository operates on the TENANT schema (schema-per-tenant).
 * Tenant isolation is handled by TenantRoutingDataSource + search_path.
 *
 * <p><strong>CRITICAL:</strong> TenantContext must be set before calling these methods.
 * Otherwise, queries will fail as public schema no longer has profiles table.
 *
 * @see Profile
 * @see com.estoquecentral.shared.tenant.TenantContext
 * @see com.estoquecentral.shared.tenant.TenantRoutingDataSource
 */
@Repository
public interface ProfileRepository extends CrudRepository<Profile, UUID> {

    /**
     * Finds all active profiles in the current tenant schema.
     *
     * <p>Tenant context must be set via TenantContext.setTenantId() before calling.
     *
     * @return list of active profiles for current tenant
     */
    @Query("SELECT * FROM profiles WHERE ativo = true ORDER BY nome")
    List<Profile> findByAtivoTrue();

    /**
     * Finds all profiles in the current tenant schema (active and inactive).
     *
     * <p>Tenant context must be set via TenantContext.setTenantId() before calling.
     *
     * @return list of all profiles for current tenant
     */
    @Query("SELECT * FROM profiles ORDER BY nome")
    List<Profile> findAll();

    /**
     * Finds a profile by name in the current tenant schema.
     *
     * <p>Tenant context must be set via TenantContext.setTenantId() before calling.
     *
     * @param nome the profile name
     * @return Optional containing the profile if found
     */
    @Query("SELECT * FROM profiles WHERE nome = :nome")
    Optional<Profile> findByNome(@Param("nome") String nome);

    /**
     * Checks if a profile exists by name in the current tenant schema.
     *
     * <p>Tenant context must be set via TenantContext.setTenantId() before calling.
     *
     * @param nome the profile name
     * @return true if exists, false otherwise
     */
    @Query("SELECT COUNT(*) > 0 FROM profiles WHERE nome = :nome")
    boolean existsByNome(@Param("nome") String nome);
}
