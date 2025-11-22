# Story 3.4: Receiving Processing and Weighted Average Cost Update

**Epic**: 3 - Purchasing & Inventory Replenishment
**Story ID**: 3.4
**Status**: approved
**Created**: 2025-11-21
**Updated**: 2025-11-21

---

## User Story

Como **sistema de gestão de estoque**,
Eu quero **processar recebimentos de mercadoria atualizando estoque e calculando custo médio ponderado automaticamente**,
Para que **o estoque reflita entrada de mercadorias com custo atualizado para cálculo correto de margem**.

---

## Context & Business Value

Esta story implementa o processamento backend do recebimento de mercadorias capturado pela Story 3.3. Atualiza estoque, calcula custo médio ponderado, cria movimentações auditáveis e atualiza status da Ordem de Compra.

**Valor de Negócio:**
- **Custo Preciso**: Custo médio ponderado garante margem calculada corretamente
- **Auditoria**: Histórico completo de recebimentos com rastreabilidade
- **Automação**: Elimina cálculo manual de custo e atualização de estoque
- **Integridade**: Transação atômica garante consistência de dados

**Contexto Arquitetural:**
- **Transação Atômica**: Atualização de estoque + custo + movimentações + status OC em única transação
- **Custo Médio Ponderado**: `(estoque_atual * custo_atual + quantidade_recebida * custo_recebimento) / (estoque_atual + quantidade_recebida)`
- **Idempotência**: Recebimentos duplicados devem ser detectados e rejeitados
- **Audit Trail**: Todas as alterações registradas em stock_movements

---

## Acceptance Criteria

### AC1: Tabela receivings Criada
- [ ] Migration cria tabela `receivings` no schema tenant:
  - `id` (UUID, PK)
  - `tenant_id` (UUID, FK para tenants)
  - `receiving_number` (VARCHAR(20), auto-gerado, unique por tenant) - formato: RCV-YYYYMM-0001
  - `purchase_order_id` (UUID, FK para purchase_orders)
  - `stock_location_id` (UUID, FK para stock_locations)
  - `receiving_date` (DATE, NOT NULL)
  - `received_by_user_id` (UUID, FK para users)
  - `notes` (TEXT)
  - `status` (VARCHAR(20), DEFAULT 'COMPLETED') - COMPLETED, CANCELLED
  - `data_criacao` (TIMESTAMP)
- [ ] Migration cria tabela `receiving_items`:
  - `id` (UUID, PK)
  - `receiving_id` (UUID, FK para receivings, ON DELETE CASCADE)
  - `purchase_order_item_id` (UUID, FK para purchase_order_items)
  - `product_id` (UUID, FK para products, NULLABLE)
  - `variant_id` (UUID, FK para product_variants, NULLABLE)
  - `quantity_received` (DECIMAL(10,2), NOT NULL)
  - `unit_cost` (DECIMAL(10,2), NOT NULL) - custo da OC
  - `new_weighted_average_cost` (DECIMAL(10,2)) - custo médio após recebimento
  - `notes` (TEXT)
- [ ] Índices: `idx_receivings_tenant_id`, `idx_receivings_po_id`, `idx_receivings_receiving_number`
- [ ] Constraint: `CHECK (quantity_received > 0)`

### AC2: Endpoint de Processamento de Recebimento
- [ ] `POST /api/receivings` cria recebimento com payload:
  ```json
  {
    "purchase_order_id": "uuid",
    "receiving_date": "2025-11-21",
    "notes": "Recebido conforme esperado",
    "items": [
      {
        "purchase_order_item_id": "uuid",
        "quantity_received": 50.00,
        "notes": "Lote 12345"
      }
    ]
  }
  ```
- [ ] Validação: OC deve existir e ter status SENT ou PARTIALLY_RECEIVED
- [ ] Validação: todos os `purchase_order_item_id` devem pertencer à OC informada
- [ ] Validação: `quantity_received` não pode exceder `quantity_pending` do item da OC
- [ ] Retorna HTTP 400 se validações falharem
- [ ] Retorna HTTP 201 com recebimento criado se sucesso

### AC3: Atualização de Estoque Transacional
- [ ] Recebimento processado em transação @Transactional
- [ ] Para cada item recebido:
  1. Buscar estoque atual no `stock_location_id` da OC
  2. Calcular novo custo médio ponderado
  3. Atualizar `stock.quantity_available += quantity_received`
  4. Atualizar `stock.cost` com novo custo médio ponderado
  5. Se não existir registro de estoque, criar com custo = custo da OC
