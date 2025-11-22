# Story 2.3: Products with Variants (Matrix)

**Epic**: 2 - Product Catalog & Inventory Foundation
**Story ID**: 2.3
**Status**: Ready for Review
**Created**: 2025-11-21
**Updated**: 2025-11-21

---

## User Story

Como **gerente de loja**,
Eu quero **produtos com variantes (matriz Cor x Tamanho)**,
Para que **eu possa gerenciar produtos que têm múltiplas combinações de atributos**.

---

## Context & Business Value

Esta story implementa produtos com variantes (tipo VARIANT), permitindo gerenciar produtos com múltiplas combinações de atributos como Cor x Tamanho. Cada variante tem SKU único, código de barras próprio, e preço/custo específico ou herdado do produto pai.

**Valor de Negócio:**
- **Flexibilidade**: Suporta produtos de moda, calçados, acessórios com múltiplos atributos
- **Controle Granular**: Cada variante tem estoque, preço e custo independentes
- **Geração Automática**: Matriz gera automaticamente todas as combinações possíveis
- **Performance**: Limitação de 3 atributos x 100 variantes previne degradação
- **Escalabilidade**: Suporta desde 2 variantes (PP/GG) até 100 (3 atributos com muitos valores)

**Contexto Arquitetural:**
- **Herança Single-Table**: Produto pai (type=VARIANT) + tabelas auxiliares para atributos e variantes
- **JSON Storage**: `combination_json` armazena combinação de valores (ex: {"Cor": "Azul", "Tamanho": "M"})
- **Desnormalização Estratégica**: Variante duplica price/cost para evitar joins em queries de venda
- **Índice Composto**: `(product_id, combination_json)` garante unicidade de combinação

---

## Acceptance Criteria

### AC1: Tabelas de Variantes Criadas
- [ ] Migration cria tabela `product_attributes`:
  - `id` (UUID, PK)
  - `product_id` (UUID, FK para products)
  - `attribute_name` (VARCHAR(50), ex: "Cor", "Tamanho", "Voltagem")
  - `display_order` (INTEGER, para ordenar atributos na UI)
- [ ] Migration cria tabela `product_attribute_values`:
  - `id` (UUID, PK)
  - `attribute_id` (UUID, FK para product_attributes)
  - `value` (VARCHAR(50), ex: "Azul", "M", "110V")
  - `display_order` (INTEGER)
- [ ] Migration cria tabela `product_variants`:
  - `id` (UUID, PK)
  - `product_id` (UUID, FK para products)
  - `sku` (VARCHAR(50), UNIQUE, NOT NULL)
  - `barcode` (VARCHAR(50), NULLABLE)
  - `combination_json` (JSONB, armazena {"Cor": "Azul", "Tamanho": "M"})
  - `price` (DECIMAL(10,2), NULLABLE - se null herda do pai)
  - `cost` (DECIMAL(10,2), NULLABLE - se null herda do pai)
  - `ativo` (BOOLEAN, DEFAULT true)
- [ ] Constraint: máximo 3 atributos por produto (validação em service)
- [ ] Constraint: máximo 100 variantes por produto (validação em service)

### AC2: Endpoints de Criação de Produto com Variantes
- [ ] `POST /api/products` com `type=VARIANT` cria produto pai + define atributos
- [ ] Request inclui array de atributos: `[{"name": "Cor", "values": ["Azul", "Preto"]}, {"name": "Tamanho", "values": ["P", "M", "G"]}]`
- [ ] Validação: máximo 3 atributos
- [ ] Validação: produto pai não pode ter SKU próprio (SKUs são das variantes)
- [ ] Response retorna produto pai com ID e atributos criados

### AC3: Endpoints de Geração de Variantes
- [ ] `POST /api/products/{id}/variants/generate` gera matriz de variantes automaticamente
- [ ] Gera todas as combinações possíveis (produto cartesiano): Cor[Azul, Preto] x Tamanho[P, M, G] = 6 variantes
- [ ] SKU gerado automaticamente: `{produto_pai_sku}-{attr1_value}-{attr2_value}` (ex: "CAM-POLO-AZUL-M")
- [ ] Preço/custo herdam do produto pai (null nas variantes)
- [ ] Validação: máximo 100 variantes
- [ ] Response retorna lista de variantes criadas

### AC4: Endpoints de Gestão de Variantes
- [ ] `POST /api/products/{id}/variants` cria variante manualmente (para casos especiais)
- [ ] `GET /api/products/{id}/variants` retorna todas as variantes de um produto pai
- [ ] `PUT /api/products/{productId}/variants/{variantId}` edita variante (SKU, barcode, price, cost)
- [ ] `DELETE /api/products/{productId}/variants/{variantId}` marca variante como inativa (soft delete)
- [ ] Validação: não permite deletar todas as variantes (produto pai precisa ter pelo menos 1 variante ativa)

