package com.estoquecentral.tenant.adapter.in.dto;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * TenantSettingDTO - Data transfer object for tenant settings
 * Story 4.6: Stock Reservation and Automatic Release
 */
public record TenantSettingDTO(
    UUID id,
    UUID tenantId,
    String settingKey,
    String settingValue,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {}
