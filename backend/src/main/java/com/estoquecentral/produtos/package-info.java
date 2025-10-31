/**
 * Produtos Module - Product Catalog Management
 *
 * This is a Spring Modulith module responsible for:
 * - Product catalog (simple, variants, composites/kits)
 * - Product categories (hierarchical)
 * - Product attributes and specifications
 * - CSV product imports
 *
 * <p>Module follows hexagonal architecture:
 * - domain/ contains entities, value objects, ports (interfaces)
 * - application/ contains use case implementations (services)
 * - adapter/ contains REST controllers (in) and persistence (out)
 */
@org.springframework.modulith.ApplicationModule
package com.estoquecentral.produtos;
