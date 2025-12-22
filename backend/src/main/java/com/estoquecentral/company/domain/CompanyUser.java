package com.estoquecentral.company.domain;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import java.time.Instant;

/**
 * CompanyUser - represents the association between a user and a company with a specific role.
 *
 * <p>This entity enables multi-company access for users, allowing a single user to be
 * a collaborator in multiple companies with different roles in each.</p>
 *
 * @since 1.0
 */
@Table("company_users")
public record CompanyUser(
    @Id Long id,
    Long companyId,
    Long userId,
    String role,
    Instant invitedAt,
    Instant acceptedAt,
    boolean active
) {
    /**
     * Creates a new company-user association (invitation).
     */
    public static CompanyUser invite(Long companyId, Long userId, String role) {
        return new CompanyUser(
            null,
            companyId,
            userId,
            role,
            Instant.now(),
            null,
            false
        );
    }

    /**
     * Accepts the invitation and activates the association.
     */
    public CompanyUser accept() {
        return new CompanyUser(
            this.id,
            this.companyId,
            this.userId,
            this.role,
            this.invitedAt,
            Instant.now(),
            true
        );
    }

    /**
     * Updates the user's role in the company.
     */
    public CompanyUser updateRole(String newRole) {
        return new CompanyUser(
            this.id,
            this.companyId,
            this.userId,
            newRole,
            this.invitedAt,
            this.acceptedAt,
            this.active
        );
    }

    /**
     * Removes the user from the company (soft delete).
     */
    public CompanyUser deactivate() {
        return new CompanyUser(
            this.id,
            this.companyId,
            this.userId,
            this.role,
            this.invitedAt,
            this.acceptedAt,
            false
        );
    }
}
