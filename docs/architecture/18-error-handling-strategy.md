# 18. Error Handling Strategy

## 18.1. Error Classification

### **Exception Hierarchy**

```
RuntimeException
├── DomainException (4xx errors)
│   ├── ProdutoNaoEncontradoException
│   ├── EstoqueInsuficienteException
│   └── VendaJaCanceladaException
├── BusinessException (4xx errors)
│   ├── SKUDuplicadoException
│   ├── PrecoInvalidoException
│   └── ClienteInativoException
├── InfrastructureException (5xx errors)
│   ├── DatabaseConnectionException
│   └── ExternalAPIException
└── SecurityException (401/403)
    ├── UnauthorizedException
    └── ForbiddenException
```

---

## 18.2. Backend Error Handling

### **18.2.1. Domain Exceptions**

```java
// Base domain exception
public abstract class DomainException extends RuntimeException {
    protected DomainException(String message) {
        super(message);
    }

    protected DomainException(String message, Throwable cause) {
        super(message, cause);
    }
}

// Specific domain exceptions
public class ProdutoNaoEncontradoException extends DomainException {
    public ProdutoNaoEncontradoException(ProdutoId id) {
        super("Produto não encontrado: " + id.value());
    }
}

public class EstoqueInsuficienteException extends DomainException {
    private final ProdutoId produtoId;
    private final int disponivel;
    private final int solicitado;

    public EstoqueInsuficienteException(ProdutoId produtoId, int disponivel, int solicitado) {
        super(String.format(
            "Estoque insuficiente para produto %s. Disponível: %d, Solicitado: %d",
            produtoId.value(), disponivel, solicitado
        ));
        this.produtoId = produtoId;
        this.disponivel = disponivel;
        this.solicitado = solicitado;
    }

    // Getters for additional context
    public ProdutoId getProdutoId() { return produtoId; }
    public int getDisponivel() { return disponivel; }
    public int getSolicitado() { return solicitado; }
}
```

### **18.2.2. Business Exceptions**

```java
public abstract class BusinessException extends RuntimeException {
    protected BusinessException(String message) {
        super(message);
    }
}

public class SKUDuplicadoException extends BusinessException {
    public SKUDuplicadoException(String sku) {
        super("SKU já existe: " + sku);
    }
}

public class PrecoInvalidoException extends BusinessException {
    public PrecoInvalidoException(String mensagem) {
        super(mensagem);
    }
}
```

### **18.2.3. Global Exception Handler**

```java
@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(DomainException.class)
    ResponseEntity<ErrorResponse> handleDomain(DomainException ex, WebRequest request) {
        log.warn("Domain exception: {}", ex.getMessage());

        var error = new ErrorResponse(
            "DOMAIN_ERROR",
            ex.getMessage(),
            Instant.now(),
            getPath(request)
        );

        return ResponseEntity
            .status(HttpStatus.BAD_REQUEST)
            .body(error);
    }

    @ExceptionHandler(BusinessException.class)
    ResponseEntity<ErrorResponse> handleBusiness(BusinessException ex, WebRequest request) {
        log.warn("Business exception: {}", ex.getMessage());

        var error = new ErrorResponse(
            "BUSINESS_ERROR",
            ex.getMessage(),
            Instant.now(),
            getPath(request)
        );

        return ResponseEntity
            .status(HttpStatus.BAD_REQUEST)
            .body(error);
    }

    @ExceptionHandler(EstoqueInsuficienteException.class)
    ResponseEntity<ErrorResponse> handleEstoqueInsuficiente(
        EstoqueInsuficienteException ex,
        WebRequest request
    ) {
        log.warn("Estoque insuficiente: produto={}, disponivel={}, solicitado={}",
            ex.getProdutoId(), ex.getDisponivel(), ex.getSolicitado());

        var error = new ErrorResponse(
            "ESTOQUE_INSUFICIENTE",
            ex.getMessage(),
            Instant.now(),
            getPath(request),
            Map.of(
                "produtoId", ex.getProdutoId().value().toString(),
                "disponivel", ex.getDisponivel(),
                "solicitado", ex.getSolicitado()
            )
        );

        return ResponseEntity
            .status(HttpStatus.CONFLICT)
            .body(error);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    ResponseEntity<ErrorResponse> handleValidation(
        MethodArgumentNotValidException ex,
        WebRequest request
    ) {
        var errors = ex.getBindingResult()
            .getFieldErrors()
            .stream()
            .collect(Collectors.toMap(
                FieldError::getField,
                FieldError::getDefaultMessage,
                (a, b) -> a + "; " + b
            ));

        log.warn("Validation error: {}", errors);

        var error = new ErrorResponse(
            "VALIDATION_ERROR",
            "Erro de validação",
            Instant.now(),
            getPath(request),
            errors
        );

        return ResponseEntity
            .status(HttpStatus.BAD_REQUEST)
            .body(error);
    }

    @ExceptionHandler(AccessDeniedException.class)
    ResponseEntity<ErrorResponse> handleAccessDenied(
        AccessDeniedException ex,
        WebRequest request
    ) {
        log.warn("Access denied: {}", ex.getMessage());

        var error = new ErrorResponse(
            "ACCESS_DENIED",
            "Acesso negado",
            Instant.now(),
            getPath(request)
        );

        return ResponseEntity
            .status(HttpStatus.FORBIDDEN)
            .body(error);
    }

    @ExceptionHandler(Exception.class)
    ResponseEntity<ErrorResponse> handleGeneric(Exception ex, WebRequest request) {
        log.error("Unexpected error", ex);

        var error = new ErrorResponse(
            "INTERNAL_ERROR",
            "Erro interno do servidor",
            Instant.now(),
            getPath(request)
        );

        return ResponseEntity
            .status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body(error);
    }

    private String getPath(WebRequest request) {
        return ((ServletWebRequest) request).getRequest().getRequestURI();
    }
}
```

