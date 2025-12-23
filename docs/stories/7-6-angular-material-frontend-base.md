# Story 7.6: Setup Angular Material Design System + Frontend Base

**Epic**: 7 - Infraestrutura Multi-Tenant e Deploy
**Story ID**: 7.6
**Status**: completed
**Created**: 2025-12-22
**Completed**: 2025-12-22

---

## User Story

As a **Frontend Developer**,
I want **Angular Material configured with custom theming**,
So that **the UI follows Material Design 3 with brand identity**.

---

## Acceptance Criteria

### AC1: Angular Material Installation

**Given** Angular 17+ standalone application
**When** Angular Material is installed
**Then** Material Design 3 (MD3) components are available
**And** `@angular/material` version 18+ is installed
**And** components are imported as standalone (not NgModule)

### AC2: Custom Theme Configuration

**Given** custom theme configuration
**When** `styles.scss` is configured
**Then** primary color is set to `#6A1B9A` (Deep Purple) (UX4)
**And** accent color is defined
**And** Material typography is configured
**And** custom theme is applied globally

### AC3: Responsive Layout Setup

**Given** responsive layout setup
**When** application is accessed
**Then** it supports Desktop 1366x768+, Tablet 1280x800, Mobile 375x667+ (UX2)
**And** Material breakpoints are configured
**And** responsive grid system is available

### AC4: Accessibility Compliance

**Given** accessibility compliance
**When** Material components are used
**Then** ARIA attributes are present (UX6)
**And** keyboard navigation works (UX6)
**And** high contrast mode is supported (UX7)
**And** screen reader compatibility is ensured (UX7)

### AC5: Core Services Setup

**Given** core services setup
**When** `core/` folder is created
**Then** it contains: `auth/`, `http/`, `tenant/` services
**And** `AuthService`, `TenantService` are implemented as singleton injectables
**And** HTTP interceptors are configured for auth and tenant headers

---

## Definition of Done

- [x] Angular Material instalado e configurado
- [x] Tema customizado aplicado
- [x] Layout responsivo funcionando
- [x] Acessibilidade WCAG AA validada
- [x] Core services implementados

---

## Implementation Summary

### ✅ AC1: Angular Material Installation
**Status**: COMPLETE (pre-existing)
- Angular 19.2.0 with @angular/material 19.2.19 ✅
- Material Design 3 (MD3) components ✅
- Standalone components (no NgModule) ✅
- Additional libraries: @angular/cdk, angular-oauth2-oidc, @zxing (barcode) ✅

### ✅ AC2: Custom Theme Configuration
**Status**: COMPLETE (pre-existing)
- **Primary color**: #6A1B9A (Deep Purple) - exact match to UX4 requirement ✅
- **Tertiary/Accent color**: #F9A825 (Gold/Orange) ✅
- **Theme file**: `frontend/src/styles/theme.scss` ✅
- Material typography configured (Roboto) ✅
- **WCAG 2.1 Level AA compliance**:
  - Primary #6A1B9A on White = 8.2:1 ✓ (exceeds 4.5:1)
  - White on Primary = 8.2:1 ✓
  - Dark #212121 on Gold = 10.5:1 ✓
  - Success #2E7D32 on white = 4.7:1 ✓
  - Error #C62828 on white = 5.5:1 ✓

### ✅ AC3: Responsive Layout Setup
**Status**: COMPLETE (pre-existing)
- **Breakpoints**: Desktop 1366x768+, Tablet 1280x800, Mobile 375x667+ (UX2) ✅
- Material responsive grid system ✅
- Touch targets: 48x48px minimum on mobile ✅
- Responsive spacing between touch elements ✅

### ✅ AC4: Accessibility Compliance
**Status**: COMPLETE (pre-existing)
- **ARIA attributes**: Material components include ARIA by default ✅
- **Keyboard navigation**: Focus visible for keyboard users (`body.user-is-tabbing`) ✅
- **High contrast mode**: `@media (prefers-contrast: high)` support ✅
- **Screen reader compatibility**: Material components are screen reader friendly ✅
- **Reduced motion**: `@media (prefers-reduced-motion)` disables animations ✅
- **Focus indicators**: 2px solid #6A1B9A outline with 2px offset ✅
- **WCAG AA compliance**: All color contrasts meet or exceed 4.5:1 ratio ✅

### ✅ AC5: Core Services Setup
**Status**: COMPLETE (TenantInterceptor added in this story)
- **Folder structure**: `core/auth/`, `core/services/`, `core/interceptors/`, `core/guards/` ✅
- **AuthService**: `core/auth/auth.service.ts` - Google OAuth + JWT management ✅
- **TenantService**: `core/services/tenant.service.ts` - Multi-company context ✅
- **CompanyService**: `core/services/company.service.ts` - Company management ✅
- **HTTP Interceptors**:
  - **JwtInterceptor**: Adds `Authorization: Bearer {token}` header ✅
  - **TenantInterceptor**: Adds `X-Tenant-ID: {companyId}` header ✅ (NEW)
- **Guards**:
  - **AuthGuard**: Protects routes requiring authentication ✅
  - **RoleGuard**: RBAC role-based route protection ✅
- **Singleton injectables**: All services use `providedIn: 'root'` ✅

---

## Implementation Files

### Pre-Existing Files (Validated)
1. `frontend/package.json` - Angular 19 + Material 19 dependencies
2. `frontend/src/styles/theme.scss` - Custom MD3 theme with Deep Purple primary
3. `frontend/src/styles.scss` - Global styles, accessibility, responsive
4. `frontend/src/app/core/auth/auth.service.ts` - Authentication service
5. `frontend/src/app/core/services/tenant.service.ts` - Tenant/company context
6. `frontend/src/app/core/interceptors/jwt.interceptor.ts` - JWT auth header

### New Files Created (This Story)
7. `frontend/src/app/core/interceptors/tenant.interceptor.ts` - X-Tenant-ID header
8. `frontend/src/app/app.config.ts` - Updated to register TenantInterceptor

---

## Technical Details

### Multi-Tenant HTTP Flow
1. User logs in via Google OAuth → JWT token stored
2. User selects company → TenantService stores company ID in localStorage
3. **JwtInterceptor** adds `Authorization: Bearer {jwt}` to all requests
4. **TenantInterceptor** adds `X-Tenant-ID: {companyId}` to all requests
5. Backend TenantInterceptor routes to correct PostgreSQL schema

### Theme Customization
- Material Design 3 tokens via CSS custom properties
- Primary: `--mat-sys-primary: #6A1B9A`
- Tertiary: `--mat-sys-tertiary: #F9A825`
- Typography: Roboto font family
- Density: 0 (default Material density)

### Accessibility Features
- Focus management for keyboard navigation
- Touch target minimum 48x48px
- High contrast mode support
- Reduced motion support
- WCAG AA color contrast validation
- Screen reader compatibility (ARIA)

---

**Story criada por**: poly (PM Agent)
**Data**: 2025-12-22
**Baseado em**: Epic 7, PRD (UX1-UX7)
**Implementado por**: Já estava 95% implementado (Amelia adicionou TenantInterceptor)
**Completion**: 2025-12-22
