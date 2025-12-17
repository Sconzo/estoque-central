# 🎨 Estoque Central - Design System

## Visão Geral

Este Design System define o sistema visual completo da aplicação Estoque Central, incluindo paleta de cores, tipografia, espaçamento, componentes e guidelines de uso.

## 📊 Paleta de Cores

### Cores Primárias (Identidade da Marca)

| Cor | Hex | Uso |
|-----|-----|-----|
| Primary 500 | `#1976d2` | Cor principal da marca, botões primários, links |
| Primary 600 | `#1565c0` | Hover state de elementos primários |
| Primary 700 | `#0d47a1` | Active state de elementos primários |
| Primary 100 | `#bbdefb` | Backgrounds suaves, estados de seleção |

### Cores Semânticas

#### ✅ Sucesso (Verde)
- **Main**: `#4caf50` - Mensagens de sucesso, indicadores positivos
- **Light**: `#e8f5e9` - Backgrounds de sucesso
- **Dark**: `#388e3c` - Texto sobre backgrounds claros

#### ⚠️ Atenção (Laranja)
- **Main**: `#ff9800` - Avisos, alertas moderados
- **Light**: `#fff3e0` - Backgrounds de warning
- **Dark**: `#f57c00` - Texto sobre backgrounds claros

#### ❌ Erro (Vermelho)
- **Main**: `#f44336` - Erros, ações destrutivas
- **Light**: `#ffebee` - Backgrounds de erro
- **Dark**: `#d32f2f` - Texto sobre backgrounds claros

#### ℹ️ Informação (Azul Claro)
- **Main**: `#03a9f4` - Mensagens informativas
- **Light**: `#e1f5fe` - Backgrounds informativos
- **Dark**: `#0288d1` - Texto sobre backgrounds claros

### Cores Neutras (Cinzas)

| Nome | Hex | Uso |
|------|-----|-----|
| Neutral 0 | `#ffffff` | Backgrounds de cards, modais |
| Neutral 50 | `#fafafa` | Background secundário |
| Neutral 100 | `#f5f5f5` | Background terciário, estados hover |
| Neutral 300 | `#e0e0e0` | Bordas, divisores |
| Neutral 500 | `#9e9e9e` | Texto terciário, placeholders |
| Neutral 600 | `#757575` | Texto secundário |
| Neutral 900 | `#212121` | Texto principal |

### Cores por Módulo

Cada módulo tem sua própria identidade visual:

| Módulo | Cor Principal | Uso |
|--------|---------------|-----|
| 📦 Produtos | Verde `#4caf50` | Cards, badges, ícones de produtos |
| 📋 Estoque | Laranja `#ff9800` | Alertas de estoque, movimentações |
| 💰 Vendas | Azul `#1976d2` | PDV, pedidos, relatórios de vendas |
| 👥 Clientes | Roxo `#9c27b0` | Cards de clientes, histórico |
| 🔗 Integrações | Ciano `#00acc1` | Status de conexão, sync |

## 🔤 Tipografia

### Fonte

- **Principal**: Roboto, Helvetica Neue, Arial, sans-serif
- **Monoespaçada**: Roboto Mono, Courier New, monospace

### Tamanhos

| Nome | Tamanho | Uso |
|------|---------|-----|
| xs | 12px | Labels pequenas, metadata |
| sm | 14px | Texto secundário, descrições |
| base | 16px | Texto principal do corpo |
| lg | 18px | Subtítulos |
| xl | 20px | Títulos de seção |
| 2xl | 24px | Títulos de cards |
| 3xl | 30px | Títulos de página |
| 4xl | 36px | Headings principais |

### Pesos

- **Light** (300): Textos leves, números grandes
- **Normal** (400): Texto do corpo
- **Medium** (500): Subtítulos, ênfase leve
- **Semibold** (600): Títulos de cards
- **Bold** (700): Headings principais, CTAs

## 📏 Espaçamento

Sistema baseado em múltiplos de 4px:

| Nome | Tamanho | Uso |
|------|---------|-----|
| 1 | 4px | Padding interno mínimo |
| 2 | 8px | Espaçamento entre ícone e texto |
| 3 | 12px | Padding de botões pequenos |
| 4 | 16px | Padding padrão de componentes |
| 5 | 20px | Margin entre elementos |
| 6 | 24px | Padding de cards |
| 8 | 32px | Espaçamento entre seções |
| 12 | 48px | Margin entre blocos maiores |

