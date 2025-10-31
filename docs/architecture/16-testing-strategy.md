# 16. Testing Strategy

## 16.1. Testing Pyramid

```
                    E2E
                  (Playwright)
                 /           \
               /               \
             /                   \
           /      Integration      \
         /       (Testcontainers)    \
       /                               \
     /                                   \
   /              Unit Tests              \
  /          (JUnit + Mockito)             \
 /__________________________________________\
           ArchUnit (Architecture)
```

### **Coverage Targets**

| Layer | Target | Tool |
|-------|--------|------|
| **Unit Tests** | 80%+ | JaCoCo |
| **Integration Tests** | 70%+ | JaCoCo |
| **E2E Critical Paths** | 100% | Playwright |
| **Overall Coverage** | 75%+ | JaCoCo |

---

## 16.2. Backend Testing

### **16.2.1. Unit Tests (JUnit 5 + Mockito)**

#### **Domain Model Tests**

```java
@DisplayName("Produto - Domain Model Tests")
class ProdutoTest {

    @Test
    @DisplayName("Deve criar produto simples com sucesso")
    void deveCriarProdutoSimples() {
        // Given
        var nome = "Notebook Dell";
        var preco = Money.fromReais(new BigDecimal("3500.00"));

        // When
        var produto = Produto.criar(nome, "DELL-001", preco, categoriaId);

        // Then
        assertThat(produto.getNome()).isEqualTo(nome);
        assertThat(produto.getPreco()).isEqualTo(preco);
        assertThat(produto.getTipo()).isEqualTo(TipoProduto.SIMPLES);
        assertThat(produto.getDomainEvents())
            .hasSize(1)
            .first()
            .isInstanceOf(ProdutoCriadoEvent.class);
    }

    @Test
    @DisplayName("Deve lançar exceção ao atualizar preço negativo")
    void deveLancarExcecaoPrecoNegativo() {
        // Given
        var produto = Produto.criar("Produto", "SKU-001", Money.fromReais(BigDecimal.TEN), categoriaId);
        var precoNegativo = new Money(-1000L, "BRL");

        // When / Then
        assertThatThrownBy(() -> produto.atualizarPreco(precoNegativo))
            .isInstanceOf(DomainException.class)
            .hasMessage("Preço não pode ser negativo");
    }

    @Nested
    @DisplayName("Produtos Variantes")
    class ProdutosVariantes {
        @Test
        void deveCriarProdutoPaiComVariantes() {
            // Given
            var produtoPai = Produto.criarPai("Camiseta Básica", "CAM-001", categoriaId);

            var varianteP = Produto.criarVarianteFilho(
                "Camiseta P",
                "CAM-001-P",
                produtoPai.getId(),
                List.of(
                    new VarianteAtributo("Tamanho", "P")
                )
            );

            // Then
            assertThat(varianteP.getTipo()).isEqualTo(TipoProduto.VARIANTE_FILHO);
            assertThat(varianteP.getProdutoPaiId()).isEqualTo(produtoPai.getId());
            assertThat(varianteP.getAtributos()).hasSize(1);
        }
    }
}
```

#### **Service Tests (com Mocks)**

```java
@ExtendWith(MockitoExtension.class)
@DisplayName("ProdutoService - Application Service Tests")
class ProdutoServiceTest {

    @Mock
    ProdutoRepository repository;

    @Mock
    EventPublisher eventPublisher;

    @InjectMocks
    ProdutoService service;

    @Test
    @DisplayName("Deve criar produto e publicar evento")
    void deveCriarProdutoEPublicarEvento() {
        // Given
        var command = new CriarProdutoCommand(
            "Notebook",
            "NB-001",
            Money.fromReais(new BigDecimal("3500.00")),
            UUID.randomUUID()
        );

        when(repository.save(any(Produto.class)))
            .thenAnswer(inv -> inv.getArgument(0));

        // When
        var produtoId = service.criar(command);

        // Then
        assertThat(produtoId).isNotNull();
        verify(repository).save(any(Produto.class));
        verify(eventPublisher).publish(any(ProdutoCriadoEvent.class));
    }

    @Test
    @DisplayName("Deve lançar exceção ao criar produto com SKU duplicado")
    void deveLancarExcecaoSkuDuplicado() {
        // Given
        var command = new CriarProdutoCommand(
            "Produto",
            "SKU-DUPLICADO",
            Money.fromReais(BigDecimal.TEN),
            UUID.randomUUID()
        );

        when(repository.existsBySku("SKU-DUPLICADO")).thenReturn(true);

        // When / Then
        assertThatThrownBy(() -> service.criar(command))
            .isInstanceOf(BusinessException.class)
            .hasMessage("SKU já existe");
    }
}
```

