# Story 2.4: Composite Products / Kits (BOM)

**Epic**: 2 - Product Catalog & Inventory Foundation
**Story ID**: 2.4
**Status**: approved
**Created**: 2025-11-21
**Updated**: 2025-11-21

---

## User Story

Como **gerente de loja**,
Eu quero **produtos compostos/kits com BOM (Bill of Materials)**,
Para que **eu possa vender kits formados por múltiplos componentes (ex: Kit Churrasco = Carvão + Acendedor + Espetos)**.

---

## Context & Business Value

Esta story implementa produtos compostos/kits (tipo COMPOSITE) com Bill of Materials (BOM), permitindo vender produtos formados por múltiplos componentes. Suporta dois modos: BOM virtual (baixa componentes na venda) e BOM físico (kit pré-montado com estoque próprio).

**Valor de Negócio:**
- **Flexibilidade**: Permite criar kits promocionais, combos, produtos bundled
- **BOM Virtual**: Evita necessidade de montagem prévia (kit monta automaticamente na venda)
- **BOM Físico**: Suporta kits pré-montados com controle de estoque próprio
- **Cálculo Automático**: Estoque disponível calculado dinamicamente baseado em componentes
- **Prevenção de Erro**: Validação impede vender kit sem estoque suficiente de componentes

**Contexto Arquitetural:**
- **Herança Single-Table**: Produto composite usa type=COMPOSITE
- **BOM Table**: Relaciona produto composto com componentes (product_components)
- **Recursion Prevention**: Validação impede componente ser outro produto composto (1 nível apenas)
- **Stock Calculation**: Query calcula estoque disponível baseado no componente mais limitante

---

## Acceptance Criteria

### AC1: Tabela product_components Criada
- [ ] Migration cria tabela `product_components`:
  - `id` (UUID, PK)
  - `product_id` (UUID, FK para products - o kit/composto)
  - `component_product_id` (UUID, FK para products - o componente)
  - `quantity_required` (DECIMAL(10,3), NOT NULL - quantidade necessária do componente)
- [ ] Constraint: `component_product_id` não pode ser produto type=COMPOSITE (validação em service)
- [ ] Índice: `idx_product_components_product_id`
- [ ] Constraint UNIQUE `(product_id, component_product_id)` previne duplicação

### AC2: Campo bom_type em Products
- [ ] Migration adiciona coluna `bom_type` em `products`:
  - ENUM: `VIRTUAL`, `PHYSICAL`
  - NULLABLE (apenas para products com type=COMPOSITE)
- [ ] BOM Virtual: estoque calculado dinamicamente, não tem registro em `stock`
- [ ] BOM Físico: kit pré-montado, tem registro próprio em `stock`

### AC3: Endpoints de Criação de Produto Composto
- [ ] `POST /api/products` com `type=COMPOSITE` cria produto composto
- [ ] Request inclui `bomType` (VIRTUAL ou PHYSICAL)
- [ ] Validação: produto composto não pode ter `controls_inventory=false`
- [ ] Response retorna produto criado com ID

### AC4: Endpoints de Gestão de BOM
- [ ] `POST /api/products/{id}/bom` define lista de componentes
- [ ] Request: `[{"componentProductId": "uuid", "quantityRequired": 2.5}]`
- [ ] Validação: componente não pode ser produto COMPOSITE (evita recursão)
- [ ] Validação: componente precisa existir e estar ativo
- [ ] Validação: `quantityRequired` > 0
- [ ] `GET /api/products/{id}/bom` retorna lista de componentes com detalhes do produto
- [ ] `DELETE /api/products/{id}/bom/{componentId}` remove componente do BOM

