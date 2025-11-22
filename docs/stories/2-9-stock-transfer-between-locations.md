# Story 2.9: Stock Transfer Between Locations

**Epic**: 2 - Product Catalog & Inventory Foundation
**Story ID**: 2.9
**Status**: approved
**Created**: 2025-11-21
**Updated**: 2025-11-21

---

## User Story

Como **estoquista**,
Eu quero **transferir produtos entre locais de estoque com rastreabilidade**,
Para que **eu possa movimentar mercadoria entre loja, depósito e CD mantendo histórico**.

---

## Context & Business Value

Esta story implementa a funcionalidade de transferência de estoque entre diferentes locais de armazenamento (multi-warehouse), garantindo rastreabilidade completa de todas as movimentações através do sistema de histórico auditável.

**Valor de Negócio:**
- **Flexibilidade Operacional**: Permite redistribuir estoque entre locais conforme demanda
- **Rastreabilidade**: Histórico completo de origem e destino de cada transferência
- **Auditoria**: Cada transferência gera duas movimentações linkadas (saída + entrada)
- **Controle**: Validação automática de disponibilidade antes de transferir

**Contexto Arquitetural:**
- **Transação Atômica**: Transferência deve ser atomic (saída origem + entrada destino)
- **Auditoria**: Cria duas movimentações em `stock_movements` (EXIT + ENTRY) linkadas
- **Validação**: Verifica disponibilidade no local de origem antes de transferir
- **Integridade**: Rollback automático se falhar qualquer etapa

---

## Acceptance Criteria

### AC1: Tabela stock_transfers Criada
- [ ] Migration cria tabela `stock_transfers` no schema tenant com colunas:
  - `id` (UUID, PK)
  - `tenant_id` (UUID, FK para tenants)
  - `product_id` (UUID, FK para products, NULLABLE)
  - `variant_id` (UUID, FK para product_variants, NULLABLE)
  - `origin_location_id` (UUID, FK para stock_locations)
  - `destination_location_id` (UUID, FK para stock_locations)
  - `quantity` (DECIMAL(10,2), NOT NULL)
  - `reason` (TEXT)
  - `user_id` (UUID, FK para users)
  - `status` (VARCHAR(20), DEFAULT 'COMPLETED')
  - `data_criacao` (TIMESTAMP)
- [ ] Índices criados: `idx_stock_transfers_tenant_id`, `idx_stock_transfers_origin`, `idx_stock_transfers_destination`
- [ ] Constraint: `CHECK (origin_location_id != destination_location_id)`
- [ ] Constraint: `CHECK (quantity > 0)`

### AC2: Endpoint de Criação de Transferência
- [ ] `POST /api/stock/transfers` cria transferência com payload:
  ```json
  {
    "product_id": "uuid",
    "origin_location_id": "uuid",
    "destination_location_id": "uuid",
    "quantity": 10.0,
    "reason": "Reposição de estoque da loja"
  }
  ```
- [ ] Validação: origem e destino devem ser diferentes
- [ ] Validação: quantidade deve ser maior que zero
- [ ] Validação: estoque origem tem `quantity_available >= quantity`
- [ ] Retorna HTTP 400 se validações falharem com mensagem clara
- [ ] Retorna HTTP 201 com transferência criada se sucesso

### AC3: Atualização de Estoque Transacional
- [ ] Transferência é processada em transação @Transactional
- [ ] Atualiza tabela `stock` na origem: `quantity_available -= quantity`
- [ ] Atualiza tabela `stock` no destino: `quantity_available += quantity`
- [ ] Se não existir registro de estoque no destino, cria automaticamente
- [ ] Se qualquer etapa falhar, rollback completo (origem não é debitada)

### AC4: Criação de Movimentações de Auditoria
- [ ] Cria duas movimentações em `stock_movements` linkadas ao `transfer_id`:
  1. **EXIT** (origem):
     - `type = EXIT`
     - `stock_location_id = origin_location_id`
     - `quantity = -quantity` (negativo)
     - `document_id = transfer_id`
     - `balance_before` e `balance_after` registrados
  2. **ENTRY** (destino):
     - `type = ENTRY`
     - `stock_location_id = destination_location_id`
     - `quantity = +quantity` (positivo)
     - `document_id = transfer_id`
     - `balance_before` e `balance_after` registrados
- [ ] Ambas movimentações têm mesmo `user_id` e `timestamp`

