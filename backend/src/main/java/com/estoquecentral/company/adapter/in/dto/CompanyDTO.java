package com.estoquecentral.company.adapter.in.dto;

import com.estoquecentral.company.domain.Company;

import java.time.Instant;
import java.util.UUID;

/**
 * DTO for Company responses.
 *
 * @since 1.0
 */
public record CompanyDTO(
    UUID id,
    UUID tenantId,
    String schemaName,
    String name,
    String cnpj,
    String email,
    String phone,
    UUID ownerUserId,
    Instant createdAt,
    Instant updatedAt,
    boolean active
) {
    public static CompanyDTO from(Company company) {
        return new CompanyDTO(
            company.id(),
            company.tenantId(),
            company.schemaName(),
            company.name(),
            company.cnpj(),
            company.email(),
            company.phone(),
            company.ownerUserId(),
            company.createdAt(),
            company.updatedAt(),
            company.active()
        );
    }
}
