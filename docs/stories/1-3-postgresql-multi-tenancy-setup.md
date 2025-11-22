# Story 1.3: PostgreSQL Multi-Tenancy Setup

**Epic**: 1 - Foundation & Core Infrastructure
**Story ID**: 1.3
**Status**: done
**Created**: 2025-01-30
**Updated**: 2025-01-21

---

## User Story

Como **arquiteto de sistemas**,
Eu quero **multi-tenancy schema-per-tenant com roteamento dinâmico de datasource**,
Para que **cada cliente (tenant) tenha isolamento completo de dados**.

---

## Context & Business Value

Esta story implementa o padrão schema-per-tenant para multi-tenancy, onde cada cliente tem seu próprio schema PostgreSQL isolado. O roteamento dinâmico de datasource garante que cada requisição HTTP acesse apenas os dados do tenant correto, eliminando riscos de vazamento de dados entre clientes.

**Valor de Negócio:**
- Isolamento completo de dados por cliente (compliance LGPD)
- Backups e restore independentes por tenant
- Escalabilidade: novos tenants não afetam existentes
- Segurança: impossível acessar dados de outro tenant via SQL injection

**Padrão Arquitetural:**
- AbstractRoutingDataSource do Spring roteia conexões baseado em contexto ThreadLocal
- TenantContext armazena tenant ID da requisição atual
- Flyway migrations executam automaticamente em cada schema de tenant

[Source: docs/architecture/09-database-schema.md:1-150]

---

## Acceptance Criteria

### AC1: AbstractRoutingDataSource Configurado
- [ ] `AbstractRoutingDataSource` do Spring configurado para rotear para schema correto
- [ ] `TenantRoutingDataSource` extends AbstractRoutingDataSource e implementa `determineCurrentLookupKey()`
- [ ] Método `determineCurrentLookupKey()` retorna schema name do `TenantContext`
- [ ] Bean `DataSource` registrado no Spring ApplicationContext

**Validação**: Teste unitário mocka `TenantContext` e verifica que routing seleciona schema correto

### AC2: TenantContext ThreadLocal Implementado
- [ ] Classe `TenantContext` criada com `ThreadLocal<String>` para armazenar tenant ID
- [ ] Métodos `setTenantId(String tenantId)` e `getTenantId()` implementados
- [ ] Método `clear()` limpa ThreadLocal ao fim da requisição (evita memory leak)
- [ ] Tenant ID é propagado para threads filhas se necessário (InheritableThreadLocal)

**Validação**: Teste valida que `setTenantId()` armazena valor isolado por thread

### AC3: TenantInterceptor Extrai Tenant ID
- [ ] `TenantInterceptor` implementa `HandlerInterceptor` do Spring MVC
- [ ] Método `preHandle()` extrai tenant ID do header `X-Tenant-ID` ou subdomain
- [ ] Se header presente: `TenantContext.setTenantId(headerValue)`
- [ ] Se subdomain: extrai primeiro segmento do hostname (ex: `tenant1.app.com` → `tenant1`)
- [ ] Método `afterCompletion()` chama `TenantContext.clear()` para evitar vazamento
- [ ] Interceptor registrado globalmente no `WebMvcConfigurer`

**Validação**: Teste de integração envia requisição com header `X-Tenant-ID: test-tenant` e verifica que contexto é populado

### AC4: Schema Public com Tabela Tenants
- [ ] Flyway migration `V001__create_tenants_table.sql` criada no diretório `db/migration`
- [ ] Tabela `public.tenants` criada com campos: id (UUID), nome, schema_name (UNIQUE), email, ativo, data_criacao
- [ ] Index criado: `idx_tenants_schema_name` para lookup rápido
- [ ] Migration executa automaticamente no startup via Flyway

**Validação**: Query `SELECT * FROM public.tenants` executa sem erros

### AC5: Endpoint POST /api/tenants Cria Tenant
- [ ] Controller `TenantController` criado com endpoint `POST /api/tenants`
- [ ] Request body: `{ "nome": "...", "email": "..." }`
- [ ] Service `TenantService.createTenant()` implementa lógica:
  1. Gera UUID para tenant ID
  2. Gera schema_name: `tenant_{uuid}` (ex: `tenant_a1b2c3d4`)
  3. Insere registro na tabela `public.tenants`
  4. Executa DDL: `CREATE SCHEMA IF NOT EXISTS tenant_{uuid}`
  5. Retorna HTTP 201 com JSON: `{ "id": "...", "schema_name": "..." }`
