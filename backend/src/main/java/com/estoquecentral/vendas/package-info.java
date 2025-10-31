/**
 * Vendas Module - Sales Management
 *
 * This is a Spring Modulith module responsible for:
 * - PDV (Point of Sale) operations
 * - B2B sales orders
 * - B2C online sales
 * - Pricing rules and discounts
 * - Sales order fulfillment
 *
 * <p>Module follows hexagonal architecture:
 * - domain/ contains entities, value objects, ports (interfaces)
 * - application/ contains use case implementations (services)
 * - adapter/ contains REST controllers (in) and persistence (out)
 */
@org.springframework.modulith.ApplicationModule
package com.estoquecentral.vendas;
