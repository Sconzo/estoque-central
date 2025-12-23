package com.estoquecentral.auth.adapter.out;

import com.estoquecentral.auth.domain.ProfileRole;
import org.springframework.data.jdbc.repository.query.Modifying;
import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

/**
 * ProfileRoleRepository - Data access for ProfileRole join table
 *
 * <p>This repository operates on the TENANT schema (schema-per-tenant).
 * Manages the Many-to-Many relationship between Profiles and Roles.
 *
 * <p><strong>CRITICAL:</strong> TenantContext must be set before calling these methods.
 *
 * @see ProfileRole
 * @see com.estoquecentral.auth.domain.Profile
 * @see com.estoquecentral.auth.domain.Role
 * @see com.estoquecentral.shared.tenant.TenantContext
 */
@Repository
public interface ProfileRoleRepository extends CrudRepository<ProfileRole, UUID> {

    /**
     * Finds all profile-role associations for a given profile in current tenant schema.
     *
     * @param profileId the profile ID
     * @return list of ProfileRole associations
     */
    @Query("SELECT * FROM profile_roles WHERE profile_id = :profileId")
    List<ProfileRole> findByProfileId(@Param("profileId") UUID profileId);

    /**
     * Finds all profile-role associations for a given role in current tenant schema.
     *
     * @param roleId the role ID
     * @return list of ProfileRole associations
     */
    @Query("SELECT * FROM profile_roles WHERE role_id = :roleId")
    List<ProfileRole> findByRoleId(@Param("roleId") UUID roleId);

    /**
     * Deletes all role associations for a given profile in current tenant schema.
     * Used when updating profile roles (delete all, then insert new ones).
     *
     * @param profileId the profile ID
     */
    @Modifying
    @Query("DELETE FROM profile_roles WHERE profile_id = :profileId")
    void deleteByProfileId(@Param("profileId") UUID profileId);

    /**
     * Checks if a profile has a specific role in current tenant schema.
     *
     * @param profileId the profile ID
     * @param roleId    the role ID
     * @return true if association exists, false otherwise
     */
    @Query("SELECT COUNT(*) > 0 FROM profile_roles WHERE profile_id = :profileId AND role_id = :roleId")
    boolean existsByProfileIdAndRoleId(@Param("profileId") UUID profileId, @Param("roleId") UUID roleId);
}