### AC5: Endpoint de Histórico de Transferências
- [ ] `GET /api/stock/transfers` retorna histórico de transferências com filtros:
  - `product_id` (opcional)
  - `origin_location_id` (opcional)
  - `destination_location_id` (opcional)
  - `date_from` / `date_to` (opcional)
  - `user_id` (opcional)
- [ ] Retorna lista paginada (default 20 por página)
- [ ] Response inclui: transfer_id, produto, origem, destino, quantidade, razão, usuário, data

### AC6: Frontend - Formulário de Transferência
- [ ] Component Angular `StockTransferFormComponent` criado
- [ ] Campo autocomplete para buscar produto (por nome ou SKU)
- [ ] Dropdown para selecionar local de origem (mostra estoque disponível)
- [ ] Dropdown para selecionar local de destino
- [ ] Campo numérico para quantidade (validação: max = estoque disponível origem)
- [ ] Campo textarea para motivo/razão da transferência
- [ ] Botão "Transferir" desabilitado se validações não passarem

### AC7: Frontend - Confirmação e Feedback
- [ ] Modal de confirmação exibe resumo antes de confirmar:
  - "Transferir 10 unidades de [Produto X]"
  - "De: [Depósito Central] (Disponível: 50)"
  - "Para: [Loja Shopping] (Disponível: 5)"
- [ ] Ao confirmar, exibe loading spinner durante processamento
- [ ] Sucesso: toast verde "Transferência realizada com sucesso"
- [ ] Erro: toast vermelho com mensagem do backend
- [ ] Após sucesso, limpa formulário e atualiza estoque em tempo real

### AC8: Frontend - Histórico de Transferências
- [ ] Component `StockTransferHistoryComponent` exibe tabela com colunas:
  - Data/Hora
  - Produto
  - Origem
  - Destino
  - Quantidade
  - Usuário
  - Motivo
- [ ] Filtros: período, produto, origem, destino
- [ ] Paginação (20 registros por página)
- [ ] Ordenação por data decrescente (mais recentes primeiro)

---

## Tasks & Subtasks

### Task 1: Criar Migration de stock_transfers
- [ ] Criar migration `V039__create_stock_transfers_table.sql`
- [ ] Definir estrutura com FKs para products, stock_locations, users
- [ ] Criar índices e constraints
- [ ] Testar migration: `mvn flyway:migrate`

### Task 2: Criar Entidade e Repository
- [ ] Criar `StockTransfer.java` em `inventory.domain`
- [ ] Criar `StockTransferRepository` extends `CrudRepository`
- [ ] Método `findByTenantId()`
- [ ] Método customizado para filtros (product, locations, dates)

### Task 3: Implementar StockTransferService
- [ ] Criar `StockTransferService` com método `transferStock()`
- [ ] Anotar com `@Transactional` para garantir atomicidade
- [ ] Validar disponibilidade no estoque de origem
- [ ] Atualizar tabela `stock` (origem e destino)
- [ ] Criar duas movimentações em `stock_movements`
- [ ] Método `getTransferHistory()` com filtros

### Task 4: Criar StockTransferController
- [ ] Criar `POST /api/stock/transfers`
- [ ] Criar `GET /api/stock/transfers` com query params para filtros
- [ ] Tratamento de erros (400 para validações, 409 para estoque insuficiente)
- [ ] DTO: `StockTransferRequestDTO` e `StockTransferResponseDTO`

### Task 5: Frontend - StockTransferFormComponent
- [ ] Criar component com formulário reativo
- [ ] Implementar autocomplete de produto (debounce 300ms)
- [ ] Dropdown de locais com display de estoque disponível
- [ ] Validação: quantidade <= estoque disponível origem
- [ ] Modal de confirmação antes de submeter

### Task 6: Frontend - StockTransferHistoryComponent
- [ ] Criar component com tabela de histórico
- [ ] Implementar filtros (datepicker para período, dropdowns)
- [ ] Paginação com Angular Material Paginator
- [ ] Service: `StockTransferService.getHistory(filters)`

### Task 7: Testes
- [ ] Teste de integração: transferência bem-sucedida cria 2 movimentações
- [ ] Teste: validação de estoque insuficiente retorna 400
- [ ] Teste: rollback se falhar atualização de destino
- [ ] Teste: constraint impede origem == destino
- [ ] Teste: busca com filtros retorna resultados corretos

