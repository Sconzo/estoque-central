# Story: Refatorar Frontend para UX Design Specification

**ID:** STORY-UX-001
**Criado:** 2025-12-14
**Autor:** poly
**Status:** split-into-smaller-stories
**Estimativa:** 76-120 horas (2-3 semanas)

**Split Stories:**
- UX-1: Setup Angular Material (4-8h) - ready-for-dev
- UX-2: Componentes Compartilhados (8-12h) - ready-for-dev
- UX-3: Refatorar Dashboard (8-12h) - ready-for-dev
- UX-4: Refatorar Forms (12-16h) - ready-for-dev
- UX-5: Refatorar Feedback (6-10h) - ready-for-dev
- UX-6: Navigation Responsiva (12-16h) - ready-for-dev
- UX-7: Acessibilidade WCAG (10-14h) - ready-for-dev
- UX-8: Performance & Validação (8-12h) - ready-for-dev

---

## 📋 Contexto

O Estoque Central possui UX Design Specification completa (`docs/ux-design-specification.md`) que define identidade visual, componentes, padrões de UX e acessibilidade WCAG AA. Esta story implementa essa spec no frontend Angular existente.

**Referências:**
- `docs/ux-design-specification.md` (2.648 linhas)
- `docs/REFACTOR-FRONTEND-UX.md` (guia de implementação)
- `docs/ux-color-themes-purple.html` (visualizador)
- `docs/dashboard-mockup-purple.html` (mockup)

---

## 🎯 Acceptance Criteria

**Visual:**
- [ ] AC-V1: Cor primária roxo `#6A1B9A` em todos botões primários
- [ ] AC-V2: Cor accent dourado `#F9A825` apenas em warnings/destaques
- [ ] AC-V3: Typography Roboto em toda aplicação
- [ ] AC-V4: Material Icons (não emojis)
- [ ] AC-V5: Spacing 8px base unit consistente

**Componentes:**
- [ ] AC-C1: Botões usando `mat-raised-button`, `mat-stroked-button`, `mat-icon-button`
- [ ] AC-C2: Forms usando `mat-form-field` appearance="outline"
- [ ] AC-C3: Alerts/toasts usando `MatSnackBar`
- [ ] AC-C4: Modals usando `MatDialog`
- [ ] AC-C5: Loading usando `MatProgressSpinner` ou `MatProgressBar`

**Responsividade:**
- [ ] AC-R1: Desktop (≥1280px): Sidebar permanente, 3-4 colunas
- [ ] AC-R2: Tablet (960-1279px): Sidebar dismissible, 2 colunas
- [ ] AC-R3: Mobile (<960px): Hamburger menu, 1 coluna, bottom nav

**Acessibilidade WCAG AA:**
- [ ] AC-A1: Elementos interativos navegáveis por teclado
- [ ] AC-A2: Botões com ícone têm `aria-label`
- [ ] AC-A3: Forms têm validação `mat-error`
- [ ] AC-A4: Color contrast ≥4.5:1
- [ ] AC-A5: Touch targets ≥48x48px mobile

**Performance:**
- [ ] AC-P1: Bundle size <+500KB
- [ ] AC-P2: First Contentful Paint <1.5s
- [ ] AC-P3: Time to Interactive <3s
- [ ] AC-P4: Lighthouse score ≥90

---

## 📝 Tasks & Subtasks

### Task 1: Setup Angular Material e Tema Customizado

**Objetivo:** Instalar Angular Material e configurar tema Deep Purple Luxury

**Subtasks:**

- [ ] **1.1** Instalar Angular Material
  - Executar: `ng add @angular/material`
  - Selecionar: Custom theme, Typography Yes, Animations Yes
  - Validar: pacotes instalados em `package.json`

