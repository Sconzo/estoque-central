# Azure Deployment - Estoque Central
**Story 7.7**: Azure Container Apps + Static Web Apps Deployment

This folder contains Infrastructure as Code (IaC) for deploying Estoque Central to Azure using Bicep templates.

---

## 📁 Folder Structure

```
azure/
├── main.bicep                      # Main orchestration template
├── parameters.json                 # Deployment parameters (template)
├── deploy.sh                       # Automated deployment script
├── README.md                       # This file
└── modules/
    ├── container-apps.bicep        # Backend Container App
    ├── static-web-app.bicep        # Frontend Static Web App
    ├── key-vault.bicep             # Secrets management
    ├── postgresql.bicep            # Database
    └── redis.bicep                 # Cache
```

---

## 🏗️ Infrastructure Components

### 1. **Azure Container Apps** (Backend)
- **Auto-scaling**: Min 2 → Max 10 replicas (production)
- **Resources**: 1 vCPU, 2Gi memory per replica
- **Health Probes**: Liveness `/actuator/health`, Readiness `/actuator/health/readiness`
- **Scaling Rules**:
  - HTTP: 50 concurrent requests
  - CPU: 70% utilization

### 2. **Azure Static Web Apps** (Frontend)
- **CDN**: Global content delivery
- **HTTPS**: Enforced automatically
- **Auto-deploy**: From GitHub via GitHub Actions
- **SKU**: Free (dev), Standard (prod)

### 3. **Azure Database for PostgreSQL**
- **Version**: PostgreSQL 17
- **SKU**: Burstable B2s (dev), GeneralPurpose D4s_v3 (prod)
- **Backup**: 7 days retention, geo-redundant (prod)
- **HA**: Zone-redundant (prod only)

### 4. **Azure Cache for Redis**
- **TLS**: 1.2+ enforced
- **Eviction Policy**: LRU (Least Recently Used)
- **SKU**: Basic C0 (dev), Standard C1 (prod)

### 5. **Azure Key Vault**
- **Secrets**:
  - `database-password`
  - `jwt-secret`
  - `google-oauth-client-secret`
- **Access**: System-assigned Managed Identity

---

## 🚀 Prerequisites

1. **Azure CLI** (version 2.50+)
   ```bash
   az version
   # If not installed: https://learn.microsoft.com/cli/azure/install-azure-cli
   ```

2. **Azure Subscription**
   ```bash
   az login
   az account set --subscription <subscription-id>
   ```

3. **Docker** (for building container images)
   ```bash
   docker --version
   ```

4. **GitHub Account** (for Static Web Apps auto-deploy)

---

## 📝 Step-by-Step Deployment

### Step 1: Configure Parameters

Edit `azure/parameters.json` with your values:

```json
{
  "environment": "dev",
  "location": "eastus",
  "uniqueSuffix": "yourcompany",
  "googleOAuthClientId": "your-client-id.apps.googleusercontent.com"
}
```

### Step 2: Set Secrets

Create a temporary Key Vault for bootstrap secrets (or use environment variables):

```bash
# Option 1: Environment variables
export DB_PASSWORD="SecurePassword123!"
export JWT_SECRET="your-256-bit-secret-minimum-32-characters"
export GOOGLE_OAUTH_SECRET="your-google-oauth-secret"

# Option 2: Azure Key Vault (recommended for production)
az keyvault create --name kv-secrets --resource-group rg-secrets
az keyvault secret set --vault-name kv-secrets --name database-password --value "SecurePassword123!"
az keyvault secret set --vault-name kv-secrets --name jwt-secret --value "your-jwt-secret"
az keyvault secret set --vault-name kv-secrets --name google-oauth-client-secret --value "your-oauth-secret"
```

### Step 3: Validate Deployment (What-If)

```bash
chmod +x azure/deploy.sh
./azure/deploy.sh dev --what-if
```

### Step 4: Deploy Infrastructure

```bash
./azure/deploy.sh dev
```

**Deployment time**: ~15-20 minutes

### Step 5: Build and Push Docker Image

```bash
# Build backend image
docker build -f docker/backend.Dockerfile -t estoque-central:latest .

# Tag for Azure Container Registry
docker tag estoque-central:latest <your-acr>.azurecr.io/estoque-central:latest

# Login to ACR
az acr login --name <your-acr>

# Push image
docker push <your-acr>.azurecr.io/estoque-central:latest
```

