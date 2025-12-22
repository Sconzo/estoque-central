# Story 10.9: Frontend - Controle de Acesso Baseado em Roles (UI)

**Epic**: 10 - Gestão de Colaboradores e Permissões RBAC
**Story ID**: 10.9
**Status**: pending
**Created**: 2025-12-22

---

## User Story

As a **User**,
I want **to see only features I have permission to access**,
So that **the UI is tailored to my role**.

---

## Acceptance Criteria

### AC1: Role Directive
**Given** role-based UI rendering
**When** components check permissions
**Then** Angular directive `*appHasRole="'ADMIN'"` hides/shows elements

### AC2: Admin User UI
**Given** Admin user
**When** admin navigates the app
**Then** all menu items visible
**And** "Configurações" accessible

### AC3: Gerente User UI
**Given** Gerente user
**When** gerente navigates the app
**Then** "Produtos" and "Estoque" with write access
**And** "Configurações → Colaboradores" hidden

### AC4: Vendedor User UI
**Given** Vendedor user
**When** vendedor navigates the app
**Then** "Vendas" with write access
**And** "Configurações" completely hidden

### AC5: Route Guard
**Given** unauthorized access attempt
**When** user manually navigates to restricted route
**Then** route guard redirects to `/dashboard` or `/403`

---

## Definition of Done
- [ ] Role directive implementado
- [ ] Admin, Gerente, Vendedor UIs
- [ ] Route guard configurado

---

**Story criada por**: poly (PM Agent)
**Data**: 2025-12-22
