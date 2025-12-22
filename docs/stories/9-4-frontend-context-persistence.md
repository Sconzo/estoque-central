# Story 9.4: Frontend - Persistência de Contexto em Sessão

**Epic**: 9 - Seleção e Troca de Contexto Multi-Empresa
**Story ID**: 9.4
**Status**: pending
**Created**: 2025-12-22

---

## User Story

As a **User**,
I want **my company context to persist across page refreshes**,
So that **I don't have to re-select my company every time**.

---

## Acceptance Criteria

### AC1: TenantService Storage
**Given** `TenantService` implementation
**When** user selects or switches company
**Then** `tenantId` is stored in browser local storage or session storage
**And** storage key: `currentTenantId`

### AC2: Page Refresh Handling
**Given** page refresh (F5)
**When** application reloads
**Then** `TenantService.ngOnInit()` or `APP_INITIALIZER` reads `currentTenantId` from storage
**And** if `tenantId` exists, it's set as current context
**And** if missing, user is redirected to `/select-company`

### AC3: HTTP Interceptor
**Given** HTTP interceptor
**When** API requests are made
**Then** `TenantInterceptor` adds header: `X-Tenant-ID: {tenantId}` (ARCH22)
**And** header is added automatically to all requests

### AC4: JWT Expiration
**Given** JWT expiration
**When** JWT expires
**Then** user is redirected to login
**And** after re-login, previous `tenantId` is reloaded (FR11)

### AC5: Context Consistency
**Given** context consistency
**When** user navigates between routes
**Then** tenant context remains consistent
**And** all API calls use the same `tenantId`

---

## Definition of Done
- [ ] TenantService com storage
- [ ] Page refresh handling
- [ ] HTTP interceptor configurado
- [ ] JWT expiration handling
- [ ] Context consistency validada

---

**Story criada por**: poly (PM Agent)
**Data**: 2025-12-22
**Baseado em**: Epic 9, PRD (FR11, ARCH22)
