# Story 7.3: Implementação Google OAuth 2.0 + JWT Backend

**Epic**: 7 - Infraestrutura Multi-Tenant e Deploy
**Story ID**: 7.3
**Status**: pending
**Created**: 2025-12-22

---

## User Story

As a **User**,
I want **to authenticate using my Google account**,
So that **I can securely access the system without creating a new password**.

---

## Acceptance Criteria

### AC1: Spring Security OAuth2 Configuration

**Given** Spring Security OAuth2 Client configuration
**When** application starts
**Then** OAuth2 login is enabled for Google provider
**And** redirect URI is configured: `/login/oauth2/code/google`
**And** client ID and secret are loaded from environment variables

### AC2: OAuth Flow e JWT Generation

**Given** a user clicks "Login com Google"
**When** OAuth2 flow completes successfully
**Then** user is redirected to frontend with JWT token
**And** JWT payload contains: `sub` (email), `tenantId`, `roles[]`, `iat`, `exp`
**And** JWT is signed with HS256 algorithm
**And** JWT secret is loaded from Azure Key Vault (ARCH9)

### AC3: User Registration on First Login

**Given** a new Google OAuth user
**When** first login occurs
**Then** a record is created/updated in `public.users` table
**And** `google_id` is stored for future logins
**And** `ultimo_login` timestamp is updated

### AC4: JWT Token Validation

**Given** JWT token validation
**When** protected endpoints are accessed
**Then** Spring Security validates JWT signature
**And** expiration time (`exp`) is checked
**And** invalid/expired tokens return 401 Unauthorized

---

## Definition of Done

- [ ] OAuth2 configurado e funcionando
- [ ] JWT generation implementado
- [ ] User creation/update no primeiro login
- [ ] Token validation funcionando
- [ ] Testes de integração passando

---

**Story criada por**: poly (PM Agent)
**Data**: 2025-12-22
**Baseado em**: Epic 7, PRD (FR25, ARCH9)
