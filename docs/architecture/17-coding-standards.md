# 17. Coding Standards

## ⚠️ CRITICAL: Rules for AI Agents

**This section contains MANDATORY rules for AI agents developing code for this project.**

Violating these rules will result in:
- Code review rejection
- Build failures
- Architecture violations

**When in doubt, ASK before implementing.**

---

## 17.1. Architecture Rules (MANDATORY)

### **Rule 1: Hexagonal Architecture Layers**

```
✅ ALLOWED:
domain → (nothing)           # Domain depends on nothing
application → domain         # Application depends on domain
adapter → application        # Adapters depend on application
adapter → domain             # Adapters can depend on domain

❌ FORBIDDEN:
domain → adapter             # Domain NEVER depends on adapters
domain → application         # Domain NEVER depends on application
application → adapter        # Application NEVER depends on adapters
```

**Example:**

```java
// ✅ CORRECT: Domain model with no external dependencies
package com.estoquecentral.produtos.domain.model;

public class Produto extends AggregateRoot {
    private final ProdutoId id;
    private String nome;
    private Money preco;

    // Pure domain logic, no framework annotations
}

// ❌ WRONG: Domain model depending on Spring
package com.estoquecentral.produtos.domain.model;

import org.springframework.stereotype.Component; // ❌ NO!

@Component // ❌ NO!
public class Produto {
    @Autowired // ❌ NO!
    private ProdutoRepository repository;
}
```

### **Rule 2: Spring Modulith Module Boundaries**

```
✅ ALLOWED:
produtos → shared            # All modules can depend on shared
vendas → (outros via events) # Cross-module via events only

❌ FORBIDDEN:
vendas → produtos            # Direct module dependencies
produtos → vendas            # Direct module dependencies
```

**Example:**

```java
// ❌ WRONG: Direct dependency between modules
package com.estoquecentral.vendas;

import com.estoquecentral.produtos.domain.model.Produto; // ❌ NO!

class VendaService {
    void criar(Venda venda) {
        Produto produto = produtoService.buscar(id); // ❌ Direct call!
    }
}

// ✅ CORRECT: Use events for cross-module communication
package com.estoquecentral.vendas;

class VendaService {
    void criar(Venda venda) {
        venda.finalize();
        eventPublisher.publish(new VendaFinalizadaEvent(venda.getId()));
        // Estoque module listens to this event
    }
}

// In estoque module
@EventListener
void on(VendaFinalizadaEvent event) {
    estoqueService.baixar(event.getItens());
}
```

### **Rule 3: Repository Pattern**

```
✅ CORRECT:
- Repository interfaces in domain.port.out
- Repository implementations in adapter.out.persistence
- Return domain objects, NOT entities

❌ WRONG:
- Repository interfaces in infrastructure layer
- Returning JPA entities from repositories
- Using repositories directly in controllers
```

**Example:**

```java
// ✅ CORRECT
package com.estoquecentral.produtos.domain.port.out;

public interface ProdutoRepository {
    void save(Produto produto);
    Optional<Produto> findById(ProdutoId id);
    List<Produto> findAll();
}

// ✅ CORRECT
package com.estoquecentral.produtos.adapter.out.persistence;

@Repository
class ProdutoJdbcRepository implements ProdutoRepository {
    @Override
    public void save(Produto produto) {
        jdbcTemplate.update("INSERT INTO produtos ...", params);
    }
}

// ❌ WRONG
@RestController
class ProdutoController {
    @Autowired
    ProdutoRepository repository; // ❌ Controllers should use use cases, not repositories!

    @GetMapping
    List<Produto> list() {
        return repository.findAll(); // ❌ Bypass application layer!
    }
}

// ✅ CORRECT
@RestController
class ProdutoController {
    @Autowired
    ListarProdutosUseCase listarProdutosUseCase; // ✅ Use case!

    @GetMapping
    List<ProdutoResponse> list() {
        return listarProdutosUseCase.listar()
            .stream()
            .map(this::toResponse)
            .toList();
    }
}
```

---

## 17.2. Java Backend Standards