- [ ] Validações: nome não vazio, email válido

**Validação**: `POST /api/tenants` retorna HTTP 201 e novo schema existe no PostgreSQL

### AC6: Flyway Migrations Executam em Cada Schema
- [ ] Flyway configurado para descobrir schemas dinâmicos (via query em `public.tenants`)
- [ ] Migration `V002__create_tenant_tables.sql` criada com schema completo (usuarios, produtos, vendas, etc.)
- [ ] No startup: Flyway executa V002 em todos os schemas de tenants existentes
- [ ] Ao criar novo tenant: Flyway executa V002 apenas no novo schema
- [ ] Migrations ignoram schema `public` (apenas para tabela `tenants`)

**Validação**: Criar tenant → Query `SELECT * FROM tenant_abc123.usuarios` executa sem erro

### AC7: Teste de Integração Valida Isolamento
- [ ] Teste cria 2 tenants: Tenant A e Tenant B
- [ ] Insere produto "Produto A" no schema do Tenant A
- [ ] Insere produto "Produto B" no schema do Tenant B
- [ ] Query com `TenantContext.setTenantId(tenantA)` retorna apenas "Produto A"
- [ ] Query com `TenantContext.setTenantId(tenantB)` retorna apenas "Produto B"
- [ ] Teste valida que dados não vazam entre tenants

**Validação**: Teste de integração `MultiTenancyIsolationTest` passa com sucesso

---

## Tasks & Subtasks

### Task 1: Criar TenantContext para ThreadLocal
**AC: #2**
- [ ] Criar classe `com.estoquecentral.shared.tenant.TenantContext` no módulo shared
- [ ] Implementar `ThreadLocal<String> currentTenant` para armazenar tenant ID
- [ ] Método `setTenantId(String tenantId)`: armazena tenant no ThreadLocal
- [ ] Método `getTenantId()`: retorna tenant ID do ThreadLocal (ou null)
- [ ] Método `clear()`: limpa ThreadLocal
- [ ] Adicionar javadoc explicando uso do ThreadLocal

### Task 2: Implementar TenantRoutingDataSource
**AC: #1**
- [ ] Criar classe `com.estoquecentral.shared.tenant.TenantRoutingDataSource` extends `AbstractRoutingDataSource`
- [ ] Override `determineCurrentLookupKey()`:
  ```java
  @Override
  protected Object determineCurrentLookupKey() {
      String tenantId = TenantContext.getTenantId();
      if (tenantId == null) {
          return "public"; // Default schema
      }
      return "tenant_" + tenantId; // Schema name
  }
  ```
- [ ] Criar `@Configuration` class `DataSourceConfig` para registrar TenantRoutingDataSource como bean
- [ ] Configurar `targetDataSources` map para conter schemas descobertos
- [ ] Documentar que schemas devem ser adicionados dinamicamente ao criar novos tenants

### Task 3: Criar TenantInterceptor para Extrair Tenant ID
**AC: #3**
- [ ] Criar classe `com.estoquecentral.shared.tenant.TenantInterceptor` implements `HandlerInterceptor`
- [ ] Override `preHandle()`:
  ```java
  @Override
  public boolean preHandle(HttpServletRequest request, ...) {
      String tenantId = request.getHeader("X-Tenant-ID");
      if (tenantId == null) {
          tenantId = extractFromSubdomain(request.getServerName());
      }
      if (tenantId != null) {
          TenantContext.setTenantId(tenantId);
      }
      return true;
  }
  ```
- [ ] Override `afterCompletion()`: chama `TenantContext.clear()`
- [ ] Método `extractFromSubdomain(String hostname)`: regex para extrair primeiro segmento
- [ ] Registrar interceptor globalmente no `WebMvcConfigurer`

### Task 4: Criar Migration V001 - Tabela Tenants
**AC: #4**
- [ ] Criar arquivo `backend/src/main/resources/db/migration/V001__create_tenants_table.sql`
- [ ] SQL: `CREATE TABLE IF NOT EXISTS public.tenants (...)`
- [ ] Campos: id UUID PRIMARY KEY, nome VARCHAR(255) NOT NULL, schema_name VARCHAR(255) UNIQUE NOT NULL, email VARCHAR(255) NOT NULL, ativo BOOLEAN DEFAULT true, data_criacao TIMESTAMP DEFAULT NOW()
- [ ] Criar index: `CREATE INDEX idx_tenants_schema_name ON public.tenants(schema_name);`
- [ ] Validar que Flyway executa migration no startup

