# Story 10.2: Backend - Endpoint para Listar Colaboradores

**Epic**: 10 - Gestão de Colaboradores e Permissões RBAC
**Story ID**: 10.2
**Status**: pending
**Created**: 2025-12-22

---

## User Story

As a **Admin**,
I want **to see all collaborators in my company**,
So that **I can manage the team and their permissions**.

---

## Acceptance Criteria

### AC1: Endpoint Configuration
**Given** authenticated endpoint `/api/collaborators` (GET)
**When** request is sent with valid JWT
**Then** endpoint requires authentication (any role)
**And** endpoint extracts `tenantId` from JWT

### AC2: Query Execution
**Given** query execution
**When** endpoint fetches collaborators
**Then** query joins `public.user_tenants` + `public.users` + tenant schema `profiles`
**And** query filters by `tenant_id` AND `status = 'ativo'`

### AC3: Performance
**Given** performance requirement
**When** endpoint is called
**Then** response time < 200ms (NFR5)
**And** query uses indexes on `user_tenants.tenant_id`

---

## Definition of Done
- [ ] Endpoint implementado
- [ ] Query otimizada
- [ ] Performance < 200ms
- [ ] Testes validados

---

**Story criada por**: poly (PM Agent)
**Data**: 2025-12-22
