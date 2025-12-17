# 🎨 Integração Tailwind + Design System

## Como integrar as cores do Design System com Tailwind CSS

Atualize o arquivo `tailwind.config.js` para usar as cores do nosso Design System:

```javascript
/** @type {import('tailwindcss').Config} */
module.exports = {
  content: [
    "./src/**/*.{html,ts}",
  ],
  theme: {
    extend: {
      colors: {
        // Primary Colors
        primary: {
          50: '#e3f2fd',
          100: '#bbdefb',
          200: '#90caf9',
          300: '#64b5f6',
          400: '#42a5f5',
          500: '#1976d2',  // Main
          600: '#1565c0',
          700: '#0d47a1',
          800: '#0a3d91',
          900: '#063270',
        },
        // Secondary Colors
        secondary: {
          50: '#f3e5f5',
          100: '#e1bee7',
          200: '#ce93d8',
          300: '#ba68c8',
          400: '#ab47bc',
          500: '#9c27b0',  // Main
          600: '#8e24aa',
          700: '#7b1fa2',
          800: '#6a1b9a',
          900: '#4a148c',
        },
        // Success (Green)
        success: {
          50: '#e8f5e9',
          100: '#c8e6c9',
          200: '#a5d6a7',
          300: '#81c784',
          400: '#66bb6a',
          500: '#4caf50',  // Main
          600: '#43a047',
          700: '#388e3c',
          800: '#2e7d32',
          900: '#1b5e20',
        },
        // Warning (Orange)
        warning: {
          50: '#fff3e0',
          100: '#ffe0b2',
          200: '#ffcc80',
          300: '#ffb74d',
          400: '#ffa726',
          500: '#ff9800',  // Main
          600: '#fb8c00',
          700: '#f57c00',
          800: '#ef6c00',
          900: '#e65100',
        },
        // Error (Red)
        error: {
          50: '#ffebee',
          100: '#ffcdd2',
          200: '#ef9a9a',
          300: '#e57373',
          400: '#ef5350',
          500: '#f44336',  // Main
          600: '#e53935',
          700: '#d32f2f',
          800: '#c62828',
          900: '#b71c1c',
        },
        // Info (Cyan)
        info: {
          50: '#e1f5fe',
          100: '#b3e5fc',
          200: '#81d4fa',
          300: '#4fc3f7',
          400: '#29b6f6',
          500: '#03a9f4',  // Main
          600: '#039be5',
          700: '#0288d1',
          800: '#0277bd',
          900: '#01579b',
        },
        // Module Colors
        products: {
          DEFAULT: '#4caf50',
          light: '#c8e6c9',
          dark: '#388e3c',
        },
        inventory: {
          DEFAULT: '#ff9800',
          light: '#ffe0b2',
          dark: '#f57c00',
        },
        sales: {
          DEFAULT: '#1976d2',
          light: '#bbdefb',
          dark: '#0d47a1',
        },
        customers: {
          DEFAULT: '#9c27b0',
          light: '#e1bee7',
          dark: '#7b1fa2',
        },
        integrations: {
          DEFAULT: '#00acc1',
          light: '#b2ebf2',
          dark: '#00838f',
        },
      },
      fontFamily: {
        sans: ['Roboto', 'Helvetica Neue', 'Arial', 'sans-serif'],
        mono: ['Roboto Mono', 'Courier New', 'monospace'],
      },
      fontSize: {
        xs: '0.75rem',    // 12px
        sm: '0.875rem',   // 14px
        base: '1rem',     // 16px
        lg: '1.125rem',   // 18px
        xl: '1.25rem',    // 20px
        '2xl': '1.5rem',  // 24px
        '3xl': '1.875rem', // 30px
        '4xl': '2.25rem', // 36px
        '5xl': '3rem',    // 48px
      },
      spacing: {
        '1': '0.25rem',   // 4px
        '2': '0.5rem',    // 8px
        '3': '0.75rem',   // 12px
        '4': '1rem',      // 16px
        '5': '1.25rem',   // 20px
        '6': '1.5rem',    // 24px
        '8': '2rem',      // 32px
        '10': '2.5rem',   // 40px
        '12': '3rem',     // 48px
        '16': '4rem',     // 64px
        '20': '5rem',     // 80px
        '24': '6rem',     // 96px
      },
      borderRadius: {
        'none': '0',
        'sm': '0.125rem',   // 2px
        'DEFAULT': '0.25rem', // 4px
        'md': '0.375rem',   // 6px
        'lg': '0.5rem',     // 8px
        'xl': '0.75rem',    // 12px
        '2xl': '1rem',      // 16px
        'full': '9999px',
      },
      boxShadow: {
        'sm': '0 1px 2px 0 rgba(0, 0, 0, 0.05)',
        'DEFAULT': '0 2px 4px 0 rgba(0, 0, 0, 0.1)',
        'md': '0 4px 6px -1px rgba(0, 0, 0, 0.1)',
        'lg': '0 10px 15px -3px rgba(0, 0, 0, 0.1)',
        'xl': '0 20px 25px -5px rgba(0, 0, 0, 0.1)',
        '2xl': '0 25px 50px -12px rgba(0, 0, 0, 0.25)',
        'inner': 'inset 0 2px 4px 0 rgba(0, 0, 0, 0.06)',
      },
      transitionDuration: {
        'fast': '150ms',
        'DEFAULT': '200ms',
        'slow': '300ms',
        'slower': '500ms',
      },
      zIndex: {
        'dropdown': '1000',
        'sticky': '1020',
        'fixed': '1030',
        'modal-backdrop': '1040',
        'modal': '1050',
        'popover': '1060',
        'tooltip': '1070',
      },
    },
  },
  plugins: [],
}
```

