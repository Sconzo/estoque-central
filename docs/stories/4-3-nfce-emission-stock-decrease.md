# Story 4.3: NFCe Emission and Stock Decrease

**Epic**: 4 - Sales Channels - PDV & B2B
**Story ID**: 4.3
**Status**: approved
**Created**: 2025-11-21
**Updated**: 2025-11-21

---

## User Story

Como **sistema PDV**,
Eu quero **processar venda com emissão automática de NFCe e baixa de estoque em transação atômica**,
Para que **vendas sejam registradas fiscalmente e estoque atualizado de forma consistente**.

---

## Context & Business Value

Implementa processamento completo de venda: registro no banco, baixa de estoque, emissão de NFCe via middleware terceiro (Focus NFe/NFe.io), e criação de movimentações auditáveis. Transação atômica garante consistência (NFR13).

**Valor de Negócio:**
- **Compliance Fiscal**: NFCe emitida automaticamente conforme legislação
- **Consistência**: Transação atômica (venda + estoque) ou rollback total (NFR13)
- **Auditoria**: Histórico completo de vendas e movimentações
- **Resiliência**: Fila de retry se emissão NFCe falhar (Story 4.4)

---

## Acceptance Criteria

### AC1: Tabelas sales e sale_items Criadas
- [ ] Migration cria `sales`:
  - id, tenant_id, sale_number (auto-gerado: SALE-YYYYMM-0001), customer_id, stock_location_id
  - payment_method (DINHEIRO, DEBITO, CREDITO, PIX), payment_amount_received, change_amount
  - total_amount, nfce_status (PENDING, EMITTED, FAILED, CANCELLED), nfce_key, nfce_xml
  - created_by_user_id, sale_date, data_criacao
- [ ] Migration cria `sale_items`:
  - id, sale_id, product_id, variant_id, quantity, unit_price, total_price
- [ ] Índices: idx_sales_tenant_id, idx_sales_sale_number, idx_sales_nfce_status

### AC2: Endpoint POST /api/sales (Processar Venda)
- [ ] Recebe payload (exemplo em Technical Notes)
- [ ] Validações: estoque disponível >= quantidade
- [ ] Transação @Transactional:
  1. Criar registro de venda
  2. Criar itens da venda
  3. Baixar estoque (quantity_available -= quantity)
  4. Criar movimentações SALE em stock_movements
  5. Tentar emitir NFCe (chamada síncrona, timeout 10s)
  6. Se NFCe OK: commit, retorna HTTP 201
  7. Se NFCe falha: salva com nfce_status=PENDING, enfileira retry (Story 4.4), retorna HTTP 201 (venda salva, NFCe pendente)
  8. Se falha estoque/bd: rollback completo

### AC3: Integração com Middleware NFCe (Focus NFe ou NFe.io)
- [ ] Service `NfceService` com método `emitNfce(sale)`
- [ ] Monta XML conforme layout SEFAZ (itens, cliente, totais, impostos)
- [ ] Envia para middleware via REST API
- [ ] Timeout: 10 segundos (evita travar PDV)
- [ ] Se sucesso: salva nfce_key, nfce_xml, atualiza status=EMITTED
- [ ] Se timeout/erro: atualiza status=PENDING, lança exception capturada para enfileirar retry

### AC4: Baixa Automática de Estoque
- [ ] Para cada item da venda:
  - Atualizar `stock.quantity_available -= quantity`
  - Validação prévia: estoque >= quantidade (HTTP 409 se insuficiente)
  - Criar movimentação tipo SALE em stock_movements

### AC5: Suporte a Produtos Compostos (BOM Virtual - FR9)
- [ ] Se produto é COMPOSITE com BOM virtual:
  - Não baixa estoque do produto pai
  - Baixa estoque de cada componente conforme BOM
  - Exemplo: Kit Churrasco (BOM virtual) baixa Carvão (2x), Acendedor (1x), Espetos (4x)
  - Validação: todos os componentes têm estoque suficiente

### AC6: Frontend - Feedback de Venda Processada
- [ ] Ao clicar "Confirmar Pagamento" (Story 4.2):
  - Loading spinner exibido
  - Request POST /api/sales
  - Sucesso: toast verde "Venda realizada! NFCe: [chave]" ou "Venda realizada! NFCe em processamento..."
  - Erro: toast vermelho com mensagem
  - Após sucesso: limpa carrinho, retorna ao estado inicial

