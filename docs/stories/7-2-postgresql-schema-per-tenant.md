# Story 7.2: Setup PostgreSQL Multi-Tenant com Schema-per-Tenant

**Epic**: 7 - Infraestrutura Multi-Tenant e Deploy
**Story ID**: 7.2
**Status**: pending
**Created**: 2025-12-22

---

## User Story

As a **System Administrator**,
I want **PostgreSQL database configured with schema-per-tenant isolation**,
So that **each company's data is completely isolated in dedicated schemas**.

---

## Acceptance Criteria

### AC1: Public Schema com Tabelas de Metadata

**Given** a PostgreSQL 16+ database instance
**When** Flyway migrations are executed
**Then** the `public` schema contains metadata tables:
- `public.tenants` (id, nome, schema_name, cnpj, email, ativo, data_criacao, data_atualizacao)
- `public.users` (id, nome, email, google_id, ativo, ultimo_login, created_at, updated_at)
- `public.user_tenants` (user_id, tenant_id, profile_id, status, created_at)
**And** indexes are created: `idx_tenants_schema_name`, `idx_users_email`, `idx_user_tenants_user_id`

### AC2: Criação Dinâmica de Schema por Tenant

**Given** a new tenant creation
**When** `TenantProvisioner` service is called
**Then** a new schema `tenant_{uuid}` is created dynamically
**And** the schema contains tables: `profiles`, `profile_roles`
**And** default profiles are seeded: Admin, Gerente, Vendedor
**And** the operation completes in < 30 seconds (NFR2)
**And** success rate is > 99% (NFR1)

### AC3: Estratégia de Migrations Flyway

**Given** Flyway migration strategy
**When** database migrations run
**Then** migrations are versioned (V001__, V002__, etc.)
**And** migrations can be applied to multiple tenant schemas
**And** rollback strategy is documented

---

## Tasks & Subtasks

### Task 1: Criar Migrations Flyway para Public Schema
**Subtasks:**
1. [x] Criar migration V003 para tabela `users`
2. [x] Criar migration V004 para tabelas `companies` e `company_users`
3. [x] Adicionar indexes otimizados para queries multi-tenant

### Task 2: Implementar TenantProvisioner Service
**Subtasks:**
1. [ ] Criar serviço `TenantProvisioner` no módulo tenant
2. [ ] Implementar criação dinâmica de schema PostgreSQL
3. [ ] Implementar aplicação de migrations Flyway por schema
4. [ ] Implementar seed de dados padrão (profiles, roles)

### Task 3: Atualizar FlywayConfiguration
**Subtasks:**
1. [ ] Configurar multi-location Flyway (public + tenant)
2. [ ] Implementar strategy para aplicar migrations em múltiplos schemas

### Task 4: Criar Testes de Integração
**Subtasks:**
1. [ ] Criar testes para criação de tenant schema
2. [ ] Validar migrations aplicadas corretamente
3. [ ] Validar performance < 30s

---

## Definition of Done

- [ ] Migrations Flyway criadas e testadas
- [ ] TenantProvisioner service implementado
- [ ] Testes de criação de schema validados
- [ ] Performance < 30s validada
- [ ] Documentação atualizada

---

**Story criada por**: poly (PM Agent)
**Data**: 2025-12-22
**Baseado em**: Epic 7, PRD (FR24)