- [ ] **1.2** Criar arquivo `src/styles/theme.scss`
  - Copiar paleta Deep Purple da `docs/REFACTOR-FRONTEND-UX.md` linhas 41-113
  - Definir `$estoque-primary` (roxo #6A1B9A)
  - Definir `$estoque-accent` (dourado #F9A825)
  - Definir `$estoque-warn` (vermelho padrão 800)
  - Aplicar tema: `@include mat.all-component-themes($estoque-theme)`

- [ ] **1.3** Atualizar `src/styles.scss`
  - Importar: `@use './styles/theme'`
  - Adicionar classes semânticas: `.success-text`, `.warning-text`, `.error-text`, `.info-text`
  - Adicionar classes snackbar: `.success-snackbar`, `.error-snackbar`, `.warning-snackbar`
  - Código ref: `docs/REFACTOR-FRONTEND-UX.md` linhas 117-147

- [ ] **1.4** Configurar Locale pt-BR
  - Editar `src/app/app.config.ts`
  - Importar: `registerLocaleData`, `localePt`, `MAT_DATE_LOCALE`
  - Adicionar providers: `LOCALE_ID: 'pt-BR'`, `MAT_DATE_LOCALE: 'pt-BR'`
  - Código ref: `docs/REFACTOR-FRONTEND-UX.md` linhas 153-168

- [ ] **1.5** Validar Setup
  - Criar página teste com botão: `<button mat-raised-button color="primary">Teste</button>`
  - Validar: botão aparece roxo #6A1B9A
  - Validar: fonte Roboto aplicada
  - Remover página teste após validação

**Tests:**
- [ ] Unit: Validar providers pt-BR em `app.config.spec.ts`
- [ ] E2E: Botão mat-raised-button color="primary" renderiza roxo

**DoD:** Angular Material instalado, tema roxo funcionando, locale pt-BR ativo

---

### Task 2: Criar Componentes Compartilhados Base

**Objetivo:** Criar biblioteca de componentes reutilizáveis seguindo UX spec

**Subtasks:**

- [ ] **2.1** Criar estrutura de pastas
  - Criar: `src/app/shared/components/buttons/`
  - Criar: `src/app/shared/components/feedback/`
  - Executar: `ng g c shared/components/buttons/primary-button --standalone --skip-tests`
  - Executar: `ng g c shared/components/feedback/metric-card --standalone --skip-tests`

- [ ] **2.2** Implementar `PrimaryButtonComponent`
  - Copiar código: `docs/REFACTOR-FRONTEND-UX.md` linhas 197-246
  - Props: `label`, `loadingText`, `icon`, `color`, `disabled`, `loading`, `ariaLabel`
  - Output: `onClick` EventEmitter
  - Validar: loading state mostra spinner 16px
  - Validar: `aria-busy` muda com loading

- [ ] **2.3** Implementar `MetricCardComponent`
  - Copiar código: `docs/REFACTOR-FRONTEND-UX.md` linhas 250-327
  - Props: `title`, `value`, `changePercent`, `icon`, `color`
  - Border-left 4px com cor customizada
  - Seta ↑↓ conforme changePercent positivo/negativo
  - Validar: `aria-label` inclui título + "metric"
  - Validar: `aria-live="polite"` no valor

- [ ] **2.4** Criar página de exemplo para validar componentes
  - Rota: `/component-showcase`
  - Usar `PrimaryButtonComponent` com loading states
  - Usar `MetricCardComponent` com diferentes cores
  - Validar visualmente contra `docs/dashboard-mockup-purple.html`

**Tests:**
- [ ] Unit: `PrimaryButtonComponent` emite onClick quando clicado
- [ ] Unit: `PrimaryButtonComponent` disabled quando loading=true
- [ ] Unit: `MetricCardComponent` renderiza changePercent corretamente
- [ ] Unit: ARIA labels corretos em ambos componentes

**DoD:** 2 componentes compartilhados funcionais, testados, validados visualmente

---

### Task 3: Refatorar Dashboard com Material Components

**Objetivo:** Aplicar Material Design no Dashboard existente

**Subtasks:**

- [ ] **3.1** Refatorar cards de métricas
  - Localizar: `frontend/src/app/features/dashboard/dashboard.component.html`
  - Substituir divs de métricas por `<app-metric-card>`
  - Aplicar cores: vendas (roxo #6A1B9A), estoque crítico (vermelho #C62828)
  - Adicionar: `changePercent` se houver dados de comparação
  - Código ref: `docs/REFACTOR-FRONTEND-UX.md` linhas 343-369

- [ ] **3.2** Refatorar botões de ação
  - Substituir botões HTML por `mat-raised-button` color="primary"
  - Botões secundários: `mat-stroked-button`
  - Adicionar `<mat-icon>` antes do texto
  - Touch targets mobile: min-height 48px
  - Código ref: `docs/REFACTOR-FRONTEND-UX.md` linhas 373-391

- [ ] **3.3** Adicionar grid responsivo
  - CSS: mobile 1 coluna, tablet 2 colunas, desktop 4 colunas
  - Breakpoints: xs(<600px), sm(600-959px), md(960-1279px), lg(≥1280px)
  - Gap: 16px, padding: 16px
  - Código ref: `docs/REFACTOR-FRONTEND-UX.md` linhas 588-605

- [ ] **3.4** Validar visualmente
  - Comparar com: `docs/dashboard-mockup-purple.html`
  - Validar: cores roxo primário e dourado accent
  - Validar: Material Icons (não emojis)
  - Validar: responsivo em 375px, 768px, 1366px, 1920px

**Tests:**
- [ ] Unit: Dashboard carrega métricas mockadas
- [ ] Unit: Botões renderizam com cores corretas
- [ ] E2E: Dashboard renderiza sem erros
- [ ] Visual: Screenshot regression test (opcional)

**DoD:** Dashboard refatorado 100% Material, responsivo, visualmente validado

---

### Task 4: Refatorar Forms com mat-form-field

**Objetivo:** Padronizar todos forms com Material appearance="outline"

**Subtasks:**

- [ ] **4.1** Mapear todos forms existentes
  - Buscar: `<form` em `frontend/src/app/**/*.html`
  - Listar: arquivos que precisam refatoração
  - Priorizar: forms mais usados primeiro

- [ ] **4.2** Refatorar form de produto (exemplo)
  - Substituir inputs por `<mat-form-field appearance="outline">`
  - Adicionar `<mat-label>` com asterisco se required
  - Adicionar `<mat-error>` com mensagens claras
  - Adicionar `<mat-hint>` onde necessário
  - Código ref: `docs/REFACTOR-FRONTEND-UX.md` linhas 410-438

- [ ] **4.3** Aplicar validação on-blur
  - Reactive forms: `FormGroup` com validators
  - Mostrar erro apenas após `touched` ou submit
  - NUNCA validar "while typing"
  - Ref: `docs/ux-design-specification.md` seção 7.1.3 (linhas 1120-1123)

- [ ] **4.4** Adicionar ARIA attributes
  - Input: `aria-label`, `aria-describedby`, `aria-required`
  - Error: `role="alert"`
  - Hint: `id` para aria-describedby
  - Código ref: `docs/REFACTOR-FRONTEND-UX.md` linhas 657-668

- [ ] **4.5** Botões de form actions
  - Layout: Cancelar (stroked) à esquerda, Salvar (raised primary) à direita
  - Disabled: Salvar desabilitado se form inválido
  - Código ref: `docs/REFACTOR-FRONTEND-UX.md` linhas 431-437

**Tests:**
- [ ] Unit: Validação funciona corretamente
- [ ] Unit: ARIA attributes presentes
- [ ] E2E: Form submit com dados válidos
- [ ] E2E: Form mostra erros com dados inválidos
- [ ] A11y: axe-core passa sem erros

**DoD:** Todos forms usando mat-form-field, validação WCAG AA, testados

---

### Task 5: Refatorar Feedback com MatSnackBar

**Objetivo:** Substituir alerts/toasts por MatSnackBar seguindo UX patterns

**Subtasks:**

- [ ] **5.1** Mapear alerts existentes
  - Buscar: `alert(`, `window.alert`, outros toasts
  - Listar: componentes que precisam refatoração

- [ ] **5.2** Implementar MatSnackBar success
  - Injetar: `MatSnackBar` no constructor
  - Método: `showSuccess(message: string)`
  - Config: duration 3s, panelClass `success-snackbar`, position top-end
  - Icon: `✓` prefixo
  - Código ref: `docs/REFACTOR-FRONTEND-UX.md` linhas 454-461

- [ ] **5.3** Implementar MatSnackBar error
  - Método: `showError(message: string, retryFn?: () => void)`
  - Config: duration 5s, panelClass `error-snackbar`, action "Tentar Novamente"
  - Icon: `✕` prefixo
  - onAction: executar retryFn se fornecido
  - Código ref: `docs/REFACTOR-FRONTEND-UX.md` linhas 463-472

- [ ] **5.4** Implementar MatSnackBar warning e info
  - Warning: duration 4s, panelClass `warning-snackbar`, icon `⚠`
  - Info: duration 3s, cor azul, icon `ℹ`
  - Ref: `docs/ux-design-specification.md` seção 7.1.2 (linhas 1070-1089)

- [ ] **5.5** Adicionar ARIA live regions
  - Success/Info: `aria-live="polite"`
  - Error: `aria-live="assertive"`
  - Validar: screen reader anuncia mensagens

**Tests:**
- [ ] Unit: MatSnackBar.open chamado com params corretos
- [ ] Unit: Retry callback executado em error snackbar
- [ ] E2E: Snackbar aparece e desaparece após duration
- [ ] A11y: Screen reader anuncia mensagens

**DoD:** Todos alerts substituídos por MatSnackBar, ARIA live regions, testados

---

### Task 6: Implementar Navigation Responsiva

**Objetivo:** Sidebar desktop, hamburger mobile, bottom nav opcional

**Subtasks:**

- [ ] **6.1** Instalar Angular CDK Layout
  - Verificar: `@angular/cdk` em `package.json`
  - Importar: `BreakpointObserver` de `@angular/cdk/layout`

- [ ] **6.2** Implementar detecção de breakpoint
  - `app.component.ts`: injetar `BreakpointObserver`
  - Observable: `breakpointObserver.observe([Breakpoints.Handset])`
  - Prop: `isDesktop = !result.matches`
  - Código ref: `docs/REFACTOR-FRONTEND-UX.md` linhas 564-576

- [ ] **6.3** Criar navigation desktop
  - `mat-sidenav-container` com `mat-sidenav mode="side" opened`
  - Width: 260px
  - Conteúdo: `<app-main-nav>` (criar se não existe)
  - Toolbar: roxo primary, user menu top-right
  - Código ref: `docs/REFACTOR-FRONTEND-UX.md` linhas 493-520

- [ ] **6.4** Criar navigation mobile
  - `mat-sidenav mode="over"` (overlay)
  - Toolbar: hamburger button esquerda, título centro
  - Bottom nav: 3-5 itens principais (dashboard, vendas, estoque)
  - `aria-label` em todos botões
  - Código ref: `docs/REFACTOR-FRONTEND-UX.md` linhas 524-559

- [ ] **6.5** Implementar skip links
  - Primeiro elemento body: `<a href="#main-content" class="skip-link">`
  - CSS: hidden até focus (top: -40px → top: 0 on focus)
  - Cor: roxo #6A1B9A, texto branco
  - Código ref: `docs/REFACTOR-FRONTEND-UX.md` linhas 724-749

**Tests:**
- [ ] Unit: BreakpointObserver muda isDesktop corretamente
- [ ] E2E: Desktop mostra sidebar permanente
- [ ] E2E: Mobile mostra hamburger menu
- [ ] E2E: Skip link funciona com Tab
- [ ] A11y: Keyboard navigation completa

**DoD:** Navigation adaptativa desktop/mobile, skip links, WCAG AA

---

### Task 7: Implementar Acessibilidade WCAG AA

**Objetivo:** ARIA labels, keyboard nav, color contrast, touch targets

**Subtasks:**

- [ ] **7.1** Adicionar ARIA labels em botões com ícone
  - Buscar: `<button mat-icon-button>` sem aria-label
  - Adicionar: `aria-label` descritivo
  - Ex: `aria-label="Excluir produto"`
  - Código ref: `docs/REFACTOR-FRONTEND-UX.md` linhas 643-653

- [ ] **7.2** Implementar focus visível
  - CSS: `*:focus { outline: 2px solid #6A1B9A; outline-offset: 2px; }`
  - Remover outline apenas se `body:not(.user-is-tabbing)`
  - JS: detectar Tab keydown → add class `.user-is-tabbing`
  - JS: detectar mousedown → remove class
  - Código ref: `docs/REFACTOR-FRONTEND-UX.md` linhas 694-720

- [ ] **7.3** Validar color contrast
  - Ferramenta: https://webaim.org/resources/contrastchecker/
  - Validar: roxo #6A1B9A em branco = 8.2:1 ✓
  - Corrigir: dourado #F9A825 NUNCA como texto em branco
  - Dourado: apenas background ou ícone
  - Código ref: `docs/REFACTOR-FRONTEND-UX.md` linhas 756-774

- [ ] **7.4** Ajustar touch targets mobile
  - CSS media query: `@media (max-width: 1279px)`
  - Buttons: `min-height: 48px; min-width: 48px;`
  - Spacing: mínimo 8px entre targets
  - Código ref: `docs/REFACTOR-FRONTEND-UX.md` linhas 618-624

- [ ] **7.5** Testar com screen reader
  - Windows: NVDA ou JAWS
  - Mac: VoiceOver
  - Validar: todos elementos anunciados corretamente
  - Validar: live regions funcionam (snackbars)

**Tests:**
- [ ] A11y: axe-core passa 100%
- [ ] A11y: Lighthouse accessibility score ≥90
- [ ] Manual: Keyboard navigation completa (Tab, Enter, Esc)
- [ ] Manual: Screen reader anuncia tudo corretamente
- [ ] Manual: Touch targets ≥48x48px em mobile

**DoD:** WCAG 2.1 Level AA compliant, axe-core passa, Lighthouse ≥90

---

### Task 8: Performance e Validação Final

**Objetivo:** Bundle size, performance metrics, validação completa

**Subtasks:**

- [ ] **8.1** Analisar bundle size
  - Executar: `ng build --stats-json`
  - Analisar: `webpack-bundle-analyzer dist/stats.json`
  - Validar: aumento <500KB vs baseline
  - Otimizar: lazy loading se necessário

- [ ] **8.2** Medir performance metrics
  - Lighthouse: First Contentful Paint <1.5s
  - Lighthouse: Time to Interactive <3s
  - Lighthouse: Overall score ≥90
  - Otimizar: tree-shaking, minification

- [ ] **8.3** Validação visual completa
  - Desktop 1920px: sidebar permanente, 4 colunas
  - Laptop 1366px: sidebar permanente, 3 colunas
  - Tablet 768px: sidebar dismissible, 2 colunas
  - Mobile 375px: hamburger menu, 1 coluna
  - Comparar: `docs/dashboard-mockup-purple.html`

- [ ] **8.4** Validação de todos AC
  - Revisar: checklist de Acceptance Criteria no topo
  - Marcar: todos itens AC-V*, AC-C*, AC-R*, AC-A*, AC-P*
  - Documentar: evidências (screenshots, test reports)

- [ ] **8.5** Code review
  - Revisar: Material Icons (não emojis) em todo codebase
  - Revisar: Cores roxo/dourado aplicadas corretamente
  - Revisar: ARIA labels completos
  - Revisar: Tests passando 100%

**Tests:**
- [ ] E2E: Smoke test em todas telas refatoradas
- [ ] Performance: Lighthouse report ≥90
- [ ] A11y: axe-core 100% pass
- [ ] Visual: Regression tests (opcional)

**DoD:** Bundle <500KB extra, Lighthouse ≥90, todos AC validados

---

## 📊 Definition of Done (Story-Level)

**Code:**
- [ ] Todos tasks/subtasks marcados [x]
- [ ] Angular Material instalado e tema roxo aplicado
- [ ] Componentes compartilhados criados e testados
- [ ] Dashboard refatorado com Material components
- [ ] Forms usando mat-form-field appearance="outline"
- [ ] Feedback usando MatSnackBar (success, error, warning, info)
- [ ] Navigation responsiva (desktop sidebar, mobile hamburger)
- [ ] ARIA labels completos em elementos interativos
- [ ] Focus visível implementado (outline roxo)
- [ ] Color contrast ≥4.5:1 validado
- [ ] Touch targets ≥48px em mobile

**Tests:**
- [ ] Unit tests: 100% passing
- [ ] E2E tests: 100% passing
- [ ] A11y tests: axe-core passa sem erros
- [ ] Performance: Lighthouse score ≥90

**Documentation:**
- [ ] Code comments em componentes complexos
- [ ] README atualizado com instruções de build
- [ ] Screenshots de antes/depois (opcional)

**Acceptance Criteria:**
- [ ] Todos 20 AC marcados como completos
- [ ] Evidências documentadas (screenshots, test reports)
- [ ] Aprovação visual do usuário (poly)

**Ready for Review:**
- [ ] Branch pushed para remote
- [ ] Pull request criado
- [ ] Todos checks CI/CD passando
- [ ] Solicitado review de outro dev

---

## 📁 Arquivos Modificados (preencher durante execução)

### Criados:
- `src/styles/theme.scss`
- `src/app/shared/components/buttons/primary-button.component.ts`
- `src/app/shared/components/feedback/metric-card.component.ts`
- (adicionar outros)

### Modificados:
- `src/styles.scss`
- `src/app/app.config.ts`
- `frontend/src/app/features/dashboard/dashboard.component.html`
- `frontend/src/app/features/dashboard/dashboard.component.ts`
- (adicionar outros)

---

## 🧪 Test Strategy

**Unit Tests:**
- Componentes compartilhados (PrimaryButton, MetricCard)
- ARIA attributes presentes
- Event emitters funcionam
- Props renderizam corretamente

**E2E Tests:**
- Dashboard carrega sem erros
- Forms submit com validação
- Navigation responsiva funciona
- MatSnackBar aparece e desaparece

**Accessibility Tests:**
- axe-core automated scan
- Keyboard navigation manual
- Screen reader manual (NVDA/VoiceOver)
- Color contrast validation

**Performance Tests:**
- Lighthouse audit
- Bundle size analysis
- Core Web Vitals

---

## 📝 Notas de Implementação

**Prioridades:**
1. Setup e Fundação (Task 1) - CRÍTICO
2. Componentes Base (Task 2) - ALTO
3. Dashboard (Task 3) - ALTO
4. Forms (Task 4) - MÉDIO
5. Feedback (Task 5) - MÉDIO
6. Navigation (Task 6) - MÉDIO
7. Acessibilidade (Task 7) - ALTO
8. Performance (Task 8) - MÉDIO

**Riscos:**
- Bundle size pode aumentar >500KB → mitigar com lazy loading
- Refatoração pode quebrar funcionalidades → testes regressão críticos
- Screen reader testing pode revelar issues → buffer 20% tempo

**Dependências Externas:**
- Angular Material 18+
- @angular/cdk
- Roboto font (Google Fonts)

---

**Status Final:** ⏳ Aguardando execução via Dev Agent workflow