### **Naming Conventions**

```java
// Classes: PascalCase
public class ProdutoService {}
public class VendaController {}
public record Money(long valor, String moeda) {}

// Methods: camelCase
public void criarProduto() {}
public Optional<Produto> buscarPorId(UUID id) {}

// Variables: camelCase
String nomeProduto;
Money precoUnitario;
List<ItemVenda> itensVenda;

// Constants: UPPER_SNAKE_CASE
public static final int MAX_RETRY_ATTEMPTS = 10;
public static final Duration RETRY_DELAY = Duration.ofSeconds(30);

// Packages: lowercase
com.estoquecentral.produtos.domain.model
com.estoquecentral.vendas.adapter.in.web
```

### **Code Style**

```java
// ✅ PREFER: Records for immutable data
public record CriarProdutoCommand(
    String nome,
    String sku,
    Money preco,
    UUID categoriaId
) {}

// ✅ PREFER: Explicit types over var
List<Produto> produtos = repository.findAll(); // ✅
var produtos = repository.findAll();           // ⚠️ Use sparingly

// ✅ PREFER: Stream API for collections
produtos.stream()
    .filter(Produto::isAtivo)
    .map(Produto::getNome)
    .toList();

// ❌ AVOID: Mutable state in domain objects
public class Produto {
    public String nome; // ❌ Public mutable field

    public void setNome(String nome) { // ❌ Public setter
        this.nome = nome;
    }
}

// ✅ CORRECT: Encapsulation with behavior
public class Produto {
    private String nome;

    public void renomear(String novoNome) {
        if (novoNome == null || novoNome.isBlank()) {
            throw new DomainException("Nome não pode ser vazio");
        }
        this.nome = novoNome;
        registerEvent(new ProdutoRenomeadoEvent(id, novoNome));
    }
}
```

### **Value Objects**

```java
// ✅ ALWAYS use Money for currency values
public record Money(long valor, String moeda) {
    public static Money fromReais(BigDecimal reais) {
        long centavos = reais.multiply(BigDecimal.valueOf(100))
                             .setScale(0, RoundingMode.HALF_UP)
                             .longValue();
        return new Money(centavos, "BRL");
    }

    public Money add(Money other) {
        if (!moeda.equals(other.moeda)) {
            throw new IllegalArgumentException("Cannot add different currencies");
        }
        return new Money(valor + other.valor, moeda);
    }
}

// ❌ NEVER use primitives for money
public class Produto {
    private double preco; // ❌ NEVER use double for money!
    private float custo;  // ❌ NEVER use float for money!
}

// ✅ CORRECT
public class Produto {
    private Money preco;
    private Money custo;
}
```

### **Domain Events**

```java
// ✅ Events should be immutable records
public record ProdutoCriadoEvent(
    ProdutoId produtoId,
    String nome,
    Instant timestamp
) {}

// ✅ Events should be in domain.event package
package com.estoquecentral.produtos.domain.event;

// ✅ Event naming: PastTense + Event
ProdutoCriadoEvent     // ✅
ProdutoAtualizadoEvent // ✅
VendaFinalizadaEvent   // ✅

ProdutoCreateEvent     // ❌ Not past tense
ProdutoCriar           // ❌ Missing "Event" suffix
```

### **Exception Handling**

```java
// ✅ CORRECT: Domain exceptions
package com.estoquecentral.produtos.domain;

public class ProdutoNaoEncontradoException extends DomainException {
    public ProdutoNaoEncontradoException(ProdutoId id) {
        super("Produto não encontrado: " + id.value());
    }
}

// ✅ CORRECT: Business exceptions
public class EstoqueInsuficienteException extends BusinessException {
    public EstoqueInsuficienteException(ProdutoId id, int disponivel, int solicitado) {
        super(String.format(
            "Estoque insuficiente para produto %s. Disponível: %d, Solicitado: %d",
            id.value(), disponivel, solicitado
        ));
    }
}

// ❌ WRONG: Generic exceptions
throw new Exception("Erro"); // ❌ Too generic!
throw new RuntimeException("Produto não encontrado"); // ❌ Use domain exception!
```

