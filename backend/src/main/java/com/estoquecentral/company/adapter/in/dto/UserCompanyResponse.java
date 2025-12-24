package com.estoquecentral.company.adapter.in.dto;

import java.util.UUID;

/**
 * DTO for user's company list response (Story 8.4 - AC2).
 *
 * <p>Returns company information along with user's profile/role in that company.
 *
 * @param tenantId Tenant UUID
 * @param nome Company name
 * @param cnpj Company CNPJ
 * @param profileId User's profile ID in this company
 * @param profileName User's profile name (e.g., "Admin", "Gerente", "Vendedor")
 *
 * @since 1.0
 */
public record UserCompanyResponse(
    UUID tenantId,
    String nome,
    String cnpj,
    UUID profileId,
    String profileName
) {
}
