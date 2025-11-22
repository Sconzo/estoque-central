# Story 3.5: Stock Adjustment (Ajuste de Estoque)

**Epic**: 3 - Purchasing & Inventory Replenishment
**Story ID**: 3.5
**Status**: approved
**Created**: 2025-11-21
**Updated**: 2025-11-21

---

## User Story

Como **gerente de estoque**,
Eu quero **realizar ajustes manuais de estoque com justificativa e auditoria**,
Para que **eu possa corrigir divergências identificadas em inventário físico ou perdas/danos**.

---

## Context & Business Value

Esta story implementa a funcionalidade de ajuste manual de estoque, necessária para correções de inventário, registro de perdas, danos, furtos ou qualquer divergência entre estoque físico e sistema. Diferente de transferências (Story 2.9), ajustes alteram a quantidade total sem movimentar entre locais.

**Valor de Negócio:**
- **Acuracidade**: Permite corrigir divergências mantendo estoque preciso (NFR12: 99%+ acuracidade)
- **Auditoria**: Registra motivo, responsável e timestamp de cada ajuste
- **Compliance**: Histórico imutável para auditorias fiscais e inventário
- **Transparência**: Gestão pode rastrear ajustes frequentes indicando problemas operacionais

**Contexto Arquitetural:**
- **Tipos de Ajuste**: INCREASE (entrada manual), DECREASE (saída manual)
- **Motivos Predefinidos**: Inventário, Perda, Dano, Furto, Erro de Lançamento, Outros
- **Audit Trail**: Movimentação tipo ADJUSTMENT em stock_movements
- **Validação**: Ajuste negativo não pode resultar em estoque negativo

---

## Acceptance Criteria

### AC1: Tabela stock_adjustments Criada
- [ ] Migration cria tabela `stock_adjustments` no schema tenant:
  - `id` (UUID, PK)
  - `tenant_id` (UUID, FK para tenants)
  - `adjustment_number` (VARCHAR(20), auto-gerado, unique por tenant) - formato: ADJ-YYYYMM-0001
  - `product_id` (UUID, FK para products, NULLABLE)
  - `variant_id` (UUID, FK para product_variants, NULLABLE)
  - `stock_location_id` (UUID, FK para stock_locations)
  - `adjustment_type` (VARCHAR(10), NOT NULL) - INCREASE, DECREASE
  - `quantity` (DECIMAL(10,2), NOT NULL) - sempre positivo, tipo define sinal
  - `reason_code` (VARCHAR(50), NOT NULL) - INVENTORY, LOSS, DAMAGE, THEFT, ERROR, OTHER
  - `reason_description` (TEXT) - justificativa detalhada obrigatória
  - `adjusted_by_user_id` (UUID, FK para users)
  - `adjustment_date` (DATE, NOT NULL)
  - `balance_before` (DECIMAL(10,2)) - estoque antes do ajuste
  - `balance_after` (DECIMAL(10,2)) - estoque após ajuste
  - `data_criacao` (TIMESTAMP)
- [ ] Índices: `idx_adjustments_tenant_id`, `idx_adjustments_product_id`, `idx_adjustments_location_id`
- [ ] Constraint: `CHECK (adjustment_type IN ('INCREASE', 'DECREASE'))`
- [ ] Constraint: `CHECK (reason_code IN ('INVENTORY', 'LOSS', 'DAMAGE', 'THEFT', 'ERROR', 'OTHER'))`
- [ ] Constraint: `CHECK (quantity > 0)`

### AC2: Endpoint de Criação de Ajuste
- [ ] `POST /api/stock/adjustments` cria ajuste com payload:
  ```json
  {
    "product_id": "uuid",
    "stock_location_id": "uuid",
    "adjustment_type": "DECREASE",
    "quantity": 5.00,
    "reason_code": "DAMAGE",
    "reason_description": "3 unidades danificadas no transporte interno, 2 unidades vencidas",
    "adjustment_date": "2025-11-21"
  }
  ```
