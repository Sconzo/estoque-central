# 11. Backend Architecture

## 11.1. Hexagonal Architecture (Ports & Adapters)

```
produtos/
├── domain/                         # Core domain (sem dependências)
│   ├── model/
│   │   └── Produto.java            # Aggregate root
│   ├── port/
│   │   ├── in/                     # Inbound ports (use cases)
│   │   │   └── CriarProdutoUseCase.java
│   │   └── out/                    # Outbound ports
│   │       └── ProdutoRepository.java
│   └── event/
│       └── ProdutoCriadoEvent.java
├── application/                    # Application services
│   └── service/
│       └── ProdutoService.java     # Implementa use cases
└── adapter/                        # Adapters
    ├── in/                         # Inbound adapters
    │   └── web/
    │       └── ProdutoController.java
    └── out/                        # Outbound adapters
        └── persistence/
            └── ProdutoJdbcRepository.java
```

## 11.2. Domain Model Example

```java
// Aggregate Root
public class Produto extends AggregateRoot {
    private final ProdutoId id;
    private String nome;
    private Money preco;

    public static Produto criar(String nome, Money preco) {
        var produto = new Produto(ProdutoId.generate(), nome, preco);
        produto.registerEvent(new ProdutoCriadoEvent(produto.id));
        return produto;
    }

    public void atualizarPreco(Money novoPreco) {
        if (novoPreco.isNegative()) {
            throw new DomainException("Preço não pode ser negativo");
        }
        this.preco = novoPreco;
        registerEvent(new ProdutoPrecoAtualizadoEvent(id, novoPreco));
    }

    // Value Object
    public record ProdutoId(UUID value) {
        public static ProdutoId generate() {
            return new ProdutoId(UUID.randomUUID());
        }
    }
}

// Value Object Money
public record Money(long valor, String moeda) {
    public static Money fromReais(BigDecimal reais) {
        long centavos = reais.multiply(BigDecimal.valueOf(100))
                             .setScale(0, RoundingMode.HALF_UP)
                             .longValue();
        return new Money(centavos, "BRL");
    }

    public Money add(Money other) {
        return new Money(valor + other.valor, moeda);
    }

    public boolean isNegative() {
        return valor < 0;
    }
}
```

## 11.3. Use Case Port (Interface)

```java
// Inbound port
public interface CriarProdutoUseCase {
    ProdutoId criar(CriarProdutoCommand command);

    record CriarProdutoCommand(
        String nome,
        String sku,
        Money preco,
        UUID categoriaId
    ) {}
}
```

## 11.4. Application Service (Implementation)

```java
@Service
@Transactional
class ProdutoService implements CriarProdutoUseCase {
    private final ProdutoRepository repository;
    private final EventPublisher eventPublisher;

    @Override
    public ProdutoId criar(CriarProdutoCommand command) {
        var produto = Produto.criar(
            command.nome(),
            command.preco()
        );

        repository.save(produto);

        produto.getDomainEvents().forEach(eventPublisher::publish);

        return produto.getId();
    }
}
```

## 11.5. REST Controller (Inbound Adapter)

```java
@RestController
@RequestMapping("/api/produtos")
class ProdutoController {
    private final CriarProdutoUseCase criarProdutoUseCase;
    private final BuscarProdutoUseCase buscarProdutoUseCase;

    @PostMapping
    ResponseEntity<ProdutoResponse> criar(@RequestBody @Valid CriarProdutoRequest request) {
        var command = new CriarProdutoUseCase.CriarProdutoCommand(
            request.nome(),
            request.sku(),
            Money.fromReais(request.preco()),
            request.categoriaId()
        );

        var produtoId = criarProdutoUseCase.criar(command);

        return ResponseEntity.status(HttpStatus.CREATED)
            .body(new ProdutoResponse(produtoId.value()));
    }

    @GetMapping("/{id}")
    ResponseEntity<ProdutoResponse> buscar(@PathVariable UUID id) {
        return buscarProdutoUseCase.buscar(ProdutoId.of(id))
            .map(produto -> ResponseEntity.ok(toResponse(produto)))
            .orElse(ResponseEntity.notFound().build());
    }
}
```

