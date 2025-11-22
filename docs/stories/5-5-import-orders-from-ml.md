# Story 5.5: Import and Process Orders from Mercado Livre

**Epic**: 5 - Marketplace Integration - Mercado Livre
**Story ID**: 5.5
**Status**: drafted
**Created**: 2025-11-21

---

## User Story

Como **sistema de vendas**,
Eu quero **importar pedidos do Mercado Livre automaticamente e processar baixa/reserva de estoque**,
Para que **vendas ML reflitam no estoque em tempo real (FR14)**.

---

## Context & Business Value

Importa pedidos ML via webhook ou polling, identifica variantes vendidas, reserva/baixa estoque automaticamente e atualiza status de envio/entrega no ML.

---

## Acceptance Criteria

### AC1: Tabela marketplace_orders Criada
- [ ] `marketplace_orders`: id, tenant_id, marketplace, order_id_marketplace, sale_id (FK para sales, nullable), sales_order_id (FK para sales_orders, nullable), customer_name, customer_email, total_amount, status (PENDING, PAID, SHIPPED, DELIVERED, CANCELLED), imported_at

### AC2: Webhook ML (Notificações)
- [ ] Endpoint POST /api/webhooks/mercadolivre/orders
- [ ] ML envia notificação ao criar/atualizar pedido
- [ ] Payload: { topic: "orders_v2", resource: "/orders/{id}" }
- [ ] Enfileira order_id para processamento assíncrono

### AC3: Worker de Importação de Pedidos
- [ ] @Scheduled ou event-driven (consome fila webhook)
- [ ] GET /orders/{id} busca detalhes completos do pedido
- [ ] Identifica produtos/variantes vendidos (via listing_id)
- [ ] Cria venda interna (tabela sales ou sales_orders):
  - Se pagamento confirmado (status=paid): cria sale + baixa estoque (como PDV)
  - Se aguardando pagamento: cria sales_order + reserva estoque
- [ ] Salva em marketplace_orders linkando com sale/sales_order
- [ ] Sincroniza estoque com ML (Story 5.4)

### AC4: Identificação de Variantes Vendidas
- [ ] ML retorna order_items[].item.id (listing_id) + variation_id
- [ ] Sistema busca marketplace_listing por listing_id
- [ ] Se variation_id != null: busca variant correspondente

### AC5: Atualização de Status de Envio no ML
- [ ] Quando venda é faturada/enviada (futura integração transportadora):
  - PUT /shipments/{shipment_id} atualiza status=ready_to_ship ou shipped
  - ML notifica comprador automaticamente

### AC6: Frontend - Pedidos do Mercado Livre
- [ ] Component `MercadoLivreOrdersComponent`
- [ ] Lista pedidos importados com status
- [ ] Filtros: período, status
- [ ] Link para pedido original no ML
- [ ] Indicação de venda/ordem interna linkada

---

## Tasks
1. Migration marketplace_orders
2. Webhook endpoint + queue
3. OrderImportWorker scheduled job
4. Variant identification logic
5. Integration com SaleService (Story 4.3)
6. Shipment status update
7. Frontend orders list
8. Testes

---

**Story criada por**: PM Agent
**Data**: 2025-11-21
