# 3. Tech Stack

> **Última verificação de versões**: 2025-01-30 (via WebSearch)
> **Próxima revisão recomendada**: 2025-04-30 (trimestral)

## 3.1. Technology Matrix

| Categoria | Tecnologia | Versão | Propósito | Status |
|-----------|-----------|--------|-----------|--------|
| **Frontend Framework** | Angular | 19+ (LTS) ou 20 | SPA framework com standalone components | ✅ Escolhido |
| **Frontend Language** | TypeScript | 5.8+ | Type-safe JavaScript | ✅ Escolhido |
| **Frontend UI** | Angular Material | 19+ | Componentes UI prontos | ✅ Escolhido |
| **Frontend Styling** | Tailwind CSS | 3.4+ | Utility-first CSS | ✅ Escolhido |
| **Frontend State** | Angular Signals | 19+ | Reactive state management | ✅ Escolhido |
| **Frontend HTTP** | Angular HttpClient | 19+ | HTTP requests com interceptors | ✅ Escolhido |
| **Frontend Forms** | Reactive Forms | 19+ | Form validation e controle | ✅ Escolhido |
| **Frontend Routing** | Angular Router | 19+ | Client-side routing com guards | ✅ Escolhido |
| **Backend Language** | Java | 21 (LTS) | Backend programming language | ✅ Escolhido |
| **Backend Framework** | Spring Boot | 3.5+ | Application framework | ✅ Escolhido |
| **Backend Architecture** | Spring Modulith | 1.1+ | Modular monolith com event-driven | ✅ Escolhido |
| **Backend Data Access** | Spring Data JDBC | 3.5+ | Repository pattern sem ORM magic | ✅ Escolhido |
| **Backend Security** | Spring Security | 6.3+ | Authentication & Authorization | ✅ Escolhido |
| **Backend Validation** | Bean Validation | 3.0+ | Request/response validation | ✅ Escolhido |
| **Database** | PostgreSQL | 17+ ou 18 | Relational database (schema-per-tenant) | ✅ Escolhido |
| **Cache & Queue** | Redis | 8.0+ | Cache + Delayed Queue (Redisson) | ✅ Escolhido |
| **Redis Client** | Redisson | 3.25+ | Java Redis client com DelayedQueue | ✅ Escolhido |
| **Database Migration** | Flyway | 10+ | Schema versioning | ✅ Escolhido |
| **API Specification** | OpenAPI | 3.0 | API contract-first design | ✅ Escolhido |
| **API Documentation** | Springdoc OpenAPI | 2.3+ | Auto-generate OpenAPI from code | ✅ Escolhido |
| **Authentication** | Google OAuth 2.0 | - | User authentication | ✅ Escolhido |
| **Authorization** | JWT | - | Stateless token-based auth | ✅ Escolhido |
| **Testing (Backend Unit)** | JUnit 5 | 5.10+ | Unit testing framework | ✅ Escolhido |
| **Testing (Backend Mock)** | Mockito | 5.8+ | Mocking framework | ✅ Escolhido |
| **Testing (Backend Integration)** | Testcontainers | 1.19+ | Integration tests com Docker | ✅ Escolhido |
| **Testing (Backend Architecture)** | ArchUnit | 1.2+ | Enforce architectural rules | ✅ Escolhido |
| **Testing (Frontend Unit)** | Jasmine + Karma | - | Unit testing framework | ✅ Escolhido |
| **Testing (Frontend E2E)** | Playwright | 1.40+ | End-to-end testing | ✅ Escolhido |
| **Build Tool (Backend)** | Maven | 3.9+ | Dependency management e build | ✅ Escolhido |
| **Build Tool (Frontend)** | npm | 10+ | Package manager | ✅ Escolhido |
| **Runtime (Frontend)** | Node.js | 22 LTS ou 24 LTS | JavaScript runtime para build | ✅ Escolhido |
| **Containerization** | Docker | 24+ | Container runtime | ✅ Escolhido |
| **Container Orchestration** | Docker Compose | 2.20+ | Local development | ✅ Escolhido |
| **Cloud Provider** | Azure | - | Cloud infrastructure | ✅ Escolhido |
| **Cloud Compute** | Azure Container Apps | - | Managed container hosting | ✅ Escolhido |
| **Cloud Database** | Azure Database for PostgreSQL | Flexible Server | Managed PostgreSQL | ✅ Escolhido |
| **Cloud Cache** | Azure Cache for Redis | - | Managed Redis | ✅ Escolhido |
| **Cloud Static Hosting** | Azure Static Web Apps | - | Frontend hosting + CDN | ✅ Escolhido |
| **IaC** | Azure Bicep | - | Infrastructure as Code | ✅ Escolhido |
| **CI/CD** | GitHub Actions | - | Automated pipelines | ✅ Escolhido |
| **Monitoring** | Azure Application Insights | - | APM + Logs + Metrics | ✅ Escolhido |
| **Logging** | Logback (SLF4J) | 1.4+ | Structured logging | ✅ Escolhido |
| **External API: Auth** | Google OAuth 2.0 | - | User authentication | ✅ Integração |
| **External API: Marketplace** | Mercado Livre API | v2.0 | Product sync + Order import | ✅ Integração |
| **External API: Fiscal** | Focus NFe API | v2 | NFCe emission | ✅ Integração |

