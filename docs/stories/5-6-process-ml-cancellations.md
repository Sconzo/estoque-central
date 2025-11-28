# Story 5.6: Process Mercado Livre Cancellations

**Epic**: 5 - Marketplace Integration - Mercado Livre
**Story ID**: 5.6
**Status**: completed
**Created**: 2025-11-21
**Completed**: 2025-11-27 (finalized)

---

## User Story

Como **sistema de gestão de estoque**,
Eu quero **processar cancelamentos importados do Mercado Livre atualizando estoque automaticamente**,
Para que **cancelamentos ML estornem baixa ou liberem reserva (FR23)**.

---

## Context & Business Value

Processa cancelamentos ML (comprador/vendedor/ML cancela pedido) estornando estoque automaticamente. Diferencia baixa já realizada (estorno via entrada manual) vs reserva (liberação).

---

## Acceptance Criteria

### AC1: Webhook ML - Cancelamento
- [x] Endpoint POST /api/webhooks/mercadolivre/orders (mesmo webhook Story 5.5)
- [x] ML envia notificação com status=cancelled
- [x] Webhook triggers cancellation processing automatically

### AC2: Worker de Processamento de Cancelamento
- [x] MercadoLivreCancellationService.processCancellation() implementado
- [x] Busca marketplace_order cancelada
- [x] Identifica venda/ordem interna linkada:
  - Se sale (estoque baixado): chama processSaleCancellation() para estorno
  - Se sales_order (estoque reservado): chama processSalesOrderCancellation() para liberação
- [x] Atualiza marketplace_order.status = CANCELLED
- [x] Integration com MercadoLivreOrderImportService.updateExistingOrder()
- [x] Cria movimentação tipo ADJUSTMENT em stock_movements (via StockAdjustmentService)
- [x] Sincroniza estoque com ML (via MarketplaceStockSyncService após cancelamento)

### AC3: Estorno de Venda (Sale)
- [x] Se pedido ML cancelado após venda registrada (sale):
  - Service method processSaleCancellation() implementado
  - [x] Cria stock_adjustment tipo INCREASE para cada item (via StockAdjustmentService)
  - [x] Reason: "Estorno venda ML cancelada - Order [order_id] - Sale [sale_number]"
  - [x] Atualiza sale.status = CANCELLED (novo campo status adicionado)
- [x] Logging e documentação de ações realizadas

### AC4: Liberação de Reserva (Sales Order)
- [x] Se pedido ML cancelado antes de faturar (sales_order):
  - Service method processSalesOrderCancellation() implementado
  - [x] Libera reserva (via StockReservationService.release())
  - [x] Atualiza sales_order.status = CANCELLED
- [x] Logging e documentação de ações realizadas

### AC5: Notificação de Cancelamento
- [x] NotificationService.notifyCancellation() implementado
- [x] Logs detalhados para vendedor: "Pedido ML [order_id] cancelado. Estoque atualizado."
- [x] Estrutura pronta para integração com email/push notification (TODO futuro)

### AC6: Frontend - Histórico de Cancelamentos
- [x] Filtro de status em MercadoLivreOrdersComponent
- [x] Toggle buttons: "Todos", "Ativos", "Cancelados"
- [x] Lista pedidos cancelados separadamente
- [x] Exibe: order_id, customer, total, status (com chip vermelho para CANCELLED), payment, internal record, imported date
- [x] Filtro automático aplicado na listagem

---

## Tasks
1. ✅ Webhook processing for cancellations
2. ✅ MercadoLivreCancellationService implementation completa
3. ✅ Integration com StockAdjustmentService (Story 3.5)
4. ✅ Integration com StockReservationService (Story 4.6)
5. ✅ Notification service implementation
6. ✅ Integration com MarketplaceStockSyncService
7. ✅ Sale.status field implementation (migration + entity)
8. ✅ Frontend cancellations filter
9. ✅ Testes unitários (5 test cases)

---

## Definition of Done

- [x] Código implementado e revisado
- [x] Backend compila sem erros
- [x] Frontend compila sem erros
- [x] Testes unitários escritos e passando (5/5 tests)
- [x] Integração completa com serviços de estoque
- [x] Notificações implementadas (logs + estrutura para email)
- [x] Sincronização automática com ML após cancelamento
- [x] Documentação atualizada

---

## Implementation Summary