### **18.2.4. Error Response DTO**

```java
public record ErrorResponse(
    String code,
    String message,
    Instant timestamp,
    String path,
    Map<String, Object> details
) {
    public ErrorResponse(String code, String message, Instant timestamp, String path) {
        this(code, message, timestamp, path, Collections.emptyMap());
    }
}

// Example JSON response:
// {
//   "code": "ESTOQUE_INSUFICIENTE",
//   "message": "Estoque insuficiente para produto abc-123. Disponível: 5, Solicitado: 10",
//   "timestamp": "2025-01-27T20:00:00Z",
//   "path": "/api/vendas",
//   "details": {
//     "produtoId": "abc-123",
//     "disponivel": 5,
//     "solicitado": 10
//   }
// }
```

---

## 18.3. External API Error Handling

### **18.3.1. Mercado Livre API**

```java
@Service
@Slf4j
public class MercadoLivreClient {
    private final RestTemplate restTemplate;

    @Retryable(
        value = {RestClientException.class, ResourceAccessException.class},
        maxAttempts = 3,
        backoff = @Backoff(delay = 1000, multiplier = 2)
    )
    public MercadoLivreProduct syncProduct(Produto produto) {
        try {
            var request = mapToMLProduct(produto);
            var response = restTemplate.postForObject(
                mlApiUrl + "/items",
                request,
                MercadoLivreProduct.class
            );
            return response;

        } catch (HttpClientErrorException.BadRequest e) {
            log.error("ML Bad Request: {}", e.getResponseBodyAsString());
            throw new MLValidationException("Dados inválidos: " + e.getResponseBodyAsString());

        } catch (HttpClientErrorException.Unauthorized e) {
            log.error("ML Unauthorized - token expirado");
            throw new MLAuthenticationException("Token expirado, necessário renovar");

        } catch (HttpClientErrorException.TooManyRequests e) {
            log.warn("ML Rate limit exceeded");
            throw new MLRateLimitException("Rate limit excedido, aguardar");

        } catch (HttpServerErrorException e) {
            log.error("ML Server error: status={}", e.getStatusCode());
            throw new MLServerException("Erro no servidor do Mercado Livre");

        } catch (ResourceAccessException e) {
            log.error("ML Connection timeout", e);
            throw new MLConnectionException("Timeout ao conectar com Mercado Livre");
        }
    }

    @Recover
    public MercadoLivreProduct recover(RestClientException e, Produto produto) {
        log.error("Failed to sync product after retries: produtoId={}", produto.getId(), e);
        // Marcar produto como falha de sincronização
        eventPublisher.publish(new MLSyncFailedEvent(produto.getId(), e.getMessage()));
        throw new MLSyncException("Falha ao sincronizar após 3 tentativas", e);
    }
}
```