- [ ] Validação: produto e local devem existir
- [ ] Validação: se DECREASE, estoque disponível >= quantidade (não permite negativo)
- [ ] Validação: reason_description obrigatória (min 10 caracteres)
- [ ] Retorna HTTP 400 se validações falharem
- [ ] Retorna HTTP 201 com ajuste criado

### AC3: Atualização de Estoque Transacional
- [ ] Ajuste processado em transação @Transactional
- [ ] Buscar estoque atual no local
- [ ] Registrar `balance_before = estoque_atual`
- [ ] Se INCREASE: `stock.quantity_available += quantity`
- [ ] Se DECREASE: `stock.quantity_available -= quantity`
- [ ] Registrar `balance_after = novo_estoque`
- [ ] Salvar ajuste e estoque na mesma transação
- [ ] Rollback se falhar qualquer etapa

### AC4: Criação de Movimentação de Auditoria
- [ ] Criar movimentação em `stock_movements`:
  - `type = ADJUSTMENT`
  - `stock_location_id` = local do ajuste
  - `quantity` = +quantity (INCREASE) ou -quantity (DECREASE)
  - `document_id = adjustment_id`
  - `user_id = adjusted_by_user_id`
  - `reason = reason_description`
  - `balance_before` e `balance_after`
- [ ] Movimentação é imutável

### AC5: Geração de Número de Ajuste
- [ ] Número gerado automaticamente: `ADJ-YYYYMM-0001`
- [ ] Sequência reinicia mensalmente
- [ ] Único por tenant
- [ ] Similar a OrderNumberGenerator

### AC6: Endpoint de Histórico de Ajustes
- [ ] `GET /api/stock/adjustments` retorna lista paginada com filtros:
  - `product_id` (opcional)
  - `stock_location_id` (opcional)
  - `adjustment_type` (opcional)
  - `reason_code` (opcional)
  - `adjustment_date_from` / `adjustment_date_to` (opcional)
  - `user_id` (opcional)
- [ ] Response inclui: adjustment_number, produto, local, tipo, quantidade, motivo, usuário, data
- [ ] Paginação: default 20 por página
- [ ] Ordenação: por adjustment_date decrescente

### AC7: Endpoint de Detalhes de Ajuste
- [ ] `GET /api/stock/adjustments/{id}` retorna detalhes completos
- [ ] Inclui: todos os campos + balance_before/after para auditoria

### AC8: Frontend - Formulário de Ajuste de Estoque
- [ ] Component Angular `StockAdjustmentFormComponent` criado
- [ ] Campos:
  - Produto* (autocomplete por nome ou SKU)
  - Local de Estoque* (dropdown)
  - Tipo de Ajuste* (radio: Entrada Manual / Saída Manual)
  - Quantidade* (input numérico, > 0)
  - Motivo* (dropdown): Inventário, Perda, Dano, Furto, Erro de Lançamento, Outros
  - Descrição/Justificativa* (textarea, min 10 caracteres)
  - Data do Ajuste* (datepicker, default: hoje)
- [ ] Display de estoque atual do produto no local selecionado
- [ ] Se tipo = DECREASE, validação: quantidade <= estoque disponível
- [ ] Validação inline com mensagens claras

### AC9: Frontend - Modal de Confirmação de Ajuste
- [ ] Antes de submeter, exibir modal de confirmação:
  - "Confirmar ajuste de estoque?"
  - Tipo: Entrada/Saída
  - Produto: [Nome]
  - Local: [Nome]
  - Quantidade: X unidades
  - Estoque atual: Y → Novo estoque: Z
  - Motivo: [Razão]
- [ ] Botões: Confirmar (vermelho se DECREASE, verde se INCREASE), Cancelar
- [ ] Após confirmar, loading spinner durante processamento

### AC10: Frontend - Histórico de Ajustes
- [ ] Component `StockAdjustmentHistoryComponent` criado
- [ ] Tabela com colunas: Número, Data, Produto, Local, Tipo, Quantidade, Estoque Antes, Estoque Depois, Motivo, Usuário
- [ ] Filtros: produto, local, tipo, motivo, período, usuário
- [ ] Badge visual para tipo: verde (INCREASE), vermelho (DECREASE)
- [ ] Ícone de alerta para motivos críticos (THEFT, LOSS)
- [ ] Paginação e ordenação
- [ ] Modal de detalhes com justificativa completa

