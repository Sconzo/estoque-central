# Multi-Tenancy - Estoque Central

O **Estoque Central** utiliza a estratégia **schema-per-tenant** para isolamento completo de dados entre clientes. Cada tenant (cliente) possui seu próprio schema PostgreSQL isolado, garantindo segurança e conformidade com a LGPD.

## ⚠️ STATUS ATUAL (2025-12-12)

**IMPORTANTE:** A implementação multi-tenant está **parcialmente completa**. Atualmente, existe um **workaround temporário** para permitir login enquanto o `search_path` não está sendo configurado corretamente.

### Problema Atual

O `TenantRoutingDataSource` e `TenantInterceptor` **não estão configurando o `search_path` do PostgreSQL** antes das queries. Resultado:

- ❌ Backend tenta buscar `usuarios` no schema do tenant
- ❌ PostgreSQL não encontra (tabela não existe no tenant schema)
- ❌ Query falha: `ERROR: relation "usuarios" does not exist`

### Solução Temporária Implementada

Para desbloquear o desenvolvimento, criamos a tabela `usuarios` no schema **`public`** (ao invés do schema do tenant):

```sql
-- docker/init-scripts/01-init-dev-data.sql
CREATE TABLE IF NOT EXISTS public.usuarios (
    id UUID PRIMARY KEY,
    google_id VARCHAR(255) NOT NULL UNIQUE,
    email VARCHAR(255) NOT NULL,
    nome VARCHAR(255) NOT NULL,
    tenant_id UUID NOT NULL,
    role VARCHAR(50) NOT NULL,
    ...
);
```

**Como isso resolve?**
- PostgreSQL procura primeiro no schema `public` quando `search_path` não está configurado
- Login funciona porque a tabela `usuarios` é encontrada
- Filtramos por `tenant_id` em todas as queries para manter isolamento

**Limitações:**
- ⚠️ **NÃO é a arquitetura multi-tenant ideal**
- ⚠️ Todos os tenants compartilham a mesma tabela (mas dados ficam isolados via `tenant_id`)
- ⚠️ Antes de produção, deve-se implementar a solução correta (veja seção abaixo)

### Próximos Passos para Correção

1. **Implementar configuração do `search_path`** no `TenantInterceptor`
2. **Migrar tabela `usuarios`** para os schemas de cada tenant
3. **Remover `public.usuarios`** após testes bem-sucedidos
4. **Validar isolamento completo** entre tenants

Veja a seção **"Como Funciona Internamente"** abaixo para entender como **deveria** funcionar quando completo.

---

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

## 🔧 GUIA: Corrigindo o search_path (Migração da Solução Temporária)

Este guia detalha como migrar da **solução temporária** (usuarios no public) para a **arquitetura correta** (search_path configurado).

### Passo 1: Verificar Estado Atual

```bash
# Conectar ao PostgreSQL
docker exec -it estoque-central-postgres psql -U postgres -d estoque_central

# Verificar que usuarios está no public
\dt public.usuarios

# Verificar schemas de tenant existentes
SELECT schema_name FROM information_schema.schemata
WHERE schema_name LIKE 'tenant_%';

# Verificar que usuarios NÃO existe nos tenant schemas
\dt tenant_00000000_0000_0000_0000_000000000000.usuarios
```

### Passo 2: Modificar TenantInterceptor

**Arquivo:** `backend/src/main/java/com/estoquecentral/shared/tenant/TenantInterceptor.java`

Adicionar configuração do `search_path` ANTES de cada requisição:

```java
@Component
public class TenantInterceptor implements HandlerInterceptor {

    @Autowired
    private DataSource dataSource;

    @Autowired
    private TenantRepository tenantRepository;

    @Override
    public boolean preHandle(HttpServletRequest request,
                           HttpServletResponse response,
                           Object handler) throws Exception {

        // 1. Extrair tenant ID do header
        String tenantId = request.getHeader("X-Tenant-ID");

        if (tenantId != null && !tenantId.isEmpty()) {
            // 2. Validar que tenant existe
            Tenant tenant = tenantRepository.findById(UUID.fromString(tenantId))
                .orElseThrow(() -> new TenantNotFoundException(tenantId));

            // 3. Configurar TenantContext (já existente)
            TenantContext.setTenantId(tenantId);

            // 4. ⭐ NOVO: Configurar search_path do PostgreSQL
            String schemaName = tenant.getSchemaName();
            try (Connection conn = dataSource.getConnection();
                 Statement stmt = conn.createStatement()) {

                // Define o search_path para o schema do tenant
                // ⚠️ IMPORTANTE: Sempre incluir 'public' no final para acessar tabela tenants
                stmt.execute(String.format(
                    "SET search_path TO %s, public",
                    schemaName
                ));

                logger.debug("search_path configurado: {}, public", schemaName);
            }
        }

        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request,
                              HttpServletResponse response,
                              Object handler,
                              Exception ex) throws Exception {
        // Limpar contexto (já existente)
        TenantContext.clear();

        // ⭐ NOVO: Resetar search_path
        try (Connection conn = dataSource.getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.execute("SET search_path TO public");
        } catch (Exception e) {
            logger.warn("Erro ao resetar search_path: {}", e.getMessage());
        }
    }
}
```

### Passo 3: Criar Tabela usuarios nos Tenant Schemas

**Opção A: Via Flyway Migration** (recomendado para produção)

Criar arquivo: `backend/src/main/resources/db/migration/tenant/V003__create_usuarios_table.sql`

```sql
-- Esta migration será aplicada em TODOS os schemas de tenant automaticamente
CREATE TABLE usuarios (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    google_id VARCHAR(255) NOT NULL UNIQUE,
    email VARCHAR(255) NOT NULL UNIQUE,
    nome VARCHAR(255) NOT NULL,
    picture_url VARCHAR(500),
    role VARCHAR(50) NOT NULL CHECK (role IN ('ADMIN', 'GERENTE', 'VENDEDOR', 'ESTOQUISTA')),
    tenant_id UUID NOT NULL,
    profile_id UUID,
    ativo BOOLEAN NOT NULL DEFAULT true,
    data_criacao TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    data_atualizacao TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Índices
CREATE INDEX idx_usuarios_google_id ON usuarios(google_id);
CREATE INDEX idx_usuarios_email ON usuarios(email);
CREATE INDEX idx_usuarios_role ON usuarios(role);
CREATE INDEX idx_usuarios_ativo ON usuarios(ativo) WHERE ativo = true;

-- Foreign key para tenants (no schema public)
ALTER TABLE usuarios
ADD CONSTRAINT fk_usuarios_tenant
FOREIGN KEY (tenant_id) REFERENCES public.tenants(id);
```

**Opção B: SQL Manual** (mais rápido para dev)

```sql
-- Conectar ao PostgreSQL
docker exec -it estoque-central-postgres psql -U postgres -d estoque_central

-- Criar tabela no schema do tenant de desenvolvimento
CREATE TABLE tenant_00000000_0000_0000_0000_000000000000.usuarios (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    google_id VARCHAR(255) NOT NULL UNIQUE,
    email VARCHAR(255) NOT NULL UNIQUE,
    nome VARCHAR(255) NOT NULL,
    picture_url VARCHAR(500),
    role VARCHAR(50) NOT NULL CHECK (role IN ('ADMIN', 'GERENTE', 'VENDEDOR', 'ESTOQUISTA')),
    tenant_id UUID NOT NULL,
    profile_id UUID,
    ativo BOOLEAN NOT NULL DEFAULT true,
    data_criacao TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    data_atualizacao TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Criar índices
CREATE INDEX idx_usuarios_google_id ON tenant_00000000_0000_0000_0000_000000000000.usuarios(google_id);
CREATE INDEX idx_usuarios_email ON tenant_00000000_0000_0000_0000_000000000000.usuarios(email);
CREATE INDEX idx_usuarios_role ON tenant_00000000_0000_0000_0000_000000000000.usuarios(role);
```

### Passo 4: Migrar Dados Existentes

