# Estoque Central UX Design Specification

_Created on 2025-12-13 by poly_
_Generated using BMad Method - Create UX Design Workflow v1.0_

---

## Executive Summary

### Visão do Projeto

**Estoque Central** é um ERP omnichannel funcional e confiável para PMEs brasileiras que vendem em múltiplos canais (loja física + marketplaces). O sistema resolve o caos de overselling e controle manual através de três interfaces especializadas: PDV Touchscreen (velocidade para operadores de caixa), Ordem de Venda B2B (contexto completo para vendedores corporativos) e Recebimento Mobile (operação com uma mão em depósito).

### Usuários-Alvo

- **PMEs brasileiras** com 2-20 funcionários
- Faturamento R$ 50k-500k/mês
- 100-5.000 SKUs
- **Familiaridade tecnológica média** (usam Excel, WhatsApp Business)
- **Segmentos:** Pet shops, mini-mercados, distribuidoras, lojas de produtos naturais

**Personas Principais:**
1. Operador de Caixa (PDV)
2. Vendedor B2B (Ordem de Venda)
3. Pessoal de Depósito (Recebimento Mobile)
4. Gerente/Admin (Dashboard, Relatórios, Configurações)

### Experiência Core

**DNA do Sistema:** Single source of truth - qualquer ação de estoque (venda, entrada, transferência) atualiza TUDO instantaneamente.

**Três Interfaces Especializadas (não genérica):**
1. **PDV Touchscreen** - Velocidade brutal: 3-5 toques para venda completa com NFCe
2. **Ordem de Venda B2B** - Contexto completo: estoque em tempo real, histórico do cliente
3. **Recebimento Mobile** - Operação com uma mão: scanner de código de barras via câmera

### Resposta Emocional Desejada

- 🛡️ **Confiança** - "Posso confiar que esse número está certo"
- ⚡ **Eficiência** - "Consigo fazer meu trabalho sem travamento"
- 🎯 **Controle** - "Sei exatamente o que está acontecendo"
- 😌 **Tranquilidade** - "Não vou descobrir um overselling depois"

### Plataformas

**Web Responsive + PWA:**
- **Desktop 1366x768+** - Ordem de Venda, Configurações, Relatórios
- **Tablet touchscreen 1280x800** - PDV fullscreen
- **Smartphone 375x667+** - Recebimento Mobile com câmera

**Material Design** com identidade brasileira, WCAG AA compliance.

### Complexidade UX

**Moderada-a-Alta:** Múltiplas personas, multi-plataforma, sincronização real-time, interfaces especializadas por contexto de uso.

### Inspiração - Mercado Brasileiro

Padrões validados por Conta Azul, Bling e Olist:
- Dashboard-first com métricas acionáveis
- Sincronização transparente para prevenir overselling
- Layout limpo que reduz erros
- Automação inteligente para reduzir digitação manual

---

## 1. Design System Foundation

### 1.1 Design System Choice

**Decisão:** Angular Material (Material Design 3)

**Versão:** Angular Material 18+ (Material Design 3 stable)

**Rationale:**

**Por que Angular Material:**

1. **Integração Nativa com Angular**
   - Biblioteca oficial mantida pelo Angular team
   - Compatibilidade garantida com Angular 17+ (Standalone Components, Signals)
   - Melhor DX (Developer Experience) para o time

2. **Acessibilidade WCAG AA Nativa**
   - ARIA attributes e keyboard navigation built-in
   - High contrast mode support
   - Screen reader compatibility
   - Avaliação contínua contra padrões WCAG pelo time Angular

3. **Familiaridade e Confiança**
   - Material Design é amplamente reconhecido (Gmail, Google Drive, Google Cloud)
   - Linguagem visual familiar transmite confiança (alinha com emoção desejada)
   - Reduz curva de aprendizado para usuários

4. **Multi-Plataforma**
   - Responsivo por design
   - Funciona bem em desktop, tablet e mobile
   - Componentes adaptam-se aos diferentes contextos de uso

5. **Theming e Customização**
   - Sistema de theming baseado em tokens (Material Design 3)
   - Permite customização para identidade brasileira
   - Mantém consistência visual

6. **Componentes Suficientes para MVP**
   - Buttons, Forms, Navigation, Data Tables, Modals, Alerts
   - Date pickers, Autocomplete, Chips
   - Side navigation, Toolbars, Menus, Tabs
   - Tudo que precisamos para as três interfaces especializadas

**Componentes Principais que Usaremos:**

**Para PDV Touchscreen:**
- MatButton (large touch targets)
- MatAutocomplete (busca de produtos)
- MatTable ou MatList (carrinho de compras)
- MatDialog (confirmações)
- MatProgressSpinner (loading NFCe)
- MatSnackBar (feedback de ações)

**Para Ordem de Venda B2B:**
- MatTable com sorting/filtering (grid de produtos)
- MatFormField (inputs de cliente, produtos)
- MatSelect (seleção de depósito)
- MatDatepicker (data do pedido)
- MatSideNav (histórico do cliente)
- MatCard (totalizadores)

**Para Recebimento Mobile:**
- MatButton (ações primárias)
- MatFormField (quantidade)
- MatList (OCs pendentes)
- MatIcon (feedback visual)
- MatProgressBar (progresso do recebimento)

**Para Dashboard/Relatórios:**
- MatCard (métricas)
- MatTable (relatórios tabulares)
- MatTabs (diferentes views)
- MatDateRangePicker (filtros de período)
- MatChip (tags e filtros)

**Trade-offs Aceitos:**

❌ **Componentes avançados não incluídos:**
- Charts/gráficos (usaremos Chart.js ou Recharts separadamente)
- DataTable super avançada (AG-Grid se necessário no futuro)
- Scheduler/Calendar complexo (não precisa no MVP)

**Mitigação:** Material fornece 90% do que precisamos; os 10% restantes podem ser complementados com bibliotecas especializadas se necessário.

**Alternativa Considerada:** PrimeNG
- ✅ Mais componentes (80+)
- ✅ DataTable mais rica
- ❌ Mais pesado
- ❌ Curva de aprendizado maior
- ❌ Menos familiar para usuários
- **Conclusão:** Overkill para MVP; Material é suficiente e mais adequado

