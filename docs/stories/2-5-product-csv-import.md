# Story 2.5: Product CSV Import

**Epic**: 2 - Product Catalog & Inventory Foundation
**Story ID**: 2.5
**Status**: approved
**Created**: 2025-11-21
**Updated**: 2025-11-21

---

## User Story

Como **gerente de loja**,
Eu quero **importar produtos via CSV/Excel com preview e validação**,
Para que **eu possa cadastrar centenas de produtos rapidamente sem entrada manual**.

---

## Context & Business Value

Esta story implementa importação em massa de produtos via arquivo CSV, com preview de validações e confirmação em duas etapas (preview → confirm). Suporta produtos simples, variantes e compostos, com validações completas antes da persistência.

**Valor de Negócio:**
- **Produtividade**: Importar 1000 produtos em < 30s vs horas de entrada manual
- **Migração**: Facilita migração de sistemas legados (export CSV → import)
- **Atualização em Massa**: Permite atualizar preços/custos de centenas de produtos
- **Redução de Erros**: Preview com validações previne importar dados inválidos
- **Rastreabilidade**: Histórico de importações permite auditoria

**Contexto Arquitetural:**
- **Two-Phase Commit**: Preview valida sem persistir, confirm persiste apenas válidos
- **Batch Processing**: Usa Spring Batch para processar grandes volumes (> 1000 linhas)
- **Async Processing**: Importações longas rodam em background com webhook/polling de status
- **Idempotência**: Mesmo arquivo pode ser reimportado (SKU único previne duplicação)

---

## Acceptance Criteria

### AC1: Tabela import_logs Criada
- [ ] Migration cria tabela `import_logs` no schema tenant:
  - `id` (UUID, PK)
  - `tenant_id` (UUID, FK para tenants)
  - `user_id` (UUID, FK para usuarios)
  - `file_name` (VARCHAR(255))
  - `total_rows` (INTEGER)
  - `success_rows` (INTEGER)
  - `error_rows` (INTEGER)
  - `status` (ENUM: PREVIEW, PROCESSING, COMPLETED, FAILED)
  - `error_details` (JSONB, armazena erros de cada linha)
  - `created_at` (TIMESTAMP)
- [ ] Índice: `idx_import_logs_tenant_user`

### AC2: Endpoint de Preview
- [ ] `POST /api/products/import/preview` recebe arquivo CSV (multipart/form-data)
- [ ] Validações por linha:
  - Campos obrigatórios preenchidos (name, sku, price, cost, type)
  - SKU não duplicado no arquivo e não existe no banco (por tenant)
  - category_id válido (se fornecido)
  - price e cost numéricos e >= 0
  - type válido (SIMPLE, VARIANT, COMPOSITE)
  - Para variantes: colunas de atributos presentes (ex: Cor, Tamanho)
- [ ] Response retorna:
  - Total de linhas processadas
  - Primeiras 10 linhas (preview)
  - Lista de erros (linha + mensagem): `[{row: 15, error: "SKU duplicado"}]`
  - Flag `hasErrors` (boolean)
- [ ] Não persiste dados (apenas validação)
- [ ] Processa até 10.000 linhas em < 5s (NFR17)

### AC3: Endpoint de Confirmação
- [ ] `POST /api/products/import/confirm` recebe mesmo arquivo + confirmação
- [ ] Persiste apenas linhas válidas (skip linhas com erro)
- [ ] Cria produtos em batch (Spring Batch ou JPA batch insert)
- [ ] Para produtos simples: cria direto na tabela `products`
- [ ] Para variantes: cria produto pai + atributos + variantes
- [ ] Para compostos: cria produto + componentes (requer planilha auxiliar ou coluna components_json)
- [ ] Response retorna:
  - ID do import_log criado
  - Total de produtos criados
  - Total de erros
  - Lista de produtos criados (IDs e SKUs)
- [ ] Importação de 1000 produtos completa em < 30s (NFR17)

