# Story 10.3: Backend - Endpoint para Remover Colaborador

**Epic**: 10 - Gestão de Colaboradores e Permissões RBAC
**Story ID**: 10.3
**Status**: pending
**Created**: 2025-12-22

---

## User Story

As a **Admin**,
I want **to remove a collaborator from my company**,
So that **I can revoke access when someone leaves the team**.

---

## Acceptance Criteria

### AC1: Endpoint Security
**Given** authenticated endpoint `/api/collaborators/{userId}` (DELETE)
**When** admin sends delete request
**Then** endpoint requires JWT with `ADMIN` role

### AC2: Soft Delete
**Given** valid removal request
**When** admin removes a collaborator
**Then** backend updates `public.user_tenants`: Set `status = 'inativo'`

### AC3: Self-Removal Protection
**Given** self-removal protection
**When** admin tries to remove themselves
**Then** endpoint returns 400 Bad Request

### AC4: Last Admin Protection
**Given** last admin protection
**When** removing user would leave tenant with zero admins
**Then** endpoint returns 400 Bad Request

---

## Definition of Done
- [ ] Endpoint implementado
- [ ] Soft delete funcionando
- [ ] Self-removal protection
- [ ] Last admin protection

---

**Story criada por**: poly (PM Agent)
**Data**: 2025-12-22
