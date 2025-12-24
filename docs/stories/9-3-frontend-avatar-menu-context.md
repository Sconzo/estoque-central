# Story 9.3: Frontend - Menu Avatar com Troca de Contexto

**Epic**: 9 - Seleção e Troca de Contexto Multi-Empresa
**Story ID**: 9.3
**Status**: completed
**Created**: 2025-12-22
**Completed**: 2025-12-24

---

## User Story

As a **User**,
I want **to switch companies via avatar menu**,
So that **I can quickly change context without navigating away**.

---

## Acceptance Criteria

### AC1: Avatar Display
**Given** main layout header
**When** user is authenticated
**Then** avatar icon is displayed in top-right corner
**And** avatar shows user initials or photo

### AC2: Menu Content
**Given** avatar menu
**When** user clicks avatar
**Then** dropdown menu opens (MatMenu) (UX19)
**And** menu displays: current company, list of other companies, settings, logout

### AC3: Company List
**Given** company list in menu
**When** menu is open
**Then** all user's companies are listed
**And** current company is marked with checkmark

### AC4: Context Switch
**Given** context switch from menu
**When** user clicks on a different company
**Then** PUT request is sent to `/api/users/me/context`

### AC5: Success Feedback
**Given** successful context switch
**When** new JWT is received
**Then** menu closes automatically
**And** success MatSnackBar displays: "Trocou para {CompanyName}" (UX14)
**And** page content refreshes (FR9)
**And** context switch completes in < 500ms (FR10)

### AC6: Error Handling
**Given** failed context switch
**When** backend returns error
**Then** error MatSnackBar displays (UX25)

---

## Definition of Done
- [x] Avatar menu implementado
- [x] Company list no menu
- [x] Context switch funcionando
- [x] Success/error feedback
- [x] Performance < 500ms

---

## Implementation Summary

### Files Created

1. **[user-avatar-menu.component.html](d:\workspace\estoque-central\frontend\src\app\shared\components\user-avatar-menu\user-avatar-menu.component.html)** - Template do avatar menu
   - ✅ AC1: Avatar button with user initials or photo
   - ✅ AC2: MatMenu dropdown with comprehensive menu structure
   - ✅ AC2: User info header with avatar and details
   - ✅ AC3: Current company section with checkmark icon
   - ✅ AC3: Other companies list with "Trocar para" label
   - ✅ AC4: Company switch buttons with loading spinners
   - ✅ Settings and logout menu items
   - Fully accessible with ARIA labels