---

## 17.3. TypeScript Frontend Standards

### **Naming Conventions**

```typescript
// Components: kebab-case files, PascalCase class
produto-list.component.ts  // ✅ File name
export class ProdutoListComponent {} // ✅ Class name

// Services: kebab-case files, PascalCase class
produto.service.ts  // ✅
export class ProdutoService {} // ✅

// Interfaces: PascalCase
export interface Produto {}
export interface Venda {}

// Variables/Functions: camelCase
const nomeProduto = 'Notebook';
function calcularTotal() {}

// Constants: UPPER_SNAKE_CASE
export const MAX_ITEMS_PER_PAGE = 50;
```

### **Component Structure**

```typescript
// ✅ CORRECT: Standalone component structure
@Component({
  selector: 'app-produto-list',
  standalone: true,
  imports: [
    CommonModule,
    MatTableModule,
    MatButtonModule
  ],
  templateUrl: './produto-list.component.html',
  styleUrl: './produto-list.component.scss'
})
export class ProdutoListComponent implements OnInit {
  // 1. Signals for reactive state
  produtos = signal<Produto[]>([]);
  loading = signal(false);
  erro = signal<string | null>(null);

  // 2. Computed values
  totalProdutos = computed(() => this.produtos().length);

  // 3. Inject dependencies
  private produtoService = inject(ProdutoService);
  private router = inject(Router);

  // 4. Lifecycle
  ngOnInit() {
    this.loadProdutos();
  }

  // 5. Public methods
  loadProdutos() {
    this.loading.set(true);
    this.produtoService.listar().subscribe({
      next: (produtos) => {
        this.produtos.set(produtos);
        this.loading.set(false);
      },
      error: (err) => {
        this.erro.set('Erro ao carregar produtos');
        this.loading.set(false);
      }
    });
  }

  // 6. Event handlers
  onDelete(id: string) {
    if (confirm('Deseja realmente deletar?')) {
      this.produtoService.deletar(id).subscribe(() => {
        this.loadProdutos();
      });
    }
  }
}
```

### **State Management with Signals**

```typescript
// ✅ CORRECT: Signal-based store
@Injectable()
export class PdvStore {
  // Private mutable signals
  private _carrinho = signal<ItemCarrinho[]>([]);
  private _cliente = signal<Cliente | null>(null);

  // Public readonly signals
  readonly carrinho = this._carrinho.asReadonly();
  readonly cliente = this._cliente.asReadonly();

  // Computed values
  readonly totalItens = computed(() =>
    this._carrinho().reduce((sum, item) => sum + item.quantidade, 0)
  );

  readonly subtotal = computed(() =>
    this._carrinho().reduce((sum, item) =>
      sum + (item.preco.valor * item.quantidade), 0
    )
  );

  // Actions
  adicionarItem(produto: Produto, quantidade: number) {
    this._carrinho.update(items => [
      ...items,
      { produto, quantidade, preco: produto.preco }
    ]);
  }

  removerItem(produtoId: string) {
    this._carrinho.update(items =>
      items.filter(i => i.produto.id !== produtoId)
    );
  }

  limpar() {
    this._carrinho.set([]);
    this._cliente.set(null);
  }
}

// ❌ WRONG: BehaviorSubject (old pattern)
export class PdvService {
  private carrinhoSubject = new BehaviorSubject<ItemCarrinho[]>([]); // ❌ Use signals!
  carrinho$ = this.carrinhoSubject.asObservable();
}
```

### **HTTP Services**

