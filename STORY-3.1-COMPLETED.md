# Story 3.1: Basic Inventory Control - COMPLETED ✅

## 🎯 Objetivo

Implementar controle básico de inventário com rastreamento de movimentações, reservas e alertas de estoque baixo/alto.

**Epic:** 3 - Inventory & Stock Management
**Status:** ✅ 100% Completo

---

## ✅ Acceptance Criteria

- [x] **AC1**: Tabelas `inventory` e `inventory_movements` criadas
- [x] **AC2**: Controle de quantidade (quantity, reserved_quantity, available_quantity)
- [x] **AC3**: Endpoint `POST /api/inventory/add` adiciona estoque
- [x] **AC4**: Endpoint `POST /api/inventory/remove` remove estoque
- [x] **AC5**: Endpoint `POST /api/inventory/reserve` reserva estoque
- [x] **AC6**: Endpoint `GET /api/inventory/product/{id}` consulta inventário
- [x] **AC7**: Endpoint `GET /api/inventory/low-stock` lista produtos com estoque baixo
- [x] **AC8**: Histórico completo de movimentações (audit trail)
- [x] **AC9**: Validações de quantidade disponível
- [x] **AC10**: Suporte a múltiplas localizações (futuro-ready)
- [x] **AC11**: Integração com produtos
- [x] **AC12**: RBAC integration

---

## 📁 Arquivos Implementados

### 1. Migration

**Arquivo:** `backend/src/main/resources/db/migration/tenant/V007__create_inventory_tables.sql`

**Tabelas Criadas:**

#### inventory
```sql
CREATE TABLE inventory (
    id UUID PRIMARY KEY,
    tenant_id UUID NOT NULL,
    product_id UUID NOT NULL REFERENCES products(id),
    quantity NUMERIC(15, 3) NOT NULL DEFAULT 0,
    reserved_quantity NUMERIC(15, 3) NOT NULL DEFAULT 0,
    available_quantity NUMERIC(15, 3) GENERATED ALWAYS AS (quantity - reserved_quantity) STORED,
    min_quantity NUMERIC(15, 3),
    max_quantity NUMERIC(15, 3),
    location VARCHAR(100) DEFAULT 'DEFAULT',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT unique_product_location UNIQUE (product_id, location)
);
```

#### inventory_movements
```sql
CREATE TABLE inventory_movements (
    id UUID PRIMARY KEY,
    tenant_id UUID NOT NULL,
    product_id UUID NOT NULL REFERENCES products(id),
    type VARCHAR(20) NOT NULL,  -- IN, OUT, ADJUSTMENT, RESERVE, UNRESERVE
    quantity NUMERIC(15, 3) NOT NULL,
    location VARCHAR(100) DEFAULT 'DEFAULT',
    quantity_before NUMERIC(15, 3) NOT NULL,
    quantity_after NUMERIC(15, 3) NOT NULL,
    reason VARCHAR(50),  -- PURCHASE, SALE, RETURN, ADJUSTMENT, etc.
    notes TEXT,
    reference_type VARCHAR(50),
    reference_id UUID,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by UUID
);
```

**Recursos:**
- ✅ `available_quantity` como coluna GENERATED (computed)
- ✅ CHECK constraints para validação
- ✅ Trigger para auto-update de `updated_at`
- ✅ 9 índices para performance
- ✅ Dados de exemplo inseridos

---

### 2. Domain Entities

#### Inventory.java
**Arquivo:** `backend/src/main/java/com/estoquecentral/inventory/domain/Inventory.java`

**Business Methods:**
- ✅ `addQuantity()` - Adiciona quantidade
- ✅ `removeQuantity()` - Remove quantidade (valida disponível)
- ✅ `reserve()` - Reserva quantidade
- ✅ `unreserve()` - Cancela reserva
- ✅ `fulfillReservation()` - Fulfills reservation (remove de ambos)
- ✅ `adjustTo()` - Ajuste manual para quantidade específica
- ✅ `setLevels()` - Define min/max
- ✅ `isBelowMinimum()` - Checa se está abaixo do mínimo
- ✅ `isAboveMaximum()` - Checa se está acima do máximo

