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
 * <p>This repository operates on the PUBLIC schema.
 * Profiles are tenant-specific (filtered by tenant_id column).
 *
 * @see Profile
 */
@Repository
public interface ProfileRepository extends CrudRepository<Profile, UUID> {

    /**
     * Finds all active profiles for a given tenant.
     *
     * @param tenantId the tenant ID
     * @return list of active profiles for the tenant
     */
    @Query("SELECT * FROM public.profiles WHERE tenant_id = :tenantId AND ativo = true ORDER BY nome")
    List<Profile> findByTenantIdAndAtivoTrue(@Param("tenantId") UUID tenantId);

    /**
     * Finds all profiles for a given tenant (active and inactive).
     *
     * @param tenantId the tenant ID
     * @return list of all profiles for the tenant
     */
    @Query("SELECT * FROM public.profiles WHERE tenant_id = :tenantId ORDER BY nome")
    List<Profile> findByTenantId(@Param("tenantId") UUID tenantId);

    /**
     * Finds a profile by tenant ID and name.
     *
     * @param tenantId the tenant ID
     * @param nome     the profile name
     * @return Optional containing the profile if found
     */
    @Query("SELECT * FROM public.profiles WHERE tenant_id = :tenantId AND nome = :nome")
    Optional<Profile> findByTenantIdAndNome(@Param("tenantId") UUID tenantId, @Param("nome") String nome);

    /**
     * Checks if a profile exists by tenant ID and name.
     *
     * @param tenantId the tenant ID
     * @param nome     the profile name
     * @return true if exists, false otherwise
     */
    @Query("SELECT COUNT(*) > 0 FROM public.profiles WHERE tenant_id = :tenantId AND nome = :nome")
    boolean existsByTenantIdAndNome(@Param("tenantId") UUID tenantId, @Param("nome") String nome);
}
