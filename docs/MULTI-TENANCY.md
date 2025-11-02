# Multi-Tenancy - Estoque Central

O **Estoque Central** utiliza a estratégia **schema-per-tenant** para isolamento completo de dados entre clientes. Cada tenant (cliente) possui seu próprio schema PostgreSQL isolado, garantindo segurança e conformidade com a LGPD.

## Arquitetura Multi-Tenant

- **Schema Público (`public`)**: Contém apenas a tabela `tenants` com metadados de todos os clientes
- **Schemas de Tenant (`tenant_{uuid}`)**: Cada cliente tem um schema isolado com todas as tabelas de negócio (produtos, vendas, estoque, etc.)

**Exemplo:**
```
PostgreSQL Database: estoque_central
├── public                           (metadata)
│   └── tenants                      (lista de todos os tenants)
├── tenant_a1b2c3d4e5f6...          (Tenant: Empresa ABC)
│   ├── usuarios
│   ├── produtos
│   ├── vendas
│   ├── estoque
│   ├── clientes
│   └── ...
└── tenant_f6e5d4c3b2a1...          (Tenant: Empresa XYZ)
    ├── usuarios
    ├── produtos
    ├── vendas
    ├── estoque
    ├── clientes
    └── ...
```

## Criar Novo Tenant

Use o endpoint `POST /api/tenants` para criar um novo tenant:

```bash
curl -X POST http://localhost:8080/api/tenants \
  -H "Content-Type: application/json" \
  -d '{
    "nome": "Empresa ABC Ltda",
    "email": "contato@empresaabc.com"
  }'
```

**Resposta:**
```json
{
  "id": "a1b2c3d4-e5f6-7890-abcd-ef1234567890",
  "nome": "Empresa ABC Ltda",
  "schemaName": "tenant_a1b2c3d4e5f67890abcdef1234567890",
  "email": "contato@empresaabc.com",
  "ativo": true,
  "dataCriacao": "2025-01-30T10:15:30Z"
}
```

O sistema automaticamente:
1. ✅ Cria um novo UUID para o tenant
2. ✅ Cria o schema PostgreSQL isolado
3. ✅ Executa as migrations (cria todas as tabelas de negócio)
4. ✅ Retorna os detalhes do tenant criado

## Fazer Requisições com Tenant ID

Todas as requisições de API devem incluir o header `X-Tenant-ID` para identificar o tenant:

```bash
# Listar produtos do tenant A
curl http://localhost:8080/api/produtos \
  -H "X-Tenant-ID: a1b2c3d4-e5f6-7890-abcd-ef1234567890"

# Criar venda para o tenant A
curl -X POST http://localhost:8080/api/vendas \
  -H "X-Tenant-ID: a1b2c3d4-e5f6-7890-abcd-ef1234567890" \
  -H "Content-Type: application/json" \
  -d '{"clienteId": "...", "itens": [...]}'
```

**Importante:** O header `X-Tenant-ID` usa o **UUID do tenant** (campo `id`), não o `schemaName`.

## Listar Todos os Tenants

```bash
curl http://localhost:8080/api/tenants
```

**Resposta:**
```json
[
  {
    "id": "a1b2c3d4-e5f6-7890-abcd-ef1234567890",
    "nome": "Empresa ABC Ltda",
    "schemaName": "tenant_a1b2c3d4e5f67890abcdef1234567890",
    "email": "contato@empresaabc.com",
    "ativo": true,
    "dataCriacao": "2025-01-30T10:15:30Z"
  },
  {
    "id": "f6e5d4c3-b2a1-9087-fedc-ba0987654321",
    "nome": "Empresa XYZ S.A.",
    "schemaName": "tenant_f6e5d4c3b2a19087fedcba0987654321",
    "email": "admin@empresaxyz.com.br",
    "ativo": true,
    "dataCriacao": "2025-01-29T15:30:00Z"
  }
]
```

## Isolamento de Dados

O sistema garante **isolamento completo** entre tenants:

- ✅ **SQL Injection**: Impossível acessar dados de outro tenant via SQL injection
- ✅ **Backups Independentes**: Cada schema pode ter backup/restore separado
- ✅ **Performance**: Índices e queries otimizados por tenant
- ✅ **Escalabilidade**: Novos tenants não afetam tenants existentes
- ✅ **Conformidade LGPD**: Dados completamente isolados por cliente

## Como Funciona Internamente

Fluxo de uma requisição com multi-tenancy:

```
1. Request HTTP chega
   ├── Header: X-Tenant-ID: a1b2c3d4-e5f6-7890-abcd-ef1234567890
   └── URI: GET /api/produtos

2. TenantInterceptor (preHandle)
   ├── Extrai tenant ID do header X-Tenant-ID
   └── Armazena em TenantContext (ThreadLocal)

3. Controller executa
   └── Repository precisa acessar banco

4. TenantRoutingDataSource
   ├── Lê TenantContext.getTenantId()
   ├── Retorna schema name: "tenant_a1b2c3d4e5f6..."
   └── PostgreSQL executa: SET search_path TO tenant_a1b2c3d4e5f6...

5. Query executa no schema isolado
   └── SELECT * FROM produtos  (dentro do schema do tenant)

6. TenantInterceptor (afterCompletion)
   └── Limpa TenantContext (previne memory leaks)
```

### Componentes Principais

#### TenantContext
```java
// Armazena tenant ID no ThreadLocal (isolado por thread/request)
TenantContext.setTenantId("a1b2c3d4-e5f6-7890-abcd-ef1234567890");
String tenantId = TenantContext.getTenantId();
TenantContext.clear(); // SEMPRE limpar após request
```

#### TenantInterceptor
- Intercepta TODAS as requisições HTTP
- Extrai tenant ID do header `X-Tenant-ID` (ou subdomain no futuro)
- Popula `TenantContext`
- Limpa `TenantContext` após request (evita vazamento)

#### TenantRoutingDataSource
- Extends `AbstractRoutingDataSource` do Spring
- Implementa `determineCurrentLookupKey()` → retorna schema name
- Roteia conexões PostgreSQL para o schema correto

#### FlywayMultiTenantConfig
- Executa migrations em TODOS os schemas de tenants no startup
- Migrations V002+ aplicadas em cada schema tenant
- Migration V001 aplicada apenas no schema `public`

## Schema Naming Convention

- **Schema público**: `public` (apenas tabela `tenants`)
- **Schema de tenant**: `tenant_{uuid_sem_hifens}`
  - Formato UUID: `a1b2c3d4-e5f6-7890-abcd-ef1234567890`
  - Schema name: `tenant_a1b2c3d4e5f67890abcdef1234567890` (remove hífens)

## Migrations

### V001 - Public Schema (db/migration/V001__create_tenants_table.sql)
```sql
-- Aplicada ao schema PUBLIC apenas
CREATE TABLE public.tenants (
    id UUID PRIMARY KEY,
    nome VARCHAR(255) NOT NULL,
    schema_name VARCHAR(255) UNIQUE NOT NULL,
    email VARCHAR(255) NOT NULL,
    ativo BOOLEAN DEFAULT true,
    data_criacao TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
```

### V002 - Tenant Schema (db/migration/tenant/V002__create_tenant_schema.sql)
```sql
-- Aplicada a CADA schema de tenant
CREATE TABLE usuarios (...);
CREATE TABLE categorias (...);
CREATE TABLE produtos (...);
CREATE TABLE estoque (...);
CREATE TABLE vendas (...);
CREATE TABLE clientes (...);
CREATE TABLE compras (...);
-- ... todas as tabelas de negócio
```

## Segurança

### Validação de Tenant ID
- ✅ Tenant ID é **validado** antes de setar o contexto
- ✅ Schema name segue formato **UUID rígido** (previne SQL injection)
- ✅ Queries usam **PreparedStatement** com binding (não concatenação de strings)

### Thread Safety
- ✅ `ThreadLocal` garante isolamento entre requisições concorrentes
- ✅ `TenantContext.clear()` sempre executado no `afterCompletion`
- ✅ Previne memory leaks e vazamento de tenant ID

### Logging de Segurança
- ⚠️ Tentativas de acesso a tenants inexistentes são logadas
- ⚠️ Requisições sem tenant ID são logadas (nível DEBUG)

## Troubleshooting

### Erro: "No tenant context set"
**Causa**: Requisição não incluiu header `X-Tenant-ID`
**Solução**: Adicione o header com o UUID do tenant

```bash
curl http://localhost:8080/api/produtos \
  -H "X-Tenant-ID: a1b2c3d4-e5f6-7890-abcd-ef1234567890"
```

### Erro: "Tenant with email already exists"
**Causa**: Já existe um tenant com o mesmo email
**Solução**: Use um email diferente ou liste tenants existentes:

```bash
curl http://localhost:8080/api/tenants
```

### Erro: "Failed to create tenant schema"
**Causa**: Problema na criação do schema PostgreSQL ou migrations
**Solução**: Verifique logs do Flyway e permissões PostgreSQL

```bash
docker-compose logs app | grep -i flyway
```

### Dados vazam entre tenants
**Causa**: `TenantContext` não está sendo limpo corretamente
**Solução**: Verifique que `TenantInterceptor.afterCompletion()` está registrado

## Referências

- Story 1.3: `docs/stories/1-3-postgresql-multi-tenancy-setup.md`
- Arquitetura: `docs/architecture/09-database-schema.md`
- Código: `backend/src/main/java/com/estoquecentral/shared/tenant/`

---

**Implementado em**: Story 1.3 (Epic 1 - Foundation & Core Infrastructure)
**Data**: 2025-01-30
