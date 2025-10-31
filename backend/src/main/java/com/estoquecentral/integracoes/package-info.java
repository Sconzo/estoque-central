/**
 * Integracoes Module - External Integrations
 *
 * This is a Spring Modulith module responsible for:
 * - Mercado Livre API integration (OAuth2)
 * - Product sync to/from marketplaces
 * - Order import from marketplaces
 * - Stock synchronization
 * - Webhook handling
 *
 * <p>Module follows hexagonal architecture:
 * - domain/ contains entities, value objects, ports (interfaces)
 * - application/ contains use case implementations (services)
 * - adapter/ contains REST controllers (in) and persistence (out)
 */
@org.springframework.modulith.ApplicationModule
package com.estoquecentral.integracoes;
