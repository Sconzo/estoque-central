# Story 2.2: Simple Products CRUD

**Epic**: 2 - Product Catalog & Inventory Foundation
**Story ID**: 2.2
**Status**: drafted
**Created**: 2025-11-21
**Updated**: 2025-11-21

---

## User Story

Como **gerente de loja**,
Eu quero **cadastrar produtos simples (SKU único sem variações)**,
Para que **eu possa gerenciar catálogo básico de produtos**.

---

## Context & Business Value

Esta story implementa o CRUD completo de produtos simples (tipo SIMPLE), que representam itens com SKU único e sem variações. Produtos simples são a base do catálogo e o tipo mais comum em pequeno/médio varejo.

**Valor de Negócio:**
- **Simplicidade**: Interface intuitiva para cadastro rápido de produtos
- **Validação**: SKU único por tenant previne duplicações e erros de estoque
- **Performance**: Busca rápida (< 500ms) permite agilidade no PDV e gestão
- **Organização**: Integração com categorias facilita navegação e relatórios
- **Controle**: Flag `controls_inventory` permite vender produtos de serviço sem estoque

**Contexto Arquitetural:**
- **Polimorfismo de Tipo**: Campo `type` (SIMPLE, VARIANT, COMPOSITE) permite herança single-table
- **Tenant Isolation**: `tenant_id` garante isolamento de dados entre tenants
- **Soft Delete**: Status inativo permite manter histórico sem poluir catálogo ativo
- **Índices Estratégicos**: Índice composto `(tenant_id, sku)` para validação rápida de unicidade

---

## Acceptance Criteria

### AC1: Tabela products Criada
- [ ] Migration cria tabela `products` no schema tenant com colunas:
  - `id` (UUID, PK)
  - `tenant_id` (UUID, FK para tenants)
  - `type` (ENUM: SIMPLE, VARIANT, COMPOSITE, NOT NULL)
  - `name` (VARCHAR(200), NOT NULL)
  - `sku` (VARCHAR(50), NOT NULL)
  - `barcode` (VARCHAR(50), NULLABLE)
  - `description` (TEXT)
  - `category_id` (UUID, FK para categories, NULLABLE)
  - `price` (DECIMAL(10,2), NOT NULL)
  - `cost` (DECIMAL(10,2), NOT NULL, DEFAULT 0)
  - `unit` (VARCHAR(20), DEFAULT 'UN')
  - `controls_inventory` (BOOLEAN, DEFAULT true)
  - `ativo` (BOOLEAN, DEFAULT true)
  - `data_criacao` (TIMESTAMP)
  - `data_atualizacao` (TIMESTAMP)
- [ ] Constraint UNIQUE `(tenant_id, sku)` garante SKU único por tenant
- [ ] Índices criados: `idx_products_tenant_id`, `idx_products_category_id`, `idx_products_sku`
- [ ] Constraint impede deletar categoria com produtos (`category_id` ON DELETE RESTRICT)

### AC2: Endpoints CRUD de Products
- [ ] `POST /api/products` cria produto simples com validações
- [ ] Validação: SKU único por tenant (HTTP 409 se duplicado)
- [ ] Validação: category_id existe (HTTP 404 se inválida)
- [ ] Validação: price e cost >= 0
- [ ] `GET /api/products` retorna lista paginada (page, size, sort)
- [ ] Filtros suportados: `name`, `sku`, `categoryId`, `status` (ativo/inativo), `controlsInventory`
- [ ] `GET /api/products/{id}` retorna detalhes com informações da categoria (nome, caminho)
- [ ] `PUT /api/products/{id}` edita produto (não permite mudar `type` após criação)
- [ ] `DELETE /api/products/{id}` marca como inativo (soft delete)

### AC3: Busca Rápida (Performance Requirement)
- [ ] Endpoint `GET /api/products/search?q={query}` busca por nome ou SKU
- [ ] Usa índice full-text ou LIKE otimizado
- [ ] Retorna resultados em < 500ms para catálogo de até 10.000 produtos (NFR3)
- [ ] Teste de performance valida tempo de resposta

