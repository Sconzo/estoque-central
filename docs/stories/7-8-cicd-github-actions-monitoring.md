# Story 7.8: CI/CD Pipeline GitHub Actions + Monitoring

**Epic**: 7 - Infraestrutura Multi-Tenant e Deploy
**Story ID**: 7.8
**Status**: completed
**Created**: 2025-12-22
**Completed**: 2025-12-22

---

## User Story

As a **Developer**,
I want **automated CI/CD pipelines with monitoring**,
So that **code changes are tested, built, and deployed automatically with observability**.

---

## Acceptance Criteria

### AC1: GitHub Actions Workflow Backend

**Given** GitHub Actions workflow for backend
**When** code is pushed to `main` branch
**Then** workflow runs: test → build → docker build → push to GHCR → deploy to Azure
**And** tests must pass before deployment
**And** Docker image is tagged with commit SHA and `latest`
**And** deployment to Azure Container Apps is automated

### AC2: GitHub Actions Workflow Frontend

**Given** GitHub Actions workflow for frontend
**When** code is pushed to `main` branch
**Then** workflow runs: lint → test → build → deploy to Static Web Apps
**And** Angular tests run in headless Chrome
**And** production build is optimized

### AC3: Azure Application Insights Setup

**Given** Azure Application Insights setup
**When** application runs in production
**Then** logs, metrics, and traces are collected (ARCH13)
**And** custom events can be logged
**And** performance metrics are tracked
**And** alerts are configured for critical errors

### AC4: Health Check Monitoring

**Given** health check monitoring
**When** Container App is running
**Then** `/health` endpoint returns 200 OK when healthy
**And** `/health` endpoint returns 503 Service Unavailable when unhealthy
**And** health check validates database and Redis connectivity (ARCH27)
**And** unhealthy instances are restarted automatically

### AC5: Rollback Strategy

**Given** rollback strategy
**When** deployment fails or issues are detected
**Then** blue-green deployment allows instant rollback (ARCH29)
**And** traffic can be shifted between revisions (ARCH30)
**And** previous revision can be activated in < 5 minutes

---

## Definition of Done

- [x] CI/CD pipelines criados e funcionando
- [x] Testes automatizados executando
- [x] Application Insights configurado
- [x] Health checks implementados
- [x] Rollback strategy testada (blue-green deployment)

---

## Implementation Summary

### ✅ AC1: GitHub Actions Workflow Backend
**Status**: COMPLETE
- **File**: `.github/workflows/backend-ci-cd.yml`
- **Pipeline Stages**:
  1. **Test**: Unit + Integration tests with PostgreSQL 17 + Redis 8 ✅
  2. **Build**: Multi-stage Docker build + push to GHCR ✅
  3. **Deploy Dev**: Automated deployment to Container Apps (dev) ✅
  4. **Deploy Prod**: Blue-green deployment with smoke tests ✅
- **Tagging**: `latest` + `sha-{commit}` ✅
- **Registry**: GitHub Container Registry (ghcr.io) ✅
- **Auto-rollback**: On smoke test failure ✅

### ✅ AC2: GitHub Actions Workflow Frontend
**Status**: COMPLETE
- **File**: `.github/workflows/frontend-ci-cd.yml`
- **Pipeline Stages**:
  1. **Lint & Test**: ESLint + Jasmine (headless Chrome) ✅
  2. **Build**: Development + Production builds ✅
  3. **Deploy Dev**: Azure Static Web Apps (dev) ✅
  4. **Deploy Prod**: Azure Static Web Apps (prod) + smoke tests ✅
- **Optimizations**: Production build minified + tree-shaking ✅
- **Code Coverage**: Uploaded to Codecov ✅

### ✅ AC3: Azure Application Insights Setup
**Status**: COMPLETE
- **Configuration**: `application.properties` (lines 60-69)
- **Connection String**: Environment variable `APPLICATIONINSIGHTS_CONNECTION_STRING` ✅
- **Auto-collection**:
  - HTTP requests (duration, status codes) ✅
  - Dependencies (database, Redis, external APIs) ✅
  - Exceptions (stack traces, context) ✅
  - Performance counters (CPU, memory, GC) ✅
- **Custom Events**: Support for business metrics ✅
- **W3C Tracing**: Distributed tracing enabled ✅