- [ ] Se qualquer etapa falhar, rollback completo

### AC4: Cálculo de Custo Médio Ponderado
- [ ] Fórmula implementada corretamente:
  ```
  novo_custo = (estoque_atual_qty * custo_atual + qty_recebida * custo_recebimento) /
               (estoque_atual_qty + qty_recebida)
  ```
- [ ] Se estoque atual = 0, novo custo = custo do recebimento
- [ ] Custo arredondado para 2 casas decimais
- [ ] Novo custo salvo em `stock.cost` e `receiving_items.new_weighted_average_cost`

### AC5: Criação de Movimentações de Auditoria
- [ ] Para cada item recebido, criar movimentação em `stock_movements`:
  - `type = ENTRY` ou `PURCHASE`
  - `stock_location_id` = local da OC
  - `quantity = quantity_received` (positivo)
  - `document_id = receiving_id`
  - `user_id = received_by_user_id`
  - `balance_before` e `balance_after` registrados
  - `reason = "Recebimento OC [order_number]"`
- [ ] Movimentações são imutáveis (insert-only)

### AC6: Atualização de Status da Ordem de Compra
- [ ] Após processar recebimento, atualizar `purchase_order_items.quantity_received`:
  ```
  quantity_received += quantity_received_neste_recebimento
  ```
- [ ] Verificar se todos os itens da OC foram recebidos completamente:
  - Se SIM: atualizar `purchase_order.status = COMPLETED`
  - Se NÃO: atualizar `purchase_order.status = PARTIALLY_RECEIVED`
- [ ] Status da OC atualizado automaticamente na mesma transação

### AC7: Geração de Número de Recebimento
- [ ] Número de recebimento gerado automaticamente: `RCV-YYYYMM-0001`
- [ ] Sequência reinicia a cada mês (similar a PO)
- [ ] Número único por tenant
- [ ] Implementado com lógica similar ao OrderNumberGenerator

### AC8: Frontend - Finalização de Recebimento (Story 3.3)
- [ ] Botão "Finalizar Recebimento" na tela de resumo (Story 3.3)
- [ ] Ao clicar, submete payload para `POST /api/receivings`
- [ ] Loading spinner durante processamento
- [ ] Sucesso: toast verde "Recebimento processado com sucesso - [receiving_number]"
- [ ] Erro: toast vermelho com mensagem do backend
- [ ] Após sucesso, limpa fila local e retorna à seleção de OCs

### AC9: Endpoint de Histórico de Recebimentos
- [ ] `GET /api/receivings` retorna lista paginada com filtros:
  - `purchase_order_id` (opcional)
  - `stock_location_id` (opcional)
  - `receiving_date_from` / `receiving_date_to` (opcional)
  - `status` (opcional)
- [ ] Response inclui: receiving_number, OC, fornecedor, data, total itens, total valor, usuário
- [ ] Paginação: default 20 por página

### AC10: Endpoint de Detalhes de Recebimento
- [ ] `GET /api/receivings/{id}` retorna detalhes completos:
  - Cabeçalho: receiving_number, OC, fornecedor, data, usuário, notas
  - Itens: produto, qty_recebida, custo_unit, novo_custo_médio, notas
- [ ] Exibe custo médio antes e depois para cada item (auditoria)

---

## Tasks & Subtasks

### Task 1: Criar Migrations de receivings e receiving_items
- [ ] Criar migration `V043__create_receivings_table.sql`
- [ ] Criar migration `V044__create_receiving_items_table.sql`
- [ ] Definir estrutura master-detail com FKs
- [ ] Criar índices e constraints
- [ ] Testar migrations: `mvn flyway:migrate`

### Task 2: Adicionar Campo cost na Tabela stock
- [ ] Criar migration `V045__add_cost_to_stock_table.sql`
- [ ] Adicionar coluna `cost` (DECIMAL(10,2), DEFAULT 0.00)
- [ ] Coluna armazena custo médio ponderado atual do produto/local

### Task 3: Criar Entidades e Repositories
- [ ] Criar `Receiving.java` em `purchasing.domain`
- [ ] Criar `ReceivingItem.java` em `purchasing.domain`
- [ ] Criar `ReceivingRepository` extends `CrudRepository`
- [ ] Criar `ReceivingItemRepository` extends `CrudRepository`