---

## Definition of Done (DoD)

- [ ] Migration executada com sucesso
- [ ] Entidade StockTransfer e Repository criados
- [ ] StockTransferService implementado com transação atômica
- [ ] StockTransferController com endpoints funcionais
- [ ] Frontend permite criar transferência com validações
- [ ] Histórico de transferências exibido corretamente
- [ ] Duas movimentações criadas em stock_movements para cada transferência
- [ ] Testes de integração passando (incluindo rollback)
- [ ] Code review aprovado
- [ ] Documentação técnica atualizada

---

## Dependencies & Blockers

**Depende de:**
- Story 2.6 (Stock Locations Management) - Precisa de locais cadastrados
- Story 2.7 (Multi-Warehouse Stock Control) - Precisa de tabela `stock`
- Story 2.8 (Stock Movement History) - Precisa de tabela `stock_movements`

**Bloqueia:**
- Nenhuma story diretamente, mas é funcionalidade crítica para operação multi-depósito

---

## Technical Notes

**Exemplo de Transação Atômica:**
```java
@Transactional
public StockTransfer transferStock(StockTransferRequestDTO request) {
    // 1. Validar estoque origem
    Stock originStock = stockRepository.findByProductAndLocation(
        request.getProductId(), request.getOriginLocationId()
    );
    if (originStock.getQuantityAvailable() < request.getQuantity()) {
        throw new InsufficientStockException("Estoque insuficiente na origem");
    }

    // 2. Criar registro de transferência
    StockTransfer transfer = new StockTransfer();
    transfer.setProductId(request.getProductId());
    transfer.setOriginLocationId(request.getOriginLocationId());
    transfer.setDestinationLocationId(request.getDestinationLocationId());
    transfer.setQuantity(request.getQuantity());
    transfer.setReason(request.getReason());
    transfer.setUserId(getCurrentUserId());
    transfer.setStatus("COMPLETED");
    StockTransfer savedTransfer = transferRepository.save(transfer);

    // 3. Atualizar estoque origem (saída)
    originStock.setQuantityAvailable(
        originStock.getQuantityAvailable().subtract(request.getQuantity())
    );
    stockRepository.save(originStock);

    // 4. Atualizar estoque destino (entrada)
    Stock destinationStock = stockRepository.findByProductAndLocation(
        request.getProductId(), request.getDestinationLocationId()
    ).orElse(createNewStock(request.getProductId(), request.getDestinationLocationId()));

    BigDecimal balanceBeforeDest = destinationStock.getQuantityAvailable();
    destinationStock.setQuantityAvailable(
        destinationStock.getQuantityAvailable().add(request.getQuantity())
    );
    stockRepository.save(destinationStock);

    // 5. Criar movimentações de auditoria
    createStockMovement(StockMovementType.EXIT, request.getProductId(),
        request.getOriginLocationId(), request.getQuantity().negate(),
        savedTransfer.getId(), "Transferência para " + getLocationName(request.getDestinationLocationId()));

    createStockMovement(StockMovementType.ENTRY, request.getProductId(),
        request.getDestinationLocationId(), request.getQuantity(),
        savedTransfer.getId(), "Transferência de " + getLocationName(request.getOriginLocationId()));

    return savedTransfer;
}
```

**Payload de Request:**
```json
{
  "product_id": "123e4567-e89b-12d3-a456-426614174000",
  "origin_location_id": "223e4567-e89b-12d3-a456-426614174000",
  "destination_location_id": "323e4567-e89b-12d3-a456-426614174000",
  "quantity": 10.0,
  "reason": "Reposição de estoque da loja devido a alta demanda"
}
```

**Response de Sucesso:**
```json
{
  "id": "423e4567-e89b-12d3-a456-426614174000",
  "product_name": "Notebook Dell Inspiron 15",
  "origin_location": "Depósito Central",
  "destination_location": "Loja Shopping",
  "quantity": 10.0,
  "reason": "Reposição de estoque da loja devido a alta demanda",
  "user": "João Silva",
  "status": "COMPLETED",
  "created_at": "2025-11-21T14:30:00Z"
}
```

---

## Change Log

- **2025-11-21**: Story drafted pelo assistente Claude Code
- **2025-11-21**: Corrigida versão da migration de V036 para V039 (Product Owner Sarah - validação épico)

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