### ✅ AC4: Health Check Monitoring
**Status**: COMPLETE
- **Endpoints**:
  - `/actuator/health` - Overall status ✅
  - `/actuator/health/liveness` - Liveness probe ✅
  - `/actuator/health/readiness` - Readiness probe ✅
- **Custom Health Indicators**:
  - **DatabaseHealthIndicator**: Validates PostgreSQL connectivity (`SELECT 1`) ✅
  - **RedisHealthIndicator**: Validates Redis connectivity (`PING`) ✅
- **Health Groups**:
  - Liveness: `livenessState` ✅
  - Readiness: `readinessState`, `database`, `redis` ✅
- **Auto-restart**: Unhealthy instances restarted by Container Apps ✅

### ✅ AC5: Rollback Strategy
**Status**: COMPLETE
- **Blue-Green Deployment**: Revision-based traffic splitting ✅
- **Traffic Shift**: 0% → 100% after smoke tests pass ✅
- **Rollback Time**: < 5 minutes (instant traffic switch) ✅
- **Auto-rollback**: CI/CD pipeline deactivates failed revision ✅
- **Manual Rollback**: Azure CLI commands documented ✅

---

## Implementation Files

### CI/CD Workflows
1. `.github/workflows/backend-ci-cd.yml` - Backend CI/CD pipeline
2. `.github/workflows/frontend-ci-cd.yml` - Frontend CI/CD pipeline

### Health Indicators
3. `backend/src/main/java/com/estoquecentral/shared/health/DatabaseHealthIndicator.java` - PostgreSQL health check
4. `backend/src/main/java/com/estoquecentral/shared/health/RedisHealthIndicator.java` - Redis health check

### Configuration
5. `backend/src/main/resources/application.properties` - Updated with Application Insights + health groups

### Documentation
6. `docs/monitoring-observability.md` - Comprehensive monitoring guide

---

## Technical Details

### Backend Pipeline Flow
```
Push to main/develop
    ↓
Run Tests (PostgreSQL + Redis services)
    ↓ (tests pass)
Build Docker Image (multi-stage)
    ↓
Push to ghcr.io (tag: latest + sha-commit)
    ↓
Deploy to Azure Container Apps
    ↓
Run Smoke Tests (health check)
    ↓ (tests pass)
Switch Traffic 0% → 100%
    ↓ (tests fail)
Auto-rollback (deactivate revision)
```

### Health Check Response Example
```json
{
  "status": "UP",
  "components": {
    "livenessState": {
      "status": "UP"
    },
    "readinessState": {
      "status": "UP"
    },
    "database": {
      "status": "UP",
      "details": {
        "database": "PostgreSQL",
        "version": "17.1",
        "activeConnections": 5,
        "validationQuery": "SELECT 1"
      }
    },
    "redis": {
      "status": "UP",
      "details": {
        "cache": "Redis",
        "address": "rediss://redis.cache.windows.net:6380",
        "connectionPoolSize": 20,
        "validationCommand": "PING"
      }
    }
  }
}
```

### Rollback Commands
```bash
# List revisions
az containerapp revision list \
  --name ca-estoque-backend-prod \
  --resource-group rg-estoque-prod

# Rollback to previous revision
az containerapp revision set-traffic \
  --name ca-estoque-backend-prod \
  --resource-group rg-estoque-prod \
  --revision-weight <previous-revision>=100

# Deactivate problematic revision
az containerapp revision deactivate \
  --name ca-estoque-backend-prod \
  --resource-group rg-estoque-prod \
  --revision <bad-revision>
```

### Required GitHub Secrets
- `AZURE_CREDENTIALS_DEV` - Service principal (dev)
- `AZURE_CREDENTIALS_PROD` - Service principal (prod)
- `AZURE_STATIC_WEB_APPS_API_TOKEN_DEV` - SWA token (dev)
- `AZURE_STATIC_WEB_APPS_API_TOKEN_PROD` - SWA token (prod)

---

**Story criada por**: poly (PM Agent)
**Data**: 2025-12-22
**Baseado em**: Epic 7, PRD (ARCH13, ARCH27-ARCH30)
**Implementado por**: Amelia (Dev Agent)
**Completion**: 2025-12-22
