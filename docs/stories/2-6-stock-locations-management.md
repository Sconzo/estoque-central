# Story 2.6: Stock Locations Management

**Epic**: 2 - Product Catalog & Inventory Foundation
**Story ID**: 2.6
**Status**: approved
**Created**: 2025-11-21
**Updated**: 2025-11-21

---

## User Story

Como **gerente de loja**,
Eu quero **CRUD de locais de estoque (multi-depósito)**,
Para que **eu possa gerenciar múltiplos locais de armazenamento (loja, CD, depósito)**.

---

## Context & Business Value

Esta story implementa o cadastro e gerenciamento de locais de estoque (stock locations), que representam depósitos, lojas, centros de distribuição, ou qualquer local físico onde produtos são armazenados. É a base para controle de estoque multi-depósito.

**Valor de Negócio:**
- **Multi-Depósito**: Suporta negócios com múltiplos pontos de estoque (loja + CD)
- **Rastreabilidade**: Cada local tem responsável associado para auditoria
- **Organização**: Facilita transferências e relatórios por local
- **Escalabilidade**: Suporta expansão de negócio (novas lojas, novos depósitos)

**Contexto Arquitetural:**
- **Tenant Isolation**: Locais de estoque são tenant-specific
- **Soft Delete**: Locais inativos mantêm histórico mas não aparecem em dropdowns
- **Constraint**: Não permite deletar local com estoque alocado (previne perda de dados)
- **Responsável**: FK para usuarios permite rastreabilidade e auditoria

---

## Acceptance Criteria

### AC1: Tabela stock_locations Criada
- [ ] Migration cria tabela `stock_locations` no schema tenant:
  - `id` (UUID, PK)
  - `tenant_id` (UUID, FK para tenants)
  - `name` (VARCHAR(100), NOT NULL)
  - `code` (VARCHAR(20), UNIQUE per tenant - ex: "LOJA-SP", "CD-RJ")
  - `address` (TEXT, NULLABLE)
  - `responsible_user_id` (UUID, FK para usuarios, NULLABLE)
  - `ativo` (BOOLEAN, DEFAULT true)
  - `data_criacao` (TIMESTAMP)
  - `data_atualizacao` (TIMESTAMP)
- [ ] Constraint UNIQUE `(tenant_id, code)` garante código único por tenant
- [ ] Índice: `idx_stock_locations_tenant_id`
- [ ] Constraint ON DELETE RESTRICT em `responsible_user_id` (não deleta usuário se for responsável)

### AC2: Endpoints CRUD de Stock Locations
- [ ] `POST /api/stock-locations` cria local de estoque
- [ ] Validação: `code` único por tenant (HTTP 409 se duplicado)
- [ ] Validação: `responsible_user_id` existe e está ativo (HTTP 404 se inválido)
- [ ] `GET /api/stock-locations` retorna lista de locais ativos (com filtro opcional `includeInactive=true`)
- [ ] `GET /api/stock-locations/{id}` retorna detalhes de um local com informações do responsável
- [ ] `PUT /api/stock-locations/{id}` edita local (nome, código, endereço, responsável)
- [ ] Validação na edição: não permite mudar `code` se local já tem estoque alocado
- [ ] `DELETE /api/stock-locations/{id}` marca como inativo (soft delete)
- [ ] Validação na deleção: impede deletar se houver estoque alocado (HTTP 409 com mensagem clara)

### AC3: Validação de Estoque Alocado
- [ ] Endpoint `GET /api/stock-locations/{id}/has-stock` retorna boolean indicando se há estoque
- [ ] Query verifica se existe registro em `stock` com `stock_location_id = {id}` e `quantity_available > 0`
- [ ] Soft delete valida chamando este endpoint antes de marcar como inativo
- [ ] Mensagem de erro amigável: "Não é possível inativar local com estoque alocado. Transfira o estoque antes."

### AC4: Frontend - Stock Location Form
- [ ] Component Angular `StockLocationFormComponent` com formulário reativo
- [ ] Campos: nome (obrigatório), código (obrigatório, alfanumérico, max 20 chars), endereço (textarea), responsável (dropdown de usuários ativos)
- [ ] Validação client-side: código alfanumérico, código único (AsyncValidator)
- [ ] Mensagem de erro para código duplicado: "Código {code} já está em uso"
- [ ] Botões: Salvar, Cancelar

