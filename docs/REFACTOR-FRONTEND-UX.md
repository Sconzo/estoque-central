# [REFACTOR] Implementar UX Design Specification no Frontend

## 📋 Contexto

O projeto possui uma **UX Design Specification completa** que define toda a identidade visual, componentes, padrões de UX e acessibilidade do Estoque Central ERP. Esta task cobre a refatoração do frontend Angular existente para seguir essas diretrizes.

**Documentação de Referência:**
- `docs/ux-design-specification.md` - Especificação completa (2.648 linhas)
- `docs/ux-color-themes-purple.html` - Visualizador do tema roxo escolhido
- `docs/dashboard-mockup-purple.html` - Mockup do dashboard

## 🎯 Objetivos

1. ✅ Implementar Angular Material com tema customizado (Deep Purple Luxury)
2. ✅ Refatorar componentes existentes para usar Angular Material
3. ✅ Aplicar padrões de UX consistentes (buttons, forms, modals, feedback)
4. ✅ Implementar responsividade (mobile/tablet/desktop)
5. ✅ Garantir acessibilidade WCAG AA
6. ✅ Criar biblioteca de componentes compartilhados

## 📦 Fases de Implementação

### Fase 1: Setup e Fundação (Sprint 1 - 1 semana)

**1.1 Instalar Angular Material**

```bash
ng add @angular/material
```

Quando perguntado:
- **Prebuilt theme:** Custom
- **Typography:** Yes
- **Animations:** Yes

**1.2 Criar Tema Customizado**

Criar arquivo `src/styles/theme.scss`:

```scss
@use '@angular/material' as mat;

// Paleta Primary (Deep Purple)
$estoque-primary: mat.define-palette((
  50: #F3E5F5,
  100: #E1BEE7,
  200: #CE93D8,
  300: #BA68C8,
  400: #AB47BC,
  500: #6A1B9A,  // Primary
  600: #8E24AA,
  700: #7B1FA2,
  800: #6A1B9A,
  900: #4A148C,
  contrast: (
    50: #212121,
    100: #212121,
    200: #212121,
    300: #FFFFFF,
    400: #FFFFFF,
    500: #FFFFFF,
    600: #FFFFFF,
    700: #FFFFFF,
    800: #FFFFFF,
    900: #FFFFFF,
  )
), 500);

// Paleta Accent (Dourado/Âmbar)
$estoque-accent: mat.define-palette((
  50: #FFF8E1,
  100: #FFECB3,
  200: #FFE082,
  300: #FFD54F,
  400: #FFCA28,
  500: #F9A825,  // Accent
  600: #FFB300,
  700: #FFA000,
  800: #FF8F00,
  900: #FF6F00,
  contrast: (
    50: #212121,
    100: #212121,
    200: #212121,
    300: #212121,
    400: #212121,
    500: #212121,
    600: #212121,
    700: #212121,
    800: #FFFFFF,
    900: #FFFFFF,
  )
), 500);

// Paleta Warn (Vermelho)
$estoque-warn: mat.define-palette(mat.$red-palette, 800);

// Criar tema
$estoque-theme: mat.define-light-theme((
  color: (
    primary: $estoque-primary,
    accent: $estoque-accent,
    warn: $estoque-warn,
  ),
  typography: mat.define-typography-config(
    $font-family: 'Roboto, sans-serif',
  ),
  density: 0,
));

// Aplicar tema
@include mat.all-component-themes($estoque-theme);
```

**1.3 Atualizar `src/styles.scss`**

```scss
@use './styles/theme';

// Global styles
html, body {
  height: 100%;
  margin: 0;
  font-family: Roboto, "Helvetica Neue", sans-serif;
}

// Cores semânticas customizadas
.success-text { color: #2E7D32; }
.warning-text { color: #F9A825; }
.error-text { color: #C62828; }
.info-text { color: #0277BD; }

.success-snackbar {
  background: #2E7D32 !important;
  color: white !important;
}

.error-snackbar {
  background: #C62828 !important;
  color: white !important;
}

.warning-snackbar {
  background: #F9A825 !important;
  color: #212121 !important;
}
```

**1.4 Configurar Locale pt-BR**

Em `src/app/app.config.ts` (ou `app.module.ts` se não for standalone):

