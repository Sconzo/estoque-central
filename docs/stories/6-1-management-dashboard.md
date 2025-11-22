# Story 6.1: Management Dashboard

**Epic**: 6 - Reporting & Analytics
**Story ID**: 6.1
**Status**: drafted
**Created**: 2025-11-21

---

## User Story

Como **gerente**,
Eu quero **dashboard consolidando vendas do dia por canal, estoque crítico e pedidos pendentes**,
Para que **eu tenha visão rápida da operação ao acessar o sistema (FR16)**.

---

## Context & Business Value

Dashboard principal exibido após login com KPIs em tempo real: vendas hoje (PDV, OV, ML), estoque total em valor, produtos em ruptura e pedidos pendentes. Atualização automática a cada 5 minutos.

---

## Acceptance Criteria

### AC1: Endpoint GET /api/dashboard/summary
- [ ] Retorna JSON:
  ```json
  {
    "sales_today": {
      "pdv": {"count": 45, "total_amount": 12500.00},
      "sales_orders": {"count": 8, "total_amount": 5400.00},
      "mercadolivre": {"count": 23, "total_amount": 8900.00},
      "total": {"count": 76, "total_amount": 26800.00}
    },
    "stock_value": 150000.00,
    "products_below_minimum": 12,
    "pending_orders": {
      "sales_orders_confirmed": 5,
      "mercadolivre_pending": 3
    }
  }
  ```
- [ ] Performance: < 1s (usa cache Redis 5min)

### AC2: Cards de Vendas do Dia
- [ ] Card "Vendas PDV Hoje": contador + valor total (ícone ponto de venda)
- [ ] Card "Ordens de Venda Hoje": contador + valor (ícone B2B)
- [ ] Card "Mercado Livre Hoje": contador + valor (ícone ML)
- [ ] Card "Total Vendas Hoje": destacado, soma de todos

### AC3: Card Estoque Total em Valor
- [ ] Fórmula: SUM(quantity_available * cost) de todos os produtos
- [ ] Exibe: R$ 150.000,00
- [ ] Tooltip: "Calculado com base no custo médio ponderado"

### AC4: Card Produtos em Ruptura
- [ ] Contador de produtos com estoque < mínimo configurado (FR18)
- [ ] Badge vermelho se > 0
- [ ] Link "Ver Detalhes" abre lista de produtos em ruptura

### AC5: Card Pedidos Pendentes
- [ ] SalesOrders CONFIRMED não faturadas: contador
- [ ] Mercado Livre importados PENDING: contador
- [ ] Link "Gerenciar" abre lista correspondente

### AC6: Gráfico de Vendas da Semana
- [ ] Line chart: últimos 7 dias
- [ ] Séries: PDV, OV, ML (cores distintas)
- [ ] Tooltip ao hover: data, canal, valor

### AC7: Atualização Automática
- [ ] Polling a cada 5 minutos (ou WebSocket futura)
- [ ] Indicador visual "Atualizado há X minutos"

### AC8: Frontend - DashboardComponent
- [ ] Grid responsivo 2x3 cards (desktop) ou stack (mobile)
- [ ] Chart.js ou ApexCharts para gráficos
- [ ] Skeleton loading ao carregar

---

## Tasks
1. DashboardService backend (queries otimizadas)
2. Cache Redis (TTL 5min)
3. DashboardController endpoint
4. Frontend DashboardComponent
5. Chart integration
6. Auto-refresh polling
7. Testes (performance < 1s)

---

**Story criada por**: PM Agent
**Data**: 2025-11-21
