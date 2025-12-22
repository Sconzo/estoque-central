# Story 10.4: Backend - Endpoint para Promover Colaborador para Admin

**Epic**: 10 - Gestão de Colaboradores e Permissões RBAC
**Story ID**: 10.4
**Status**: pending
**Created**: 2025-12-22

---

## User Story

As a **Admin**,
I want **to promote a collaborator to admin role**,
So that **I can share administrative responsibilities**.

---

## Acceptance Criteria

### AC1: Endpoint Configuration
**Given** authenticated endpoint `/api/collaborators/{userId}/promote` (PUT)
**When** admin promotes a user
**Then** endpoint requires JWT with `ADMIN` role
**And** updates `public.user_tenants.profile_id` to Admin profile UUID

### AC2: Already Admin Check
**Given** already admin
**When** user is already admin
**Then** endpoint returns 400 Bad Request

### AC3: Multiple Admins Support
**Given** multiple admins support
**When** new admin is created
**Then** tenant can have multiple admins (FR17)
**And** all admins have equal permissions

---

## Definition of Done
- [ ] Endpoint implementado
- [ ] Profile update funcionando
- [ ] Already admin check
- [ ] Multiple admins suportado

---

**Story criada por**: poly (PM Agent)
**Data**: 2025-12-22
