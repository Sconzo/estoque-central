package com.estoquecentral.company.adapter.in.dto;

import com.estoquecentral.company.domain.CompanyUser;

import java.time.Instant;
import java.util.UUID;

/**
 * DTO for CompanyUser (collaborator) responses.
 *
 * @since 1.0
 */
public record CollaboratorDTO(
    UUID id,
    UUID companyId,
    UUID userId,
    String role,
    Instant invitedAt,
    Instant acceptedAt,
    boolean active
) {
    public static CollaboratorDTO from(CompanyUser companyUser) {
        return new CollaboratorDTO(
            companyUser.id(),
            companyUser.companyId(),
            companyUser.userId(),
            companyUser.role(),
            companyUser.invitedAt(),
            companyUser.acceptedAt(),
            companyUser.active()
        );
    }
}
