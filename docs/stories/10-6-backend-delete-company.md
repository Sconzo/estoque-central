# Story 10.6: Backend - Endpoint para Deletar Empresa

**Epic**: 10 - Gestão de Colaboradores e Permissões RBAC
**Story ID**: 10.6
**Status**: pending
**Created**: 2025-12-22

---

## User Story

As a **Admin**,
I want **to delete my company**,
So that **I can remove all data if I no longer need the system**.

---

## Acceptance Criteria

### AC1: Endpoint Security
**Given** authenticated endpoint `/api/companies/current` (DELETE)
**When** admin deletes company
**Then** endpoint requires JWT with `ADMIN` role
**And** confirmation token required

### AC2: Orphan Protection
**Given** tenant orphan protection
**When** deleting a company
**Then** backend checks if any users are ONLY linked to this tenant
**And** if orphan users exist, deletion is blocked (FR19)

### AC3: Soft Delete
**Given** safe deletion
**When** no orphan users exist
**Then** backend sets `public.tenants.ativo = false`
**And** sets all `user_tenants.status = 'inativo'`
**And** tenant schema is NOT dropped (retained for recovery)

---

## Definition of Done
- [ ] Endpoint implementado
- [ ] Orphan protection
- [ ] Soft delete
- [ ] Schema retention

---

**Story criada por**: poly (PM Agent)
**Data**: 2025-12-22
