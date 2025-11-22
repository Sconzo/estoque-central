# Story 2.8: Stock Movement History

**Epic**: 2 - Product Catalog & Inventory Foundation
**Story ID**: 2.8
**Status**: approved
**Created**: 2025-11-21
**Updated**: 2025-11-21

---

## User Story

Como **auditor**,
Eu quero **histórico completo e imutável de todas as movimentações de estoque**,
Para que **eu possa rastrear qualquer alteração de estoque para auditoria**.

---

## Context & Business Value

Esta story implementa o histórico completo e auditável de todas as movimentações de estoque (stock movements). Toda alteração de estoque (compra, venda, transferência, ajuste) cria um registro imutável (insert-only) para rastreabilidade e auditoria.

**Valor de Negócio:**
- **Auditoria**: Rastreabilidade completa de todas as alterações de estoque
- **Compliance**: Atende requisitos fiscais e regulatórios de rastreamento
- **Debug**: Facilita investigação de divergências de estoque
- **Análise**: Permite relatórios de movimentação (giro de estoque, produtos mais vendidos)
- **Imutabilidade**: Registros não podem ser alterados ou deletados (previne fraude)

**Contexto Arquitetural:**
- **Append-Only Table**: Apenas INSERT, nunca UPDATE ou DELETE
- **Balance Tracking**: Armazena saldo antes e depois de cada movimentação
- **Document Reference**: FK para documento origem (venda, compra, transferência)
- **Movement Types**: ENUM com todos os tipos de movimentação possíveis
- **Event Sourcing**: Permite reconstruir estado atual a partir do histórico

---

## Acceptance Criteria

### AC1: Tabela stock_movements Criada
- [ ] Migration cria tabela `stock_movements` no schema tenant:
  - `id` (UUID, PK)
  - `tenant_id` (UUID, FK para tenants)
  - `product_id` (UUID, FK para products, NULLABLE para variantes)
  - `variant_id` (UUID, FK para product_variants, NULLABLE)
  - `stock_location_id` (UUID, FK para stock_locations, NOT NULL)
  - `type` (ENUM: ENTRY, EXIT, TRANSFER_OUT, TRANSFER_IN, ADJUSTMENT, SALE, PURCHASE, RESERVE, RELEASE, BOM_ASSEMBLY, BOM_DISASSEMBLY)
  - `quantity` (DECIMAL(10,3), NOT NULL - positivo=entrada, negativo=saída)
  - `balance_before` (DECIMAL(10,3), NOT NULL)
  - `balance_after` (DECIMAL(10,3), NOT NULL)
  - `user_id` (UUID, FK para usuarios, NOT NULL - quem fez a movimentação)
  - `document_type` (VARCHAR(50), NULLABLE - ex: "SALE", "PURCHASE", "TRANSFER")
  - `document_id` (UUID, NULLABLE - FK genérico para documento origem)
  - `reason` (TEXT, NULLABLE - motivo/observação)
  - `created_at` (TIMESTAMP, DEFAULT CURRENT_TIMESTAMP)
- [ ] Constraint: `product_id` OU `variant_id` deve ser preenchido
- [ ] Índices: `idx_stock_movements_product`, `idx_stock_movements_variant`, `idx_stock_movements_location`, `idx_stock_movements_created_at`
- [ ] Índice composto: `idx_stock_movements_document` `(document_type, document_id)` para rastreamento
- [ ] **Importante**: Tabela SEM UPDATE ou DELETE (apenas INSERT)

### AC2: Enum MovementType Definido
- [ ] Tipos de movimentação:
  - `ENTRY`: Entrada manual de estoque (inventário inicial, recebimento sem NF)
  - `EXIT`: Saída manual de estoque (perda, quebra, furto)
  - `TRANSFER_OUT`: Saída por transferência entre locais
  - `TRANSFER_IN`: Entrada por transferência entre locais
  - `ADJUSTMENT`: Ajuste de inventário (positivo ou negativo)
  - `SALE`: Saída por venda (FK para sale_id)
  - `PURCHASE`: Entrada por compra (FK para purchase_id)
  - `RESERVE`: Reserva de estoque (diminui disponível, aumenta reservado)
  - `RELEASE`: Liberação de reserva (aumenta disponível, diminui reservado)
  - `BOM_ASSEMBLY`: Montagem de kit (saída de componentes, entrada de kit se físico)
  - `BOM_DISASSEMBLY`: Desmontagem de kit (entrada de componentes, saída de kit)

