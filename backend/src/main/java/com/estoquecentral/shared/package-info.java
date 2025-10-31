/**
 * Shared Module - Common Domain Elements
 *
 * This module contains shared domain elements used across bounded contexts:
 * - Value Objects (Money, TenantId, ProdutoId, etc.)
 * - Common infrastructure components
 * - Shared utilities
 *
 * <p>This is NOT a Spring Modulith module - it's a shared kernel.
 * All other modules can depend on shared, but shared should not depend on any other module.
 */
package com.estoquecentral.shared;