## 🎭 Sombras

| Nome | Valor | Uso |
|------|-------|-----|
| sm | `0 1px 2px rgba(0,0,0,0.05)` | Bordas sutis |
| base | `0 2px 4px rgba(0,0,0,0.1)` | Cards padrão |
| md | `0 4px 6px rgba(0,0,0,0.1)` | Dropdowns, popovers |
| lg | `0 10px 15px rgba(0,0,0,0.1)` | Modais, elementos elevados |
| xl | `0 20px 25px rgba(0,0,0,0.1)` | Drawer, side panels |

## 🔘 Componentes

### Botões

#### Primário
- **Background**: Primary 500
- **Hover**: Primary 600
- **Active**: Primary 700
- **Texto**: Branco
- **Padding**: 12px 24px
- **Border Radius**: 4px

#### Secundário
- **Background**: Neutral 100
- **Hover**: Neutral 200
- **Active**: Neutral 300
- **Texto**: Neutral 900
- **Padding**: 12px 24px
- **Border Radius**: 4px

#### Perigo
- **Background**: Error 500
- **Hover**: Error 600
- **Active**: Error 700
- **Texto**: Branco

### Cards

- **Background**: Branco
- **Border**: Neutral 200 (1px)
- **Border Radius**: 8px
- **Padding**: 20px
- **Shadow**: shadow-base
- **Hover Shadow**: shadow-md

### Inputs

- **Background**: Branco
- **Border**: Neutral 300
- **Border Focus**: Primary 500
- **Padding**: 12px
- **Border Radius**: 4px
- **Placeholder**: Neutral 500

### Badges

#### Sucesso
- **Background**: Success 100 (`#e8f5e9`)
- **Texto**: Success 800 (`#2e7d32`)

#### Atenção
- **Background**: Warning 100 (`#fff3e0`)
- **Texto**: Warning 800 (`#ef6c00`)

#### Erro
- **Background**: Error 100 (`#ffebee`)
- **Texto**: Error 800 (`#c62828`)

## 🎬 Animações e Transições

### Duração

- **Fast**: 150ms - Mudanças rápidas (hover)
- **Base**: 200ms - Transições padrão
- **Slow**: 300ms - Animações de entrada/saída
- **Slower**: 500ms - Animações complexas

### Easing

- **Ease In**: `cubic-bezier(0.4, 0, 1, 1)` - Entrada
- **Ease Out**: `cubic-bezier(0, 0, 0.2, 1)` - Saída
- **Ease In Out**: `cubic-bezier(0.4, 0, 0.2, 1)` - Entrada e saída

## 📱 Breakpoints

| Nome | Largura | Uso |
|------|---------|-----|
| xs | 0px | Mobile pequeno |
| sm | 576px | Mobile grande |
| md | 768px | Tablet |
| lg | 992px | Desktop pequeno |
| xl | 1200px | Desktop grande |
| 2xl | 1400px | Desktop extra grande |

## ♿ Acessibilidade

### Contraste de Cores

Todas as combinações de cores seguem WCAG 2.1 Level AA:
- **Texto normal**: Contraste mínimo de 4.5:1
- **Texto grande** (≥18px ou ≥14px bold): Contraste mínimo de 3:1
- **Elementos UI**: Contraste mínimo de 3:1

### Estados de Foco

Todos os elementos interativos têm indicador visual de foco:
- **Outline**: Primary 500
- **Width**: 2px
- **Offset**: 2px

## 🎨 Como Usar

### Em SCSS

```scss
@import './theme';

.my-component {
  background: $primary-500;
  color: $neutral-0;
  padding: $spacing-4;
  border-radius: $radius-md;
  box-shadow: $shadow-base;
  transition: all $transition-base $ease-out;

  &:hover {
    background: $primary-600;
    box-shadow: $shadow-md;
  }
}
```

### Em CSS (Custom Properties)

```css
.my-component {
  background: var(--color-primary);
  color: var(--text-primary);
  border: 1px solid var(--border-color);
}
```

### Classes Utilitárias (Tailwind)

O projeto usa Tailwind CSS. Você pode estender a configuração para usar estas cores.

## 🌙 Dark Mode (Planejado)

O sistema está preparado para suportar dark mode no futuro através de CSS Custom Properties.

---

**Versão**: 1.0
**Última atualização**: Dezembro 2024
**Mantido por**: Time de UX/UI
