# Epic 1: Foundation & Core Infrastructure

**Objetivo:** Estabelecer a fundação técnica do sistema com infraestrutura de projeto (monorepo Maven + Spring Boot + Angular), multi-tenancy schema-per-tenant, autenticação via Google OAuth 2.0, sistema de permissões (Roles/Profiles/Users), containerização Docker e pipeline CI/CD básico para Azure. Este épico entrega um sistema deployável com login funcional e endpoint de health check, permitindo que todos os épicos subsequentes construam sobre uma base sólida e testada.

---

## Story 1.1: Project Scaffolding & Build Setup

Como **desenvolvedor**,
Eu quero **estrutura de monorepo Maven multi-module com backend Spring Boot e frontend Angular**,
Para que **eu tenha ambiente de desenvolvimento consistente com builds automatizados**.

**Critérios de Aceitação:**
1. Projeto Maven multi-module criado com módulos: `backend`, `frontend`, `shared`
2. Backend usa Spring Boot 3.3+ com Java 25, Spring Modulith, Spring Data JDBC
3. Frontend usa Angular 17+ com Standalone Components
4. Build Maven (`mvn clean install`) compila frontend e backend com sucesso
5. Script `dev-setup.sh` inicializa ambiente local (instala dependências Node, Maven)
6. `.gitignore` configurado para ignorar `target/`, `node_modules/`, `.env`
7. `README.md` documenta comandos de build, execução local e estrutura de módulos

---

## Story 1.2: Docker Containerization

Como **engenheiro DevOps**,
Eu quero **Dockerfile multi-stage para build e runtime otimizado**,
Para que **a aplicação possa ser deployada consistentemente em qualquer ambiente**.

**Critérios de Aceitação:**
1. Dockerfile multi-stage criado: stage 1 (build Maven + Angular), stage 2 (runtime JRE slim)
2. Base image usa `eclipse-temurin:21-jre-alpine` para runtime (segura, otimizada)
3. Imagem Docker final tem tamanho < 300MB
4. `docker-compose.yml` criado com serviços: app, postgres, redis
5. Comando `docker-compose up` inicia ambiente completo localmente
6. Variáveis de ambiente configuráveis via arquivo `.env` (DATABASE_URL, REDIS_URL, etc)
7. Health check endpoint `/actuator/health` responde HTTP 200 quando container está pronto

---

## Story 1.3: PostgreSQL Multi-Tenancy Setup

Como **arquiteto de sistemas**,
Eu quero **multi-tenancy schema-per-tenant com roteamento dinâmico de datasource**,
Para que **cada cliente (tenant) tenha isolamento completo de dados**.

**Critérios de Aceitação:**
1. `AbstractRoutingDataSource` do Spring configurado para rotear para schema correto baseado em contexto do tenant
2. `TenantContext` ThreadLocal armazena tenant ID da requisição atual
3. Middleware `TenantInterceptor` extrai tenant ID do header `X-Tenant-ID` ou subdomain e popula contexto
4. Script Flyway migration cria schema `public` com tabela `tenants` (id, name, schema_name, status)
5. Endpoint `POST /api/tenants` cria novo tenant: insere registro em `tenants` e cria schema dedicado
6. Flyway executa migrations em cada schema de tenant automaticamente
7. Teste de integração valida que dados de tenant A não são visíveis para tenant B

---

## Story 1.4: Google OAuth 2.0 Authentication

Como **usuário**,
Eu quero **autenticação via Google OAuth 2.0**,
Para que **eu possa fazer login de forma segura sem gerenciar senhas**.

**Critérios de Aceitação:**
1. Spring Security OAuth2 Client configurado com Google Provider
2. Tela de login Angular exibe botão "Entrar com Google"
3. Fluxo OAuth redireciona para Google, usuário autoriza e retorna com código
4. Backend troca código por token, valida token e cria sessão (JWT ou Spring Session)
5. Dados do Google (ID, email, nome) são extraídos e armazenados na tabela `users`
6. JWT token retornado ao frontend contém: user ID, tenant ID, roles
7. Frontend armazena JWT em localStorage e inclui em header `Authorization: Bearer <token>` em todas as requisições
8. Endpoint `/api/auth/me` retorna dados do usuário autenticado
9. Logout limpa sessão no backend e remove JWT do frontend

