# Story 5.6: Process Mercado Livre Cancellations

**Epic**: 5 - Marketplace Integration - Mercado Livre
**Story ID**: 5.6
**Status**: drafted
**Created**: 2025-11-21

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
- [ ] Endpoint POST /api/webhooks/mercadolivre/orders (mesmo webhook Story 5.5)
- [ ] ML envia notificação com status=cancelled
- [ ] Enfileira para processamento

### AC2: Worker de Processamento de Cancelamento
- [ ] Busca marketplace_order cancelada
- [ ] Identifica venda/ordem interna linkada:
  - Se sale (estoque baixado): cria ajuste tipo INCREASE (estorno)
  - Se sales_order (estoque reservado): libera reserva
- [ ] Atualiza marketplace_order.status = CANCELLED
- [ ] Cria movimentação tipo REVERSAL em stock_movements
- [ ] Sincroniza estoque com ML (Story 5.4)

### AC3: Estorno de Venda (Sale)
- [ ] Se pedido ML cancelado após venda registrada (sale):
  - Cria stock_adjustment tipo INCREASE para cada item
  - Reason: "Estorno venda ML cancelada - Order [order_id]"
  - Atualiza sale.status = CANCELLED (novo status)

### AC4: Liberação de Reserva (Sales Order)
- [ ] Se pedido ML cancelado antes de faturar (sales_order):
  - Libera reserva (quantity_reserved -= quantity)
  - Atualiza sales_order.status = CANCELLED

### AC5: Notificação de Cancelamento
- [ ] Envia notificação para vendedor: "Pedido ML [order_id] cancelado. Estoque atualizado."
- [ ] Email ou push notification

### AC6: Frontend - Histórico de Cancelamentos
- [ ] Aba "Cancelamentos" em MercadoLivreOrdersComponent
- [ ] Lista pedidos cancelados
- [ ] Exibe: order_id, data cancelamento, motivo, estorno/liberação realizada

---

## Tasks
1. Webhook processing for cancellations
2. CancellationProcessorWorker
3. Integration com StockAdjustmentService (Story 3.5)
4. Integration com StockReservationService (Story 4.6)
5. Notification service
6. Frontend cancellations tab
7. Testes

---

**Story criada por**: PM Agent
**Data**: 2025-11-21
