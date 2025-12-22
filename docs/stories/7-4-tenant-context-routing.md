# Story 7.4: Implementação TenantContext + TenantInterceptor + Routing

**Epic**: 7 - Infraestrutura Multi-Tenant e Deploy
**Story ID**: 7.4
**Status**: pending
**Created**: 2025-12-22

---

## User Story

As a **System**,
I want **automatic tenant routing based on JWT context**,
So that **queries execute in the correct tenant schema without manual intervention**.

---

## Acceptance Criteria

### AC1: TenantContext ThreadLocal Implementation

**Given** `TenantContext` ThreadLocal implementation
**When** a request arrives with JWT
**Then** `TenantFilter` extracts `tenantId` from JWT payload
**And** `TenantContext.setCurrentTenant(tenantId)` is called
**And** context is cleared in `finally` block after request completes

### AC2: AbstractRoutingDataSource Configuration

**Given** `AbstractRoutingDataSource` configuration
**When** `determineCurrentLookupKey()` is called
**Then** it retrieves `tenantId` from `TenantContext`
**And** returns data source key: `tenant_{tenantId}`
**And** Hibernate/JDBC queries execute in the correct schema

### AC3: Automatic Schema Routing

**Given** database query execution
**When** repository methods are called
**Then** queries automatically execute in the tenant's schema
**And** `SET search_path TO tenant_{uuid}` is applied
**And** no manual schema switching is required in code

### AC4: Performance e Isolamento

**Given** tenant context switch performance
**When** user switches between companies
**Then** context switch completes in < 500ms (NFR3, FR10)
**And** no data leakage occurs between tenants (NFR9)

---

## Definition of Done

- [ ] TenantContext implementado
- [ ] TenantFilter criado e configurado
- [ ] AbstractRoutingDataSource funcionando
- [ ] Testes de isolamento validados
- [ ] Performance < 500ms validada

---

**Story criada por**: poly (PM Agent)
**Data**: 2025-12-22
**Baseado em**: Epic 7, PRD (FR26, NFR3)