### AC7: Logs de Auditoria Fiscal (NFR16)
- [ ] Tabela `fiscal_events` para auditoria (retenção 5 anos NFR16):
  - id, tenant_id, event_type (NFCE_EMITTED, NFCE_CANCELLED, NFCE_FAILED), sale_id, nfce_key, xml_snapshot, timestamp, user_id
- [ ] Registro criado ao emitir, cancelar ou falhar NFCe
- [ ] Logs imutáveis (insert-only)

---

## Tasks & Subtasks

### Task 1: Criar Migrations de sales, sale_items, fiscal_events
- [ ] V048__create_sales_table.sql
- [ ] V049__create_sale_items_table.sql
- [ ] V050__create_fiscal_events_table.sql

### Task 2: Criar Entidades e Repositories
- [ ] Sale.java, SaleItem.java, FiscalEvent.java
- [ ] Enum PaymentMethod, NfceStatus
- [ ] SaleRepository, SaleItemRepository, FiscalEventRepository

### Task 3: Implementar SaleNumberGenerator
- [ ] Formato: SALE-YYYYMM-9999
- [ ] Sequência mensal por tenant

### Task 4: Implementar NfceService (Integração Middleware)
- [ ] Método `emitNfce(Sale sale)` retorna NfceResponse
- [ ] Monta XML conforme layout SEFAZ 4.0
- [ ] Chamada REST para Focus NFe/NFe.io (configurável via env var)
- [ ] Timeout 10s (RestTemplate com timeout config)
- [ ] Tratamento de erros: timeout, 4xx, 5xx

### Task 5: Implementar SaleService
- [ ] Método `processSale(SaleRequestDTO)` anotado @Transactional
- [ ] Validar estoque disponível
- [ ] Criar venda e itens
- [ ] Baixar estoque (chamar StockService.decreaseStock())
- [ ] Criar movimentações SALE
- [ ] Tentar emitir NFCe (try-catch)
- [ ] Se falha NFCe: salvar PENDING e enfileirar retry

### Task 6: Criar SaleController
- [ ] POST /api/sales
- [ ] DTOs: SaleRequestDTO, SaleResponseDTO
- [ ] Tratamento de erros (409 estoque insuficiente, 500 outros)

### Task 7: Frontend - Integração PDV (Story 4.2)
- [ ] SaleService.processSale(payload)
- [ ] Loading state durante requisição
- [ ] Toasts de sucesso/erro

### Task 8: Testes

#### Testing

- [ ] Teste: venda com estoque suficiente cria sale e baixa estoque
- [ ] Teste: venda com produto BOM virtual baixa componentes
- [ ] Teste: NFCe emitida com sucesso atualiza status=EMITTED
- [ ] Teste: NFCe falha marca PENDING e não faz rollback de venda
- [ ] Teste: estoque insuficiente retorna 409 e não cria venda
- [ ] Teste: movimentações SALE criadas corretamente

---

## Definition of Done (DoD)

- [ ] Migrations executadas
- [ ] Entidades Sale, SaleItem, FiscalEvent criadas
- [ ] NfceService integrado com middleware
- [ ] SaleService implementado com transação atômica
- [ ] SaleController com endpoint POST /api/sales
- [ ] Frontend processa venda com feedback
- [ ] Estoque baixado corretamente
- [ ] Suporte a produtos BOM virtual (FR9)
- [ ] Logs de auditoria fiscal criados (NFR16)
- [ ] Testes de integração passando
- [ ] Code review aprovado

---

## Dependencies & Blockers

**Depende de:**
- Story 4.1 (Customers) - Venda precisa de customer_id
- Story 4.2 (PDV) - PDV envia payload de venda
- Story 2.7 (Stock Control) - Atualiza estoque
- Story 2.8 (Stock Movements) - Cria movimentações

**Bloqueia:**
- Story 4.4 (Fila de Retry NFCe) - Enfileira vendas com PENDING

---

## Technical Notes

**Payload Request:**
```json
{
  "customer_id": "123e4567-e89b-12d3-a456-426614174000",
  "stock_location_id": "223e4567-e89b-12d3-a456-426614174000",
  "payment_method": "DINHEIRO",
  "payment_amount_received": 100.00,
  "items": [
    {
      "product_id": "323e4567-e89b-12d3-a456-426614174000",
      "quantity": 2.00,
      "unit_price": 45.00
    }
  ]
}
```

