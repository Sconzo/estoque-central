# 🎨 Exemplos de Uso - Design System

## Exemplos Práticos de Implementação

### 📦 Cards de Módulos

```scss
@import './theme';

.module-card {
  background: $card-bg;
  border: 1px solid $card-border;
  border-radius: $radius-lg;
  padding: $spacing-6;
  box-shadow: $shadow-base;
  transition: all $transition-base $ease-out;

  &:hover {
    box-shadow: $shadow-md;
    transform: translateY(-2px);
  }

  // Produtos
  &.products {
    border-left: 4px solid $products-primary;
  }

  // Estoque
  &.inventory {
    border-left: 4px solid $inventory-primary;
  }

  // Vendas
  &.sales {
    border-left: 4px solid $sales-primary;
  }

  // Clientes
  &.customers {
    border-left: 4px solid $customers-primary;
  }
}
```

### 🔘 Botões

```scss
@import './theme';

// Botão Primário
.btn-primary {
  background: $btn-primary-bg;
  color: $btn-primary-text;
  padding: $spacing-3 $spacing-6;
  border-radius: $radius-base;
  font-weight: $font-medium;
  border: none;
  cursor: pointer;
  transition: all $transition-base $ease-out;

  &:hover {
    background: $btn-primary-bg-hover;
    box-shadow: $shadow-md;
  }

  &:active {
    background: $btn-primary-bg-active;
    transform: scale(0.98);
  }

  &:disabled {
    background: $bg-disabled;
    color: $text-disabled;
    cursor: not-allowed;
  }
}

// Botão Secundário
.btn-secondary {
  background: $btn-secondary-bg;
  color: $btn-secondary-text;
  padding: $spacing-3 $spacing-6;
  border-radius: $radius-base;
  font-weight: $font-medium;
  border: 1px solid $border-medium;
  cursor: pointer;
  transition: all $transition-base $ease-out;

  &:hover {
    background: $btn-secondary-bg-hover;
    border-color: $border-dark;
  }

  &:active {
    background: $btn-secondary-bg-active;
  }
}

// Botão Perigo
.btn-danger {
  background: $btn-danger-bg;
  color: $btn-danger-text;
  padding: $spacing-3 $spacing-6;
  border-radius: $radius-base;
  font-weight: $font-medium;
  border: none;
  cursor: pointer;
  transition: all $transition-base $ease-out;

  &:hover {
    background: $btn-danger-bg-hover;
  }

  &:active {
    background: $btn-danger-bg-active;
  }
}
```

### 📝 Inputs e Forms

```scss
@import './theme';

.form-input {
  width: 100%;
  padding: $spacing-3;
  border: 1px solid $input-border;
  border-radius: $radius-base;
  background: $input-bg;
  color: $input-text;
  font-size: $text-base;
  transition: all $transition-base $ease-out;

  &::placeholder {
    color: $input-placeholder;
  }

  &:hover {
    border-color: $input-border-hover;
  }

  &:focus {
    outline: none;
    border-color: $input-border-focus;
    box-shadow: 0 0 0 3px rgba($primary-500, 0.1);
  }

  &:disabled {
    background: $input-disabled-bg;
    color: $input-disabled-text;
    cursor: not-allowed;
  }

  &.error {
    border-color: $border-error;

    &:focus {
      box-shadow: 0 0 0 3px rgba($error-500, 0.1);
    }
  }
}

.form-label {
  display: block;
  margin-bottom: $spacing-2;
  font-size: $text-sm;
  font-weight: $font-medium;
  color: $text-primary;
}

.form-error {
  margin-top: $spacing-2;
  font-size: $text-sm;
  color: $error-500;
}
```

### 🏷️ Badges e Tags

```scss
@import './theme';

.badge {
  display: inline-flex;
  align-items: center;
  padding: $spacing-1 $spacing-3;
  border-radius: $radius-full;
  font-size: $text-xs;
  font-weight: $font-semibold;

  &.success {
    background: $badge-success-bg;
    color: $badge-success-text;
  }

  &.warning {
    background: $badge-warning-bg;
    color: $badge-warning-text;
  }

  &.error {
    background: $badge-error-bg;
    color: $badge-error-text;
  }

  &.info {
    background: $badge-info-bg;
    color: $badge-info-text;
  }
}
```

### 📊 Tabelas

```scss
@import './theme';

.data-table {
  width: 100%;
  border-collapse: collapse;
  background: $card-bg;
  border-radius: $radius-lg;
  overflow: hidden;
  box-shadow: $shadow-base;

  thead {
    background: $table-header-bg;

    th {
      padding: $spacing-4;
      text-align: left;
      font-size: $text-sm;
      font-weight: $font-semibold;
      color: $text-secondary;
      border-bottom: 2px solid $table-border;
    }
  }

  tbody {
    tr {
      border-bottom: 1px solid $table-border;
      transition: background $transition-fast $ease-out;

      &:hover {
        background: $table-row-hover;
      }

      &:last-child {
        border-bottom: none;
      }
    }

    td {
      padding: $spacing-4;
      font-size: $text-sm;
      color: $text-primary;
    }
  }
}
```

### 🎯 Alertas e Notificações