### AC3: Criação Automática de Movimentações
- [ ] Toda alteração de estoque cria registro em `stock_movements`
- [ ] Campos `balance_before` e `balance_after` preenchidos automaticamente
- [ ] Transação atomica: alteração de `stock` + insert em `stock_movements` no mesmo commit
- [ ] Se falhar insert em `stock_movements`, rollback completo (previne inconsistência)
- [ ] Service method: `createMovement()` centraliza lógica de criação

### AC4: Endpoints de Consulta de Movimentações
- [ ] `GET /api/stock/movements` retorna histórico com filtros e paginação
- [ ] Filtros: `productId`, `variantId`, `stockLocationId`, `type`, `userId`, `dateFrom`, `dateTo`, `documentType`, `documentId`
- [ ] Ordenação padrão: `created_at DESC` (mais recentes primeiro)
- [ ] Response inclui detalhes do produto, local, usuário
- [ ] `GET /api/stock/movements/{id}` retorna detalhes de uma movimentação específica

### AC5: Validação de Integridade de Saldos
- [ ] Endpoint `GET /api/stock/movements/validate-balance?productId={id}&locationId={id}` valida integridade
- [ ] Valida que `balance_after` da última movimentação = `quantity_available` atual em `stock`
- [ ] Response: `{valid: true, lastMovementBalance: 100, currentStockBalance: 100}`
- [ ] Se inválido: `{valid: false, lastMovementBalance: 100, currentStockBalance: 95, discrepancy: -5}`
- [ ] Teste automatizado valida integridade após cada operação

### AC6: Frontend - Stock Movement Timeline
- [ ] Component Angular `StockMovementTimelineComponent` exibe timeline de movimentações
- [ ] Input: `productId` e opcionalmente `stockLocationId`
- [ ] Timeline com ícones por tipo: 📥 (ENTRY), 📤 (EXIT), 🔄 (TRANSFER), 🛒 (SALE), etc.
- [ ] Cada item exibe: tipo, quantidade, saldos (antes/depois), usuário, data/hora, motivo
- [ ] Filtros: período (last 7 days, last 30 days, custom), tipo de movimentação, local
- [ ] Paginação infinita (scroll infinito ou "Load More")
- [ ] Exportação para CSV/Excel

### AC7: Frontend - Movement Details Modal
- [ ] Ao clicar em movimentação, abre modal com detalhes completos
- [ ] Exibe: produto, variante (se houver), local, tipo, quantidade, saldos, usuário, data/hora precisa, motivo
- [ ] Se `document_id` presente: link para documento origem (ex: "Ver Venda #12345")
- [ ] Botão "Exportar para PDF" (gera comprovante de movimentação)

---

## Tasks & Subtasks

### Task 1: Criar Migration de stock_movements
- [ ] Criar migration `V038__create_stock_movements_table.sql`
- [ ] Definir estrutura com constraints e FKs
- [ ] Criar índices (simples e compostos)
- [ ] Testar migration: `mvn flyway:migrate`

### Task 2: Criar Entidade StockMovement
- [ ] Criar `StockMovement.java` em `catalog.domain`
- [ ] Enum `MovementType` com todos os tipos
- [ ] Relacionamentos `@ManyToOne` com Product, ProductVariant, StockLocation, User
- [ ] Annotation `@Immutable` (Hibernate) para prevenir UPDATE
- [ ] Validação: `balanceAfter = balanceBefore + quantity`

### Task 3: Criar StockMovementRepository
- [ ] Criar `StockMovementRepository` extends `CrudRepository`
- [ ] Método `findByProductIdOrderByCreatedAtDesc()`
- [ ] Método `findByVariantIdOrderByCreatedAtDesc()`
- [ ] Query customizada com filtros dinâmicos (Specification ou QueryDSL)
- [ ] Método `findLastByProductAndLocation()` para validação de saldo

### Task 4: Implementar StockMovementService
- [ ] Criar `StockMovementService` com método central `createMovement()`
- [ ] Método `createMovement()`:
  - Obtém saldo atual de `stock`
  - Calcula novo saldo (`balanceBefore + quantity`)
  - Cria registro em `stock_movements`
  - Atualiza registro em `stock`
  - Transação atomica (@Transactional)