---

## 3.2. Frontend Stack Details

### **Angular 19+ (LTS) Standalone Architecture**

```typescript
// Exemplo de standalone component
import { Component, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { MatTableModule } from '@angular/material/table';
import { ProdutoService } from './produto.service';

@Component({
  selector: 'app-produto-list',
  standalone: true,
  imports: [CommonModule, MatTableModule],
  templateUrl: './produto-list.component.html'
})
export class ProdutoListComponent {
  produtos = signal<Produto[]>([]);

  constructor(private produtoService: ProdutoService) {
    this.loadProdutos();
  }

  loadProdutos() {
    this.produtoService.listar().subscribe(data => {
      this.produtos.set(data);
    });
  }
}
```

### **Key Libraries**

| Biblioteca | Versão | Propósito |
|-----------|--------|-----------|
| `@angular/core` | 19+ | Core framework |
| `@angular/material` | 19+ | UI components (table, dialog, etc) |
| `@angular/forms` | 19+ | Reactive forms |
| `@angular/router` | 19+ | Routing |
| `@angular/common/http` | 19+ | HTTP client |
| `rxjs` | 7.8+ | Reactive programming |
| `tailwindcss` | 3.4+ | Utility CSS |
| `date-fns` | 3.0+ | Date manipulation |
| `@playwright/test` | 1.40+ | E2E testing |

---

## 3.3. Backend Stack Details

### **Spring Boot 3.3+ com Spring Modulith**

```xml
<!-- pom.xml principais dependências -->
<dependencies>
    <!-- Spring Boot Starters -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-web</artifactId>
    </dependency>
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-data-jdbc</artifactId>
    </dependency>
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-security</artifactId>
    </dependency>
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-oauth2-client</artifactId>
    </dependency>
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-validation</artifactId>
    </dependency>

    <!-- Spring Modulith -->
    <dependency>
        <groupId>org.springframework.modulith</groupId>
        <artifactId>spring-modulith-starter-core</artifactId>
    </dependency>
    <dependency>
        <groupId>org.springframework.modulith</groupId>
        <artifactId>spring-modulith-events-jdbc</artifactId>
    </dependency>

    <!-- Database -->
    <dependency>
        <groupId>org.postgresql</groupId>
        <artifactId>postgresql</artifactId>
    </dependency>
    <dependency>
        <groupId>org.flywaydb</groupId>
        <artifactId>flyway-core</artifactId>
    </dependency>

    <!-- Redis -->
    <dependency>
        <groupId>org.redisson</groupId>
        <artifactId>redisson-spring-boot-starter</artifactId>
        <version>3.25.0</version>
    </dependency>

    <!-- OpenAPI -->
    <dependency>
        <groupId>org.springdoc</groupId>
        <artifactId>springdoc-openapi-starter-webmvc-ui</artifactId>
        <version>2.3.0</version>
    </dependency>

    <!-- JWT -->
    <dependency>
        <groupId>io.jsonwebtoken</groupId>
        <artifactId>jjwt-api</artifactId>
        <version>0.12.3</version>
    </dependency>

    <!-- Testing -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-test</artifactId>
        <scope>test</scope>
    </dependency>
    <dependency>
        <groupId>org.testcontainers</groupId>
        <artifactId>postgresql</artifactId>
        <scope>test</scope>
    </dependency>
    <dependency>
        <groupId>com.tngtech.archunit</groupId>
        <artifactId>archunit-junit5</artifactId>
        <version>1.2.1</version>
        <scope>test</scope>
    </dependency>
</dependencies>
```

