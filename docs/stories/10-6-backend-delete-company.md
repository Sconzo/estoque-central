# Story 10.6: Backend - Endpoint para Deletar Empresa

**Epic**: 10 - Gestão de Colaboradores e Permissões RBAC
**Story ID**: 10.6
**Status**: completed
**Created**: 2025-12-22
**Completed**: 2025-12-27

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
- [x] Endpoint implementado
- [x] Orphan protection (AC2)
- [x] Soft delete (AC3)
- [x] Schema retention (AC3)
- [x] Build compilando com sucesso

## Implementation Summary

### Arquivos Modificados
1. **CompanyManagementController.java** - Adicionado endpoint DELETE
2. **CompanyService.java** - Adicionado método `deleteCompanyWithValidation()`:
   - **AC2 - Orphan Protection**: Verifica se algum usuário está APENAS vinculado a esta company
     - Itera por todos os collaborators
     - Para cada um, verifica quantas companies ativas possui
     - Bloqueia deleção se encontrar orphan user
     - Retorna 400 Bad Request com mensagem explicativa
   - **AC3 - Soft Delete**:
     - Define company.active = false
     - Define company_users.active = false para todos os associados
     - Retém tenant schema (não executa DROP SCHEMA)
3. **CompanyUserRepository.java** - Adicionado método `findAllActiveByUserId()`

### Endpoints Implementados
- `DELETE /api/companies/current` - Deleta company atual do usuário (requer ADMIN)
  - Extrai tenantId do JWT
  - Valida orphan users (AC2)
  - Executa soft delete (AC3)
  - Schema é retido para possível recuperação

---

**Story criada por**: poly (PM Agent)
**Data**: 2025-12-22