### Task 4: Implementar ReceivingNumberGenerator
- [ ] Service `ReceivingNumberGenerator` similar a `OrderNumberGenerator`
- [ ] Formato: `RCV-YYYYMM-9999`
- [ ] Sequência reinicia mensalmente

### Task 5: Implementar WeightedAverageCostCalculator
- [ ] Service `WeightedAverageCostCalculator` com método:
  ```java
  BigDecimal calculateNewCost(
      BigDecimal currentQty,
      BigDecimal currentCost,
      BigDecimal receivedQty,
      BigDecimal receivedCost
  )
  ```
- [ ] Implementar fórmula de custo médio ponderado
- [ ] Tratamento de divisão por zero (estoque inicial = 0)
- [ ] Arredondamento para 2 casas decimais

### Task 6: Implementar ReceivingService
- [ ] Criar `ReceivingService` com método `processReceiving()`
- [ ] Anotar com `@Transactional` para atomicidade
- [ ] Validações: OC existe, items válidos, quantidades não excedem pendente
- [ ] Atualizar estoque e custo médio
- [ ] Criar movimentações em stock_movements
- [ ] Atualizar quantity_received da OC
- [ ] Atualizar status da OC (PARTIALLY_RECEIVED ou COMPLETED)
- [ ] Método `getReceivingHistory()` com filtros

### Task 7: Criar ReceivingController
- [ ] Criar endpoints: POST (processar), GET (list), GET (detail)
- [ ] DTOs: `ReceivingRequestDTO`, `ReceivingResponseDTO`, `ReceivingItemDTO`
- [ ] Tratamento de erros (400 para validações, 409 para quantidade excedida)

### Task 8: Frontend - Integração com Story 3.3
- [ ] Implementar `ReceivingService.finalizeReceiving()` no frontend
- [ ] Botão "Finalizar" em `ReceivingSummaryComponent` chama service
- [ ] Tratamento de sucesso/erro com toasts
- [ ] Limpeza de fila local após sucesso

### Task 9: Frontend - Histórico de Recebimentos (Opcional)
- [ ] Component `ReceivingHistoryComponent` (desktop)
- [ ] Lista de recebimentos com filtros
- [ ] Modal de detalhes mostrando itens e custos médios

### Task 10: Testes

#### Testing

- [ ] Teste de integração: recebimento completo atualiza estoque e custo
- [ ] Teste: custo médio ponderado calculado corretamente
- [ ] Teste: recebimento parcial atualiza OC para PARTIALLY_RECEIVED
- [ ] Teste: recebimento total atualiza OC para COMPLETED
- [ ] Teste: movimentação ENTRY criada em stock_movements
- [ ] Teste: quantidade excedendo pendente retorna HTTP 400
- [ ] Teste: rollback se falhar atualização de estoque

---

## Definition of Done (DoD)

- [ ] Migrations executadas com sucesso
- [ ] Entidades Receiving e ReceivingItem criadas
- [ ] ReceivingNumberGenerator implementado
- [ ] WeightedAverageCostCalculator implementado e testado
- [ ] ReceivingService implementado com transação atômica
- [ ] ReceivingController com endpoints REST
- [ ] Frontend finaliza recebimento e processa payload
- [ ] Estoque atualizado corretamente após recebimento
- [ ] Custo médio ponderado calculado e salvo
- [ ] Movimentações de auditoria criadas
- [ ] Status da OC atualizado automaticamente
- [ ] Testes de integração passando (incluindo cálculo de custo)
- [ ] Code review aprovado
- [ ] Documentação técnica atualizada

---

## Dependencies & Blockers

**Depende de:**
- Story 3.2 (Purchase Order Creation) - Recebimento referencia OC
- Story 3.3 (Mobile Receiving) - Frontend captura dados
- Story 2.7 (Multi-Warehouse Stock Control) - Atualiza tabela stock
- Story 2.8 (Stock Movement History) - Cria movimentações

**Bloqueia:**
- Nenhuma story diretamente, mas habilita fluxo completo de compras

---

## Technical Notes

