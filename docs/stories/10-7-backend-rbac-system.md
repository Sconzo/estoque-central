# Story 10.7: Backend - Sistema RBAC com Profiles e Roles

**Epic**: 10 - Gestão de Colaboradores e Permissões RBAC
**Story ID**: 10.7
**Status**: pending
**Created**: 2025-12-22

---

## User Story

As a **System**,
I want **role-based access control implemented**,
So that **permissions are enforced across all endpoints**.

---

## Acceptance Criteria

### AC1: Default Profiles Seeding
**Given** tenant schema `profiles` table
**When** tenant is created
**Then** 3 default profiles are seeded (FR23):
- Admin (full access)
- Gerente (read all + write products/stock)
- Vendedor (read products/stock + write sales)

### AC2: Profile Roles Assignment
**Given** tenant schema `profile_roles` table
**When** profiles are seeded
**Then** each profile has associated roles

### AC3: Spring Security Integration
**Given** Spring Security configuration
**When** endpoint is annotated with `@PreAuthorize("hasRole('ADMIN')")`
**Then** Spring Security validates JWT roles (ARCH21)
**And** request denied with 403 if role missing

### AC4: Tenant Context Validation
**Given** tenant context validation
**When** user accesses protected resource
**Then** system validates user belongs to active tenant (FR22)

---

## Definition of Done
- [ ] Profiles seeding implementado
- [ ] Roles assignment
- [ ] Spring Security configurado
- [ ] Tenant context validation

---

**Story criada por**: poly (PM Agent)
**Data**: 2025-12-22