### **Java 21 Features Utilizadas**

- **Virtual Threads**: Para melhorar throughput (via Project Loom)
- **Pattern Matching for switch**: Código mais limpo em domain logic
- **Record Classes**: Value objects imutáveis (Money, ProdutoId)
- **Sealed Classes**: Hierarchy control (TipoProduto, StatusVenda)

```java
// Exemplo de sealed class
public sealed interface TipoProduto permits Simples, VariantePai, VarianteFilho, Composto {
    String codigo();
}

record Simples() implements TipoProduto {
    @Override
    public String codigo() { return "SIMPLES"; }
}
```

---

## 3.4. Database Stack

### **PostgreSQL 16+ Features**

- **Schema-per-tenant**: Isolamento via `CREATE SCHEMA tenant_{uuid}`
- **Row-level Security**: (não usado, schema isolation é suficiente)
- **JSONB**: Campos flexíveis (ex: metadados de integração ML)
- **Full-text Search**: Busca de produtos (via `tsvector`)
- **Triggers**: Audit trail (created_at, updated_at)

```sql
-- Exemplo de schema multi-tenant
CREATE SCHEMA tenant_123e4567;

CREATE TABLE tenant_123e4567.produtos (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    sku VARCHAR(100) UNIQUE NOT NULL,
    nome VARCHAR(255) NOT NULL,
    preco_centavos BIGINT NOT NULL,
    search_vector TSVECTOR,
    created_at TIMESTAMP DEFAULT NOW(),
    updated_at TIMESTAMP DEFAULT NOW()
);

-- Full-text search index
CREATE INDEX idx_produtos_search ON tenant_123e4567.produtos USING GIN(search_vector);

-- Trigger para atualizar updated_at
CREATE TRIGGER produtos_updated_at
    BEFORE UPDATE ON tenant_123e4567.produtos
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();
```

### **Flyway Migrations**

```
backend/src/main/resources/db/migration/
├── V001__criar_schema_master.sql
├── V002__criar_tabela_tenants.sql
├── V003__criar_tabela_usuarios.sql
├── V004__criar_tabelas_produtos.sql
├── V005__criar_tabelas_estoque.sql
└── V006__criar_tabelas_vendas.sql
```

---

## 3.5. Redis Stack

### **Redisson Features Utilizadas**

| Feature | Propósito | Exemplo |
|---------|-----------|---------|
| **DelayedQueue** | Retry NFCe com backoff exponencial | 10 tentativas com delays crescentes |
| **Cache** | Session storage | JWT blacklist, user sessions |
| **Pub/Sub** | Real-time updates | Notificar frontend de mudanças |
| **Lock** | Distributed locking | Evitar double-processing de pedidos |

```java
// Exemplo de DelayedQueue para retry NFCe
@Service
public class NFCeRetryService {
    private final RDelayedQueue<NFCeRetryTask> retryQueue;

    public void scheduleRetry(UUID vendaId, int attemptNumber) {
        var delay = calculateBackoff(attemptNumber); // 30s, 1m, 2m, 4m, ...
        var task = new NFCeRetryTask(vendaId, attemptNumber + 1);
        retryQueue.offer(task, delay.toSeconds(), TimeUnit.SECONDS);
    }

    private Duration calculateBackoff(int attempt) {
        return Duration.ofSeconds(30L * (1L << attempt)); // Exponential backoff
    }
}
```

---

## 3.6. External APIs Integration

### **Google OAuth 2.0**

```yaml
# application.yml
spring:
  security:
    oauth2:
      client:
        registration:
          google:
            client-id: ${GOOGLE_CLIENT_ID}
            client-secret: ${GOOGLE_CLIENT_SECRET}
            scope: openid,profile,email
            redirect-uri: http://localhost:4200/auth/callback
        provider:
          google:
            authorization-uri: https://accounts.google.com/o/oauth2/v2/auth
            token-uri: https://oauth2.googleapis.com/token
            user-info-uri: https://openidconnect.googleapis.com/v1/userinfo
```

### **Mercado Livre API**

| Endpoint | Propósito | Rate Limit |
|----------|-----------|----------|
| `POST /oauth/token` | Obter access token | - |
| `GET /users/me` | Info do usuário | 10 req/s |
| `POST /items` | Criar anúncio | 5 req/s |
| `PUT /items/:id` | Atualizar anúncio | 5 req/s |
| `GET /orders/search` | Buscar pedidos | 10 req/s |

