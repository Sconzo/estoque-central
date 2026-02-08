# Sprint Change Proposal
**Projeto:** Estoque Central
**Data:** 2025-02-07
**Agente:** PM (John)
**Usuário:** poly
**Status:** ✅ APROVADO

---

## 1. Issue Summary (Resumo do Problema)

### Problema
**Desktop Avatar Menu não implementa troca de contexto de empresa**

Durante testes manuais, foi descoberto que o layout desktop do sistema não possui a funcionalidade de troca de empresa via menu avatar, conforme especificado na Story 9.3 e requisitado no Journey 1 do PRD.

### Contexto
- **Descoberto:** Testes manuais por poly
- **Story afetada:** 9.3 (marcada como "completed" mas incompleta)
- **Discrepância:** Documentação afirma desktop implementado, código não reflete isso

### Evidência
- **Mobile (linha 70-71):** Usa `<app-user-avatar-menu>` ✅ Funciona
- **Desktop (linhas 151-183):** Menu antigo inline ❌ Sem troca de empresa
- **Componente existe:** `UserAvatarMenuComponent` implementado e funcional
- **Backend funciona:** `/api/users/me/context` operacional

### Impacto Imediato
- ❌ Journey 1 (Joaquin - multi-empresa) quebrado no desktop
- ❌ Inconsistência UX: mobile funciona, desktop não
- ❌ Epic 9 tecnicamente incompleto
- ❌ PRD não cumprido integralmente

---

## 2. Impact Analysis (Análise de Impacto)

### Epic Impact
**Epic 9: Seleção e Troca de Contexto Multi-Empresa**
- Status: ⚠️ Parcialmente completo (mobile 100%, desktop 0%)
- Story 9.3: Precisa correção desktop
- Épicos futuros: Não afetados criticamente

### Artifact Conflicts

| Artefato | Status | Conflito |
|----------|--------|----------|
| **PRD** | ⚠️ | Journey 1 quebrado - especifica "menu avatar com troca" |
| **Arquitetura** | ✅ | Nenhum conflito |
| **UI/UX** | ⚠️ | Inconsistência desktop vs mobile (UX19) |
| **Story 9.3** | ⚠️ | Documentação incorreta |
| **Testes** | ⚠️ | E2E desktop podem estar faltando |

### Technical Impact
- ✅ Backend: Funcional
- ✅ Services: Funcionais
- ✅ Componente: Existe e funciona
- ❌ Template: Desktop usando código antigo

---

## 3. Recommended Approach (Abordagem Recomendada)

### ⭐ Opção Selecionada: Direct Adjustment (Ajuste Direto)

**Descrição:** Substituir menu desktop antigo pelo componente `UserAvatarMenuComponent` existente.

**Justificativa:**
- ✅ **Menor esforço:** 30 min - 1 hora
- ✅ **Menor risco:** Componente já validado no mobile
- ✅ **Maior valor:** Completa Epic 9, entrega Journey 1
- ✅ **Sustentabilidade:** Código unificado (mobile + desktop)
- ✅ **Moral:** Não descarta trabalho, correção rápida

**Estimativa:**
- **Esforço:** 🟢 LOW (1-2 horas total)
- **Risco:** 🟢 LOW
- **Impacto no timeline:** Zero

**Alternativas rejeitadas:**
- ❌ Rollback: Alto esforço, alto risco, valor negativo
- ❌ Redefinir MVP: Overhead desnecessário
- ❌ Criar componente separado: Duplicação de código

---

## 4. Detailed Change Proposals (Propostas de Mudança Detalhadas)

### Mudança 1: Código - main-layout.component.html

**Arquivo:** `frontend/src/app/shared/layouts/main-layout/main-layout.component.html`

**OLD (linhas 151-183):**
```html
<button
  mat-icon-button
  [matMenuTriggerFor]="userMenu"
  aria-label="Menu do usuário">
  @if (currentUser().pictureUrl) {
    <img [src]="currentUser().pictureUrl" class="user-avatar">
  } @else {
    <mat-icon>account_circle</mat-icon>
  }
</button>
<mat-menu #userMenu="matMenu">
  <div class="user-menu-header">
    <strong>{{ currentUser().nome }}</strong>
    <small>{{ currentUser().email }}</small>
  </div>
  <mat-divider></mat-divider>
  <button mat-menu-item>
    <mat-icon>person</mat-icon>
    <span>Perfil</span>
  </button>
  <button mat-menu-item>
    <mat-icon>settings</mat-icon>
    <span>Configurações</span>
  </button>
  <mat-divider></mat-divider>
  <button mat-menu-item (click)="logout()">
    <mat-icon>logout</mat-icon>
    <span>Sair</span>
  </button>
</mat-menu>
```

**NEW:**
```html
<!-- Story 9.3: User avatar menu (desktop) -->
<app-user-avatar-menu></app-user-avatar-menu>
```

**Rationale:** Unifica código mobile e desktop, entrega funcionalidade completa de troca de empresa conforme Story 9.3 e Journey 1 do PRD.

---

### Mudança 2: Documentação - Story 9.3

**Arquivo:** `docs/stories/9-3-frontend-avatar-menu-context.md`

**Adicionar à seção "Implementation Summary":**

