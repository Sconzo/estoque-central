// =============================================================================
// Azure Container Apps - Backend Deployment
// Story 7.7: Azure Container Apps (AC1)
// =============================================================================
// Deploys:
// - Container Apps Environment (shared)
// - Container App (Spring Boot backend)
// - Auto-scaling rules
// - Health probes
// =============================================================================

param location string
param environment string
param uniqueSuffix string
param tags object

// Database connection
param databaseHost string
param databaseName string
param databaseUser string

// Redis connection
param redisHost string
param redisPort int
param redisSslEnabled bool

// OAuth
param googleOAuthClientId string

// Key Vault reference
param keyVaultName string

// =============================================================================
// Variables
// =============================================================================

var containerAppEnvName = 'cae-${uniqueSuffix}-${environment}'
var containerAppName = 'ca-${uniqueSuffix}-backend-${environment}'
var logAnalyticsName = 'log-${uniqueSuffix}-${environment}'

var containerImage = 'estoquecentral.azurecr.io/estoque-central:latest' // Will be updated by CI/CD
var containerPort = 8080

// Auto-scaling configuration (AC1)
var minReplicas = (environment == 'prod') ? 2 : 1  // Prod: min 2, Dev: min 1
var maxReplicas = (environment == 'prod') ? 10 : 3 // Prod: max 10, Dev: max 3

// =============================================================================
// Log Analytics Workspace (required for Container Apps)
// =============================================================================

resource logAnalytics 'Microsoft.OperationalInsights/workspaces@2023-09-01' = {
  name: logAnalyticsName
  location: location
  tags: tags
  properties: {
    sku: {
      name: 'PerGB2018'
    }
    retentionInDays: 30
  }
}

// =============================================================================
// Container Apps Environment
// =============================================================================

resource containerAppEnvironment 'Microsoft.App/managedEnvironments@2023-05-01' = {
  name: containerAppEnvName
  location: location
  tags: tags
  properties: {
    appLogsConfiguration: {
      destination: 'log-analytics'
      logAnalyticsConfiguration: {
        customerId: logAnalytics.properties.customerId
        sharedKey: logAnalytics.listKeys().primarySharedKey
      }
    }
    zoneRedundant: (environment == 'prod') // Zone redundancy only for production
  }
}

// =============================================================================
// Container App (Spring Boot Backend)
// =============================================================================

resource containerApp 'Microsoft.App/containerApps@2023-05-01' = {
  name: containerAppName
  location: location
  tags: tags
  properties: {
    managedEnvironmentId: containerAppEnvironment.id
    configuration: {
      ingress: {
        external: true
        targetPort: containerPort
        transport: 'http' // Changed to 'auto' for HTTPS
        allowInsecure: false
        traffic: [
          {
            latestRevision: true
            weight: 100
          }
        ]
        cors: {
          allowedOrigins: [
            'https://${uniqueSuffix}-${environment}.azurestaticapps.net' // Frontend URL
            'http://localhost:4200' // Local development
          ]
          allowedMethods: [
            'GET'
            'POST'
            'PUT'
            'DELETE'
            'PATCH'
            'OPTIONS'
          ]
          allowedHeaders: [
            '*'
          ]
          exposeHeaders: [
            'Content-Length'
            'Content-Type'
          ]
          maxAge: 3600
          allowCredentials: false
        }
      }
      secrets: [
        {
          name: 'database-password'
          keyVaultUrl: 'https://${keyVaultName}.vault.azure.net/secrets/database-password'
          identity: 'system'
        }
        {
          name: 'jwt-secret'
          keyVaultUrl: 'https://${keyVaultName}.vault.azure.net/secrets/jwt-secret'
          identity: 'system'
        }
        {
          name: 'google-oauth-client-secret'
          keyVaultUrl: 'https://${keyVaultName}.vault.azure.net/secrets/google-oauth-client-secret'
          identity: 'system'
        }
      ]
      activeRevisionsMode: 'Single' // Deployment mode
    }
    template: {
      containers: [
        {
          name: 'backend'
          image: containerImage
          resources: {
            cpu: json('1.0')        // 1 vCPU (AC1)
            memory: '2Gi'            // 2Gi memory (AC1)
          }
          env: [
            // Spring Profile
            {
              name: 'SPRING_PROFILES_ACTIVE'
              value: environment
            }
            // Database configuration
            {
              name: 'DATABASE_URL'
              value: 'jdbc:postgresql://${databaseHost}:5432/${databaseName}?sslmode=require'
            }
            {
              name: 'DATABASE_USER'
              value: databaseUser
            }
            {
              name: 'DATABASE_PASSWORD'
              secretRef: 'database-password'
            }
            // Redis configuration (TLS enabled)
            {
              name: 'REDIS_ADDRESS'
              value: 'rediss://${redisHost}:${redisPort}'
            }
            {
              name: 'REDIS_SSL_ENABLED'
              value: string(redisSslEnabled)
            }
            // JWT configuration
            {
              name: 'JWT_SECRET'
              secretRef: 'jwt-secret'
            }
            // Google OAuth
            {
              name: 'GOOGLE_OAUTH_CLIENT_ID'
              value: googleOAuthClientId
            }
            {
              name: 'GOOGLE_OAUTH_CLIENT_SECRET'
              secretRef: 'google-oauth-client-secret'
            }
            // JVM options for container
            {
              name: 'JAVA_OPTS'
              value: '-XX:+UseContainerSupport -XX:MaxRAMPercentage=75.0 -XX:+UseG1GC'
            }
          ]
          probes: [
            // Liveness probe (AC1)
            {
              type: 'Liveness'
              httpGet: {
                path: '/actuator/health'
                port: containerPort
                scheme: 'HTTP'
              }
              initialDelaySeconds: 40
              periodSeconds: 30
              timeoutSeconds: 3
              failureThreshold: 3
              successThreshold: 1
            }
            // Readiness probe (AC1)
            {
              type: 'Readiness'
              httpGet: {
                path: '/actuator/health/readiness'
                port: containerPort
                scheme: 'HTTP'
              }
              initialDelaySeconds: 10
              periodSeconds: 10
              timeoutSeconds: 3
              failureThreshold: 3
              successThreshold: 1
            }
            // Startup probe
            {
              type: 'Startup'
              httpGet: {
                path: '/actuator/health'
                port: containerPort
                scheme: 'HTTP'
              }
              initialDelaySeconds: 0
              periodSeconds: 5
              timeoutSeconds: 3
              failureThreshold: 30 // 150 seconds total startup time
              successThreshold: 1
            }
          ]
        }
      ]
      scale: {
        minReplicas: minReplicas
        maxReplicas: maxReplicas
        rules: [
          // HTTP scaling rule (AC1)
          {
            name: 'http-scaling-rule'
            http: {
              metadata: {
                concurrentRequests: '50' // Scale when > 50 concurrent requests
              }
            }
          }
          // CPU scaling rule (AC1)
          {
            name: 'cpu-scaling-rule'
            custom: {
              type: 'cpu'
              metadata: {
                type: 'Utilization'
                value: '70' // Scale when CPU > 70%
              }
            }
          }
        ]
      }
    }
  }
  identity: {
    type: 'SystemAssigned' // For Key Vault access
  }
}

// =============================================================================
// Outputs
// =============================================================================

output backendUrl string = 'https://${containerApp.properties.configuration.ingress.fqdn}'
output containerAppName string = containerApp.name
output containerAppEnvironmentName string = containerAppEnvironment.name
