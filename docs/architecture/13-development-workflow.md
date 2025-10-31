# 13. Development Workflow

## 13.1. Local Development Setup

### **Pré-requisitos**

Antes de começar, garanta que você tem instalado:

| Ferramenta | Versão Mínima | Comando de Verificação |
|-----------|---------------|------------------------|
| **Java (JDK)** | 21 | `java -version` |
| **Maven** | 3.9+ | `mvn -version` |
| **Node.js** | 18+ | `node -v` |
| **npm** | 9+ | `npm -v` |
| **Docker** | 24+ | `docker -v` |
| **Docker Compose** | 2.20+ | `docker compose version` |
| **Git** | 2.40+ | `git --version` |
| **PostgreSQL Client** | 16+ (opcional) | `psql --version` |

**IDE Recomendadas:**
- **Backend**: IntelliJ IDEA Ultimate ou VS Code com Java Extension Pack
- **Frontend**: VS Code com Angular Language Service

---

### **Initial Setup**

#### **1. Clone do Repositório**

```bash
git clone <repo-url>
cd estoque-central
```

#### **2. Configuração do Backend**

```bash
cd backend

# Copiar template de configuração
cp src/main/resources/application-dev.yml.template src/main/resources/application-dev.yml

# Editar application-dev.yml com suas credenciais locais
# Campos obrigatórios:
# - spring.datasource.url (jdbc:postgresql://localhost:5432/estoque_central)
# - spring.security.oauth2.client.registration.google.client-id
# - spring.security.oauth2.client.registration.google.client-secret
# - app.mercadolivre.client-id
# - app.mercadolivre.client-secret
# - app.focusnfe.api-token
```

**Exemplo de `application-dev.yml`:**

```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/estoque_central
    username: postgres
    password: postgres
  data:
    redis:
      host: localhost
      port: 6379

  security:
    oauth2:
      client:
        registration:
          google:
            client-id: ${GOOGLE_CLIENT_ID}
            client-secret: ${GOOGLE_CLIENT_SECRET}
            scope: openid,profile,email
            redirect-uri: http://localhost:4200/auth/callback

app:
  jwt:
    secret: dev-secret-change-in-production-min-256-bits
    expiration: 86400000 # 24 horas

  mercadolivre:
    client-id: ${ML_CLIENT_ID}
    client-secret: ${ML_CLIENT_SECRET}
    redirect-uri: http://localhost:4200/integracoes/ml/callback

  focusnfe:
    api-token: ${FOCUSNFE_TOKEN}
    environment: homologacao
    webhook-url: http://localhost:8080/webhooks/focusnfe
```

#### **3. Configuração do Frontend**

```bash
cd frontend

# Copiar template de ambiente
cp src/environments/environment.development.ts.template src/environments/environment.development.ts

# Editar com a URL do backend local
```

**Exemplo de `environment.development.ts`:**

```typescript
export const environment = {
  production: false,
  apiUrl: 'http://localhost:8080/api',
  googleClientId: '<GOOGLE_CLIENT_ID>',
  mercadoLivreAppId: '<ML_APP_ID>'
};
```

#### **4. Subir Infraestrutura com Docker Compose**

```bash
# Na raiz do projeto
docker compose up -d postgres redis

# Verificar status
docker compose ps

# Logs (opcional)
docker compose logs -f postgres
```

#### **5. Rodar Migrações do Banco**

```bash
cd backend

# Rodar Flyway migrations
./mvnw flyway:migrate

# Verificar status das migrations
./mvnw flyway:info
```

**Nota:** As migrations criam:
- Schema `public` com tabela `tenants`
- Schemas por tenant (ex: `tenant_123e4567`)
- Tabelas de domínio em cada schema de tenant

#### **6. Popular Banco com Dados de Teste (Opcional)**

```bash
# Rodar script de seed
cd scripts
./seed-dev-data.sh

# Ou via Maven test profile
cd ../backend
./mvnw spring-boot:run -Dspring-boot.run.profiles=dev,seed
```

**Dados criados:**
- 1 tenant de demonstração (`demo-tenant`)
- Usuário admin: `admin@demo.com` / `Admin@123`
- 50 produtos de exemplo
- 10 categorias
- Movimentações de estoque

---

## 13.2. Comandos de Desenvolvimento

### **Backend (Spring Boot)**

