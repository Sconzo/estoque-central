# Story: UX-2 Criar Componentes Compartilhados Base

**ID:** UX-2
**Criado:** 2025-12-14
**Status:** completed
**Estimativa:** 8-12 horas
**Tempo Real:** ~1.5 horas
**Epic:** STORY-UX-001
**Dependência:** UX-1 (Setup Angular Material) ✅

---

## 📋 Contexto

Criar biblioteca de componentes reutilizáveis: PrimaryButton e MetricCard seguindo UX Design Specification.

**Referências:**
- `docs/REFACTOR-FRONTEND-UX.md` - Fase 2 (linhas 180-335)

---

## 🎯 Acceptance Criteria

- [x] AC-1: `PrimaryButtonComponent` standalone com loading state ✅
- [x] AC-2: `MetricCardComponent` standalone com border colorido ✅
- [x] AC-3: ARIA labels completos (aria-label, aria-busy, aria-live) ✅
- [x] AC-4: Componentes validados em página showcase ✅

---

## 📝 Tasks & Subtasks

### Task 2: Criar Componentes Compartilhados Base

- [x] **2.1** Criar estrutura de pastas
  - ✅ `src/app/shared/components/buttons/`
  - ✅ `src/app/shared/components/feedback/`
  - ✅ `ng g c shared/components/buttons/primary-button --standalone`
  - ✅ `ng g c shared/components/feedback/metric-card --standalone`

- [x] **2.2** Implementar `PrimaryButtonComponent`
  - ✅ Props: label, loadingText, icon, color, disabled, loading, ariaLabel
  - ✅ Output: onClick EventEmitter
  - ✅ Loading: spinner 16px, aria-busy muda dinamicamente
  - ✅ Material Design imports (MatButtonModule, MatIconModule, MatProgressSpinnerModule)
  - ✅ Implemented per spec with all ARIA attributes

- [x] **2.3** Implementar `MetricCardComponent`
  - ✅ Props: title, value, changePercent, icon, color
  - ✅ Border-left 4px colorido dinamicamente
  - ✅ Seta ↑↓ conforme changePercent positivo/negativo
  - ✅ ARIA: aria-label="{{title}} metric", aria-live="polite"
  - ✅ Material Design imports (MatCardModule, MatIconModule)
  - ✅ CSS classes positive/negative para changePercent

- [x] **2.4** Criar página showcase `/component-showcase`
  - ✅ PrimaryButton: loading states, cores primary/accent/warn
  - ✅ PrimaryButton: com/sem ícones, estados disabled
  - ✅ MetricCard: 5 exemplos com cores diferentes
  - ✅ MetricCard: changePercent positivo (+12.5%), negativo (-8.3%), undefined
  - ✅ Build successful, route added

---

## 🧪 Tests

- [x] Unit: PrimaryButton emite onClick ✅
- [x] Unit: PrimaryButton disabled quando loading=true ✅
- [x] Unit: PrimaryButton disabled quando disabled=true ✅
- [x] Unit: PrimaryButton exibe loadingText quando loading ✅
- [x] Unit: PrimaryButton aria-busy correto (9 tests total) ✅
- [x] Unit: MetricCard renderiza title e value ✅
- [x] Unit: MetricCard renderiza changePercent positivo com ↑ ✅
- [x] Unit: MetricCard renderiza changePercent negativo com ↓ ✅
- [x] Unit: MetricCard não renderiza changePercent quando undefined ✅
- [x] Unit: MetricCard aplica border-left colorido ✅
- [x] Unit: ARIA labels corretos em ambos componentes (11 tests total) ✅
- [x] Build: Application builds successfully ✅

**Test Results:** 20/20 tests passed (9 PrimaryButton + 11 MetricCard)

---

## 📁 File List

### Criados:
- `src/app/shared/components/buttons/primary-button/primary-button.component.ts`
- `src/app/shared/components/buttons/primary-button/primary-button.component.spec.ts`
- `src/app/shared/components/feedback/metric-card/metric-card.component.ts`
- `src/app/shared/components/feedback/metric-card/metric-card.component.spec.ts`
- `src/app/component-showcase/component-showcase.component.ts`
- `src/app/component-showcase/component-showcase.component.html`
- `src/app/component-showcase/component-showcase.component.scss`

### Modificados:
- `src/app/app.routes.ts` - Added /component-showcase route
- `package.json` - Added @angular/animations dependency

---

## 📊 Definition of Done

- [x] 2 componentes funcionais (PrimaryButton + MetricCard) ✅
- [x] Tests passando (20/20 unit tests) ✅
- [x] ARIA labels validados (aria-label, aria-busy, aria-live, role) ✅
- [x] Showcase visual OK (accessible at /component-showcase) ✅
- [x] Build successful ✅

---

**Dev Agent Record:**

### Implementation Plan:
1. Create folder structure for shared components (buttons, feedback)
2. Generate PrimaryButton component with Angular CLI
3. Implement PrimaryButton with Material Design modules and ARIA support
4. Generate MetricCard component with Angular CLI
5. Implement MetricCard with dynamic styling and accessibility
6. Create showcase page to demonstrate all component states
7. Write comprehensive unit tests for both components
8. Validate build and test execution

### Completion Notes:

**Components Implemented:**

1. **PrimaryButtonComponent** (`shared/components/buttons/primary-button/`)
   - Standalone component with Material Design integration
   - Props: label, loadingText, icon, color (primary|accent|warn), disabled, loading, ariaLabel
   - Output: onClick EventEmitter
   - Features:
     - Loading state with 16px spinner
     - Dynamic aria-busy attribute
     - Icon support (Material Icons)
     - Disabled state (manual or auto when loading)
     - Min height 40px for touch targets
   - Tests: 9/9 passed ✅

2. **MetricCardComponent** (`shared/components/feedback/metric-card/`)
   - Standalone component with Material Card and Icons
   - Props: title, value, changePercent, icon, color
   - Features:
     - 4px colored left border (dynamic color)
     - Up/down arrows (↑/↓) based on changePercent
     - Positive/negative CSS classes (#2E7D32 green / #C62828 red)
     - ARIA: aria-label on card, aria-live="polite" on value
     - Material Icons with color matching
   - Tests: 11/11 passed ✅

**Showcase Page** (`/component-showcase`)
   - Demonstrates PrimaryButton: 3 colors, icons, loading, disabled states
   - Demonstrates MetricCard: 5 examples with different colors and changePercent values
   - Grid layout for metric cards
   - Accessible via http://localhost:4200/component-showcase

**Dependencies:**
   - Added @angular/animations (required by Material components)

**Accessibility Highlights:**
   - All interactive elements have ARIA labels
   - Loading states use aria-busy
   - Live regions (aria-live) for dynamic content
   - Proper role attributes
   - Icon elements marked aria-hidden="true"
   - Semantic HTML structure

**Test Coverage:**
   - PrimaryButton: onClick emission, disabled states, loading behavior, ARIA attributes
   - MetricCard: rendering, changePercent calculation, border colors, ARIA labels
   - Total: 20/20 unit tests passing

**Time:** ~1.5 hours (significantly faster than 8-12h estimate)

---

**Change Log:**
- 2025-12-14 14:03: Story completed - All AC, tasks, tests passed (20/20)
- 2025-12-14: Story criada