### AC11: Relatório de Ajustes Frequentes (Alerta)
- [ ] Endpoint `GET /api/stock/adjustments/frequent-adjustments` retorna produtos com ajustes frequentes:
  - Produtos com 3+ ajustes nos últimos 30 dias
  - Agrupado por produto e local
  - Soma de ajustes positivos e negativos
- [ ] Response: product_id, location_id, total_adjustments, total_increase, total_decrease
- [ ] Dashboard exibe alerta visual para produtos com ajustes frequentes

---

## Tasks & Subtasks

### Task 1: Criar Migration de stock_adjustments
- [ ] Criar migration `V046__create_stock_adjustments_table.sql`
- [ ] Definir estrutura com FKs e constraints
- [ ] Criar índices
- [ ] Testar migration: `mvn flyway:migrate`

### Task 2: Criar Entidade e Repository
- [ ] Criar `StockAdjustment.java` em `inventory.domain`
- [ ] Enums: `AdjustmentType`, `AdjustmentReasonCode`
- [ ] Criar `StockAdjustmentRepository` extends `CrudRepository`
- [ ] Método customizado para busca com filtros
- [ ] Método para frequent-adjustments query

### Task 3: Implementar AdjustmentNumberGenerator
- [ ] Service `AdjustmentNumberGenerator` similar aos anteriores
- [ ] Formato: `ADJ-YYYYMM-9999`

### Task 4: Implementar StockAdjustmentService
- [ ] Criar `StockAdjustmentService` com método `createAdjustment()`
- [ ] Anotar com `@Transactional`
- [ ] Validações: estoque suficiente para DECREASE
- [ ] Atualizar estoque
- [ ] Criar movimentação de auditoria
- [ ] Método `getAdjustmentHistory()` com filtros
- [ ] Método `getFrequentAdjustments()`

### Task 5: Criar StockAdjustmentController
- [ ] Criar endpoints: POST, GET (list), GET (detail), GET (frequent)
- [ ] DTOs: `StockAdjustmentRequestDTO`, `StockAdjustmentResponseDTO`
- [ ] Tratamento de erros (400 para validações)

### Task 6: Frontend - StockAdjustmentFormComponent
- [ ] Criar formulário reativo com validações
- [ ] Autocomplete de produto
- [ ] Display de estoque atual
- [ ] Validação customizada para DECREASE
- [ ] Modal de confirmação

### Task 7: Frontend - StockAdjustmentHistoryComponent
- [ ] Criar component com tabela
- [ ] Filtros avançados
- [ ] Badges visuais por tipo
- [ ] Modal de detalhes

### Task 8: Dashboard - Alerta de Ajustes Frequentes
- [ ] Card no dashboard exibindo produtos com ajustes frequentes
- [ ] Ícone de alerta visual
- [ ] Link para detalhes do histórico

### Task 9: Testes

#### Testing

- [ ] Teste de integração: ajuste INCREASE atualiza estoque corretamente
- [ ] Teste: ajuste DECREASE atualiza estoque corretamente
- [ ] Teste: ajuste DECREASE com quantidade > estoque retorna 400
- [ ] Teste: movimentação ADJUSTMENT criada
- [ ] Teste: balance_before e balance_after registrados
- [ ] Teste: frequent-adjustments retorna produtos corretos

---

## Definition of Done (DoD)

- [ ] Migration executada com sucesso
- [ ] Entidade StockAdjustment e Repository criados
- [ ] AdjustmentNumberGenerator implementado
- [ ] StockAdjustmentService implementado com transação atômica
- [ ] StockAdjustmentController com endpoints REST
- [ ] Frontend permite criar ajuste com validações
- [ ] Modal de confirmação implementado
- [ ] Histórico de ajustes exibido corretamente
- [ ] Movimentações de auditoria criadas
- [ ] Alerta de ajustes frequentes no dashboard
- [ ] Testes de integração passando
- [ ] Code review aprovado
- [ ] Documentação técnica atualizada