**Cálculo de Custo Médio Ponderado:**
```java
@Service
public class WeightedAverageCostCalculator {
    public BigDecimal calculateNewCost(
        BigDecimal currentQty,
        BigDecimal currentCost,
        BigDecimal receivedQty,
        BigDecimal receivedCost
    ) {
        // Se estoque atual é zero, novo custo = custo do recebimento
        if (currentQty.compareTo(BigDecimal.ZERO) == 0) {
            return receivedCost;
        }

        // Custo médio ponderado
        BigDecimal totalValue = currentQty.multiply(currentCost)
                                .add(receivedQty.multiply(receivedCost));
        BigDecimal totalQty = currentQty.add(receivedQty);

        return totalValue.divide(totalQty, 2, RoundingMode.HALF_UP);
    }
}
```

**Exemplo de Cálculo:**
- Estoque atual: 100 unidades a R$ 10,00 = R$ 1.000,00
- Recebimento: 50 unidades a R$ 12,00 = R$ 600,00
- Novo estoque: 150 unidades
- Novo custo médio: (1.000 + 600) / 150 = R$ 10,67

**Processamento de Recebimento (Service):**
```java
@Service
public class ReceivingService {
    @Autowired
    private ReceivingRepository receivingRepository;
    @Autowired
    private StockRepository stockRepository;
    @Autowired
    private StockMovementRepository movementRepository;
    @Autowired
    private PurchaseOrderRepository poRepository;
    @Autowired
    private WeightedAverageCostCalculator costCalculator;
    @Autowired
    private ReceivingNumberGenerator numberGenerator;

    @Transactional
    public Receiving processReceiving(ReceivingRequestDTO request) {
        // 1. Validar OC
        PurchaseOrder po = poRepository.findById(request.getPurchaseOrderId())
            .orElseThrow(() -> new NotFoundException("OC não encontrada"));

        if (!po.getStatus().equals(PurchaseOrderStatus.SENT) &&
            !po.getStatus().equals(PurchaseOrderStatus.PARTIALLY_RECEIVED)) {
            throw new InvalidStatusException("OC não está disponível para recebimento");
        }

        // 2. Criar registro de recebimento
        Receiving receiving = new Receiving();
        receiving.setReceivingNumber(numberGenerator.generateReceivingNumber(getTenantId()));
        receiving.setPurchaseOrderId(po.getId());
        receiving.setStockLocationId(po.getStockLocationId());
        receiving.setReceivingDate(request.getReceivingDate());
        receiving.setReceivedByUserId(getCurrentUserId());
        receiving.setNotes(request.getNotes());
        receiving.setStatus(ReceivingStatus.COMPLETED);
        Receiving savedReceiving = receivingRepository.save(receiving);

        // 3. Processar cada item
        for (ReceivingItemRequestDTO itemRequest : request.getItems()) {
            PurchaseOrderItem poItem = po.getItems().stream()
                .filter(i -> i.getId().equals(itemRequest.getPurchaseOrderItemId()))
                .findFirst()
                .orElseThrow(() -> new ValidationException("Item não pertence a esta OC"));

            // Validar quantidade pendente
            BigDecimal pendingQty = poItem.getQuantityOrdered().subtract(poItem.getQuantityReceived());
            if (itemRequest.getQuantityReceived().compareTo(pendingQty) > 0) {
                throw new ValidationException(
                    String.format("Quantidade recebida (%s) excede pendente (%s)",
                        itemRequest.getQuantityReceived(), pendingQty)
                );
            }

            // Atualizar estoque e calcular custo médio
            Stock stock = stockRepository.findByProductAndLocation(
                poItem.getProductId(), po.getStockLocationId()
            ).orElse(createNewStock(poItem.getProductId(), po.getStockLocationId()));

            BigDecimal newCost = costCalculator.calculateNewCost(
                stock.getQuantityAvailable(),
                stock.getCost(),
                itemRequest.getQuantityReceived(),
                poItem.getUnitCost()
            );

            stock.setQuantityAvailable(
                stock.getQuantityAvailable().add(itemRequest.getQuantityReceived())
            );
            stock.setCost(newCost);
            stockRepository.save(stock);

            // Criar item de recebimento
            ReceivingItem receivingItem = new ReceivingItem();
            receivingItem.setReceivingId(savedReceiving.getId());
            receivingItem.setPurchaseOrderItemId(poItem.getId());
            receivingItem.setProductId(poItem.getProductId());
            receivingItem.setQuantityReceived(itemRequest.getQuantityReceived());
            receivingItem.setUnitCost(poItem.getUnitCost());
            receivingItem.setNewWeightedAverageCost(newCost);
            receivingItem.setNotes(itemRequest.getNotes());
            receivingItemRepository.save(receivingItem);

            // Criar movimentação de estoque
            createStockMovement(
                StockMovementType.PURCHASE,
                poItem.getProductId(),
                po.getStockLocationId(),
                itemRequest.getQuantityReceived(),
                savedReceiving.getId(),
                "Recebimento OC " + po.getOrderNumber()
            );

            // Atualizar quantity_received na OC
            poItem.setQuantityReceived(
                poItem.getQuantityReceived().add(itemRequest.getQuantityReceived())
            );
        }

        // 4. Atualizar status da OC
        boolean allItemsReceived = po.getItems().stream()
            .allMatch(item -> item.getQuantityReceived().compareTo(item.getQuantityOrdered()) >= 0);

        po.setStatus(allItemsReceived ?
            PurchaseOrderStatus.COMPLETED :
            PurchaseOrderStatus.PARTIALLY_RECEIVED
        );
        poRepository.save(po);

        return savedReceiving;
    }
}
```

