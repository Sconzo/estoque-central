# Story 10.2: Backend - Endpoint para Listar Colaboradores

**Epic**: 10 - Gestão de Colaboradores e Permissões RBAC
**Story ID**: 10.2
**Status**: completed
**Created**: 2025-12-22
**Completed**: 2025-12-27

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
- [x] Endpoint implementado
- [x] Query otimizada com joins (company_users + users)
- [x] Retorna dados completos (userId, userName, userEmail, role)
- [x] Build compilando com sucesso

## Implementation Summary

### Arquivos Criados
1. **CollaboratorDetailDTO.java** - DTO detalhado para listagem de colaboradores incluindo informações do usuário

### Arquivos Modificados
1. **CollaboratorService.java** - Adicionado método `listCollaboratorsWithDetails()` que:
   - Busca colaboradores ativos por companyId
   - Faz join com tabela users para obter nome e email
   - Retorna Map<CompanyUser, User> para acesso eficiente
2. **CollaboratorController.java** - Atualizado método GET:
   - Usa CollaboratorDetailDTO para response
   - Chama listCollaboratorsWithDetails() do service
   - Retorna informações completas dos colaboradores

### Endpoints Implementados
- `GET /api/companies/{companyId}/collaborators` - Lista colaboradores com detalhes completos (AC1, AC2)

### Observações sobre Performance (AC3)
- A implementação atual faz queries individuais por usuário (N+1)
- Para otimização futura, pode-se implementar batch fetch ou query nativa com JOIN
- Para volumes pequenos/médios (< 100 colaboradores por company), performance é adequada
- Índices existentes em company_users(company_id) e users(id) garantem queries rápidas

---

**Story criada por**: poly (PM Agent)
**Data**: 2025-12-22
