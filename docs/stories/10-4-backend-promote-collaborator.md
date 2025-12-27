# Story 10.4: Backend - Endpoint para Promover Colaborador para Admin

**Epic**: 10 - Gestão de Colaboradores e Permissões RBAC
**Story ID**: 10.4
**Status**: completed
**Created**: 2025-12-22
**Completed**: 2025-12-27

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
- [x] Endpoint implementado
- [x] Role update funcionando (updateRole("ADMIN"))
- [x] Already admin check (AC2)
- [x] Multiple admins suportado (AC3)
- [x] Build compilando com sucesso

## Implementation Summary

### Arquivos Modificados
1. **CollaboratorService.java** - Atualizado método `promoteToAdmin()`:
   - AC2: Valida se usuário já é ADMIN antes de promover
   - Lança IllegalStateException se já for admin
   - AC1, AC3: Atualiza role para "ADMIN" via `updateRole()`
   - Suporta múltiplos admins (sem limite)

2. **CollaboratorController.java** - Atualizado endpoint PUT:
   - Trata IllegalStateException e converte para 400 Bad Request
   - Documentação completa dos ACs
   - Confirma suporte a múltiplos admins

### Validações Implementadas
- **AC2 - Already admin check**: Verifica se usuário já é admin
  - Retorna 400 Bad Request com mensagem "User is already an admin"
- **AC3 - Multiple admins support**: Sem limite de admins por company
  - Todos admins têm permissões iguais
  - Sistema suporta promoção de múltiplos usuários para ADMIN

---

**Story criada por**: poly (PM Agent)
**Data**: 2025-12-22
