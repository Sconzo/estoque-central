# Story 6.6: ABC Curve Analysis (Pareto)

**Epic**: 6 - Reporting & Analytics
**Story ID**: 6.6
**Status**: drafted
**Created**: 2025-11-21

---

## User Story

Como **gerente comercial**,
Eu quero **curva ABC de produtos baseada em análise de Pareto (80/20)**,
Para que **eu foque esforços nos produtos que geram mais receita (FR17)**.

---

## Context & Business Value

Análise ABC classifica produtos em 3 categorias baseado em receita acumulada:
- **A**: 80% da receita (top performers)
- **B**: 15% da receita (intermediários)
- **C**: 5% da receita (cauda longa)

Permite priorizar gestão de estoque, marketing e compras nos produtos A.

---

## Acceptance Criteria

### AC1: Endpoint GET /api/reports/abc-curve
- [ ] Filtros: período (default: últimos 12 meses)
- [ ] Retorna:
  ```json
  [
    {
      "product_id": "...",
      "sku": "PROD-001",
      "name": "Notebook Dell",
      "revenue": 225000.00,
      "revenue_percentage": 15.5,
      "cumulative_percentage": 15.5,
      "classification": "A"
    },
    ...
  ]
  ```
- [ ] Ordenado por revenue desc
- [ ] Classification: A (0-80%), B (80-95%), C (95-100%)

### AC2: Cálculo de Curva ABC
- [ ] Query: SUM(sale_items.total_price) GROUP BY product
- [ ] Ordenar por revenue desc
- [ ] Calcular cumulative_percentage
- [ ] Classificar: A se cumulative <= 80%, B se <= 95%, C resto

### AC3: Frontend - ABCCurveReportComponent
- [ ] Filtro: período
- [ ] Cards resumo: "X produtos A (80% receita)", "Y produtos B (15%)", "Z produtos C (5%)"
- [ ] Tabela: Rank, Produto, Receita, % do Total, % Acumulado, Classe (badge colorido)
- [ ] Gráfico de Pareto: line chart com barra (receita) + linha (% acumulada)
- [ ] Filtro rápido por classe (A/B/C)
- [ ] Exportação CSV

### AC4: Gráfico de Pareto
- [ ] Eixo X: produtos (ordenados por receita desc, TOP 50)
- [ ] Eixo Y esquerdo: receita (barras azuis)
- [ ] Eixo Y direito: % acumulado (linha vermelha)
- [ ] Linhas horizontais em 80% e 95% (threshold A/B/C)

---

## Tasks
1. ABCCurveService com lógica de classificação
2. Query de agregação de receita
3. Cálculo de cumulative percentage
4. Frontend component com gráfico Pareto (Chart.js combo)
5. Filtros e badges
6. Testes (validar classificação A/B/C)

---

**Story criada por**: PM Agent
**Data**: 2025-11-21
