// =============================================================================
// Azure Static Web Apps - Frontend Deployment
// Story 7.7: Static Web Apps (AC2)
// =============================================================================
// Deploys Angular application via CDN with:
// - HTTPS enforcement
// - Custom domain support
// - Auto-deploy from GitHub (via GitHub Actions)
// =============================================================================

param location string
param environment string
param uniqueSuffix string
param tags object
param backendUrl string

// =============================================================================
// Variables
// =============================================================================

var staticWebAppName = 'swa-${uniqueSuffix}-${environment}'

// SKU selection based on environment
var sku = (environment == 'prod') ? 'Standard' : 'Free'

// =============================================================================
// Static Web App
// =============================================================================

resource staticWebApp 'Microsoft.Web/staticSites@2023-01-01' = {
  name: staticWebAppName
  location: location
  tags: tags
  sku: {
    name: sku
    tier: sku
  }
  properties: {
    repositoryUrl: 'https://github.com/your-org/estoque-central' // Update with actual repo
    branch: (environment == 'prod') ? 'main' : 'develop'
    buildProperties: {
      appLocation: '/frontend'
      apiLocation: '' // No Azure Functions API
      outputLocation: 'dist/frontend/browser'
      appBuildCommand: 'npm run build -- --configuration=${environment}'
    }
    stagingEnvironmentPolicy: 'Enabled'
    allowConfigFileUpdates: true
    provider: 'GitHub'
    enterpriseGradeCdnStatus: 'Disabled' // Enable for prod if needed
  }
}

// =============================================================================
// Application Settings (Environment Variables for Angular)
// =============================================================================

resource staticWebAppConfig 'Microsoft.Web/staticSites/config@2023-01-01' = {
  parent: staticWebApp
  name: 'appsettings'
  properties: {
    API_URL: backendUrl
    ENVIRONMENT: environment
  }
}

// =============================================================================
// Custom Domain (Optional - configure after deployment)
// =============================================================================

// resource customDomain 'Microsoft.Web/staticSites/customDomains@2023-01-01' = if (environment == 'prod') {
//   parent: staticWebApp
//   name: 'app.estoquecentral.com.br'
//   properties: {}
// }

// =============================================================================
// Outputs
// =============================================================================

output frontendUrl string = 'https://${staticWebApp.properties.defaultHostname}'
output staticWebAppName string = staticWebApp.name
output deploymentToken string = staticWebApp.listSecrets().properties.apiKey
