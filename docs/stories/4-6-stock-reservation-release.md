# Story 4.6: Stock Reservation and Automatic Release

**Epic**: 4 - Sales Channels - PDV & B2B
**Story ID**: 4.6
**Status**: approved
**Created**: 2025-11-21
**Updated**: 2025-11-21

---

## User Story

Como **sistema de gestão de estoque**,
Eu quero **reservar estoque ao confirmar Ordens de Venda e liberar automaticamente reservas após 7 dias se não faturadas**,
Para que **estoque reservado não fique travado indefinidamente (FR24)**.

---

## Context & Business Value

Implementa reserva de estoque ao confirmar Ordem de Venda (status DRAFT → CONFIRMED) e liberação automática de reservas de ordens não faturadas após prazo configurável (7 dias por tenant, FR24). Atualiza campo `quantity_reserved` em `stock` e cria movimentações auditáveis tipo RESERVE/RELEASE.

---

## Acceptance Criteria

### AC1: Atualização de quantity_reserved em Tabela stock
- [ ] Campo `quantity_reserved` já existe (Story 2.7)
- [ ] Fórmula: `quantity_for_sale = quantity_available - quantity_reserved`
- [ ] Reserva: `quantity_reserved += quantity`
- [ ] Liberação: `quantity_reserved -= quantity`

### AC2: Reserva de Estoque ao Confirmar Ordem
- [ ] Método `SalesOrderService.confirmOrder(orderId)`:
  1. Valida estoque: `quantity_for_sale >= quantity_ordered` para cada item
  2. Atualiza `stock.quantity_reserved += quantity` para cada item
  3. Atualiza `sales_order_items.quantity_reserved = quantity_ordered`
  4. Cria movimentações tipo RESERVE em `stock_movements`
  5. Atualiza `sales_order.status = CONFIRMED`
  6. Transação @Transactional, rollback se falhar
- [ ] Se estoque insuficiente: retorna HTTP 409 com detalhes (produto, disponível vs solicitado)

### AC3: Liberação Manual de Reservas (ao Cancelar Ordem)
- [ ] Método `SalesOrderService.cancelOrder(orderId)`:
  1. Atualiza `stock.quantity_reserved -= quantity` para cada item
  2. Cria movimentações tipo RELEASE em `stock_movements`
  3. Atualiza `sales_order.status = CANCELLED`
  4. Transação @Transactional

### AC4: Liberação Automática após 7 Dias (FR24)
- [ ] @Scheduled job roda diariamente às 02:00 AM
- [ ] Query: ordens CONFIRMED, created_at < (NOW() - N days)
- [ ] N configurável por tenant (default: 7 dias)
- [ ] Para cada ordem encontrada:
  1. Libera reservas (atualiza quantity_reserved)
  2. Atualiza status para EXPIRED
  3. Cria movimentações RELEASE
  4. Envia notificação para vendedor (email/push)

### AC5: Configuração de Prazo de Liberação por Tenant
- [ ] Tabela `tenant_settings`:
  - tenant_id, setting_key, setting_value
- [ ] Setting: `sales_order_auto_release_days` (default: 7)
- [ ] Endpoint `PUT /api/settings/sales-order-release-days` (admin only)

### AC6: Movimentações de Auditoria
- [ ] Tipo RESERVE criado ao confirmar ordem:
  - type = RESERVE
  - quantity = +quantity (positivo, indica reserva)
  - document_id = sales_order_id
  - reason = "Reserva OV [order_number]"
- [ ] Tipo RELEASE criado ao cancelar/expirar:
  - type = RELEASE
  - quantity = +quantity (positivo, indica liberação)
  - document_id = sales_order_id
  - reason = "Liberação OV [order_number] - [motivo]"

### AC7: Frontend - Indicador de Estoque Reservado
- [ ] Tela de consulta de estoque (Story 2.7) exibe coluna "Reservado"
- [ ] Tooltip ao passar mouse: "Reservado por Ordens de Venda pendentes"
- [ ] Link "Ver Ordens" abre modal com lista de ordens que reservam aquele produto/local

### AC8: Frontend - Alerta de Ordens Próximas à Expiração
- [ ] Dashboard exibe card "Ordens Próximas à Expiração"
- [ ] Lista ordens CONFIRMED com < 2 dias para expirar
- [ ] Botão "Faturar" ou "Prorrogar" (prorrogar adiciona +7 dias)