### AC5: Frontend - Stock Location List
- [ ] Component Angular `StockLocationListComponent` exibe tabela de locais
- [ ] Colunas: Código, Nome, Endereço (truncado), Responsável, Status, Ações
- [ ] Filtro: checkbox "Mostrar inativos"
- [ ] Ações: Editar (ícone lápis), Deletar (ícone lixeira com confirmação)
- [ ] Modal de confirmação de deleção: "Tem certeza que deseja inativar {name}?"
- [ ] Se deletar local com estoque: exibe erro "Não é possível inativar local com estoque alocado"

---

## Tasks & Subtasks

### Task 1: Criar Migration de stock_locations
- [ ] Criar migration `V036__create_stock_locations_table.sql`
- [ ] Definir estrutura com constraints e FKs
- [ ] Criar índices
- [ ] Testar migration: `mvn flyway:migrate`

### Task 2: Criar Entidade StockLocation
- [ ] Criar `StockLocation.java` em `catalog.domain`
- [ ] Anotar com `@Entity`, `@Table(name = "stock_locations")`
- [ ] Relacionamento `@ManyToOne` com User (responsible)
- [ ] Validação: `code` alfanumérico (regex: `^[A-Z0-9-]+$`)

### Task 3: Criar StockLocationRepository
- [ ] Criar `StockLocationRepository` extends `CrudRepository`
- [ ] Método `findByTenantIdAndAtivoTrue(UUID tenantId)`
- [ ] Método `findByTenantIdAndCodeIgnoreCase(UUID tenantId, String code)` para validação
- [ ] Método `existsByIdAndStockQuantityAvailableGreaterThanZero()` usando query customizada

### Task 4: Implementar StockLocationService
- [ ] Criar `StockLocationService` com métodos CRUD
- [ ] Método `createStockLocation()`: valida código único, valida responsável
- [ ] Método `updateStockLocation()`: valida código único se alterado
- [ ] Método `deleteStockLocation()`: valida se não tem estoque alocado, então soft delete
- [ ] Método `hasAllocatedStock()`: verifica se local tem estoque
- [ ] Validação: lançar `DuplicateCodeException` se código duplicado
- [ ] Validação: lançar `StockLocationHasStockException` se tentar deletar local com estoque

### Task 5: Criar StockLocationController
- [ ] Criar `StockLocationController` em `catalog.adapter.in.web`
- [ ] Endpoints REST completos (POST, GET list, GET by id, PUT, DELETE)
- [ ] Endpoint `GET /{id}/has-stock` para validação
- [ ] DTOs: `CreateStockLocationRequest`, `UpdateStockLocationRequest`, `StockLocationResponse`
- [ ] Tratamento de erros: 409 para código duplicado ou local com estoque

### Task 6: Frontend - StockLocationFormComponent
- [ ] Criar component em `features/catalog/stock-location-form`
- [ ] FormBuilder com validações (Validators.required, Validators.pattern)
- [ ] AsyncValidator para código único
- [ ] Dropdown de usuários (busca usuários ativos via UserService)
- [ ] Textarea para endereço
- [ ] Mensagens de erro customizadas

### Task 7: Frontend - StockLocationListComponent
- [ ] Criar component em `features/catalog/stock-location-list`
- [ ] Tabela com MatTable ou PrimeNG Table
- [ ] Checkbox "Mostrar inativos" com refiltragem
- [ ] Modal de confirmação para deletar
- [ ] Tratamento de erro: exibe toast/snackbar se tentar deletar local com estoque
- [ ] Integração com StockLocationService (HTTP calls)

### Task 8: Testes
- [ ] Teste de integração: criar local de estoque
- [ ] Teste: código duplicado retorna 409
- [ ] Teste: responsável inválido retorna 404
- [ ] Teste: soft delete marca ativo=false
- [ ] Teste: impede deletar local com estoque alocado
- [ ] Teste: permite deletar local sem estoque
- [ ] Teste frontend: validação de formulário

