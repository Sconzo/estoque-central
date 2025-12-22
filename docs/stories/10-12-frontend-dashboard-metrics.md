# Story 10.12: Frontend - Dashboard com Métricas Acionáveis

**Epic**: 10 - Gestão de Colaboradores e Permissões RBAC
**Story ID**: 10.12
**Status**: pending
**Created**: 2025-12-22

---

## User Story

As a **User**,
I want **a dashboard with key metrics**,
So that **I can quickly assess business status**.

---

## Acceptance Criteria

### AC1: Dashboard Widgets
**Given** dashboard route `/dashboard`
**When** user navigates after login
**Then** dashboard displays MatCard widgets (UX21):
- Total de Colaboradores
- Perfis Ativos (Admin: X, Gerente: Y, Vendedor: Z)
- Última atualização da empresa

### AC2: Admin-Only Widgets
**Given** admin user dashboard
**When** admin views dashboard
**Then** additional admin-only cards visible:
- "Gerenciar Colaboradores" quick link
- "Configurações da Empresa" quick link

### AC3: Loading States
**Given** dashboard metrics
**When** data is loaded
**Then** GET requests fetch data
**And** loading state shows skeleton loaders (UX13)
**And** errors show retry option (UX25)

### AC4: Responsive Design
**Given** responsive design
**When** dashboard viewed on mobile
**Then** cards stack vertically
**And** touch targets 44x44px (UX20)
**And** Deep Purple theme (UX4)

---

## Definition of Done
- [ ] Dashboard widgets implementados
- [ ] Admin-only widgets
- [ ] Loading states
- [ ] Responsive design

---

**Story criada por**: poly (PM Agent)
**Data**: 2025-12-22
