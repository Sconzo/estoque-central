# Story 6.4: Top Selling Products Report

**Epic**: 6 - Reporting & Analytics
**Story ID**: 6.4
**Status**: drafted
**Created**: 2025-11-21

---

## User Story

Como **gerente de compras**,
Eu quero **relatório de produtos mais vendidos por unidade e valor**,
Para que **eu priorize reposição de estoque dos campeões de venda (FR17)**.

---

## Acceptance Criteria

### AC1: Endpoint GET /api/reports/top-products
- [ ] Filtros: período, canal (ALL, PDV, OV, ML), metric (QUANTITY, REVENUE)
- [ ] Limit: TOP N (default: 20, max: 100)
- [ ] Retorna:
  ```json
  [
    {
      "product_id": "...",
      "sku": "PROD-001",
      "name": "Notebook Dell",
      "quantity_sold": 150,
      "revenue": 225000.00,
      "rank": 1
    },
    ...
  ]
  ```

### AC2: Ranking por Quantidade
- [ ] Ordena por quantity_sold desc
- [ ] Exibe: rank, produto, quantidade vendida, receita gerada

### AC3: Ranking por Valor (Receita)
- [ ] Ordena por revenue desc
- [ ] Exibe: rank, produto, receita, quantidade vendida

### AC4: Frontend - TopProductsReportComponent
- [ ] Filtros: período, canal, métrica (toggle: Unidades / Receita), TOP N (slider 10-100)
- [ ] Tabela ranking: #, Produto (SKU - Nome), Quantidade, Receita
- [ ] Badge TOP 3 com medalhas (ouro/prata/bronze)
- [ ] Gráfico de barras horizontais (TOP 10)
- [ ] Exportação CSV

---

## Tasks
1. TopProductsReportService
2. Aggregation query (SUM quantity, SUM revenue GROUP BY product)
3. Frontend component com ranking
4. Chart horizontal bars
5. Testes

---

**Story criada por**: PM Agent
**Data**: 2025-11-21