```typescript
// ✅ CORRECT: Service structure
@Injectable({
  providedIn: 'root'
})
export class ProdutoService {
  private http = inject(HttpClient);
  private apiUrl = environment.apiUrl;

  listar(): Observable<Produto[]> {
    return this.http.get<Produto[]>(`${this.apiUrl}/produtos`);
  }

  buscar(id: string): Observable<Produto> {
    return this.http.get<Produto>(`${this.apiUrl}/produtos/${id}`);
  }

  criar(produto: CriarProdutoRequest): Observable<Produto> {
    return this.http.post<Produto>(`${this.apiUrl}/produtos`, produto);
  }

  atualizar(id: string, produto: AtualizarProdutoRequest): Observable<Produto> {
    return this.http.put<Produto>(`${this.apiUrl}/produtos/${id}`, produto);
  }

  deletar(id: string): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/produtos/${id}`);
  }
}
```

---

## 17.4. SQL Standards

### **Table Naming**

```sql
-- ✅ CORRECT: snake_case
CREATE TABLE produtos (id UUID PRIMARY KEY);
CREATE TABLE itens_venda (id UUID PRIMARY KEY);
CREATE TABLE movimentacoes_estoque (id UUID PRIMARY KEY);

-- ❌ WRONG: PascalCase or camelCase
CREATE TABLE Produtos (id UUID); -- ❌
CREATE TABLE itensVenda (id UUID); -- ❌
```

### **Column Naming**

```sql
-- ✅ CORRECT: snake_case
created_at TIMESTAMP
updated_at TIMESTAMP
preco_centavos BIGINT
custo_medio_ponderado_centavos BIGINT

-- ❌ WRONG: camelCase
createdAt TIMESTAMP -- ❌
precoCentavos BIGINT -- ❌
```

### **Foreign Keys**

```sql
-- ✅ CORRECT: Naming convention
ALTER TABLE produtos
ADD CONSTRAINT fk_produtos_categoria
FOREIGN KEY (categoria_id) REFERENCES categorias(id);

-- ✅ Index for FK
CREATE INDEX idx_produtos_categoria ON produtos(categoria_id);
```

---

## 17.5. Git Commit Standards

### **Commit Message Format**

```
type(scope): subject

Examples:
feat(produtos): adicionar campo de desconto percentual
fix(vendas): corrigir cálculo de troco no PDV
refactor(estoque): extrair lógica de custo médio para service
test(compras): adicionar testes para recebimento de mercadoria
docs(api): atualizar OpenAPI spec com novos endpoints
chore(deps): atualizar Spring Boot para 3.3.1
perf(produtos): adicionar índice para busca por SKU
style(frontend): aplicar formatação Prettier
```

**Types:**
- `feat`: Nova funcionalidade
- `fix`: Correção de bug
- `refactor`: Refatoração sem mudança de comportamento
- `test`: Adicionar ou corrigir testes
- `docs`: Documentação
- `chore`: Tarefas de manutenção (deps, config)
- `perf`: Melhoria de performance
- `style`: Formatação de código

---

## 17.6. Code Review Checklist

Before submitting code for review:

### **Architecture**
- [ ] Respects hexagonal architecture layers
- [ ] No circular dependencies
- [ ] Domain model free of framework dependencies
- [ ] Cross-module communication via events only

### **Domain Model**
- [ ] Aggregates enforce invariants
- [ ] Value objects are immutable
- [ ] Domain events published for significant state changes
- [ ] Money type used for currency values

### **Testing**
- [ ] Unit tests cover business logic (80%+)
- [ ] Integration tests with Testcontainers
- [ ] No @SpringBootTest for unit tests
- [ ] Mocks used appropriately

### **Security**
- [ ] Input validation via Bean Validation
- [ ] Tenant context enforced
- [ ] No hardcoded secrets
- [ ] Authorization checks (@PreAuthorize)

### **Performance**
- [ ] Appropriate indexes on database
- [ ] No N+1 query problems
- [ ] Caching applied where needed
- [ ] Connection pooling configured

### **Code Quality**
- [ ] No compiler warnings
- [ ] Checkstyle passes
- [ ] SonarQube quality gate passes
- [ ] All tests pass

---

## 17.7. Common Mistakes to Avoid

### **❌ DON'T:**

1. **Use primitives for domain concepts**
   ```java
   double preco; // ❌ Use Money
   String produtoId; // ❌ Use ProdutoId value object
   ```

2. **Put business logic in controllers**
   ```java
   @PostMapping
   ResponseEntity<?> criar(@RequestBody ProdutoRequest req) {
       var produto = new Produto();
       produto.setNome(req.getNome());
       // ... business logic ... ❌ Should be in service!
   }
   ```

3. **Return entities from REST API**
   ```java
   @GetMapping
   Produto buscar(@PathVariable UUID id) { // ❌ Return DTO!
       return repository.findById(id);
   }
   ```

4. **Use @Transactional in domain layer**
   ```java
   package com.estoquecentral.produtos.domain;

   @Transactional // ❌ Domain should not know about transactions!
   public class Produto {}
   ```

5. **Catch and ignore exceptions**
   ```java
   try {
       service.criar(produto);
   } catch (Exception e) {
       // ❌ Never swallow exceptions silently!
   }
   ```

### **✅ DO:**

1. **Use value objects**
   ```java
   public record Money(long valor, String moeda) {}
   public record ProdutoId(UUID value) {}
   ```

2. **Business logic in application/domain layers**
   ```java
   @Service
   class ProdutoService implements CriarProdutoUseCase {
       public ProdutoId criar(CriarProdutoCommand command) {
           var produto = Produto.criar(command.nome(), command.preco());
           repository.save(produto);
           return produto.getId();
       }
   }
   ```

3. **Return DTOs from REST API**
   ```java
   @GetMapping
   ProdutoResponse buscar(@PathVariable UUID id) {
       return service.buscar(id)
           .map(this::toResponse)
           .orElseThrow(() -> new NotFoundException());
   }
   ```

4. **@Transactional in application layer**
   ```java
   @Service
   @Transactional // ✅ Correct place!
   class ProdutoService {}
   ```

5. **Handle exceptions properly**
   ```java
   try {
       service.criar(produto);
   } catch (DomainException e) {
       log.error("Domain error: {}", e.getMessage());
       throw new BadRequestException(e.getMessage());
   }
   ```

---

## 17.8. Batch Import Pattern (CSV/Excel)

### **File Upload Endpoint**

```java
// ✅ CORRECT: Two-phase import (preview + confirm)
@RestController
@RequestMapping("/api/produtos")
class ProdutoController {

