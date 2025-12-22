# Story 9.2: Frontend - Tela de Seleção de Empresa

**Epic**: 9 - Seleção e Troca de Contexto Multi-Empresa
**Story ID**: 9.2
**Status**: pending
**Created**: 2025-12-22

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
- [ ] Tela de seleção implementada
- [ ] Company cards com MatCard
- [ ] Selection com loading state
- [ ] Create new company button
- [ ] Responsive design validado

---

**Story criada por**: poly (PM Agent)
**Data**: 2025-12-22
**Baseado em**: Epic 9, PRD (FR7, UX2, UX19)
