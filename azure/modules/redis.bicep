// =============================================================================
// Azure Cache for Redis
// Story 7.5: Redis Cache with Tenant Isolation
// Story 7.7: Redis Infrastructure
// =============================================================================

param location string
param environment string
param uniqueSuffix string
param tags object

// =============================================================================
// Variables
// =============================================================================

var redisName = 'redis-${uniqueSuffix}-${environment}'

// SKU selection based on environment
var sku = (environment == 'prod') ? 'Standard' : 'Basic'
var capacity = (environment == 'prod') ? 1 : 0  // Prod: C1 (1GB), Dev: C0 (250MB)

// =============================================================================
// Azure Cache for Redis
// =============================================================================

resource redis 'Microsoft.Cache/redis@2023-08-01' = {
  name: redisName
  location: location
  tags: tags
  properties: {
    sku: {
      name: sku
      family: 'C'
      capacity: capacity
    }
    enableNonSslPort: false // Enforce TLS (Story 7.5 AC1)
    minimumTlsVersion: '1.2' // TLS 1.2+ required
    publicNetworkAccess: 'Enabled' // Use Private Endpoint for production
    redisConfiguration: {
      'maxmemory-policy': 'allkeys-lru' // LRU eviction policy (Story 7.5 AC4)
      'maxmemory-reserved': '50'
      'maxfragmentationmemory-reserved': '50'
    }
    redisVersion: '6' // Redis 6.x
  }
}

// =============================================================================
// Outputs
// =============================================================================

output hostName string = redis.properties.hostName
output sslPort int = redis.properties.sslPort
output redisName string = redis.name
output primaryKey string = redis.listKeys().primaryKey