### AC9: Endpoint GET /api/sales-orders/expiring-soon
- [ ] Retorna ordens CONFIRMED com < 2 dias para expirar
- [ ] Filtro: days_until_expiration (default: 2)

### AC10: Endpoint PUT /api/sales-orders/{id}/extend
- [ ] Prorroga prazo de expiração por +N dias (default: +7)
- [ ] Atualiza campo `extended_until` (novo campo)
- [ ] Requer permissão MANAGER ou ADMIN

---

## Tasks & Subtasks

### Task 1: Adicionar Enums RESERVE e RELEASE ao StockMovementType
- [ ] Enum existente (Story 2.8), adicionar novos valores

### Task 2: Criar Tabela tenant_settings
- [ ] Migration V053__create_tenant_settings_table.sql
- [ ] Seed com sales_order_auto_release_days = 7

### Task 3: Implementar StockReservationService
- [ ] Método `reserve(productId, locationId, quantity, orderId)`
- [ ] Método `release(productId, locationId, quantity, orderId, reason)`
- [ ] Atualiza stock.quantity_reserved
- [ ] Cria movimentações RESERVE/RELEASE

### Task 4: Atualizar SalesOrderService
- [ ] Método `confirmOrder()` chama StockReservationService.reserve()
- [ ] Método `cancelOrder()` chama StockReservationService.release()
- [ ] Validação de estoque disponível antes de reservar

### Task 5: Implementar AutoReleaseScheduledJob
- [ ] @Scheduled(cron = "0 0 2 * * ?") // 02:00 AM diariamente
- [ ] Query ordens expiradas
- [ ] Libera reservas e atualiza status EXPIRED
- [ ] Notifica vendedor

### Task 6: Implementar TenantSettingsService
- [ ] Método `getAutoReleaseDays(tenantId)` retorna configuração
- [ ] Endpoint PUT /api/settings/sales-order-release-days

### Task 7: Frontend - Indicador de Estoque Reservado
- [ ] Atualizar StockTableComponent para exibir coluna "Reservado"
- [ ] Modal de detalhes com ordens que reservam

### Task 8: Frontend - Card "Ordens Próximas à Expiração"
- [ ] Component `ExpiringSalesOrdersCardComponent` no dashboard
- [ ] Lista com ações (Faturar, Prorrogar)

### Task 9: Testes

#### Testing

- [ ] Teste: confirmar ordem reserva estoque corretamente
- [ ] Teste: quantity_for_sale calculado corretamente após reserva
- [ ] Teste: cancelar ordem libera reservas
- [ ] Teste: job automático libera ordens expiradas
- [ ] Teste: prorrogação adiciona +7 dias
- [ ] Teste: movimentações RESERVE e RELEASE criadas

---

## Definition of Done (DoD)

- [ ] Campo quantity_reserved utilizado corretamente
- [ ] StockReservationService implementado
- [ ] SalesOrderService reserva/libera estoque
- [ ] Job automático de liberação funciona
- [ ] Configuração de prazo por tenant
- [ ] Frontend exibe estoque reservado
- [ ] Dashboard alerta ordens próximas à expiração
- [ ] Testes passando
- [ ] Code review aprovado

---

## Dependencies & Blockers

**Depende de:**
- Story 2.7 (Stock Control) - Campo quantity_reserved
- Story 2.8 (Stock Movements) - Movimentações RESERVE/RELEASE
- Story 4.5 (Ordem de Venda) - Ordens que reservam estoque

**Bloqueia:**
- Nenhuma story, mas é crítico para operação B2B (FR24)

---

## Technical Notes

**Reserva de Estoque:**
```java
@Service
public class StockReservationService {
    @Transactional
    public void reserve(UUID productId, UUID locationId, BigDecimal quantity, UUID orderId) {
        Stock stock = stockRepository.findByProductAndLocation(productId, locationId)
            .orElseThrow(() -> new NotFoundException("Estoque não encontrado"));

        BigDecimal forSale = stock.getQuantityAvailable().subtract(stock.getQuantityReserved());
        if (forSale.compareTo(quantity) < 0) {
            throw new InsufficientStockException(
                String.format("Estoque insuficiente. Disponível para venda: %s, Solicitado: %s",
                    forSale, quantity)
            );
        }

        stock.setQuantityReserved(stock.getQuantityReserved().add(quantity));
        stockRepository.save(stock);

        createStockMovement(RESERVE, productId, locationId, quantity, orderId,
            "Reserva OV " + getOrderNumber(orderId));
    }

    @Transactional
    public void release(UUID productId, UUID locationId, BigDecimal quantity, UUID orderId, String reason) {
        Stock stock = stockRepository.findByProductAndLocation(productId, locationId)
            .orElseThrow(() -> new NotFoundException("Estoque não encontrado"));

        stock.setQuantityReserved(stock.getQuantityReserved().subtract(quantity));
        stockRepository.save(stock);

        createStockMovement(RELEASE, productId, locationId, quantity, orderId, reason);
    }
}
```

