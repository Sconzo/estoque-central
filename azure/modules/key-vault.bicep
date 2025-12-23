// =============================================================================
// Azure Key Vault - Secrets Management
// Story 7.7: Environment Variables and Secrets (AC3)
// =============================================================================

param location string
param environment string
param uniqueSuffix string
param tags object

@secure()
param databasePassword string
@secure()
param jwtSecret string
@secure()
param googleOAuthClientSecret string

// =============================================================================
// Variables
// =============================================================================

var keyVaultName = 'kv-${uniqueSuffix}-${environment}'

// =============================================================================
// Key Vault
// =============================================================================

resource keyVault 'Microsoft.KeyVault/vaults@2023-07-01' = {
  name: keyVaultName
  location: location
  tags: tags
  properties: {
    sku: {
      family: 'A'
      name: 'standard'
    }
    tenantId: subscription().tenantId
    enableRbacAuthorization: false
    enabledForDeployment: true
    enabledForTemplateDeployment: true
    enabledForDiskEncryption: false
    enableSoftDelete: true
    softDeleteRetentionInDays: 90
    accessPolicies: [] // Will be updated by Container App managed identity
    networkAcls: {
      defaultAction: 'Allow' // Change to 'Deny' for production and use private endpoints
      bypass: 'AzureServices'
    }
  }
}

// =============================================================================
// Secrets
// =============================================================================

resource databasePasswordSecret 'Microsoft.KeyVault/vaults/secrets@2023-07-01' = {
  parent: keyVault
  name: 'database-password'
  properties: {
    value: databasePassword
  }
}

resource jwtSecretValue 'Microsoft.KeyVault/vaults/secrets@2023-07-01' = {
  parent: keyVault
  name: 'jwt-secret'
  properties: {
    value: jwtSecret
  }
}

resource googleOAuthClientSecretValue 'Microsoft.KeyVault/vaults/secrets@2023-07-01' = {
  parent: keyVault
  name: 'google-oauth-client-secret'
  properties: {
    value: googleOAuthClientSecret
  }
}

// =============================================================================
// Outputs
// =============================================================================

output keyVaultName string = keyVault.name
output keyVaultId string = keyVault.id
output keyVaultUri string = keyVault.properties.vaultUri
