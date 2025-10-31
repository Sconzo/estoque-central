/**
 * Estoque Module - Inventory Management
 *
 * This is a Spring Modulith module responsible for:
 * - Multi-warehouse stock control
 * - Stock movements and transfers
 * - Stock reservations
 * - Inventory counting
 * - Weighted average cost calculation
 *
 * <p>Module follows hexagonal architecture:
 * - domain/ contains entities, value objects, ports (interfaces)
 * - application/ contains use case implementations (services)
 * - adapter/ contains REST controllers (in) and persistence (out)
 */
@org.springframework.modulith.ApplicationModule
package com.estoquecentral.estoque;