```bash
cd backend

# Rodar aplicação em modo dev (hot reload com spring-boot-devtools)
./mvnw spring-boot:run -Dspring-boot.run.profiles=dev

# Rodar em debug mode (porta 5005)
./mvnw spring-boot:run -Dspring-boot.run.jvmArguments="-Xdebug -Xrunjdwp:transport=dt_socket,server=y,suspend=n,address=*:5005"

# Rodar testes unitários
./mvnw test

# Rodar testes de integração (com Testcontainers)
./mvnw verify -P integration-tests

# Rodar testes com coverage (JaCoCo)
./mvnw test jacoco:report
# Relatório em: target/site/jacoco/index.html

# Verificar estilo de código (Checkstyle)
./mvnw checkstyle:check

# Verificar arquitetura (ArchUnit)
./mvnw test -Dtest=ArchitectureTests

# Build sem testes
./mvnw clean package -DskipTests

# Build completo
./mvnw clean package
```

**Configurar Hot Reload:**

1. Adicione ao `application-dev.yml`:
```yaml
spring:
  devtools:
    restart:
      enabled: true
      additional-paths: src/main/java
```

2. No IntelliJ:
   - Settings → Build, Execution, Deployment → Compiler
   - ✅ Build project automatically
   - Settings → Advanced Settings
   - ✅ Allow auto-make to start even if developed application is running

### **Frontend (Angular)**

```bash
cd frontend

# Instalar dependências
npm install

# Rodar em modo dev (http://localhost:4200)
npm start
# Ou: ng serve

# Rodar com proxy API (evita CORS)
ng serve --proxy-config proxy.conf.json

# Rodar testes unitários (Jasmine + Karma)
npm test
# Ou: ng test

# Rodar testes em headless mode (CI)
ng test --watch=false --browsers=ChromeHeadless

# Rodar testes E2E (Playwright)
npm run e2e

# Rodar linter (ESLint)
npm run lint

# Corrigir problemas de lint automaticamente
npm run lint:fix

# Build de produção
npm run build
# Ou: ng build --configuration production

# Build de desenvolvimento
ng build --configuration development

# Analisar bundle size
ng build --stats-json
npx webpack-bundle-analyzer dist/estoque-central/stats.json
```

**Configurar Proxy API:**

Crie `frontend/proxy.conf.json`:
```json
{
  "/api": {
    "target": "http://localhost:8080",
    "secure": false,
    "logLevel": "debug",
    "changeOrigin": true
  }
}
```

---

## 13.3. Testing Commands

### **Executar Toda a Suite de Testes**

```bash
# Backend: Unit + Integration
cd backend
./mvnw verify

# Frontend: Unit + E2E
cd frontend
npm test -- --watch=false --code-coverage
npm run e2e
```

### **Testes Específicos**

```bash
# Backend: Teste específico
./mvnw test -Dtest=ProdutoServiceTest

# Backend: Testes de um módulo
./mvnw test -Dtest=com.estoquecentral.produtos.*

# Frontend: Teste específico
ng test --include='**/produto-list.component.spec.ts'

# Frontend: Teste de um módulo
ng test --include='**/produtos/**/*.spec.ts'
```

### **Coverage Reports**

```bash
# Backend (JaCoCo)
./mvnw verify
open target/site/jacoco/index.html

# Frontend (Karma Coverage)
ng test --code-coverage --watch=false
open coverage/estoque-central/index.html
```

---

## 13.4. Build Commands

### **Build Local**

```bash
# Backend: JAR executável
cd backend
./mvnw clean package
java -jar target/estoque-central-0.0.1-SNAPSHOT.jar

# Frontend: Static files
cd frontend
npm run build
# Output em: dist/estoque-central/
```

### **Build Docker Images**

```bash
# Build backend image
docker build -f docker/backend.Dockerfile -t estoque-central-backend:latest .

# Build frontend image
docker build -f docker/frontend.Dockerfile -t estoque-central-frontend:latest .

# Build todas as imagens com compose
docker compose build
```

### **Multi-stage Build (Otimizado)**

```bash
# Build e rodar backend
docker build -f docker/backend.Dockerfile -t backend:dev .
docker run -p 8080:8080 --env-file .env backend:dev

# Build e rodar frontend
docker build -f docker/frontend.Dockerfile -t frontend:dev .
docker run -p 80:80 frontend:dev
```

---

## 13.5. Common Development Tasks

### **Gerenciamento de Banco de Dados**

```bash
# Conectar ao PostgreSQL local
psql -h localhost -U postgres -d estoque_central

# Rodar migrações manualmente
cd backend
./mvnw flyway:migrate

# Limpar banco (⚠️ CUIDADO: Apaga tudo)
./mvnw flyway:clean
./mvnw flyway:migrate

# Ver histórico de migrations
./mvnw flyway:info

# Reparar migrations com checksum quebrado
./mvnw flyway:repair

# Criar nova migration
# 1. Crie arquivo: backend/src/main/resources/db/migration/V{num}__{desc}.sql
# 2. Exemplo: V006__adicionar_coluna_desconto_produto.sql
# 3. Rode: ./mvnw flyway:migrate
```

