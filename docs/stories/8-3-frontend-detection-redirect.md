# Story 8.3: Frontend - Detecção e Redirecionamento para Cadastro

**Epic**: 8 - Criação Self-Service de Empresa
**Story ID**: 8.3
**Status**: completed
**Created**: 2025-12-22
**Completed**: 2025-12-24

---

## User Story

As a **User**,
I want **to be automatically directed to company registration**,
So that **I don't get stuck after login without knowing what to do next**.

---

## Acceptance Criteria

### AC1: AuthService JWT Decoding

**Given** `AuthService` after OAuth login
**When** JWT is received from backend
**Then** service decodes JWT to extract `tenantId` and `roles`
**And** service calls `/api/users/me/companies` to get list of linked companies

### AC2: Zero Companies Redirect

**Given** user with zero companies
**When** companies list is empty
**Then** `AuthGuard` redirects to `/create-company` route (FR6)
**And** redirect happens automatically after login
**And** user sees company registration form

### AC3: One Company Auto-Select

**Given** user with 1 company
**When** companies list has exactly 1 item
**Then** `AuthGuard` auto-selects that company (FR8)
**And** `TenantService.setCurrentTenant(tenantId)` is called
**And** user is redirected to `/dashboard`
**And** no manual selection is required

### AC4: Multiple Companies Redirect

**Given** user with 2+ companies
**When** companies list has multiple items
**Then** user is redirected to `/select-company` route (FR7)
**And** selection screen is displayed (handled in Epic 9)

### AC5: Navigation Persistence

**Given** navigation persistence
**When** user refreshes page (F5)
**Then** tenant context is reloaded from local storage or session (FR11)
**And** user remains in current company context
**And** no re-authentication is required

---

## Definition of Done

- [x] AuthService decode JWT implementado
- [x] AuthGuard com lógica de redirecionamento
- [x] Zero companies → create-company
- [x] One company → auto-select + dashboard
- [x] Multiple companies → select-company
- [x] Persistence funcionando

---

## Implementation Summary

### ✅ AC1: AuthService JWT Decoding
**Status**: COMPLETE
- **Service**: CompanyService already has JWT decoding capability via AuthService
- **Method Added**: [CompanyService.getUserCompanies()](frontend/src/app/core/services/company.service.ts:47-49)
- **Endpoint**: `GET /api/users/me/companies`
- **Response**: Returns `UserCompanyResponse[]` with tenantId, nome, cnpj, profileName

### ✅ AC2: Zero Companies Redirect
**Status**: COMPLETE
- **Implementation**: [AuthGuard.canActivate()](frontend/src/app/core/guards/auth.guard.ts:45-67)
- **Logic**: `if (companies.length === 0) → redirect to /create-company`
- **Route**: `/create-company` (public route, no guard required)

### ✅ AC3: One Company Auto-Select
**Status**: COMPLETE
- **Implementation**: [AuthGuard.canActivate()](frontend/src/app/core/guards/auth.guard.ts:51-55)
- **Logic**:
  ```typescript
  if (companies.length === 1) {
    this.tenantService.setCurrentTenant(companies[0].tenantId);
    return this.router.createUrlTree(['/dashboard']);
  }
  ```
- **No Manual Selection**: User automatically redirected to dashboard

### ✅ AC4: Multiple Companies Redirect
**Status**: COMPLETE
- **Implementation**: [AuthGuard.canActivate()](frontend/src/app/core/guards/auth.guard.ts:56-60)
- **Logic**: `if (companies.length > 1) → redirect to /select-company`
- **Component Created**: [SelectCompanyComponent](frontend/src/app/features/company/select-company/select-company.component.ts)
- **Route**: `/select-company` (public route for company selection)
- **Note**: Full Epic 9 implementation will enhance this component

### ✅ AC5: Navigation Persistence
**Status**: COMPLETE
- **Implementation**: [TenantService.getCurrentTenant()](frontend/src/app/core/services/tenant.service.ts:50-52)
- **Storage**: `localStorage.getItem('current_tenant_id')`
- **Guard Check**: [AuthGuard line 38-42](frontend/src/app/core/guards/auth.guard.ts:38-42)
- **Behavior**: If tenant context exists, user bypasses company selection and accesses protected routes directly

---

## Implementation Files

### Services Modified
1. **CompanyService.ts** (MODIFIED)
   - Added method: `getUserCompanies(): Observable<UserCompanyResponse[]>`
   - Added interface: `UserCompanyResponse`
   - Location: [company.service.ts:47-49](frontend/src/app/core/services/company.service.ts:47-49)

2. **TenantService.ts** (MODIFIED)
   - Added method: `getCurrentTenant(): string | null`
   - Location: [tenant.service.ts:50-52](frontend/src/app/core/services/tenant.service.ts:50-52)

### Guards Modified
3. **AuthGuard.ts** (MODIFIED)
   - Completely rewritten with company detection logic
   - Implements AC2, AC3, AC4, AC5
   - Location: [auth.guard.ts:31-68](frontend/src/app/core/guards/auth.guard.ts:31-68)

### Components Created
4. **SelectCompanyComponent.ts** (NEW)
   - Placeholder component for multi-company selection
   - Lists companies, allows selection
   - Auto-selects if only 1 company (edge case)
   - Location: [select-company.component.ts](frontend/src/app/features/company/select-company/select-company.component.ts)

### Routes Modified
5. **app.routes.ts** (MODIFIED)
   - Added route: `/select-company` → SelectCompanyComponent
   - Location: [app.routes.ts:18-21](frontend/src/app/app.routes.ts:18-21)

---

## Technical Details

### Authentication Flow (After Login)
```
User logs in via OAuth
    ↓
JWT token stored in localStorage
    ↓
User navigates to protected route
    ↓
AuthGuard.canActivate() triggered
    ↓
Check: Is user authenticated? (JWT valid)
    NO → Redirect to /login
    YES → Continue
    ↓
Check: Tenant context exists? (getCurrentTenant())
    YES → Allow access (AC5 - Persistence)
    NO → Fetch companies
    ↓
CompanyService.getUserCompanies() → GET /api/users/me/companies
    ↓
Response: UserCompanyResponse[]
    ↓
Switch based on companies.length:
    - 0 companies → Redirect to /create-company (AC2)
    - 1 company → Auto-select + Redirect to /dashboard (AC3)
    - 2+ companies → Redirect to /select-company (AC4)
```

### Persistence Flow (Page Refresh)
```
User refreshes page (F5)
    ↓
AuthGuard.canActivate() triggered
    ↓
Check: Tenant context exists?
    getCurrentTenant() → localStorage.getItem('current_tenant_id')
    ↓
    YES → return true (allow access, no API call needed)
    NO → Fetch companies and redirect (as above)
```

---

## Tests

### Unit Tests - AuthGuard
**File**: `auth.guard.spec.ts` (CREATED)
**Location**: `frontend/src/app/core/guards/auth.guard.spec.ts`
**Tests Included**:
- ✅ Should redirect to /login if not authenticated
- ✅ AC5: Should allow access if tenant context exists (persistence)
- ✅ AC2: Should redirect to /create-company if zero companies
- ✅ AC3: Should auto-select and redirect to /dashboard if one company
- ✅ AC4: Should redirect to /select-company if multiple companies
- ✅ Should handle API errors gracefully (fallback to /create-company)

---

**Story criada por**: poly (PM Agent)
**Data**: 2025-12-22
**Implementado por**: Amelia (Dev Agent)
**Completion**: 2025-12-24
**Baseado em**: Epic 8, PRD (FR6, FR7, FR8, FR11)