    @PostMapping("/import/preview")
    ResponseEntity<ImportPreviewResponse> preview(
        @RequestParam("file") MultipartFile file
    ) {
        // Phase 1: Validate and return preview
        var result = importService.previewImport(file);
        return ResponseEntity.ok(result);
    }

    @PostMapping("/import/confirm")
    ResponseEntity<ImportResultResponse> confirm(
        @RequestParam("file") MultipartFile file
    ) {
        // Phase 2: Persist validated data
        var result = importService.confirmImport(file);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/import/template")
    ResponseEntity<Resource> downloadTemplate() {
        var template = importService.generateTemplate();
        return ResponseEntity.ok()
            .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=produtos-template.csv")
            .contentType(MediaType.parseMediaType("text/csv"))
            .body(template);
    }
}
```

### **Validation Strategy**

```java
// ✅ CORRECT: Collect-all-errors approach
@Service
class ProdutoImportService {

    public ImportPreviewResponse previewImport(MultipartFile file) {
        // 1. Validate file format
        if (!isValidFormat(file)) {
            throw new InvalidFileFormatException("Apenas CSV ou Excel (.xlsx) são suportados");
        }

        // 2. Parse file
        List<ProdutoImportRow> rows = parseFile(file);

        // 3. Validate ALL rows and collect errors
        List<ImportError> errors = new ArrayList<>();
        List<ProdutoImportRow> validRows = new ArrayList<>();

        for (int i = 0; i < rows.size(); i++) {
            ProdutoImportRow row = rows.get(i);
            int rowNumber = i + 2; // +2 porque linha 1 é header e arrays começam em 0

            var rowErrors = validateRow(row, rowNumber);
            if (rowErrors.isEmpty()) {
                validRows.add(row);
            } else {
                errors.addAll(rowErrors);
            }
        }

        // 4. Return preview with first 10 valid rows
        return new ImportPreviewResponse(
            rows.size(),
            validRows.size(),
            errors.size(),
            errors,
            validRows.stream().limit(10).toList()
        );
    }