**Exemplo de Migration:**

```sql
-- backend/src/main/resources/db/migration/V006__adicionar_coluna_desconto_produto.sql

ALTER TABLE produtos
ADD COLUMN percentual_desconto DECIMAL(5,2) DEFAULT 0.00 CHECK (percentual_desconto >= 0 AND percentual_desconto <= 100);

COMMENT ON COLUMN produtos.percentual_desconto IS 'Desconto percentual aplicado ao produto (0-100%)';
```

### **Criar Novo Tenant**

```bash
# Via psql
psql -h localhost -U postgres -d estoque_central -c "
INSERT INTO public.tenants (id, nome, schema_name, ativo, data_criacao)
VALUES (gen_random_uuid(), 'Novo Tenant', 'tenant_novo', true, NOW());
"

# Rodar migrations no novo schema
./mvnw flyway:migrate -Dflyway.schemas=tenant_novo
```

### **Debugging**

#### **Backend (IntelliJ IDEA)**

1. Crie configuração "Run/Debug Configurations"
2. Adicione "Spring Boot" configuration
3. Main class: `com.estoquecentral.EstoqueCentralApplication`
4. Active profiles: `dev`
5. Environment variables: `GOOGLE_CLIENT_ID=...;GOOGLE_CLIENT_SECRET=...`
6. Clique em "Debug" (Shift+F9)

#### **Backend (VS Code)**

Crie `.vscode/launch.json`:
```json
{
  "version": "0.2.0",
  "configurations": [
    {
      "type": "java",
      "name": "Debug Spring Boot",
      "request": "launch",
      "mainClass": "com.estoquecentral.EstoqueCentralApplication",
      "projectName": "estoque-central",
      "args": "--spring.profiles.active=dev",
      "envFile": "${workspaceFolder}/.env"
    }
  ]
}
```

#### **Frontend (VS Code)**

1. Rode `ng serve` no terminal
2. Instale extensão "Debugger for Chrome" ou use Edge
3. Crie `.vscode/launch.json`:

```json
{
  "version": "0.2.0",
  "configurations": [
    {
      "type": "chrome",
      "request": "launch",
      "name": "Debug Angular",
      "url": "http://localhost:4200",
      "webRoot": "${workspaceFolder}/frontend/src",
      "sourceMapPathOverrides": {
        "webpack:/*": "${webRoot}/*"
      }
    }
  ]
}
```

### **Logs e Monitoring Local**

```bash
# Backend: Logs em tempo real
tail -f backend/logs/application.log

# Backend: Logs com filtro
tail -f backend/logs/application.log | grep ERROR

# Docker: Logs dos containers
docker compose logs -f backend
docker compose logs -f postgres

# Redis: Monitor comandos
docker compose exec redis redis-cli monitor

# PostgreSQL: Habilitar query logging
# Edite postgresql.conf:
log_statement = 'all'
log_duration = on
```

### **Limpar Ambiente de Desenvolvimento**

```bash
# Backend: Limpar build
cd backend
./mvnw clean

# Frontend: Limpar node_modules e build
cd frontend
rm -rf node_modules dist .angular
npm install

# Docker: Remover volumes e rebuild
docker compose down -v
docker compose up -d --build

# Git: Limpar arquivos não rastreados
git clean -fdx
```

---

## 13.6. Troubleshooting

### **Problema: Backend não inicia - Erro de conexão com PostgreSQL**

```bash
# Verificar se PostgreSQL está rodando
docker compose ps postgres

# Testar conexão manualmente
psql -h localhost -U postgres -d estoque_central

# Solução: Recriar container
docker compose down postgres
docker compose up -d postgres
```

### **Problema: Frontend não compila - Erro de TypeScript**

```bash
# Limpar cache do Angular
rm -rf frontend/.angular

# Reinstalar dependências
cd frontend
rm -rf node_modules package-lock.json
npm install

# Verificar versão do Node (deve ser 18+)
node -v
```

### **Problema: Testes falhando - Testcontainers não inicia**

```bash
# Verificar se Docker está rodando
docker ps

# Limpar containers antigos do Testcontainers
docker rm -f $(docker ps -aq --filter "label=org.testcontainers")

# Rodar com logs detalhados
./mvnw test -X
```

### **Problema: Hot reload não funciona**

**Backend:**
```bash
# Verificar se spring-boot-devtools está no pom.xml
grep devtools backend/pom.xml

# Adicionar se não existir:
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-devtools</artifactId>
    <scope>runtime</scope>
</dependency>
```