```sql
-- Migrar usuarios do public para o schema do tenant
INSERT INTO tenant_00000000_0000_0000_0000_000000000000.usuarios (
    id, google_id, email, nome, picture_url, role,
    tenant_id, profile_id, ativo, data_criacao, data_atualizacao
)
SELECT
    id, google_id, email, nome, picture_url, role,
    tenant_id, profile_id, ativo, data_criacao, data_atualizacao
FROM public.usuarios
WHERE tenant_id = '00000000-0000-0000-0000-000000000000';

-- Verificar que os dados foram migrados
SELECT count(*) FROM tenant_00000000_0000_0000_0000_000000000000.usuarios;
SELECT count(*) FROM public.usuarios;
```

### Passo 5: Testar com search_path Configurado

```bash
# 1. Reiniciar aplicação para carregar TenantInterceptor modificado
docker-compose restart app

# 2. Tentar fazer login
# - Abrir http://localhost:4200/login
# - Fazer login com Google
# - Verificar que NÃO há erro "relation usuarios does not exist"

# 3. Verificar logs
docker-compose logs app | grep -i "search_path configurado"

# 4. (Opcional) Verificar search_path no banco
docker exec -it estoque-central-postgres psql -U postgres -d estoque_central

-- Durante uma requisição autenticada, executar:
SHOW search_path;
-- Esperado: tenant_00000000_0000_0000_0000_000000000000, public
```

### Passo 6: Remover public.usuarios (Após Testes)

⚠️ **CUIDADO:** Só execute após confirmar que login funciona com a nova configuração!

```sql
-- Backup antes de remover (recomendado)
CREATE TABLE public.usuarios_backup AS SELECT * FROM public.usuarios;

-- Remover tabela do public
DROP TABLE public.usuarios;

-- Verificar que aplicação continua funcionando
-- (o search_path agora aponta para o schema do tenant)
```

### Passo 7: Atualizar docker/init-scripts

Modificar `docker/init-scripts/01-init-dev-data.sql` para criar usuarios no tenant schema:

```sql
-- Remover criação de public.usuarios

-- Adicionar criação no tenant schema
CREATE TABLE IF NOT EXISTS tenant_00000000_0000_0000_0000_000000000000.usuarios (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    google_id VARCHAR(255) NOT NULL UNIQUE,
    email VARCHAR(255) NOT NULL UNIQUE,
    nome VARCHAR(255) NOT NULL,
    picture_url VARCHAR(500),
    role VARCHAR(50) NOT NULL CHECK (role IN ('ADMIN', 'GERENTE', 'VENDEDOR', 'ESTOQUISTA')),
    tenant_id UUID NOT NULL,
    profile_id UUID,
    ativo BOOLEAN NOT NULL DEFAULT true,
    data_criacao TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    data_atualizacao TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Criar usuário admin no schema do tenant
INSERT INTO tenant_00000000_0000_0000_0000_000000000000.usuarios (...)
VALUES (...);
```

### Checklist de Validação

Após implementar, validar que:

- [ ] Login funciona sem erros "relation usuarios does not exist"
- [ ] Logs mostram "search_path configurado: tenant_..."
- [ ] Query `SHOW search_path;` retorna o schema do tenant
- [ ] Usuarios criados aparecem no schema correto (não no public)
- [ ] Múltiplos tenants têm dados isolados
- [ ] Aplicação reinicia sem erros (testar `docker-compose restart app`)
- [ ] Criar novo tenant executa migration de usuarios automaticamente

### Rollback (Se Algo Der Errado)

```sql
-- 1. Restaurar public.usuarios
CREATE TABLE public.usuarios AS SELECT * FROM public.usuarios_backup;

-- 2. Reverter TenantInterceptor (remover configuração de search_path)

-- 3. Reiniciar aplicação
docker-compose restart app
```

---

## Referências

- Story 1.3: `docs/stories/1-3-postgresql-multi-tenancy-setup.md`
- Arquitetura: `docs/architecture/09-database-schema.md`
- Código: `backend/src/main/java/com/estoquecentral/shared/tenant/`
- Init Scripts: `docker/init-scripts/01-init-dev-data.sql`

---

**Implementado em**: Story 1.3 (Epic 1 - Foundation & Core Infrastructure)
**Data**: 2025-01-30
**Última atualização**: 2025-12-12 (Documentação do workaround temporário)
