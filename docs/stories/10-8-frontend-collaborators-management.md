# Story 10.8: Frontend - Tela de Gestão de Colaboradores

**Epic**: 10 - Gestão de Colaboradores e Permissões RBAC
**Story ID**: 10.8
**Status**: pending
**Created**: 2025-12-22

---

## User Story

As a **Admin**,
I want **a screen to manage collaborators**,
So that **I can invite, view, and remove team members**.

---

## Acceptance Criteria

### AC1: Collaborators Table
**Given** admin navigates to `/settings/collaborators`
**When** page loads
**Then** MatTable displays list from `/api/collaborators` (UX17)
**And** columns: Nome, Email, Perfil, Data, Ações

### AC2: Invite Dialog
**Given** invite collaborator form
**When** admin clicks "Convidar Colaborador"
**Then** MatDialog opens with form (UX18): Email, Perfil

### AC3: Form Submission
**Given** form submission
**When** admin submits invitation
**Then** POST to `/api/collaborators`
**And** on success, dialog closes, table refreshes

### AC4: Remove Collaborator
**Given** remove collaborator
**When** admin clicks "Remover"
**Then** confirmation dialog appears
**And** on confirm, DELETE request sent

### AC5: Promote to Admin
**Given** promote to admin
**When** admin clicks "Promover para Admin"
**Then** PUT to `/api/collaborators/{userId}/promote`

---

## Definition of Done
- [ ] MatTable implementado
- [ ] Invite dialog
- [ ] Remove confirmation
- [ ] Promote funcionando

---

**Story criada por**: poly (PM Agent)
**Data**: 2025-12-22