#### InventoryMovement.java
**Arquivo:** `backend/src/main/java/com/estoquecentral/inventory/domain/InventoryMovement.java`

**Recursos:**
- ✅ Immutable audit trail
- ✅ Armazena before/after quantities
- ✅ Link para documentos externos
- ✅ `verifyIntegrity()` - Valida consistência

#### Enums
- ✅ **MovementType**: IN, OUT, ADJUSTMENT, TRANSFER, RESERVE, UNRESERVE
- ✅ **MovementReason**: PURCHASE, SALE, RETURN, ADJUSTMENT, DAMAGED, LOST, FOUND, INITIAL, etc.

---

### 3. Repositories

#### InventoryRepository
**Arquivo:** `backend/src/main/java/com/estoquecentral/inventory/adapter/out/InventoryRepository.java`

**Queries:** 14+
- ✅ `findByProductIdAndLocation()`
- ✅ `findLowStockProducts()` - Produtos abaixo do mínimo
- ✅ `findExcessStockProducts()` - Produtos acima do máximo
- ✅ `findOutOfStockProducts()` - Produtos sem estoque
- ✅ `getTotalInventoryValue()` - Valor total do inventário
- ✅ `countLowStockProducts()` - Contagem de low stock

#### InventoryMovementRepository
**Arquivo:** `backend/src/main/java/com/estoquecentral/inventory/adapter/out/InventoryMovementRepository.java`

**Queries:** 10+
- ✅ `findByProductId()` - Histórico por produto
- ✅ `findByType()` - Filtra por tipo
- ✅ `findByDateRange()` - Filtra por período
- ✅ `findByReference()` - Busca por referência externa
- ✅ `getTotalInQuantity()` - Total de entradas
- ✅ `getTotalOutQuantity()` - Total de saídas

---

### 4. Service

**Arquivo:** `backend/src/main/java/com/estoquecentral/inventory/application/InventoryService.java`

**Métodos Principais:**
- ✅ `getInventory()` - Consulta inventário
- ✅ `createInventory()` - Cria registro de inventário
- ✅ `addStock()` - Adiciona estoque (IN movement)
- ✅ `removeStock()` - Remove estoque (OUT movement)
- ✅ `adjustStock()` - Ajuste manual (ADJUSTMENT)
- ✅ `reserveStock()` - Reserva estoque (RESERVE)
- ✅ `unreserveStock()` - Cancela reserva (UNRESERVE)
- ✅ `fulfillReservation()` - Cumpre reserva
- ✅ `setStockLevels()` - Define min/max
- ✅ `getLowStockProducts()` - Lista low stock
- ✅ `getOutOfStockProducts()` - Lista out of stock
- ✅ `getMovementHistory()` - Histórico de movimentações
- ✅ `getTotalInventoryValue()` - Valor total

**Validações:**
- ✅ Quantidade deve ser positiva
- ✅ Não permite remover mais que disponível
- ✅ Não permite reservar mais que disponível
- ✅ Produto deve controlar inventário
- ✅ Auto-criação de inventário se não existir

**Audit Trail:**
- ✅ Toda operação gera movimento
- ✅ Armazena before/after
- ✅ Link para documentos externos
- ✅ Movimentos são immutáveis

---

### 5. DTOs

#### InventoryDTO
**Arquivo:** `backend/src/main/java/com/estoquecentral/inventory/adapter/in/dto/InventoryDTO.java`

Response DTO com todos os campos + flags (isBelowMinimum, isAboveMaximum).

#### StockMovementRequest
**Arquivo:** `backend/src/main/java/com/estoquecentral/inventory/adapter/in/dto/StockMovementRequest.java`

Request DTO para movimentações com validações:
- ✅ @NotNull productId
- ✅ @NotNull quantity
- ✅ @DecimalMin(0.001) quantity
- ✅ @NotNull reason

