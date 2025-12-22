# Story 7.6: Setup Angular Material Design System + Frontend Base

**Epic**: 7 - Infraestrutura Multi-Tenant e Deploy
**Story ID**: 7.6
**Status**: pending
**Created**: 2025-12-22

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

- [ ] Angular Material instalado e configurado
- [ ] Tema customizado aplicado
- [ ] Layout responsivo funcionando
- [ ] Acessibilidade WCAG AA validada
- [ ] Core services implementados

---

**Story criada por**: poly (PM Agent)
**Data**: 2025-12-22
**Baseado em**: Epic 7, PRD (UX1-UX7)
