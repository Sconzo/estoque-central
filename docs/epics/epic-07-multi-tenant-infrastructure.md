# Epic 7: Infraestrutura Multi-Tenant e Deploy

**Objetivo:** Estabelecer fundação técnica completa com multi-tenancy schema-per-tenant, autenticação Google OAuth 2.0 + JWT, e deploy funcional em Azure Container Apps. Sistema escalável e isolado pronto para receber empresas.

**FRs Cobertas:** FR24, FR25, FR26, FR27

**Additional Requirements:** ARCH1-ARCH33, UX1-UX7

**Standalone:** ✅ Sim - Infraestrutura completa e funcional
**Habilita Futuros:** ✅ Epic 8, 9, 10 dependem desta fundação

---

## Stories

- [Story 7.1: Configuração de Monorepo e Estrutura de Projeto](../stories/7-1-monorepo-project-structure.md)
- [Story 7.2: Setup PostgreSQL Multi-Tenant com Schema-per-Tenant](../stories/7-2-postgresql-schema-per-tenant.md)
- [Story 7.3: Implementação Google OAuth 2.0 + JWT Backend](../stories/7-3-google-oauth-jwt-backend.md)
- [Story 7.4: Implementação TenantContext + TenantInterceptor + Routing](../stories/7-4-tenant-context-routing.md)
- [Story 7.5: Setup Redis para Cache com Tenant Isolation](../stories/7-5-redis-cache-tenant-isolation.md)
- [Story 7.6: Setup Angular Material Design System + Frontend Base](../stories/7-6-angular-material-frontend-base.md)
- [Story 7.7: Deploy Azure Container Apps + Static Web Apps](../stories/7-7-azure-deploy-container-apps.md)
- [Story 7.8: CI/CD Pipeline GitHub Actions + Monitoring](../stories/7-8-cicd-github-actions-monitoring.md)