---

## Definition of Done (DoD)

- [ ] Migration executada com sucesso
- [ ] Entidade StockLocation e Repository criados
- [ ] StockLocationService implementado com validações
- [ ] StockLocationController com todos os endpoints
- [ ] Frontend StockLocationFormComponent funcional
- [ ] Frontend StockLocationListComponent com filtros
- [ ] Validação de estoque alocado funciona
- [ ] Testes de integração passando
- [ ] Code review aprovado
- [ ] Documentação técnica atualizada

---

## Dependencies & Blockers

**Depende de:**
- Story 1.5 (RBAC) - Relacionamento com usuarios (responsável)

**Bloqueia:**
- Story 2.7 (Multi-Warehouse Stock) - Estoque depende de stock_location_id
- Story 2.9 (Stock Transfer) - Transferências entre locais

---

## Technical Notes

**Validação de Estoque Alocado (Query):**
```java
public boolean hasAllocatedStock(UUID stockLocationId) {
    Long count = entityManager.createQuery(
        "SELECT COUNT(s) FROM Stock s " +
        "WHERE s.stockLocationId = :locationId " +
        "AND s.quantityAvailable > 0",
        Long.class)
        .setParameter("locationId", stockLocationId)
        .getSingleResult();

    return count > 0;
}

public void deleteStockLocation(UUID id) {
    StockLocation location = stockLocationRepository.findById(id)
        .orElseThrow(() -> new NotFoundException("Local não encontrado"));

    if (hasAllocatedStock(id)) {
        throw new StockLocationHasStockException(
            "Não é possível inativar local com estoque alocado. Transfira o estoque antes.");
    }

    location.setAtivo(false);
    stockLocationRepository.save(location);
}
```

**AsyncValidator para Código Único (Frontend):**
```typescript
codeUniqueValidator(): AsyncValidatorFn {
  return (control: AbstractControl): Observable<ValidationErrors | null> => {
    if (!control.value) return of(null);

    return this.stockLocationService.checkCodeExists(control.value).pipe(
      debounceTime(500),
      map(exists => exists ? { codeExists: true } : null),
      catchError(() => of(null))
    );
  };
}
```

**Exemplo de Request/Response:**
```json
// POST /api/stock-locations
{
  "name": "Loja São Paulo - Shopping Morumbi",
  "code": "LOJA-SP-MOR",
  "address": "Av. Roque Petroni Jr., 1089 - Morumbi, São Paulo - SP",
  "responsibleUserId": "uuid-usuario"
}

// Response 201 Created
{
  "id": "uuid-location",
  "name": "Loja São Paulo - Shopping Morumbi",
  "code": "LOJA-SP-MOR",
  "address": "Av. Roque Petroni Jr., 1089 - Morumbi, São Paulo - SP",
  "responsible": {
    "id": "uuid-usuario",
    "name": "João Silva",
    "email": "joao.silva@loja.com"
  },
  "ativo": true,
  "dataCriacao": "2025-11-21T10:30:00Z"
}

// GET /api/stock-locations
{
  "locations": [
    {
      "id": "uuid-1",
      "name": "Loja São Paulo",
      "code": "LOJA-SP-MOR",
      "ativo": true
    },
    {
      "id": "uuid-2",
      "name": "Centro de Distribuição RJ",
      "code": "CD-RJ",
      "ativo": true
    }
  ]
}
```

**Migration SQL:**
```sql
CREATE TABLE stock_locations (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID NOT NULL REFERENCES tenants(id),
    name VARCHAR(100) NOT NULL,
    code VARCHAR(20) NOT NULL,
    address TEXT,
    responsible_user_id UUID REFERENCES usuarios(id) ON DELETE RESTRICT,
    ativo BOOLEAN DEFAULT true,
    data_criacao TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    data_atualizacao TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uq_stock_locations_tenant_code UNIQUE (tenant_id, code)
);

CREATE INDEX idx_stock_locations_tenant_id ON stock_locations(tenant_id);
CREATE INDEX idx_stock_locations_responsible ON stock_locations(responsible_user_id);
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