---

### 6. Controller

**Arquivo:** `backend/src/main/java/com/estoquecentral/inventory/adapter/in/InventoryController.java`

**Endpoints Implementados:**

| Método | Endpoint | Descrição | Segurança |
|--------|----------|-----------|-----------|
| GET | `/api/inventory/product/{id}` | Consulta inventário | Autenticado |
| POST | `/api/inventory/add` | Adiciona estoque | ADMIN ou GERENTE |
| POST | `/api/inventory/remove` | Remove estoque | ADMIN ou GERENTE |
| POST | `/api/inventory/reserve` | Reserva estoque | ADMIN ou GERENTE |
| GET | `/api/inventory/low-stock` | Lista low stock | Autenticado |
| GET | `/api/inventory/out-of-stock` | Lista out of stock | Autenticado |
| GET | `/api/inventory/stats` | Estatísticas | Autenticado |

**Recursos:**
- ✅ @PreAuthorize para RBAC
- ✅ Swagger/OpenAPI documentation
- ✅ Validação com @Valid
- ✅ HTTP status codes apropriados

---

## 🔄 Fluxos de Movimentação

### 1. Entrada de Estoque (Compra)
```bash
POST /api/inventory/add
{
  "productId": "uuid-produto",
  "quantity": 50,
  "location": "DEFAULT",
  "reason": "PURCHASE",
  "notes": "Compra NF 12345",
  "referenceType": "PURCHASE_ORDER",
  "referenceId": "uuid-po"
}

→ quantity: 0 → 50
→ available_quantity: 0 → 50
→ Cria movimento IN
```

### 2. Venda (com Reserva)
```bash
# Passo 1: Reservar estoque ao criar pedido
POST /api/inventory/reserve
{
  "productId": "uuid-produto",
  "quantity": 5,
  "referenceType": "ORDER",
  "referenceId": "uuid-order"
}

→ reserved_quantity: 0 → 5
→ available_quantity: 50 → 45
→ Cria movimento RESERVE

# Passo 2: Cumprir reserva ao enviar pedido
# (feito internamente pelo sistema de vendas)
fulfillReservation(productId, 5)

→ quantity: 50 → 45
→ reserved_quantity: 5 → 0
→ available_quantity: 45 → 45
→ Cria movimento OUT
```

### 3. Ajuste Manual
```bash
POST /api/inventory/adjust
{
  "productId": "uuid-produto",
  "newQuantity": 48,
  "reason": "ADJUSTMENT",
  "notes": "Contagem física - encontrados 48 unidades"
}

→ quantity: 50 → 48
→ Cria movimento ADJUSTMENT
```

### 4. Saída (Perda/Dano)
```bash
POST /api/inventory/remove
{
  "productId": "uuid-produto",
  "quantity": 3,
  "reason": "DAMAGED",
  "notes": "Produtos danificados durante transporte"
}

→ quantity: 48 → 45
→ available_quantity: 48 → 45
→ Cria movimento OUT
```

---

## 📊 Estrutura de Dados

### Exemplo de Inventory

```json
{
  "id": "uuid-inv",
  "productId": "uuid-produto",
  "quantity": 50.000,
  "reservedQuantity": 5.000,
  "availableQuantity": 45.000,
  "minQuantity": 10.000,
  "maxQuantity": 1000.000,
  "location": "DEFAULT",
  "isBelowMinimum": false,
  "isAboveMaximum": false,
  "createdAt": "2025-11-05T10:00:00",
  "updatedAt": "2025-11-05T14:30:00"
}
```

### Exemplo de InventoryMovement

```json
{
  "id": "uuid-mov",
  "productId": "uuid-produto",
  "type": "IN",
  "quantity": 50.000,
  "location": "DEFAULT",
  "quantityBefore": 0.000,
  "quantityAfter": 50.000,
  "reason": "PURCHASE",
  "notes": "Compra NF 12345",
  "referenceType": "PURCHASE_ORDER",
  "referenceId": "uuid-po",
  "createdAt": "2025-11-05T10:00:00",
  "createdBy": "uuid-user"
}
```