### AC5: Cálculo de Estoque Disponível (BOM Virtual)
- [ ] Endpoint `GET /api/products/{id}/available-stock` calcula estoque disponível do kit
- [ ] Cálculo: `MIN(component_stock / quantity_required)` para todos os componentes
- [ ] Exemplo: Kit precisa 2 espetos (estoque: 10) e 1 carvão (estoque: 3) = pode montar 3 kits (limitado pelo carvão)
- [ ] Se algum componente tem estoque 0, kit tem estoque 0
- [ ] Response: `{"productId": "uuid", "availableQuantity": 3, "limitingComponent": {"id": "uuid-carvao", "name": "Carvão", "stock": 3}}`

### AC6: Baixa de Estoque na Venda (BOM Virtual)
- [ ] Ao vender kit BOM virtual, baixa estoque de cada componente
- [ ] Quantidade baixada: `quantity_sold * quantity_required`
- [ ] Exemplo: venda de 2 kits (precisa 2 espetos cada) = baixa 4 espetos
- [ ] Cria movimentação `BOM_ASSEMBLY` para cada componente
- [ ] Transação atomica: se falhar baixa de algum componente, rollback completo

### AC7: Controle de Estoque (BOM Físico)
- [ ] BOM físico tem registro próprio em `stock` (como produto simples)
- [ ] Endpoint `POST /api/products/{id}/assemble` monta kits fisicamente
- [ ] Request: `{"quantity": 10, "stockLocationId": "uuid"}`
- [ ] Validação: verifica estoque suficiente de componentes
- [ ] Baixa estoque de componentes e adiciona estoque do kit
- [ ] Cria movimentações: EXIT para componentes, ENTRY para kit

### AC8: Frontend - BOM Component Manager
- [ ] Component Angular `BomManagerComponent` permite adicionar/remover componentes
- [ ] Autocomplete para buscar produtos (apenas SIMPLE e VARIANT)
- [ ] Campo de quantidade requerida (decimal, min=0.001)
- [ ] Tabela de componentes: SKU, Nome, Quantidade Requerida, Estoque Atual, Ações
- [ ] Se BOM virtual: exibe cálculo de kits disponíveis em tempo real
- [ ] Se BOM físico: botão "Montar Kits" abre modal de montagem

---

## Tasks & Subtasks

### Task 1: Criar Migration de product_components
- [ ] Criar migration `V033__create_product_components_table.sql`
- [ ] Definir estrutura com FKs e constraints
- [ ] Criar índices
- [ ] Testar migration: `mvn flyway:migrate`

### Task 2: Adicionar Campo bom_type em Products
- [ ] Criar migration `V034__add_bom_type_to_products.sql`
- [ ] Enum `BOM_TYPE` (VIRTUAL, PHYSICAL)
- [ ] Testar migration

### Task 3: Criar Entidade ProductComponent
- [ ] Criar `ProductComponent.java` em `catalog.domain`
- [ ] Relacionamentos `@ManyToOne` com Product (produto e componente)
- [ ] Enum `BomType` (VIRTUAL, PHYSICAL)
- [ ] Adicionar campo `bomType` em entidade `Product`

### Task 4: Criar ProductComponentRepository
- [ ] Criar `ProductComponentRepository` extends `CrudRepository`
- [ ] Método `findByProductId(UUID productId)`
- [ ] Método `deleteByProductIdAndComponentProductId()`

### Task 5: Implementar CompositeProductService
- [ ] Criar `CompositeProductService` com método `addComponent()`
- [ ] Validação: componente não pode ser COMPOSITE (impede recursão)
- [ ] Validação: `quantityRequired` > 0
- [ ] Método `calculateAvailableStock()`: calcula MIN(stock / qty_required)
- [ ] Método `assemblePhysicalKits()`: monta kits físicos (baixa componentes, adiciona kit)
- [ ] Método `disassembleKit()`: baixa kit, adiciona componentes (estorno de montagem)

