# 12. Unified Project Structure

## 12.1. Complete Monorepo Structure

```
estoque-central/
│
├── .github/
│   └── workflows/
│       ├── backend-ci.yml
│       ├── frontend-ci.yml
│       └── deploy-prod.yml
│
├── backend/                        # Maven single module
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/com/estoquecentral/
│   │   │   │   ├── auth/
│   │   │   │   ├── produtos/
│   │   │   │   │   ├── domain/
│   │   │   │   │   │   ├── model/
│   │   │   │   │   │   │   ├── Produto.java
│   │   │   │   │   │   │   └── Categoria.java
│   │   │   │   │   │   ├── port/
│   │   │   │   │   │   │   ├── in/
│   │   │   │   │   │   │   │   └── CriarProdutoUseCase.java
│   │   │   │   │   │   │   └── out/
│   │   │   │   │   │   │       └── ProdutoRepository.java
│   │   │   │   │   │   └── event/
│   │   │   │   │   │       └── ProdutoCriadoEvent.java
│   │   │   │   │   ├── application/
│   │   │   │   │   │   └── service/
│   │   │   │   │   │       └── ProdutoService.java
│   │   │   │   │   └── adapter/
│   │   │   │   │       ├── in/web/
│   │   │   │   │       │   └── ProdutoController.java
│   │   │   │   │       └── out/persistence/
│   │   │   │   │           └── ProdutoJdbcRepository.java
│   │   │   │   ├── vendas/
│   │   │   │   ├── estoque/
│   │   │   │   ├── compras/
│   │   │   │   ├── fiscal/
│   │   │   │   ├── integracoes/
│   │   │   │   └── shared/
│   │   │   │       ├── domain/valueobject/
│   │   │   │       │   ├── Money.java
│   │   │   │       │   └── TenantId.java
│   │   │   │       └── infrastructure/
│   │   │   │           ├── tenant/
│   │   │   │           │   ├── TenantContext.java
│   │   │   │           │   └── TenantFilter.java
│   │   │   │           └── config/
│   │   │   │               ├── SecurityConfig.java
│   │   │   │               └── DatabaseConfig.java
│   │   │   └── resources/
│   │   │       ├── application.yml
│   │   │       ├── application-dev.yml
│   │   │       ├── application-prod.yml
│   │   │       └── db/migration/
│   │   │           ├── V001__criar_schema_master.sql
│   │   │           ├── V002__criar_tabela_tenants.sql
│   │   │           └── V003__criar_tabelas_tenant.sql
│   │   └── test/
│   │       └── java/com/estoquecentral/
│   │           ├── produtos/
│   │           │   ├── ProdutoServiceTest.java
│   │           │   └── ProdutoIntegrationTest.java
│   │           └── ArchitectureTests.java
│   ├── pom.xml
│   └── mvnw
│
├── frontend/                       # Angular npm workspace
│   ├── src/
│   │   ├── app/
│   │   │   ├── core/
│   │   │   │   ├── auth/
│   │   │   │   │   ├── auth.service.ts
│   │   │   │   │   ├── auth.guard.ts
│   │   │   │   │   └── auth.interceptor.ts
│   │   │   │   └── tenant/
│   │   │   │       ├── tenant.service.ts
│   │   │   │       └── tenant.interceptor.ts
│   │   │   ├── shared/
│   │   │   │   ├── components/
│   │   │   │   │   ├── data-table/
│   │   │   │   │   └── loading-spinner/
│   │   │   │   ├── models/
│   │   │   │   │   ├── produto.model.ts
│   │   │   │   │   ├── venda.model.ts
│   │   │   │   │   └── money.model.ts
│   │   │   │   └── pipes/
│   │   │   │       └── money.pipe.ts
│   │   │   ├── features/
│   │   │   │   ├── produtos/
│   │   │   │   │   ├── produto-list/
│   │   │   │   │   │   ├── produto-list.component.ts
│   │   │   │   │   │   ├── produto-list.component.html
│   │   │   │   │   │   ├── produto-list.component.scss
│   │   │   │   │   │   └── produto-list.component.spec.ts
│   │   │   │   │   ├── produto-form/
│   │   │   │   │   └── services/
│   │   │   │   │       └── produto.service.ts
│   │   │   │   ├── pdv/
│   │   │   │   │   ├── pdv.component.ts
│   │   │   │   │   ├── carrinho/
│   │   │   │   │   ├── pagamento/
│   │   │   │   │   └── stores/
│   │   │   │   │       └── pdv.store.ts
│   │   │   │   ├── vendas/
│   │   │   │   ├── estoque/
│   │   │   │   └── compras/
│   │   │   ├── layout/
│   │   │   │   ├── main-layout/
│   │   │   │   │   ├── main-layout.component.ts
│   │   │   │   │   ├── header/
│   │   │   │   │   └── sidebar/
│   │   │   │   └── auth-layout/
│   │   │   ├── app.component.ts
│   │   │   ├── app.config.ts
│   │   │   └── app.routes.ts
│   │   ├── environments/
│   │   │   ├── environment.ts
│   │   │   └── environment.development.ts
│   │   ├── assets/
│   │   ├── styles.scss
│   │   └── index.html
│   ├── angular.json
│   ├── package.json
│   ├── tsconfig.json
│   └── tailwind.config.js
│
├── docker/
│   ├── backend.Dockerfile
│   ├── frontend.Dockerfile
│   └── docker-compose.yml
│
├── infrastructure/
│   └── azure/
│       ├── main.bicep
│       ├── modules/
│       │   ├── container-app.bicep
│       │   ├── database.bicep
│       │   └── redis.bicep
│       └── parameters/
│           ├── dev.json
│           ├── staging.json
│           └── prod.json
│
├── scripts/
│   ├── init-dev-env.sh
│   ├── seed-dev-data.sh
│   └── deploy.sh
│
├── docs/
│   ├── prd.md
│   ├── architecture.md
│   ├── architecture/
│   │   ├── 01-introducao.md
│   │   ├── 02-high-level-architecture.md
│   │   └── ...
│   └── api/
│       └── openapi.yaml
│
├── README.md
├── .gitignore
└── .editorconfig
```

