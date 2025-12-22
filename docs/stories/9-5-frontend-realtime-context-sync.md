# Story 9.5: Frontend - Sincronização de Contexto em Tempo Real

**Epic**: 9 - Seleção e Troca de Contexto Multi-Empresa
**Story ID**: 9.5
**Status**: pending
**Created**: 2025-12-22

---

## User Story

As a **User**,
I want **the UI to update immediately after switching companies**,
So that **I see the correct data for the selected company**.

---

## Acceptance Criteria

### AC1: Angular Signals State Management
**Given** Angular Signals for state management
**When** `TenantService.setCurrentTenant(tenantId)` is called
**Then** a Signal `currentTenant$` is updated
**And** components subscribed to the signal reactively update

### AC2: Data Refresh on Context Switch
**Given** dashboard or data-heavy screens
**When** tenant context switches
**Then** all API calls are re-executed with new `tenantId`
**And** data refreshes automatically to show new company's data
**And** previous company's data is cleared from cache

### AC3: Navigation Preservation
**Given** navigation after context switch
**When** user is on `/products` and switches company
**Then** products list refreshes to show new company's products
**And** URL stays on `/products` (no redirect to dashboard)

### AC4: Redis Cache Isolation
**Given** Redis cache invalidation
**When** context switch occurs
**Then** previous tenant's cached data is not shown
**And** new tenant's data is fetched fresh or from their cache

### AC5: Performance Optimization
**Given** performance optimization
**When** switching between frequently accessed companies
**Then** data can be pre-fetched or cached per tenant
**And** switch feels instant (< 500ms total) (FR10, NFR3)

---

## Definition of Done
- [ ] Angular Signals implementado
- [ ] Data refresh automático
- [ ] Navigation preservation
- [ ] Cache isolation validado
- [ ] Performance < 500ms

---

**Story criada por**: poly (PM Agent)
**Data**: 2025-12-22
**Baseado em**: Epic 9, PRD (FR10, NFR3)