---

## Story 1.5: Role-Based Access Control (RBAC)

Como **administrador de sistema**,
Eu quero **sistema de permissões com Roles, Profiles e Users**,
Para que **eu possa controlar quem acessa cada módulo do sistema**.

**Critérios de Aceitação:**
1. Tabelas criadas: `roles` (id, name, description), `profiles` (id, name), `profile_roles` (profile_id, role_id), `users` (id, google_id, email, name, profile_id, tenant_id)
2. Endpoint `POST /api/roles` cria role (ex: "Vendas", "Estoque", "PDV")
3. Endpoint `POST /api/profiles` cria profile e associa múltiplos roles (ex: Profile "Gerente" tem roles ["Vendas", "Estoque", "Relatórios"])
4. Endpoint `PUT /api/users/{id}/profile` atribui profile a usuário
5. Anotação `@RequiresRole("Estoque")` em controllers bloqueia acesso se usuário não tiver role
6. Middleware backend valida roles em cada requisição e retorna HTTP 403 se não autorizado
7. Guard Angular (`RoleGuard`) bloqueia navegação para rotas protegidas no frontend
8. Diretiva Angular `*hasRole="'Estoque'"` esconde elementos UI se usuário não tiver role
9. Teste valida que usuário com profile "Caixa" (role "PDV") não acessa endpoint `/api/compras`

---

## Story 1.6: CI/CD Pipeline com GitHub Actions

Como **desenvolvedor**,
Eu quero **pipeline CI/CD automatizado com GitHub Actions**,
Para que **commits em main façam build, test e deploy automático para Azure**.

**Critérios de Aceitação:**
1. GitHub Actions workflow `.github/workflows/ci-cd.yml` criado com jobs: `build`, `test`, `deploy`
2. Job `build`: executa `mvn clean install`, builda imagem Docker, faz push para Azure Container Registry (ACR)
3. Job `test`: executa testes unitários e de integração com Testcontainers (PostgreSQL + Redis)
4. Job `deploy`: faz deploy da imagem Docker para Azure Container Apps (ou App Service for Containers)
5. Pipeline executa automaticamente em push para branch `main`
6. Pull requests executam apenas `build` e `test` (não deploy)
7. Secrets do GitHub configurados: `AZURE_CREDENTIALS`, `ACR_USERNAME`, `ACR_PASSWORD`
8. Deploy bem-sucedido atualiza aplicação em Azure e health check `/actuator/health` responde HTTP 200

---

## Story 1.7: Azure Infrastructure Setup

Como **engenheiro DevOps**,
Eu quero **infraestrutura Azure provisionada (PostgreSQL, Redis, Container Apps)**,
Para que **a aplicação tenha ambiente de produção funcional**.

**Critérios de Aceitação:**
1. Azure Database for PostgreSQL - Flexible Server criado (sku: B1ms para MVP)
2. Azure Cache for Redis criado (sku: Basic C0 para MVP)
3. Azure Container Registry (ACR) criado para armazenar imagens Docker
4. Azure Container Apps (ou App Service for Containers) criado com:
   - Variáveis de ambiente: `DATABASE_URL`, `REDIS_URL`, `GOOGLE_OAUTH_CLIENT_ID`, `GOOGLE_OAUTH_CLIENT_SECRET`
   - Secrets armazenados em Azure Key Vault e referenciados via managed identity
5. Azure Front Door ou Application Gateway configurado como ponto de entrada (HTTPS)
6. DNS configurado apontando para Azure Front Door
7. Logs da aplicação fluem para Azure Monitor / Application Insights
8. Health check configurado no Azure Container Apps monitora `/actuator/health`