### **16.2.2. Integration Tests (Testcontainers)**

```java
@SpringBootTest
@Testcontainers
@DisplayName("ProdutoRepository - Integration Tests")
class ProdutoRepositoryIntegrationTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine")
        .withDatabaseName("testdb")
        .withUsername("test")
        .withPassword("test");

    @Container
    static GenericContainer<?> redis = new GenericContainer<>("redis:7-alpine")
        .withExposedPorts(6379);

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        registry.add("spring.data.redis.host", redis::getHost);
        registry.add("spring.data.redis.port", redis::getFirstMappedPort);
    }

    @Autowired
    ProdutoRepository repository;

    @Autowired
    JdbcTemplate jdbcTemplate;

    @BeforeEach
    void setUp() {
        // Limpar banco antes de cada teste
        jdbcTemplate.execute("DELETE FROM produtos");
    }

    @Test
    @DisplayName("Deve salvar e buscar produto por ID")
    void deveSalvarEBuscarProduto() {
        // Given
        var produto = Produto.criar(
            "Notebook",
            "NB-001",
            Money.fromReais(new BigDecimal("3500.00")),
            CategoriaId.of(UUID.randomUUID())
        );

        // When
        repository.save(produto);
        var encontrado = repository.findById(produto.getId());

        // Then
        assertThat(encontrado)
            .isPresent()
            .get()
            .satisfies(p -> {
                assertThat(p.getNome()).isEqualTo("Notebook");
                assertThat(p.getSku()).isEqualTo("NB-001");
                assertThat(p.getPreco()).isEqualTo(produto.getPreco());
            });
    }

    @Test
    @DisplayName("Deve retornar lista vazia quando não há produtos")
    void deveRetornarListaVazia() {
        // When
        var produtos = repository.findAll();

        // Then
        assertThat(produtos).isEmpty();
    }

    @Test
    @DisplayName("Deve buscar produtos por categoria")
    void deveBuscarProdutosPorCategoria() {
        // Given
        var categoriaId = CategoriaId.of(UUID.randomUUID());

        var produto1 = Produto.criar("Produto 1", "SKU-001", Money.fromReais(BigDecimal.TEN), categoriaId);
        var produto2 = Produto.criar("Produto 2", "SKU-002", Money.fromReais(BigDecimal.TEN), categoriaId);
        var produto3 = Produto.criar("Produto 3", "SKU-003", Money.fromReais(BigDecimal.TEN), CategoriaId.of(UUID.randomUUID()));

        repository.save(produto1);
        repository.save(produto2);
        repository.save(produto3);

        // When
        var produtos = repository.findByCategoria(categoriaId);

        // Then
        assertThat(produtos).hasSize(2);
    }
}
```

### **16.2.3. API Tests (REST Assured)**

```java
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@Testcontainers
@DisplayName("ProdutoController - API Integration Tests")
class ProdutoControllerIntegrationTest {

    @LocalServerPort
    int port;

    @Autowired
    TestRestTemplate restTemplate;

    String jwtToken;

    @BeforeEach
    void setUp() {
        RestAssured.port = port;
        jwtToken = obterTokenAutenticacao();
    }

    @Test
    @DisplayName("Deve criar produto via API")
    void deveCriarProdutoViaAPI() {
        given()
            .contentType(ContentType.JSON)
            .header("Authorization", "Bearer " + jwtToken)
            .header("X-Tenant-ID", TENANT_ID)
            .body("""
                {
                  "nome": "Notebook Dell",
                  "sku": "DELL-001",
                  "preco": 3500.00,
                  "categoriaId": "%s"
                }
                """.formatted(CATEGORIA_ID))
        .when()
            .post("/api/produtos")
        .then()
            .statusCode(201)
            .body("id", notNullValue())
            .body("nome", equalTo("Notebook Dell"))
            .body("sku", equalTo("DELL-001"));
    }

    @Test
    @DisplayName("Deve retornar 401 sem token")
    void deveRetornar401SemToken() {
        given()
            .contentType(ContentType.JSON)
            .body("{}")
        .when()
            .post("/api/produtos")
        .then()
            .statusCode(401);
    }

    @Test
    @DisplayName("Deve retornar 400 com dados inválidos")
    void deveRetornar400DadosInvalidos() {
        given()
            .contentType(ContentType.JSON)
            .header("Authorization", "Bearer " + jwtToken)
            .body("""
                {
                  "nome": "",
                  "sku": "INVALID SKU",
                  "preco": -100
                }
                """)
        .when()
            .post("/api/produtos")
        .then()
            .statusCode(400)
            .body("errors", hasSize(greaterThan(0)));
    }
}
```