**Response Success:**
```json
{
  "id": "423e4567-e89b-12d3-a456-426614174000",
  "sale_number": "SALE-202511-0123",
  "total_amount": 90.00,
  "change_amount": 10.00,
  "nfce_status": "EMITTED",
  "nfce_key": "35251112345678901234550010001234561001234567",
  "sale_date": "2025-11-21T15:30:00Z"
}
```

**Processamento de Venda (Service):**
```java
@Service
public class SaleService {
    @Transactional
    public Sale processSale(SaleRequestDTO request) {
        // 1. Validar estoque
        for (SaleItemRequestDTO item : request.getItems()) {
            Stock stock = stockRepository.findByProductAndLocation(...);
            if (stock.getQuantityAvailable().compareTo(item.getQuantity()) < 0) {
                throw new InsufficientStockException("Estoque insuficiente");
            }
        }

        // 2. Criar venda
        Sale sale = new Sale();
        sale.setSaleNumber(saleNumberGenerator.generate(getTenantId()));
        sale.setCustomerId(request.getCustomerId());
        sale.setStockLocationId(request.getStockLocationId());
        sale.setPaymentMethod(request.getPaymentMethod());
        sale.setTotalAmount(calculateTotal(request.getItems()));
        sale.setNfceStatus(NfceStatus.PENDING);
        Sale savedSale = saleRepository.save(sale);

        // 3. Criar itens e baixar estoque
        for (SaleItemRequestDTO itemReq : request.getItems()) {
            SaleItem item = new SaleItem();
            item.setSaleId(savedSale.getId());
            item.setProductId(itemReq.getProductId());
            item.setQuantity(itemReq.getQuantity());
            item.setUnitPrice(itemReq.getUnitPrice());
            saleItemRepository.save(item);

            // Baixar estoque
            stockService.decreaseStock(
                itemReq.getProductId(),
                request.getStockLocationId(),
                itemReq.getQuantity()
            );

            // Criar movimentação
            createStockMovement(SALE, itemReq.getProductId(), savedSale.getId(), ...);
        }

        // 4. Tentar emitir NFCe
        try {
            NfceResponse nfce = nfceService.emitNfce(savedSale);
            savedSale.setNfceStatus(NfceStatus.EMITTED);
            savedSale.setNfceKey(nfce.getKey());
            savedSale.setNfceXml(nfce.getXml());
            fiscalEventRepository.save(createFiscalEvent(NFCE_EMITTED, savedSale));
        } catch (Exception e) {
            // NFCe falhou, mas venda está salva
            savedSale.setNfceStatus(NfceStatus.PENDING);
            fiscalEventRepository.save(createFiscalEvent(NFCE_FAILED, savedSale, e.getMessage()));
            // Enfileirar retry (Story 4.4)
            retryQueueService.enqueue(savedSale.getId());
        }

        return saleRepository.save(savedSale);
    }
}
```

---

## Change Log

| Data       | Autor                  | Alteração                                                         |
|------------|------------------------|-------------------------------------------------------------------|
| 2025-11-21 | Claude Code (PM)       | Story drafted                                                     |
| 2025-11-21 | Sarah (PO)             | Migration versions corrigidas de V040-V042 para V048-V050 (validação épico) |
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
- ✅ Migration versions corrected to V048-V050
- ✅ Template compliance achieved

**Notes**:
- OUTSTANDING transactional integrity design (@Transactional for sale + stock + NFCe)
- NFCe resilience pattern excellent (PENDING status + enqueue retry if fail)
- NFR13 compliance (atomic transactions) properly implemented
- NFR16 compliance (fiscal_events audit table with 5-year retention) specified
- BOM virtual support (FR9) integrated correctly
- Ready for development

**Technical Highlights**:
- Atomic transaction: venda + itens + baixa estoque + movimentações + NFCe
- Rollback on failure prevents inconsistent state
- Middleware integration (Focus NFe/NFe.io) with 10s timeout
- fiscal_events table for immutable audit trail
- Supports composite products (BOM virtual) with component stock decrease

**Risk Mitigation**:
- NFCe timeout won't block PDV (saved as PENDING, retried by Story 4.4)
- Stock validation before sale prevents overselling
- Fiscal compliance via middleware delegation

---

**Story criada por**: Claude Code Assistant (Product Manager)
**Data**: 2025-11-21
**Baseado em**: Epic 4, docs/epics/epic-04-sales-channels.md
