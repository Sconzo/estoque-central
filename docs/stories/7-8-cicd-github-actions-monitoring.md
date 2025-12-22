# Story 7.8: CI/CD Pipeline GitHub Actions + Monitoring

**Epic**: 7 - Infraestrutura Multi-Tenant e Deploy
**Story ID**: 7.8
**Status**: pending
**Created**: 2025-12-22

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

- [ ] CI/CD pipelines criados e funcionando
- [ ] Testes automatizados executando
- [ ] Application Insights configurado
- [ ] Health checks implementados
- [ ] Rollback strategy testada

---

**Story criada por**: poly (PM Agent)
**Data**: 2025-12-22
**Baseado em**: Epic 7, PRD (ARCH13, ARCH27-ARCH30)