- [ ] Método `getMovements()` com filtros
- [ ] Método `validateBalance()` compara última movimentação com estoque atual
- [ ] Método `getMovementsByDocument()` retorna movimentações de um documento

### Task 5: Refatorar Services Existentes
- [ ] Modificar `ProductService`, `SaleService`, `PurchaseService`, etc.
- [ ] Toda alteração de estoque chama `stockMovementService.createMovement()`
- [ ] Exemplos:
  - Venda: `createMovement(type=SALE, quantity=-qtySold, documentId=saleId)`
  - Compra: `createMovement(type=PURCHASE, quantity=+qtyPurchased, documentId=purchaseId)`
  - Ajuste: `createMovement(type=ADJUSTMENT, quantity=diff, reason="Inventário")`

### Task 6: Criar StockMovementController
- [ ] Criar `StockMovementController` em `catalog.adapter.in.web`
- [ ] Endpoints: GET list (com filtros), GET by-id, GET validate-balance
- [ ] DTOs: `StockMovementResponse`, `ValidateBalanceResponse`
- [ ] Paginação com `@PageableDefault(size = 50, sort = "createdAt,desc")`

### Task 7: Frontend - StockMovementTimelineComponent
- [ ] Criar component em `features/catalog/stock-movement-timeline`
- [ ] Timeline com PrimeNG Timeline ou implementação customizada
- [ ] Ícones e cores por tipo de movimentação
- [ ] Filtros com FormGroup (período, tipo, local)
- [ ] Scroll infinito ou paginação
- [ ] Service: `StockMovementService` com métodos HTTP

### Task 8: Frontend - MovementDetailsModal
- [ ] Criar modal component
- [ ] Exibe detalhes completos da movimentação
- [ ] Link para documento origem (roteamento condicional)
- [ ] Botão de exportação para PDF (chama endpoint backend)

### Task 9: Testes
- [ ] Teste de integração: criar movimentação atualiza stock e cria registro
- [ ] Teste: validação de saldo retorna true após movimentação
- [ ] Teste: transação rollback se falhar criar movimentação
- [ ] Teste: query de filtros retorna movimentações corretas
- [ ] Teste: imutabilidade (tentar UPDATE deve falhar)
- [ ] Teste: reconstrução de saldo a partir do histórico

---

## Definition of Done (DoD)

- [ ] Migration executada com sucesso
- [ ] Entidade StockMovement e Repository criados
- [ ] StockMovementService implementado
- [ ] Todos os services existentes integrados (criam movimentações)
- [ ] StockMovementController com endpoints de consulta
- [ ] Validação de integridade de saldo funciona
- [ ] Frontend StockMovementTimelineComponent funcional
- [ ] Frontend MovementDetailsModal com detalhes completos
- [ ] Testes de integração passando
- [ ] Code review aprovado
- [ ] Documentação técnica atualizada

---

## Dependencies & Blockers

**Depende de:**
- Story 2.7 (Multi-Warehouse Stock) - Movimentações alteram estoque

**Bloqueia:**
- Story 3.x (Vendas) - Vendas criam movimentações
- Story 4.x (Compras) - Compras criam movimentações

---

## Technical Notes

**Criação de Movimentação (Service):**
```java
@Transactional
public StockMovement createMovement(CreateMovementRequest request) {
    // Obtém estoque atual
    Stock stock = stockRepository.findByProductIdAndLocationId(
        request.getProductId(), request.getStockLocationId())
        .orElse(new Stock());  // Cria se não existe

    // Calcula saldos
    BigDecimal balanceBefore = stock.getQuantityAvailable();
    BigDecimal balanceAfter = balanceBefore.add(request.getQuantity());

    // Validação: saldo não pode ficar negativo
    if (balanceAfter.compareTo(BigDecimal.ZERO) < 0) {
        throw new InsufficientStockException("Estoque insuficiente para a movimentação");
    }

    // Cria movimentação (imutável)
    StockMovement movement = StockMovement.builder()
        .productId(request.getProductId())
        .stockLocationId(request.getStockLocationId())
        .type(request.getType())
        .quantity(request.getQuantity())
        .balanceBefore(balanceBefore)
        .balanceAfter(balanceAfter)
        .userId(request.getUserId())
        .documentType(request.getDocumentType())
        .documentId(request.getDocumentId())
        .reason(request.getReason())
        .build();

    movement = stockMovementRepository.save(movement);

    // Atualiza estoque
    stock.setQuantityAvailable(balanceAfter);
    if (request.getType() == MovementType.RESERVE) {
        stock.setQuantityReserved(stock.getQuantityReserved().add(request.getQuantity().abs()));
    } else if (request.getType() == MovementType.RELEASE) {
        stock.setQuantityReserved(stock.getQuantityReserved().subtract(request.getQuantity().abs()));
    }
    stockRepository.save(stock);

    return movement;
}
```