---

## Dependencies & Blockers

**Depende de:**
- Story 2.6 (Stock Locations Management) - Precisa de locais cadastrados
- Story 2.7 (Multi-Warehouse Stock Control) - Atualiza tabela stock
- Story 2.8 (Stock Movement History) - Cria movimentações

**Bloqueia:**
- Nenhuma story diretamente

**Nota:**
Esta story é complementar à Story 2.9 (Transferências). Enquanto 2.9 move estoque entre locais (total não muda), esta story ajusta quantidade total (entrada/saída manual sem origem/destino).

---

## Technical Notes

**Geração de Número de Ajuste:**
```java
@Service
public class AdjustmentNumberGenerator {
    // Similar a OrderNumberGenerator e ReceivingNumberGenerator
    public synchronized String generateAdjustmentNumber(UUID tenantId) {
        String yearMonth = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMM"));
        String prefix = "ADJ-" + yearMonth + "-";

        String maxNumber = repository.findMaxAdjustmentNumberByTenantAndYearMonth(
            tenantId, yearMonth
        ).orElse(null);

        int nextSequence = 1;
        if (maxNumber != null) {
            nextSequence = Integer.parseInt(maxNumber.substring(prefix.length())) + 1;
        }

        return String.format("%s%04d", prefix, nextSequence);
    }
}
```

**Processamento de Ajuste (Service):**
```java
@Service
public class StockAdjustmentService {
    @Transactional
    public StockAdjustment createAdjustment(StockAdjustmentRequestDTO request) {
        // 1. Validar produto e local
        Product product = productRepository.findById(request.getProductId())
            .orElseThrow(() -> new NotFoundException("Produto não encontrado"));

        StockLocation location = locationRepository.findById(request.getStockLocationId())
            .orElseThrow(() -> new NotFoundException("Local não encontrado"));

        // 2. Buscar estoque atual
        Stock stock = stockRepository.findByProductAndLocation(
            request.getProductId(), request.getStockLocationId()
        ).orElseGet(() -> createNewStock(request.getProductId(), request.getStockLocationId()));

        BigDecimal balanceBefore = stock.getQuantityAvailable();

        // 3. Validar se DECREASE não resulta em negativo
        if (request.getAdjustmentType() == AdjustmentType.DECREASE) {
            if (stock.getQuantityAvailable().compareTo(request.getQuantity()) < 0) {
                throw new ValidationException(
                    String.format("Estoque insuficiente. Disponível: %s, Solicitado: %s",
                        stock.getQuantityAvailable(), request.getQuantity())
                );
            }
        }

        // 4. Criar registro de ajuste
        StockAdjustment adjustment = new StockAdjustment();
        adjustment.setAdjustmentNumber(
            adjustmentNumberGenerator.generateAdjustmentNumber(getTenantId())
        );
        adjustment.setProductId(request.getProductId());
        adjustment.setStockLocationId(request.getStockLocationId());
        adjustment.setAdjustmentType(request.getAdjustmentType());
        adjustment.setQuantity(request.getQuantity());
        adjustment.setReasonCode(request.getReasonCode());
        adjustment.setReasonDescription(request.getReasonDescription());
        adjustment.setAdjustedByUserId(getCurrentUserId());
        adjustment.setAdjustmentDate(request.getAdjustmentDate());
        adjustment.setBalanceBefore(balanceBefore);

        // 5. Atualizar estoque
        if (request.getAdjustmentType() == AdjustmentType.INCREASE) {
            stock.setQuantityAvailable(stock.getQuantityAvailable().add(request.getQuantity()));
        } else {
            stock.setQuantityAvailable(stock.getQuantityAvailable().subtract(request.getQuantity()));
        }

        adjustment.setBalanceAfter(stock.getQuantityAvailable());
        stockRepository.save(stock);

        StockAdjustment savedAdjustment = adjustmentRepository.save(adjustment);

        // 6. Criar movimentação de auditoria
        BigDecimal movementQuantity = request.getAdjustmentType() == AdjustmentType.INCREASE ?
            request.getQuantity() : request.getQuantity().negate();

        createStockMovement(
            StockMovementType.ADJUSTMENT,
            request.getProductId(),
            request.getStockLocationId(),
            movementQuantity,
            savedAdjustment.getId(),
            String.format("%s - %s: %s",
                request.getAdjustmentType(),
                request.getReasonCode(),
                request.getReasonDescription()
            )
        );

        return savedAdjustment;
    }
}
```