### AC4: Endpoint de Template CSV
- [ ] `GET /api/products/import/template?type={SIMPLE|VARIANT|COMPOSITE}` retorna template CSV
- [ ] Template para SIMPLE: `name,sku,barcode,description,category_id,price,cost,unit,controls_inventory`
- [ ] Template para VARIANT: adiciona colunas `attribute_1,attribute_2,attribute_3,variant_value_1,variant_value_2,variant_value_3`
- [ ] Template para COMPOSITE: adiciona coluna `components_json` (ex: `[{"sku":"COMP-001","qty":2}]`)
- [ ] Primeira linha = cabeçalho, segunda linha = exemplo preenchido
- [ ] Content-Type: `text/csv`
- [ ] Content-Disposition: `attachment; filename="template-products-simple.csv"`

### AC5: Suporte a Produtos Variantes em CSV
- [ ] Formato de variantes: uma linha = uma variante
- [ ] Linhas com mesmo SKU base + atributos diferentes = variantes do mesmo produto pai
- [ ] Exemplo:
  ```csv
  name,sku,attribute_1,value_1,attribute_2,value_2,price,cost
  Camiseta Polo,CAM-POLO-AZUL-P,Cor,Azul,Tamanho,P,79.90,40.00
  Camiseta Polo,CAM-POLO-AZUL-M,Cor,Azul,Tamanho,M,79.90,40.00
  ```
- [ ] Validação: agrupa por nome + atributos e cria produto pai com variantes

### AC6: Suporte a Produtos Compostos em CSV
- [ ] Coluna `components_json` contém JSON array: `[{"sku":"COMP-001","qty":2},{"sku":"COMP-002","qty":1}]`
- [ ] Validação: todos os componentes (SKUs) existem no banco
- [ ] Validação: componentes não podem ser COMPOSITE (evita recursão)
- [ ] Cria produto + relacionamentos em `product_components`

### AC7: Frontend - Import Component
- [ ] Component Angular `ProductImportComponent` permite upload de arquivo
- [ ] Botões: "Escolher Arquivo", "Download Template" (dropdown: Simple/Variant/Composite)
- [ ] Upload chama endpoint `/preview` e exibe resultados
- [ ] Tabela de preview com primeiras 10 linhas e erros highlight em vermelho
- [ ] Card de resumo: "X linhas válidas, Y linhas com erro"
- [ ] Se hasErrors=false: botão "Confirmar Importação" habilitado
- [ ] Se hasErrors=true: botão desabilitado com tooltip "Corrija os erros antes de importar"
- [ ] Progress bar durante confirmação (polling de status via endpoint `/import-logs/{id}`)

### AC8: Endpoint de Status de Importação
- [ ] `GET /api/products/import-logs/{id}` retorna status de importação
- [ ] Response: `{status: "PROCESSING", progress: 45, totalRows: 1000, processedRows: 450}`
- [ ] Frontend usa polling a cada 2s para atualizar progress bar
- [ ] Ao completar: exibe mensagem "Importação concluída: X produtos criados"

---

## Tasks & Subtasks

### Task 1: Criar Migration de import_logs
- [ ] Criar migration `V035__create_import_logs_table.sql`
- [ ] Definir estrutura com JSONB para error_details
- [ ] Criar índices
- [ ] Testar migration: `mvn flyway:migrate`

### Task 2: Criar Entidade ImportLog
- [ ] Criar `ImportLog.java` em `catalog.domain`
- [ ] Enum `ImportStatus` (PREVIEW, PROCESSING, COMPLETED, FAILED)
- [ ] Campo `errorDetails` tipo Map<Integer, String> (linha → erro)
- [ ] Criar `ImportLogRepository`

### Task 3: Implementar CsvParserService
- [ ] Criar `CsvParserService` com lib Apache Commons CSV ou OpenCSV
- [ ] Método `parseFile(MultipartFile file)` retorna lista de `ProductCsvRow`
- [ ] DTO `ProductCsvRow` com campos do CSV
- [ ] Método `validateRow()` aplica validações e retorna lista de erros
- [ ] Método `groupVariants()` agrupa linhas de variantes por produto pai

