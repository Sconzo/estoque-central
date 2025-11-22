# Story 6.3: Sales Report by Period and Channel

**Epic**: 6 - Reporting & Analytics
**Story ID**: 6.3
**Status**: drafted
**Created**: 2025-11-21

---

## User Story

Como **gerente comercial**,
Eu quero **relatório de vendas por período com breakdown por canal e gráficos**,
Para que **eu analise performance de cada canal (FR17)**.

---

## Acceptance Criteria

### AC1: Endpoint GET /api/reports/sales-by-channel
- [ ] Filtros: período (date_from, date_to), canal (PDV, OV, ML, ALL)
- [ ] Retorna:
  ```json
  {
    "summary": {
      "pdv": {"count": 450, "total_amount": 125000.00},
      "sales_orders": {"count": 80, "total_amount": 54000.00},
      "mercadolivre": {"count": 230, "total_amount": 89000.00}
    },
    "daily_breakdown": [
      {"date": "2025-11-01", "pdv": 4500, "sales_orders": 1800, "mercadolivre": 3200},
      ...
    ]
  }
  ```

### AC2: Gráfico de Barras Empilhadas
- [ ] Eixo X: dias do período
- [ ] Eixo Y: valor vendido
- [ ] Séries: PDV (azul), OV (verde), ML (amarelo)
- [ ] Tooltip: data, canal, valor

### AC3: Tabela Detalhada
- [ ] Colunas: Data, PDV (R$), OV (R$), ML (R$), Total (R$)
- [ ] Linha de totais no rodapé
- [ ] Exportação CSV

### AC4: Frontend - SalesByChannelReportComponent
- [ ] Filtro período (presets: Hoje, Últimos 7 dias, Mês Atual, Personalizado)
- [ ] Cards de resumo (1 por canal + total)
- [ ] Gráfico stacked bar chart
- [ ] Tabela detalhada
- [ ] Botão "Exportar CSV"

---

## Tasks
1. SalesReportService.getSalesByChannel(filters)
2. Daily aggregation query
3. CSV export
4. Frontend component com gráfico (Chart.js/ApexCharts)
5. Testes

---

**Story criada por**: PM Agent
**Data**: 2025-11-21
