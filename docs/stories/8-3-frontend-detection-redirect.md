# Story 8.3: Frontend - Detecção e Redirecionamento para Cadastro

**Epic**: 8 - Criação Self-Service de Empresa
**Story ID**: 8.3
**Status**: pending
**Created**: 2025-12-22

---

## User Story

As a **User**,
I want **to be automatically directed to company registration**,
So that **I don't get stuck after login without knowing what to do next**.

---

## Acceptance Criteria

### AC1: AuthService JWT Decoding

**Given** `AuthService` after OAuth login
**When** JWT is received from backend
**Then** service decodes JWT to extract `tenantId` and `roles`
**And** service calls `/api/users/me/companies` to get list of linked companies

### AC2: Zero Companies Redirect

**Given** user with zero companies
**When** companies list is empty
**Then** `AuthGuard` redirects to `/create-company` route (FR6)
**And** redirect happens automatically after login
**And** user sees company registration form

### AC3: One Company Auto-Select

**Given** user with 1 company
**When** companies list has exactly 1 item
**Then** `AuthGuard` auto-selects that company (FR8)
**And** `TenantService.setCurrentTenant(tenantId)` is called
**And** user is redirected to `/dashboard`
**And** no manual selection is required

### AC4: Multiple Companies Redirect

**Given** user with 2+ companies
**When** companies list has multiple items
**Then** user is redirected to `/select-company` route (FR7)
**And** selection screen is displayed (handled in Epic 9)

### AC5: Navigation Persistence

**Given** navigation persistence
**When** user refreshes page (F5)
**Then** tenant context is reloaded from local storage or session (FR11)
**And** user remains in current company context
**And** no re-authentication is required

---

## Definition of Done

- [ ] AuthService decode JWT implementado
- [ ] AuthGuard com lógica de redirecionamento
- [ ] Zero companies → create-company
- [ ] One company → auto-select + dashboard
- [ ] Multiple companies → select-company
- [ ] Persistence funcionando

---

**Story criada por**: poly (PM Agent)
**Data**: 2025-12-22
**Baseado em**: Epic 8, PRD (FR6, FR7, FR8, FR11)