**Payload de Request:**
```json
{
  "purchase_order_id": "123e4567-e89b-12d3-a456-426614174000",
  "receiving_date": "2025-11-21",
  "notes": "Recebido conforme esperado. Sem avarias.",
  "items": [
    {
      "purchase_order_item_id": "223e4567-e89b-12d3-a456-426614174000",
      "quantity_received": 50.00,
      "notes": "Lote 12345"
    },
    {
      "purchase_order_item_id": "323e4567-e89b-12d3-a456-426614174000",
      "quantity_received": 25.00,
      "notes": "Lote 12346"
    }
  ]
}
```

**Response de Sucesso:**
```json
{
  "id": "423e4567-e89b-12d3-a456-426614174000",
  "receiving_number": "RCV-202511-0015",
  "purchase_order": {
    "id": "123e4567-e89b-12d3-a456-426614174000",
    "order_number": "PO-202511-0042",
    "supplier_name": "Distribuidora ABC Ltda",
    "new_status": "PARTIALLY_RECEIVED"
  },
  "stock_location": {
    "id": "523e4567-e89b-12d3-a456-426614174000",
    "name": "Depósito Central"
  },
  "receiving_date": "2025-11-21",
  "received_by": {
    "id": "623e4567-e89b-12d3-a456-426614174000",
    "name": "Maria Santos"
  },
  "items": [
    {
      "id": "723e4567-e89b-12d3-a456-426614174000",
      "product": {
        "id": "823e4567-e89b-12d3-a456-426614174000",
        "sku": "PROD-001",
        "name": "Notebook Dell Inspiron 15"
      },
      "quantity_received": 50.00,
      "unit_cost": 15.50,
      "previous_cost": 14.80,
      "new_weighted_average_cost": 15.23,
      "notes": "Lote 12345"
    },
    {
      "id": "923e4567-e89b-12d3-a456-426614174000",
      "product": {
        "id": "a23e4567-e89b-12d3-a456-426614174000",
        "sku": "PROD-002",
        "name": "Mouse Logitech MX Master 3"
      },
      "quantity_received": 25.00,
      "unit_cost": 25.00,
      "previous_cost": 23.50,
      "new_weighted_average_cost": 24.38,
      "notes": "Lote 12346"
    }
  ],
  "status": "COMPLETED",
  "data_criacao": "2025-11-21T15:30:00Z"
}
```

---

## Change Log

| Data       | Autor                  | Alteração                                                         |
|------------|------------------------|-------------------------------------------------------------------|
| 2025-11-21 | Claude Code (PM)       | Story drafted                                                     |
| 2025-11-21 | Sarah (PO)             | Migration versions corrigidas de V023-V025 para V043-V045 (validação épico) |
| 2025-11-21 | Sarah (PO)             | Adicionadas seções Status, Testing, QA Results (template compliance) |

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
- ✅ Migration versions corrected to V043-V045
- ✅ Template compliance achieved

**Notes**:
- OUTSTANDING weighted average cost calculation implementation
- Transactional integrity well-designed
- Atomic update pattern (stock + cost + movements + PO status) excellent
- Ready for development

**Technical Highlights**:
- Custo médio ponderado: `(qty_atual * custo_atual + qty_recebida * custo_recebimento) / (qty_atual + qty_recebida)`
- Edge case handling for zero stock (first receipt) properly addressed
- Idempotency considerations present

---

**Story criada por**: Claude Code Assistant (Product Manager)
**Data**: 2025-11-21
**Baseado em**: Epic 3, docs/epics/epic-03-purchasing.md
