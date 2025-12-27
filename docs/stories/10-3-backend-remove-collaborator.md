# Story 10.3: Backend - Endpoint para Remover Colaborador

**Epic**: 10 - Gestão de Colaboradores e Permissões RBAC
**Story ID**: 10.3
**Status**: completed
**Created**: 2025-12-22
**Completed**: 2025-12-27

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
- [x] Endpoint implementado
- [x] Soft delete funcionando (deactivate)
- [x] Self-removal protection (AC3)
- [x] Last admin protection (AC4)
- [x] Build compilando com sucesso

## Implementation Summary

### Arquivos Modificados
1. **CollaboratorService.java** - Atualizado método `removeCollaborator()`:
   - Adicionado parâmetro `currentUserId` para validação de auto-remoção
   - AC3: Valida se userId == currentUserId e lança exceção
   - AC4: Conta admins ativos e previne remoção do último admin
   - Adicionado método privado `countActiveAdmins()` para contagem
   - AC2: Soft delete via `deactivate()` (já existente)

2. **CollaboratorController.java** - Atualizado endpoint DELETE:
   - Passa currentUserId extraído do JWT para o service
   - Trata IllegalStateException e converte para 400 Bad Request
   - Documentação completa dos ACs

### Validações Implementadas
- **AC3 - Self-removal protection**: Usuário não pode se remover
  - Retorna 400 Bad Request com mensagem "You cannot remove yourself from the company"
- **AC4 - Last admin protection**: Company deve ter pelo menos 1 admin
  - Conta admins ativos antes de remover
  - Retorna 400 Bad Request com mensagem "Cannot remove the last admin from the company"

---

**Story criada por**: poly (PM Agent)
**Data**: 2025-12-22
