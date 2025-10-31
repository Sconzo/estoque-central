/**
 * Compras Module - Purchasing Management
 *
 * This is a Spring Modulith module responsible for:
 * - Supplier management
 * - Purchase orders
 * - Goods receiving (with mobile scanner support)
 * - Cost updates (weighted average)
 * - Purchase analytics
 *
 * <p>Module follows hexagonal architecture:
 * - domain/ contains entities, value objects, ports (interfaces)
 * - application/ contains use case implementations (services)
 * - adapter/ contains REST controllers (in) and persistence (out)
 */
@org.springframework.modulith.ApplicationModule
package com.estoquecentral.compras;