### **18.3.2. Focus NFe API (com Retry Queue)**

```java
@Service
@Slf4j
public class FocusNFeService {
    private final FocusNFeClient client;
    private final RDelayedQueue<NFCeRetryTask> retryQueue;

    public void emitirNFCe(Venda venda) {
        try {
            var response = client.emitir(venda);

            if (response.getStatus().equals("autorizado")) {
                nfceRepository.updateStatus(venda.getId(), StatusNFCe.AUTORIZADA);
                eventPublisher.publish(new NFCeAutorizadaEvent(venda.getId(), response.getChave()));

            } else if (response.getStatus().equals("processando")) {
                log.info("NFCe em processamento, aguardando webhook");
                nfceRepository.updateStatus(venda.getId(), StatusNFCe.EMITINDO);

            } else {
                throw new NFCeRejectedException(response.getMensagem());
            }

        } catch (FocusNFeTimeoutException e) {
            log.warn("Timeout ao emitir NFCe, agendando retry: vendaId={}", venda.getId());
            agendarRetry(venda.getId(), 1);

        } catch (FocusNFeSefazOfflineException e) {
            log.warn("SEFAZ offline, agendando retry: vendaId={}", venda.getId());
            agendarRetry(venda.getId(), 1);

        } catch (FocusNFeRejectedException e) {
            log.error("NFCe rejeitada: vendaId={}, motivo={}", venda.getId(), e.getMessage());
            nfceRepository.updateStatus(venda.getId(), StatusNFCe.REJEITADA);
            nfceRepository.updateErro(venda.getId(), e.getMessage());
            throw e;
        }
    }

    private void agendarRetry(VendaId vendaId, int tentativa) {
        if (tentativa > 10) {
            log.error("NFCe falhou após 10 tentativas: vendaId={}", vendaId);
            nfceRepository.updateStatus(vendaId, StatusNFCe.FALHA_PERMANENTE);
            eventPublisher.publish(new NFCeFalhaPermanenteEvent(vendaId));
            return;
        }

        var delay = calcularBackoff(tentativa);
        var task = new NFCeRetryTask(vendaId, tentativa);
        retryQueue.offer(task, delay.toSeconds(), TimeUnit.SECONDS);

        nfceRepository.updateProximaTentativa(vendaId, Instant.now().plus(delay));
        log.info("NFCe retry agendado: vendaId={}, tentativa={}, delay={}",
            vendaId, tentativa, delay);
    }

    private Duration calcularBackoff(int tentativa) {
        // Exponential backoff: 30s, 1m, 2m, 4m, 8m, 16m, 32m, 1h, 2h, 4h
        return Duration.ofSeconds(30L * (1L << (tentativa - 1)));
    }
}
```

---

## 18.4. Frontend Error Handling

### **18.4.1. HTTP Error Interceptor**

```typescript
// error.interceptor.ts
export const errorInterceptor: HttpInterceptorFn = (req, next) => {
  const router = inject(Router);
  const snackBar = inject(MatSnackBar);

  return next(req).pipe(
    catchError((error: HttpErrorResponse) => {
      let errorMessage = 'Erro desconhecido';

      if (error.error instanceof ErrorEvent) {
        // Client-side error
        errorMessage = `Erro: ${error.error.message}`;
      } else {
        // Server-side error
        switch (error.status) {
          case 400:
            errorMessage = error.error?.message || 'Requisição inválida';
            if (error.error?.details) {
              errorMessage += '\n' + formatValidationErrors(error.error.details);
            }
            break;

          case 401:
            errorMessage = 'Sessão expirada. Faça login novamente.';
            router.navigate(['/login']);
            break;

          case 403:
            errorMessage = 'Acesso negado. Você não tem permissão para esta ação.';
            break;

          case 404:
            errorMessage = 'Recurso não encontrado';
            break;

          case 409:
            errorMessage = error.error?.message || 'Conflito ao processar requisição';
            break;

          case 429:
            errorMessage = 'Muitas requisições. Por favor, aguarde alguns segundos.';
            break;

          case 500:
            errorMessage = 'Erro interno do servidor. Tente novamente mais tarde.';
            break;

          case 503:
            errorMessage = 'Serviço temporariamente indisponível. Tente novamente.';
            break;

          default:
            errorMessage = error.error?.message || `Erro ${error.status}`;
        }
      }

      // Log to console
      console.error('HTTP Error:', {
        url: error.url,
        status: error.status,
        message: errorMessage,
        error: error.error
      });

      // Show user notification
      snackBar.open(errorMessage, 'Fechar', {
        duration: 5000,
        panelClass: ['error-snackbar']
      });

      return throwError(() => error);
    })
  );
};

function formatValidationErrors(details: Record<string, any>): string {
  return Object.entries(details)
    .map(([field, message]) => `${field}: ${message}`)
    .join('\n');
}
```

