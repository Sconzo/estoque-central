# Story 7.5: Setup Redis para Cache com Tenant Isolation

**Epic**: 7 - Infraestrutura Multi-Tenant e Deploy
**Story ID**: 7.5
**Status**: pending
**Created**: 2025-12-22

---

## User Story

As a **System**,
I want **Redis cache configured with tenant-prefixed keys**,
So that **cached data is isolated per tenant and performance is optimized**.

---

## Acceptance Criteria

### AC1: Redis Connection Configuration

**Given** Redis 7.2+ instance (Azure Cache for Redis)
**When** Spring Data Redis or Redisson is configured
**Then** connection is established to Redis instance
**And** TLS is enabled (minimum TLS 1.2)
**And** connection string is loaded from environment variables

### AC2: Tenant-Prefixed Cache Keys

**Given** cache key generation
**When** data is cached
**Then** keys are prefixed with `tenant:{tenantId}:*`
**And** examples: `tenant:123e4567:products:*`, `tenant:123e4567:users:*`
**And** cache isolation prevents cross-tenant data access (FR27)

### AC3: Session Management

**Given** session management
**When** user session is created
**Then** session data is stored in Redis
**And** session key includes tenant context
**And** TTL (Time To Live) is configured appropriately

### AC4: Cache Invalidation

**Given** cache invalidation
**When** tenant data is updated
**Then** corresponding cache keys are invalidated
**And** cache eviction policies are configured (LRU)

---

## Definition of Done

- [ ] Redis configurado e conectado
- [ ] Tenant-prefixed keys implementado
- [ ] Session management funcionando
- [ ] Cache invalidation testado
- [ ] Testes de isolamento validados

---

**Story criada por**: poly (PM Agent)
**Data**: 2025-12-22
**Baseado em**: Epic 7, PRD (FR27)
