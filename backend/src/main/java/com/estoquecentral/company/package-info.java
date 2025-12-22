/**
 * Company Module - Multi-tenant company management and registration.
 *
 * <p>This module handles company (tenant) creation, management, and self-service registration
 * using Spring Modulith to enforce bounded context boundaries. It enables users to create
 * and manage multiple companies within the multi-tenant system.</p>
 *
 * <p><strong>Responsibilities:</strong></p>
 * <ul>
 *   <li>Self-service company registration</li>
 *   <li>Company profile management (CRUD operations)</li>
 *   <li>User-company associations</li>
 *   <li>Company context switching</li>
 *   <li>Collaborator invitations and management</li>
 * </ul>
 *
 * <p><strong>Architecture:</strong> Hexagonal (Ports & Adapters)</p>
 * <ul>
 *   <li><strong>domain/</strong>: Core business entities (Company, CompanyUser, etc.)</li>
 *   <li><strong>application/</strong>: Use cases and services (CompanyService, CollaboratorService)</li>
 *   <li><strong>adapter/in/</strong>: Inbound adapters (REST controllers, DTOs)</li>
 *   <li><strong>adapter/out/</strong>: Outbound adapters (repositories)</li>
 * </ul>
 *
 * <p><strong>Dependencies:</strong></p>
 * <ul>
 *   <li>tenant - Tenant infrastructure and context</li>
 *   <li>auth - User authentication and authorization</li>
 *   <li>shared - Common utilities and tenant context</li>
 * </ul>
 *
 * <p><strong>Key Use Cases (from Epics 7-10):</strong></p>
 * <ul>
 *   <li><strong>Epic 8:</strong> Self-service company creation (public endpoint)</li>
 *   <li><strong>Epic 9:</strong> Company context selection and switching</li>
 *   <li><strong>Epic 10:</strong> Collaborator management with RBAC</li>
 * </ul>
 *
 * @since 1.0
 */
@org.springframework.modulith.ApplicationModule(
    displayName = "Company Management",
    allowedDependencies = {"tenant", "auth", "shared"}
)
package com.estoquecentral.company;