### **18.4.2. Global Error Handler**

```typescript
// global-error-handler.ts
@Injectable()
export class GlobalErrorHandler implements ErrorHandler {
  private snackBar = inject(MatSnackBar);

  handleError(error: Error): void {
    console.error('Global error:', error);

    let message = 'Ocorreu um erro inesperado';

    if (error instanceof TypeError) {
      message = 'Erro de tipo de dados';
    } else if (error instanceof ReferenceError) {
      message = 'Erro de referência';
    } else if (error.message) {
      message = error.message;
    }

    this.snackBar.open(message, 'Fechar', {
      duration: 5000,
      panelClass: ['error-snackbar']
    });

    // Em produção, enviar para serviço de logging (Application Insights)
    if (environment.production) {
      this.logToAppInsights(error);
    }
  }

  private logToAppInsights(error: Error): void {
    // TODO: Integrar com Application Insights
  }
}

// Registrar em app.config.ts
export const appConfig: ApplicationConfig = {
  providers: [
    { provide: ErrorHandler, useClass: GlobalErrorHandler },
    // ...
  ]
};
```

### **18.4.3. Service Error Handling**

```typescript
// produto.service.ts
@Injectable({
  providedIn: 'root'
})
export class ProdutoService {
  private http = inject(HttpClient);
  private apiUrl = environment.apiUrl;

  listar(): Observable<Produto[]> {
    return this.http.get<Produto[]>(`${this.apiUrl}/produtos`).pipe(
      retry({
        count: 2,
        delay: 1000,
        resetOnSuccess: true
      }),
      catchError((error: HttpErrorResponse) => {
        console.error('Erro ao listar produtos:', error);
        return throwError(() => new Error('Falha ao carregar produtos'));
      })
    );
  }

  criar(produto: CriarProdutoRequest): Observable<Produto> {
    return this.http.post<Produto>(`${this.apiUrl}/produtos`, produto).pipe(
      catchError((error: HttpErrorResponse) => {
        if (error.status === 409) {
          return throwError(() => new Error('SKU já existe'));
        }
        return throwError(() => new Error('Falha ao criar produto'));
      })
    );
  }

  deletar(id: string): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/produtos/${id}`).pipe(
      catchError((error: HttpErrorResponse) => {
        if (error.status === 409) {
          return throwError(() => new Error(
            'Não é possível deletar produto com movimentações'
          ));
        }
        return throwError(() => new Error('Falha ao deletar produto'));
      })
    );
  }
}
```

---

## 18.5. Error Logging

### **18.5.1. Structured Logging (Backend)**

```java
@Slf4j
@Service
public class VendaService {
    public void finalizarVenda(VendaId vendaId, FormaPagamento formaPagamento) {
        try {
            var venda = vendaRepository.findById(vendaId)
                .orElseThrow(() -> new VendaNaoEncontradaException(vendaId));

            venda.finalizar(formaPagamento);
            vendaRepository.save(venda);

            log.info("Venda finalizada: vendaId={}, total={}, formaPagamento={}",
                vendaId,
                venda.getTotal(),
                formaPagamento
            );

            eventPublisher.publish(new VendaFinalizadaEvent(vendaId));

        } catch (EstoqueInsuficienteException e) {
            log.warn("Estoque insuficiente ao finalizar venda: vendaId={}, produtoId={}, disponivel={}, solicitado={}",
                vendaId,
                e.getProdutoId(),
                e.getDisponivel(),
                e.getSolicitado()
            );
            throw e;

        } catch (Exception e) {
            log.error("Erro inesperado ao finalizar venda: vendaId={}", vendaId, e);
            throw new VendaProcessamentoException("Erro ao finalizar venda", e);
        }
    }
}
```

### **18.5.2. Log Levels**

| Level | Usage | Example |
|-------|-------|---------|
| **ERROR** | Erros graves que impedem operação | `log.error("Falha ao conectar DB", e)` |
| **WARN** | Situações recuperáveis mas indesejadas | `log.warn("Estoque baixo: {}", produtoId)` |
| **INFO** | Eventos de negócio importantes | `log.info("Venda finalizada: {}", vendaId)` |
| **DEBUG** | Informações de debug | `log.debug("Query executed: {}", sql)` |
| **TRACE** | Detalhes muito granulares | `log.trace("Entering method: {}", method)` |

### **18.5.3. Log Context (MDC)**

```java
@Component
public class LoggingFilter extends OncePerRequestFilter {
    @Override
    protected void doFilterInternal(
        HttpServletRequest request,
        HttpServletResponse response,
        FilterChain filterChain
    ) throws ServletException, IOException {
        try {
            MDC.put("requestId", UUID.randomUUID().toString());
            MDC.put("tenantId", TenantContext.getTenant().toString());
            MDC.put("userId", getCurrentUserId());

            filterChain.doFilter(request, response);

        } finally {
            MDC.clear();
        }
    }
}