```typescript
import { LOCALE_ID } from '@angular/core';
import { registerLocaleData } from '@angular/common';
import localePt from '@angular/common/locales/pt';
import { MAT_DATE_LOCALE } from '@angular/material/core';

registerLocaleData(localePt);

export const appConfig: ApplicationConfig = {
  providers: [
    { provide: LOCALE_ID, useValue: 'pt-BR' },
    { provide: MAT_DATE_LOCALE, useValue: 'pt-BR' },
    // ... outros providers
  ]
};
```

**Checklist Fase 1:**

- [ ] Angular Material instalado
- [ ] `theme.scss` criado com paleta customizada
- [ ] `styles.scss` atualizado com cores semânticas
- [ ] Locale pt-BR configurado
- [ ] Testar: botão `<button mat-raised-button color="primary">Teste</button>` deve aparecer roxo

---

### Fase 2: Componentes Compartilhados (Sprint 1 - continuação)

**2.1 Criar Estrutura de Shared Components**

```bash
mkdir -p src/app/shared/components
cd src/app/shared/components

# Criar componentes base
ng g c buttons/primary-button --skip-tests
ng g c feedback/loading-spinner --skip-tests
ng g c feedback/metric-card --skip-tests
```

**2.2 Implementar Componentes Base**

**`primary-button.component.ts`:**
```typescript
import { Component, Input, Output, EventEmitter } from '@angular/core';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-primary-button',
  standalone: true,
  imports: [CommonModule, MatButtonModule, MatIconModule, MatProgressSpinnerModule],
  template: `
    <button
      mat-raised-button
      [color]="color"
      [disabled]="disabled || loading"
      (click)="onClick.emit()"
      [attr.aria-label]="ariaLabel"
      [attr.aria-busy]="loading">

      <mat-spinner *ngIf="loading" diameter="16" aria-hidden="true"></mat-spinner>
      <mat-icon *ngIf="icon && !loading" aria-hidden="true">{{ icon }}</mat-icon>
      <span>{{ loading ? loadingText : label }}</span>
    </button>
  `,
  styles: [`
    button {
      min-height: 40px;

      mat-spinner {
        display: inline-block;
        margin-right: 8px;
      }

      mat-icon {
        margin-right: 8px;
      }
    }
  `]
})
export class PrimaryButtonComponent {
  @Input() label: string = 'Confirmar';
  @Input() loadingText: string = 'Processando...';
  @Input() icon?: string;
  @Input() color: 'primary' | 'accent' | 'warn' = 'primary';
  @Input() disabled: boolean = false;
  @Input() loading: boolean = false;
  @Input() ariaLabel?: string;
  @Output() onClick = new EventEmitter<void>();
}
```

