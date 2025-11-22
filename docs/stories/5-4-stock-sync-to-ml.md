# Story 5.4: Stock Synchronization to Mercado Livre

**Epic**: 5 - Marketplace Integration - Mercado Livre
**Story ID**: 5.4
**Status**: drafted
**Created**: 2025-11-21

---

## User Story

Como **sistema de gestão de estoque**,
Eu quero **sincronizar estoque com Mercado Livre automaticamente após vendas PDV/OV em latência < 5min**,
Para que **eu evite overselling entre canais (FR15, NFR4)**.

---

## Context & Business Value

Sincroniza estoque do sistema → ML após vendas em outros canais. Worker processa fila com latência < 5min (95% dos casos, NFR4). Aplica margem de segurança configurável (Story 5.7).

---

## Acceptance Criteria

### AC1: Fila Redis de Sincronização
- [ ] Após venda PDV/OV (Story 4.3): enfileira product_id para sync
- [ ] Deduplica (se produto já na fila, não adiciona novamente)

### AC2: Worker de Sincronização
- [ ] @Scheduled roda a cada 1 minuto
- [ ] Consome fila, agrupa por tenant
- [ ] Para cada produto:
  1. Calcula estoque disponível (quantity_available - quantity_reserved)
  2. Aplica margem de segurança (Story 5.7, default: 100%)
  3. PUT /items/{listing_id} atualiza available_quantity no ML
- [ ] Latência < 5min em 95% dos casos (NFR4)
- [ ] Taxa de erro < 1% (NFR5)

### AC3: Sincronização Manual (UI)
- [ ] Botão "Sincronizar Agora" força sync imediato de produto específico
- [ ] Endpoint POST /api/integrations/mercadolivre/sync-stock/{product_id}

### AC4: Logs de Sincronização
- [ ] Tabela `marketplace_sync_logs`: id, tenant_id, product_id, marketplace, sync_type (STOCK, PRICE), old_value, new_value, status (SUCCESS, ERROR), error_message, timestamp
- [ ] Auditoria completa de sincronizações

### AC5: Tratamento de Erros
- [ ] Erro 4xx/5xx: retry até 3 vezes com backoff exponencial
- [ ] Após 3 falhas: marca connection status=ERROR, notifica usuário

### AC6: Frontend - Histórico de Sincronizações
- [ ] Component `SyncHistoryComponent`
- [ ] Lista sync_logs com filtros (produto, status, período)
- [ ] Badge: verde (SUCCESS), vermelho (ERROR)

---

## Tasks
1. Enfileirar após vendas (Story 4.3 integration)
2. StockSyncWorker scheduled job
3. Deduplicação na fila
4. marketplace_sync_logs migration + entity
5. Retry logic com backoff
6. Frontend sync history
7. Testes (incluindo latência < 5min)

---

**Story criada por**: PM Agent
**Data**: 2025-11-21