### **16.2.4. Architecture Tests (ArchUnit)**

```java
@AnalyzeClasses(packages = "com.estoquecentral")
@DisplayName("Architecture Tests")
class ArchitectureTests {

    @ArchTest
    static final ArchRule domainShouldNotDependOnInfrastructure =
        noClasses()
            .that().resideInAPackage("..domain..")
            .should().dependOnClassesThat().resideInAPackage("..adapter..")
            .because("Domain should not depend on infrastructure");

    @ArchTest
    static final ArchRule repositoriesShouldOnlyBeInOutPackage =
        classes()
            .that().haveSimpleNameEndingWith("Repository")
            .should().resideInAPackage("..port.out..")
            .because("Repositories are outbound ports");

    @ArchTest
    static final ArchRule controllersShouldBeInAdapterInWeb =
        classes()
            .that().haveSimpleNameEndingWith("Controller")
            .should().resideInAPackage("..adapter.in.web..")
            .andShould().beAnnotatedWith(RestController.class)
            .because("Controllers are inbound web adapters");

    @ArchTest
    static final ArchRule servicesShouldBeInApplicationLayer =
        classes()
            .that().haveSimpleNameEndingWith("Service")
            .should().resideInAPackage("..application..")
            .andShould().beAnnotatedWith(Service.class);

    @ArchTest
    static final ArchRule domainEventsShouldBeRecords =
        classes()
            .that().haveSimpleNameEndingWith("Event")
            .and().resideInAPackage("..domain.event..")
            .should().beRecords()
            .because("Domain events should be immutable records");

    @ArchTest
    static final ArchRule noCyclicDependencies =
        slices()
            .matching("com.estoquecentral.(*)..")
            .should().beFreeOfCycles();
}
```

---

## 16.3. Frontend Testing

### **16.3.1. Component Tests (Jasmine + Karma)**

```typescript
// produto-list.component.spec.ts
describe('ProdutoListComponent', () => {
  let component: ProdutoListComponent;
  let fixture: ComponentFixture<ProdutoListComponent>;
  let produtoService: jasmine.SpyObj<ProdutoService>;

  beforeEach(async () => {
    const produtoServiceSpy = jasmine.createSpyObj('ProdutoService', ['listar', 'deletar']);

    await TestBed.configureTestingModule({
      imports: [ProdutoListComponent],
      providers: [
        { provide: ProdutoService, useValue: produtoServiceSpy }
      ]
    }).compileComponents();

    produtoService = TestBed.inject(ProdutoService) as jasmine.SpyObj<ProdutoService>;
    fixture = TestBed.createComponent(ProdutoListComponent);
    component = fixture.componentInstance;
  });

  it('deve criar o componente', () => {
    expect(component).toBeTruthy();
  });

  it('deve carregar produtos ao inicializar', () => {
    // Arrange
    const mockProdutos: Produto[] = [
      { id: '1', nome: 'Produto 1', sku: 'SKU-001', preco: { valor: 1000, moeda: 'BRL' } },
      { id: '2', nome: 'Produto 2', sku: 'SKU-002', preco: { valor: 2000, moeda: 'BRL' } }
    ];
    produtoService.listar.and.returnValue(of(mockProdutos));

    // Act
    fixture.detectChanges();

    // Assert
    expect(component.produtos()).toEqual(mockProdutos);
    expect(produtoService.listar).toHaveBeenCalled();
  });

  it('deve deletar produto ao clicar em deletar', () => {
    // Arrange
    const produtoId = '123';
    produtoService.deletar.and.returnValue(of(void 0));

    // Act
    component.deletar(produtoId);

    // Assert
    expect(produtoService.deletar).toHaveBeenCalledWith(produtoId);
  });

  it('deve exibir mensagem de erro quando falha ao carregar', () => {
    // Arrange
    produtoService.listar.and.returnValue(throwError(() => new Error('Erro ao carregar')));

    // Act
    fixture.detectChanges();

    // Assert
    expect(component.erro()).toBe('Erro ao carregar produtos');
  });
});
```

