#!/bin/bash
# =============================================================================
# Azure Deployment Script - Estoque Central
# Story 7.7: Azure Container Apps + Static Web Apps
# =============================================================================
# Usage:
#   ./azure/deploy.sh <environment> [--what-if]
# Examples:
#   ./azure/deploy.sh dev
#   ./azure/deploy.sh prod --what-if
# =============================================================================

set -e  # Exit on error

# =============================================================================
# Configuration
# =============================================================================

ENVIRONMENT=${1:-dev}
WHAT_IF=${2:-}
LOCATION="eastus"
UNIQUE_SUFFIX="estoque"
SUBSCRIPTION_ID=$(az account show --query id -o tsv)

# Color codes for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# =============================================================================
# Functions
# =============================================================================

log_info() {
    echo -e "${GREEN}[INFO]${NC} $1"
}

log_warn() {
    echo -e "${YELLOW}[WARN]${NC} $1"
}

log_error() {
    echo -e "${RED}[ERROR]${NC} $1"
}

# =============================================================================
# Validation
# =============================================================================

log_info "Validating environment: $ENVIRONMENT"

if [[ ! "$ENVIRONMENT" =~ ^(dev|staging|prod)$ ]]; then
    log_error "Invalid environment. Must be: dev, staging, or prod"
    exit 1
fi

# Check if Azure CLI is installed
if ! command -v az &> /dev/null; then
    log_error "Azure CLI not found. Please install: https://learn.microsoft.com/cli/azure/install-azure-cli"
    exit 1
fi

# Check if logged in
if ! az account show &> /dev/null; then
    log_error "Not logged in to Azure. Run: az login"
    exit 1
fi

log_info "Using subscription: $SUBSCRIPTION_ID"

# =============================================================================
# Secrets Validation
# =============================================================================

log_info "Validating secrets..."

# Prompt for secrets if not in Key Vault
read -sp "Enter database password: " DB_PASSWORD
echo
read -sp "Enter JWT secret (min 256 bits): " JWT_SECRET
echo
read -sp "Enter Google OAuth client secret: " GOOGLE_OAUTH_SECRET
echo

if [ ${#JWT_SECRET} -lt 32 ]; then
    log_error "JWT secret must be at least 256 bits (32 characters)"
    exit 1
fi

# =============================================================================
# What-If Mode
# =============================================================================

if [[ "$WHAT_IF" == "--what-if" ]]; then
    log_warn "Running in WHAT-IF mode (no actual deployment)"
    WHAT_IF_FLAG="--what-if"
else
    WHAT_IF_FLAG=""
fi

# =============================================================================
# Deployment
# =============================================================================

log_info "Starting deployment to Azure..."
log_info "Environment: $ENVIRONMENT"
log_info "Location: $LOCATION"
log_info "Suffix: $UNIQUE_SUFFIX"

# Deploy infrastructure
az deployment sub create \
    --name "estoque-central-${ENVIRONMENT}-$(date +%Y%m%d-%H%M%S)" \
    --location "$LOCATION" \
    --template-file azure/main.bicep \
    --parameters \
        environment="$ENVIRONMENT" \
        location="$LOCATION" \
        uniqueSuffix="$UNIQUE_SUFFIX" \
        databasePassword="$DB_PASSWORD" \
        jwtSecret="$JWT_SECRET" \
        googleOAuthClientId="$GOOGLE_OAUTH_CLIENT_ID" \
        googleOAuthClientSecret="$GOOGLE_OAUTH_SECRET" \
    $WHAT_IF_FLAG

if [[ "$WHAT_IF" != "--what-if" ]]; then
    log_info "✅ Deployment completed successfully!"

    # Get outputs
    BACKEND_URL=$(az deployment sub show \
        --name "estoque-central-${ENVIRONMENT}-latest" \
        --query properties.outputs.backendUrl.value -o tsv)

    FRONTEND_URL=$(az deployment sub show \
        --name "estoque-central-${ENVIRONMENT}-latest" \
        --query properties.outputs.frontendUrl.value -o tsv)

    log_info "Backend URL: $BACKEND_URL"
    log_info "Frontend URL: $FRONTEND_URL"

    log_info "Next steps:"
    log_info "1. Build and push Docker image to Azure Container Registry"
    log_info "2. Update Container App with new image"
    log_info "3. Configure GitHub Actions for CI/CD"
    log_info "4. Set up custom domain (if applicable)"
else
    log_warn "What-if mode completed. Review changes above."
fi