## 12.2. Key Configuration Files

### **Root README.md**

```markdown
# Estoque Central

Sistema ERP omnichannel para gestão de inventário.

## Quick Start

### Prerequisites
- Java 21
- Node 18+
- Docker

### Local Development

1. Start infrastructure:
   ```bash
   docker compose up -d
   ```

2. Run backend:
   ```bash
   cd backend
   ./mvnw spring-boot:run
   ```

3. Run frontend:
   ```bash
   cd frontend
   npm start
   ```

4. Access: http://localhost:4200
```

### **docker-compose.yml**

```yaml
services:
  postgres:
    image: postgres:16-alpine
    environment:
      POSTGRES_DB: estoque_central
      POSTGRES_USER: postgres
      POSTGRES_PASSWORD: postgres
    ports:
      - "5432:5432"
    volumes:
      - postgres_data:/var/lib/postgresql/data

  redis:
    image: redis:7-alpine
    ports:
      - "6379:6379"

  backend:
    build:
      context: .
      dockerfile: docker/backend.Dockerfile
    ports:
      - "8080:8080"
    environment:
      SPRING_DATASOURCE_URL: jdbc:postgresql://postgres:5432/estoque_central
      SPRING_DATA_REDIS_HOST: redis
    depends_on:
      - postgres
      - redis

  frontend:
    build:
      context: .
      dockerfile: docker/frontend.Dockerfile
    ports:
      - "4200:80"

volumes:
  postgres_data:
```

### **Backend Dockerfile (Multi-stage)**

```dockerfile
# Build stage
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app
COPY backend/pom.xml .
RUN mvn dependency:go-offline
COPY backend/src ./src
RUN mvn clean package -DskipTests

# Runtime stage
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
```

### **Frontend Dockerfile (Multi-stage)**

```dockerfile
# Build stage
FROM node:18-alpine AS build
WORKDIR /app
COPY frontend/package*.json ./
RUN npm ci
COPY frontend/ ./
RUN npm run build

# Runtime stage
FROM nginx:alpine
COPY --from=build /app/dist/estoque-central /usr/share/nginx/html
COPY docker/nginx.conf /etc/nginx/nginx.conf
EXPOSE 80
```

## 12.3. Naming Conventions

### **Backend (Java)**
- **Classes**: PascalCase (`ProdutoService`, `VendaController`)
- **Methods**: camelCase (`criarProduto`, `buscarPorId`)
- **Packages**: lowercase (`com.estoquecentral.produtos`)
- **Constants**: UPPER_SNAKE_CASE (`MAX_RETRY_ATTEMPTS`)

### **Frontend (TypeScript)**
- **Components**: kebab-case files, PascalCase class (`produto-list.component.ts`, `ProdutoListComponent`)
- **Services**: kebab-case files, PascalCase class (`produto.service.ts`, `ProdutoService`)
- **Interfaces**: PascalCase (`Produto`, `Venda`)
- **Variables**: camelCase (`totalItens`, `precoUnitario`)

### **Database (SQL)**
- **Tables**: snake_case (`produtos`, `itens_venda`)
- **Columns**: snake_case (`preco_centavos`, `created_at`)
- **Schemas**: lowercase (`tenant_123e4567`)

### **SQL Migrations**
- **Pattern**: `V{num}__{description}.sql`
- **Examples**:
  - `V001__criar_schema_master.sql`
  - `V002__criar_tabela_tenants.sql`
  - `V003__adicionar_coluna_desconto.sql`

## 12.4. Git Workflow

### **Branch Convention**
```
main                         # Production
develop                      # Development
feature/EST-123-feature      # New features
bugfix/EST-456-bug-fix       # Bug fixes
hotfix/EST-789-critical      # Urgent production fixes
```

### **Commit Convention**
```
type(scope): subject

Examples:
feat(produtos): adicionar campo de desconto percentual
fix(vendas): corrigir cálculo de troco no PDV
refactor(estoque): extrair lógica de custo médio
test(compras): adicionar testes de recebimento
docs(api): atualizar OpenAPI spec
```

**Types**: feat, fix, refactor, test, docs, chore, perf, style
