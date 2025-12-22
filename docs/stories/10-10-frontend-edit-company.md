# Story 10.10: Frontend - Edição de Dados da Empresa

**Epic**: 10 - Gestão de Colaboradores e Permissões RBAC
**Story ID**: 10.10
**Status**: pending
**Created**: 2025-12-22

---

## User Story

As a **Admin**,
I want **to edit my company's information via UI**,
So that **I can keep data up-to-date without technical help**.

---

## Acceptance Criteria

### AC1: Company Data Form
**Given** admin navigates to `/settings/company`
**When** page loads
**Then** form displays current company data from `/api/companies/current` (GET)
**And** fields: Nome, CNPJ, Email, Telefone, Endereço

### AC2: Real-Time Validation
**Given** form editing
**When** admin modifies fields
**Then** validators run in real-time
**And** save button disabled if invalid

### AC3: Form Submission
**Given** form submission
**When** admin clicks "Salvar Alterações"
**Then** PUT to `/api/companies/current`
**And** button shows loading spinner

### AC4: Success Feedback
**Given** successful save
**When** backend returns success
**Then** success MatSnackBar: "Dados atualizados" (UX14)

### AC5: Delete Company Option
**Given** delete company option
**When** admin scrolls to danger zone
**Then** "Deletar Empresa" button visible (red)
**And** on click, strong confirmation dialog

---

## Definition of Done
- [ ] Form implementado
- [ ] Validation em tempo real
- [ ] Save com loading
- [ ] Delete option

---

**Story criada por**: poly (PM Agent)
**Data**: 2025-12-22
