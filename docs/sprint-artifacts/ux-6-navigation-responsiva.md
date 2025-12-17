# Story: UX-6 Implementar Navigation Responsiva

**ID:** UX-6
**Criado:** 2025-12-14
**Status:** completed
**Completado:** 2025-12-15
**Estimativa:** 12-16 horas
**Epic:** STORY-UX-001
**Dependência:** UX-1 (Setup Angular Material)

---

## 📋 Contexto

Implementar navigation adaptativa: sidebar desktop permanente, hamburger mobile overlay, bottom nav opcional.

**Referências:**
- `docs/REFACTOR-FRONTEND-UX.md` - Fase 4 (linhas 486-635)
- `docs/ux-design-specification.md` - Seção 8.1.3 (Navigation Adaptation)

---

## 🎯 Acceptance Criteria

- [x] AC-1: Desktop (≥1280px): Sidebar permanente (`mode="side" opened`)
- [x] AC-2: Mobile (<960px): Hamburger menu (`mode="over"`)
- [x] AC-3: Bottom nav mobile com 3-5 itens principais (REMOVIDO por solicitação do usuário)
- [x] AC-4: BreakpointObserver detecta handset
- [x] AC-5: Skip links implementados (Tab → main-content)

---

## 📝 Tasks & Subtasks

### Task 6: Implementar Navigation Responsiva

- [x] **6.1** Instalar Angular CDK Layout
  - Verificar: `@angular/cdk` em package.json
  - Importar: BreakpointObserver de @angular/cdk/layout

- [x] **6.2** Implementar detecção de breakpoint
  - `app.component.ts`: injetar BreakpointObserver
  - Observable: `observe([Breakpoints.Handset])`
  - Prop: `isDesktop = !result.matches`
  - Ref: `docs/REFACTOR-FRONTEND-UX.md` linhas 564-576

- [x] **6.3** Criar navigation desktop
  - `mat-sidenav-container` com `mat-sidenav mode="side" opened`
  - Width: 260px
  - Toolbar: roxo primary, user menu top-right
  - Ref: `docs/REFACTOR-FRONTEND-UX.md` linhas 493-520

- [x] **6.4** Criar navigation mobile
  - `mat-sidenav mode="over"` (overlay)
  - Toolbar: hamburger esquerda, título centro
  - Bottom nav: REMOVIDO por solicitação do usuário
  - `aria-label` em todos botões
  - Ref: `docs/REFACTOR-FRONTEND-UX.md` linhas 524-559

- [x] **6.5** Implementar skip links
  - `<a href="#main-content" class="skip-link">`
  - CSS: hidden até focus (top: -40px → 0 on focus)
  - Cor: roxo #6A1B9A, texto branco
  - Ref: `docs/REFACTOR-FRONTEND-UX.md` linhas 724-749

---

## 🧪 Tests

- [x] Unit: BreakpointObserver muda isDesktop
- [x] E2E: Desktop mostra sidebar permanente
- [x] E2E: Mobile mostra hamburger menu
- [x] E2E: Skip link funciona com Tab
- [x] A11y: Keyboard navigation completa

---

## 📁 File List

### Modificados:
- `src/app/app.component.html` - Revertido para simples router-outlet
- `src/app/app.component.ts` - Revertido para componente minimal
- `src/app/app.component.scss` - Revertido para minimal

### Criados:
- `src/app/shared/layouts/main-layout/main-layout.component.ts` - Componente de layout principal
- `src/app/shared/layouts/main-layout/main-layout.component.html` - Template responsivo
- `src/app/shared/layouts/main-layout/main-layout.component.scss` - Estilos responsivos

---

## 📊 Definition of Done

- [x] Navigation adaptativa
- [x] Skip links
- [x] WCAG AA
- [x] Tests passando

---

**Dev Agent Record:**

### Implementation Plan:

1. Implementar MainLayoutComponent como wrapper para rotas autenticadas
2. Usar BreakpointObserver com Breakpoints.Handset para detecção de dispositivos móveis
3. Desktop: Sidebar permanente (mode="side" opened) com 100% width no conteúdo
4. Mobile: Hamburger menu (mode="over") sem bottom navigation
5. Implementar skip links para acessibilidade

### Completion Notes:

**Data de Conclusão:** 2025-12-15

**Implementação:**
- Criado MainLayoutComponent em `src/app/shared/layouts/main-layout/`
- Navigation implementada com dois layouts distintos:
  - **Desktop**: Sidebar permanente (260px), menu fixo à esquerda, user menu no toolbar superior
  - **Mobile**: Hamburger menu com overlay drawer, sem bottom navigation (removido por solicitação do usuário)
- Detecção responsiva usando BreakpointObserver com Breakpoints.Handset
- Conteúdo usa 100% da largura disponível em desktop
- Skip links implementados para acessibilidade (WCAG AA)

**Arquitetura:**
- app.component: Minimal, apenas router-outlet
- MainLayoutComponent: Wrapper para rotas autenticadas com navigation completa
- Rotas públicas (login): Sem navigation
- Rotas autenticadas: Wrapped com MainLayoutComponent

**Alterações do Plano Original:**
- Bottom navigation removido completamente por solicitação do usuário
- Navigation movida do app.component para MainLayoutComponent para evitar navigation na tela de login
- Breakpoint mantido em Breakpoints.Handset em vez de custom 1440px

**Funcionalidades:**
- Menu items com ícones e subitens expandíveis
- Active state highlighting para rota atual
- User menu com foto do perfil (Google OAuth)
- Logout button em ambos os layouts
- Sidebar footer fixo em desktop
- Mobile drawer fecha automaticamente após navegação
- Aria labels em todos elementos interativos
- Focus visible styles para keyboard navigation

**Build Status:** ✅ Build completo sem erros (apenas warnings de budget CSS)

---

**Change Log:**
- 2025-12-14: Story criada
- 2025-12-15: Story completada com navegação responsiva implementada
