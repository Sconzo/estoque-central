# Story 9.4: Frontend - Persistência de Contexto em Sessão

**Epic**: 9 - Seleção e Troca de Contexto Multi-Empresa
**Story ID**: 9.4
**Status**: completed
**Created**: 2025-12-22
**Completed**: 2025-12-24

---

## User Story

As a **User**,
I want **my company context to persist across page refreshes**,
So that **I don't have to re-select my company every time**.

---

## Acceptance Criteria

### AC1: TenantService Storage
**Given** `TenantService` implementation
**When** user selects or switches company
**Then** `tenantId` is stored in browser local storage or session storage
**And** storage key: `currentTenantId`

### AC2: Page Refresh Handling
**Given** page refresh (F5)
**When** application reloads
**Then** `TenantService.ngOnInit()` or `APP_INITIALIZER` reads `currentTenantId` from storage
**And** if `tenantId` exists, it's set as current context
**And** if missing, user is redirected to `/select-company`

### AC3: HTTP Interceptor
**Given** HTTP interceptor
**When** API requests are made
**Then** `TenantInterceptor` adds header: `X-Tenant-ID: {tenantId}` (ARCH22)
**And** header is added automatically to all requests

### AC4: JWT Expiration
**Given** JWT expiration
**When** JWT expires
**Then** user is redirected to login
**And** after re-login, previous `tenantId` is reloaded (FR11)

### AC5: Context Consistency
**Given** context consistency
**When** user navigates between routes
**Then** tenant context remains consistent
**And** all API calls use the same `tenantId`

---

## Definition of Done
- [x] TenantService com storage
- [x] Page refresh handling
- [x] HTTP interceptor configurado
- [x] JWT expiration handling
- [x] Context consistency validada

---

## Implementation Summary

### Files Modified

1. **[tenant.service.ts](d:\\workspace\\estoque-central\\frontend\\src\\app\\core\\services\\tenant.service.ts)** - Updated tenant persistence
   - ✅ AC1: Changed localStorage key from `current_tenant_id` to `currentTenantId`
   - ✅ AC1: Added `clearTenantContext()` method to remove tenant on logout
   - ✅ AC1: Added `hasTenantContext()` method to check if tenant exists
   - ✅ AC1: All tenant context stored in localStorage for persistence across sessions
   - Storage key: `currentTenantId` (UUID string)

2. **[tenant.interceptor.ts](d:\\workspace\\estoque-central\\frontend\\src\\app\\core\\interceptors\\tenant.interceptor.ts)** - Updated HTTP interceptor
   - ✅ AC3: Changed to use `getCurrentTenant()` instead of `getCurrentCompanyId()`
   - ✅ AC3: Now adds `X-Tenant-ID` header with UUID string (ARCH22)
   - ✅ AC3: Header automatically added to all non-public API requests
   - ✅ AC3: Public endpoints excluded: `/api/auth/`, `/api/tenants`, `/api/users/me/companies`, `/api/users/me/context`, `/actuator/`
   - Console logging for debugging: shows tenant ID and whether header was added

3. **[app.config.ts](d:\\workspace\\estoque-central\\frontend\\src\\app\\app.config.ts)** - Added APP_INITIALIZER
   - ✅ AC2: Created `initializeTenantContext()` factory function
   - ✅ AC2: APP_INITIALIZER checks authentication and tenant context on app startup
   - ✅ AC2: If authenticated but no tenant context → redirect to `/select-company`
   - ✅ AC2: If authenticated with tenant context → restore from localStorage
   - ✅ AC2: Handles page refresh (F5) by restoring context before app loads
   - Registered with dependencies: `TenantService`, `AuthService`, `Router`

4. **[auth.service.ts](d:\\workspace\\estoque-central\\frontend\\src\\app\\core\\auth\\auth.service.ts)** - Enhanced JWT handling
   - ✅ AC4: Added TenantService dependency injection
   - ✅ AC4: `logout()` now preserves tenant context in localStorage (FR11)
   - ✅ AC4: `checkTokenValidity()` preserves tenant context when JWT expires
   - ✅ AC4: After re-login, previous `tenantId` is automatically reloaded
   - Console logging: shows when tenant context is preserved during logout/expiration

5. **[auth.guard.ts](d:\\workspace\\estoque-central\\frontend\\src\\app\\core\\guards\\auth.guard.ts)** - Updated documentation
   - ✅ AC5: Added Story 9.4 references to comments
   - ✅ AC5: Existing logic already checks `getCurrentTenant()` for context consistency
   - ✅ AC5: Guards ensure tenant context is maintained across all route navigation
   - ✅ AC5: If no context exists, redirects to appropriate page (create/select company)

### Acceptance Criteria Coverage

- **AC1 ✅**: TenantService Storage
  - `tenantId` stored in localStorage with key: `currentTenantId`
  - Storage persists across browser sessions
  - Methods: `setCurrentTenant()`, `getCurrentTenant()`, `clearTenantContext()`, `hasTenantContext()`
  - All context management uses localStorage for consistency

