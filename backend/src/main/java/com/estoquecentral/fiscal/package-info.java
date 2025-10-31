/**
 * Fiscal Module - Tax and Fiscal Document Management
 *
 * This is a Spring Modulith module responsible for:
 * - NFCe emission via Focus NFe API
 * - NFCe retry queue with exponential backoff
 * - Tax calculations
 * - Fiscal compliance
 * - Document status tracking
 *
 * <p>Module follows hexagonal architecture:
 * - domain/ contains entities, value objects, ports (interfaces)
 * - application/ contains use case implementations (services)
 * - adapter/ contains REST controllers (in) and persistence (out)
 */
@org.springframework.modulith.ApplicationModule
package com.estoquecentral.fiscal;