### Task 4: Implementar ProductImportService
- [ ] Criar `ProductImportService` com método `preview()`
- [ ] Método `preview()`: parseia CSV, valida linhas, retorna preview sem persistir
- [ ] Método `confirmImport()`: parseia, valida, persiste válidos
- [ ] Usa `@Async` para importações > 100 linhas (roda em background)
- [ ] Cria registro em `import_logs` para rastreabilidade
- [ ] Método `getImportStatus()` retorna status atual de importação

### Task 5: Implementar Batch Processing (Spring Batch)
- [ ] Configurar Spring Batch para importações > 1000 linhas
- [ ] ItemReader: lê CSV linha por linha
- [ ] ItemProcessor: valida e transforma em Product
- [ ] ItemWriter: persiste em batch (chunk size = 100)
- [ ] JobExecutionListener atualiza `import_logs` com progresso

### Task 6: Criar ProductImportController
- [ ] Criar `ProductImportController` em `catalog.adapter.in.web`
- [ ] Endpoint `POST /import/preview` (multipart/form-data)
- [ ] Endpoint `POST /import/confirm` (multipart/form-data)
- [ ] Endpoint `GET /import/template?type={type}` retorna CSV
- [ ] Endpoint `GET /import-logs/{id}` retorna status
- [ ] DTOs: `ImportPreviewResponse`, `ImportConfirmResponse`

### Task 7: Gerar Templates CSV
- [ ] Método `generateTemplate()` cria CSV com cabeçalho + linha de exemplo
- [ ] Templates separados para SIMPLE, VARIANT, COMPOSITE
- [ ] Exemplo preenchido com dados fictícios para guiar usuário

### Task 8: Frontend - ProductImportComponent
- [ ] Criar component em `features/catalog/product-import`
- [ ] Upload de arquivo (input type=file accept=".csv")
- [ ] Botões de download de templates (3 botões ou dropdown)
- [ ] Tabela de preview com highlight de erros (PrimeNG Table)
- [ ] Card de resumo de validação
- [ ] Progress bar com polling de status
- [ ] Service: `ProductImportService` com métodos HTTP

### Task 9: Testes
- [ ] Teste de integração: importar CSV com 10 produtos simples
- [ ] Teste: CSV com erros (SKU duplicado) retorna preview com erros
- [ ] Teste: confirmação importa apenas linhas válidas (skip erros)
- [ ] Teste: importar variantes agrupa corretamente por produto pai
- [ ] Teste: importar compostos cria componentes
- [ ] Teste de performance: importar 1000 produtos em < 30s

---

## Definition of Done (DoD)

- [ ] Migration executada com sucesso
- [ ] CsvParserService implementado
- [ ] ProductImportService com preview e confirm
- [ ] Spring Batch configurado para grandes volumes
- [ ] ProductImportController com todos os endpoints
- [ ] Templates CSV para SIMPLE, VARIANT, COMPOSITE
- [ ] Frontend ProductImportComponent funcional
- [ ] Preview com validações funciona
- [ ] Importação de 1000 produtos completa em < 30s
- [ ] Testes de integração passando
- [ ] Code review aprovado
- [ ] Documentação técnica atualizada

---

## Dependencies & Blockers

**Depende de:**
- Story 2.2 (Simple Products) - Importa produtos simples
- Story 2.3 (Products with Variants) - Importa variantes
- Story 2.4 (Composite Products) - Importa compostos

**Bloqueia:**
- Nenhuma story diretamente bloqueada

---

## Technical Notes

**Estrutura de CSV para Produtos Simples:**
```csv
name,sku,barcode,description,category_id,price,cost,unit,controls_inventory
Notebook Dell Inspiron 15,DELL-INS-15-001,7891234567890,Notebook 15.6 polegadas,550e8400-e29b-41d4-a716-446655440000,3499.90,2800.00,UN,true
Mouse Logitech MX Master,LOG-MOUSE-MX,7891234567891,Mouse sem fio,550e8400-e29b-41d4-a716-446655440001,349.90,200.00,UN,true
```

