# Story 9.2: Frontend - Tela de Seleção de Empresa

**Epic**: 9 - Seleção e Troca de Contexto Multi-Empresa
**Story ID**: 9.2
**Status**: completed
**Created**: 2025-12-22
**Completed**: 2025-12-24

---

## User Story

As a **User with multiple companies**,
I want **to see a list of my companies after login**,
So that **I can choose which one to access**.

---

## Acceptance Criteria

### AC1: Automatic Redirect
**Given** user with 2+ companies
**When** user completes OAuth login
**Then** `AuthGuard` redirects to `/select-company` route (FR7)

### AC2: Company List Display
**Given** select company screen
**When** `/select-company` is displayed
**Then** screen shows list of companies from `/api/users/me/companies`
**And** each company card displays: name, CNPJ, profile
**And** companies are displayed as MatCard components (UX19)

### AC3: Company Selection
**Given** company selection
**When** user clicks on a company card
**Then** PUT request is sent to `/api/users/me/context`
**And** loading spinner is shown

### AC4: Success Handling
**Given** successful selection
**When** backend returns new JWT
**Then** JWT is stored and user is redirected to `/dashboard`

### AC5: Create New Company Option
**Given** option to create new company
**When** select screen is displayed
**Then** "Criar Nova Empresa" button is visible

### AC6: Responsive Design
**Given** responsive design
**When** screen is viewed on different devices
**Then** cards adapt to Desktop, Tablet, Mobile (UX2)

---

## Definition of Done
- [x] Tela de seleção implementada
- [x] Company cards com MatCard
- [x] Selection com loading state
- [x] Create new company button
- [x] Responsive design validado

---

## Implementation Summary

### Files Created

1. **[select-company.component.html](d:\workspace\estoque-central\frontend\src\app\features\company\select-company\select-company.component.html)** - Template da tela de seleção
   - ✅ AC2: Company cards grid with responsive layout
   - ✅ AC2: MatCard components displaying company info (name, CNPJ, profile)
   - ✅ AC3: Loading spinner during selection
   - ✅ AC5: "Criar Nova Empresa" button in footer
   - ✅ AC5: Empty state with "Criar Minha Primeira Empresa" button
   - Material Design icons for visual enhancement

2. **[select-company.component.scss](d:\workspace\estoque-central\frontend\src\app\features\company\select-company\select-company.component.scss)** - Estilos da tela
   - ✅ AC6: Responsive grid layout (Desktop: multi-column, Tablet: 2-column, Mobile: 1-column)
   - ✅ UX2: Breakpoints at 1024px, 768px, and 480px
   - ✅ UX4: Deep Purple theme (#6A1B9A) throughout
   - ✅ UX19: MatCard hover effects and animations
   - ✅ UX20: Large touch targets (48x48px buttons, exceeds 44px requirement)
   - Gradient background for modern look
   - Smooth transitions and animations

3. **[select-company.component.ts](d:\workspace\estoque-central\frontend\src\app\features\company\select-company\select-company.component.ts)** - Lógica do componente
   - ✅ AC2: Loads companies from `/api/users/me/companies`
   - ✅ AC2: Formats CNPJ for display (XX.XXX.XXX/XXXX-XX)
   - ✅ AC3: Calls `switchContext()` when company is selected
   - ✅ AC3: Shows loading spinner during context switch
   - ✅ AC4: Stores new JWT token in localStorage
   - ✅ AC4: Updates tenant context via TenantService
   - ✅ AC4: Redirects to /dashboard on success
   - ✅ Error handling with user-friendly messages (403, 404, etc.)
   - Auto-selects company if user has only 1

### Files Modified

1. **[company.service.ts](d:\workspace\estoque-central\frontend\src\app\core\services\company.service.ts)** - Adicionado método switchContext
   - ✅ AC3: `switchContext(tenantId)` method
   - ✅ Sends PUT to `/api/users/me/context`
   - ✅ Returns `Observable<SwitchContextResponse>`
   - Added `SwitchContextResponse` interface

2. **[auth.guard.ts](d:\workspace\estoque-central\frontend\src\app\core\guards\auth.guard.ts)** - Já implementado na Story 8.3
   - ✅ AC1: Redirects users with 2+ companies to `/select-company` (lines 56-60)
   - ✅ Integration with Story 8.3 logic

### Acceptance Criteria Coverage

- **AC1 ✅**: Automatic Redirect
  - AuthGuard redirects users with 2+ companies to `/select-company`
  - Already implemented in Story 8.3 (auth.guard.ts:56-60)

- **AC2 ✅**: Company List Display
  - Fetches companies from `/api/users/me/companies`
  - Displays as responsive grid of MatCard components
  - Each card shows: company name, formatted CNPJ, user's profile
  - Company avatar icon with Deep Purple background
  - Hover effects and smooth transitions

- **AC3 ✅**: Company Selection
  - Clicking card triggers `selectCompany()`
  - PUT request to `/api/users/me/context` with tenantId
  - Loading spinner shown during request
  - Card gets "selecting" state with purple border
  - Button text changes to "Acessando..."

- **AC4 ✅**: Success Handling
  - New JWT stored in localStorage
  - Tenant context updated via TenantService
  - Success snackbar shown with company name
  - Redirect to `/dashboard` after 500ms delay
  - Error handling for 403, 404, and other status codes

- **AC5 ✅**: Create New Company Option
  - "Criar Nova Empresa" button in footer (always visible)
  - "Criar Minha Primeira Empresa" button in empty state
  - Both buttons navigate to `/create-company`
  - Material Design icons for visual clarity

- **AC6 ✅**: Responsive Design
  - Desktop (>1024px): Multi-column grid
  - Tablet (768px-1024px): 2-column grid
  - Mobile (<768px): 1-column grid, full-width buttons
  - Small mobile (<480px): Optimized spacing
  - Touch-friendly buttons (48px height, exceeds 44px UX20)

### UX Validation

- ✅ **UX2**: Responsive design with breakpoints for Desktop, Tablet, Mobile
- ✅ **UX4**: Deep Purple theme (#6A1B9A) applied consistently
- ✅ **UX19**: MatCard components for company display with hover effects
- ✅ **UX20**: Large touch targets (48x48px buttons)

### Integration with Other Stories

- **Story 8.3**: Reuses AuthGuard redirect logic for AC1
- **Story 8.4**: Uses `/api/users/me/companies` endpoint
- **Story 9.1**: Calls `/api/users/me/context` endpoint for context switching
- **Story 8.6**: Follows same UX patterns (loading states, snackbars, Deep Purple theme)

### User Experience Flow

1. User with 2+ companies logs in
2. AuthGuard detects multiple companies → redirect to `/select-company`
3. Loading overlay appears while fetching companies
4. Grid of company cards displayed with info
5. User clicks on desired company
6. Card shows loading state, button disabled
7. PUT request to switch context
8. Success snackbar appears
9. Redirect to `/dashboard` with new context

---

**Story criada por**: poly (PM Agent)
**Data**: 2025-12-22
**Implementada por**: Claude Sonnet 4.5
**Data de implementação**: 2025-12-24
**Baseado em**: Epic 9, PRD (FR7, UX2, UX4, UX19, UX20)