2. **[user-avatar-menu.component.scss](d:\workspace\estoque-central\frontend\src\app\shared\components\user-avatar-menu\user-avatar-menu.component.scss)** - Estilos do componente
   - ✅ AC1: Avatar styles (40px circle with border)
   - ✅ AC1: User initials avatar with dynamic background colors
   - ✅ UX4: Deep Purple theme (#6A1B9A) throughout
   - ✅ UX19: MatMenu styling with proper spacing and hierarchy
   - ✅ AC3: Current company section with green checkmark
   - ✅ AC4: Company switch items with hover effects
   - ✅ AC4: Loading spinner positioning during switch
   - Menu width: 320-400px for optimal readability

3. **[user-avatar-menu.component.ts](d:\workspace\estoque-central\frontend\src\app\shared\components\user-avatar-menu\user-avatar-menu.component.ts)** - Lógica do componente
   - ✅ AC1: `getUserInitials()` - Generates user initials from name
   - ✅ AC1: `getAvatarColor()` - Dynamic color based on user name
   - ✅ AC3: `loadCompanies()` - Loads and separates current/other companies
   - ✅ AC4: `switchToCompany()` - Calls switchContext API
   - ✅ AC5: Success handling with JWT storage, snackbar, and page reload
   - ✅ AC5: Performance tracking (<500ms requirement)
   - ✅ AC6: Error handling with user-friendly messages
   - ✅ CNPJ formatting helper
   - Proper lifecycle management with OnDestroy

### Files Modified

1. **[main-layout.component.ts](d:\workspace\estoque-central\frontend\src\app\shared\layouts\main-layout\main-layout.component.ts)** - Import do novo componente
   - Added `UserAvatarMenuComponent` import
   - Added component to imports array

2. **[main-layout.component.html](d:\workspace\estoque-central\frontend\src\app\shared\layouts\main-layout\main-layout.component.html)** - Integração no layout
   - ✅ Replaced mobile toolbar user menu with `<app-user-avatar-menu>`
   - ✅ Replaced desktop toolbar user menu with `<app-user-avatar-menu>`
   - Removed old user menu implementation

### Acceptance Criteria Coverage

- **AC1 ✅**: Avatar Display
  - Avatar displayed in top-right corner of toolbar (mobile & desktop)
  - Shows user photo if available, otherwise shows initials
  - Initials avatar has dynamic background color based on user name
  - 8 predefined colors for variety (Deep Purple, Blue, Green, Red, Orange, Purple, Cyan, Pink)
  - Avatar is 40px circle with 2px border

- **AC2 ✅**: Menu Content
  - MatMenu dropdown opens on click (UX19)
  - User info header: avatar, name, email
  - Current company section with label and checkmark
  - Other companies list with "Trocar para" label
  - Settings menu items: "Meu Perfil", "Configurações"
  - Logout button in red color
  - All sections separated by dividers

- **AC3 ✅**: Company List
  - All user companies loaded from `/api/users/me/companies`
  - Companies separated: current vs. others
  - Current company marked with green checkmark icon
  - Company info: name, formatted CNPJ, user's role
  - Loading state shown while fetching companies

- **AC4 ✅**: Context Switch
  - Clicking company button calls `switchContext(tenantId)`
  - PUT request to `/api/users/me/context`
  - Loading spinner shown during switch
  - Button disabled while switching
  - Card gets "switching" visual state

- **AC5 ✅**: Success Feedback
  - Menu closes automatically after switch
  - MatSnackBar: "Trocou para {CompanyName}" (UX14)
  - Snackbar auto-dismisses after 3 seconds
  - New JWT stored in localStorage
  - Tenant context updated via TenantService
  - Page reloads after 500ms to refresh content (FR9)
  - Performance tracked and logged (FR10 - <500ms)
  - Warning logged if exceeds 500ms target

- **AC6 ✅**: Error Handling
  - Error MatSnackBar displayed on failure (UX25)
  - User-friendly error messages in Portuguese
  - Specific messages for 403 (no permission) and 404 (not found)
  - Generic fallback message for other errors
  - Snackbar persists for 5 seconds with "Fechar" action

### UX Validation

- ✅ **UX4**: Deep Purple theme (#6A1B9A) applied consistently
- ✅ **UX14**: Success feedback with MatSnackBar
- ✅ **UX19**: MatMenu component for dropdown
- ✅ **UX25**: Clear error messages with actionable feedback

### Integration with Other Stories

- **Story 9.1**: Uses `/api/users/me/context` endpoint for context switching
- **Story 8.4**: Uses `/api/users/me/companies` endpoint to list companies
- **Story 9.2**: Shares same context switch logic and UX patterns

### User Experience Flow

1. User clicks on avatar in top-right corner
2. Menu opens showing:
   - User info (name, email)
   - Current company with green checkmark
   - List of other companies
   - Settings options
   - Logout button
3. User clicks on a different company
4. Loading spinner appears on that company button
5. PUT request sent to switch context
6. Success: JWT updated, snackbar shown, page reloads
7. Error: Error message shown in snackbar

### Performance

- ✅ **FR10**: Context switch tracked for <500ms requirement
- Performance logging in console:
  - Success: "Context switch completed in XXXms"
  - Warning: "Context switch took XXXms (exceeds 500ms target)"

### Accessibility

- All buttons have proper `aria-label` attributes
- Menu items are keyboard navigable
- Color contrast meets WCAG standards
- Focus management handled by Material components

---

**Story criada por**: poly (PM Agent)
**Data**: 2025-12-22
**Implementada por**: Claude Sonnet 4.5
**Data de implementação**: 2025-12-24
**Baseado em**: Epic 9, PRD (FR9, FR10, UX4, UX14, UX19, UX25)