### AC5: Estoque por Variante
- [ ] Estoque é controlado por `variant_id`, não pelo produto pai
- [ ] Endpoint `GET /api/stock?productId={id}` retorna estoque agregado de todas as variantes
- [ ] Endpoint `GET /api/stock?variantId={id}` retorna estoque de uma variante específica
- [ ] UI exibe estoque total do produto pai (soma de todas as variantes)

### AC6: Frontend - Variant Matrix Component
- [ ] Component Angular `VariantMatrixComponent` exibe grade visual de variantes
- [ ] UI permite definir atributos (até 3) com valores múltiplos
- [ ] Botão "Gerar Variantes" chama endpoint `/generate` e exibe tabela de resultados
- [ ] Tabela exibe colunas: SKU (editável), Atributo1, Atributo2, Atributo3, Preço (editável), Custo (editável), Estoque, Ações
- [ ] Permite editar SKU, preço, custo inline antes de salvar
- [ ] Permite deletar variantes individualmente (ícone lixeira)

### AC7: Frontend - Variant Display
- [ ] Tela de visualização de produto mostra dropdown de variantes
- [ ] Dropdown exibe combinação de atributos: "Azul / M"
- [ ] Ao selecionar variante, exibe SKU, preço, estoque específico
- [ ] Tela de listagem de produtos mostra badge com número de variantes (ex: "6 variantes")

---

## Tasks & Subtasks

### Task 1: Criar Migrations de Variantes
- [ ] Criar migration `V030__create_product_attributes_table.sql`
- [ ] Criar migration `V031__create_product_attribute_values_table.sql`
- [ ] Criar migration `V032__create_product_variants_table.sql`
- [ ] Criar índices: `idx_variants_product_id`, `idx_variants_sku`
- [ ] Testar migrations: `mvn flyway:migrate`

### Task 2: Criar Entidades
- [ ] Criar `ProductAttribute.java` em `catalog.domain`
- [ ] Criar `ProductAttributeValue.java` em `catalog.domain`
- [ ] Criar `ProductVariant.java` em `catalog.domain`
- [ ] Relacionamentos `@ManyToOne` e `@OneToMany` apropriados
- [ ] Annotation `@Type(JsonBinaryType.class)` para combination_json (Hibernate)

### Task 3: Criar Repositories
- [ ] Criar `ProductAttributeRepository`
- [ ] Criar `ProductAttributeValueRepository`
- [ ] Criar `ProductVariantRepository`
- [ ] Método `findByProductId(UUID productId)`
- [ ] Método `countByProductId(UUID productId)` para validar limite de 100

### Task 4: Implementar VariantService
- [ ] Criar `VariantService` com método `createProductWithVariants()`
- [ ] Método `generateVariants()`: produto cartesiano de atributos
- [ ] Método `generateVariantSku()`: concatena valores de atributos
- [ ] Validação: máximo 3 atributos
- [ ] Validação: máximo 100 variantes
- [ ] Método `updateVariant()`: edita SKU, price, cost
- [ ] Método `deleteVariant()`: soft delete com validação de pelo menos 1 variante ativa

### Task 5: Criar VariantController
- [ ] Criar `VariantController` em `catalog.adapter.in.web`
- [ ] Endpoints: POST create, POST generate, GET list, PUT update, DELETE
- [ ] DTOs: `CreateVariantProductRequest`, `GenerateVariantsRequest`, `VariantResponse`
- [ ] Tratamento de erros: 400 para limite excedido, 404 para produto não encontrado

### Task 6: Frontend - VariantMatrixComponent
- [ ] Criar component em `features/catalog/variant-matrix`
- [ ] FormArray para atributos dinâmicos
- [ ] Chips input para valores de atributos (ex: PrimeNG Chips)
- [ ] Botão "Gerar Variantes" com loading spinner
- [ ] Tabela de variantes geradas com edição inline (MatTable ou PrimeNG)
- [ ] Validação: máximo 3 atributos, máximo 100 variantes (feedback visual)

### Task 7: Frontend - VariantSelectorComponent
- [ ] Criar component reutilizável para seleção de variantes
- [ ] Dropdown com combinações de atributos
- [ ] Exibe SKU, preço, estoque da variante selecionada
- [ ] Output emite variantId selecionado

### Task 8: Testes
- [ ] Teste de integração: criar produto com 2 atributos (6 variantes)
- [ ] Teste: geração automática de matriz
- [ ] Teste: validação de limite de 3 atributos falha
- [ ] Teste: validação de limite de 100 variantes falha
- [ ] Teste: soft delete de variante
- [ ] Teste: impede deletar última variante ativa