### Task 5: Criar Migration V002 - Schema Completo para Tenants
**AC: #6**
- [ ] Criar arquivo `backend/src/main/resources/db/migration/V002__create_tenant_schema.sql`
- [ ] SQL completo baseado em `docs/architecture/09-database-schema.md`:
  - Tabela `usuarios` (id, nome, email, google_id, role, ativo)
  - Tabela `categorias` (id, nome, descricao, ativa)
  - Tabela `produtos` (id, tipo, sku, nome, preco_centavos, custo_centavos, categoria_id, produto_pai_id)
  - Tabela `estoque` (id, produto_id, quantidade_disponivel, quantidade_reservada, custo_medio_ponderado_centavos)
  - Tabela `movimentacoes_estoque` (id, produto_id, tipo, quantidade, custo_unitario_centavos)
  - Tabela `clientes` (id, tipo, nome, cpf_cnpj, email, telefone)
  - Tabela `vendas` (id, numero, tipo, status, cliente_id, total_centavos)
  - Tabela `itens_venda` (id, venda_id, produto_id, quantidade, preco_unitario_centavos)
- [ ] Adicionar indexes relevantes conforme documentado
- [ ] Esta migration será executada em cada schema de tenant

### Task 6: Implementar TenantService e TenantController
**AC: #5**
- [ ] Criar `com.estoquecentral.auth.domain.Tenant` entity com campos do AC4
- [ ] Criar `com.estoquecentral.auth.adapter.out.TenantRepository` extends JdbcRepository
- [ ] Criar `com.estoquecentral.auth.application.TenantService` com método `createTenant(String nome, String email)`
- [ ] Lógica de `createTenant`:
  1. `UUID tenantId = UUID.randomUUID()`
  2. `String schemaName = "tenant_" + tenantId.toString().replace("-", "")`
  3. Inserir na tabela `public.tenants`
  4. Executar DDL: `jdbcTemplate.execute("CREATE SCHEMA IF NOT EXISTS " + schemaName)`
  5. Executar Flyway migrations no novo schema
  6. Retornar `TenantDTO` com id e schema_name
- [ ] Criar `com.estoquecentral.auth.adapter.in.TenantController`:
  - Endpoint: `POST /api/tenants`
  - Request: `CreateTenantRequest { nome, email }`
  - Response: HTTP 201 + `TenantDTO`
- [ ] Validações: `@NotBlank` para nome, `@Email` para email

### Task 7: Configurar Flyway para Multi-Tenancy
**AC: #6**
- [ ] Atualizar `application.properties`:
  ```properties
  spring.flyway.enabled=true
  spring.flyway.baseline-on-migrate=true
  spring.flyway.locations=classpath:db/migration
  spring.flyway.schemas=public
  ```
- [ ] Criar `FlywayMultiTenantConfig` para executar migrations em schemas de tenants:
  ```java
  @PostConstruct
  public void migrateTenantSchemas() {
      List<String> schemas = tenantRepository.findAllSchemaNames();
      for (String schema : schemas) {
          Flyway flyway = Flyway.configure()
              .dataSource(dataSource)
              .schemas(schema)
              .locations("classpath:db/migration/tenant")
              .load();
          flyway.migrate();
      }
  }
  ```
- [ ] Mover V002 para subdiretório `db/migration/tenant/`
- [ ] V001 permanece em `db/migration/` (apenas public schema)

### Task 8: Testes de Integração
**AC: #7**
- [ ] Criar `MultiTenancyIsolationTest` usando `@SpringBootTest` + Testcontainers PostgreSQL
- [ ] Test setup:
  1. Criar Tenant A via `POST /api/tenants`
  2. Criar Tenant B via `POST /api/tenants`
  3. Inserir produto "Produto A" no schema do Tenant A
  4. Inserir produto "Produto B" no schema do Tenant B
- [ ] Test assertions:
  1. Query com header `X-Tenant-ID: tenantA` retorna apenas "Produto A"
  2. Query com header `X-Tenant-ID: tenantB` retorna apenas "Produto B"
  3. Query sem header `X-Tenant-ID` retorna vazio ou erro (sem tenant default)
- [ ] Validar que `TenantContext.clear()` é chamado corretamente (não vaza entre requisições)

