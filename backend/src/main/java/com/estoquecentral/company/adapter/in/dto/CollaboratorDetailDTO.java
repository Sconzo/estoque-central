package com.estoquecentral.company.adapter.in.dto;

import com.estoquecentral.auth.domain.User;
import com.estoquecentral.company.domain.CompanyUser;

import java.time.Instant;
import java.util.UUID;

/**
 * Detailed DTO for collaborator listing (Story 10.2).
 *
 * <p>Includes both CompanyUser association data and User profile information.
 *
 * @since 1.0
 */
public record CollaboratorDetailDTO(
    UUID id,
    UUID companyId,
    UUID userId,
    String userName,
    String userEmail,
    String role,
    Instant invitedAt,
    Instant acceptedAt,
    boolean active
) {
    /**
     * Creates a detailed DTO from CompanyUser and User entities.
     *
     * @param companyUser the company-user association
     * @param user the user entity
     * @return the detailed DTO
     */
    public static CollaboratorDetailDTO from(CompanyUser companyUser, User user) {
        return new CollaboratorDetailDTO(
            companyUser.id(),
            companyUser.companyId(),
            companyUser.userId(),
            user.getNome(),
            user.getEmail(),
            companyUser.role(),
            companyUser.invitedAt(),
            companyUser.acceptedAt(),
            companyUser.active()
        );
    }
}
