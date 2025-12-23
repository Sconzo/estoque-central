package com.estoquecentral.company.adapter.in.dto;

import java.util.UUID;

/**
 * Response DTO for company creation (Story 8.1 - AC5).
 *
 * @param tenantId UUID of the provisioned tenant
 * @param nome Company business name
 * @param schemaName PostgreSQL schema name for the tenant
 * @param token JWT token for immediate access to the new company
 */
public record CreateCompanyResponse(
        UUID tenantId,
        String nome,
        String schemaName,
        String token
) {}