### Task 6: Criar CompositeProductController
- [ ] Criar `CompositeProductController` em `catalog.adapter.in.web`
- [ ] Endpoints: POST add-bom, GET bom, DELETE remove-component
- [ ] Endpoint: GET available-stock (calcula estoque)
- [ ] Endpoint: POST assemble (monta kits físicos)
- [ ] DTOs: `AddBomComponentRequest`, `BomComponentResponse`, `AssembleKitsRequest`

### Task 7: Integrar com StockMovementService
- [ ] Modificar `StockMovementService` para suportar tipo `BOM_ASSEMBLY`
- [ ] Método `assembleBomVirtual()`: baixa componentes ao vender kit virtual
- [ ] Método `assemblePhysicalKits()`: baixa componentes e adiciona kit
- [ ] Validação: verifica estoque suficiente antes de montar

### Task 8: Frontend - BomManagerComponent
- [ ] Criar component em `features/catalog/bom-manager`
- [ ] Autocomplete de produtos (busca por nome/SKU)
- [ ] FormArray para componentes dinâmicos
- [ ] Tabela de componentes com ações (remover)
- [ ] Indicador visual de estoque disponível para kits
- [ ] Modal de montagem de kits físicos

### Task 9: Testes
- [ ] Teste de integração: criar produto composto com 3 componentes
- [ ] Teste: cálculo de estoque disponível (componente limitante)
- [ ] Teste: validação de recursão (componente não pode ser COMPOSITE) falha
- [ ] Teste: montagem de kit físico baixa componentes e adiciona kit
- [ ] Teste: venda de kit virtual baixa componentes corretamente
- [ ] Teste: transação rollback se estoque insuficiente de componente

---

## Definition of Done (DoD)

- [ ] Migrations executadas com sucesso
- [ ] Entidade ProductComponent criada
- [ ] CompositeProductService implementado com validações
- [ ] CompositeProductController com todos os endpoints
- [ ] Cálculo de estoque disponível funciona corretamente
- [ ] Frontend BomManagerComponent funcional
- [ ] Montagem de kits físicos funciona
- [ ] Venda de kits virtuais baixa componentes
- [ ] Testes de integração passando
- [ ] Code review aprovado
- [ ] Documentação técnica atualizada

---

## Dependencies & Blockers

**Depende de:**
- Story 2.2 (Simple Products) - Componentes são produtos simples
- Story 2.7 (Multi-Warehouse Stock) - Cálculo de estoque disponível

**Bloqueia:**
- Story 3.x (Vendas) - PDV precisa baixar componentes de kits virtuais

---

## Technical Notes

**Algoritmo de Cálculo de Estoque Disponível (BOM Virtual):**
```java
public AvailableStockResponse calculateAvailableStock(UUID productId, UUID stockLocationId) {
    Product product = productRepository.findById(productId).orElseThrow();
    if (product.getBomType() != BomType.VIRTUAL) {
        throw new InvalidOperationException("Cálculo apenas para BOM virtual");
    }

    List<ProductComponent> components = componentRepository.findByProductId(productId);
    if (components.isEmpty()) {
        return new AvailableStockResponse(productId, 0, null);
    }

    // Calcula quantos kits podem ser montados para cada componente
    Integer minKits = null;
    ProductComponent limitingComponent = null;

    for (ProductComponent comp : components) {
        Stock stock = stockRepository.findByProductIdAndLocationId(
            comp.getComponentProductId(), stockLocationId).orElse(null);

        if (stock == null || stock.getQuantityAvailable() == 0) {
            return new AvailableStockResponse(productId, 0, comp);
        }

        int possibleKits = (int) Math.floor(stock.getQuantityAvailable() / comp.getQuantityRequired());

        if (minKits == null || possibleKits < minKits) {
            minKits = possibleKits;
            limitingComponent = comp;
        }
    }

    return new AvailableStockResponse(productId, minKits, limitingComponent);
}
```

