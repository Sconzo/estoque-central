# Story 10.5: Backend - Endpoint para Editar Dados da Empresa

**Epic**: 10 - Gestão de Colaboradores e Permissões RBAC
**Story ID**: 10.5
**Status**: completed
**Created**: 2025-12-22
**Completed**: 2025-12-27

---

## User Story

As a **Admin**,
I want **to update my company's information**,
So that **I can keep company data accurate**.

---

## Acceptance Criteria

### AC1: Endpoint Configuration
**Given** authenticated endpoint `/api/companies/current` (PUT)
**When** admin updates company data
**Then** endpoint requires JWT with `ADMIN` role
**And** accepts: nome, cnpj, email, telefone, endereco

### AC2: Data Update
**Given** valid update
**When** company data is updated
**Then** backend updates record in `public.tenants`
**And** `data_atualizacao` timestamp is updated
**And** validation rules applied

### AC3: Success Response
**Given** successful update
**When** update completes
**Then** endpoint returns 200 OK with updated company data

---

## Definition of Done
- [x] Endpoint implementado
- [x] Validation rules (via Jakarta Validation)
- [x] Timestamp update (Company.update() atualiza updated_at)
- [x] Build compilando com sucesso

## Implementation Summary

### Arquivos Criados
1. **UpdateCompanyRequest.java** - DTO para request de atualização de company
2. **CompanyManagementController.java** - Controller para endpoints autenticados de gestão de company

### Arquivos Modificados
1. **CompanyService.java** - Adicionado método `getCompanyByTenantId()`:
   - Busca company por tenantId (extraído do JWT)
   - Utiliza método existente `updateCompany()` que atualiza timestamp

### Endpoints Implementados
- `PUT /api/companies/current` - Atualiza company atual do usuário (requer ADMIN)
  - Extrai tenantId do JWT
  - Atualiza nome, email, telefone
  - Retorna company atualizada

---

**Story criada por**: poly (PM Agent)
**Data**: 2025-12-22