```java
// Cliente Mercado Livre com retry
@Service
public class MercadoLivreClient {
    @Retryable(
        value = {RestClientException.class},
        maxAttempts = 3,
        backoff = @Backoff(delay = 1000, multiplier = 2)
    )
    public MercadoLivreProduct createProduct(Produto produto) {
        // ... REST call
    }
}
```

### **Focus NFe API**

| Endpoint | Propósito | Ambiente |
|----------|-----------|----------|
| `POST /v2/nfce` | Emitir NFCe | Produção / Homologação |
| `GET /v2/nfce/:ref` | Consultar status | Produção / Homologação |
| `DELETE /v2/nfce/:ref` | Cancelar NFCe | Produção / Homologação |
| `POST /webhooks` | Receber callback | Configurado no painel |

```java
// Cliente Focus NFe com webhook
@Service
public class FocusNFeClient {
    public EmissaoNFCeResponse emitirNFCe(Venda venda) {
        var request = FocusNFeRequest.builder()
            .natureza_operacao("VENDA")
            .cliente(mapCliente(venda.getCliente()))
            .items(mapItens(venda.getItens()))
            .webhook_url("https://api.estoquecentral.com/webhooks/focusnfe")
            .build();

        return restTemplate.postForObject(
            focusNFeUrl + "/v2/nfce",
            request,
            EmissaoNFCeResponse.class
        );
    }
}
```

---

## 3.7. Testing Stack

### **Backend Testing Pyramid**

```
                    E2E
                   (API)
                  /     \
                 /       \
            Integration Tests
           (Testcontainers)
              /         \
             /           \
        Unit Tests      ArchUnit
       (JUnit+Mockito)  (Rules)
```

### **Frontend Testing Pyramid**

```
              E2E
           (Playwright)
            /      \
           /        \
      Component Tests
       (Jasmine+Karma)
```

### **Coverage Goals**

| Tipo | Meta | Ferramenta |
|------|------|-----------|
| **Backend Unit** | 80%+ | JaCoCo |
| **Backend Integration** | 70%+ | JaCoCo |
| **Frontend Unit** | 75%+ | Karma Coverage |
| **E2E Critical Paths** | 100% | Playwright |

---

## 3.8. DevOps Stack

### **CI/CD Pipeline (GitHub Actions)**

```yaml
# .github/workflows/backend-ci.yml
name: Backend CI

on: [push, pull_request]

jobs:
  test:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v4
      - uses: actions/setup-java@v4
        with:
          java-version: '21'
      - name: Run tests
        run: ./mvnw verify
      - name: Upload coverage
        uses: codecov/codecov-action@v3
```

### **Infrastructure as Code**

```bicep
// infrastructure/azure/main.bicep
module containerApp 'modules/container-app.bicep' = {
  name: 'estoque-central-backend'
  params: {
    name: 'estoque-central-api'
    location: location
    image: 'ghcr.io/estoque-central/backend:latest'
    env: [
      {
        name: 'SPRING_DATASOURCE_URL'
        value: postgresConnectionString
      }
    ]
  }
}
```

---

## 3.9. Monitoring & Observability Stack

| Ferramenta | Propósito |
|-----------|-----------|
| **Application Insights** | APM, distributed tracing |
| **Log Analytics** | Centralized logging |
| **Azure Monitor** | Metrics e alertas |
| **Prometheus** (futuro) | Time-series metrics |
| **Grafana** (futuro) | Dashboards customizados |

---

## 3.10. Dependency Version Management

### **Backend (pom.xml)**

```xml
<properties>
    <java.version>21</java.version>
    <spring-boot.version>3.3.0</spring-boot.version>
    <spring-modulith.version>1.1.0</spring-modulith.version>
    <testcontainers.version>1.19.3</testcontainers.version>
</properties>
```

### **Frontend (package.json)**

```json
{
  "dependencies": {
    "@angular/core": "^17.0.0",
    "@angular/material": "^17.0.0",
    "tailwindcss": "^3.4.0",
    "rxjs": "^7.8.0"
  },
  "devDependencies": {
    "@playwright/test": "^1.40.0",
    "jasmine-core": "^5.1.0"
  }
}
```

---

**Próxima seção: Data Models (TypeScript interfaces + conceitual)**