---

## Definition of Done (DoD)

- [ ] Migrations executadas com sucesso
- [ ] Entidades ProductAttribute, ProductAttributeValue, ProductVariant criadas
- [ ] VariantService implementado com validações
- [ ] VariantController com todos os endpoints
- [ ] Frontend VariantMatrixComponent funcional
- [ ] Frontend VariantSelectorComponent reutilizável
- [ ] Geração automática de variantes funciona corretamente
- [ ] Testes de integração passando
- [ ] Code review aprovado
- [ ] Documentação técnica atualizada

---

## Dependencies & Blockers

**Depende de:**
- Story 2.2 (Simple Products) - Herda estrutura de Product

**Bloqueia:**
- Story 2.7 (Multi-Warehouse Stock) - Estoque por variante
- Story 2.8 (Stock Movement) - Movimentações por variante

---

## Technical Notes

**Algoritmo de Geração de Variantes (Produto Cartesiano):**
```java
public List<ProductVariant> generateVariants(UUID productId) {
    Product product = productRepository.findById(productId).orElseThrow();
    List<ProductAttribute> attributes = attributeRepository.findByProductId(productId);

    // Pega valores de cada atributo
    List<List<String>> attributeValues = attributes.stream()
        .map(attr -> valueRepository.findByAttributeId(attr.getId()))
        .map(values -> values.stream().map(ProductAttributeValue::getValue).collect(Collectors.toList()))
        .collect(Collectors.toList());

    // Produto cartesiano
    List<Map<String, String>> combinations = cartesianProduct(attributeValues, attributes);

    // Validação: máximo 100 variantes
    if (combinations.size() > 100) {
        throw new TooManyVariantsException("Limite de 100 variantes excedido");
    }

    // Cria variantes
    List<ProductVariant> variants = new ArrayList<>();
    for (Map<String, String> combination : combinations) {
        ProductVariant variant = new ProductVariant();
        variant.setProductId(productId);
        variant.setSku(generateVariantSku(product.getSku(), combination));
        variant.setCombinationJson(combination);
        variants.add(variant);
    }

    return variantRepository.saveAll(variants);
}

private List<Map<String, String>> cartesianProduct(List<List<String>> lists, List<ProductAttribute> attrs) {
    List<Map<String, String>> result = new ArrayList<>();
    cartesianProductHelper(lists, attrs, 0, new HashMap<>(), result);
    return result;
}

private void cartesianProductHelper(List<List<String>> lists, List<ProductAttribute> attrs,
                                     int index, Map<String, String> current,
                                     List<Map<String, String>> result) {
    if (index == lists.size()) {
        result.add(new HashMap<>(current));
        return;
    }
    for (String value : lists.get(index)) {
        current.put(attrs.get(index).getAttributeName(), value);
        cartesianProductHelper(lists, attrs, index + 1, current, result);
        current.remove(attrs.get(index).getAttributeName());
    }
}

private String generateVariantSku(String baseSku, Map<String, String> combination) {
    String suffix = combination.values().stream()
        .map(String::toUpperCase)
        .collect(Collectors.joining("-"));
    return baseSku + "-" + suffix;
}
```

**Estrutura de combination_json (JSONB):**
```json
{
  "Cor": "Azul",
  "Tamanho": "M",
  "Voltagem": "110V"
}
```

**Query de Estoque Agregado:**
```sql
-- Estoque total de todas as variantes de um produto
SELECT
    p.id AS product_id,
    p.name,
    SUM(s.quantity_available) AS total_quantity_available,
    SUM(s.quantity_reserved) AS total_quantity_reserved
FROM products p
JOIN product_variants pv ON pv.product_id = p.id
JOIN stock s ON s.variant_id = pv.id
WHERE p.id = :productId
GROUP BY p.id, p.name;
```

**Exemplo de Request/Response:**
```json
// POST /api/products (type=VARIANT)
{
  "type": "VARIANT",
  "name": "Camiseta Polo",
  "categoryId": "uuid-categoria-camisetas",
  "price": 79.90,
  "cost": 40.00,
  "attributes": [
    {
      "name": "Cor",
      "values": ["Azul", "Preto", "Branco"]
    },
    {
      "name": "Tamanho",
      "values": ["P", "M", "G"]
    }
  ]
}

// POST /api/products/{id}/variants/generate
// Response 201 Created
{
  "productId": "uuid-produto-pai",
  "variantsGenerated": 9,
  "variants": [
    {
      "id": "uuid-var-1",
      "sku": "CAM-POLO-AZUL-P",
      "combination": {"Cor": "Azul", "Tamanho": "P"},
      "price": 79.90,  // herdado do pai
      "cost": 40.00
    },
    // ... 8 outras variantes
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
