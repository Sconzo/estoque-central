// =============================================================================
// Azure Database for PostgreSQL Flexible Server
// Story 7.7: Database Infrastructure
// =============================================================================

param location string
param environment string
param uniqueSuffix string
param tags object

@secure()
param administratorPassword string

// =============================================================================
// Variables
// =============================================================================

var postgresqlServerName = 'psql-${uniqueSuffix}-${environment}'
var administratorLogin = 'estoqueadmin'
var databaseName = 'estoque_central'

// SKU selection based on environment
var skuName = (environment == 'prod') ? 'Standard_D4s_v3' : 'Standard_B2s'
var tier = (environment == 'prod') ? 'GeneralPurpose' : 'Burstable'
var storageSizeGB = (environment == 'prod') ? 128 : 32

// =============================================================================
// PostgreSQL Flexible Server
// =============================================================================

resource postgresqlServer 'Microsoft.DBforPostgreSQL/flexibleServers@2023-03-01-preview' = {
  name: postgresqlServerName
  location: location
  tags: tags
  sku: {
    name: skuName
    tier: tier
  }
  properties: {
    administratorLogin: administratorLogin
    administratorLoginPassword: administratorPassword
    version: '17' // PostgreSQL 17
    storage: {
      storageSizeGB: storageSizeGB
      autoGrow: 'Enabled'
    }
    backup: {
      backupRetentionDays: 7
      geoRedundantBackup: (environment == 'prod') ? 'Enabled' : 'Disabled'
    }
    highAvailability: {
      mode: (environment == 'prod') ? 'ZoneRedundant' : 'Disabled'
    }
    availabilityZone: '1'
    network: {
      // Public access for MVP - use Private Endpoint for production
      publicNetworkAccess: 'Enabled'
    }
  }
}

// =============================================================================
// Firewall Rules (Allow Azure Services)
// =============================================================================

resource firewallRule 'Microsoft.DBforPostgreSQL/flexibleServers/firewallRules@2023-03-01-preview' = {
  parent: postgresqlServer
  name: 'AllowAzureServices'
  properties: {
    startIpAddress: '0.0.0.0'
    endIpAddress: '0.0.0.0' // Special Azure internal IP range
  }
}

// =============================================================================
// Database
// =============================================================================

resource database 'Microsoft.DBforPostgreSQL/flexibleServers/databases@2023-03-01-preview' = {
  parent: postgresqlServer
  name: databaseName
  properties: {
    charset: 'UTF8'
    collation: 'en_US.utf8'
  }
}

// =============================================================================
// Outputs
// =============================================================================

output fullyQualifiedDomainName string = postgresqlServer.properties.fullyQualifiedDomainName
output serverName string = postgresqlServer.name
output databaseName string = database.name
