# Story 6.5: Current Stock Multi-Warehouse Report

**Epic**: 6 - Reporting & Analytics
**Story ID**: 6.5
**Status**: drafted
**Created**: 2025-11-21

---

## User Story

Como **gerente de estoque**,
Eu quero **relatório de estoque atual com view consolidada e detalhada por local**,
Para que **eu saiba exatamente quanto tenho em cada depósito (FR17)**.

---

## Acceptance Criteria

### AC1: Endpoint GET /api/reports/current-stock
- [ ] Filtros: produto, categoria, local, below_minimum (boolean)
- [ ] Modo: consolidated (soma todos os locais) ou detailed (breakdown por local)
- [ ] Retorna:
  - Consolidated: product_id, sku, name, total_available, total_reserved, total_for_sale, value (qty * cost)
  - Detailed: + stock_location_name, quantity_available, quantity_reserved

### AC2: View Consolidada
- [ ] Tabela: Produto, Disponível (soma todos locais), Reservado, Disponível p/ Venda, Valor em Estoque
- [ ] Linha de totais: soma de tudo, valor total em estoque
- [ ] Ordenação: nome, SKU, quantidade, valor

### AC3: View Detalhada (Breakdown por Local)
- [ ] Tabela: Produto, Local, Disponível, Reservado, Disponível p/ Venda, Custo Médio
- [ ] Agrupamento por produto (expandir/colapsar)
- [ ] Subtotais por produto

### AC4: Filtro "Abaixo do Mínimo"
- [ ] Checkbox "Apenas produtos abaixo do estoque mínimo"
- [ ] Destaca em vermelho produtos em ruptura
- [ ] Badge com quantidade de produtos em ruptura

### AC5: Frontend - CurrentStockReportComponent
- [ ] Toggle: View Consolidada / Detalhada
- [ ] Filtros: produto, categoria, local, abaixo do mínimo
- [ ] Tabela com ordenação em colunas
- [ ] Linha de totais destacada
- [ ] Exportação CSV

---

## Tasks
1. CurrentStockReportService (queries consolidada e detalhada)
2. Below minimum filter logic
3. Frontend component com 2 views (toggle)
4. Sorting e export CSV
5. Testes

---

**Story criada por**: PM Agent
**Data**: 2025-11-21