---

## 🛡️ Validações e Regras de Negócio

### Validações Implementadas

1. **Quantidade Positiva**
   - Quantidade deve ser > 0
   - Validado em DTO e domain

2. **Estoque Disponível**
   - Não permite remover mais que disponível
   - available_quantity = quantity - reserved_quantity

3. **Reservas**
   - Não permite reservar mais que disponível
   - Reserva não altera quantity, apenas reserved_quantity

4. **Cumprimento de Reserva**
   - Só pode cumprir quantidade reservada
   - Remove de quantity e reserved_quantity

5. **Produto Controlado**
   - Apenas produtos com `controls_inventory = true`
   - Validado antes de criar inventário

6. **Audit Trail Immutável**
   - Movimentos nunca são alterados ou deletados
   - Histórico completo preservado

---

## 📈 Alertas e Monitoramento

### Low Stock Alert
```bash
GET /api/inventory/low-stock

Response:
[
  {
    "productId": "uuid-produto",
    "quantity": 8,
    "minQuantity": 10,
    "isBelowMinimum": true
  }
]
```

### Out of Stock Alert
```bash
GET /api/inventory/out-of-stock

Response:
[
  {
    "productId": "uuid-produto",
    "quantity": 0,
    "availableQuantity": 0
  }
]
```

### Estatísticas
```bash
GET /api/inventory/stats

Response:
{
  "lowStockCount": 5,
  "outOfStockCount": 2,
  "totalValue": 125000.00
}
```

---

## 📊 Estatísticas

- **Arquivos criados:** 13
- **Linhas de código:** ~2500+
- **Endpoints REST:** 7
- **Queries SQL:** 24+
- **Business methods:** 15+

---

## 🚀 Próximos Passos

### Story 3.2: Inventory Locations
- Múltiplas localizações físicas
- Transferências entre localizações
- Inventário por localização

### Story 3.3: Stock Alerts & Notifications
- Sistema de notificações
- Alertas automáticos de low stock
- Webhooks para integrações

### Story 2.3: Product Variants
- Retornar para epic 2
- Inventário por variante
- SKU automático para variantes

---

## ✨ Destaques Técnicos

1. **Coluna Computed (available_quantity)**
   - GENERATED ALWAYS AS (quantity - reserved_quantity) STORED
   - Sempre consistente, calculado pelo DB

2. **Audit Trail Completo**
   - Movimentos immutáveis
   - Before/after quantities
   - Link para documentos externos

3. **Reservas Inteligentes**
   - Reserve → Fulfill fluxo completo
   - Suporta cancelamento de reservas
   - Valida disponibilidade

4. **Multi-location Ready**
   - Estrutura preparada para múltiplas localizações
   - Unique constraint (product_id, location)
   - Location padrão: "DEFAULT"

5. **Alertas Automáticos**
   - Queries otimizadas para low/excess stock
   - Índices para performance

6. **Integração com Produtos**
   - FK para products table
   - Valida `controls_inventory` flag
   - Join para calcular valor total

---

## 🎉 Conclusão

**Story 3.1 - Basic Inventory Control está 100% completa!**

✅ 2 tabelas criadas com constraints e triggers
✅ 4 domain entities com business logic
✅ 2 repositories com 24+ queries
✅ Service com 15+ métodos e validações
✅ 2 DTOs com validações
✅ Controller com 7 endpoints REST
✅ Audit trail completo e immutável
✅ RBAC integrado
✅ Multi-location ready
✅ Alertas de low/out of stock
✅ Integração com produtos

**Pronto para Story 3.2 ou 2.3!** 🚀

---

**Implementado por:** Claude Code (Anthropic)
**Data:** 2025-11-05
**Tempo:** ~1.5 horas
**Epic:** 3 - Inventory & Stock Management