// Logs incluirão automaticamente: [requestId=xxx tenantId=yyy userId=zzz]
```

---

## 18.6. User-Friendly Error Messages

### **Error Message Guidelines**

```
❌ BAD:
"NullPointerException at line 42"
"Error: 500"
"Produto não pode ser null"

✅ GOOD:
"Não foi possível encontrar o produto solicitado"
"Estoque insuficiente. Disponível: 5 unidades"
"O preço deve ser maior que zero"
```

### **Domain-Specific Messages**

```java
public class ProdutoMessages {
    public static String produtoNaoEncontrado(ProdutoId id) {
        return "O produto solicitado não foi encontrado. Verifique se o código está correto.";
    }

    public static String estoqueInsuficiente(int disponivel, int solicitado) {
        return String.format(
            "Não há estoque suficiente. Disponível: %d unidades, Solicitado: %d unidades",
            disponivel,
            solicitado
        );
    }

    public static String skuDuplicado(String sku) {
        return String.format(
            "O SKU '%s' já está em uso. Por favor, escolha outro código.",
            sku
        );
    }
}
```

---

## 18.7. Error Monitoring

### **Application Insights Integration**

```java
@Component
@Slf4j
public class ApplicationInsightsLogger {
    private final TelemetryClient telemetryClient;

    public void trackException(Exception exception, Map<String, String> properties) {
        var telemetry = new ExceptionTelemetry(exception);
        telemetry.getProperties().putAll(properties);
        telemetryClient.trackException(telemetry);
    }

    public void trackError(String errorType, String message, Map<String, String> properties) {
        var event = new EventTelemetry(errorType);
        event.getProperties().put("message", message);
        event.getProperties().putAll(properties);
        telemetryClient.trackEvent(event);
    }
}
```

### **Error Alerting Rules**

- **Critical**: Error rate > 1% → Alerta imediato
- **High**: Error rate > 0.5% → Alerta em 5 minutos
- **Medium**: Error rate > 0.1% → Alerta em 15 minutos
- **Low**: Error rate < 0.1% → Log apenas

---

## 18.8. Circuit Breaker Pattern

```java
@Service
public class MercadoLivreClient {
    private final CircuitBreaker circuitBreaker;

    public MercadoLivreClient() {
        var config = CircuitBreakerConfig.custom()
            .failureRateThreshold(50) // 50% failure rate
            .waitDurationInOpenState(Duration.ofSeconds(60))
            .slidingWindowSize(10)
            .build();

        this.circuitBreaker = CircuitBreaker.of("mercadolivre", config);
    }

    public MercadoLivreProduct syncProduct(Produto produto) {
        return circuitBreaker.executeSupplier(() -> {
            // ML API call
            return restTemplate.postForObject(mlApiUrl, produto, MercadoLivreProduct.class);
        });
    }
}
```
