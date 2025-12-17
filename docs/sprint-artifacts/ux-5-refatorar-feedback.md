# Story: UX-5 Refatorar Feedback com MatSnackBar

**ID:** UX-5
**Criado:** 2025-12-14
**Status:** ready-for-dev
**Estimativa:** 6-10 horas
**Epic:** STORY-UX-001
**Dependência:** UX-1 (Setup Angular Material)

---

## 📋 Contexto

Substituir alerts/toasts por MatSnackBar seguindo UX patterns (success 3s, error 5s, warning 4s, info 3s).

**Referências:**
- `docs/REFACTOR-FRONTEND-UX.md` - Fase 3.4 (linhas 441-473)
- `docs/ux-design-specification.md` - Seção 7.1.2 (Feedback Patterns)

---

## 🎯 Acceptance Criteria

- [ ] AC-1: Todos alerts substituídos por MatSnackBar
- [ ] AC-2: Success: 3s, verde #2E7D32, ícone ✓
- [ ] AC-3: Error: 5s, vermelho #C62828, ícone ✕, action "Tentar Novamente"
- [ ] AC-4: Warning: 4s, dourado #F9A825
- [ ] AC-5: ARIA live regions: polite (success/info), assertive (error)

---

## 📝 Tasks & Subtasks

### Task 5: Refatorar Feedback com MatSnackBar

- [ ] **5.1** Mapear alerts existentes
  - Buscar: `alert(`, `window.alert`, outros toasts
  - Listar: componentes para refatoração

- [ ] **5.2** Implementar MatSnackBar success
  - Método: `showSuccess(message: string)`
  - Duration 3s, panelClass `success-snackbar`, position top-end
  - Prefixo: `✓`
  - Ref: `docs/REFACTOR-FRONTEND-UX.md` linhas 454-461

- [ ] **5.3** Implementar MatSnackBar error
  - Método: `showError(message: string, retryFn?: () => void)`
  - Duration 5s, panelClass `error-snackbar`, action "Tentar Novamente"
  - Prefixo: `✕`
  - onAction: executar retryFn
  - Ref: `docs/REFACTOR-FRONTEND-UX.md` linhas 463-472

- [ ] **5.4** Implementar warning e info
  - Warning: 4s, `warning-snackbar`, ícone `⚠`
  - Info: 3s, cor azul, ícone `ℹ`
  - Ref: `docs/ux-design-specification.md` seção 7.1.2

- [ ] **5.5** Adicionar ARIA live regions
  - Success/Info: `aria-live="polite"`
  - Error: `aria-live="assertive"`
  - Validar: screen reader anuncia

---

## 🧪 Tests

- [ ] Unit: MatSnackBar.open chamado com params corretos
- [ ] Unit: Retry callback executado em error
- [ ] E2E: Snackbar aparece e desaparece
- [ ] A11y: Screen reader anuncia

---

## 📁 File List

### Modificados:
(preencher durante implementação - componentes com feedback)

---

## 📊 Definition of Done

- [ ] Alerts substituídos
- [ ] ARIA live regions
- [ ] Tests passando

---

**Dev Agent Record:**

### Implementation Plan:


### Completion Notes:


---

**Change Log:**
- 2025-12-14: Story criada
