# Story 8.6: Frontend - Loading States e Feedback Visual

**Epic**: 8 - Criação Self-Service de Empresa
**Story ID**: 8.6
**Status**: pending
**Created**: 2025-12-22

---

## User Story

As a **User**,
I want **clear visual feedback during company creation**,
So that **I know the system is working and feel confident in the process**.

---

## Acceptance Criteria

### AC1: Button Loading State

**Given** form submission
**When** user clicks "Criar Empresa"
**Then** button shows loading spinner (MatProgressSpinner inside button) (UX13)
**And** button text changes to "Criando..."
**And** button is disabled to prevent double-click

### AC2: Full-Screen Loading Overlay

**Given** provisioning in progress
**When** request is sent to backend
**Then** full-screen or modal loading overlay is displayed
**And** loading message: "Criando seu espaço isolado..." (FR4)
**And** progress indicator animates to show activity
**And** estimated time "Isso pode levar até 30 segundos" is displayed

### AC3: Success Feedback

**Given** successful creation
**When** backend returns success
**Then** loading overlay is removed
**And** success MatSnackBar appears with message: "Empresa criada com sucesso! Redirecionando..." (UX14)
**And** snackBar auto-dismisses after 3 seconds
**And** redirect to dashboard occurs smoothly

### AC4: Error Feedback

**Given** creation failure
**When** backend returns error
**Then** loading overlay is removed
**And** error MatSnackBar appears with clear message (UX25)
**And** error includes retry action: "Tentar Novamente" button
**And** snackBar persists until user dismisses or retries

### AC5: Design Principles

**Given** design principles
**When** UI is displayed
**Then** colors follow Deep Purple theme `#6A1B9A` (UX4)
**And** buttons have large touch targets 44x44px (UX20)
**And** states are always visible (loading, success, error) (UX24)
**And** design transmits Confiança and Eficiência emotions (UX8)

---

## Definition of Done

- [ ] Button loading state implementado
- [ ] Full-screen overlay criado
- [ ] Success feedback com MatSnackBar
- [ ] Error feedback com retry
- [ ] Design principles aplicados
- [ ] UX validada

---

**Story criada por**: poly (PM Agent)
**Data**: 2025-12-22
**Baseado em**: Epic 8, PRD (FR4, UX4, UX8, UX13, UX14, UX20, UX24, UX25)
