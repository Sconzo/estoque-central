package com.estoquecentral.company.adapter.in.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * Request DTO for switching company context (Story 9.1 - AC1).
 *
 * <p>This endpoint allows authenticated users to switch between companies
 * they have access to without logging out and back in.
 *
 * <p><strong>Validation Rules (Story 9.1 - AC1, AC2):</strong>
 * <ul>
 *   <li>tenantId: required, valid UUID format</li>
 *   <li>Backend validates user has access to this tenant</li>
 *   <li>Backend validates company_users.active = true</li>
 * </ul>
 *
 * @param tenantId Company's tenant ID (UUID) to switch to
 */
public record SwitchContextRequest(
        @NotBlank(message = "Tenant ID é obrigatório")
        String tenantId
) {}
