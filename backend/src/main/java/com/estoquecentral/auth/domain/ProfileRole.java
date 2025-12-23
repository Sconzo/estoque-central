package com.estoquecentral.auth.domain;

import org.springframework.data.relational.core.mapping.Table;

import java.util.UUID;

/**
 * ProfileRole - Join table for Many-to-Many relationship Profile ↔ Role
 *
 * <p>This entity represents the association between a Profile and a Role.
 * One Profile can have multiple Roles, and one Role can be in multiple Profiles.
 *
 * <p><strong>Example:</strong>
 * <pre>
 * Profile: "Gerente Loja" (id=123)
 * Roles:
 *   - GERENTE (id=1)
 *   - VENDEDOR (id=2)
 *   - ESTOQUISTA (id=3)
 *
 * ProfileRole records:
 *   - (profile_id=123, role_id=1)
 *   - (profile_id=123, role_id=2)
 *   - (profile_id=123, role_id=3)
 * </pre>
 *
 * <p><strong>Storage:</strong> Lives in TENANT schema (schema-per-tenant isolation)
 * <p><strong>Note:</strong> role_id references public.roles (global roles shared across tenants)
 *
 * @see Profile
 * @see Role
 */
@Table("profile_roles")
public class ProfileRole {

    /**
     * FK to profiles.id
     */
    private UUID profileId;

    /**
     * FK to roles.id
     */
    private UUID roleId;

    /**
     * Default constructor for Spring Data JDBC.
     */
    public ProfileRole() {
    }

    /**
     * Constructor for creating a new profile-role association.
     *
     * @param profileId profile UUID
     * @param roleId    role UUID
     */
    public ProfileRole(UUID profileId, UUID roleId) {
        this.profileId = profileId;
        this.roleId = roleId;
    }

    // ========================================================================
    // Getters and Setters
    // ========================================================================

    public UUID getProfileId() {
        return profileId;
    }

    public void setProfileId(UUID profileId) {
        this.profileId = profileId;
    }

    public UUID getRoleId() {
        return roleId;
    }

    public void setRoleId(UUID roleId) {
        this.roleId = roleId;
    }

    @Override
    public String toString() {
        return "ProfileRole{" +
                "profileId=" + profileId +
                ", roleId=" + roleId +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ProfileRole that = (ProfileRole) o;

        if (!profileId.equals(that.profileId)) return false;
        return roleId.equals(that.roleId);
    }

    @Override
    public int hashCode() {
        int result = profileId.hashCode();
        result = 31 * result + roleId.hashCode();
        return result;
    }
}
