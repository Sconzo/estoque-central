package com.estoquecentral.company.adapter.in.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * Request DTO for creating a new company (Story 8.1 - AC1, Story 8.5 - AC1).
 *
 * <p>This endpoint is public (no JWT required) to allow new users to
 * create their first company immediately after OAuth login.
 *
 * <p><strong>Validation Rules (Story 8.5 - AC1):</strong>
 * <ul>
 *   <li>nome: required, max 255 characters</li>
 *   <li>email: required, valid email format</li>
 *   <li>cnpj: optional, 14 digits if provided</li>
 *   <li>telefone: optional</li>
 *   <li>userId: required</li>
 * </ul>
 *
 * @param nome Company business name (required, max 255 chars)
 * @param cnpj Brazilian tax ID (optional, 14 digits if provided)
 * @param email Company contact email (required, email format)
 * @param telefone Company phone (optional)
 * @param userId User ID of the company owner (required)
 */
public record CreateCompanyRequest(
        @NotBlank(message = "Nome da empresa é obrigatório")
        @Size(max = 255, message = "Nome da empresa deve ter no máximo 255 caracteres")
        String nome,

        @Pattern(regexp = "^\\d{14}$|^$", message = "CNPJ deve conter exatamente 14 dígitos numéricos")
        String cnpj,

        @NotBlank(message = "Email é obrigatório")
        @Email(message = "Email inválido. Por favor, forneça um email válido")
        String email,

        String telefone,

        @NotBlank(message = "ID do usuário é obrigatório")
        String userId
) {}