**Payload de Request:**
```json
{
  "product_id": "123e4567-e89b-12d3-a456-426614174000",
  "stock_location_id": "223e4567-e89b-12d3-a456-426614174000",
  "adjustment_type": "DECREASE",
  "quantity": 5.00,
  "reason_code": "DAMAGE",
  "reason_description": "3 unidades danificadas durante transporte interno devido a queda de pallet. 2 unidades com prazo de validade vencido identificadas em inventário.",
  "adjustment_date": "2025-11-21"
}
```

**Response de Sucesso:**
```json
{
  "id": "323e4567-e89b-12d3-a456-426614174000",
  "adjustment_number": "ADJ-202511-0008",
  "product": {
    "id": "123e4567-e89b-12d3-a456-426614174000",
    "sku": "PROD-001",
    "name": "Notebook Dell Inspiron 15"
  },
  "stock_location": {
    "id": "223e4567-e89b-12d3-a456-426614174000",
    "name": "Depósito Central"
  },
  "adjustment_type": "DECREASE",
  "quantity": 5.00,
  "reason_code": "DAMAGE",
  "reason_description": "3 unidades danificadas durante transporte interno devido a queda de pallet. 2 unidades com prazo de validade vencido identificadas em inventário.",
  "balance_before": 150.00,
  "balance_after": 145.00,
  "adjusted_by": {
    "id": "423e4567-e89b-12d3-a456-426614174000",
    "name": "Carlos Silva"
  },
  "adjustment_date": "2025-11-21",
  "data_criacao": "2025-11-21T16:00:00Z"
}
```

**Query de Ajustes Frequentes:**
```sql
-- Produtos com 3+ ajustes nos últimos 30 dias
SELECT
    product_id,
    stock_location_id,
    COUNT(*) as total_adjustments,
    SUM(CASE WHEN adjustment_type = 'INCREASE' THEN quantity ELSE 0 END) as total_increase,
    SUM(CASE WHEN adjustment_type = 'DECREASE' THEN quantity ELSE 0 END) as total_decrease
FROM stock_adjustments
WHERE tenant_id = :tenantId
  AND adjustment_date >= CURRENT_DATE - INTERVAL '30 days'
GROUP BY product_id, stock_location_id
HAVING COUNT(*) >= 3
ORDER BY total_adjustments DESC;
```

---

## Change Log

| Data       | Autor                  | Alteração                                                         |
|------------|------------------------|-------------------------------------------------------------------|
| 2025-11-21 | Claude Code (PM)       | Story drafted                                                     |
| 2025-11-21 | Sarah (PO)             | Migration version corrigida de V026 para V046 (validação épico)  |
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
- ✅ Migration version corrected to V046
- ✅ Template compliance achieved

**Notes**:
- Adjustment types well-defined (INCREASE/DECREASE)
- Reason codes comprehensive (INVENTORY, LOSS, DAMAGE, THEFT, ERROR, OTHER)
- Audit trail complete (balance_before/after, justification)
- Frequent adjustment alerting feature adds value (operational insights)
- Ready for development

**Validation Notes**:
- Validação de estoque não-negativo corretamente especificada
- Transação atômica garante consistência
- Complementar à Story 2.9 (Transferências) - diferenciação clara

---

**Story criada por**: Claude Code Assistant (Product Manager)
**Data**: 2025-11-21
**Baseado em**: Epic 3, docs/epics/epic-03-purchasing.md
