package com.estoquecentral.company.adapter.in.dto;

import com.estoquecentral.company.domain.CompanyUser;
import com.estoquecentral.auth.domain.User;

import java.time.Instant;
import java.util.UUID;

/**
 * Response DTO for collaborator invitation.
 *
 * <p>Used in Story 10.1 - Backend Endpoint for Inviting Collaborators.
 *
 * @since 1.0
 */
public record InviteCollaboratorResponse(
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
     * Creates a response DTO from CompanyUser and User entities.
     *
     * @param companyUser the company-user association
     * @param user the user entity
     * @return the response DTO
     */
    public static InviteCollaboratorResponse from(CompanyUser companyUser, User user) {
        return new InviteCollaboratorResponse(
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