### Step 6: Update Container App

```bash
az containerapp update \
  --name ca-estoque-backend-dev \
  --resource-group rg-estoque-dev \
  --image <your-acr>.azurecr.io/estoque-central:latest
```

### Step 7: Verify Deployment

```bash
# Get backend URL
BACKEND_URL=$(az containerapp show \
  --name ca-estoque-backend-dev \
  --resource-group rg-estoque-dev \
  --query properties.configuration.ingress.fqdn -o tsv)

# Test health endpoint
curl https://$BACKEND_URL/actuator/health

# Expected response:
# {"status":"UP"}
```

---

## 🔧 Manual Deployment (Azure Portal)

If you prefer manual deployment via Azure Portal:

1. **Upload Bicep Templates**:
   - Navigate to: Azure Portal → Deploy a custom template → Build your own template
   - Upload `azure/main.bicep`

2. **Fill Parameters**:
   - Environment: dev/staging/prod
   - Location: East US
   - Unique Suffix: yourcompany
   - Secrets: Use Key Vault references

3. **Review + Create**:
   - Validate template
   - Review costs
   - Click "Create"

---

## 🔄 CI/CD Integration

The infrastructure supports automated deployments via GitHub Actions (Story 7.8).

**GitHub Actions Workflow** (to be created):
```yaml
name: Deploy to Azure

on:
  push:
    branches: [main, develop]

jobs:
  deploy:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v3
      - name: Build Docker image
        run: docker build -f docker/backend.Dockerfile -t estoque-central .
      - name: Push to ACR
        run: |
          az acr login --name <your-acr>
          docker push <your-acr>.azurecr.io/estoque-central:latest
      - name: Update Container App
        run: az containerapp update ...
```

---

## 🌍 Environment Configuration

### Development
- **Auto-scaling**: Min 1, Max 3
- **SKU**: Basic/Burstable tiers
- **Cost**: ~$50-100/month

### Production
- **Auto-scaling**: Min 2, Max 10
- **SKU**: Standard/GeneralPurpose tiers
- **HA**: Zone-redundant database
- **Cost**: ~$300-500/month

---

## 🔐 Security Best Practices

1. **Secrets Management**:
   - ✅ All secrets in Azure Key Vault
   - ✅ Managed Identity for Container Apps
   - ✅ No secrets in code or environment files

2. **Network Security**:
   - 🔄 Public endpoints (MVP) → Private Endpoints (production)
   - ✅ HTTPS enforced
   - ✅ CORS configured

3. **Database Security**:
   - ✅ SSL/TLS required
   - ✅ Firewall rules (Azure services only)
   - 🔄 Migrate to Private Endpoint for production

---

## 📊 Monitoring

### Application Insights
Container Apps automatically sends telemetry to Log Analytics.

**View logs**:
```bash
az containerapp logs show \
  --name ca-estoque-backend-dev \
  --resource-group rg-estoque-dev \
  --tail 100
```

### Health Checks
- **Liveness**: `/actuator/health`
- **Readiness**: `/actuator/health/readiness`
- **Metrics**: `/actuator/metrics`

---

## 🧹 Cleanup

To delete all resources:

```bash
az group delete --name rg-estoque-dev --yes --no-wait
```

---

## 📚 References

- [Azure Container Apps Documentation](https://learn.microsoft.com/azure/container-apps/)
- [Azure Static Web Apps Documentation](https://learn.microsoft.com/azure/static-web-apps/)
- [Bicep Language Reference](https://learn.microsoft.com/azure/azure-resource-manager/bicep/)
- [Azure Architecture Center - Multi-Tenant SaaS](https://learn.microsoft.com/azure/architecture/guide/multitenant/overview)

---

## ✅ Acceptance Criteria Checklist

- [x] **AC1**: Container Apps auto-scaling (min 2, max 10)
- [x] **AC2**: Static Web App with CDN and HTTPS
- [x] **AC3**: Secrets in Key Vault
- [x] **AC4**: CORS configured
- [x] **AC5**: Health probes configured

---

**Story**: 7.7
**Created**: 2025-12-22
**Implemented by**: Amelia (Dev Agent)
