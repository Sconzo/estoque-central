# Multi-Tenant Modules Architecture

**Status**: Implemented (Story 7.1)
**Version**: 1.0
**Last Updated**: 2025-12-22

## Overview

This document describes the architecture of the multi-tenant modules added to support self-service company creation and management (Epics 7-10).

## Backend Modules

### Tenant Module

**Package**: `com.estoquecentral.tenant`
**Purpose**: Tenant-specific settings and configuration management.

**Structure (Hexagonal Architecture)**:
```
com.estoquecentral.tenant/
├── domain/
│   └── TenantSetting.java         # Tenant configuration entity
├── application/
│   └── TenantSettingsService.java # Tenant settings use cases
└── adapter/
    ├── in/
    │   └── web/
    │       └── TenantSettingsController.java # REST endpoints
    └── out/
        └── TenantSettingRepository.java # Database access
```

**Key Responsibilities**:
- Manage tenant-specific settings (e.g., auto-release days)
- Provide tenant configuration to other modules
- Support tenant isolation at the configuration level

### Company Module

**Package**: `com.estoquecentral.company`
**Purpose**: Self-service company creation and multi-company management.

**Structure (Hexagonal Architecture)**:
```
com.estoquecentral.company/
├── domain/
│   ├── Company.java                # Company aggregate root
│   └── CompanyUser.java            # User-company association
├── application/
│   ├── CompanyService.java         # Company CRUD use cases
│   └── CollaboratorService.java    # Collaborator management
└── adapter/
    ├── in/
    │   ├── dto/
    │   │   ├── CompanyDTO.java
    │   │   ├── CreateCompanyRequest.java
    │   │   └── CollaboratorDTO.java
    │   └── web/
    │       ├── CompanyController.java       # Company REST endpoints
    │       └── CollaboratorController.java  # Collaborator REST endpoints
    └── out/
        ├── CompanyRepository.java      # Company persistence
        └── CompanyUserRepository.java  # Association persistence
```

**Key Responsibilities**:
- Self-service company registration (Epic 8)
- Company CRUD operations (Epic 10)
- User-company associations (multi-company support)
- Collaborator invitation and management (Epic 10)
- Role-based access control (RBAC) per company

**Key Entities**:

#### Company
Represents a tenant/company in the system.

**Fields**:
- `id`: Primary key
- `name`: Company name
- `cnpj`: Brazilian tax ID (unique)
- `email`: Company email
- `phone`: Company phone
- `ownerUserId`: User who created/owns the company
- `createdAt`, `updatedAt`: Timestamps
- `active`: Soft delete flag

**Domain Methods**:
- `create()`: Factory method for new company
- `update()`: Updates company information
- `deactivate()`: Soft delete

#### CompanyUser
Represents the many-to-many relationship between users and companies with roles.

**Fields**:
- `id`: Primary key
- `companyId`: Foreign key to company
- `userId`: Foreign key to user
- `role`: User role in this company (ADMIN, USER, etc.)
- `invitedAt`: Invitation timestamp
- `acceptedAt`: When user accepted invitation
- `active`: Active association flag

**Domain Methods**:
- `invite()`: Creates a pending invitation
- `accept()`: Activates the association
- `updateRole()`: Changes user role
- `deactivate()`: Removes user from company

## Frontend Modules

### Core Services

#### TenantService

**Location**: `src/app/core/services/tenant.service.ts`
**Purpose**: Manage current company context and switching.

**Key Features**:
- Signal-based reactive state (`currentCompanyId`)
- LocalStorage persistence of current company
- Company switching logic
- Auto-detection for single-company users

**API Methods**:
- `switchCompany(companyId)`: Changes current context
- `getCurrentCompanyId()`: Gets current company
- `getUserCompanies()`: Fetches all user companies
- `autoDetectCompany()`: Auto-selects if user has only one company
- `clearContext()`: Clears context on logout

#### CompanyService

**Location**: `src/app/core/services/company.service.ts`
**Purpose**: Company CRUD operations.

