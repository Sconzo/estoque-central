# Story 6.2: Stock Movements Report

**Epic**: 6 - Reporting & Analytics
**Story ID**: 6.2
**Status**: drafted
**Created**: 2025-11-21

---

## User Story

Como **auditor/gerente**,
Eu quero **relatório de movimentações de estoque com filtros avançados e exportação CSV**,
Para que **eu possa rastrear alterações e gerar relatórios fiscais (FR17)**.

---

## Acceptance Criteria

### AC1: Endpoint GET /api/reports/stock-movements
- [ ] Filtros: período (date_from, date_to), produto, local, tipo, usuário
- [ ] Paginação: 50 por página (configurável)
- [ ] Ordenação: timestamp desc (default)
- [ ] Retorna: tipo, produto, local, quantidade, saldo antes/depois, usuário, timestamp, documento relacionado

### AC2: Exportação CSV
- [ ] Endpoint GET /api/reports/stock-movements/export?format=csv
- [ ] Mesmos filtros que listagem
- [ ] Gera CSV com todas as colunas
- [ ] Download com filename: movimentacoes_estoque_YYYYMMDD.csv

### AC3: Frontend - StockMovementsReportComponent
- [ ] Filtros: período (datepicker range), produto (autocomplete), local (dropdown), tipo (multi-select), usuário (autocomplete)
- [ ] Tabela: Tipo, Produto, Local, Quantidade, Saldo Antes, Saldo Depois, Usuário, Data/Hora, Documento
- [ ] Paginação
- [ ] Botão "Exportar CSV"

---

## Tasks
1. ReportService.getStockMovements(filters)
2. CSV export utility
3. ReportController endpoints
4. Frontend component com filtros
5. Testes

---

**Story criada por**: PM Agent
**Data**: 2025-11-21
