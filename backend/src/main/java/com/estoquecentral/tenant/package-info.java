/**
 * Tenant Module - Multi-tenancy management and tenant settings.
 *
 * <p>This module handles tenant-specific configurations and settings using Spring Modulith
 * to enforce bounded context boundaries. It manages tenant isolation and configuration
 * for the multi-tenant architecture using schema-per-tenant strategy.</p>
 *
 * <p><strong>Responsibilities:</strong></p>
 * <ul>
 *   <li>Tenant settings management (auto-release days, etc.)</li>
 *   <li>Tenant-specific configuration storage</li>
 *   <li>Tenant context and isolation support</li>
 * </ul>
 *
 * <p><strong>Architecture:</strong> Hexagonal (Ports & Adapters)</p>
 * <ul>
 *   <li><strong>domain/</strong>: Core business entities (TenantSetting)</li>
 *   <li><strong>application/</strong>: Use cases and services (TenantSettingsService)</li>
 *   <li><strong>adapter/in/</strong>: Inbound adapters (REST controllers)</li>
 *   <li><strong>adapter/out/</strong>: Outbound adapters (repositories)</li>
 * </ul>
 *
 * <p><strong>Dependencies:</strong></p>
 * <ul>
 *   <li>shared.tenant - Tenant context and routing infrastructure</li>
 * </ul>
 *
 * @since 1.0
 */
@org.springframework.modulith.ApplicationModule(
    displayName = "Tenant Management",
    allowedDependencies = {"shared"}
)
package com.estoquecentral.tenant;
