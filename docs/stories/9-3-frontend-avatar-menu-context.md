# Story 9.3: Frontend - Menu Avatar com Troca de Contexto

**Epic**: 9 - Seleção e Troca de Contexto Multi-Empresa
**Story ID**: 9.3
**Status**: pending
**Created**: 2025-12-22

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
- [ ] Avatar menu implementado
- [ ] Company list no menu
- [ ] Context switch funcionando
- [ ] Success/error feedback
- [ ] Performance < 500ms

---

**Story criada por**: poly (PM Agent)
**Data**: 2025-12-22
**Baseado em**: Epic 9, PRD (FR9, FR10, UX14, UX19, UX25)
