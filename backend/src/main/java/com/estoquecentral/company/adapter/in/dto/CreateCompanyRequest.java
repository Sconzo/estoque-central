package com.estoquecentral.company.adapter.in.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * Request DTO for creating a new company (Story 8.1 - AC1).
 *
 * <p>This endpoint is public (no JWT required) to allow new users to
 * create their first company immediately after OAuth login.
 *
 * @param nome Company business name (required)
 * @param cnpj Brazilian tax ID (required)
 * @param email Company contact email (required)
 * @param telefone Company phone (optional)
 * @param userId User ID of the company owner (required)
 */
public record CreateCompanyRequest(
        @NotBlank(message = "Company name is required")
        String nome,

        @NotBlank(message = "CNPJ is required")
        String cnpj,

        @NotBlank(message = "Email is required")
        @Email(message = "Invalid email format")
        String email,

        String telefone,

        @NotNull(message = "User ID is required")
        Long userId
) {}