**Frontend:**
```bash
# Matar processo na porta 4200
npx kill-port 4200

# Rodar com polling (se estiver usando WSL/Docker)
ng serve --poll=2000
```

---

## 13.7. Performance Tips

### **Backend: Reduzir tempo de startup**

```yaml
# application-dev.yml
spring:
  jpa:
    show-sql: false # Desabilitar SQL logging
  devtools:
    restart:
      exclude: static/**,templates/**
```

### **Frontend: Acelerar build**

```bash
# Usar cache persistente
ng build --configuration development --build-optimizer=false

# Desabilitar source maps em dev
# angular.json → projects.estoque-central.architect.build.options
"sourceMap": false
```

### **Docker: Cache de layers**

```dockerfile
# Copiar apenas pom.xml primeiro (cache de dependências)
COPY pom.xml .
RUN mvn dependency:go-offline

# Depois copiar código fonte
COPY src ./src
RUN mvn package
```

---

## 13.8. Git Workflow

### **Branching Strategy**

```
main          ← Produção (protegida)
  ↑
develop       ← Desenvolvimento (protegida)
  ↑
feature/EST-123-adicionar-desconto-produto
bugfix/EST-456-corrigir-calculo-estoque
hotfix/EST-789-falha-critica-nfce
```

### **Commit Convention**

```bash
# Formato: <type>(<scope>): <subject>

# Exemplos:
git commit -m "feat(produtos): adicionar campo de desconto percentual"
git commit -m "fix(vendas): corrigir cálculo de troco no PDV"
git commit -m "refactor(estoque): extrair lógica de custo médio para service"
git commit -m "test(compras): adicionar testes para recebimento de mercadoria"
git commit -m "docs(api): atualizar OpenAPI spec com novos endpoints"
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

### **Pull Request Checklist**

Antes de abrir PR, garanta:

- [ ] Código compila sem erros (`mvn clean package` + `ng build`)
- [ ] Todos os testes passam (`mvn verify` + `npm test`)
- [ ] Linter sem warnings (`mvn checkstyle:check` + `npm run lint`)
- [ ] Coverage > 80% para código novo
- [ ] Migrations testadas localmente
- [ ] Documentação atualizada (se aplicável)
- [ ] PR description segue template
- [ ] Branch está atualizada com `develop`

```bash
# Sincronizar com develop antes de PR
git checkout develop
git pull
git checkout feature/sua-branch
git rebase develop
git push --force-with-lease
```

---

## 13.9. Environment Variables Summary

**Backend (.env ou application-dev.yml):**

```bash
# Database
SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5432/estoque_central
SPRING_DATASOURCE_USERNAME=postgres
SPRING_DATASOURCE_PASSWORD=postgres

# Redis
SPRING_DATA_REDIS_HOST=localhost
SPRING_DATA_REDIS_PORT=6379

# JWT
APP_JWT_SECRET=<256-bit-secret>
APP_JWT_EXPIRATION=86400000

# Google OAuth
GOOGLE_CLIENT_ID=<google-client-id>
GOOGLE_CLIENT_SECRET=<google-client-secret>

# Mercado Livre
ML_CLIENT_ID=<ml-app-id>
ML_CLIENT_SECRET=<ml-secret>

# Focus NFe
FOCUSNFE_TOKEN=<api-token>
FOCUSNFE_ENVIRONMENT=homologacao
```

**Frontend (environment.development.ts):**

```typescript
export const environment = {
  production: false,
  apiUrl: 'http://localhost:8080/api',
  googleClientId: '<google-client-id>',
  mercadoLivreAppId: '<ml-app-id>'
};
```

---

## 13.10. Quick Reference Card

### **Start Everything**

```bash
# 1. Subir infra
docker compose up -d postgres redis

# 2. Backend (terminal 1)
cd backend && ./mvnw spring-boot:run

# 3. Frontend (terminal 2)
cd frontend && npm start

# 4. Acessar: http://localhost:4200
```

### **Stop Everything**

```bash
# Ctrl+C nos terminais do backend e frontend

# Parar Docker
docker compose down
```

### **Daily Development Loop**

```bash
# 1. Pull latest changes
git checkout develop && git pull

# 2. Create feature branch
git checkout -b feature/EST-123-nova-feature

# 3. Code + Test
# ... fazer alterações ...
./mvnw test  # backend
npm test     # frontend

# 4. Commit
git add .
git commit -m "feat(modulo): descrição"

# 5. Push + PR
git push -u origin feature/EST-123-nova-feature
# Abrir PR no GitHub/GitLab
```

---

**🎯 Pronto para começar o desenvolvimento!**

Com este workflow, você tem tudo configurado para desenvolver, testar e fazer deploy do **Estoque Central** de forma eficiente e padronizada.