```markdown
### Correção Desktop (2025-02-07)

**Issue Identificada:**
Durante testes manuais, descobriu-se que o desktop toolbar estava usando menu avatar simplificado (linhas 151-183) ao invés do `UserAvatarMenuComponent` completo.

**Correção Aplicada:**
- Removido: Menu inline antigo (linhas 151-183)
- Adicionado: `<app-user-avatar-menu></app-user-avatar-menu>`
- Verificado: Desktop e mobile agora usam mesmo componente

**Resultado:**
- ✅ Desktop avatar menu mostra lista de empresas
- ✅ Troca de contexto funciona em < 500ms
- ✅ Consistência UX entre mobile e desktop
- ✅ Journey 1 (Joaquin) funcional em todas plataformas
```

**Rationale:** Corrigir documentação para refletir estado real e histórico de correção.

---

## 5. Implementation Handoff (Plano de Implementação)

### Change Scope Classification
🟢 **MINOR** - Pode ser implementado diretamente pela equipe de desenvolvimento

### Handoff Recipients

**Primary: Dev Agent**
- **Responsabilidade:** Implementar correção de código
- **Entregáveis:**
  - ✅ Código atualizado: `main-layout.component.html`
  - ✅ Testes manuais: Desktop + Mobile
  - ✅ Evidência de testes (screenshots/vídeo)
  - ✅ Commit com mensagem descritiva
- **Tempo estimado:** 1-2 horas

**Secondary: PM Agent (poly)**
- **Responsabilidade:** Validar e aprovar correção
- **Entregáveis:**
  - ✅ Aprovação da implementação
  - ✅ Validação Journey 1 funcionando
  - ✅ Sign-off Story 9.3 completa
- **Tempo estimado:** 15-30 min

**Optional: TEA Agent**
- **Responsabilidade:** Adicionar testes E2E desktop
- **Tempo estimado:** 30 min - 1 hora

### Implementation Plan (4 Fases)

**Fase 1: Código (30 min - 1h)**
1. Abrir `frontend/src/app/shared/layouts/main-layout/main-layout.component.html`
2. Localizar linhas 151-183 (desktop user menu antigo)
3. Remover todo o bloco:
   - `<button mat-icon-button [matMenuTriggerFor]="userMenu">...</button>`
   - `<mat-menu #userMenu="matMenu">...</mat-menu>`
4. Adicionar no lugar: `<app-user-avatar-menu></app-user-avatar-menu>`
5. Verificar que `main-layout.component.ts` já importa `UserAvatarMenuComponent`

**Fase 2: Testes (15-30 min)**
1. **Teste desktop (>1024px):**
   - Login com usuário multi-empresa
   - Clicar avatar no toolbar
   - Verificar menu completo com lista de empresas
   - Trocar para outra empresa
   - Confirmar troca em < 500ms
   - Verificar snackbar de sucesso
2. **Teste mobile (<768px):**
   - Repetir testes acima
   - Confirmar zero regressão
3. Capturar evidência (screenshots/vídeo)

**Fase 3: Documentação (15 min)**
1. Abrir `docs/stories/9-3-frontend-avatar-menu-context.md`
2. Adicionar seção "Correção Desktop (2025-02-07)" conforme especificado acima
3. Salvar alterações

**Fase 4: Commit & Deploy (10 min)**
1. Commit com mensagem:
```bash
git add frontend/src/app/shared/layouts/main-layout/main-layout.component.html
git add docs/stories/9-3-frontend-avatar-menu-context.md
git commit -m "fix(frontend): integrate UserAvatarMenu in desktop layout

- Replace simplified desktop user menu with full UserAvatarMenuComponent
- Ensures desktop users can switch company context via avatar menu
- Fixes Story 9.3 desktop implementation gap
- Maintains consistency between mobile and desktop UX

Refs: Epic 9, Story 9.3, Journey 1 (Joaquin)

Co-Authored-By: Claude Sonnet 4.5 <noreply@anthropic.com>"
```
2. Push para branch de desenvolvimento
3. Solicitar validação PM (poly)
4. Deploy staging → produção após aprovação

### Success Criteria
- ✅ Desktop avatar menu mostra lista de empresas
- ✅ Troca de contexto < 500ms
- ✅ Snackbar de sucesso aparece
- ✅ Mobile continua funcionando (zero regressão)
- ✅ Código unificado (mobile + desktop)
- ✅ Documentação atualizada

---

## 6. Approval & Sign-off

**Aprovado por:** poly (PM)
**Data de aprovação:** 2025-02-07
**Status:** ✅ APROVADO PARA IMPLEMENTAÇÃO

**Próximos passos:**
1. Handoff para Dev Agent
2. Implementação conforme plano de 4 fases
3. Validação por PM (poly)
4. Deploy produção

---

## 📊 Summary

| Item | Valor |
|------|-------|
| **Escopo** | Minor (correção localizada) |
| **Esforço** | 1-2 horas total |
| **Risco** | LOW |
| **Impacto MVP** | Crítico (desbloqueia Journey 1) |
| **Timeline** | Sem atraso |
| **Agent** | Dev (implementa) + PM (valida) |
| **Status** | ✅ APROVADO |

---

**Documento gerado por:** PM Agent (John)
**Workflow:** Correct Course (Sprint Change Management)
**BMad Method versão:** 6.0.0-alpha.16