    private List<ImportError> validateRow(ProdutoImportRow row, int rowNumber) {
        List<ImportError> errors = new ArrayList<>();

        // Campo obrigatório vazio
        if (row.nome() == null || row.nome().isBlank()) {
            errors.add(new ImportError(rowNumber, "nome", "Nome é obrigatório"));
        }

        // SKU duplicado
        if (repository.existsBySku(row.sku())) {
            errors.add(new ImportError(rowNumber, "sku", "SKU já existe: " + row.sku()));
        }

        // Preço inválido
        if (row.preco() != null && row.preco().compareTo(BigDecimal.ZERO) <= 0) {
            errors.add(new ImportError(rowNumber, "preco", "Preço deve ser maior que zero"));
        }

        // Categoria inexistente
        if (row.categoriaId() != null && !categoriaRepository.existsById(row.categoriaId())) {
            errors.add(new ImportError(rowNumber, "categoriaId", "Categoria não encontrada"));
        }

        return errors;
    }
}
```

### **Response DTOs**

```java
// Preview response
public record ImportPreviewResponse(
    int totalRows,
    int validRows,
    int invalidRows,
    List<ImportError> errors,
    List<ProdutoImportRow> preview
) {}

public record ImportError(
    int row,
    String field,
    String message
) {}

// Confirm response
public record ImportResultResponse(
    int imported,
    int skipped,
    List<String> warnings
) {}
```

### **Transaction Strategy**

```java
// ✅ CORRECT: All-or-nothing transaction
@Service
class ProdutoImportService {

    @Transactional
    public ImportResultResponse confirmImport(MultipartFile file) {
        List<ProdutoImportRow> rows = parseFile(file);

        // Re-validate (frontend pode ter modificado arquivo)
        List<ImportError> errors = validateAllRows(rows);
        if (!errors.isEmpty()) {
            throw new ValidationException("Arquivo contém erros", errors);
        }

        // Persist all rows in single transaction
        int imported = 0;
        for (ProdutoImportRow row : rows) {
            var produto = mapToProduto(row);
            repository.save(produto);
            imported++;
        }

        // If any error occurs, transaction will rollback automatically
        return new ImportResultResponse(imported, 0, List.of());
    }
}
```

### **File Parsing (CSV)**

```java
// ✅ CORRECT: Use Apache Commons CSV
@Service
class CsvParser {

    public List<ProdutoImportRow> parseCsv(MultipartFile file) throws IOException {
        try (var reader = new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8)) {
            var csvParser = CSVFormat.DEFAULT
                .builder()
                .setHeader()
                .setSkipHeaderRecord(true)
                .build()
                .parse(reader);

            List<ProdutoImportRow> rows = new ArrayList<>();
            for (CSVRecord record : csvParser) {
                rows.add(new ProdutoImportRow(
                    record.get("nome"),
                    record.get("sku"),
                    record.get("barcode"),
                    new BigDecimal(record.get("preco")),
                    UUID.fromString(record.get("categoriaId"))
                ));
            }
            return rows;
        }
    }
}
```

### **Frontend Implementation**

```typescript
// ✅ CORRECT: Angular component for CSV import
@Component({
  selector: 'app-produto-import',
  template: `
    <input type="file"
           accept=".csv,.xlsx"
           (change)="onFileSelected($event)">

    <button (click)="preview()"
            [disabled]="!selectedFile">
      Preview Importação
    </button>

    @if (previewData()) {
      <div class="preview">
        <h3>Preview: {{ previewData().validRows }} válidos,
            {{ previewData().invalidRows }} inválidos</h3>

        @if (previewData().errors.length > 0) {
          <div class="errors">
            @for (error of previewData().errors; track error.row) {
              <p>Linha {{ error.row }}, Campo {{ error.field }}:
                 {{ error.message }}</p>
            }
          </div>
        }

        <table>
          @for (row of previewData().preview; track row.sku) {
            <tr>
              <td>{{ row.nome }}</td>
              <td>{{ row.sku }}</td>
              <td>{{ row.preco | currency:'BRL' }}</td>
            </tr>
          }
        </table>

        <button (click)="confirm()"
                [disabled]="previewData().invalidRows > 0">
          Confirmar Importação
        </button>
      </div>
    }
  `
})
export class ProdutoImportComponent {
  selectedFile = signal<File | null>(null);
  previewData = signal<ImportPreviewResponse | null>(null);