**Job de Liberação Automática:**
```java
@Component
public class AutoReleaseScheduledJob {
    @Scheduled(cron = "0 0 2 * * ?") // 02:00 AM diariamente
    public void releaseExpiredOrders() {
        List<UUID> tenants = tenantRepository.findAllIds();

        for (UUID tenantId : tenants) {
            TenantContext.setCurrentTenant(tenantId);

            int autoReleaseDays = tenantSettingsService.getAutoReleaseDays(tenantId);
            LocalDate expirationDate = LocalDate.now().minusDays(autoReleaseDays);

            List<SalesOrder> expiredOrders = salesOrderRepository.findExpiredOrders(
                tenantId, expirationDate, SalesOrderStatus.CONFIRMED
            );

            for (SalesOrder order : expiredOrders) {
                for (SalesOrderItem item : order.getItems()) {
                    stockReservationService.release(
                        item.getProductId(),
                        order.getStockLocationId(),
                        item.getQuantityReserved(),
                        order.getId(),
                        "Liberação automática - Ordem expirada após " + autoReleaseDays + " dias"
                    );
                }

                order.setStatus(SalesOrderStatus.EXPIRED);
                salesOrderRepository.save(order);

                notificationService.notifyOrderExpired(order);
            }
        }
    }
}
```

---

## Change Log

| Data       | Autor                  | Alteração                                                         |
|------------|------------------------|-------------------------------------------------------------------|
| 2025-11-21 | Claude Code (PM)       | Story drafted                                                     |
| 2025-11-21 | Sarah (PO)             | Migration version corrigida de V052 para V053 (validação épico)  |
| 2025-11-21 | Sarah (PO)             | Adicionadas seções Change Log, Testing, Dev Agent Record, QA Results (template compliance) |

---

## Dev Agent Record

**Agent Model Used:**
Claude 3.5 Sonnet (claude-sonnet-4-5-20250929)

### Debug Log References

### Completion Notes List

### File List

---

## QA Results

**Validation Status**: ✅ Approved
**Validated By**: Sarah (Product Owner)
**Validation Date**: 2025-11-21

**Findings**:
- ✅ Epic alignment verified
- ✅ Acceptance Criteria complete and testable
- ✅ Task breakdown adequate
- ✅ Migration version corrected to V053
- ✅ Template compliance achieved

**Notes**:
- Excellent stock reservation mechanism using existing `quantity_reserved` field (Story 2.7)
- FR24 compliance (auto-release após 7 dias) properly implemented
- Tenant-configurable release period adds flexibility
- @Scheduled job for automatic release well-designed (02:00 AM daily)
- Dashboard alert for expiring orders prevents loss of reservations
- Ready for development

**Technical Highlights**:
- Formula: `quantity_for_sale = quantity_available - quantity_reserved`
- Reservation: `quantity_reserved += quantity` on confirm
- Release: `quantity_reserved -= quantity` on cancel/expire
- RESERVE and RELEASE movement types for audit trail
- Configurable per-tenant: `sales_order_auto_release_days` (default: 7)

**Business Logic**:
- Manual release: cancel order
- Automatic release: @Scheduled job finds orders CONFIRMED + created_at < (NOW() - N days)
- Status transitions: CONFIRMED → EXPIRED (on auto-release)
- Notification sent to salesperson on expiration

**Dashboard Features**:
- Card "Ordens Próximas à Expiração" (< 2 dias)
- Actions: Faturar (invoices) or Prorrogar (extends +7 days)
- Extension requires MANAGER/ADMIN permission

---

**Story criada por**: Claude Code Assistant (Product Manager)
**Data**: 2025-11-21
**Baseado em**: Epic 4, docs/epics/epic-04-sales-channels.md
