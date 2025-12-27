# Story 10.8: Frontend - Tela de Gestão de Colaboradores

**Epic**: 10 - Gestão de Colaboradores e Permissões RBAC
**Story ID**: 10.8
**Status**: completed
**Created**: 2025-12-22
**Completed**: 2025-12-27

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
- [x] MatTable implementado
- [x] Invite dialog
- [x] Remove confirmation
- [x] Promote funcionando

## Implementation Plan

### Arquivos a Criar

1. **collaborators-management.component.ts** - Componente principal
   - Rota: `/settings/collaborators`
   - Carrega lista via `GET /api/companies/{companyId}/collaborators`
   - MatTable com colunas: Nome, Email, Role, Data Convite, Ações

2. **collaborators-management.component.html** - Template
   - MatTable com datasource
   - Botão "Convidar Colaborador"
   - Ações por linha: Promover, Remover

3. **invite-collaborator-dialog.component.ts** - Dialog de convite
   - FormGroup com validators (email, role)
   - POST to `/api/companies/{companyId}/collaborators`
   - MatSnackBar para feedback

4. **collaborator.service.ts** - Service Angular
   ```typescript
   - listCollaborators(companyId: number): Observable<CollaboratorDetail[]>
   - inviteCollaborator(companyId: number, request: InviteRequest): Observable<InviteResponse>
   - removeCollaborator(companyId: number, userId: number): Observable<void>
   - promoteToAdmin(companyId: number, userId: number): Observable<void>
   ```

5. **collaborator.model.ts** - Interfaces TypeScript
   ```typescript
   interface CollaboratorDetail {
     id: number;
     userId: number;
     userName: string;
     userEmail: string;
     role: string;
     invitedAt: string;
     active: boolean;
   }
   ```

### Fluxo de Implementação

1. **MatTable com dados** (AC1)
   - DataSource conectado ao service
   - Refresh automático após ações

2. **Dialog de convite** (AC2, AC3)
   - MatDialog com form reativo
   - Validators: email required & valid, role required
   - Submit chama service.inviteCollaborator()
   - OnSuccess: fecha dialog, refresh table, snackbar

3. **Remover colaborador** (AC4)
   - MatDialog de confirmação
   - OnConfirm: service.removeCollaborator()
   - Trata erros de self-removal e last-admin

4. **Promover para admin** (AC5)
   - Botão condicional (só para non-admins)
   - Dialog de confirmação
   - service.promoteToAdmin()
   - Trata erro de already-admin

---

**Story criada por**: poly (PM Agent)
**Data**: 2025-12-22