**`metric-card.component.ts`:**
```typescript
import { Component, Input } from '@angular/core';
import { MatCardModule } from '@angular/material/card';
import { MatIconModule } from '@angular/material/icon';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-metric-card',
  standalone: true,
  imports: [CommonModule, MatCardModule, MatIconModule],
  template: `
    <mat-card
      class="metric-card"
      [style.border-left]="'4px solid ' + color"
      role="region"
      [attr.aria-label]="title + ' metric'">

      <mat-card-header>
        <mat-icon [style.color]="color" aria-hidden="true">{{ icon }}</mat-icon>
        <mat-card-title>{{ title }}</mat-card-title>
      </mat-card-header>

      <mat-card-content>
        <div class="metric-value" [style.color]="color" aria-live="polite">
          {{ value }}
        </div>
        <div
          class="metric-change"
          [class.positive]="changePercent > 0"
          [class.negative]="changePercent < 0"
          *ngIf="changePercent !== undefined">
          <span [attr.aria-label]="'Mudança do período anterior: ' + changePercent + '%'">
            {{ changePercent > 0 ? '↑' : '↓' }} {{ changePercent > 0 ? '+' : '' }}{{ changePercent }}%
          </span>
        </div>
      </mat-card-content>
    </mat-card>
  `,
  styles: [`
    .metric-card {
      margin: 16px;

      mat-card-header {
        display: flex;
        align-items: center;
        gap: 8px;
        margin-bottom: 16px;

        mat-icon {
          font-size: 24px;
          width: 24px;
          height: 24px;
        }
      }

      .metric-value {
        font-size: 32px;
        font-weight: 600;
        margin-bottom: 8px;
      }

      .metric-change {
        font-size: 14px;

        &.positive { color: #2E7D32; }
        &.negative { color: #C62828; }
      }
    }
  `]
})
export class MetricCardComponent {
  @Input() title!: string;
  @Input() value!: string;
  @Input() changePercent?: number;
  @Input() icon: string = 'info';
  @Input() color: string = '#6A1B9A';
}
```

**Checklist Fase 2:**

- [ ] Estrutura `shared/components/` criada
- [ ] `PrimaryButtonComponent` implementado
- [ ] `MetricCardComponent` implementado
- [ ] Componentes testados em uma página de exemplo

---

### Fase 3: Refatorar Telas Existentes (Sprint 2-3 - 2 semanas)

**3.1 Dashboard**

**ANTES:**
```html
<div class="dashboard">
  <div class="metric">
    <h3>Vendas Hoje</h3>
    <p>R$ 12.450</p>
  </div>
</div>
```

**DEPOIS:**
```html
<div class="dashboard">
  <app-metric-card
    title="Vendas Hoje"
    value="R$ 12.450"
    [changePercent]="15"
    icon="point_of_sale"
    color="#6A1B9A">
  </app-metric-card>

  <app-metric-card
    title="Estoque Crítico"
    value="3 itens"
    icon="inventory_2"
    color="#C62828">
  </app-metric-card>
</div>
```

**3.2 Refatorar Buttons**

**ANTES:**
```html
<button class="btn btn-primary" (click)="onSave()">Salvar</button>
<button class="btn btn-secondary" (click)="onCancel()">Cancelar</button>
```

**DEPOIS:**
```html
<div class="button-group">
  <button mat-stroked-button (click)="onCancel()">Cancelar</button>
  <app-primary-button
    label="Salvar"
    icon="save"
    [loading]="isSaving"
    (onClick)="onSave()">
  </app-primary-button>
</div>
```

**3.3 Refatorar Forms**

**ANTES:**
```html
<form>
  <label>Nome do Produto</label>
  <input type="text" [(ngModel)]="product.name">

  <label>Quantidade</label>
  <input type="number" [(ngModel)]="product.quantity">

  <button type="submit">Salvar</button>
</form>
```

**DEPOIS:**
```html
<form [formGroup]="productForm" (ngSubmit)="onSubmit()">
  <mat-form-field appearance="outline">
    <mat-label>Nome do Produto <span class="required">*</span></mat-label>
    <input matInput formControlName="name" required>
    <mat-error *ngIf="productForm.get('name')?.hasError('required')">
      Nome é obrigatório
    </mat-error>
  </mat-form-field>

  <mat-form-field appearance="outline">
    <mat-label>Quantidade</mat-label>
    <input matInput type="number" formControlName="quantity" required>
    <mat-hint>Estoque disponível: 120 unidades</mat-hint>
    <mat-error *ngIf="productForm.get('quantity')?.hasError('required')">
      Quantidade é obrigatória
    </mat-error>
    <mat-error *ngIf="productForm.get('quantity')?.hasError('min')">
      Quantidade deve ser maior que 0
    </mat-error>
  </mat-form-field>

  <div class="form-actions">
    <button mat-stroked-button type="button" (click)="onCancel()">Cancelar</button>
    <button mat-raised-button color="primary" type="submit" [disabled]="!productForm.valid">
      <mat-icon>save</mat-icon>
      Salvar
    </button>
  </div>
</form>
```

**3.4 Refatorar Feedback/Notificações**

**ANTES:**
```typescript
alert('Produto salvo com sucesso!');
```

**DEPOIS:**
```typescript
import { MatSnackBar } from '@angular/material/snack-bar';

constructor(private snackBar: MatSnackBar) {}

onSaveSuccess() {
  this.snackBar.open('✓ Produto salvo com sucesso!', 'Fechar', {
    duration: 3000,
    panelClass: ['success-snackbar'],
    horizontalPosition: 'end',
    verticalPosition: 'top'
  });
}

onSaveError(error: string) {
  this.snackBar.open(`✕ Erro ao salvar: ${error}`, 'Tentar Novamente', {
    duration: 5000,
    panelClass: ['error-snackbar'],
    horizontalPosition: 'end',
    verticalPosition: 'top'
  }).onAction().subscribe(() => {
    this.onSave(); // Retry
  });
}
```

**Checklist Fase 3:**

- [ ] Dashboard refatorado com MetricCards
- [ ] Todos os botões usando `mat-raised-button`, `mat-stroked-button` ou `app-primary-button`
- [ ] Todos os forms usando `mat-form-field` com `appearance="outline"`
- [ ] Validações usando `mat-error`
- [ ] Alerts/toasts substituídos por `MatSnackBar`
- [ ] Loading states usando `MatProgressSpinner` ou `MatProgressBar`

---

### Fase 4: Navigation & Responsividade (Sprint 3 - continuação)

**4.1 Implementar Navigation Responsiva**

**`app.component.html`:**
```html
<!-- Desktop -->
<mat-sidenav-container class="app-container" *ngIf="isDesktop">
  <mat-sidenav mode="side" opened class="app-sidenav">
    <app-main-nav></app-main-nav>
  </mat-sidenav>

  <mat-sidenav-content>
    <mat-toolbar color="primary">
      <span>Estoque Central</span>
      <span class="spacer"></span>
      <button mat-icon-button [matMenuTriggerFor]="userMenu" aria-label="Menu do usuário">
        <mat-icon>account_circle</mat-icon>
      </button>
      <mat-menu #userMenu="matMenu">
        <button mat-menu-item>
          <mat-icon>settings</mat-icon>
          <span>Configurações</span>
        </button>
        <button mat-menu-item (click)="logout()">
          <mat-icon>logout</mat-icon>
          <span>Sair</span>
        </button>
      </mat-menu>
    </mat-toolbar>

    <div class="content">
      <router-outlet></router-outlet>
    </div>
  </mat-sidenav-content>
</mat-sidenav-container>

<!-- Mobile -->
<div class="mobile-container" *ngIf="!isDesktop">
  <mat-toolbar color="primary">
    <button mat-icon-button (click)="sidenav.toggle()" aria-label="Abrir menu">
      <mat-icon>menu</mat-icon>
    </button>
    <span>{{ pageTitle }}</span>
  </mat-toolbar>

  <mat-sidenav-container>
    <mat-sidenav #sidenav mode="over">
      <app-main-nav (itemClick)="sidenav.close()"></app-main-nav>
    </mat-sidenav>

    <mat-sidenav-content>
      <div class="content">
        <router-outlet></router-outlet>
      </div>
    </mat-sidenav-content>
  </mat-sidenav-container>

  <!-- Bottom Navigation (opcional) -->
  <mat-toolbar class="bottom-nav">
    <button mat-button routerLink="/dashboard" routerLinkActive="active">
      <mat-icon>dashboard</mat-icon>
      <span>Dashboard</span>
    </button>
    <button mat-button routerLink="/vendas" routerLinkActive="active">
      <mat-icon>point_of_sale</mat-icon>
      <span>Vendas</span>
    </button>
    <button mat-button routerLink="/estoque" routerLinkActive="active">
      <mat-icon>inventory_2</mat-icon>
      <span>Estoque</span>
    </button>
  </mat-toolbar>
</div>
```

**`app.component.ts`:**
```typescript
import { BreakpointObserver, Breakpoints } from '@angular/cdk/layout';

export class AppComponent {
  isDesktop = true;
  pageTitle = 'Dashboard';

  constructor(private breakpointObserver: BreakpointObserver) {
    this.breakpointObserver.observe([Breakpoints.Handset])
      .subscribe(result => {
        this.isDesktop = !result.matches;
      });
  }
}
```

**4.2 CSS Responsivo**

```scss
// Breakpoints (Material Design)
$xs: 600px;
$sm: 960px;
$md: 1280px;
$lg: 1920px;

.dashboard {
  display: grid;
  gap: 16px;
  padding: 16px;

  // Mobile
  grid-template-columns: 1fr;

  // Tablet
  @media (min-width: $xs) {
    grid-template-columns: repeat(2, 1fr);
  }

  // Desktop
  @media (min-width: $md) {
    grid-template-columns: repeat(4, 1fr);
  }
}

// Forms full-width em mobile
mat-form-field {
  @media (min-width: $sm) {
    width: 300px;
  }

  @media (max-width: $sm - 1) {
    width: 100%;
  }
}

// Touch targets em mobile/tablet
@media (max-width: $md - 1) {
  button {
    min-height: 48px;
    min-width: 48px;
  }
}
```

**Checklist Fase 4:**

- [ ] Navigation sidebar para desktop (sempre visível)
- [ ] Navigation hamburger menu para mobile (overlay)
- [ ] Bottom navigation opcional para mobile
- [ ] Breakpoint observer implementado
- [ ] CSS responsivo aplicado (grid, forms, touch targets)
- [ ] Testar em: Desktop (1920px), Laptop (1366px), Tablet (768px), Mobile (375px)

---

### Fase 5: Acessibilidade (Sprint 4 - 1 semana)

**5.1 ARIA Labels**

**Botões com apenas ícone:**
```html
<!-- ❌ Incorreto -->
<button mat-icon-button>
  <mat-icon>delete</mat-icon>
</button>

<!-- ✅ Correto -->
<button mat-icon-button aria-label="Excluir produto">
  <mat-icon>delete</mat-icon>
</button>
```

**Forms:**
```html
<mat-form-field appearance="outline">
  <mat-label>Quantidade</mat-label>
  <input
    matInput
    type="number"
    formControlName="quantity"
    aria-label="Quantidade do produto"
    aria-describedby="quantity-hint"
    aria-required="true">
  <mat-hint id="quantity-hint">Estoque disponível: 120 unidades</mat-hint>
  <mat-error role="alert">Quantidade é obrigatória</mat-error>
</mat-form-field>
```

**Modals:**
```html
<div
  role="dialog"
  aria-labelledby="dialog-title"
  aria-describedby="dialog-description"
  aria-modal="true">

  <h2 id="dialog-title" mat-dialog-title>Confirmar Exclusão</h2>
  <mat-dialog-content id="dialog-description">
    Tem certeza que deseja excluir este produto?
  </mat-dialog-content>

  <mat-dialog-actions align="end">
    <button mat-stroked-button mat-dialog-close>Cancelar</button>
    <button mat-raised-button color="warn" (click)="onDelete()">Excluir</button>
  </mat-dialog-actions>
</div>
```

**5.2 Keyboard Navigation**

**Focus visível:**
```scss
// Mostrar outline apenas quando navegando por teclado
*:focus {
  outline: 2px solid #6A1B9A;
  outline-offset: 2px;
}

body:not(.user-is-tabbing) *:focus {
  outline: none;
}
```

**Detectar uso de teclado:**
```typescript
// app.component.ts
ngOnInit() {
  document.body.addEventListener('keydown', (e) => {
    if (e.key === 'Tab') {
      document.body.classList.add('user-is-tabbing');
    }
  });

  document.body.addEventListener('mousedown', () => {
    document.body.classList.remove('user-is-tabbing');
  });
}
```

**5.3 Skip Links**

```html
<!-- Primeiro elemento do body -->
<a href="#main-content" class="skip-link">
  Pular para conteúdo principal
</a>

<main id="main-content" tabindex="-1">
  <router-outlet></router-outlet>
</main>
```

```scss
.skip-link {
  position: absolute;
  top: -40px;
  left: 0;
  background: #6A1B9A;
  color: white;
  padding: 8px;
  z-index: 100;
  text-decoration: none;

  &:focus {
    top: 0;
  }
}
```

**5.4 Color Contrast Validation**

Usar ferramenta: https://webaim.org/resources/contrastchecker/

**Validar:**
- ✅ `#6A1B9A` (roxo) em `#FFFFFF` (branco) = 8.2:1 - AAA ✓
- ✅ `#212121` (texto) em `#FFFFFF` (branco) = 16.1:1 - AAA ✓
- ✅ `#757575` (secundário) em `#FFFFFF` = 4.6:1 - AA ✓
- ❌ `#F9A825` (dourado) em `#FFFFFF` = 2.1:1 - FAIL

**Correção:**
```html
<!-- ❌ Incorreto: dourado como texto -->
<p style="color: #F9A825;">Atenção!</p>

<!-- ✅ Correto: dourado como background -->
<div style="background: #F9A825; color: #212121;">
  ⚠ Estoque baixo
</div>

<!-- ✅ Correto: dourado como ícone -->
<mat-icon style="color: #F9A825;">warning</mat-icon>
```

**Checklist Fase 5:**

- [ ] Todos os botões com ícone têm `aria-label`
- [ ] Todos os forms têm `aria-describedby`, `aria-required`
- [ ] Todos os modals têm `aria-labelledby`, `aria-modal`
- [ ] Focus visível implementado (outline roxo)
- [ ] Skip links implementados
- [ ] Color contrast validado (nenhum FAIL)
- [ ] Testar navegação por teclado (Tab, Enter, Esc)
- [ ] Testar com screen reader (NVDA/JAWS no Windows, VoiceOver no Mac)

---

## 📊 Critérios de Aceitação

### Visual:
- [ ] Cor primária roxo `#6A1B9A` aplicada em todos os botões primários
- [ ] Cor accent dourado `#F9A825` usada apenas em warnings e destaques
- [ ] Typography Roboto em toda a aplicação
- [ ] Icons Material Icons (não emojis)
- [ ] Spacing consistente (8px base unit)

### Componentes:
- [ ] Todos os botões usando Material (`mat-raised-button`, `mat-stroked-button`, `mat-icon-button`)
- [ ] Todos os forms usando `mat-form-field` com `appearance="outline"`
- [ ] Todos os alerts/toasts usando `MatSnackBar`
- [ ] Todos os modals usando `MatDialog`
- [ ] Loading states usando `MatProgressSpinner` ou `MatProgressBar`

### Responsividade:
- [ ] Desktop (≥1280px): Sidebar permanente, layout 3-4 colunas
- [ ] Tablet (960-1279px): Sidebar dismissible, layout 2 colunas
- [ ] Mobile (<960px): Hamburger menu, layout 1 coluna, bottom nav

### Acessibilidade:
- [ ] Todos os elementos interativos navegáveis por teclado
- [ ] Todos os botões com ícone têm `aria-label`
- [ ] Todos os forms têm validação com `mat-error`
- [ ] Color contrast mínimo 4.5:1 (WCAG AA)
- [ ] Touch targets mínimo 48x48px em mobile

### Performance:
- [ ] Bundle size não aumentou significativamente (< +500KB)
- [ ] First Contentful Paint < 1.5s
- [ ] Time to Interactive < 3s
- [ ] Lighthouse score ≥ 90

---

## 📚 Referências

**Documentação Principal:**
- [UX Design Specification](./ux-design-specification.md)
- [Color Theme Visualizer](./ux-color-themes-purple.html)
- [Dashboard Mockup](./dashboard-mockup-purple.html)

**Angular Material:**
- [Components](https://material.angular.dev/components)
- [Theming Guide](https://material.angular.dev/guide/theming)
- [Accessibility](https://material.angular.dev/cdk/a11y/overview)

**WCAG 2.1:**
- [Guidelines](https://www.w3.org/WAI/WCAG21/quickref/)
- [Contrast Checker](https://webaim.org/resources/contrastchecker/)

**Material Design 3:**
- [Design System](https://m3.material.io/)
- [Color System](https://m3.material.io/styles/color/the-color-system/key-colors-tones)

---

## ⏱️ Estimativa de Esforço

| Fase | Descrição | Estimativa |
|------|-----------|-----------|
| **Fase 1** | Setup e Fundação | 4-8 horas |
| **Fase 2** | Componentes Compartilhados | 8-12 horas |
| **Fase 3** | Refatorar Telas Existentes | 40-60 horas |
| **Fase 4** | Navigation & Responsividade | 16-24 horas |
| **Fase 5** | Acessibilidade | 8-16 horas |
| **TOTAL** | **2-3 semanas** | **76-120 horas** |

**Nota:** Estimativa baseada em projeto com ~10-15 telas. Ajustar conforme necessário.

---

## 🚀 Getting Started

1. **Ler documentação:**
   - `docs/ux-design-specification.md` (completa)
   - Focar nas seções 7 (UX Patterns) e 9 (Implementation Guidance)

2. **Setup inicial:**
   ```bash
   # Instalar Angular Material
   ng add @angular/material

   # Criar tema customizado
   touch src/styles/theme.scss
   ```

3. **Começar pela Fase 1:**
   - Configurar tema roxo
   - Testar com um botão simples
   - Validar que tudo está funcionando antes de prosseguir

4. **Implementar incrementalmente:**
   - Não tentar refatorar tudo de uma vez
   - Começar com uma tela (ex: Dashboard)
   - Validar visualmente contra mockups
   - Repetir para outras telas

5. **Pedir ajuda se necessário:**
   - Documentação é extensa, mas pode ter dúvidas
   - Perguntar sobre decisões de UX específicas
   - Validar implementações antes de finalizar

---

## 📝 Notas Importantes

⚠️ **NÃO usar emojis como ícones** - O usuário especificou explicitamente para usar Material Icons

⚠️ **Sempre usar Material Icons:**
```html
<!-- ✅ Correto -->
<mat-icon>inventory_2</mat-icon>

<!-- ❌ Incorreto -->
<span>📦</span>
```

⚠️ **Color contrast:** Nunca usar dourado `#F9A825` como cor de texto em fundo branco (contraste insuficiente)

⚠️ **Touch targets:** Em mobile/tablet, SEMPRE usar mínimo 48x48px para elementos clicáveis

⚠️ **Locale:** Sempre usar `pt-BR` para datas e moeda (R$)

---

**Criado em:** 2025-12-14
**Autor:** poly
**Baseado em:** UX Design Specification v1.0
