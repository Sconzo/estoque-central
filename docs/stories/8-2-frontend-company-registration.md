# Story 8.2: Frontend - Tela de Cadastro de Empresa

**Epic**: 8 - Criação Self-Service de Empresa
**Story ID**: 8.2
**Status**: pending
**Created**: 2025-12-22

---

## User Story

As a **User**,
I want **a simple form to register my company**,
So that **I can quickly set up my account and start using the system**.

---

## Acceptance Criteria

### AC1: Route Guard e Redirecionamento

**Given** a user without linked companies
**When** user completes Google OAuth login
**Then** user is redirected to `/create-company` route (FR6)
**And** Angular route guard checks if user has companies
**And** users with companies are redirected to `/select-company` or `/dashboard`

### AC2: Company Registration Form

**Given** create company form
**When** form is displayed
**Then** it contains MatFormField inputs (UX16):
- Nome da Empresa (required, max 255 chars)
- CNPJ (optional, 14 digits with validation)
- Email (required, email format validation)
- Telefone (optional, phone format)
**And** form uses Angular Reactive Forms
**And** validators are applied for required fields

### AC3: Form Validation

**Given** form validation
**When** user submits invalid data
**Then** error messages are displayed inline below fields
**And** submit button is disabled until form is valid
**And** error messages are clear and actionable (UX25)

### AC4: Form Submission

**Given** form submission
**When** user clicks "Criar Empresa" button
**Then** POST request is sent to `/api/public/companies`
**And** request payload includes form data + `userId` from auth context
**And** loading spinner is displayed (MatProgressSpinner) (UX13, FR4)
**And** form is disabled during submission

### AC5: Loading State

**Given** loading state during provisioning
**When** backend is creating tenant + schema
**Then** loading message displays: "Criando seu espaço isolado... quase lá!" (FR4)
**And** progress indicator shows activity for 15-30 seconds
**And** user cannot navigate away or submit again

### AC6: Success Handling

**Given** successful company creation
**When** backend returns 201 Created
**Then** success message is displayed (MatSnackBar) (UX14)
**And** JWT with `tenantId` is stored in local storage or session
**And** user is redirected to `/dashboard`
**And** `TenantService` updates current tenant context

### AC7: Error Handling

**Given** company creation failure
**When** backend returns error
**Then** error message is displayed (MatSnackBar) (UX25)
**And** error message explains what went wrong
**And** retry button is available
**And** form is re-enabled for editing

---

## Definition of Done

- [ ] Route guard implementado
- [ ] Formulário criado com validações
- [ ] Submit com loading state
- [ ] Success handling funcionando
- [ ] Error handling completo
- [ ] Testes unitários passando

---

**Story criada por**: poly (PM Agent)
**Data**: 2025-12-22
**Baseado em**: Epic 8, PRD (FR6, UX13, UX14, UX16, UX25)