```scss
@import './theme';

.alert {
  padding: $spacing-4;
  border-radius: $radius-base;
  border-left: 4px solid;
  display: flex;
  align-items: start;
  gap: $spacing-3;

  &.success {
    background: $success-50;
    border-color: $success-500;
    color: $success-900;
  }

  &.warning {
    background: $warning-50;
    border-color: $warning-500;
    color: $warning-900;
  }

  &.error {
    background: $error-50;
    border-color: $error-500;
    color: $error-900;
  }

  &.info {
    background: $info-50;
    border-color: $info-500;
    color: $info-900;
  }
}
```

### 📱 Sidebar de Navegação

```scss
@import './theme';

.sidebar {
  background: $sidebar-bg;
  color: $sidebar-text;
  width: 250px;
  height: 100vh;
  overflow-y: auto;
  box-shadow: $shadow-md;

  .nav-item {
    display: flex;
    align-items: center;
    gap: $spacing-3;
    padding: $spacing-3 $spacing-5;
    color: $sidebar-text;
    text-decoration: none;
    transition: all $transition-base $ease-out;

    &:hover {
      background: $sidebar-bg-hover;
      color: $sidebar-text-hover;
    }

    &.active {
      background: $sidebar-bg-active;
      color: $sidebar-text-active;
      border-left: 4px solid $sidebar-border-active;
    }
  }
}
```

### 🎨 Cards de Estatísticas

```scss
@import './theme';

.stat-card {
  background: $card-bg;
  border-radius: $radius-lg;
  padding: $spacing-6;
  box-shadow: $shadow-base;
  transition: transform $transition-base $ease-out;

  &:hover {
    transform: translateY(-2px);
    box-shadow: $shadow-md;
  }

  .stat-label {
    font-size: $text-sm;
    color: $text-secondary;
    margin-bottom: $spacing-2;
  }

  .stat-value {
    font-size: $text-4xl;
    font-weight: $font-bold;
    color: $text-primary;
    margin-bottom: $spacing-1;
  }

  .stat-change {
    font-size: $text-sm;

    &.positive {
      color: $success-600;
    }

    &.negative {
      color: $error-600;
    }
  }

  // Variações por módulo
  &.products {
    border-left: 4px solid $products-primary;
  }

  &.inventory {
    border-left: 4px solid $inventory-primary;
  }

  &.sales {
    border-left: 4px solid $sales-primary;
  }

  &.customers {
    border-left: 4px solid $customers-primary;
  }
}
```

### 🎭 Loading States

```scss
@import './theme';

.loading-spinner {
  width: 40px;
  height: 40px;
  border: 4px solid $neutral-200;
  border-top-color: $primary-500;
  border-radius: $radius-full;
  animation: spin 1s linear infinite;
}

@keyframes spin {
  to { transform: rotate(360deg); }
}

.skeleton {
  background: linear-gradient(
    90deg,
    $neutral-200 25%,
    $neutral-100 50%,
    $neutral-200 75%
  );
  background-size: 200% 100%;
  animation: skeleton-loading 1.5s ease-in-out infinite;
  border-radius: $radius-base;
}

@keyframes skeleton-loading {
  0% { background-position: 200% 0; }
  100% { background-position: -200% 0; }
}
```

### 🎪 Modal/Dialog

```scss
@import './theme';

.modal-backdrop {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: $bg-overlay;
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: $z-modal-backdrop;
  animation: fadeIn $transition-base $ease-out;
}

.modal {
  background: $card-bg;
  border-radius: $radius-xl;
  box-shadow: $shadow-2xl;
  max-width: 600px;
  width: 90%;
  max-height: 90vh;
  overflow-y: auto;
  animation: slideUp $transition-slow $ease-out;
  z-index: $z-modal;
}

.modal-header {
  padding: $spacing-6;
  border-bottom: 1px solid $border-light;

  h2 {
    margin: 0;
    font-size: $text-2xl;
    font-weight: $font-semibold;
    color: $text-primary;
  }
}

.modal-body {
  padding: $spacing-6;
}

.modal-footer {
  padding: $spacing-6;
  border-top: 1px solid $border-light;
  display: flex;
  justify-content: flex-end;
  gap: $spacing-3;
}

@keyframes fadeIn {
  from { opacity: 0; }
  to { opacity: 1; }
}

@keyframes slideUp {
  from {
    opacity: 0;
    transform: translateY(20px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}
```

## 🎯 Dicas de Uso

### 1. Sempre use variáveis

❌ **Evite:**
```scss
.my-component {
  color: #1976d2;
  padding: 20px;
}
```

✅ **Prefira:**
```scss
.my-component {
  color: $primary-500;
  padding: $spacing-5;
}
```

### 2. Use transições suaves

```scss
.interactive-element {
  transition: all $transition-base $ease-out;

  &:hover {
    transform: translateY(-2px);
    box-shadow: $shadow-md;
  }
}
```

### 3. Mantenha consistência

Use sempre os mesmos valores de espaçamento, border-radius e sombras para manter a interface coesa.

### 4. Acessibilidade primeiro

Sempre garanta contraste adequado e estados de foco visíveis:

```scss
.button {
  &:focus-visible {
    outline: 2px solid $primary-500;
    outline-offset: 2px;
  }
}
```

---

**Última atualização**: Dezembro 2024