## Exemplos de Uso com Tailwind

### Botões

```html
<!-- Botão Primário -->
<button class="bg-primary-500 hover:bg-primary-600 active:bg-primary-700
               text-white font-medium py-3 px-6 rounded
               transition-all duration-200 shadow hover:shadow-md">
  Salvar
</button>

<!-- Botão Secundário -->
<button class="bg-gray-100 hover:bg-gray-200 active:bg-gray-300
               text-gray-900 font-medium py-3 px-6 rounded border border-gray-300
               transition-all duration-200">
  Cancelar
</button>

<!-- Botão de Perigo -->
<button class="bg-error-500 hover:bg-error-600 active:bg-error-700
               text-white font-medium py-3 px-6 rounded
               transition-all duration-200">
  Excluir
</button>
```

### Cards

```html
<!-- Card Básico -->
<div class="bg-white rounded-lg shadow p-6 hover:shadow-md transition-shadow">
  <h3 class="text-xl font-semibold text-gray-900 mb-2">Título</h3>
  <p class="text-gray-600">Descrição do card</p>
</div>

<!-- Card de Módulo (Produtos) -->
<div class="bg-white rounded-lg shadow p-6 border-l-4 border-products">
  <div class="flex items-center gap-4">
    <span class="text-4xl">📦</span>
    <div>
      <div class="text-sm text-gray-600">Total de Produtos</div>
      <div class="text-3xl font-bold text-gray-900">156</div>
    </div>
  </div>
</div>
```

### Badges

```html
<!-- Badge de Sucesso -->
<span class="inline-flex items-center px-3 py-1 rounded-full
             bg-success-100 text-success-800 text-xs font-semibold">
  Ativo
</span>

<!-- Badge de Atenção -->
<span class="inline-flex items-center px-3 py-1 rounded-full
             bg-warning-100 text-warning-800 text-xs font-semibold">
  Estoque Baixo
</span>

<!-- Badge de Erro -->
<span class="inline-flex items-center px-3 py-1 rounded-full
             bg-error-100 text-error-800 text-xs font-semibold">
  Inativo
</span>
```

### Inputs

```html
<div class="mb-4">
  <label class="block text-sm font-medium text-gray-700 mb-2">
    Nome do Produto
  </label>
  <input
    type="text"
    class="w-full px-3 py-3 border border-gray-300 rounded
           focus:outline-none focus:ring-2 focus:ring-primary-500 focus:border-transparent
           hover:border-gray-400 transition-colors"
    placeholder="Digite o nome..."
  />
</div>
```

### Alerts

```html
<!-- Alert de Sucesso -->
<div class="bg-success-50 border-l-4 border-success-500 p-4 rounded">
  <p class="text-success-900">Operação realizada com sucesso!</p>
</div>

<!-- Alert de Atenção -->
<div class="bg-warning-50 border-l-4 border-warning-500 p-4 rounded">
  <p class="text-warning-900">Atenção: Estoque abaixo do mínimo.</p>
</div>

<!-- Alert de Erro -->
<div class="bg-error-50 border-l-4 border-error-500 p-4 rounded">
  <p class="text-error-900">Erro ao processar a requisição.</p>
</div>
```

### Grid de Layout

```html
<!-- Dashboard Grid -->
<div class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-6">
  <!-- Cards aqui -->
</div>

<!-- Duas Colunas -->
<div class="grid grid-cols-1 lg:grid-cols-2 gap-6">
  <!-- Conteúdo aqui -->
</div>
```

### Tabelas

```html
<div class="bg-white rounded-lg shadow overflow-hidden">
  <table class="w-full">
    <thead class="bg-gray-100 border-b-2 border-gray-200">
      <tr>
        <th class="text-left p-4 text-sm font-semibold text-gray-600">ID</th>
        <th class="text-left p-4 text-sm font-semibold text-gray-600">Produto</th>
        <th class="text-left p-4 text-sm font-semibold text-gray-600">Estoque</th>
      </tr>
    </thead>
    <tbody>
      <tr class="border-b border-gray-200 hover:bg-gray-50 transition-colors">
        <td class="p-4 text-sm text-gray-900">#001</td>
        <td class="p-4 text-sm text-gray-900">Produto A</td>
        <td class="p-4 text-sm text-gray-900">50</td>
      </tr>
    </tbody>
  </table>
</div>
```

## 🎯 Utilitários Customizados

Você também pode criar utilitários personalizados no Tailwind:

```javascript
// tailwind.config.js
module.exports = {
  // ... configuração anterior
  plugins: [
    function({ addUtilities }) {
      const newUtilities = {
        '.text-balance': {
          'text-wrap': 'balance',
        },
        '.scrollbar-hide': {
          '-ms-overflow-style': 'none',
          'scrollbar-width': 'none',
          '&::-webkit-scrollbar': {
            display: 'none'
          }
        }
      }
      addUtilities(newUtilities)
    }
  ],
}
```

---

**Nota**: O Tailwind CSS oferece grande flexibilidade. Use as classes utilitárias quando fizer sentido, mas não hesite em criar componentes SCSS customizados para elementos mais complexos ou repetitivos.
