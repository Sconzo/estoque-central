package com.estoquecentral.auth.application;

import com.estoquecentral.auth.adapter.out.ProfileRepository;
import com.estoquecentral.auth.adapter.out.ProfileRoleRepository;
import com.estoquecentral.auth.adapter.out.RoleRepository;
import com.estoquecentral.auth.domain.Profile;
import com.estoquecentral.auth.domain.ProfileRole;
import com.estoquecentral.auth.domain.Role;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * ProfileService - Application service for profile management
 *
 * <p>Handles profile lifecycle operations:
 * <ul>
 *   <li>List profiles for current tenant (via TenantContext)</li>
 *   <li>Create new profiles with roles</li>
 *   <li>Update profile metadata and roles</li>
 *   <li>Deactivate profiles (soft delete)</li>
 *   <li>Get roles for a profile</li>
 * </ul>
 *
 * <p><strong>Tenant Isolation:</strong> All operations use TenantContext for schema routing.
 * <p><strong>Security:</strong> All profile management operations require ADMIN role.
 *
 * @see Profile
 * @see ProfileRepository
 * @see com.estoquecentral.shared.tenant.TenantContext
 */
@Service
public class ProfileService {

    private static final Logger logger = LoggerFactory.getLogger(ProfileService.class);

    private final ProfileRepository profileRepository;
    private final ProfileRoleRepository profileRoleRepository;
    private final RoleRepository roleRepository;

    @Autowired
    public ProfileService(
            ProfileRepository profileRepository,
            ProfileRoleRepository profileRoleRepository,
            RoleRepository roleRepository) {
        this.profileRepository = profileRepository;
        this.profileRoleRepository = profileRoleRepository;
        this.roleRepository = roleRepository;
    }

    /**
     * Lists all active profiles for the current tenant.
     *
     * <p><strong>CRITICAL:</strong> TenantContext must be set before calling this method.
     *
     * @return list of active profiles for current tenant
     */
    public List<Profile> listActive() {
        logger.debug("Listing active profiles for current tenant");
        return profileRepository.findByAtivoTrue();
    }

    /**
     * Lists all profiles (active and inactive) for the current tenant.
     *
     * <p><strong>CRITICAL:</strong> TenantContext must be set before calling this method.
     *
     * @return list of all profiles for current tenant
     */
    public List<Profile> listAll() {
        logger.debug("Listing all profiles for current tenant");
        return profileRepository.findAll();
    }

    /**
     * Finds a profile by ID in the current tenant schema.
     *
     * @param id the profile ID
     * @return the Profile
     * @throws IllegalArgumentException if profile not found
     */
    public Profile getById(UUID id) {
        logger.debug("Finding profile by ID: {}", id);
        return profileRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Profile not found: " + id));
    }

    /**
     * Finds a profile by name in the current tenant schema.
     *
     * @param nome the profile name
     * @return the Profile
     * @throws IllegalArgumentException if profile not found
     */
    public Profile getByNome(String nome) {
        logger.debug("Finding profile by name: {}", nome);
        return profileRepository.findByNome(nome)
                .orElseThrow(() -> new IllegalArgumentException("Profile not found: " + nome));
    }

    /**
     * Creates a new profile with roles in the current tenant schema.
     *
     * <p><strong>Security:</strong> Requires ADMIN role
     * <p><strong>CRITICAL:</strong> TenantContext must be set before calling this method.
     *
     * @param nome      profile name (e.g., "Gerente Loja")
     * @param descricao human-readable description
     * @param roleIds   list of role IDs to assign to this profile
     * @return the created Profile
     * @throws IllegalArgumentException if profile name already exists for tenant
     */
    @Transactional
    public Profile create(String nome, String descricao, List<UUID> roleIds) {
        logger.info("Creating new profile: {}", nome);

        // Check if profile name already exists in current tenant schema
        if (profileRepository.existsByNome(nome)) {
            throw new IllegalArgumentException("Profile already exists: " + nome);
        }

        // Validate roles exist
        validateRolesExist(roleIds);

        // Create profile (tenant isolation via schema routing)
        Profile profile = new Profile(UUID.randomUUID(), nome, descricao);
        profile = profileRepository.save(profile);

        // Assign roles to profile
        assignRolesToProfile(profile.getId(), roleIds);

        logger.info("Profile created successfully: {}", profile);
        return profile;
    }