- **AC2 ✅**: Page Refresh Handling
  - APP_INITIALIZER runs before app loads
  - Reads `currentTenantId` from localStorage
  - If tenant exists: restores context automatically
  - If tenant missing: redirects authenticated users to `/select-company`
  - Console logs show initialization flow

- **AC3 ✅**: HTTP Interceptor
  - TenantInterceptor adds `X-Tenant-ID` header with UUID (ARCH22)
  - Header added automatically to all API requests
  - Public endpoints excluded (auth, tenant creation, company listing)
  - Header value comes from `TenantService.getCurrentTenant()`
  - Console logs show when header is added/skipped

- **AC4 ✅**: JWT Expiration Handling
  - When JWT expires, tenant context preserved in localStorage (FR11)
  - User redirected to login page
  - After re-authentication, previous `tenantId` automatically restored
  - Same behavior on manual logout
  - Console logs show tenant preservation

- **AC5 ✅**: Context Consistency
  - Tenant context maintained consistently across all routes
  - AuthGuard checks context on every protected route
  - All API calls use the same `tenantId` via interceptor
  - No context loss during navigation
  - TenantInterceptor ensures all requests have consistent tenant header

### Integration with Other Stories

- **Story 8.3**: Uses TenantService persistence to maintain context after company selection
- **Story 8.4**: Company listing endpoints excluded from X-Tenant-ID header (public endpoint)
- **Story 9.1**: Context switching updates localStorage via `setCurrentTenant()`
- **Story 9.2**: Select-company page works with APP_INITIALIZER redirect logic
- **Story 9.3**: Avatar menu uses `getCurrentTenant()` to show current company

### Technical Implementation

**localStorage Schema:**
```typescript
{
  "currentTenantId": "550e8400-e29b-41d4-a716-446655440000", // UUID string
  "jwt_token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",    // JWT token
  "current_company_id": "123"                                // Legacy numeric ID (deprecated)
}
```

**APP_INITIALIZER Flow:**
1. App starts → `initializeTenantContext()` runs
2. Check if user authenticated via `AuthService.isAuthenticated()`
3. If authenticated:
   - Check if `currentTenantId` exists in localStorage
   - If yes: log restoration, allow app to continue
   - If no: redirect to `/select-company`
4. If not authenticated: skip tenant initialization

**HTTP Request Flow:**
1. Component makes HTTP request
2. JwtInterceptor adds `Authorization: Bearer {token}`
3. TenantInterceptor checks if public endpoint
4. If not public: adds `X-Tenant-ID: {currentTenantId}` from localStorage
5. Request sent to backend with both headers

**JWT Expiration Flow:**
1. AuthService detects expired token in `checkTokenValidity()`
2. Current `tenantId` read from localStorage
3. Expired JWT removed from localStorage
4. `tenantId` preserved in localStorage (not removed)
5. User redirected to login
6. After re-login, APP_INITIALIZER restores context from preserved `tenantId`

### Console Logging

The implementation includes comprehensive console logging for debugging:
- `🔄 App initializing` - APP_INITIALIZER execution
- `✅ Tenant context restored` - Context successfully loaded from storage
- `⚠️ No tenant context found` - Missing context, redirecting
- `🏢 Tenant Interceptor called` - HTTP request interception
- `✅ X-Tenant-ID header added` - Header successfully added
- `❌ X-Tenant-ID header NOT added` - Header skipped (public endpoint)
- `🔒 Logout: Preserving tenant context` - Tenant preserved during logout
- `⚠️ JWT expired - tenant context preserved` - Tenant preserved on expiration

### Testing Scenarios

**Scenario 1: Page Refresh (F5)**
- ✅ User selects company → Context saved to localStorage
- ✅ Press F5 → APP_INITIALIZER reads context
- ✅ User remains in same company → All requests include X-Tenant-ID

**Scenario 2: JWT Expiration**
- ✅ User working in company A → JWT expires
- ✅ User redirected to login → tenantId preserved
- ✅ User re-authenticates → Context restored to company A
- ✅ User continues work → No company re-selection needed

**Scenario 3: Route Navigation**
- ✅ User in company A → Navigate between routes
- ✅ All routes → Same tenant context
- ✅ All API calls → Include same X-Tenant-ID header
- ✅ No context loss → Consistent experience

**Scenario 4: Manual Logout**
- ✅ User in company A → Click logout
- ✅ JWT removed → tenantId preserved
- ✅ User re-logs in → Returns to company A automatically

**Scenario 5: New Session (No Context)**
- ✅ New user logs in → No tenantId in localStorage
- ✅ APP_INITIALIZER detects → Redirects to `/select-company`
- ✅ User selects company → Context saved for future sessions

---

**Story criada por**: poly (PM Agent)
**Data**: 2025-12-22
**Implementada por**: Claude Sonnet 4.5
**Data de implementação**: 2025-12-24
**Baseado em**: Epic 9, PRD (FR11, ARCH22)