### AC4: Frontend - Product Form
- [ ] Component Angular `ProductFormComponent` com formulário reativo
- [ ] Campos: nome (obrigatório), SKU (obrigatório), código de barras, descrição, categoria (dropdown com árvore), preço (obrigatório), custo, unidade (dropdown: UN, KG, L, M, CX), controla estoque (checkbox)
- [ ] Validação client-side: campos obrigatórios, preço/custo >= 0, SKU único (chamada assíncrona ao backend)
- [ ] Mensagem de erro amigável para SKU duplicado: "SKU {sku} já está em uso por outro produto"
- [ ] Botões: Salvar, Cancelar

### AC5: Frontend - Product List
- [ ] Component Angular `ProductListComponent` exibe tabela paginada
- [ ] Colunas: SKU, Nome, Categoria, Preço, Custo, Estoque (se controls_inventory=true), Status, Ações
- [ ] Filtros: campo de busca rápida, filtro por categoria (dropdown), filtro por status (ativo/inativo)
- [ ] Ações: Editar (ícone lápis), Deletar (ícone lixeira com confirmação)
- [ ] Paginação com 20 itens por página
- [ ] Campo de busca com debounce de 300ms

---

## Tasks & Subtasks

### Task 1: Criar Migration de Products
- [ ] Criar migration `V029__create_products_table.sql`
- [ ] Definir estrutura completa com type ENUM
- [ ] Criar constraint UNIQUE `(tenant_id, sku)`
- [ ] Criar índices estratégicos
- [ ] Testar migration: `mvn flyway:migrate`

### Task 2: Criar Entidade e Repository
- [ ] Criar `Product.java` em `catalog.domain`
- [ ] Anotar com `@Entity`, `@Table(name = "products")`
- [ ] Enum `ProductType` (SIMPLE, VARIANT, COMPOSITE)
- [ ] Relacionamento `@ManyToOne` com Category
- [ ] Criar `ProductRepository` extends `CrudRepository`
- [ ] Método `findByTenantId(UUID tenantId, Pageable pageable)`
- [ ] Método `findByTenantIdAndSkuIgnoreCase(UUID tenantId, String sku)` para validação
- [ ] Método `findByTenantIdAndNameContainingIgnoreCaseOrSkuContainingIgnoreCase()` para busca

### Task 3: Implementar ProductService
- [ ] Criar `ProductService` com métodos CRUD
- [ ] Método `createProduct()`: valida SKU único, valida category_id
- [ ] Método `updateProduct()`: impede alterar type, valida SKU se alterado
- [ ] Método `deleteProduct()`: soft delete (marca ativo=false)
- [ ] Método `searchProducts()`: busca por nome ou SKU
- [ ] Validação: lançar `DuplicateSkuException` se SKU duplicado
- [ ] Validação: lançar `CategoryNotFoundException` se categoria inválida

### Task 4: Criar ProductController
- [ ] Criar `ProductController` em `catalog.adapter.in.web`
- [ ] Endpoints REST completos (POST, GET list, GET by id, PUT, DELETE)
- [ ] Endpoint `/search` para busca rápida
- [ ] Paginação com `@PageableDefault(size = 20)`
- [ ] DTOs: `CreateProductRequest`, `UpdateProductRequest`, `ProductResponse`
- [ ] Tratamento de erros: 409 para SKU duplicado, 404 para categoria inválida

### Task 5: Frontend - ProductFormComponent
- [ ] Criar component em `features/catalog/product-form`
- [ ] FormBuilder com validações (Validators.required, Validators.min)
- [ ] AsyncValidator para SKU único
- [ ] Dropdown de categorias com árvore (reutilizar CategoryTreeComponent)
- [ ] Dropdown de unidades (UN, KG, L, M, CX)
- [ ] Checkbox "Controla Estoque"
- [ ] Mensagens de erro customizadas

