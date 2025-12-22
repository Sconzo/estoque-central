# Story 8.1: Backend - Endpoint Público para Criação de Empresa

**Epic**: 8 - Criação Self-Service de Empresa
**Story ID**: 8.1
**Status**: pending
**Created**: 2025-12-22

---

## User Story

As a **Backend Developer**,
I want **a public API endpoint to create companies without authentication**,
So that **new users can register their first company immediately after OAuth login**.

---

## Acceptance Criteria

### AC1: Public Endpoint Configuration

**Given** a public endpoint `/api/public/companies`
**When** POST request is sent with company data
**Then** endpoint accepts JSON payload:
```json
{
  "nome": "string",
  "cnpj": "string (optional)",
  "email": "string",
  "telefone": "string (optional)",
  "userId": "uuid"
}
```
**And** endpoint does NOT require JWT authentication (ARCH23)
**And** CORS is configured to allow frontend origin

### AC2: Company Creation Logic

**Given** valid company creation request
**When** `CreateCompanyUseCase` is executed
**Then** a new record is created in `public.tenants` table
**And** `schema_name` is set to `tenant_{uuid}`
**And** `ativo` status is set to `true`
**And** `created_by` references the user ID

### AC3: Tenant Provisioning

**Given** `TenantProvisioner` service
**When** tenant record is created
**Then** new PostgreSQL schema `tenant_{uuid}` is created
**And** schema contains tables: `profiles`, `profile_roles`
**And** default profiles are seeded: Admin, Gerente, Vendedor
**And** operation completes in < 30 seconds (NFR2, FR4)
**And** success rate > 99% (NFR1)

### AC4: User-Tenant Relationship

**Given** user-tenant relationship
**When** company is created
**Then** a record is created in `public.user_tenants` table
**And** `user_id` references the creator
**And** `tenant_id` references the new company
**And** `profile_id` is set to Admin profile UUID
**And** `status` is set to 'ativo'

### AC5: Success Response

**Given** successful company creation
**When** operation completes
**Then** endpoint returns 201 Created with:
```json
{
  "tenantId": "uuid",
  "nome": "string",
  "schemaName": "string"
}
```
**And** JWT is generated with `tenantId` and `roles: [ADMIN]`

### AC6: Error Handling

**Given** company creation failure
**When** schema creation fails
**Then** error is logged in `public.tenants` with status or error column (FR5)
**And** transaction is rolled back
**And** endpoint returns 500 Internal Server Error with error message
**And** error rate < 1% (NFR13)

---

## Definition of Done

- [ ] Endpoint público criado
- [ ] CreateCompanyUseCase implementado
- [ ] TenantProvisioner funcionando
- [ ] User-tenant relationship criada
- [ ] Testes de integração validados
- [ ] Error handling completo

---

**Story criada por**: poly (PM Agent)
**Data**: 2025-12-22
**Baseado em**: Epic 8, PRD (FR1, FR2, FR3, FR4, FR5)
