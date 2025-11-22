# Story 2.1: Hierarchical Product Categories

**Epic**: 2 - Product Catalog & Inventory Foundation
**Story ID**: 2.1
**Status**: done
**Created**: 2025-11-21
**Updated**: 2025-11-21

---

## User Story

Como **gerente de loja**,
Eu quero **categorias hierárquicas ilimitadas (árvore de categorias)**,
Para que **eu possa organizar produtos em estrutura lógica (ex: Eletrônicos > Informática > Notebooks)**.

---

## Context & Business Value

Esta story implementa um sistema de categorias hierárquicas (árvore) para organizar produtos de forma lógica e escalável. A estrutura permite níveis ilimitados de profundidade usando padrão de self-reference (parent_category_id).

**Valor de Negócio:**
- **Organização**: Estrutura lógica facilita navegação e busca de produtos
- **Escalabilidade**: Suporta centenas de categorias sem degradação de performance
- **Flexibilidade**: Permite reorganização sem afetar produtos (categoria pode mudar de pai)
- **UX**: Breadcrumb exibe caminho completo facilitando localização

**Contexto Arquitetural:**
- **Self-Referencing Table**: `parent_category_id` aponta para própria tabela
- **Cycle Detection**: Validação impede criar ciclos na árvore (categoria não pode ser pai de si mesma)
- **Soft Delete**: Categoria inativa apenas se não tiver produtos associados

---

## Acceptance Criteria

### AC1: Tabela categories Criada
- [ ] Migration cria tabela `categories` no schema tenant com colunas:
  - `id` (UUID, PK)
  - `tenant_id` (UUID, FK para tenants)
  - `name` (VARCHAR(100), NOT NULL)
  - `description` (TEXT)
  - `parent_category_id` (UUID, FK para categories, NULLABLE)
  - `ativo` (BOOLEAN, DEFAULT true)
  - `data_criacao` (TIMESTAMP)
  - `data_atualizacao` (TIMESTAMP)
- [ ] Índices criados: `idx_categories_tenant_id`, `idx_categories_parent_id`
- [ ] Constraint impede deletar categoria com produtos

### AC2: Endpoints CRUD de Categories
- [ ] `POST /api/categories` cria categoria com parent opcional
- [ ] `GET /api/categories` retorna lista plana de todas as categorias
- [ ] `GET /api/categories/tree` retorna árvore hierárquica em JSON
- [ ] `GET /api/categories/{id}` retorna detalhes de uma categoria
- [ ] `PUT /api/categories/{id}` edita categoria (valida ciclos ao mudar parent)
- [ ] `DELETE /api/categories/{id}` marca como inativa (soft delete) se não tiver produtos

### AC3: Validação de Ciclos na Árvore
- [ ] Validação impede criar ciclo: categoria A não pode ser pai de B se B é ancestral de A
- [ ] Endpoint retorna HTTP 400 com mensagem clara ao tentar criar ciclo
- [ ] Teste automatizado valida detecção de ciclos em 3+ níveis

### AC4: Frontend - Árvore de Categorias
- [ ] Component Angular `CategoryTreeComponent` exibe árvore com expand/collapse
- [ ] UI permite adicionar categoria filho (ícone "+" ao lado de cada categoria)
- [ ] UI permite editar categoria (ícone lápis)
- [ ] UI permite deletar categoria (ícone lixeira, desabilitado se tiver produtos)
- [ ] Breadcrumb exibe caminho completo (ex: "Eletrônicos / Informática / Notebooks")

---

## Tasks & Subtasks

### Task 1: Criar Migration de Categories
- [ ] Criar migration `V028__create_categories_table.sql`
- [ ] Definir estrutura com parent_category_id (self-reference)
- [ ] Criar índices e constraints
- [ ] Testar migration: `mvn flyway:migrate`

### Task 2: Criar Entidade e Repository
- [ ] Criar `Category.java` em `catalog.domain`
- [ ] Criar `CategoryRepository` extends `CrudRepository`
- [ ] Método `findByTenantId()`
- [ ] Método `findByParentCategoryId()`

### Task 3: Implementar CategoryService
- [ ] Criar `CategoryService` com método `createCategory()`
- [ ] Implementar validação de ciclos (algoritmo DFS)
- [ ] Método `getCategoryTree()` retorna estrutura hierárquica
- [ ] Método `softDelete()` valida se não tem produtos

### Task 4: Criar CategoryController
- [ ] Criar endpoints CRUD
- [ ] Endpoint `/tree` retorna JSON hierárquico
- [ ] Tratamento de erros (400 para ciclos, 409 para categoria com produtos)

### Task 5: Frontend - CategoryTreeComponent
- [ ] Criar component com tree view (usar lib ou implementar recursivo)
- [ ] Implementar expand/collapse de nós
- [ ] CRUD inline (adicionar filho, editar, deletar)
- [ ] Breadcrumb component reutilizável

### Task 6: Testes
- [ ] Teste de integração: criação de árvore 3 níveis
- [ ] Teste: detecção de ciclo
- [ ] Teste: soft delete com produtos associados falha

---

## Definition of Done (DoD)

- [ ] Migration executada com sucesso
- [ ] Entidade Category e Repository criados
- [ ] CategoryService implementado com validação de ciclos
- [ ] CategoryController com todos os endpoints
- [ ] Frontend exibe árvore com expand/collapse
- [ ] Breadcrumb funciona corretamente
- [ ] Testes de integração passando
- [ ] Code review aprovado
- [ ] Documentação técnica atualizada

---

## Dependencies & Blockers

**Depende de:**
- Story 1.3 (Multi-tenancy) - Categorias são tenant-specific

**Bloqueia:**
- Story 2.2 (Simple Products) - Produtos precisam de categoria

---

## Technical Notes

**Algoritmo de Detecção de Ciclos:**
```java
// DFS para verificar se new_parent é descendente de category
boolean isCyclicUpdate(UUID categoryId, UUID newParentId) {
    Set<UUID> visited = new HashSet<>();
    return hasCycleDFS(newParentId, categoryId, visited);
}

boolean hasCycleDFS(UUID current, UUID target, Set<UUID> visited) {
    if (current.equals(target)) return true;
    if (visited.contains(current)) return false;
    visited.add(current);

    Category cat = findById(current);
    if (cat.getParentCategoryId() != null) {
        return hasCycleDFS(cat.getParentCategoryId(), target, visited);
    }
    return false;
}
```

**Estrutura JSON de Árvore:**
```json
{
  "id": "uuid1",
  "name": "Eletrônicos",
  "children": [
    {
      "id": "uuid2",
      "name": "Informática",
      "children": [
        {
          "id": "uuid3",
          "name": "Notebooks",
          "children": []
        }
      ]
    }
  ]
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