### Task 6: Frontend - ProductListComponent
- [ ] Criar component em `features/catalog/product-list`
- [ ] Tabela com MatTable ou PrimeNG Table
- [ ] Paginação server-side
- [ ] Campo de busca com debounce (RxJS debounceTime)
- [ ] Filtros por categoria e status
- [ ] Modal de confirmação para deletar
- [ ] Integração com ProductService (HTTP calls)

### Task 7: Testes
- [ ] Teste de integração: criar produto simples
- [ ] Teste: SKU duplicado retorna 409
- [ ] Teste: categoria inválida retorna 404
- [ ] Teste: soft delete marca ativo=false
- [ ] Teste de performance: busca retorna em < 500ms (com 1000 produtos)
- [ ] Teste frontend: validação de formulário

---

## Definition of Done (DoD)

- [ ] Migration executada com sucesso
- [ ] Entidade Product e Repository criados
- [ ] ProductService implementado com validações
- [ ] ProductController com todos os endpoints
- [ ] Frontend ProductFormComponent funcional
- [ ] Frontend ProductListComponent com busca e filtros
- [ ] Busca rápida retorna resultados em < 500ms
- [ ] Testes de integração passando (incluindo performance)
- [ ] Code review aprovado
- [ ] Documentação técnica atualizada

---

## Dependencies & Blockers

**Depende de:**
- Story 2.1 (Hierarchical Categories) - Produtos precisam de categoria

**Bloqueia:**
- Story 2.3 (Products with Variants) - Variantes herdam estrutura de Product
- Story 2.4 (Composite Products) - Compostos herdam estrutura de Product
- Story 2.7 (Multi-Warehouse Stock) - Estoque depende de Product

---

## Technical Notes

**Validação de SKU Único (Backend):**
```java
public Product createProduct(CreateProductRequest request) {
    // Valida SKU único
    UUID tenantId = TenantContext.getCurrentTenantId();
    productRepository.findByTenantIdAndSkuIgnoreCase(tenantId, request.getSku())
        .ifPresent(p -> {
            throw new DuplicateSkuException("SKU " + request.getSku() + " já está em uso");
        });

    // Valida categoria existe
    if (request.getCategoryId() != null) {
        categoryRepository.findById(request.getCategoryId())
            .orElseThrow(() -> new CategoryNotFoundException("Categoria inválida"));
    }

    Product product = new Product();
    product.setType(ProductType.SIMPLE);
    product.setSku(request.getSku());
    // ... set other fields
    return productRepository.save(product);
}
```

**AsyncValidator para SKU Único (Frontend):**
```typescript
skuUniqueValidator(): AsyncValidatorFn {
  return (control: AbstractControl): Observable<ValidationErrors | null> => {
    if (!control.value) return of(null);

    return this.productService.checkSkuExists(control.value).pipe(
      debounceTime(500),
      map(exists => exists ? { skuExists: true } : null),
      catchError(() => of(null))
    );
  };
}
```

**Índice para Performance de Busca:**
```sql
-- Índice composto para busca rápida
CREATE INDEX idx_products_search ON products (tenant_id, name, sku);

-- Alternativa: Full-text search (PostgreSQL)
CREATE INDEX idx_products_fulltext ON products USING GIN (to_tsvector('portuguese', name || ' ' || sku));
```

**Exemplo de Request/Response:**
```json
// POST /api/products
{
  "name": "Notebook Dell Inspiron 15",
  "sku": "DELL-INS-15-001",
  "barcode": "7891234567890",
  "description": "Notebook com tela de 15.6 polegadas",
  "categoryId": "uuid-categoria-notebooks",
  "price": 3499.90,
  "cost": 2800.00,
  "unit": "UN",
  "controlsInventory": true
}

// Response 201 Created
{
  "id": "uuid-produto",
  "type": "SIMPLE",
  "name": "Notebook Dell Inspiron 15",
  "sku": "DELL-INS-15-001",
  "category": {
    "id": "uuid-categoria-notebooks",
    "name": "Notebooks",
    "path": "Eletrônicos / Informática / Notebooks"
  },
  "price": 3499.90,
  "cost": 2800.00,
  "ativo": true,
  "dataCriacao": "2025-11-21T10:30:00Z"
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
