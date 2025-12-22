# Story 10.5: Backend - Endpoint para Editar Dados da Empresa

**Epic**: 10 - Gestão de Colaboradores e Permissões RBAC
**Story ID**: 10.5
**Status**: pending
**Created**: 2025-12-22

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
- [ ] Endpoint implementado
- [ ] Validation rules
- [ ] Timestamp update
- [ ] Testes validados

---

**Story criada por**: poly (PM Agent)
**Data**: 2025-12-22
