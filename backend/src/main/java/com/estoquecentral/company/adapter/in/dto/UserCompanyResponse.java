package com.estoquecentral.company.adapter.in.dto;

import java.util.UUID;

/**
 * DTO for user's company list response (Story 8.4 - AC2).
 *
 * <p>Returns company information along with user's profile/role in that company.
 *
 * @param id Company ID
 * @param tenantId Tenant UUID (as String)
 * @param nome Company name
 * @param cnpj Company CNPJ
 * @param profileId User's profile ID in this company (as String)
 * @param profileName User's profile name (e.g., "Admin", "Gerente", "Vendedor")
 *
 * @since 1.0
 */
public record UserCompanyResponse(
    UUID id,
    String tenantId,
    String nome,
    String cnpj,
    String profileId,
    String profileName
) {
}