### **16.3.2. Service Tests**

```typescript
// produto.service.spec.ts
describe('ProdutoService', () => {
  let service: ProdutoService;
  let httpMock: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [HttpClientTestingModule],
      providers: [ProdutoService]
    });

    service = TestBed.inject(ProdutoService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpMock.verify();
  });

  it('deve listar produtos', () => {
    // Arrange
    const mockProdutos: Produto[] = [
      { id: '1', nome: 'Produto 1', sku: 'SKU-001', preco: { valor: 1000, moeda: 'BRL' } }
    ];

    // Act
    service.listar().subscribe(produtos => {
      expect(produtos).toEqual(mockProdutos);
    });

    // Assert
    const req = httpMock.expectOne('/api/produtos');
    expect(req.request.method).toBe('GET');
    req.flush(mockProdutos);
  });

  it('deve criar produto', () => {
    // Arrange
    const novoProduto = {
      nome: 'Novo Produto',
      sku: 'SKU-NEW',
      preco: 1500
    };

    // Act
    service.criar(novoProduto).subscribe(response => {
      expect(response.id).toBeDefined();
    });

    // Assert
    const req = httpMock.expectOne('/api/produtos');
    expect(req.request.method).toBe('POST');
    expect(req.request.body).toEqual(novoProduto);
    req.flush({ id: '123', ...novoProduto });
  });
});
```

### **16.3.3. E2E Tests (Playwright)**

```typescript
// e2e/produtos.spec.ts
import { test, expect } from '@playwright/test';

test.describe('Gestão de Produtos', () => {
  test.beforeEach(async ({ page }) => {
    await page.goto('/login');
    await page.fill('[data-testid="email"]', 'admin@test.com');
    await page.fill('[data-testid="password"]', 'password');
    await page.click('[data-testid="login-button"]');
    await expect(page).toHaveURL('/dashboard');
  });

  test('deve listar produtos', async ({ page }) => {
    await page.goto('/produtos');

    await expect(page.locator('[data-testid="produto-list"]')).toBeVisible();
    await expect(page.locator('[data-testid="produto-item"]')).toHaveCount(10);
  });

  test('deve criar novo produto', async ({ page }) => {
    await page.goto('/produtos');
    await page.click('[data-testid="novo-produto-button"]');

    await page.fill('[data-testid="nome"]', 'Notebook Dell XPS 15');
    await page.fill('[data-testid="sku"]', 'DELL-XPS-15');
    await page.fill('[data-testid="preco"]', '8500.00');
    await page.selectOption('[data-testid="categoria"]', 'Eletrônicos');

    await page.click('[data-testid="salvar-button"]');

    await expect(page.locator('[data-testid="success-message"]')).toContainText('Produto criado com sucesso');
    await expect(page).toHaveURL(/\/produtos\/[a-f0-9-]+/);
  });

  test('deve exibir erros de validação', async ({ page }) => {
    await page.goto('/produtos/novo');

    await page.click('[data-testid="salvar-button"]');

    await expect(page.locator('[data-testid="nome-error"]')).toContainText('Nome é obrigatório');
    await expect(page.locator('[data-testid="sku-error"]')).toContainText('SKU é obrigatório');
    await expect(page.locator('[data-testid="preco-error"]')).toContainText('Preço é obrigatório');
  });

  test('deve buscar produtos', async ({ page }) => {
    await page.goto('/produtos');

    await page.fill('[data-testid="search-input"]', 'Notebook');
    await page.click('[data-testid="search-button"]');

    await expect(page.locator('[data-testid="produto-item"]')).toHaveCount(3);
    await expect(page.locator('[data-testid="produto-item"]').first()).toContainText('Notebook');
  });
});

test.describe('PDV - Venda Completa', () => {
  test('deve realizar venda completa com emissão de NFCe', async ({ page }) => {
    // Login
    await page.goto('/login');
    await page.fill('[data-testid="email"]', 'operador@test.com');
    await page.fill('[data-testid="password"]', 'password');
    await page.click('[data-testid="login-button"]');

    // Ir para PDV
    await page.goto('/pdv');

    // Adicionar produtos ao carrinho
    await page.fill('[data-testid="busca-produto"]', 'CAM-001');
    await page.click('[data-testid="produto-result-0"]');
    await page.fill('[data-testid="quantidade"]', '2');
    await page.click('[data-testid="adicionar-carrinho"]');

    await expect(page.locator('[data-testid="carrinho-item"]')).toHaveCount(1);
    await expect(page.locator('[data-testid="total"]')).toContainText('R$ 59,90');

    // Finalizar venda
    await page.click('[data-testid="finalizar-venda"]');

    // Selecionar forma de pagamento
    await page.click('[data-testid="pagamento-dinheiro"]');
    await page.fill('[data-testid="valor-recebido"]', '100.00');

    await expect(page.locator('[data-testid="troco"]')).toContainText('R$ 40,10');

    // Confirmar
    await page.click('[data-testid="confirmar-pagamento"]');

    // Aguardar NFCe
    await expect(page.locator('[data-testid="nfce-status"]')).toContainText('Emitindo NFCe...');
    await expect(page.locator('[data-testid="nfce-status"]')).toContainText('NFCe Autorizada', { timeout: 10000 });

    await expect(page.locator('[data-testid="chave-nfce"]')).toBeVisible();
  });
});
```

