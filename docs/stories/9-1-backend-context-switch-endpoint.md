# Story 9.1: Backend - Endpoint para Trocar Contexto de Empresa

**Epic**: 9 - Seleção e Troca de Contexto Multi-Empresa
**Story ID**: 9.1
**Status**: pending
**Created**: 2025-12-22

---

## User Story

As a **User**,
I want **to switch between my companies via API**,
So that **I can work on different companies without logging out and back in**.

---

## Acceptance Criteria

### AC1: Endpoint Configuration
**Given** authenticated endpoint `/api/users/me/context`
**When** PUT request is sent with new tenant ID
**Then** endpoint requires JWT authentication (ARCH24)
**And** request payload: `{"tenantId": "uuid"}`

### AC2: Validation
**Given** valid context switch request
**When** user switches to a company they have access to
**Then** backend validates `tenantId` exists in `public.tenants`
**And** backend validates user has access (query `public.user_tenants`)
**And** backend validates `user_tenants.status = 'ativo'`

### AC3: JWT Generation
**Given** successful validation
**When** context switch is authorized
**Then** new JWT is generated with updated `tenantId` and `roles`
**And** endpoint returns 200 OK with new JWT and tenant info

### AC4: Authorization Failure
**Given** unauthorized context switch
**When** user tries to switch to a company they don't have access to
**Then** endpoint returns 403 Forbidden
**And** error message: "Você não tem acesso a esta empresa"

### AC5: Performance
**Given** performance requirement
**When** context switch is executed
**Then** operation completes in < 500ms (NFR3, FR10)

---

## Definition of Done
- [ ] Endpoint implementado
- [ ] Validação de acesso funcionando
- [ ] JWT generation com novo contexto
- [ ] Performance < 500ms validada
- [ ] Testes de integração passando

---

**Story criada por**: poly (PM Agent)
**Data**: 2025-12-22
**Baseado em**: Epic 9, PRD (FR9, FR10, NFR3)