**Fontes:**
- [Angular Material Components](https://material.angular.dev/)
- [Angular Accessibility](https://angular.dev/best-practices/a11y)
- [Material Design 3](https://m3.material.io/)
- [PrimeNG vs Angular Material Comparison](https://developerchandan.medium.com/primeng-vs-angular-material-in-2025-which-ui-library-is-better-for-angular-projects-d98aef4c5465)

---

## 2. Core User Experience

### 2.1 Defining Experience

**Experiência Definidora:** Um ERP funcional que entrega o que promete.

O Estoque Central não é sobre inovação radical - é sobre **confiabilidade funcional** em cada interação:

**Três Interfaces Especializadas:**

1. **PDV Touchscreen** (Tablet 10")
   - Momento crítico: Operador de caixa completa venda em 30 segundos
   - Fluxo: Scan → Carrinho → Finalizar → NFCe automática
   - Princípio: Velocidade sem erros

2. **Ordem de Venda B2B** (Desktop)
   - Momento crítico: Vendedor consulta estoque real em tempo real
   - Fluxo: Cliente pede → Verifica estoque multi-depósito → Reserva → Confirma
   - Princípio: Confiança nos dados

3. **Recebimento Mobile** (Smartphone)
   - Momento crítico: Operador escaneia e confirma entrada com uma mão
   - Fluxo: Câmera → Scan → Quantidade → Confirma
   - Princípio: Precisão e rapidez

**DNA do Sistema:**
Single source of truth - qualquer ação de estoque (venda, entrada, transferência) atualiza TUDO instantaneamente, sem falhas.

### 2.2 Resposta Emocional Desejada

**O que usuários devem SENTIR ao usar o sistema:**

- 🛡️ **Confiança** - "Posso confiar que esse número está certo"
- ⚡ **Eficiência** - "Consigo fazer meu trabalho sem travamento"
- 🎯 **Controle** - "Sei exatamente o que está acontecendo"
- 😌 **Tranquilidade** - "Não vou descobrir um overselling depois"

**Princípios de Design Resultantes:**

✅ **Clareza > Criatividade** - Botões fazem o que dizem, estados sempre visíveis
✅ **Previsibilidade > Surpresa** - Mesmas ações, mesmos resultados, sempre
✅ **Eficiência > Estética** - Interface profissional, mas função vem primeiro
✅ **Confiança nos Dados** - Números batem, sincronização transparente, histórico completo

### 2.3 Plataformas e Contextos de Uso

**Web Responsive + PWA:**

- **Desktop 1366x768+** (primário)
  - Ordem de Venda, Configurações, Relatórios, Cadastros
  - Usuários: Vendedores B2B, gerentes, administrativo

- **Tablet 10" Touchscreen 1280x800** (primário)
  - PDV fullscreen modo landscape
  - Usuários: Operadores de caixa, atendentes de loja

- **Smartphone 375x667+** (Android/iOS, primário)
  - Recebimento Mobile com câmera para scanning
  - Usuários: Pessoal de depósito, recebimento

**Browsers:** Chrome/Edge Chromium (prioridade 1), Firefox/Safari (prioridade 2)

### 2.4 Análise de Inspiração - Mercado Brasileiro

**Referências Competitivas Analisadas:** Conta Azul, Bling, Olist (Tiny ERP)

**Padrões de UX que Funcionam no Mercado Brasileiro de ERPs para PMEs:**

1. **Dashboard-First Approach**
   - Centro de comando com métricas-chave visíveis
   - Views prontas (não força configuração manual)
   - Foco em "decisões acionáveis" > "dados brutos"
   - Exemplo: Conta Azul com views para fluxo de caixa, DRE, contas a pagar/receber

2. **Clareza Visual Extrema**
   - Layout limpo que reduz erros (aprendizado de Olist)
   - Contraste com "ERPs engessados com interface confusa e excesso de cliques"
   - Menos cliques, mais ação
   - Estados do sistema sempre visíveis

3. **Sincronização Transparente e em Tempo Real**
   - Mostrar QUANDO está sincronizando (loading states)
   - Confirmar QUANDO sincronizou com sucesso (feedback visual)
   - Alertar SE falhou com ação de retry clara
   - Exemplo: Bling com sincronização automática de estoque e preços em 250+ integrações

4. **Prevenção de Overselling = Confiança**
   - Reserva de estoque visível e automática
   - Números confiáveis em tempo real
   - Histórico rastreável de movimentações
   - Exemplo: Bling com feature de reserva que previne venda duplicada entre canais

5. **Automação Inteligente**
   - Reduzir digitação manual com IA
   - Scanner de código de barras via câmera
   - Cálculos automáticos (custo médio, totais)
   - Exemplos: Conta AI Captura (extração automática de documentos), Lis IA (comandos em linguagem natural)

6. **Especialização por Contexto**
   - PDV: Mínimo de cliques, máxima velocidade
   - B2B: Visibilidade completa, contexto do cliente
   - Mobile: Interface touch-optimized para operação com uma mão

**Princípios Aplicados ao Estoque Central:**
- Dashboard como hub central com métricas críticas (vendas do dia, estoque crítico, pedidos pendentes)
- Sincronização visível e transparente (ML, NFCe, estoque multi-depósito)
- Prevenção de overselling como feature core (reserva automática)
- Automação de tarefas repetitivas (cálculo de custo médio, baixa automática de BOM, sincronização pós-venda)
- Interfaces especializadas por contexto de uso (não genérica)

**Fontes:**
- [Novidades Conta Azul 2025](https://ajuda.contaazul.com/hc/pt-br/articles/12190510485773-Novidades-da-Conta-Azul-Mais-em-2025)
- [Bling Gestão de Estoque](https://ajuda.bling.com.br/hc/pt-br/articles/10448300263575-Gest%C3%A3o-de-estoque-completa)
- [Olist Lança Lis IA](https://vdvgroup.com.br/olist-erp-lanca-lis/)
- [Bling Integrações](https://www.bling.com.br/integracoes-bling)
- [Conta Azul ERP Solutions](https://latam.enterpriseviewpoint.com/conta-azul-automated-and-integrated-erp-solutions/)

---

## 3. Visual Foundation

### 3.1 Color System

**Tema Escolhido:** Luxo Profundo (Deep Purple Luxury)

**Rationale:** Diferenciação total dos concorrentes (Conta Azul, Bling, Olist usam azul), transmite inovação, qualidade premium e sofisticação. Roxo escuro com dourado como accent cria sensação de confiabilidade moderna sem ser tradicional/conservador.

**Paleta de Cores:**

**Cores Primárias:**
- **Primary:** `#6A1B9A` (Roxo Profundo)
  - Uso: Botões primários, navegação principal, links, elementos interativos principais
  - Hover: `#4A148C` (Roxo mais escuro)
  - Disabled: `#6A1B9A` com 50% opacity

- **Accent:** `#F9A825` (Dourado/Âmbar)
  - Uso: Destaques especiais, badges premium, ações secundárias importantes
  - Hover: `#F57F17` (Dourado mais escuro)
  - Uso estratégico: Parcimônia - usar apenas para destacar features premium ou ações VIP

**Cores Semânticas (Material Design 3):**

- **Success:** `#2E7D32` (Verde Escuro)
  - Uso: Estoque OK, sincronização bem-sucedida, pedidos confirmados, NFCe emitida
  - Background: `#E8F5E9` (verde claro) para alerts/banners

- **Warning:** `#F9A825` (Amarelo/Dourado - reutiliza accent)
  - Uso: Estoque baixo, atenção necessária, alertas não-críticos
  - Background: `#FFF8E1` (amarelo claro) para alerts/banners

- **Error:** `#C62828` (Vermelho Escuro)
  - Uso: Falhas, erros críticos, NFCe falhou, validações de form
  - Background: `#FFEBEE` (vermelho claro) para alerts/banners

- **Info:** `#0277BD` (Azul)
  - Uso: Informações neutras, dicas, processos em andamento
  - Background: `#E1F5FE` (azul claro) para alerts/banners

**Cores Neutras (Grayscale):**

- **Text Primary:** `#212121` (quase preto)
- **Text Secondary:** `#757575` (cinza médio)
- **Text Disabled:** `#BDBDBD` (cinza claro)
- **Divider:** `#E0E0E0` (cinza muito claro)
- **Background:** `#FAFAFA` (off-white)
- **Surface:** `#FFFFFF` (branco puro para cards/modals)
- **Border:** `#E0E0E0` (cinza muito claro)

**Aplicação por Contexto:**

**PDV Touchscreen:**
- Botão primário de venda: `#6A1B9A` (roxo)
- Feedback de sucesso: `#2E7D32` (verde)
- Alertas de estoque: `#F9A825` (dourado)
- Erros: `#C62828` (vermelho)

**Ordem de Venda B2B:**
- Ações principais: `#6A1B9A` (roxo)
- Indicadores de estoque disponível: `#2E7D32` (verde)
- Reservas: `#0277BD` (azul info)
- Estoque baixo: `#F9A825` (dourado warning)

**Dashboard:**
- Cards de métricas: Border `#6A1B9A` (roxo)
- Indicadores positivos: `#2E7D32` (verde)
- Alertas: `#F9A825` (dourado)
- Problemas: `#C62828` (vermelho)

### 3.2 Typography System

**Fonte Principal:** Roboto (Material Design 3 default)
- Razão: Legível em todas as resoluções, familiar aos usuários, otimizada para telas digitais
- Fallback: `-apple-system, BlinkMacSystemFont, 'Segoe UI', sans-serif`

**Type Scale:**

**Headings:**
- **H1:** 32px / 600 weight / 1.2 line-height - Page titles
- **H2:** 24px / 600 weight / 1.3 line-height - Section titles
- **H3:** 20px / 600 weight / 1.4 line-height - Subsection titles
- **H4:** 18px / 500 weight / 1.4 line-height - Card titles
- **H5:** 16px / 500 weight / 1.5 line-height - List headers
- **H6:** 14px / 500 weight / 1.5 line-height - Small headers

**Body:**
- **Body 1:** 16px / 400 weight / 1.5 line-height - Primary text
- **Body 2:** 14px / 400 weight / 1.5 line-height - Secondary text, forms
- **Caption:** 12px / 400 weight / 1.4 line-height - Labels, hints
- **Overline:** 10px / 500 weight / 1.5 line-height / uppercase - Tags, status

**Interactive:**
- **Button:** 14px / 500 weight / 1.5 line-height - Buttons, CTAs
- **Link:** 14px / 500 weight / 1.5 line-height / underline on hover

**Monospace (para dados numéricos, códigos):**
- **Code/Numbers:** `'Roboto Mono', monospace` - SKUs, valores monetários em relatórios

**Color de Texto por Contexto:**
- Primary text: `#212121` (headers, body principal)
- Secondary text: `#757575` (descriptions, hints)
- Disabled: `#BDBDBD`
- Links: `#6A1B9A` (roxo primary)
- Success text: `#2E7D32`
- Error text: `#C62828`

### 3.3 Icon System

**Biblioteca:** Material Icons (Angular Material built-in)
- Razão: Integrado com Angular Material, consistente, escalável, profissional
- **NÃO usar emojis** - usar apenas Material Icons com cores apropriadas

**Tamanhos de Ícones:**
- **Extra Small:** 16px - Inline com texto, badges
- **Small:** 20px - Buttons, form inputs
- **Medium:** 24px - Padrão para navegação, actions
- **Large:** 32px - Headers, empty states
- **Extra Large:** 48px - Placeholders, onboarding

**Cores de Ícones por Contexto:**

**Ícones de Navegação/Ação:**
- Primary actions: `#6A1B9A` (roxo)
- Secondary actions: `#757575` (cinza)
- Disabled: `#BDBDBD` (cinza claro)
- Active/Selected: `#6A1B9A` (roxo)

**Ícones Semânticos:**
- Success (✓ check_circle): `#2E7D32` (verde)
- Warning (⚠ warning): `#F9A825` (dourado)
- Error (✕ error): `#C62828` (vermelho)
- Info (ℹ info): `#0277BD` (azul)
- Sync (🔄 sync): `#6A1B9A` (roxo) quando ativo, `#757575` quando idle

**Ícones de Domínio (contexto do negócio):**
- Estoque/Inventory (📦 inventory_2): `#6A1B9A` (roxo)
- Vendas/Sales (💰 point_of_sale): `#6A1B9A` (roxo)
- Produtos/Products (🏷️ local_offer): `#757575` (neutro)
- Notificações (🔔 notifications): `#F9A825` (dourado) com badge count
- Dashboard (📊 dashboard): `#6A1B9A` (roxo)
- Relatórios (📈 analytics): `#6A1B9A` (roxo)
- Configurações (⚙️ settings): `#757575` (neutro)

**Exemplos de Uso:**

```html
<!-- Botão primário com ícone -->
<button mat-raised-button color="primary">
  <mat-icon>point_of_sale</mat-icon>
  Nova Venda
</button>

<!-- Alert de sucesso -->
<mat-icon class="success-icon">check_circle</mat-icon>
Estoque sincronizado

<!-- Métrica de dashboard -->
<mat-icon [style.color]="'#6A1B9A'">inventory_2</mat-icon>
Estoque Total

<!-- Notificação com badge -->
<mat-icon [matBadge]="notificationCount" matBadgeColor="warn">
  notifications
</mat-icon>
```

### 3.4 Spacing System

**Base Unit:** 8px (Material Design padrão)

**Spacing Scale:**
- **xs:** 4px (0.5 × base)
- **sm:** 8px (1 × base)
- **md:** 16px (2 × base)
- **lg:** 24px (3 × base)
- **xl:** 32px (4 × base)
- **2xl:** 48px (6 × base)
- **3xl:** 64px (8 × base)

**Uso:**
- Padding interno de componentes: `sm` (8px) a `md` (16px)
- Gaps entre elementos relacionados: `md` (16px)
- Gaps entre seções: `lg` (24px) a `xl` (32px)
- Margins de página: `xl` (32px)

### 3.5 Elevation & Shadows (Material Design 3)

**Shadow Levels:**
- **Level 1:** Cards, Tables - `0 2px 4px rgba(0,0,0,0.08)`
- **Level 2:** Buttons hover, Chips - `0 4px 8px rgba(0,0,0,0.12)`
- **Level 3:** Modals, Dropdowns - `0 8px 16px rgba(0,0,0,0.16)`
- **Level 4:** Navigation Drawer - `0 12px 24px rgba(0,0,0,0.20)`

**Uso por Componente:**
- Cards: Level 1
- Buttons (raised): Level 2
- Dialogs/Modals: Level 3
- Menus/Tooltips: Level 3
- Navigation: Level 4

### 3.6 Border Radius

**Valores:**
- **Small:** 4px - Buttons, inputs, chips
- **Medium:** 8px - Cards, tables
- **Large:** 12px - Modals, large containers
- **Full:** 50% - Avatars, circular buttons

**Aplicação:**
- Inputs/Forms: 4px
- Buttons: 4px (Material Design padrão)
- Cards: 8px
- Modals: 12px

**Interactive Visualizations:**

- Color Theme Explorer: [ux-color-themes.html](./ux-color-themes.html)

---

## 4. Design Direction

### 4.1 Chosen Design Approach

**Direção de Design Escolhida: "Modern ERP with Premium Touch"**

**Descrição:**

Um ERP moderno que equilibra **função e forma**, priorizando eficiência operacional sem abrir mão de uma identidade visual premium e diferenciada. A interface comunica confiabilidade através de design limpo e previsível, mas usa roxo profundo (#6A1B9A) para criar diferenciação visual no mercado dominado por azuis corporativos.

**Características Visuais:**

1. **Layout Limpo e Funcional**
   - Espaçamento generoso (8px base unit)
   - Hierarquia visual clara (tipografia Roboto bem definida)
   - Cards com sombras sutis (elevation levels 1-4)
   - Borders arredondados suaves (4-8px)

2. **Palette Sofisticada**
   - Primary: Roxo Profundo #6A1B9A (transmite inovação, premium)
   - Accent: Dourado #F9A825 (usado com parcimônia para destaque)
   - Semantic colors: Verde/Amarelo/Vermelho/Azul para estados
   - Neutrals: Grayscale bem definido (#212121 a #FAFAFA)

3. **Iconografia Profissional**
   - Material Icons (não emojis)
   - Cores semânticas aplicadas consistentemente
   - Tamanhos: 16px-48px dependendo do contexto

4. **Interações Fluidas**
   - Feedback visual imediato (MatSnackBar, loading states)
   - Transições suaves (Material Design 3 animations)
   - Hover states claros
   - Focus indicators visíveis (outline roxo 2px)

**Rationale da Escolha:**

✅ **Diferenciação Competitiva:**
- Concorrentes (Conta Azul, Bling, Olist) usam azul corporativo tradicional
- Roxo profundo + dourado cria identidade única sem perder profissionalismo
- Transmite inovação e qualidade premium

✅ **Alinhamento com Resposta Emocional Desejada:**
- Confiança: Layout limpo e previsível, números claros, hierarquia visual forte
- Eficiência: Componentes Material otimizados, feedback imediato
- Controle: Estados sempre visíveis, ações claras
- Tranquilidade: Design familiar (Material Design), sem surpresas

✅ **Familiaridade com Redução de Curva de Aprendizado:**
- Material Design amplamente reconhecido (Gmail, Google Drive, Google Cloud)
- Padrões consistentes em toda a aplicação
- Usuários já conhecem componentes (buttons, forms, modals)

✅ **Funcionalidade sobre Estética:**
- Design não chama atenção excessiva
- Componentes servem à função primeiro
- Layouts adaptados ao contexto de uso (PDV ≠ B2B ≠ Mobile)

**Direções Alternativas Consideradas:**

**❌ Opção 1: "Corporate Blue Traditional"**
- Primary: Azul #0277BD
- Razão da Rejeição: Muito similar aos concorrentes, não diferencia
- Feedback do usuário: Solicitou roxo para diferenciação

**❌ Opção 2: "Vibrant Startup"**
- Primary: Roxo vibrante #A72DFF, gradientes, animações ousadas
- Razão da Rejeição: Muito informal para ERP B2B, pode parecer não confiável
- Trade-off: Profissionalismo > Criatividade radical

**✅ Opção 3: "Deep Purple Luxury" (ESCOLHIDA)**
- Primary: Roxo profundo #6A1B9A, Accent: Dourado #F9A825
- Razão da Escolha: Equilíbrio perfeito entre inovação e confiabilidade
- Feedback do usuário: "vamos usar o Tema 4"

**Princípios de Design Aplicados:**

1. **Clareza > Criatividade**
   - Botões fazem o que dizem
   - Estados sempre visíveis
   - Sem surpresas, sem ambiguidade

2. **Previsibilidade > Surpresa**
   - Mesmas ações, mesmos resultados, sempre
   - Padrões consistentes em toda aplicação

3. **Eficiência > Estética**
   - Interface profissional, mas função vem primeiro
   - Layouts otimizados para cada contexto de uso

4. **Confiança nos Dados**
   - Números batem, sincronização transparente, histórico completo
   - Feedback imediato de todas as operações

**Visual References:**

- **Color Theme Visualizer:** [ux-color-themes-purple.html](./ux-color-themes-purple.html)
  - 4 opções roxas exploradas (Tema 4 escolhido)

- **Dashboard Mockup:** [dashboard-mockup-purple.html](./dashboard-mockup-purple.html)
  - Comparação visual Tema 2 vs Tema 4 (usuário escolheu Tema 4)

- **Material Design 3 Reference:** [m3.material.io](https://m3.material.io/)
  - Base para todos os componentes e interações

**Exemplo de Aplicação:**

**Dashboard - Card de Métrica:**
```html
<mat-card class="metric-card" [style.border-left]="'4px solid #6A1B9A'">
  <mat-card-header>
    <mat-icon [style.color]="'#6A1B9A'">point_of_sale</mat-icon>
    <mat-card-title>Vendas Hoje</mat-card-title>
  </mat-card-header>
  <mat-card-content>
    <div class="metric-value" [style.color]="'#6A1B9A'">R$ 12.450</div>
    <div class="metric-change positive" [style.color]="'#2E7D32'">
      ↑ +15% vs ontem
    </div>
  </mat-card-content>
</mat-card>
```

**PDV - Botão Principal:**
```html
<button mat-raised-button color="primary" class="pdv-finalize-button">
  <mat-icon>check_circle</mat-icon>
  Finalizar Venda
</button>
```
**CSS:**
```scss
.pdv-finalize-button {
  min-width: 200px;
  min-height: 48px; // Touch-friendly
  font-size: 16px;
  font-weight: 500;
}
```

**Status:** ✅ **Direção de design definida, aprovada e documentada**

---

## 5. User Journey Flows

### 5.1 Critical User Paths

As jornadas abaixo mapeiam os fluxos mais críticos do sistema, aplicando melhores práticas de ERPs e considerando comportamento esperado por usuários familiarizados com sistemas de gestão.

---

### Jornada 1: Venda Rápida PDV com NFCe ⭐⭐⭐⭐⭐

**Persona:** Operador de Caixa
**Objetivo:** Completar venda em <30 segundos com NFCe emitida
**Frequência:** 50-100x por dia
**Contexto:** PDV Touchscreen (tablet 10" landscape)

**Fluxo Principal:**

**1. Iniciar Venda**
- Tela: PDV em modo "aguardando venda"
- Ação: Operador clica "Nova Venda" ou inicia escaneando produto
- Sistema: Cria carrinho vazio

**2. Adicionar Produtos**
- **Método A - Scanner (80% dos casos):**
  - Operador escaneia código de barras
  - Sistema identifica produto/variante automaticamente
  - Adiciona 1 unidade ao carrinho
  - Mostra feedback visual (✓ verde + nome do produto)

- **Método B - Busca Manual (20% dos casos):**
  - Operador clica no campo de busca
  - Digite nome ou SKU (autocomplete agressivo)
  - Sistema mostra lista filtrada em tempo real
  - Operador seleciona produto da lista
  - **Se produto tem variantes:** Mostra grid de variantes (ex: tamanhos/cores)
  - Operador seleciona variante específica
  - Adiciona 1 unidade ao carrinho

**3. Ajustar Quantidade (se necessário)**
- Padrão: 1 unidade por scan/seleção
- Para alterar: Operador toca no item no carrinho
- Sistema mostra teclado numérico touch
- Operador digita quantidade e confirma
- Sistema atualiza total

**4. Revisar Carrinho**
- Sistema mostra lista de itens com:
  - Nome do produto
  - Quantidade
  - Preço unitário
  - Subtotal
  - **Total geral em destaque**
- Operador pode remover itens (ícone lixeira)

**5. Finalizar Venda**
- Operador clica "Finalizar Venda" (botão roxo grande)
- Sistema solicita **forma de pagamento** (obrigatório para NFCe):
  - Dinheiro
  - Cartão Débito
  - Cartão Crédito
  - PIX
  - Múltiplas formas (pagamento misto)
- Operador seleciona forma(s) de pagamento
- Se dinheiro: campo para valor recebido (calcula troco)
- Operador confirma

**6. Processamento da Venda**
- Sistema processa em ordem:
  1. **Registra venda localmente** (ID gerado)
  2. **Baixa estoque imediatamente** (single source of truth)
  3. **Enfileira NFCe para emissão** (assíncrono)
  4. Mostra feedback: "Processando NFCe..." com spinner roxo

**7. Emissão NFCe - Cenário Sucesso**
- NFCe emitida com sucesso (5-10 segundos típico)
- Sistema mostra: ✓ "NFCe #XXXX emitida com sucesso!" (verde)
- Opções:
  - Imprimir cupom
  - Enviar por email/WhatsApp
  - Exibir QR Code
- Operador clica "Próxima Venda"
- Sistema limpa tela, pronto para nova venda

**8. Emissão NFCe - Cenário Falha**
- NFCe falhou (timeout, internet, SEFAZ offline)
- Sistema:
  - **Venda JÁ está registrada** (estoque já baixado)
  - Adiciona NFCe à **fila de retry** (tenta até 10x com intervalo crescente)
  - Mostra alerta: ⚠️ "Venda #1247 concluída, NFCe em fila de retry" (amarelo)
  - **Operador pode continuar** trabalhando normalmente
  - Gerente recebe notificação de NFCe pendente
- Operador clica "Próxima Venda"

**Decisões de UX:**
- ✅ **Não bloquear operação** se NFCe falhar (venda offline-first)
- ✅ **Autocomplete agressivo** em busca (min 2 caracteres)
- ✅ **Feedback visual imediato** em cada ação (✓, loading, erro)
- ✅ **Teclado numérico touch** para quantidades (tablets)
- ✅ **Grid de variantes** para seleção visual rápida
- ✅ **Forma de pagamento obrigatória** (compliance fiscal)

**Componentes Angular Material:**
- MatAutocomplete (busca de produtos)
- MatList (carrinho de compras)
- MatButton (ações primárias)
- MatDialog (grid de variantes, teclado numérico)
- MatProgressSpinner (processando NFCe)
- MatSnackBar (feedback de sucesso/erro)

**Tempo Esperado:** 15-30 segundos (venda típica de 3-5 itens)

---

### Jornada 2: Criar Ordem de Venda B2B com Consulta de Estoque ⭐⭐⭐⭐

**Persona:** Vendedor B2B
**Objetivo:** Criar ordem consultando estoque multi-depósito em tempo real
**Frequência:** 10-20x por dia
**Contexto:** Desktop (tela grande, múltiplas informações simultâneas)

**Fluxo Principal:**

**1. Iniciar Ordem**
- Tela: Dashboard → Menu "Vendas" → "Nova Ordem de Venda"
- Sistema: Abre formulário de ordem vazia

**2. Selecionar Cliente**
- Campo: Autocomplete de clientes
- Vendedor digita nome/CNPJ
- Sistema filtra e mostra lista
- Vendedor seleciona cliente
- Sistema carrega em **sidebar direita**:
  - Histórico de pedidos (últimos 5)
  - Produtos mais comprados
  - Condições comerciais (prazo, desconto)
- **Contexto sempre visível** durante montagem do pedido

**3. Adicionar Produtos à Ordem**
- Campo de busca: Autocomplete por nome/SKU
- Vendedor digita e seleciona produto
- Sistema mostra **modal de estoque**:
  ```
  Produto: Ração Premium 15kg

  Estoque Disponível por Local:
  - Depósito Central: 450 un (disponível: 380, reservado: 70)
  - Loja Centro: 120 un (disponível: 95, reservado: 25)
  - CD Zona Sul: 0 un

  Total Disponível: 475 unidades
  Cliente quer: ___ unidades
  ```
- Vendedor insere quantidade desejada
- Sistema valida se tem estoque disponível
- **Se sim:** Adiciona ao pedido (ainda não reserva)
- **Se não:** Mostra alerta "Estoque insuficiente - disponível: X unidades"
- Vendedor decide: ajustar quantidade ou backorder

**4. Revisar Ordem**
- Grid editável com produtos:
  - SKU | Produto | Qtd | Local de Origem | Preço Unit | Subtotal
  - Vendedor pode editar quantidades inline
  - Pode remover linhas
  - Pode alterar local de origem (dropdown)
- Sistema recalcula totais em tempo real
- Mostra resumo:
  - Subtotal
  - Desconto (se aplicável)
  - Frete
  - **Total**

**5. Confirmar Ordem**
- Vendedor clica "Confirmar Ordem"
- Sistema solicita confirmações:
  - Data de entrega esperada
  - Condições de pagamento (à vista, 30/60/90 dias)
  - Observações
- Vendedor preenche e confirma

**6. Processamento**
- Sistema processa:
  1. **Cria Ordem de Venda** (status: Confirmada)
  2. **Reserva estoque automaticamente** nos locais selecionados
  3. **Atualiza estoque disponível** (disponível = total - reservado)
  4. **Sincroniza com ML** (se produtos também estão no ML, reduz disponível lá)
  5. Gera número da ordem (#OV-2024-0347)

**7. Confirmação**
- Sistema mostra: ✓ "Ordem #OV-2024-0347 criada com sucesso!"
- Opções:
  - Imprimir ordem
  - Enviar por email
  - Ir para separação
  - Nova ordem
- **Reserva expira em 7 dias** se não faturada (configurável)

**Decisões de UX:**
- ✅ **Sidebar contextual** com histórico do cliente sempre visível
- ✅ **Modal de estoque multi-depósito** antes de adicionar item
- ✅ **Grid editável inline** para ajustes rápidos
- ✅ **Reserva automática** ao confirmar (previne overselling)
- ✅ **Validação em tempo real** de estoque disponível
- ✅ **Expiração de reserva** (libera estoque se não faturar)

**Componentes Angular Material:**
- MatAutocomplete (clientes, produtos)
- MatSideNav (contexto do cliente)
- MatDialog (modal de estoque)
- MatTable (grid de produtos editável)
- MatFormField (inputs)
- MatSelect (local de origem)
- MatDatepicker (data de entrega)

**Tempo Esperado:** 3-5 minutos (ordem típica de 5-10 itens)

---

### Jornada 3: Receber Mercadoria via Scanner Mobile ⭐⭐⭐⭐

**Persona:** Operador de Depósito
**Objetivo:** Receber produtos de Ordem de Compra usando smartphone
**Frequência:** 5-15x por dia
**Contexto:** Mobile (375x667+), operação com uma mão, ambiente de depósito

**Fluxo Principal:**

**1. Acessar Recebimento**
- Tela: Menu mobile → "Recebimento"
- Sistema mostra lista de **Ordens de Compra Pendentes**:
  ```
  OC #1245 - Fornecedor ABC
  Data: 10/12/2024 | 15 itens
  [Iniciar Recebimento]

  OC #1243 - Fornecedor XYZ
  Data: 08/12/2024 | 8 itens
  [Iniciar Recebimento]
  ```
- Operador seleciona OC para receber

**2. Ver Itens da OC**
- Sistema mostra lista de produtos esperados:
  ```
  Ração Premium 15kg
  Esperado: 100 un | Recebido: 0 un
  [Receber]

  Ração Standard 3kg
  Esperado: 200 un | Recebido: 0 un
  [Receber]
  ```
- Operador clica "Receber" no primeiro item

**3. Escanear Produto**
- Sistema ativa câmera do smartphone
- Operador aponta para código de barras
- Sistema reconhece código em <2 segundos
- **Se código bate com produto esperado:**
  - ✓ Feedback verde "Produto identificado!"
  - Passa para entrada de quantidade
- **Se código NÃO bate:**
  - ✕ Erro vermelho "Produto não encontrado nesta OC"
  - Opção: Escanear novamente ou buscar manualmente

**4. Informar Quantidade Recebida**
- Tela mostra:
  ```
  Ração Premium 15kg
  Esperado: 100 unidades

  Quantidade recebida: [___]
  ```
- Teclado numérico grande (touch-friendly)
- Operador digita quantidade (ex: 100)
- Botão "Confirmar" grande e acessível

**5. Registrar Custo (se necessário)**
- Se custo na OC estava "A definir":
  - Sistema solicita: "Custo unitário: R$ [___]"
  - Operador digita valor
- Se custo já estava na OC:
  - Mostra custo esperado: "R$ 45,00/un - confirma?"
  - Operador valida ou ajusta

**6. Confirmar Recebimento do Item**
- Sistema processa:
  1. **Adiciona quantidade ao estoque** (local padrão: depósito de recebimento)
  2. **Atualiza custo médio ponderado** se custo mudou
  3. **Marca item como recebido** na OC
  4. **Registra histórico de movimentação**
  5. **Sincroniza com ML** (se produto está lá, atualiza estoque disponível)

**7. Feedback**
- ✓ "100 unidades recebidas com sucesso!"
- Sistema volta para lista de itens da OC
- Item agora mostra: "Esperado: 100 | **Recebido: 100** ✓"
- Operador repete processo para próximo item

**8. Finalizar Recebimento da OC**
- Quando todos os itens recebidos (ou operador decide parar):
- Operador clica "Finalizar Recebimento"
- Sistema pergunta: "Recebimento completo ou parcial?"
- **Se completo:** OC marcada como "Recebida"
- **Se parcial:** OC fica "Parcialmente Recebida", pode receber resto depois

**Decisões de UX:**
- ✅ **Camera-first** para scanning (mais rápido que digitar)
- ✅ **Teclado numérico grande** (touch-friendly)
- ✅ **Uma mão** - botões acessíveis, sem alcances difíceis
- ✅ **Feedback imediato** (✓ ou ✕ com cores)
- ✅ **Recebimento parcial** permitido (flexibilidade)
- ✅ **Sincronização automática** com ML

**Componentes Angular Material:**
- MatList (OCs pendentes, itens)
- MatButton (ações grandes e acessíveis)
- MatFormField (quantidade, custo)
- MatProgressBar (progresso do recebimento)
- MatSnackBar (feedback)
- ZXing (biblioteca de scanning - não Material, mas integra bem)

**Tempo Esperado:** 30-60 segundos por item (OC típica: 5-15 minutos)

---

### Jornada 4: Sincronização Automática Mercado Livre ⭐⭐⭐⭐⭐

**Persona:** Sistema (automático) + Gerente (monitoring)
**Objetivo:** Venda local baixa estoque → ML atualiza em ≤5 minutos
**Frequência:** Contínua (tempo real)
**Contexto:** Background process + Dashboard de monitoramento

**Fluxo Automático:**

**1. Evento Trigger**
- **Trigger:** Venda concluída no PDV ou Ordem B2B confirmada
- Sistema detecta: "Produto X teve estoque baixado em Y unidades"
- Sistema verifica: "Produto X está publicado no Mercado Livre?"
- **Se sim:** Adiciona à fila de sincronização
- **Se não:** Ignora (não precisa sincronizar)

**2. Cálculo de Estoque Disponível para ML**
- Sistema calcula:
  ```
  Estoque Real Total: 100 un
  Estoque Reservado (B2B pendente): 20 un
  Estoque Disponível: 80 un
  Margem de Segurança (10%): -8 un
  Estoque a Anunciar no ML: 72 un
  ```
- **Margem de segurança** previne overselling se houver vendas simultâneas

**3. Chamar API do Mercado Livre**
- Sistema faz request:
  ```
  PUT /items/{ML_ITEM_ID}
  {
    "available_quantity": 72
  }
  ```
- Aguarda resposta (timeout: 10 segundos)

**4. Processamento - Cenário Sucesso**
- ML responde: 200 OK
- Sistema registra:
  - Timestamp da sincronização
  - Estoque enviado: 72 un
  - Status: Sincronizado ✓
- Dashboard mostra: "✓ Sincronizado há 2min" (verde)

**5. Processamento - Cenário Falha**
- ML responde: Erro (timeout, 429 rate limit, 500 server error)
- Sistema:
  1. **Adiciona à fila de retry**
  2. Tenta novamente com backoff exponencial:
     - Tentativa 1: Imediato
     - Tentativa 2: 1 minuto depois
     - Tentativa 3: 5 minutos depois
     - Tentativa 4: 15 minutos depois
     - Até 10 tentativas
  3. Se falhar todas: Marca como "Falha Permanente"
  4. **Notifica gerente** via dashboard

**6. Monitoramento (Gerente)**
- Dashboard mostra:
  ```
  Sincronização Mercado Livre

  Status: ✓ Operacional
  Última sincronização: há 2 minutos
  Fila de sincronização: 0 itens

  Produtos Sincronizados (últimas 24h): 47
  Falhas: 2 (retry bem-sucedido)
  Falhas Permanentes: 0
  ```
- Se houver falhas permanentes:
  - ⚠️ Alerta vermelho no dashboard
  - Lista produtos com falha
  - Botão "Tentar Novamente Manualmente"

**Decisões de UX:**
- ✅ **Assíncrono e não-bloqueante** (venda não espera ML)
- ✅ **Margem de segurança configurável** (previne overselling)
- ✅ **Retry com backoff exponencial** (resiliência)
- ✅ **Transparência via dashboard** (gerente vê o que está acontecendo)
- ✅ **Notificações de falhas críticas** (gerente toma ação)
- ✅ **≤5 minutos SLA** para sincronização (95% dos casos)

**Componentes Angular Material:**
- MatCard (dashboard de status)
- MatBadge (contador de falhas)
- MatIcon (status indicators)
- MatTable (lista de sincronizações)
- MatProgressBar (progresso de retry)

**Tempo Esperado:** <5 minutos (95% dos casos), retry até 30min em falhas

---

### Resumo das Jornadas Críticas

| Jornada | Persona | Criticidade | Tempo Esperado | Complexidade |
|---------|---------|-------------|----------------|--------------|
| Venda PDV com NFCe | Operador Caixa | ⭐⭐⭐⭐⭐ | 15-30 seg | Média |
| Ordem B2B com Estoque | Vendedor B2B | ⭐⭐⭐⭐ | 3-5 min | Alta |
| Recebimento Scanner | Operador Depósito | ⭐⭐⭐⭐ | 5-15 min | Média |
| Sync Mercado Livre | Sistema/Gerente | ⭐⭐⭐⭐⭐ | <5 min | Alta |

**Princípios Aplicados em Todas as Jornadas:**
- ✅ **Offline-first** quando possível (não bloquear operação)
- ✅ **Feedback imediato** em cada ação (visual + cores semânticas)
- ✅ **Validação em tempo real** (prevenir erros, não corrigir depois)
- ✅ **Automação máxima** (cálculos, sincronizações, reservas)
- ✅ **Transparência** (usuário sempre sabe o que está acontecendo)
- ✅ **Resiliência** (retry, filas, não perder dados)

---

## 6. Component Library

### 6.1 Component Strategy

**Estratégia:** Usar **100% Angular Material** para componentes de UI, complementando apenas onde necessário com bibliotecas especializadas.

---

#### 6.1.1 Core Components (Angular Material)

Todos os componentes abaixo são nativos do Angular Material e serão usados extensivamente:

**Buttons & Indicators:**
- `MatButton` - Botões padrão (flat, raised, stroked)
- `MatIconButton` - Botões com apenas ícone
- `MatFabButton` - Floating action button (mobile)
- `MatBadge` - Badges de notificação
- `MatChip` - Tags, filtros removíveis
- `MatProgressSpinner` - Loading circular
- `MatProgressBar` - Loading horizontal

**Forms & Inputs:**
- `MatFormField` - Container para inputs (appearance="outline")
- `MatInput` - Text inputs
- `MatSelect` - Dropdowns
- `MatAutocomplete` - Busca com autocomplete
- `MatCheckbox` - Checkboxes
- `MatRadioButton` - Radio buttons
- `MatSlideToggle` - Toggle switches
- `MatDatepicker` - Date picker
- `MatDateRangePicker` - Range de datas

**Navigation:**
- `MatToolbar` - Top/Bottom toolbars
- `MatSidenav` - Sidebar navigation
- `MatMenu` - Dropdown menus
- `MatTabs` - Tabs horizontais
- `MatPaginator` - Paginação de tabelas

**Layout:**
- `MatCard` - Cards de conteúdo
- `MatExpansionPanel` - Acordeões
- `MatStepper` - Wizards multi-step
- `MatDivider` - Divisores visuais
- `MatList` - Listas de itens

**Popups & Modals:**
- `MatDialog` - Modais/Dialogs
- `MatSnackBar` - Toasts/Notificações
- `MatTooltip` - Tooltips on hover
- `MatBottomSheet` - Bottom sheet (mobile)

**Data Display:**
- `MatTable` - Tabelas de dados
- `MatSort` - Sorting de tabelas
- `MatPaginator` - Paginação

---

#### 6.1.2 Specialized Libraries (Complementares)

**Para funcionalidades NÃO cobertas pelo Angular Material:**

**1. Charts/Gráficos:**
- **Biblioteca:** Chart.js com ng2-charts
- **Razão:** Angular Material não tem charts nativos
- **Uso:** Dashboard (gráficos de vendas, estoque, etc.)
- **Instalação:** `npm install chart.js ng2-charts`

**2. Barcode Scanning:**
- **Biblioteca:** ZXing (`@zxing/ngx-scanner`)
- **Razão:** Scanner de código de barras via câmera
- **Uso:** Recebimento Mobile, PDV (opcional)
- **Instalação:** `npm install @zxing/ngx-scanner`

**3. Date Formatting:**
- **Biblioteca:** date-fns
- **Razão:** Manipulação avançada de datas (formato brasileiro)
- **Uso:** Pipes customizados para exibição de datas
- **Instalação:** `npm install date-fns`

**4. Currency Formatting:**
- **Biblioteca:** Nativa Angular (`CurrencyPipe`)
- **Uso:** Exibir valores monetários em BRL (R$)
- **Configuração:** Locale pt-BR

---

#### 6.1.3 Component Mapping por Interface

**PDV Touchscreen:**

| Funcionalidade | Componente Angular Material | Custom Component |
|----------------|----------------------------|------------------|
| Busca de produtos | MatAutocomplete | ProductSearchComponent |
| Carrinho de compras | MatList + MatListItem | CartListComponent |
| Botão finalizar venda | MatButton (raised, primary) | - |
| Grid de variantes | MatDialog + MatGridList | VariantSelectorDialog |
| Teclado numérico | MatDialog + MatButton | NumericKeypadDialog |
| Feedback de sucesso | MatSnackBar | - |
| Loading NFCe | MatProgressSpinner | - |

**Ordem de Venda B2B:**

| Funcionalidade | Componente Angular Material | Custom Component |
|----------------|----------------------------|------------------|
| Autocomplete cliente | MatAutocomplete | CustomerAutocompleteComponent |
| Sidebar contextual | MatSidenav | CustomerContextSidebar |
| Modal de estoque | MatDialog + MatTable | StockCheckDialog |
| Grid de produtos | MatTable + MatSort | OrderItemsTable |
| Seleção de depósito | MatSelect | - |
| Date picker entrega | MatDatepicker | - |
| Totalizadores | MatCard | OrderTotalsCard |

**Recebimento Mobile:**

| Funcionalidade | Componente Angular Material | Custom Component |
|----------------|----------------------------|------------------|
| Lista OCs pendentes | MatList + MatListItem | PurchaseOrderList |
| Scanner de barcode | - | BarcodeScanner (ZXing) |
| Teclado numérico | MatDialog + MatButton | NumericKeypadDialog |
| Progress bar | MatProgressBar | - |
| Feedback visual | MatSnackBar | - |
| Botões grandes touch | MatButton (fab ou raised) | - |

**Dashboard:**

| Funcionalidade | Componente Angular Material | Custom Component |
|----------------|----------------------------|------------------|
| Cards de métricas | MatCard | MetricCard |
| Gráficos | - | ChartComponent (Chart.js) |
| Tabela de dados | MatTable + MatPaginator | DataTable |
| Filtros de data | MatDateRangePicker | - |
| Chips de filtro | MatChip | - |
| Tabs de seção | MatTabs | - |

---

#### 6.1.4 Custom Components a Criar

**Componentes Reutilizáveis (criar biblioteca shared/):**

**1. ProductSearchComponent**
- **Encapsula:** MatAutocomplete + lógica de busca
- **Props:** `searchFn`, `onSelect`, `placeholder`
- **Usado em:** PDV, Ordem B2B

**2. NumericKeypadDialog**
- **Encapsula:** MatDialog + MatButton grid (0-9)
- **Props:** `initialValue`, `maxValue`, `label`
- **Usado em:** PDV (quantidade), Recebimento Mobile (quantidade)

**3. MetricCard**
- **Encapsula:** MatCard + formatação de métricas
- **Props:** `title`, `value`, `change`, `icon`, `color`
- **Usado em:** Dashboard

**4. StockCheckDialog**
- **Encapsula:** MatDialog + MatTable (estoque por depósito)
- **Props:** `productId`
- **Usado em:** Ordem B2B

**5. CustomerContextSidebar**
- **Encapsula:** MatSidenav + histórico/condições do cliente
- **Props:** `customerId`
- **Usado em:** Ordem B2B

**6. BarcodeScanner**
- **Encapsula:** ZXing scanner + feedback visual
- **Props:** `onScan`, `mode` (continuous/single)
- **Usado em:** Recebimento Mobile, PDV (opcional)

**7. OrderItemsTable**
- **Encapsula:** MatTable editável + validações
- **Props:** `items`, `onUpdate`, `onDelete`
- **Usado em:** Ordem B2B

**8. CartListComponent**
- **Encapsula:** MatList + ações (remover, editar qtd)
- **Props:** `cartItems`, `onRemove`, `onUpdateQty`
- **Usado em:** PDV

**Estrutura Sugerida:**

```
src/app/shared/components/
├── product-search/
│   ├── product-search.component.ts
│   ├── product-search.component.html
│   └── product-search.component.scss
├── numeric-keypad-dialog/
│   ├── numeric-keypad-dialog.component.ts
│   ├── numeric-keypad-dialog.component.html
│   └── numeric-keypad-dialog.component.scss
├── metric-card/
│   ├── metric-card.component.ts
│   ├── metric-card.component.html
│   └── metric-card.component.scss
├── barcode-scanner/
│   ├── barcode-scanner.component.ts
│   ├── barcode-scanner.component.html
│   └── barcode-scanner.component.scss
└── ... (outros componentes)
```

---

#### 6.1.5 Third-Party Component Guidelines

**Quando EVITAR bibliotecas externas:**
- Se Angular Material já tem o componente
- Se pode ser criado facilmente com HTML/CSS/TS (não vale a pena dependência)
- Se a biblioteca não é mantida ativamente (último commit >1 ano)
- Se a biblioteca não suporta Angular 17+

**Quando USAR bibliotecas externas:**
- Funcionalidade complexa não coberta por Material (charts, scanning)
- Biblioteca bem mantida com comunidade ativa
- Reduz significativamente esforço de desenvolvimento
- Tem boa documentação e suporte TypeScript

**Aprovadas para uso:**
- ✅ Chart.js (charts)
- ✅ ZXing (barcode scanning)
- ✅ date-fns (date utilities)
- ✅ Angular CDK (utilities do Material)

**NÃO usar:**
- ❌ PrimeNG (redundante com Material)
- ❌ NgBootstrap (conflita com Material)
- ❌ jQuery plugins (não é Angular way)
- ❌ Bibliotecas de charts pesadas (Highcharts, D3 - overkill para MVP)

---

#### 6.1.6 Accessibility Compliance

**Todos os componentes (custom ou Material) devem ter:**

1. **ARIA Labels:**
   - Botões com apenas ícone: `aria-label`
   - Inputs: `aria-describedby`, `aria-required`
   - Modals: `aria-labelledby`, `aria-modal`

2. **Keyboard Navigation:**
   - Tab order lógico
   - Enter para confirmar
   - Esc para cancelar/fechar
   - Arrow keys para navegação de listas

3. **Focus Management:**
   - Focus visível (outline roxo 2px)
   - Focus trap em modals
   - Retorno de focus ao fechar modal

4. **Color Contrast:**
   - Texto: Mínimo 4.5:1 (WCAG AA)
   - Interactive elements: Mínimo 4.5:1

5. **Touch Targets:**
   - Mínimo 44x44px (mobile/tablet)
   - Recomendado 48x48px

**Exemplo de Custom Component Acessível:**

```typescript
@Component({
  selector: 'app-metric-card',
  template: `
    <mat-card role="region" [attr.aria-label]="title + ' metric'">
      <mat-card-header>
        <mat-icon [style.color]="color" aria-hidden="true">{{ icon }}</mat-icon>
        <mat-card-title id="metric-title-{{id}}">{{ title }}</mat-card-title>
      </mat-card-header>
      <mat-card-content>
        <div class="metric-value" aria-live="polite">
          {{ value | currency:'BRL' }}
        </div>
        <div class="metric-change" [class.positive]="change > 0" [class.negative]="change < 0">
          <span aria-label="Change from previous period">
            {{ change > 0 ? '↑' : '↓' }} {{ change | percent }}
          </span>
        </div>
      </mat-card-content>
    </mat-card>
  `
})
export class MetricCardComponent {
  @Input() title!: string;
  @Input() value!: number;
  @Input() change!: number;
  @Input() icon!: string;
  @Input() color: string = '#6A1B9A';
  id = Math.random().toString(36).substr(2, 9);
}
```

---

### Resumo da Estratégia de Componentes

**Princípio:** Máxima reutilização, mínima dependência externa

- ✅ **Base:** Angular Material (90% das necessidades)
- ✅ **Complemento:** Chart.js (gráficos), ZXing (scanning)
- ✅ **Custom:** 8-10 componentes compartilhados reutilizáveis
- ✅ **Accessibility:** WCAG AA em todos os componentes
- ✅ **Consistência:** Mesmo design system em toda aplicação

**Trade-offs Aceitos:**
- ❌ Não teremos componentes super avançados (AG-Grid, etc.) - Material Table é suficiente
- ❌ Não teremos charts super customizados - Chart.js básico atende MVP
- ✅ Ganho: Menor bundle size, menor complexidade, manutenção mais fácil

---

---

## 7. UX Pattern Decisions

### 7.1 Consistency Rules

As decisões abaixo garantem **consistência e previsibilidade** em toda a aplicação. Usuários devem reconhecer padrões e nunca se surpreender com comportamentos inesperados.

---

#### 7.1.1 Button Hierarchy & Placement

**Hierarquia Visual:**

1. **Primary Button** (Ação principal da tela)
   - Componente: `<button mat-raised-button color="primary">`
   - Cor: Roxo `#6A1B9A`
   - Uso: 1 por tela/modal (máximo)
   - Exemplos: "Finalizar Venda", "Confirmar Ordem", "Salvar"
   - Posição: Sempre à direita (leitura Z-pattern)

2. **Secondary Button** (Ações alternativas importantes)
   - Componente: `<button mat-stroked-button color="primary">`
   - Cor: Borda roxa, fundo branco
   - Uso: Ações secundárias na mesma tela
   - Exemplos: "Cancelar", "Voltar", "Imprimir"
   - Posição: À esquerda do primary button

3. **Tertiary Button** (Ações de menor importância)
   - Componente: `<button mat-button>`
   - Cor: Texto roxo, sem fundo
   - Uso: Ações terciárias ou links
   - Exemplos: "Pular", "Ver detalhes", "Editar"
   - Posição: Separado dos buttons principais

4. **Destructive Button** (Ações irreversíveis)
   - Componente: `<button mat-raised-button color="warn">`
   - Cor: Vermelho `#C62828`
   - Uso: Delete, cancelamento de pedido, ações irreversíveis
   - Exemplos: "Excluir Produto", "Cancelar Venda"
   - **SEMPRE requer confirmação** (modal)

**Regras de Posicionamento:**

```html
<!-- Padrão em modais/forms -->
<mat-dialog-actions align="end">
  <button mat-stroked-button (click)="onCancel()">Cancelar</button>
  <button mat-raised-button color="primary" (click)="onConfirm()">Confirmar</button>
</mat-dialog-actions>

<!-- Padrão em toolbars -->
<mat-toolbar>
  <span class="spacer"></span>
  <button mat-button>Voltar</button>
  <button mat-raised-button color="primary">Salvar</button>
</mat-toolbar>
```

**Touch Targets (Mobile/Tablet):**
- Mínimo: 44x44px (WCAG AAA)
- Recomendado: 48x48px
- Espaçamento mínimo entre botões: 8px

---

#### 7.1.2 Feedback Patterns

**1. Success Feedback**

- **Componente:** MatSnackBar (toast)
- **Cor:** Verde `#2E7D32`
- **Duração:** 3 segundos
- **Ícone:** `check_circle`
- **Som:** Nenhum (silencioso)
- **Exemplos:**
  - "✓ Venda #1247 concluída com sucesso!"
  - "✓ Estoque sincronizado com Mercado Livre"
  - "✓ Produto cadastrado"

```typescript
this.snackBar.open('✓ Venda concluída com sucesso!', 'Fechar', {
  duration: 3000,
  panelClass: ['success-snackbar']
});
```

**2. Error Feedback**

- **Componente:** MatSnackBar persistente + MatDialog (se crítico)
- **Cor:** Vermelho `#C62828`
- **Duração:** 5 segundos (ou até fechar manualmente)
- **Ícone:** `error`
- **Ação:** Botão "Tentar Novamente" se aplicável
- **Exemplos:**
  - "✕ Falha ao emitir NFCe - verifique internet"
  - "✕ Produto não encontrado"
  - "✕ Estoque insuficiente"

```typescript
this.snackBar.open('✕ Falha ao emitir NFCe', 'Tentar Novamente', {
  duration: 5000,
  panelClass: ['error-snackbar']
});
```

**3. Warning Feedback**

- **Componente:** MatSnackBar
- **Cor:** Amarelo/Dourado `#F9A825`
- **Duração:** 4 segundos
- **Ícone:** `warning`
- **Exemplos:**
  - "⚠ Estoque baixo - restam 5 unidades"
  - "⚠ NFCe em fila de retry"
  - "⚠ Reserva expira em 2 dias"

**4. Info Feedback**

- **Componente:** MatSnackBar
- **Cor:** Azul `#0277BD`
- **Duração:** 3 segundos
- **Ícone:** `info`
- **Exemplos:**
  - "ℹ Sincronização agendada para 14:30"
  - "ℹ 5 produtos adicionados"

**5. Loading States**

- **In-page:** MatProgressSpinner (centro da tela/componente)
- **In-button:** MatProgressSpinner small (16px) dentro do botão
- **Background task:** MatProgressBar (topo da tela)

```html
<!-- Loading em botão -->
<button mat-raised-button color="primary" [disabled]="loading">
  <mat-spinner *ngIf="loading" diameter="16"></mat-spinner>
  <span *ngIf="!loading">Confirmar</span>
  <span *ngIf="loading">Processando...</span>
</button>

<!-- Loading full-page -->
<mat-spinner *ngIf="loading" diameter="48"></mat-spinner>

<!-- Loading background (sync) -->
<mat-progress-bar mode="indeterminate" *ngIf="syncing"></mat-progress-bar>
```

---

#### 7.1.3 Form Patterns

**Label Position:**
- **Desktop:** Labels acima dos campos (melhor leitura)
- **Mobile:** Labels flutuantes (Material Design padrão)

**Validation Timing:**
- **On Blur:** Valida quando usuário sai do campo
- **On Submit:** Valida tudo antes de submeter
- **NUNCA:** Validação "enquanto digita" (frustrante)

**Error Display:**
- **Componente:** MatError (abaixo do campo)
- **Cor:** Vermelho `#C62828`
- **Mensagens:** Claras e acionáveis

```html
<mat-form-field appearance="outline">
  <mat-label>Quantidade</mat-label>
  <input matInput type="number" formControlName="quantity" required>
  <mat-error *ngIf="form.get('quantity').hasError('required')">
    Quantidade é obrigatória
  </mat-error>
  <mat-error *ngIf="form.get('quantity').hasError('min')">
    Quantidade deve ser maior que 0
  </mat-error>
  <mat-hint>Estoque disponível: 120 unidades</mat-hint>
</mat-form-field>
```

**Required Fields:**
- Indicador: Asterisco vermelho `*` ao lado do label
- **Não usar** "Todos os campos são obrigatórios" - marcar individualmente

**Help Text:**
- **Componente:** MatHint (abaixo do campo)
- **Cor:** Cinza secundário `#757575`
- **Uso:** Explicações contextuais, limites, formatos esperados

**Form Actions:**
- **Posição:** Final do formulário, alinhados à direita
- **Padrão:** Cancelar (secondary) + Salvar (primary)
- **Validação:** Desabilitar "Salvar" se form inválido

---

#### 7.1.4 Modal Patterns

**Size Variants:**
- **Small:** 400px - Confirmações simples
- **Medium:** 600px - Formulários padrão
- **Large:** 800px - Grids, múltiplos campos
- **Full-screen:** Mobile only (breakpoint <600px)

**Dismiss Behavior:**
- **ESC key:** Fecha modal (equivale a "Cancelar")
- **Click fora:** Fecha modal SE não houver mudanças não salvas
- **Click fora com mudanças:** Mostra confirmação "Descartar alterações?"
- **Header X:** Sempre presente no topo direito

**Focus Management:**
- Ao abrir: Foco no primeiro campo editável
- Ao fechar: Retorna foco ao elemento que abriu

**Exemplo:**

```html
<!-- Modal de confirmação (small) -->
<h2 mat-dialog-title>Confirmar Exclusão</h2>
<mat-dialog-content>
  Tem certeza que deseja excluir o produto "Ração Premium 15kg"?
  <br>Esta ação não pode ser desfeita.
</mat-dialog-content>
<mat-dialog-actions align="end">
  <button mat-stroked-button mat-dialog-close>Cancelar</button>
  <button mat-raised-button color="warn" (click)="onDelete()">Excluir</button>
</mat-dialog-actions>
```

---

#### 7.1.5 Navigation Patterns

**Active State:**
- **Cor:** Roxo `#6A1B9A` (background leve `#F3E5F5` + borda esquerda 4px `#6A1B9A`)
- **Ícone:** Roxo `#6A1B9A`
- **Texto:** Bold

**Breadcrumbs:**
- **Uso:** Desktop only (telas com drill-down)
- **Formato:** `Dashboard > Vendas > Ordem #1247`
- **Separador:** `>` (Material Icon `chevron_right`)
- **Link:** Último item não é link (current page)

**Back Button:**
- **Mobile:** Header com seta voltar `<` (Material Icon `arrow_back`)
- **Desktop:** Breadcrumb substitui (não duplicar)

**Exemplo:**

```html
<!-- Breadcrumb -->
<nav aria-label="breadcrumb">
  <ol class="breadcrumb">
    <li><a routerLink="/dashboard">Dashboard</a></li>
    <li><a routerLink="/vendas">Vendas</a></li>
    <li aria-current="page">Ordem #1247</li>
  </ol>
</nav>

<!-- Mobile back -->
<mat-toolbar>
  <button mat-icon-button (click)="goBack()">
    <mat-icon>arrow_back</mat-icon>
  </button>
  <span>Ordem #1247</span>
</mat-toolbar>
```

---

#### 7.1.6 Empty State Patterns

**Três Cenários:**

**1. First Use (primeira vez na tela)**
- **Mensagem:** Positiva e orientadora
- **Ícone:** Grande (48px), neutro
- **CTA:** Botão primário para ação inicial
- **Exemplo:**
  ```
  [Ícone inventory_2]
  Nenhum produto cadastrado ainda
  Comece adicionando seu primeiro produto!
  [Cadastrar Produto]
  ```

**2. No Results (busca sem resultados)**
- **Mensagem:** Sugere ajustar filtros/busca
- **Ícone:** Médio (32px)
- **CTA:** Limpar filtros ou nova busca
- **Exemplo:**
  ```
  [Ícone search_off]
  Nenhum resultado para "Ração Gatos"
  Tente buscar por outro termo ou limpar os filtros
  [Limpar Filtros]
  ```

**3. Cleared Content (usuário limpou tudo)**
- **Mensagem:** Neutra, confirma ação
- **Sem CTA** (usuário acabou de limpar)
- **Exemplo:**
  ```
  [Ícone delete_sweep]
  Carrinho vazio
  ```

---

#### 7.1.7 Confirmation Patterns

**Quando Pedir Confirmação:**

✅ **SEMPRE confirmar:**
- Delete de registros
- Cancelamento de pedidos/vendas
- Ações financeiras irreversíveis
- Alterações que afetam múltiplos registros

❌ **NUNCA confirmar:**
- Save/Update (já tem validação)
- Ações reversíveis (Undo disponível)
- Navegação entre telas (a menos que haja mudanças não salvas)

**Formato da Confirmação:**

```html
<!-- Modal de confirmação destrutiva -->
<h2 mat-dialog-title>Cancelar Ordem de Venda?</h2>
<mat-dialog-content>
  Tem certeza que deseja cancelar a Ordem #OV-2024-0347?
  <br><br>
  <strong>Esta ação irá:</strong>
  <ul>
    <li>Liberar 150 unidades de estoque reservado</li>
    <li>Notificar o cliente sobre o cancelamento</li>
    <li>Registrar histórico de cancelamento</li>
  </ul>
  <br>
  Esta ação não pode ser desfeita.
</mat-dialog-content>
<mat-dialog-actions align="end">
  <button mat-stroked-button mat-dialog-close>Não, manter ordem</button>
  <button mat-raised-button color="warn" (click)="onCancelOrder()">
    Sim, cancelar ordem
  </button>
</mat-dialog-actions>
```

**Confirmação de Mudanças Não Salvas:**

```typescript
// Route Guard
canDeactivate(): Observable<boolean> | boolean {
  if (this.form.dirty) {
    return this.dialog.open(ConfirmDialog, {
      data: {
        title: 'Descartar alterações?',
        message: 'Você tem alterações não salvas. Deseja descartá-las?'
      }
    }).afterClosed();
  }
  return true;
}
```

---

#### 7.1.8 Notification Patterns

**Placement:**
- **Desktop:** Topo direito (não bloqueia conteúdo)
- **Mobile:** Topo (fullwidth)

**Duration:**
- Success: 3 segundos
- Info: 3 segundos
- Warning: 4 segundos
- Error: 5 segundos ou manual dismiss

**Stacking:**
- Máximo 3 notificações simultâneas
- Novas empurram antigas para cima
- Automática remoção das mais antigas

**Priority Levels:**

1. **Critical (erro crítico):** Vermelho, persiste até fechar, som opcional
2. **High (warning):** Amarelo, 4-5 segundos
3. **Normal (success/info):** Verde/Azul, 3 segundos

**Exemplo com Ação:**

```typescript
this.snackBar.open('Sincronização falhou', 'Tentar Novamente', {
  duration: 5000,
  panelClass: ['error-snackbar']
}).onAction().subscribe(() => {
  this.retrySyncronization();
});
```

---

#### 7.1.9 Search & Autocomplete Patterns

**Trigger Behavior:**
- **Min caracteres:** 2 (não buscar com 1 caractere)
- **Debounce:** 300ms (não fazer request a cada keystroke)
- **Loading:** Spinner dentro do campo

**Results Display:**
- **Max results:** 10 items
- **Highlight:** Termo buscado em bold
- **Empty state:** "Nenhum resultado para '[termo]'"
- **Keyboard navigation:** ↑↓ para navegar, Enter para selecionar

**Exemplo:**

```html
<mat-form-field appearance="outline">
  <mat-label>Buscar Produto</mat-label>
  <input matInput [matAutocomplete]="auto" formControlName="search">
  <mat-icon matSuffix *ngIf="loading">
    <mat-spinner diameter="20"></mat-spinner>
  </mat-icon>
  <mat-autocomplete #auto="matAutocomplete">
    <mat-option *ngFor="let product of filteredProducts" [value]="product">
      <span [innerHTML]="highlightTerm(product.name, searchTerm)"></span>
      <small>SKU: {{ product.sku }}</small>
    </mat-option>
    <mat-option *ngIf="filteredProducts.length === 0" disabled>
      Nenhum resultado
    </mat-option>
  </mat-autocomplete>
</mat-form-field>
```

---

#### 7.1.10 Date & Time Patterns

**Formato de Exibição:**
- **Data curta:** `14/12/2024` (padrão brasileiro)
- **Data longa:** `14 de dezembro de 2024`
- **Data + hora:** `14/12/2024 às 15:30`
- **Hora:** `15:30` (24h - padrão brasileiro)
- **Relativo (recente):** `há 5 minutos`, `há 2 horas`, `ontem`

**Date Picker:**
- **Componente:** MatDatepicker
- **Início da semana:** Domingo (Brasil)
- **Locale:** `pt-BR`
- **Formato de input:** `dd/mm/yyyy`
- **Range:** MatDateRangePicker para períodos

**Timezone:**
- **Padrão:** America/Sao_Paulo (BRT/BRST)
- **Exibir timezone:** Apenas em relatórios ou logs técnicos
- **Não exibir:** Interface do usuário (assumir BRT)

**Exemplo:**

```typescript
// providers
import { MAT_DATE_LOCALE } from '@angular/material/core';

providers: [
  { provide: MAT_DATE_LOCALE, useValue: 'pt-BR' }
]
```

```html
<!-- Date picker simples -->
<mat-form-field appearance="outline">
  <mat-label>Data da Venda</mat-label>
  <input matInput [matDatepicker]="picker" formControlName="date">
  <mat-datepicker-toggle matSuffix [for]="picker"></mat-datepicker-toggle>
  <mat-datepicker #picker></mat-datepicker>
</mat-form-field>

<!-- Date range picker -->
<mat-form-field appearance="outline">
  <mat-label>Período</mat-label>
  <mat-date-range-input [rangePicker]="rangePicker">
    <input matStartDate placeholder="Início" formControlName="startDate">
    <input matEndDate placeholder="Fim" formControlName="endDate">
  </mat-date-range-input>
  <mat-datepicker-toggle matSuffix [for]="rangePicker"></mat-datepicker-toggle>
  <mat-date-range-picker #rangePicker></mat-date-range-picker>
</mat-form-field>
```

---

### Resumo dos Padrões de UX

**Princípios Aplicados:**
- ✅ **Consistência** - Mesmos padrões em toda a aplicação
- ✅ **Previsibilidade** - Usuário sabe o que esperar
- ✅ **Feedback Imediato** - Toda ação tem resposta visual
- ✅ **Clareza sobre Criatividade** - Sem surpresas, sem ambiguidade
- ✅ **Acessibilidade** - Touch targets, ARIA, keyboard navigation
- ✅ **Material Design 3** - Componentes nativos do Angular Material

**Regras de Ouro:**
1. **1 primary button por tela** - clareza de ação principal
2. **Sempre confirmar ações destrutivas** - prevenir erros
3. **Loading states em todas as operações assíncronas** - transparência
4. **Validação on blur, não while typing** - menos frustrante
5. **Success feedback em 3 segundos, error em 5 segundos** - tempo suficiente para ler
6. **Empty states orientadores, não apenas "vazio"** - guiar usuário
7. **Breadcrumbs em desktop, back button em mobile** - navegação clara
8. **Date/time em formato brasileiro** - familiaridade

---

---

## 8. Responsive Design & Accessibility

### 8.1 Responsive Strategy

O Estoque Central é **web responsive + PWA** com três interfaces especializadas que se adaptam aos contextos de uso. A estratégia de responsividade prioriza **função sobre forma** - cada breakpoint otimiza para o caso de uso principal.

---

#### 8.1.1 Breakpoints (Material Design 3)

Seguimos os breakpoints padrão do Angular Material com adaptações para nossos contextos:

| Breakpoint | Range | Dispositivo Típico | Interface Primária |
|------------|-------|-------------------|-------------------|
| **xs** | <600px | Smartphone portrait | Recebimento Mobile |
| **sm** | 600-959px | Smartphone landscape, Tablet portrait | PDV, Recebimento |
| **md** | 960-1279px | Tablet landscape | PDV Touchscreen |
| **lg** | 1280-1919px | Desktop, Laptop | Ordem B2B, Dashboard |
| **xl** | ≥1920px | Desktop wide | Dashboard, Relatórios |

**Breakpoints Críticos:**

- **<600px (xs):** Mobile-first para Recebimento
  - Layout: Single-column
  - Navegação: Bottom nav ou hamburger menu
  - Inputs: Large touch targets (48px)
  - Modals: Full-screen

- **960-1279px (md):** Tablet landscape para PDV
  - Layout: 2-column (carrinho + produtos)
  - Navegação: Tabs horizontais
  - Inputs: Touch-optimized (44px)
  - Modals: Medium (600px)

- **≥1280px (lg+):** Desktop para Ordem B2B
  - Layout: 3-column (nav + content + sidebar)
  - Navegação: Permanent sidebar
  - Inputs: Padrão desktop (40px)
  - Modals: Large (800px)

---

#### 8.1.2 Layout Adaptation Strategy

**Mobile-First Approach:**
- Base CSS para mobile (xs)
- Progressive enhancement para desktop
- Media queries com `min-width` (não `max-width`)

**Três Padrões de Layout:**

**1. PDV Touchscreen (Tablet Landscape - md)**

```html
<div class="pdv-layout">
  <!-- Fullscreen, 2 colunas -->
  <div class="product-search-column">
    <!-- Busca + Grid de produtos -->
  </div>
  <div class="cart-column">
    <!-- Carrinho + Total + Ações -->
  </div>
</div>
```

**CSS:**
```scss
.pdv-layout {
  display: grid;
  grid-template-columns: 1fr 1fr; // 50/50 em tablet landscape
  height: 100vh;

  @media (max-width: 959px) {
    grid-template-columns: 1fr; // Single column em portrait
  }
}
```

**2. Ordem de Venda B2B (Desktop - lg+)**

```html
<div class="b2b-layout">
  <mat-sidenav-container>
    <!-- Sidebar esquerda: Navegação -->
    <mat-sidenav mode="side" opened>
      <app-main-nav></app-main-nav>
    </mat-sidenav>

    <!-- Conteúdo central -->
    <mat-sidenav-content>
      <app-order-form></app-order-form>
    </mat-sidenav-content>

    <!-- Sidebar direita: Contexto do cliente -->
    <mat-sidenav mode="side" position="end" opened>
      <app-customer-context></app-customer-context>
    </mat-sidenav>
  </mat-sidenav-container>
</div>
```

**CSS:**
```scss
.b2b-layout {
  mat-sidenav {
    width: 260px; // Nav esquerda

    &[position="end"] {
      width: 340px; // Contexto direita
    }
  }

  @media (max-width: 1279px) {
    mat-sidenav {
      mode: 'over'; // Overlay em tablet
    }
  }
}
```

**3. Recebimento Mobile (Smartphone - xs)**

```html
<div class="mobile-layout">
  <!-- Header fixo -->
  <mat-toolbar color="primary">
    <button mat-icon-button (click)="goBack()">
      <mat-icon>arrow_back</mat-icon>
    </button>
    <span>Recebimento</span>
  </mat-toolbar>

  <!-- Conteúdo scrollável -->
  <div class="scrollable-content">
    <app-receiving-scanner></app-receiving-scanner>
  </div>

  <!-- Footer fixo com ações -->
  <div class="mobile-actions">
    <button mat-raised-button color="primary" fullWidth>
      Confirmar Recebimento
    </button>
  </div>
</div>
```

**CSS:**
```scss
.mobile-layout {
  display: flex;
  flex-direction: column;
  height: 100vh;

  .scrollable-content {
    flex: 1;
    overflow-y: auto;
    padding: 16px;
  }

  .mobile-actions {
    padding: 16px;
    box-shadow: 0 -2px 4px rgba(0,0,0,0.1);

    button {
      width: 100%; // Full-width buttons em mobile
      height: 48px; // Large touch target
    }
  }
}
```

---

#### 8.1.3 Navigation Adaptation

**Desktop (≥1280px):**
- **Permanent sidebar** (sempre visível)
- Breadcrumbs para drill-down
- Top toolbar com user menu

**Tablet (960-1279px):**
- **Dismissible sidebar** (overlay, fecha após navegar)
- Tabs horizontais para seções principais
- Top toolbar com hamburger menu

**Mobile (<960px):**
- **Bottom navigation** (5 itens max) OU hamburger menu
- No breadcrumbs (usa back button)
- Top toolbar minimal (title + actions)

**Exemplo:**

```html
<!-- Desktop -->
<mat-sidenav-container *ngIf="isDesktop">
  <mat-sidenav mode="side" opened>
    <app-main-nav></app-main-nav>
  </mat-sidenav>
  <mat-sidenav-content>
    <router-outlet></router-outlet>
  </mat-sidenav-content>
</mat-sidenav-container>

<!-- Mobile -->
<div *ngIf="isMobile">
  <mat-toolbar>
    <button mat-icon-button (click)="sidenav.toggle()">
      <mat-icon>menu</mat-icon>
    </button>
    <span>{{ pageTitle }}</span>
  </mat-toolbar>

  <mat-sidenav-container>
    <mat-sidenav #sidenav mode="over">
      <app-main-nav></app-main-nav>
    </mat-sidenav>
    <mat-sidenav-content>
      <router-outlet></router-outlet>
    </mat-sidenav-content>
  </mat-sidenav-container>

  <!-- Bottom nav -->
  <mat-toolbar class="bottom-nav">
    <button mat-button routerLink="/dashboard">
      <mat-icon>dashboard</mat-icon>
      Dashboard
    </button>
    <button mat-button routerLink="/vendas">
      <mat-icon>point_of_sale</mat-icon>
      Vendas
    </button>
    <button mat-button routerLink="/estoque">
      <mat-icon>inventory_2</mat-icon>
      Estoque
    </button>
  </mat-toolbar>
</div>
```

---

#### 8.1.4 Component Adaptation Rules

**Tables → Cards em Mobile:**

Desktop (MatTable):
```html
<table mat-table [dataSource]="orders">
  <ng-container matColumnDef="id">
    <th mat-header-cell *matHeaderCellDef>Ordem</th>
    <td mat-cell *matCellDef="let order">{{ order.id }}</td>
  </ng-container>
  <!-- ... mais colunas -->
</table>
```

Mobile (MatCard):
```html
<mat-card *ngFor="let order of orders" class="mobile-order-card">
  <mat-card-header>
    <mat-card-title>Ordem {{ order.id }}</mat-card-title>
    <mat-card-subtitle>{{ order.date }}</mat-card-subtitle>
  </mat-card-header>
  <mat-card-content>
    <p>Cliente: {{ order.customer }}</p>
    <p>Total: {{ order.total | currency:'BRL' }}</p>
  </mat-card-content>
  <mat-card-actions>
    <button mat-button>Ver Detalhes</button>
  </mat-card-actions>
</mat-card>
```

**Forms → Full-width em Mobile:**

```scss
mat-form-field {
  @media (min-width: 960px) {
    width: 300px; // Fixed width em desktop
  }

  @media (max-width: 959px) {
    width: 100%; // Full-width em mobile
  }
}
```

**Dialogs → Full-screen em Mobile:**

```typescript
const dialogRef = this.dialog.open(ProductDialog, {
  width: this.isMobile ? '100vw' : '600px',
  maxWidth: this.isMobile ? '100vw' : '80vw',
  height: this.isMobile ? '100vh' : 'auto',
  panelClass: this.isMobile ? 'fullscreen-dialog' : ''
});
```

---

### 8.2 Accessibility (WCAG AA Compliance)

Comprometimento com **WCAG 2.1 Level AA** em todas as interfaces.

---

#### 8.2.1 Color Contrast

**Texto:**
- **Normal text (16px):** Mínimo 4.5:1
- **Large text (≥18px ou ≥14px bold):** Mínimo 3:1
- **Interactive elements:** Mínimo 4.5:1

**Validação das Cores Primárias:**

| Combinação | Contraste | Status |
|------------|-----------|--------|
| `#6A1B9A` (roxo) em `#FFFFFF` (branco) | 8.2:1 | ✅ AAA |
| `#212121` (texto) em `#FFFFFF` (branco) | 16.1:1 | ✅ AAA |
| `#757575` (secundário) em `#FFFFFF` | 4.6:1 | ✅ AA |
| `#F9A825` (dourado) em `#FFFFFF` | 2.1:1 | ❌ FAIL |

**Correção para Dourado:**
- ❌ Nunca usar `#F9A825` como texto em fundo branco
- ✅ Usar apenas como background (com texto escuro `#212121`)
- ✅ Usar como borda/ícone (não depende de texto)

**Exemplo Correto:**

```html
<!-- ✅ Correto: Dourado como background -->
<div class="warning-banner" style="background: #F9A825; color: #212121;">
  ⚠ Estoque baixo - restam 5 unidades
</div>

<!-- ❌ Incorreto: Dourado como texto -->
<p style="color: #F9A825;">Atenção!</p>

<!-- ✅ Correto: Dourado como ícone -->
<mat-icon style="color: #F9A825;">warning</mat-icon>
```

---

#### 8.2.2 Keyboard Navigation

**Todos os elementos interativos devem ser acessíveis via teclado:**

**Tab Order:**
- Ordem lógica de leitura (Z-pattern)
- `tabindex="0"` para elementos customizados interativos
- `tabindex="-1"` para elementos que não devem estar no tab order

**Atalhos de Teclado:**

| Ação | Atalho | Contexto |
|------|--------|----------|
| Abrir busca de produtos | `/` | PDV, Ordem B2B |
| Finalizar venda | `Ctrl+Enter` | PDV |
| Cancelar ação | `Esc` | Modals, Forms |
| Navegar entre tabs | `Ctrl+Tab` | Dashboard |
| Abrir menu | `Alt+M` | Global |

**Focus Management:**

```scss
// Focus visível e claro
*:focus {
  outline: 2px solid #6A1B9A; // Roxo primary
  outline-offset: 2px;
}

// Remover outline apenas se usando mouse (não teclado)
body:not(.user-is-tabbing) *:focus {
  outline: none;
}
```

```typescript
// Detectar uso de Tab para adicionar classe
document.body.addEventListener('keydown', (e) => {
  if (e.key === 'Tab') {
    document.body.classList.add('user-is-tabbing');
  }
});

document.body.addEventListener('mousedown', () => {
  document.body.classList.remove('user-is-tabbing');
});
```

**Exemplo de Modal Acessível:**

```html
<div role="dialog"
     aria-labelledby="dialog-title"
     aria-describedby="dialog-description"
     aria-modal="true">

  <h2 id="dialog-title">Confirmar Exclusão</h2>
  <p id="dialog-description">
    Tem certeza que deseja excluir este produto?
  </p>

  <button mat-stroked-button mat-dialog-close>Cancelar</button>
  <button mat-raised-button color="warn" (click)="onDelete()">Excluir</button>
</div>
```

---

#### 8.2.3 ARIA Labels & Roles

**Uso de ARIA:**

**Botões com apenas ícone:**
```html
<!-- ❌ Incorreto: Sem label -->
<button mat-icon-button>
  <mat-icon>delete</mat-icon>
</button>

<!-- ✅ Correto: Com aria-label -->
<button mat-icon-button aria-label="Excluir produto">
  <mat-icon>delete</mat-icon>
</button>
```

**Loading States:**
```html
<button mat-raised-button [disabled]="loading" aria-busy="{{loading}}">
  <mat-spinner *ngIf="loading" diameter="16" aria-hidden="true"></mat-spinner>
  <span>{{ loading ? 'Processando...' : 'Confirmar' }}</span>
</button>
```

**Form Fields:**
```html
<mat-form-field>
  <mat-label>Quantidade</mat-label>
  <input matInput
         type="number"
         formControlName="quantity"
         aria-label="Quantidade do produto"
         aria-describedby="quantity-hint"
         aria-required="true">
  <mat-hint id="quantity-hint">Estoque disponível: 120 unidades</mat-hint>
  <mat-error role="alert">Quantidade é obrigatória</mat-error>
</mat-form-field>
```

**Alerts/Notifications:**
```html
<!-- Success notification -->
<div role="alert" aria-live="polite" class="success-snackbar">
  ✓ Venda concluída com sucesso!
</div>

<!-- Error notification -->
<div role="alert" aria-live="assertive" class="error-snackbar">
  ✕ Falha ao emitir NFCe - verifique internet
</div>
```

---

#### 8.2.4 Touch Targets (Mobile/Tablet)

**Tamanhos Mínimos:**

| Elemento | WCAG AA (Mínimo) | Recomendado | Uso |
|----------|------------------|-------------|-----|
| Buttons | 44x44px | 48x48px | Touch primário |
| Icons | 44x44px | 48x48px | Actions |
| Links | 44x44px height | 48x48px | In-text links |
| Checkboxes | 24x24px | 32x32px | Forms |

**Espaçamento:**
- Mínimo 8px entre touch targets
- Recomendado 16px para ações críticas

**Exemplo PDV (Touchscreen):**

```scss
.pdv-button {
  min-width: 120px;
  min-height: 48px; // Touch-friendly
  margin: 8px; // Espaçamento
  font-size: 16px; // Legível
}

.pdv-product-card {
  padding: 16px;
  cursor: pointer;

  &:active {
    background: #F3E5F5; // Feedback visual ao tocar
  }
}
```

---

#### 8.2.5 Screen Reader Support

**Landmarks:**

```html
<header role="banner">
  <mat-toolbar>...</mat-toolbar>
</header>

<nav role="navigation" aria-label="Menu principal">
  <app-main-nav></app-main-nav>
</nav>

<main role="main">
  <router-outlet></router-outlet>
</main>

<aside role="complementary" aria-label="Contexto do cliente">
  <app-customer-context></app-customer-context>
</aside>

<footer role="contentinfo">
  <app-footer></app-footer>
</footer>
```

**Skip Links:**

```html
<!-- Primeiro elemento do body -->
<a href="#main-content" class="skip-link">
  Pular para conteúdo principal
</a>

<main id="main-content" tabindex="-1">
  <!-- Conteúdo -->
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

  &:focus {
    top: 0; // Aparece ao receber foco via Tab
  }
}
```

**Dynamic Content Announcements:**

```typescript
// Serviço de anúncios para screen readers
@Injectable({ providedIn: 'root' })
export class LiveAnnouncer {
  constructor(private liveAnnouncer: LiveAnnouncer) {}

  announceSuccess(message: string) {
    this.liveAnnouncer.announce(message, 'polite');
  }

  announceError(message: string) {
    this.liveAnnouncer.announce(message, 'assertive');
  }
}

// Uso:
this.liveAnnouncer.announceSuccess('Produto adicionado ao carrinho');
```

---

#### 8.2.6 Forms Accessibility

**Required Fields:**
```html
<mat-form-field>
  <mat-label>Nome do Produto <span class="required">*</span></mat-label>
  <input matInput
         formControlName="name"
         required
         aria-required="true">
</mat-form-field>
```

**Error Messaging:**
```html
<mat-error role="alert" id="name-error">
  Nome do produto é obrigatório
</mat-error>
```

**Autocomplete:**
```html
<input matInput
       type="text"
       formControlName="email"
       autocomplete="email"
       aria-autocomplete="list">
```

---

### 8.3 Performance & PWA

**Progressive Web App Features:**

**Manifest:**
```json
{
  "name": "Estoque Central",
  "short_name": "Estoque",
  "theme_color": "#6A1B9A",
  "background_color": "#FAFAFA",
  "display": "standalone",
  "scope": "/",
  "start_url": "/dashboard",
  "icons": [
    {
      "src": "icon-192x192.png",
      "sizes": "192x192",
      "type": "image/png"
    },
    {
      "src": "icon-512x512.png",
      "sizes": "512x512",
      "type": "image/png"
    }
  ]
}
```

**Service Worker:**
- Cache-first para assets estáticos
- Network-first para API calls
- Offline fallback para telas críticas

**Performance Targets:**
- First Contentful Paint: <1.5s
- Time to Interactive: <3s
- Lighthouse Score: ≥90

---

---

## 9. Implementation Guidance

### 9.1 Angular Material Theme Configuration

**Setup do Tema Roxo no Angular:**

**1. Instalar Angular Material:**
```bash
ng add @angular/material
```

**2. Criar Custom Theme (`src/styles/theme.scss`):**

```scss
@use '@angular/material' as mat;

// Definir paleta customizada (Deep Purple Luxury)
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
), 500); // Usar 500 como padrão

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

**3. Importar no `styles.scss`:**

```scss
@use './styles/theme';

// Global styles
html, body {
  height: 100%;
  margin: 0;
  font-family: Roboto, "Helvetica Neue", sans-serif;
}

// Custom classes para cores semânticas
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
```

---

### 9.2 Priorização de Implementação

**Fase 1: Fundação (Sprint 1-2)**

✅ **Semana 1-2:**
- Setup Angular Material com tema roxo
- Componentes base: Buttons, Forms, Cards, Toolbar
- Layout responsivo base (breakpoints)
- Navegação principal (sidebar desktop, hamburger mobile)

✅ **Entregável:** Shell da aplicação navegável

**Fase 2: Interface PDV (Sprint 3-4)**

✅ **Semana 3-4:**
- Layout PDV tablet landscape (2 colunas)
- Busca de produtos com autocomplete
- Carrinho de compras (MatList)
- Teclado numérico touch (MatDialog)
- Feedback visual (MatSnackBar)

✅ **Entregável:** PDV funcional para venda simples (sem NFCe)

**Fase 3: Interface Ordem B2B (Sprint 5-6)**

✅ **Semana 5-6:**
- Layout desktop 3-column
- Sidebar contextual do cliente (MatSideNav)
- Modal de consulta de estoque (MatDialog)
- Grid editável de produtos (MatTable)
- Reserva de estoque

✅ **Entregável:** Ordem B2B funcional com reserva

**Fase 4: Interface Mobile Recebimento (Sprint 7-8)**

✅ **Semana 7-8:**
- Layout mobile single-column
- Integração scanner (ZXing)
- Listagem OCs pendentes (MatList)
- Entrada de quantidade (teclado numérico)
- Atualização de estoque

✅ **Entregável:** Recebimento mobile funcional

**Fase 5: Polimento & Acessibilidade (Sprint 9-10)**

✅ **Semana 9-10:**
- ARIA labels em todos os componentes
- Keyboard navigation completa
- Touch targets ajustados (48px)
- Color contrast validation
- Screen reader testing
- Performance optimization

✅ **Entregável:** Sistema WCAG AA compliant

---

### 9.3 Component Reusability Map

**Componentes Compartilhados (criar primeiro):**

```
src/app/shared/components/
├── buttons/
│   ├── primary-button/
│   ├── secondary-button/
│   └── destructive-button/
├── forms/
│   ├── product-autocomplete/
│   ├── customer-autocomplete/
│   └── numeric-keypad-dialog/
├── feedback/
│   ├── loading-spinner/
│   ├── success-snackbar/
│   ├── error-snackbar/
│   └── warning-alert/
├── cards/
│   ├── metric-card/
│   └── entity-card/
└── tables/
    ├── data-table/
    └── mobile-card-list/
```

**Uso Cruzado:**

| Componente | PDV | Ordem B2B | Recebimento | Dashboard |
|------------|-----|-----------|-------------|-----------|
| ProductAutocomplete | ✅ | ✅ | ❌ | ❌ |
| NumericKeypad | ✅ | ❌ | ✅ | ❌ |
| SuccessSnackbar | ✅ | ✅ | ✅ | ✅ |
| MetricCard | ❌ | ❌ | ❌ | ✅ |
| DataTable | ❌ | ✅ | ❌ | ✅ |
| MobileCardList | ❌ | ❌ | ✅ | ❌ |

---

### 9.4 Design Handoff Checklist

**Para Desenvolvedores:**

- [x] **Design System definido:** Angular Material + tema roxo customizado
- [x] **Paleta de cores completa:** Primary, Accent, Semantic, Neutrals
- [x] **Typography scale:** Roboto com type scale completa
- [x] **Icon system:** Material Icons (não emojis)
- [x] **Spacing system:** 8px base unit
- [x] **Breakpoints:** xs/sm/md/lg/xl definidos
- [x] **Component patterns:** Buttons, Forms, Modals, Navigation, etc.
- [x] **Accessibility guidelines:** WCAG AA compliance rules
- [x] **User journeys mapeadas:** 4 fluxos críticos com componentes

**Artefatos Disponíveis:**

- ✅ UX Design Specification (este documento): `docs/ux-design-specification.md`
- ✅ Color Theme Visualizer: `docs/ux-color-themes-purple.html`
- ✅ Dashboard Mockup: `docs/dashboard-mockup-purple.html`
- ✅ PRD com requisitos: `docs/prd/prd.md`
- ✅ Product Brief: `docs/brief/brief.md`

**Próximos Passos para Implementação:**

1. **Setup inicial:**
   - `ng add @angular/material` (selecionar Deep Purple/Amber preset)
   - Criar `theme.scss` customizado
   - Setup breakpoints service para detecção de dispositivo

2. **Criar componentes compartilhados:**
   - Começar com buttons e forms (mais usados)
   - Implementar feedback components (snackbar, alerts)
   - Adicionar ARIA labels desde o início

3. **Implementar por interface:**
   - PDV primeiro (mais crítico, mais simples)
   - Ordem B2B segundo (mais complexo, desktop-first)
   - Recebimento Mobile terceiro (depende de ZXing)

4. **Testing & Validation:**
   - Manual keyboard navigation testing
   - Screen reader testing (NVDA/JAWS)
   - Color contrast validation (WebAIM tool)
   - Touch target validation (mobile real devices)
   - Lighthouse audit (performance + accessibility)

---

### 9.5 Completion Summary

**Documento Completo - UX Design Specification v1.0**

Este documento define **todos os aspectos visuais, interativos e de acessibilidade** do Estoque Central ERP. A especificação foi criada através de **colaboração visual iterativa** com o usuário poly, incorporando feedback em cada decisão.

**Seções Completadas:**

1. ✅ **Executive Summary** - Visão do projeto, usuários, experiência core, resposta emocional
2. ✅ **Design System Foundation** - Escolha de Angular Material com rationale completo
3. ✅ **Core User Experience** - Três interfaces especializadas, análise competitiva
4. ✅ **Visual Foundation** - Color system (Tema 4 Roxo Profundo), typography, icons, spacing, shadows
5. ✅ **User Journey Flows** - 4 jornadas críticas mapeadas com componentes Angular Material
6. ✅ **UX Pattern Decisions** - 10 padrões de consistência (buttons, feedback, forms, modals, etc.)
7. ✅ **Responsive Design & Accessibility** - Breakpoints, layouts adaptativos, WCAG AA compliance
8. ✅ **Implementation Guidance** - Theme setup, priorização, component reusability, handoff checklist

**Decisões-Chave Documentadas:**

- ✅ **Design System:** Angular Material (Material Design 3)
- ✅ **Color Theme:** Deep Purple Luxury (#6A1B9A primary, #F9A825 accent)
- ✅ **Icon Strategy:** Material Icons (NO emojis per user requirement)
- ✅ **Responsive Strategy:** Mobile-first com 3 layouts especializados
- ✅ **Accessibility:** WCAG 2.1 Level AA compliance mandatório
- ✅ **User Journeys:** 4 fluxos críticos com ERP-standard behaviors

**Artefatos Interativos Criados:**

- `ux-color-themes.html` - 4 temas azuis explorados
- `ux-color-themes-purple.html` - 4 temas roxos (Tema 4 escolhido)
- `dashboard-mockup-purple.html` - Comparação visual Tema 2 vs Tema 4

**Pronto para Implementação:**

Este documento fornece **todas as informações necessárias** para desenvolvedores começarem a implementação:
- Setup técnico (Angular Material theme config)
- Componentes mapeados (MatButton, MatTable, MatDialog, etc.)
- Código de exemplo (HTML, SCSS, TypeScript)
- Regras de acessibilidade (ARIA, keyboard nav, color contrast)
- Priorização (roadmap de 10 sprints)

**Status:** ✅ **COMPLETO E PRONTO PARA HANDOFF**

---

---

## Appendix

### Related Documents

- Product Requirements: `docs/prd/prd.md`
- Product Brief: `docs/brief/brief.md`

### Core Interactive Deliverables

This UX Design Specification was created through visual collaboration:

- **Color Theme Visualizer**: C:\Users\rspol\dev\estoque-central\docs\ux-color-themes.html
  - Interactive HTML showing all color theme options explored
  - Live UI component examples in each theme
  - Side-by-side comparison and semantic color usage

- **Design Direction Mockups**: C:\Users\rspol\dev\estoque-central\docs\ux-design-directions.html
  - Interactive HTML with 6-8 complete design approaches
  - Full-screen mockups of key screens
  - Design philosophy and rationale for each direction

### Version History

| Date       | Version | Changes                         | Author |
| ---------- | ------- | ------------------------------- | ------ |
| 2025-12-13 | 1.0     | Initial UX Design Specification | poly   |

---

_This UX Design Specification was created through collaborative design facilitation, not template generation. All decisions were made with user input and are documented with rationale._