### Backend Files Created/Modified (9 files):
1. `SaleStatus.java` (NEW) - Enum para status de venda (ACTIVE, CANCELLED)
2. `V064__add_status_to_sales.sql` (NEW) - Migration para adicionar campo status
3. `Sale.java` (MODIFIED) - Adicionado campo status, métodos isActive(), isCancelled(), cancel()
4. `MercadoLivreCancellationService.java` (MODIFIED) - Service completo:
   - processCancellation() - Entry point automático via webhook
   - processSaleCancellation() - Estorno completo de venda (StockAdjustmentService + sale.cancel())
   - processSalesOrderCancellation() - Liberação de reserva (StockReservationService.release() + salesOrder.cancel())
   - Integration com MarketplaceStockSyncService para sync automático
   - Integration com NotificationService para logs de cancelamento
5. `NotificationService.java` (MODIFIED) - Método notifyCancellation() adicionado
6. `MercadoLivreOrderImportService.java` (MODIFIED) - Updated updateExistingOrder()
7. `MercadoLivreCancellationServiceTest.java` (NEW) - 5 unit tests cobrindo todos os cenários
   - Adicionada dependency MercadoLivreCancellationService
   - Trigger automático de processamento quando status muda para CANCELLED

### Frontend Files Modified (1 file):
1. `mercadolivre-orders.component.ts` - Adicionado filtro de cancelamentos
   - Propriedades: statusFilter ('all' | 'active' | 'cancelled'), filteredOrders
   - Método applyFilter() - Filtra pedidos por status
   - UI: MatButtonToggleModule com 3 opções de filtro
   - CSS: filter-container com estilo

### Key Implementation Details:
- **Automatic Triggering**: Cancellation processing é acionado automaticamente quando webhook atualiza order status para CANCELLED
- **Dual Path Logic**: Diferencia entre sale (estoque baixado) e sales_order (estoque reservado)
- **Complete Integration**:
  - StockAdjustmentService cria ADJUSTMENT INCREASE para estorno de venda
  - StockReservationService libera reservas com RELEASE movement
  - MarketplaceStockSyncService enfileira sync automático para ML após estorno/liberação
  - NotificationService registra logs detalhados de cancelamento
- **Sale Status Management**: Novo campo `status` na entidade Sale (ACTIVE, CANCELLED) com migration V064
- **Order Status Update**: marketplace_order.status é atualizado para CANCELLED após processamento
- **Frontend Filter**: Toggle buttons permitem visualizar todos, ativos ou cancelados separadamente
- **Error Handling**: Continua processando outros itens mesmo se um falhar
- **Empty State Handling**: Mensagens diferentes para "nenhum pedido" vs "nenhum pedido com filtro selecionado"

### Build Results:
- Backend: ✅ BUILD SUCCESS (40.135s)
- Frontend: ✅ BUILD SUCCESS (incluído no backend build)
- Tests: ✅ 5/5 tests passing (MercadoLivreCancellationServiceTest)
- Warnings: Apenas budget warnings em alguns SCSS files (não críticos)

### Test Coverage:
1. ✅ processCancellation_withSale_shouldRevertStockAndCancelSale
2. ✅ processCancellation_withSalesOrder_shouldReleaseReservation
3. ✅ processCancellation_orderNotFound_shouldLogWarning
4. ✅ processCancellation_alreadyCancelled_shouldSkip
5. ✅ processCancellation_noLinkedRecord_shouldOnlyUpdateOrderStatus

### Future Enhancements:
1. **Email/Push Notifications**: Integrar NotificationService com SendGrid/AWS SES para notificar vendedor
2. **Testes de Integração**: Adicionar testes end-to-end do fluxo completo (webhook → estorno → sync)
3. **Testes Manuais**: Validar fluxo com ambiente real do ML (homologação)

---

## Change Log

| Data | Autor | Descrição |
|------|-------|-----------|
| 2025-11-21 | PM Agent | Story criada |
| 2025-11-26 | Dev Agent | Implementação inicial (backend + frontend com placeholders) |
| 2025-11-27 | Dev Agent (James) | Finalizadas todas pendências: Sale.status, integrações completas (StockAdjustmentService, StockReservationService, MarketplaceStockSyncService, NotificationService), testes unitários (5/5 passing), migration V064, builds validados |

---

**Story criada por**: PM Agent
**Data**: 2025-11-21
**Implementada por**: Dev Agent
**Data de implementação inicial**: 2025-11-26
**Finalizada por**: Dev Agent (James)
**Data de finalização**: 2025-11-27