    /**
     * Updates a profile's metadata.
     *
     * <p><strong>Security:</strong> Requires ADMIN role
     *
     * @param id        profile ID
     * @param nome      new name (optional)
     * @param descricao new description (optional)
     * @return the updated Profile
     * @throws IllegalArgumentException if profile not found or name conflicts
     */
    @Transactional
    public Profile update(UUID id, String nome, String descricao) {
        logger.info("Updating profile: {}", id);

        Profile profile = getById(id);

        // Check if new name conflicts with existing profile in same tenant
        if (nome != null && !nome.equals(profile.getNome())) {
            if (profileRepository.existsByNome(nome)) {
                throw new IllegalArgumentException("Profile name already exists: " + nome);
            }
        }

        profile.update(nome, descricao);
        profile = profileRepository.save(profile);

        logger.info("Profile updated successfully: {}", profile);
        return profile;
    }

    /**
     * Updates roles for a profile.
     * Replaces all existing roles with the new list.
     *
     * <p><strong>Security:</strong> Requires ADMIN role
     *
     * @param profileId profile ID
     * @param roleIds   new list of role IDs
     * @throws IllegalArgumentException if profile not found or roles invalid
     */
    @Transactional
    public void updateRoles(UUID profileId, List<UUID> roleIds) {
        logger.info("Updating roles for profile: {}", profileId);

        // Validate profile exists
        getById(profileId);

        // Validate roles exist
        validateRolesExist(roleIds);

        // Replace roles
        assignRolesToProfile(profileId, roleIds);

        logger.info("Profile roles updated successfully");
    }

    /**
     * Deactivates a profile (soft delete).
     *
     * <p><strong>Security:</strong> Requires ADMIN role
     *
     * @param id the profile ID
     * @throws IllegalArgumentException if profile not found
     */
    @Transactional
    public void deactivate(UUID id) {
        logger.info("Deactivating profile: {}", id);

        Profile profile = getById(id);
        profile.deactivate();
        profileRepository.save(profile);

        logger.info("Profile deactivated successfully: {}", id);
    }

    /**
     * Activates a profile.
     *
     * <p><strong>Security:</strong> Requires ADMIN role
     *
     * @param id the profile ID
     * @throws IllegalArgumentException if profile not found
     */
    @Transactional
    public void activate(UUID id) {
        logger.info("Activating profile: {}", id);

        Profile profile = getById(id);
        profile.activate();
        profileRepository.save(profile);

        logger.info("Profile activated successfully: {}", id);
    }

    /**
     * Gets all roles for a profile.
     *
     * @param profileId the profile ID
     * @return list of roles
     */
    public List<Role> getRolesByProfile(UUID profileId) {
        logger.debug("Getting roles for profile: {}", profileId);

        List<ProfileRole> profileRoles = profileRoleRepository.findByProfileId(profileId);
        List<UUID> roleIds = profileRoles.stream()
                .map(ProfileRole::getRoleId)
                .collect(Collectors.toList());

        return roleIds.stream()
                .map(roleId -> roleRepository.findById(roleId).orElse(null))
                .filter(role -> role != null && role.getAtivo())
                .collect(Collectors.toList());
    }

    /**
     * Assigns roles to a profile.
     * Deletes existing associations and creates new ones.
     *
     * @param profileId profile ID
     * @param roleIds   list of role IDs to assign
     */
    @Transactional
    protected void assignRolesToProfile(UUID profileId, List<UUID> roleIds) {
        logger.debug("Assigning {} roles to profile: {}", roleIds.size(), profileId);

        // Delete existing role associations
        profileRoleRepository.deleteByProfileId(profileId);

        // Create new associations
        for (UUID roleId : roleIds) {
            ProfileRole profileRole = new ProfileRole(profileId, roleId);
            profileRoleRepository.save(profileRole);
        }

        logger.debug("Roles assigned successfully");
    }

    /**
     * Validates that all role IDs exist and are active.
     *
     * @param roleIds list of role IDs
     * @throws IllegalArgumentException if any role not found or inactive
     */
    private void validateRolesExist(List<UUID> roleIds) {
        for (UUID roleId : roleIds) {
            Role role = roleRepository.findById(roleId)
                    .orElseThrow(() -> new IllegalArgumentException("Role not found: " + roleId));

            if (!role.getAtivo()) {
                throw new IllegalArgumentException("Role is inactive: " + roleId);
            }
        }
    }
}
