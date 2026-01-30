package com.estoquecentral.company.domain;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import java.time.Instant;
import java.util.UUID;

/**
 * Company aggregate root - represents a tenant/company in the multi-tenant system.
 *
 * <p>Each company has its own isolated database schema and can have multiple users
 * (collaborators) with different roles and permissions.</p>
 *
 * @since 1.0
 */
@Table("companies")
public record Company(
    @Id UUID id,
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
    /**
     * Creates a new company for self-service registration.
     * TenantId and schemaName will be set after tenant provisioning.
     */
    public static Company create(String name, String cnpj, String email, String phone, UUID ownerUserId) {
        Instant now = Instant.now();
        return new Company(
            null,
            null, // tenantId set after provisioning
            null, // schemaName set after provisioning
            name,
            cnpj,
            email,
            phone,
            ownerUserId,
            now,
            now,
            true
        );
    }

    /**
     * Sets tenant ID and schema name after successful provisioning.
     */
    public Company withTenantSchema(UUID tenantId, String schemaName) {
        return new Company(
            this.id,
            tenantId,
            schemaName,
            this.name,
            this.cnpj,
            this.email,
            this.phone,
            this.ownerUserId,
            this.createdAt,
            Instant.now(),
            this.active
        );
    }

    /**
     * Updates company information.
     */
    public Company update(String name, String email, String phone) {
        return new Company(
            this.id,
            this.tenantId,
            this.schemaName,
            name,
            this.cnpj,
            email,
            phone,
            this.ownerUserId,
            this.createdAt,
            Instant.now(),
            this.active
        );
    }

    /**
     * Deactivates the company (soft delete).
     */
    public Company deactivate() {
        return new Company(
            this.id,
            this.tenantId,
            this.schemaName,
            this.name,
            this.cnpj,
            this.email,
            this.phone,
            this.ownerUserId,
            this.createdAt,
            Instant.now(),
            false
        );
    }
}