**Estrutura de CSV para Variantes:**
```csv
name,sku,attribute_1,value_1,attribute_2,value_2,price,cost,category_id
Camiseta Polo,CAM-POLO-AZUL-P,Cor,Azul,Tamanho,P,79.90,40.00,uuid-categoria
Camiseta Polo,CAM-POLO-AZUL-M,Cor,Azul,Tamanho,M,79.90,40.00,uuid-categoria
Camiseta Polo,CAM-POLO-PRETO-P,Cor,Preto,Tamanho,P,79.90,40.00,uuid-categoria
```

**Validação de Preview (Service):**
```java
public ImportPreviewResponse preview(MultipartFile file) {
    List<ProductCsvRow> rows = csvParserService.parseFile(file);
    Map<Integer, List<String>> errors = new HashMap<>();
    Set<String> skusInFile = new HashSet<>();

    for (int i = 0; i < rows.size(); i++) {
        ProductCsvRow row = rows.get(i);
        List<String> rowErrors = new ArrayList<>();

        // Validações
        if (row.getName() == null || row.getName().isBlank()) {
            rowErrors.add("Nome é obrigatório");
        }
        if (row.getSku() == null || row.getSku().isBlank()) {
            rowErrors.add("SKU é obrigatório");
        } else {
            // SKU duplicado no arquivo
            if (skusInFile.contains(row.getSku())) {
                rowErrors.add("SKU duplicado no arquivo");
            }
            skusInFile.add(row.getSku());

            // SKU já existe no banco
            if (productRepository.existsByTenantIdAndSku(TenantContext.getCurrentTenantId(), row.getSku())) {
                rowErrors.add("SKU já existe no sistema");
            }
        }
        if (row.getPrice() == null || row.getPrice().compareTo(BigDecimal.ZERO) < 0) {
            rowErrors.add("Preço inválido");
        }
        if (row.getCategoryId() != null && !categoryRepository.existsById(row.getCategoryId())) {
            rowErrors.add("Categoria inválida");
        }

        if (!rowErrors.isEmpty()) {
            errors.put(i + 2, rowErrors);  // +2 porque linha 1 = cabeçalho
        }
    }

    return ImportPreviewResponse.builder()
        .totalRows(rows.size())
        .previewRows(rows.subList(0, Math.min(10, rows.size())))
        .errors(errors)
        .hasErrors(!errors.isEmpty())
        .build();
}
```

**Confirmação de Importação (Batch):**
```java
@Async
public UUID confirmImport(MultipartFile file, UUID userId) {
    ImportLog log = new ImportLog();
    log.setFileName(file.getOriginalFilename());
    log.setStatus(ImportStatus.PROCESSING);
    log.setUserId(userId);
    log = importLogRepository.save(log);

    List<ProductCsvRow> rows = csvParserService.parseFile(file);
    List<Product> validProducts = new ArrayList<>();

    for (ProductCsvRow row : rows) {
        if (isValid(row)) {
            Product product = mapToProduct(row);
            validProducts.add(product);
        }
    }

    // Batch insert
    productRepository.saveAll(validProducts);

    log.setStatus(ImportStatus.COMPLETED);
    log.setTotalRows(rows.size());
    log.setSuccessRows(validProducts.size());
    log.setErrorRows(rows.size() - validProducts.size());
    importLogRepository.save(log);

    return log.getId();
}
```

**Template CSV (Controller):**
```java
@GetMapping("/import/template")
public ResponseEntity<Resource> downloadTemplate(@RequestParam ProductType type) {
    String csv = generateTemplate(type);
    ByteArrayResource resource = new ByteArrayResource(csv.getBytes(StandardCharsets.UTF_8));

    return ResponseEntity.ok()
        .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=template-products-" + type.name().toLowerCase() + ".csv")
        .contentType(MediaType.parseMediaType("text/csv"))
        .body(resource);
}

private String generateTemplate(ProductType type) {
    if (type == ProductType.SIMPLE) {
        return "name,sku,barcode,description,category_id,price,cost,unit,controls_inventory\n" +
               "Produto Exemplo,PROD-001,7891234567890,Descrição do produto,uuid-categoria,99.90,50.00,UN,true\n";
    }
    // ... outros templates
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
