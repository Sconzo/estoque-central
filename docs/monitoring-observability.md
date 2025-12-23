# Monitoring & Observability - Estoque Central
**Story 7.8**: CI/CD Pipeline + Monitoring

This document describes the monitoring, observability, and rollback strategies for Estoque Central.

---

## 📊 Table of Contents

1. [Health Checks](#health-checks)
2. [Application Insights](#application-insights)
3. [CI/CD Pipelines](#cicd-pipelines)
4. [Rollback Strategy](#rollback-strategy)
5. [Alerts & Notifications](#alerts--notifications)
6. [Dashboards](#dashboards)

---

## 🏥 Health Checks

### Endpoints

| Endpoint | Description | Used By |
|----------|-------------|---------|
| `/actuator/health` | Overall health status | Load balancer, monitoring |
| `/actuator/health/liveness` | Liveness probe (is app running?) | Container Apps liveness probe |
| `/actuator/health/readiness` | Readiness probe (can accept traffic?) | Container Apps readiness probe |
| `/actuator/info` | Application metadata | CI/CD, monitoring |
| `/actuator/metrics` | Prometheus-compatible metrics | Observability |

### Custom Health Indicators

#### DatabaseHealthIndicator
**File**: `backend/src/main/java/com/estoquecentral/shared/health/DatabaseHealthIndicator.java`

Validates PostgreSQL connectivity:
- Query: `SELECT 1`
- Timeout: 3 seconds
- Details: PostgreSQL version, active connections

```json
{
  "status": "UP",
  "components": {
    "database": {
      "status": "UP",
      "details": {
        "database": "PostgreSQL",
        "version": "17.1",
        "activeConnections": 5
      }
    }
  }
}
```

#### RedisHealthIndicator
**File**: `backend/src/main/java/com/estoquecentral/shared/health/RedisHealthIndicator.java`

Validates Redis connectivity:
- Command: `PING → PONG`
- Timeout: 3 seconds
- Details: Redis address, connection pool size

```json
{
  "status": "UP",
  "components": {
    "redis": {
      "status": "UP",
      "details": {
        "cache": "Redis",
        "address": "rediss://your-redis.redis.cache.windows.net:6380",
        "connectionPoolSize": 20
      }
    }
  }
}
```

### Health Probe Configuration

**Liveness Probe** (Is the app alive?):
- Path: `/actuator/health/liveness`
- Interval: 30s
- Timeout: 3s
- Failure threshold: 3 (restart after 3 failures)
- Includes: `livenessState`

**Readiness Probe** (Can accept traffic?):
- Path: `/actuator/health/readiness`
- Interval: 10s
- Timeout: 3s
- Failure threshold: 3 (remove from load balancer after 3 failures)
- Includes: `readinessState`, `database`, `redis`

**Startup Probe** (Is the app starting?):
- Path: `/actuator/health`
- Interval: 5s
- Timeout: 3s
- Failure threshold: 30 (150 seconds max startup time)

---

## 📈 Application Insights

### Configuration

**Connection String**: Set via environment variable
```
APPLICATIONINSIGHTS_CONNECTION_STRING=InstrumentationKey=...;IngestionEndpoint=https://...
```

**Instrumentation Key** (legacy):
```
APPLICATIONINSIGHTS_INSTRUMENTATION_KEY=your-instrumentation-key
```

### Auto-Collected Telemetry

1. **HTTP Requests**:
   - Request duration
   - Response status codes
   - Request paths
   - User agents

2. **Dependencies**:
   - Database calls (PostgreSQL)
   - Redis operations
   - External HTTP calls

3. **Exceptions**:
   - Unhandled exceptions
   - Stack traces
   - Request context

4. **Performance Counters**:
   - CPU usage
   - Memory usage
   - GC metrics
   - Thread count

5. **Custom Events**:
   - Business metrics
   - User actions
   - Feature usage

### Custom Telemetry Example

```java
import com.microsoft.applicationinsights.TelemetryClient;

@Service
public class ProductService {
    private final TelemetryClient telemetryClient;

    public void createProduct(Product product) {
        // Track custom event
        telemetryClient.trackEvent("ProductCreated",
            Map.of("tenantId", tenantId, "category", product.getCategory()));

        // Track custom metric
        telemetryClient.trackMetric("ProductCreationTime", duration);
    }
}
```

### Kusto Queries (Log Analytics)

**Top 10 slowest requests**:
```kql
requests
| where timestamp > ago(1h)
| top 10 by duration desc
| project timestamp, name, duration, resultCode
```

**Error rate by endpoint**:
```kql
requests
| where timestamp > ago(24h)
| summarize
    TotalRequests = count(),
    Errors = countif(resultCode >= 400)
    by name
| extend ErrorRate = (Errors * 100.0) / TotalRequests
| order by ErrorRate desc
```

**Database call duration**:
```kql
dependencies
| where type == "SQL"
| summarize avg(duration), max(duration), percentile(duration, 95)
    by name
| order by avg_duration desc
```

---

## 🚀 CI/CD Pipelines

### Backend Pipeline
**File**: `.github/workflows/backend-ci-cd.yml`

**Triggers**:
- Push to `main` or `develop` branches
- Pull requests to `main` or `develop`
- Paths: `backend/**`, `docker/backend.Dockerfile`

**Stages**:

1. **Test** (10-15 minutes):
   - Unit tests (JUnit + Mockito)
   - Integration tests (Testcontainers)
   - Code coverage (JaCoCo)
   - Services: PostgreSQL 17, Redis 8

2. **Build** (5-10 minutes):
   - Multi-stage Docker build
   - Tag: `ghcr.io/{org}/backend:latest`, `ghcr.io/{org}/backend:sha-{commit}`
   - Cache: GitHub Actions cache
   - Registry: GitHub Container Registry (GHCR)

3. **Deploy Dev** (3-5 minutes):
   - Environment: `development`
   - Trigger: Push to `develop`
   - Target: Azure Container Apps (dev)
   - Smoke tests: Health check

4. **Deploy Prod** (5-10 minutes):
   - Environment: `production`
   - Trigger: Push to `main`
   - Target: Azure Container Apps (prod)
   - Strategy: Blue-Green deployment
   - Traffic: 0% → 100% after smoke tests
   - Auto-rollback on failure

### Frontend Pipeline
**File**: `.github/workflows/frontend-ci-cd.yml`

**Triggers**:
- Push to `main` or `develop` branches
- Pull requests to `main` or `develop`
- Paths: `frontend/**`

**Stages**:

1. **Lint & Test** (5-10 minutes):
   - ESLint
   - Unit tests (Jasmine + Karma, headless Chrome)
   - Code coverage

2. **Build** (5-10 minutes):
   - Development build: `npm run build -- --configuration=development`
   - Production build: `npm run build -- --configuration=production`
   - Artifacts: `dist/frontend/browser/`

3. **Deploy Dev** (2-3 minutes):
   - Environment: `development`
   - Trigger: Push to `develop`
   - Target: Azure Static Web Apps (dev)
   - E2E tests (optional)

4. **Deploy Prod** (2-3 minutes):
   - Environment: `production`
   - Trigger: Push to `main`
   - Target: Azure Static Web Apps (prod)
   - Smoke tests: Homepage load check

### Secrets Configuration

**GitHub Secrets** (required):

| Secret | Description |
|--------|-------------|
| `AZURE_CREDENTIALS_DEV` | Azure service principal for dev |
| `AZURE_CREDENTIALS_PROD` | Azure service principal for prod |
| `AZURE_STATIC_WEB_APPS_API_TOKEN_DEV` | Static Web App deployment token (dev) |
| `AZURE_STATIC_WEB_APPS_API_TOKEN_PROD` | Static Web App deployment token (prod) |

---

## 🔄 Rollback Strategy

### Blue-Green Deployment

Azure Container Apps supports **revision-based traffic splitting** for zero-downtime deployments.

#### Deployment Process

1. **New Revision Created**:
   - New Docker image is deployed
   - Traffic weight: 0% (no production traffic)
   - New revision gets unique URL for testing

2. **Smoke Tests Run**:
   - Health checks
   - Critical endpoint validation
   - Database/Redis connectivity

3. **Traffic Shift** (if tests pass):
   - Gradually shift traffic: 0% → 100%
   - Old revision remains active (instant rollback possible)

4. **Deactivate Old Revision** (after validation):
   - After 24-48 hours of stable operation
   - Old revision is deactivated to save resources

#### Manual Rollback (< 5 minutes)

**Option 1: Shift traffic back to previous revision**
```bash
# List revisions
az containerapp revision list \
  --name ca-estoque-backend-prod \
  --resource-group rg-estoque-prod \
  --output table

# Shift 100% traffic to previous revision
az containerapp revision set-traffic \
  --name ca-estoque-backend-prod \
  --resource-group rg-estoque-prod \
  --revision-weight <previous-revision>=100
```

**Option 2: Deactivate problematic revision**
```bash
az containerapp revision deactivate \
  --name ca-estoque-backend-prod \
  --resource-group rg-estoque-prod \
  --revision <problematic-revision>
```

#### Automatic Rollback

The CI/CD pipeline automatically rolls back on failure:

```yaml
- name: Rollback on failure
  if: failure()
  run: |
    az containerapp revision deactivate \
      --name ca-estoque-backend-prod \
      --resource-group rg-estoque-prod \
      --revision ${{ steps.deploy.outputs.revision-name }}
```

---

## 🔔 Alerts & Notifications

### Recommended Alerts

1. **High Error Rate**:
   - Condition: Error rate > 5% for 5 minutes
   - Severity: Critical
   - Action: Notify on-call engineer

2. **High Response Time**:
   - Condition: P95 response time > 2 seconds for 10 minutes
   - Severity: Warning
   - Action: Notify development team

3. **Health Check Failures**:
   - Condition: Health check fails 3 times consecutively
   - Severity: Critical
   - Action: Auto-restart + notify

4. **High Memory Usage**:
   - Condition: Memory usage > 85% for 15 minutes
   - Severity: Warning
   - Action: Notify DevOps team

5. **Database Connection Failures**:
   - Condition: Database health DOWN for 2 minutes
   - Severity: Critical
   - Action: Notify on-call + escalate

### Azure Monitor Alert Rules

Create alerts in Azure Portal:
1. Navigate to: Azure Monitor → Alerts → Create alert rule
2. Select resource: Container App
3. Define condition (e.g., HTTP 5xx > threshold)
4. Configure action group (email, SMS, webhook)

---

## 📊 Dashboards

### Application Insights Dashboards

**1. Overview Dashboard**:
- Request rate
- Average response time
- Error rate
- Availability

**2. Performance Dashboard**:
- P50, P95, P99 response times
- Slowest operations
- Database query performance
- Redis operation latency

**3. Dependencies Dashboard**:
- Database calls
- Redis operations
- External API calls
- Dependency failure rate

**4. Exceptions Dashboard**:
- Exception count by type
- Top failing requests
- Stack traces
- Affected users

### Grafana (Optional)

For advanced visualization, export metrics to Grafana:

1. Enable Prometheus endpoint: `/actuator/prometheus`
2. Configure Prometheus to scrape metrics
3. Import Grafana dashboards:
   - Spring Boot 2.x Dashboard (ID: 11378)
   - JVM Micrometer Dashboard (ID: 4701)

---

## ✅ Acceptance Criteria Checklist

- [x] **AC1**: Backend CI/CD workflow (test → build → deploy)
- [x] **AC2**: Frontend CI/CD workflow (lint → test → build → deploy)
- [x] **AC3**: Application Insights configured
- [x] **AC4**: Health checks (database + Redis)
- [x] **AC5**: Blue-green deployment with rollback

---

**Created**: 2025-12-22
**Story**: 7.8
**Implemented by**: Amelia (Dev Agent)