### Task 9: Documentar Multi-Tenancy Setup
**AC: N/A - Documentação**
- [ ] Atualizar README.md com seção "Multi-Tenancy":
  ```markdown
  ## Multi-Tenancy

  O sistema usa estratégia **schema-per-tenant** onde cada cliente possui schema PostgreSQL isolado.

  ### Criar Novo Tenant
  \`\`\`bash
  curl -X POST http://localhost:8080/api/tenants \
    -H "Content-Type: application/json" \
    -d '{"nome": "Empresa ABC", "email": "contato@empresaabc.com"}'
  \`\`\`

  ### Fazer Requisições com Tenant ID
  \`\`\`bash
  curl http://localhost:8080/api/produtos \
    -H "X-Tenant-ID: a1b2c3d4"
  \`\`\`

  ### Schema Naming Convention
  - Schema público: `public` (apenas tabela `tenants`)
  - Schema de tenant: `tenant_{uuid}` (ex: `tenant_a1b2c3d4e5f6`)
  \`\`\`
- [ ] Adicionar diagrama de fluxo: requisição → interceptor → context → routing → schema

---

## Dev Notes

### Learnings from Previous Story

**From Story 1-2-docker-containerization (Status: review)**

- **PostgreSQL 17 Configured**: PostgreSQL 17 já configurado em docker-compose.yml com volume persistence. Service name: "postgres", porta 5432.
- **Database Connection URL**: Pattern atual em application.properties: `jdbc:postgresql://postgres:5432/estoque_central`. Multi-tenancy continuará usando mesma conexão mas alternando schema via SET search_path.
- **Flyway Enabled**: Flyway já habilitado com `spring.flyway.enabled=true`. Migrations em `classpath:db/migration`.
- **Environment Variables**: DATABASE_URL, DATABASE_USER, DATABASE_PASSWORD carregados via .env. Usar estas mesmas variáveis para TenantRoutingDataSource.
- **Health Checks**: `management.health.db.enabled=true` já configurado. Health check verificará conectividade com schema public.
- **Docker Network**: Service name "postgres" usado para conexão via Docker network (não localhost).

