# Story 10.11: Backend - Validação de Permissões em Endpoints

**Epic**: 10 - Gestão de Colaboradores e Permissões RBAC
**Story ID**: 10.11
**Status**: pending
**Created**: 2025-12-22

---

## User Story

As a **System**,
I want **all endpoints to validate permissions**,
So that **unauthorized access is prevented at API level**.

---

## Acceptance Criteria

### AC1: Endpoint Security Annotations
**Given** endpoint security annotations
**When** endpoints are defined
**Then** public endpoints have no `@PreAuthorize`
**And** authenticated endpoints require any role
**And** admin endpoints require ADMIN role

### AC2: Missing Role Handling
**Given** request with missing role
**When** vendedor tries to access `/api/collaborators`
**Then** Spring Security denies with 403 Forbidden
**And** error response with clear message

### AC3: Tenant Validation
**Given** request with invalid tenant
**When** user tries to access resource from different tenant
**Then** `TenantFilter` validates tenant context
**And** request denied if tenant mismatch
**And** data leakage prevented (NFR9)

---

## Definition of Done
- [ ] Security annotations em todos endpoints
- [ ] Missing role handling
- [ ] Tenant validation
- [ ] Data leakage prevention

---

**Story criada por**: poly (PM Agent)
**Data**: 2025-12-22