**API Methods**:
- `createCompany(request)`: Self-service registration (Epic 8)
- `getMyCompanies()`: Lists user's companies (Epic 9)
- `updateCompany(id, request)`: Updates company info (Epic 10)
- `deleteCompany(id)`: Deactivates company (Epic 10)
- `checkCnpjExists(cnpj)`: Validates CNPJ uniqueness

#### CollaboratorService

**Location**: `src/app/core/services/collaborator.service.ts`
**Purpose**: Collaborator management operations.

**API Methods**:
- `inviteCollaborator(companyId, request)`: Invites user (Epic 10)
- `listCollaborators(companyId)`: Lists collaborators (Epic 10)
- `removeCollaborator(companyId, userId)`: Removes user (Epic 10)
- `promoteToAdmin(companyId, userId)`: Promotes to ADMIN (Epic 10)
- `updateRole(companyId, userId, role)`: Updates role (Epic 10)

### Feature Components

#### Tenant Feature

**Location**: `src/app/features/tenant/`
**Purpose**: Tenant context management UI (Epic 9).

**Components** (to be implemented in future stories):
- Company selector dropdown
- Company context switcher
- Tenant settings UI

#### Company Feature

**Location**: `src/app/features/company/`
**Purpose**: Company registration and management UI (Epics 8, 10).

**Components** (to be implemented in future stories):
- Company registration form
- Company list view
- Company edit form
- Company deletion confirmation

#### Collaborators Feature

**Location**: `src/app/features/collaborators/`
**Purpose**: Collaborator management UI (Epic 10).

**Components** (to be implemented in future stories):
- Collaborator invitation form
- Collaborator list with roles
- Role management UI
- Collaborator removal confirmation

## Spring Modulith Integration

Both `tenant` and `company` modules are registered as Spring Modulith application modules via `package-info.java`:

### Tenant Module
```java
@org.springframework.modulith.ApplicationModule(
    displayName = "Tenant Management",
    allowedDependencies = {"shared"}
)
package com.estoquecentral.tenant;
```

### Company Module
```java
@org.springframework.modulith.ApplicationModule(
    displayName = "Company Management",
    allowedDependencies = {"tenant", "auth", "shared"}
)
package com.estoquecentral.company;
```

**Benefits**:
- Enforced module boundaries at compile time
- Clear dependency graph
- Event-driven communication between modules
- Improved testability and maintainability

## Database Schema

### Tables

#### `companies`
- Stores company/tenant data
- Unique index on `cnpj`
- Owner relationship via `owner_user_id` FK

#### `company_users`
- Many-to-many join table with role
- Composite unique index on `(company_id, user_id)`
- Soft delete via `active` flag

### Flyway Migrations

Migrations for these tables will be created in subsequent stories (Epics 8-10).

## Integration Points

### With Auth Module

The `company` module depends on the `auth` module for:
- User authentication context
- User ID references in `CompanyUser`
- OAuth user information for company owner

### With Shared Module

Both modules depend on `shared` for:
- Tenant context and routing (`TenantContext`, `TenantRoutingDataSource`)
- Common value objects and utilities

### Multi-Tenant Isolation

The system uses **schema-per-tenant** isolation:
1. Each company gets its own PostgreSQL schema
2. `TenantContext` maintains current company/schema
3. `TenantRoutingDataSource` routes queries to correct schema
4. Flyway applies migrations per tenant schema

## Future Enhancements

As detailed in Epics 8-10, future stories will implement:

1. **Epic 8**: Self-service company creation endpoints and UI
2. **Epic 9**: Company context selection and persistence
3. **Epic 10**: Full collaborator management with RBAC validation

## References

- [PRD: Self-Service Multi-Tenant](../prd/prd-self-service-multi-tenant.md)
- [Epic 7: Multi-Tenant Infrastructure](../epics/epic-07-multi-tenant-infrastructure.md)
- [Epic 8: Self-Service Company Creation](../epics/epic-08-self-service-company-creation.md)
- [Epic 9: Multi-Company Context](../epics/epic-09-multi-company-context.md)
- [Epic 10: Collaborators & RBAC](../epics/epic-10-collaborators-rbac.md)
- [Story 7.1: Monorepo Project Structure](../stories/7-1-monorepo-project-structure.md)