[Source: docs/stories/1-2-docker-containerization.md#Dev-Agent-Record]

### Architecture Patterns

**AbstractRoutingDataSource Pattern:**
- Spring fornece `AbstractRoutingDataSource` para roteamento dinâmico de conexões
- Implementar `determineCurrentLookupKey()` para retornar schema name
- ThreadLocal usado para propagar tenant ID sem poluir assinaturas de métodos

**ThreadLocal Safety:**
- Sempre limpar ThreadLocal no `afterCompletion()` do interceptor
- Evita memory leaks e vazamento de tenant ID entre requisições
- InheritableThreadLocal opcional para threads assíncronas (não necessário nesta story)

[Source: docs/architecture/09-database-schema.md:1-20]

### Database Schema Strategy

**Schema Público (public):**
- Contém apenas tabela `tenants` com metadata de todos os clientes
- Flyway migration V001 cria esta tabela
- Não contém dados de negócio (produtos, vendas, etc.)

**Schemas de Tenant (tenant_{uuid}):**
- Cada tenant tem schema isolado com estrutura completa
- Flyway migration V002 aplicada a cada novo schema
- Naming convention: `tenant_` + UUID sem hífens (ex: `tenant_a1b2c3d4e5f6`)

**Benefícios:**
- Isolamento total de dados (impossível acessar outro tenant via SQL)
- Backup/restore independente por tenant
- Schema evolution independente (migrations customizadas se necessário)

[Source: docs/architecture/09-database-schema.md:1-150]

### Spring Modulith Alignment

**Módulo Auth:**
- TenantContext, TenantInterceptor, TenantRoutingDataSource pertencem ao módulo auth
- TenantService e TenantController também no módulo auth
- Tenant é conceito transversal mas ownership está em auth (autenticação vincula usuário a tenant)

**Módulo Shared:**
- TenantContext pode ser promovido para shared se outros módulos precisarem
- Por enquanto, manter em auth e expor via API interna do Spring Modulith

[Source: docs/architecture/02-high-level-architecture.md]

### Testing Strategy

**Unit Tests:**
- TenantContext: testar set/get/clear com múltiplas threads
- TenantRoutingDataSource: mockar TenantContext e verificar routing

**Integration Tests:**
- Testcontainers PostgreSQL para simular ambiente real
- Criar múltiplos schemas e validar isolamento
- Testar Flyway migrations em schemas dinâmicos

**E2E Tests:**
- Não necessário nesta story (apenas infraestrutura)
- Stories futuras (1.4-1.5) farão testes E2E com multi-tenancy

[Source: docs/architecture/16-testing-strategy.md]

### Security Considerations

**Tenant ID Validation:**
- Validar que tenant ID existe na tabela `public.tenants` antes de setar contexto
- Retornar HTTP 403 se tenant ID inválido ou inativo
- Logging de tentativas de acesso a tenants inexistentes (potencial ataque)

**SQL Injection:**
- Schema name é UUID gerado internamente (não user input)
- Usar PreparedStatement ou JdbcTemplate com binding para DDL
- NUNCA concatenar schema name diretamente em SQL string

**Thread Safety:**
- ThreadLocal garante isolamento entre requisições concorrentes
- Sempre limpar ThreadLocal no finally ou afterCompletion

[Source: docs/architecture/15-security-and-performance.md]

### Project Structure Notes

**Expected File Locations:**
```
backend/src/main/java/com/estoquecentral/
├── auth/
│   ├── domain/
│   │   └── Tenant.java
│   ├── application/
│   │   └── TenantService.java
│   ├── adapter/
│   │   ├── in/
│   │   │   └── TenantController.java
│   │   └── out/
│   │       └── TenantRepository.java
├── shared/
│   └── tenant/
│       ├── TenantContext.java
│       ├── TenantInterceptor.java
│       ├── TenantRoutingDataSource.java
│       └── config/
│           ├── DataSourceConfig.java
│           └── FlywayMultiTenantConfig.java
backend/src/main/resources/
└── db/
    └── migration/
        ├── V001__create_tenants_table.sql
        └── tenant/
            └── V002__create_tenant_schema.sql
```

[Source: docs/architecture/12-unified-project-structure.md]

### References

- **Database Schema**: `docs/architecture/09-database-schema.md:1-150` - Schema completo documentado
- **Epic 1 Story 1.3**: `docs/epics/epic-01-foundation.md:41-55` - Critérios de aceitação originais
- **Backend Architecture**: `docs/architecture/11-backend-architecture.md` - Padrões arquiteturais
- **Testing Strategy**: `docs/architecture/16-testing-strategy.md` - Pirâmide de testes
- **Security**: `docs/architecture/15-security-and-performance.md` - Considerações de segurança
- **Spring Modulith**: `docs/architecture/02-high-level-architecture.md` - Bounded contexts e módulos
- **Story Anterior**: `docs/stories/1-2-docker-containerization.md` - Context e learnings

---

## Definition of Done (DoD)

- [ ] AbstractRoutingDataSource configurado e testado
- [ ] TenantContext ThreadLocal implementado
- [ ] TenantInterceptor extrai tenant ID de header ou subdomain
- [ ] Migration V001 cria tabela `public.tenants`
- [ ] Migration V002 cria schema completo para tenants
- [ ] Endpoint `POST /api/tenants` funciona e cria schema dinamicamente
- [ ] Flyway executa migrations em cada schema de tenant
- [ ] Teste de integração valida isolamento completo entre tenants
- [ ] README.md documentado com seção Multi-Tenancy
- [ ] Code review aprovado pelo SM
- [ ] Todas as tasks/subtasks marcadas como concluídas

---

## Dependencies & Blockers

**Dependências:**
- ✅ Story 1.1 (Project Scaffolding) - Backend Spring Boot estruturado
- ✅ Story 1.2 (Docker Containerization) - PostgreSQL 17 disponível via docker-compose

**Blockers Conhecidos:**
- Nenhum

**Next Stories:**
- Story 1.4 (Google OAuth 2.0) depende desta story para vincular usuários a tenants
- Story 1.5 (RBAC) depende desta story para roles dentro de cada tenant

---

## Change Log

- **2025-01-30**: Story drafted pelo SM Agent (Bob)

---

## Dev Agent Record

### Context Reference

<!-- Path(s) to story context XML will be added here by context workflow -->

### Agent Model Used

Claude 3.5 Sonnet (SM Agent)

### Debug Log References

### Completion Notes List

### File List

---

**Story criada por**: Bob (SM Agent)
**Data**: 2025-01-30
**Baseado em**: Epic 1 (Story 1.3), docs/architecture/09-database-schema.md, Story 1.2 learnings
