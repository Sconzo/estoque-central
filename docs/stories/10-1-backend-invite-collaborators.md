# Story 10.1: Backend - Endpoint para Convidar Colaboradores

**Epic**: 10 - Gestão de Colaboradores e Permissões RBAC
**Story ID**: 10.1
**Status**: pending
**Created**: 2025-12-22

---

## User Story

As a **Admin**,
I want **to invite collaborators to my company via API**,
So that **I can build my team and delegate work**.

---

## Acceptance Criteria

### AC1: Endpoint Configuration
**Given** authenticated endpoint `/api/collaborators` (POST)
**When** admin sends invitation request
**Then** endpoint requires JWT with `ADMIN` role (ARCH21)
**And** request payload: `{"email": "string", "profileId": "uuid"}`

### AC2: Existing User Invitation
**Given** existing user invitation
**When** user already has Google account in system
**Then** backend creates record in `public.user_tenants`
**And** no email is sent (MVP simplification) (FR13)

### AC3: New User Invitation
**Given** new user invitation
**When** user email is not in `public.users`
**Then** backend creates placeholder user record
**And** `google_id`: NULL (filled on first login)

### AC4: Success Response
**Given** successful invitation
**When** user-tenant relationship is created
**Then** endpoint returns 201 Created with user details

### AC5: Duplicate Prevention
**Given** duplicate invitation
**When** user is already linked to this tenant
**Then** endpoint returns 409 Conflict

---

## Definition of Done
- [ ] Endpoint implementado com ADMIN role
- [ ] Existing user handling
- [ ] New user placeholder creation
- [ ] Duplicate check
- [ ] Testes de integração

---

**Story criada por**: poly (PM Agent)
**Data**: 2025-12-22
**Baseado em**: Epic 10, PRD (FR12, FR13, ARCH21)