## 11.6. JDBC Repository (Outbound Adapter)

```java
@Repository
class ProdutoJdbcRepository implements ProdutoRepository {
    private final JdbcTemplate jdbcTemplate;

    @Override
    public void save(Produto produto) {
        String sql = """
            INSERT INTO produtos (id, nome, sku, preco_centavos, custo_centavos)
            VALUES (:id, :nome, :sku, :preco, :custo)
            ON CONFLICT (id) DO UPDATE SET
                nome = :nome,
                preco_centavos = :preco
        """;

        var params = Map.of(
            "id", produto.getId().value(),
            "nome", produto.getNome(),
            "sku", produto.getSku(),
            "preco", produto.getPreco().valor(),
            "custo", produto.getCusto().valor()
        );

        jdbcTemplate.update(sql, params);
    }

    @Override
    public Optional<Produto> findById(ProdutoId id) {
        String sql = "SELECT * FROM produtos WHERE id = ?";

        try {
            return Optional.ofNullable(
                jdbcTemplate.queryForObject(sql, this::mapRow, id.value())
            );
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    private Produto mapRow(ResultSet rs, int rowNum) throws SQLException {
        return Produto.reconstruct(
            ProdutoId.of(UUID.fromString(rs.getString("id"))),
            rs.getString("nome"),
            Money.of(rs.getLong("preco_centavos"), "BRL")
        );
    }
}
```

## 11.7. Multi-tenancy Configuration

```java
// TenantContext (ThreadLocal)
public class TenantContext {
    private static final ThreadLocal<UUID> CURRENT_TENANT = new ThreadLocal<>();

    public static void setTenant(UUID tenantId) {
        CURRENT_TENANT.set(tenantId);
    }

    public static UUID getTenant() {
        return CURRENT_TENANT.get();
    }

    public static void clear() {
        CURRENT_TENANT.remove();
    }
}

// TenantFilter
@Component
class TenantFilter extends OncePerRequestFilter {
    private final JwtTokenProvider jwtProvider;

    @Override
    protected void doFilterInternal(HttpServletRequest request, ...) {
        String token = extractToken(request);
        UUID tenantId = jwtProvider.getTenantIdFromToken(token);

        TenantContext.setTenant(tenantId);

        try {
            filterChain.doFilter(request, response);
        } finally {
            TenantContext.clear();
        }
    }
}

// Routing DataSource
@Configuration
class MultiTenantDataSourceConfig {
    @Bean
    DataSource dataSource() {
        return new AbstractRoutingDataSource() {
            @Override
            protected Object determineCurrentLookupKey() {
                UUID tenantId = TenantContext.getTenant();
                return "tenant_" + tenantId;
            }
        };
    }
}
```

## 11.8. Event-Driven Communication

```java
// Publicar evento
@Service
class ProdutoService {
    private final ApplicationEventPublisher eventPublisher;

    public void atualizarProduto(Produto produto) {
        repository.save(produto);
        eventPublisher.publishEvent(
            new ProdutoAtualizadoEvent(produto.getId())
        );
    }
}

// Escutar evento
@Component
class EstoqueEventListener {
    @EventListener
    @Async
    void on(ProdutoCriadoEvent event) {
        // Criar entrada de estoque zerado
        estoqueService.inicializar(event.produtoId());
    }
}
```

## 11.9. Spring Security + OAuth2 + JWT

```java
@Configuration
@EnableWebSecurity
class SecurityConfig {
    @Bean
    SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        return http
            .csrf().disable()
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/api/auth/**").permitAll()
                .requestMatchers("/api/**").authenticated()
            )
            .oauth2Login(oauth2 -> oauth2
                .userInfoEndpoint(userInfo -> userInfo
                    .userService(customOAuth2UserService)
                )
            )
            .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class)
            .build();
    }
}
```
