package com.estoquecentral.company.adapter.in.dto;

import java.util.List;

/**
 * Response DTO for context switch operation (Story 9.1 - AC3).
 *
 * <p>Returns new JWT token with updated tenant context and user's roles in the new company.
 *
 * @param token New JWT token with updated tenantId and roles
 * @param tenantId Company's tenant ID (UUID)
 * @param companyName Name of the company switched to
 * @param roles User's roles in the new company context
 */
public record SwitchContextResponse(
        String token,
        String tenantId,
        String companyName,
        List<String> roles
) {}
