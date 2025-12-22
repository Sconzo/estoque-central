# Story 8.4: Backend - Endpoint para Listar Empresas do Usuário

**Epic**: 8 - Criação Self-Service de Empresa
**Story ID**: 8.4
**Status**: pending
**Created**: 2025-12-22

---

## User Story

As a **User**,
I want **the system to retrieve my linked companies**,
So that **I can see which companies I have access to**.

---

## Acceptance Criteria

### AC1: Authenticated Endpoint

**Given** authenticated endpoint `/api/users/me/companies`
**When** GET request is sent with valid JWT
**Then** endpoint requires authentication (Bearer token)
**And** endpoint extracts `userId` from JWT payload

### AC2: Query User Companies

**Given** user with linked companies
**When** endpoint is called
**Then** query joins `public.users` + `public.user_tenants` + `public.tenants`
**And** query filters by `user_id = {userId}` AND `user_tenants.status = 'ativo'`
**And** query returns companies in JSON array:
```json
[
  {
    "tenantId": "uuid",
    "nome": "string",
    "cnpj": "string",
    "profileId": "uuid",
    "profileName": "Admin"
  }
]
```

### AC3: Empty Companies List

**Given** user with no companies
**When** endpoint is called
**Then** empty array is returned: `[]`
**And** HTTP status is 200 OK (not 404)

### AC4: Performance Requirements

**Given** performance requirements
**When** endpoint is called
**Then** response time < 200ms (NFR5)
**And** query uses indexes on `user_tenants.user_id`

---

## Definition of Done

- [ ] Endpoint criado e autenticado
- [ ] Query otimizada com joins
- [ ] Empty list handling
- [ ] Performance < 200ms validada
- [ ] Indexes configurados
- [ ] Testes de integração passando

---

**Story criada por**: poly (PM Agent)
**Data**: 2025-12-22
**Baseado em**: Epic 8, PRD (NFR5)