---

## 16.4. Performance Testing

### **Load Testing (k6)**

```javascript
// load-tests/produtos-api.js
import http from 'k6/http';
import { check, sleep } from 'k6';

export const options = {
  stages: [
    { duration: '2m', target: 100 }, // Ramp up
    { duration: '5m', target: 100 }, // Sustain
    { duration: '2m', target: 0 },   // Ramp down
  ],
  thresholds: {
    http_req_duration: ['p(95)<200'],  // 95% < 200ms
    http_req_failed: ['rate<0.01'],    // Error rate < 1%
  },
};

export default function () {
  const token = __ENV.JWT_TOKEN;

  const res = http.get('https://api.estoquecentral.com/produtos', {
    headers: {
      'Authorization': `Bearer ${token}`,
      'X-Tenant-ID': 'tenant-123',
    },
  });

  check(res, {
    'status is 200': (r) => r.status === 200,
    'response time < 200ms': (r) => r.timings.duration < 200,
  });

  sleep(1);
}
```

---

## 16.5. Test Coverage Reports

### **JaCoCo Configuration**

```xml
<plugin>
    <groupId>org.jacoco</groupId>
    <artifactId>jacoco-maven-plugin</artifactId>
    <version>0.8.11</version>
    <executions>
        <execution>
            <goals>
                <goal>prepare-agent</goal>
            </goals>
        </execution>
        <execution>
            <id>report</id>
            <phase>test</phase>
            <goals>
                <goal>report</goal>
            </goals>
        </execution>
        <execution>
            <id>jacoco-check</id>
            <goals>
                <goal>check</goal>
            </goals>
            <configuration>
                <rules>
                    <rule>
                        <element>PACKAGE</element>
                        <limits>
                            <limit>
                                <counter>LINE</counter>
                                <value>COVEREDRATIO</value>
                                <minimum>0.80</minimum>
                            </limit>
                        </limits>
                    </rule>
                </rules>
            </configuration>
        </execution>
    </executions>
</plugin>
```

### **Running All Tests**

```bash
# Backend: All tests + coverage
cd backend
./mvnw verify
open target/site/jacoco/index.html

# Frontend: All tests + coverage
cd frontend
npm test -- --watch=false --code-coverage
open coverage/estoque-central/index.html

# E2E: Playwright
cd frontend
npm run e2e

# Performance: k6
k6 run load-tests/produtos-api.js
```
