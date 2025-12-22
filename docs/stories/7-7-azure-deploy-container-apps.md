# Story 7.7: Deploy Azure Container Apps + Static Web Apps

**Epic**: 7 - Infraestrutura Multi-Tenant e Deploy
**Story ID**: 7.7
**Status**: pending
**Created**: 2025-12-22

---

## User Story

As a **DevOps Engineer**,
I want **the application deployed to Azure infrastructure**,
So that **it runs in production with auto-scaling and high availability**.

---

## Acceptance Criteria

### AC1: Azure Container Apps Backend Deployment

**Given** Azure Container Apps environment
**When** backend is deployed
**Then** Container App is running with Spring Boot image
**And** auto-scaling is configured: min 2, max 10 replicas (ARCH10)
**And** CPU and memory limits are set: 1 vCPU, 2Gi memory
**And** scaling rules: HTTP concurrent requests (50), CPU (70%)
**And** health probes are configured: liveness `/health`, readiness `/health/ready` (ARCH28)

### AC2: Azure Static Web Apps Frontend Deployment

**Given** Azure Static Web Apps deployment
**When** frontend is deployed
**Then** Angular app is served via CDN
**And** custom domain is configured (if applicable)
**And** HTTPS is enforced

### AC3: Environment Variables and Secrets

**Given** environment variables configuration
**When** Container App starts
**Then** secrets are loaded from Azure Key Vault:
- `SPRING_DATASOURCE_PASSWORD`
- `APP_JWT_SECRET`
- `GOOGLE_OAUTH_CLIENT_SECRET`
**And** database connection string points to Azure PostgreSQL
**And** Redis connection string points to Azure Cache for Redis

### AC4: Networking Setup

**Given** networking setup
**When** frontend calls backend
**Then** requests route through Azure Front Door (optional for MVP)
**And** CORS is configured to allow frontend origin
**And** API is accessible at configured domain

---

## Definition of Done

- [ ] Backend deployado no Azure Container Apps
- [ ] Frontend deployado no Azure Static Web Apps
- [ ] Secrets configurados no Key Vault
- [ ] Networking e CORS configurados
- [ ] Health checks funcionando

---

**Story criada por**: poly (PM Agent)
**Data**: 2025-12-22
**Baseado em**: Epic 7, PRD (ARCH10, ARCH28)
