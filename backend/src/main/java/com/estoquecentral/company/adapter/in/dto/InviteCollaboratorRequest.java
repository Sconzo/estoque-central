package com.estoquecentral.company.adapter.in.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * Request DTO for inviting a collaborator to a company.
 *
 * <p>Used in Story 10.1 - Backend Endpoint for Inviting Collaborators.
 *
 * @since 1.0
 */
public record InviteCollaboratorRequest(
    /**
     * Email of the user to invite.
     * Can be an existing user or a new user (placeholder will be created).
     */
    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    String email,

    /**
     * Role to assign to the collaborator.
     * Examples: "ADMIN", "USER", "MANAGER"
     */
    @NotBlank(message = "Role is required")
    String role
) {
}
