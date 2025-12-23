// =============================================================================
// Main Azure Infrastructure - Estoque Central
// Story 7.7: Azure Container Apps + Static Web Apps Deployment
// =============================================================================
// Deploy command:
// az deployment sub create --location eastus --template-file azure/main.bicep --parameters azure/parameters.json
// =============================================================================

targetScope = 'subscription'

@description('Azure region for all resources')
param location string = 'eastus'

@description('Environment name (dev, staging, prod)')
@allowed([
  'dev'
  'staging'
  'prod'
])
param environment string = 'dev'

@description('Unique suffix for resource names (e.g., company name or random string)')
param uniqueSuffix string = 'estoque'

@description('Database administrator password')
@secure()
param databasePassword string

@description('JWT secret for token signing')
@secure()
param jwtSecret string

@description('Google OAuth client ID')
param googleOAuthClientId string

@description('Google OAuth client secret')
@secure()
param googleOAuthClientSecret string

// =============================================================================
// Variables
// =============================================================================

var resourceGroupName = 'rg-${uniqueSuffix}-${environment}'
var tags = {
  Environment: environment
  Application: 'Estoque Central'
  ManagedBy: 'Bicep'
  Story: '7.7'
}

// =============================================================================
// Resource Group
// =============================================================================

resource resourceGroup 'Microsoft.Resources/resourceGroups@2023-07-01' = {
  name: resourceGroupName
  location: location
  tags: tags
}

// =============================================================================
// Key Vault (must be created first - other resources depend on it)
// =============================================================================

module keyVault './modules/key-vault.bicep' = {
  name: 'keyVault-deployment'
  scope: resourceGroup
  params: {
    location: location
    environment: environment
    uniqueSuffix: uniqueSuffix
    tags: tags
    // Secrets
    databasePassword: databasePassword
    jwtSecret: jwtSecret
    googleOAuthClientSecret: googleOAuthClientSecret
  }
}

// =============================================================================
// PostgreSQL Flexible Server
// =============================================================================

module postgresql './modules/postgresql.bicep' = {
  name: 'postgresql-deployment'
  scope: resourceGroup
  params: {
    location: location
    environment: environment
    uniqueSuffix: uniqueSuffix
    tags: tags
    administratorPassword: databasePassword
  }
}

// =============================================================================
// Azure Cache for Redis
// =============================================================================

module redis './modules/redis.bicep' = {
  name: 'redis-deployment'
  scope: resourceGroup
  params: {
    location: location
    environment: environment
    uniqueSuffix: uniqueSuffix
    tags: tags
  }
}

// =============================================================================
// Container Apps Environment + Backend
// =============================================================================

module containerApps './modules/container-apps.bicep' = {
  name: 'containerApps-deployment'
  scope: resourceGroup
  params: {
    location: location
    environment: environment
    uniqueSuffix: uniqueSuffix
    tags: tags
    // Database connection
    databaseHost: postgresql.outputs.fullyQualifiedDomainName
    databaseName: 'estoque_central'
    databaseUser: 'estoqueadmin'
    // Redis connection
    redisHost: redis.outputs.hostName
    redisPort: redis.outputs.sslPort
    redisSslEnabled: true
    // OAuth
    googleOAuthClientId: googleOAuthClientId
    // Key Vault reference
    keyVaultName: keyVault.outputs.keyVaultName
  }
  dependsOn: [
    keyVault
    postgresql
    redis
  ]
}

// =============================================================================
// Static Web App (Frontend)
// =============================================================================

module staticWebApp './modules/static-web-app.bicep' = {
  name: 'staticWebApp-deployment'
  scope: resourceGroup
  params: {
    location: location
    environment: environment
    uniqueSuffix: uniqueSuffix
    tags: tags
    backendUrl: containerApps.outputs.backendUrl
  }
}

// =============================================================================
// Outputs
// =============================================================================

output resourceGroupName string = resourceGroupName
output backendUrl string = containerApps.outputs.backendUrl
output frontendUrl string = staticWebApp.outputs.frontendUrl
output keyVaultName string = keyVault.outputs.keyVaultName
output databaseHost string = postgresql.outputs.fullyQualifiedDomainName
output redisHost string = redis.outputs.hostName