**Baixa de Estoque de Kit Virtual na Venda:**
```java
@Transactional
public void sellVirtualKit(UUID productId, int quantity, UUID stockLocationId, UUID userId) {
    List<ProductComponent> components = componentRepository.findByProductId(productId);

    // Verifica se há estoque suficiente antes de iniciar
    for (ProductComponent comp : components) {
        BigDecimal requiredQty = comp.getQuantityRequired().multiply(new BigDecimal(quantity));
        Stock stock = stockRepository.findByProductIdAndLocationId(
            comp.getComponentProductId(), stockLocationId).orElseThrow();

        if (stock.getQuantityAvailable().compareTo(requiredQty) < 0) {
            throw new InsufficientStockException(
                "Estoque insuficiente de " + comp.getComponentProduct().getName());
        }
    }

    // Baixa estoque de cada componente
    for (ProductComponent comp : components) {
        BigDecimal requiredQty = comp.getQuantityRequired().multiply(new BigDecimal(quantity));

        stockMovementService.createMovement(StockMovement.builder()
            .productId(comp.getComponentProductId())
            .stockLocationId(stockLocationId)
            .type(MovementType.BOM_ASSEMBLY)
            .quantity(requiredQty.negate())  // negativo = saída
            .userId(userId)
            .reason("Baixa de componente para montagem de kit: " + productId)
            .build());
    }
}
```

**Montagem de Kits Físicos:**
```java
@Transactional
public void assemblePhysicalKits(UUID productId, int quantity, UUID stockLocationId, UUID userId) {
    Product product = productRepository.findById(productId).orElseThrow();
    if (product.getBomType() != BomType.PHYSICAL) {
        throw new InvalidOperationException("Montagem apenas para BOM físico");
    }

    // Baixa componentes (mesma lógica de kit virtual)
    sellVirtualKit(productId, quantity, stockLocationId, userId);

    // Adiciona estoque do kit montado
    stockMovementService.createMovement(StockMovement.builder()
        .productId(productId)
        .stockLocationId(stockLocationId)
        .type(MovementType.BOM_ASSEMBLY)
        .quantity(new BigDecimal(quantity))  // positivo = entrada
        .userId(userId)
        .reason("Montagem de kits físicos")
        .build());
}
```

**Query SQL de Estoque Disponível:**
```sql
-- Calcula quantos kits podem ser montados baseado em componentes
WITH component_kits AS (
    SELECT
        pc.product_id,
        pc.component_product_id,
        pc.quantity_required,
        s.quantity_available,
        FLOOR(s.quantity_available / pc.quantity_required) AS possible_kits
    FROM product_components pc
    JOIN stock s ON s.product_id = pc.component_product_id
    WHERE pc.product_id = :productId
      AND s.stock_location_id = :locationId
)
SELECT
    product_id,
    MIN(possible_kits) AS available_kits,
    (SELECT component_product_id FROM component_kits WHERE possible_kits = MIN(possible_kits) LIMIT 1) AS limiting_component
FROM component_kits
GROUP BY product_id;
```

**Exemplo de Request/Response:**
```json
// POST /api/products (type=COMPOSITE)
{
  "type": "COMPOSITE",
  "name": "Kit Churrasco Completo",
  "sku": "KIT-CHURRAS-001",
  "categoryId": "uuid-categoria-kits",
  "price": 149.90,
  "cost": 80.00,
  "bomType": "VIRTUAL"
}

// POST /api/products/{id}/bom
{
  "components": [
    {"componentProductId": "uuid-carvao", "quantityRequired": 1},
    {"componentProductId": "uuid-acendedor", "quantityRequired": 1},
    {"componentProductId": "uuid-espetos", "quantityRequired": 6}
  ]
}

// GET /api/products/{id}/available-stock?locationId=uuid-loja
{
  "productId": "uuid-kit-churras",
  "availableQuantity": 8,
  "limitingComponent": {
    "id": "uuid-acendedor",
    "name": "Acendedor de Carvão",
    "quantityRequired": 1,
    "currentStock": 8
  }
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
