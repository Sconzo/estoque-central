# Story 8.5: Backend - Validação e Tratamento de Erros

**Epic**: 8 - Criação Self-Service de Empresa
**Story ID**: 8.5
**Status**: pending
**Created**: 2025-12-22

---

## User Story

As a **Backend Developer**,
I want **robust error handling for company creation**,
So that **failures are logged, users get clear feedback, and system remains stable**.

---

## Acceptance Criteria

### AC1: Input Validation

**Given** validation rules
**When** company creation request is received
**Then** nome is validated (not null, max 255 chars)
**And** email is validated (email format)
**And** CNPJ is validated (14 digits if provided, optional)
**And** validation errors return 400 Bad Request with field-specific messages

### AC2: Duplicate Name Handling

**Given** duplicate company name check
**When** company name already exists in `public.tenants`
**Then** request is accepted (names can be duplicated across tenants)
**And** only `schema_name` must be unique (enforced by DB constraint)

### AC3: Database Connection Failure

**Given** database connection failure
**When** PostgreSQL is unavailable
**Then** error is logged with stack trace
**And** endpoint returns 503 Service Unavailable
**And** error message: "Sistema temporariamente indisponível. Tente novamente em instantes."

### AC4: Schema Creation Failure

**Given** schema creation failure
**When** `CREATE SCHEMA` SQL command fails
**Then** transaction is rolled back
**And** `public.tenants` record is deleted or marked with error status (FR5)
**And** error is logged in application logs and Application Insights
**And** endpoint returns 500 Internal Server Error
**And** error message: "Falha ao criar empresa. Nossa equipe foi notificada."

### AC5: Critical Error Logging

**Given** critical error logging
**When** tenant provisioning fails
**Then** error details are stored in `public.tenants` table (FR5)
**And** error includes: `tenant_id`, `error_message`, `stack_trace`, `timestamp`
**And** error is sent to Application Insights for alerting
**And** on-call team is notified if error rate exceeds threshold

---

## Definition of Done

- [ ] Input validation implementada
- [ ] Duplicate handling configurado
- [ ] Database failure handling
- [ ] Schema creation rollback
- [ ] Error logging completo
- [ ] Application Insights integrado
- [ ] Testes de erro validados

---

**Story criada por**: poly (PM Agent)
**Data**: 2025-12-22
**Baseado em**: Epic 8, PRD (FR5, NFR13)