  private http = inject(HttpClient);

  onFileSelected(event: Event) {
    const input = event.target as HTMLInputElement;
    if (input.files && input.files.length > 0) {
      this.selectedFile.set(input.files[0]);
    }
  }

  preview() {
    const formData = new FormData();
    formData.append('file', this.selectedFile()!);

    this.http.post<ImportPreviewResponse>(
      '/api/produtos/import/preview',
      formData
    ).subscribe(result => {
      this.previewData.set(result);
    });
  }

  confirm() {
    const formData = new FormData();
    formData.append('file', this.selectedFile()!);

    this.http.post<ImportResultResponse>(
      '/api/produtos/import/confirm',
      formData
    ).subscribe(result => {
      alert(`${result.imported} produtos importados com sucesso!`);
    });
  }
}
```

### **Performance Considerations**

```java
// ✅ CORRECT: Batch insert for large imports (1000+ rows)
@Service
class ProdutoImportService {

    @Transactional
    public ImportResultResponse confirmImport(MultipartFile file) {
        List<ProdutoImportRow> rows = parseFile(file);

        // Batch insert every 100 rows
        int batchSize = 100;
        for (int i = 0; i < rows.size(); i += batchSize) {
            List<ProdutoImportRow> batch = rows.subList(
                i,
                Math.min(i + batchSize, rows.size())
            );
            repository.saveAll(batch.stream().map(this::mapToProduto).toList());
            entityManager.flush();
            entityManager.clear(); // Clear persistence context to free memory
        }

        return new ImportResultResponse(rows.size(), 0, List.of());
    }
}
```

### **CSV Template Generation**

```java
// ✅ CORRECT: Generate template with sample data
@Service
class ProdutoImportService {

    public Resource generateTemplate() {
        var headers = List.of("nome", "sku", "barcode", "preco", "categoriaId");
        var sampleRow = List.of("Notebook Dell XPS", "DELL-001", "7891234567890", "3500.00", "uuid-aqui");

        StringBuilder csv = new StringBuilder();
        csv.append(String.join(",", headers)).append("\n");
        csv.append(String.join(",", sampleRow)).append("\n");

        return new ByteArrayResource(csv.toString().getBytes(StandardCharsets.UTF_8));
    }
}
```

---

## 17.9. IDE Configuration

### **IntelliJ IDEA**

```xml
<!-- .editorconfig -->
root = true

[*]
charset = utf-8
indent_style = space
indent_size = 4
end_of_line = lf
trim_trailing_whitespace = true
insert_final_newline = true

[*.java]
indent_size = 4

[*.{ts,js,json,html,css,scss}]
indent_size = 2

[*.yml]
indent_size = 2
```

### **VS Code (TypeScript/Angular)**

```json
// .vscode/settings.json
{
  "editor.formatOnSave": true,
  "editor.defaultFormatter": "esbenp.prettier-vscode",
  "typescript.preferences.importModuleSpecifier": "relative",
  "[typescript]": {
    "editor.defaultFormatter": "esbenp.prettier-vscode"
  }
}
```

---

## 17.9. AI Agent Guidelines

**When developing code as an AI agent:**

1. **ALWAYS read architecture documentation first**
2. **ASK before creating new architectural patterns**
3. **FOLLOW existing code structure**
4. **WRITE tests for all new code**
5. **USE value objects for domain concepts**
6. **RESPECT module boundaries**
7. **NEVER bypass application layer**
8. **ALWAYS validate input**
9. **LOG important business events**
10. **DOCUMENT complex business logic**

**If you're unsure about any decision, STOP and ASK the user for clarification.**