**Validação de Integridade de Saldo:**
```java
public ValidateBalanceResponse validateBalance(UUID productId, UUID stockLocationId) {
    // Obtém última movimentação
    StockMovement lastMovement = movementRepository
        .findFirstByProductIdAndStockLocationIdOrderByCreatedAtDesc(productId, stockLocationId)
        .orElse(null);

    // Obtém estoque atual
    Stock currentStock = stockRepository
        .findByProductIdAndLocationId(productId, stockLocationId)
        .orElse(null);

    if (lastMovement == null && currentStock == null) {
        return new ValidateBalanceResponse(true, null, null, null);
    }

    BigDecimal lastBalance = lastMovement != null ? lastMovement.getBalanceAfter() : BigDecimal.ZERO;
    BigDecimal currentBalance = currentStock != null ? currentStock.getQuantityAvailable() : BigDecimal.ZERO;
    BigDecimal discrepancy = currentBalance.subtract(lastBalance);

    boolean valid = discrepancy.compareTo(BigDecimal.ZERO) == 0;

    return ValidateBalanceResponse.builder()
        .valid(valid)
        .lastMovementBalance(lastBalance)
        .currentStockBalance(currentBalance)
        .discrepancy(discrepancy)
        .build();
}
```

**Query de Reconstrução de Saldo:**
```sql
-- Reconstrói saldo atual a partir do histórico (para validação)
SELECT
    product_id,
    stock_location_id,
    SUM(quantity) AS calculated_balance,
    MAX(balance_after) AS last_balance
FROM stock_movements
WHERE product_id = :productId
  AND stock_location_id = :locationId
  AND tenant_id = :tenantId
GROUP BY product_id, stock_location_id;
```

**Exemplo de Request/Response:**
```json
// POST /api/stock/movements (interno, chamado por services)
{
  "productId": "uuid-produto",
  "stockLocationId": "uuid-loja",
  "type": "SALE",
  "quantity": -5,  // negativo = saída
  "userId": "uuid-usuario",
  "documentType": "SALE",
  "documentId": "uuid-venda",
  "reason": "Venda #12345 - Cliente João Silva"
}

// Response 201 Created
{
  "id": "uuid-movement",
  "productId": "uuid-produto",
  "productName": "Mouse Logitech MX Master",
  "stockLocationId": "uuid-loja",
  "locationName": "Loja São Paulo",
  "type": "SALE",
  "quantity": -5,
  "balanceBefore": 50,
  "balanceAfter": 45,
  "userId": "uuid-usuario",
  "userName": "João Silva",
  "documentType": "SALE",
  "documentId": "uuid-venda",
  "reason": "Venda #12345 - Cliente João Silva",
  "createdAt": "2025-11-21T14:35:22Z"
}

// GET /api/stock/movements?productId=uuid&dateFrom=2025-11-01
{
  "movements": [
    {
      "id": "uuid-mov-1",
      "type": "SALE",
      "quantity": -5,
      "balanceBefore": 50,
      "balanceAfter": 45,
      "userName": "João Silva",
      "createdAt": "2025-11-21T14:35:22Z",
      "reason": "Venda #12345"
    },
    {
      "id": "uuid-mov-2",
      "type": "PURCHASE",
      "quantity": 20,
      "balanceBefore": 30,
      "balanceAfter": 50,
      "userName": "Maria Souza",
      "createdAt": "2025-11-20T10:15:00Z",
      "reason": "Recebimento NF #9876"
    }
  ],
  "totalElements": 127,
  "totalPages": 3,
  "currentPage": 0
}
```

---

## Change Log

- **2025-11-21**: Story drafted pelo assistente Claude Code

---

## Dev Agent Record

**Agent Model Used:**
Claude 3.5 Sonnet (claude-sonnet-4-5-20250929)

### Debug Log References

### Completion Notes List

### File List

---

**Story criada por**: Claude Code Assistant (Product Manager)
**Data**: 2025-11-21
**Baseado em**: Epic 2, docs/epics/epic-02-product-catalog.md
