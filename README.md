# Estoque Central

Sistema ERP Omnichannel Brasileiro para gestão de inventário, integrando vendas físicas (PDV), vendas online (B2B/B2C), marketplaces (Mercado Livre), e emissão de documentos fiscais (NFCe).

## Visão Geral

O Estoque Central é um sistema multi-tenant com isolamento de dados por schema PostgreSQL, implementado como um monorepo híbrido com backend Java/Spring Boot e frontend Angular standalone components.

### Principais Funcionalidades

- **Gestão de Produtos**: Produtos simples, variantes, compostos (kits)
- **Controle de Estoque**: Movimentações, reservas, custo médio ponderado
- **Vendas Multi-canal**: PDV, B2B, B2C com regras de pricing diferentes
- **Compras**: Ordens de compra, recebimento de mercadoria
- **Integração Mercado Livre**: Sync de produtos, importação de pedidos
- **Emissão Fiscal**: NFCe com retry queue (até 10 tentativas)
- **Multi-tenancy**: Isolamento completo por tenant

## Pré-requisitos

Antes de começar, certifique-se de ter as seguintes ferramentas instaladas:

### Opção 1: Desenvolvimento com Docker (Recomendado)

- **Docker 24+** - [Download](https://www.docker.com/products/docker-desktop)
- **Docker Compose V2** - (incluído com Docker Desktop)

### Opção 2: Desenvolvimento Local

- **Java 21 LTS** - [Download](https://adoptium.net/)
- **Maven 3.9+** - [Download](https://maven.apache.org/download.cgi)
- **Node.js 22 LTS ou 24 LTS** - [Download](https://nodejs.org/)
- **npm 10+** (incluído com Node.js)
- **PostgreSQL 17+ ou 18** - [Download](https://www.postgresql.org/download/)
- **Redis 8.0+** - [Download](https://redis.io/download/)

### Verificar Versões Instaladas

```bash
java -version      # Deve mostrar Java 21
mvn -version       # Deve mostrar Maven 3.9+
node -v            # Deve mostrar v22.x.x ou v24.x.x
npm -v             # Deve mostrar 10.x.x
```

## Setup Inicial

### 1. Clonar o Repositório

```bash
git clone <repository-url>
cd "ERP v6"
```

### 2. Executar Script de Setup

O script `dev-setup.sh` valida pré-requisitos e instala todas as dependências:

```bash
./scripts/dev-setup.sh
```

O script irá:
- Verificar versões de Java, Maven, Node, npm
- Instalar dependências Maven (backend)
- Instalar dependências npm (frontend)
- Criar arquivo `.env.template`

### 3. Configurar Variáveis de Ambiente

Copie o template e configure suas variáveis:

```bash
cp .env.template .env
```

Edite `.env` com suas configurações:

```properties
# Database
DATABASE_URL=jdbc:postgresql://localhost:5432/estoque_central
DATABASE_USER=postgres
DATABASE_PASSWORD=your-password

# Redis
REDIS_URL=redis://localhost:6379

# Google OAuth 2.0
GOOGLE_OAUTH_CLIENT_ID=your-client-id
GOOGLE_OAUTH_CLIENT_SECRET=your-client-secret
```

### 4. Iniciar Serviços de Infraestrutura

Certifique-se de que PostgreSQL e Redis estão rodando:

```bash
# PostgreSQL (porta 5432)
# Redis (porta 6379)
```

## Executar com Docker

**A maneira mais rápida de rodar o Estoque Central é usando Docker!** 🐳

Com Docker, você não precisa instalar Java, Maven, Node.js, PostgreSQL ou Redis localmente. Tudo é executado em containers isolados.

### Pré-requisitos

- **Docker 24+** instalado
- **Docker Compose V2** instalado

### Setup

1. **Copie o arquivo de exemplo de variáveis de ambiente:**

   ```bash
   cp .env.example .env
   ```

2. **Edite `.env` e configure suas variáveis** (especialmente OAuth credentials):

   ```bash
   # Edite o arquivo .env com suas configurações
   # As configurações padrão funcionam para desenvolvimento local

   # IMPORTANTE: Configure as credenciais OAuth do Google
   GOOGLE_OAUTH_CLIENT_ID=seu-client-id.apps.googleusercontent.com
   GOOGLE_OAUTH_CLIENT_SECRET=seu-client-secret
   ```

3. **Inicie todos os serviços:**

   ```bash
   docker-compose up -d
   ```

   Este comando irá:
   - Buildar a imagem Docker do backend (multi-stage build)
   - Iniciar PostgreSQL 17 (porta 5432)
   - Iniciar Redis 8 (porta 6379)
   - Iniciar a aplicação Spring Boot (porta 8080)

4. **Aguarde os serviços iniciarem (health checks):**

   ```bash
   docker-compose logs -f app
   ```

   Aguarde até ver: `Started EstoqueCentralApplication in X seconds`

5. **Acesse a aplicação:**

   - **API**: http://localhost:8080
   - **Health Check**: http://localhost:8080/actuator/health
   - **Swagger UI**: http://localhost:8080/swagger-ui.html

### Comandos Úteis

```bash
# Ver logs de todos os serviços
docker-compose logs -f

# Ver logs apenas do backend
docker-compose logs -f app

# Ver logs do PostgreSQL
docker-compose logs -f postgres

# Ver logs do Redis
docker-compose logs -f redis

# Reiniciar serviços
docker-compose restart

# Parar serviços (mantém dados)
docker-compose stop

# Parar e remover containers
docker-compose down

# Parar e remover volumes (CUIDADO: apaga dados!)
docker-compose down -v

# Rebuild após mudanças no código
docker-compose up -d --build

# Verificar status dos serviços
docker-compose ps
```

### Troubleshooting

#### Erro "port is already allocated"

Outra aplicação está usando a porta. Identifique e mate o processo:

```bash
# Windows
netstat -ano | findstr :8080
taskkill /PID <PID> /F

# Linux/Mac
lsof -ti:8080 | xargs kill -9
```

Ou altere a porta no `docker-compose.yml`:

```yaml
services:
  app:
    ports:
      - "8081:8080"  # Usar porta 8081 ao invés de 8080
```

#### Backend não conecta ao PostgreSQL

Aguarde ~30 segundos para o PostgreSQL inicializar completamente. O backend tem retry automático.

```bash
# Verificar se PostgreSQL está saudável
docker-compose ps postgres

# Ver logs do PostgreSQL
docker-compose logs postgres
```

#### Rebuild após mudanças no código

```bash
docker-compose up -d --build
```

#### Limpar tudo e começar do zero

```bash
# Para todos os containers e remove volumes
docker-compose down -v

# Remove imagens antigas
docker rmi estoque-central:latest

# Rebuild e inicia novamente
docker-compose up -d --build
```

## Build

### Build Completo (Backend + Frontend)

```bash
cd backend
mvn clean install
```

Este comando:
1. Compila o backend Java
2. Executa `npm install` no frontend
3. Executa `npm run build` no frontend
4. Copia o frontend buildado para `target/classes/static/`
5. Gera o arquivo `.jar` com backend + frontend

### Build Apenas Backend

```bash
cd backend
mvn clean compile
```

### Build Apenas Frontend

```bash
cd frontend
npm install
npm run build
```

## Executar Localmente

### Modo Desenvolvimento

```bash
cd backend
mvn spring-boot:run
```

A aplicação estará disponível em:
- **Frontend**: http://localhost:8080
- **API Docs (Swagger)**: http://localhost:8080/swagger-ui.html
- **Actuator Health**: http://localhost:8080/actuator/health

### Executar Frontend em Dev Mode (Hot Reload)

Em um terminal separado:

```bash
cd frontend
npm start
```

Frontend com hot reload: http://localhost:4200

## Estrutura do Projeto

O projeto segue uma arquitetura de **monorepo híbrido** com separação clara entre backend e frontend:

```
ERP v6/
├── backend/                    # Backend Java/Spring Boot
│   ├── src/main/java/com/estoquecentral/
│   │   ├── auth/              # Módulo: Autenticação e Autorização
│   │   ├── produtos/          # Módulo: Catálogo de Produtos
│   │   │   ├── domain/        # Camada de Domínio (Entities, Value Objects, Ports)
│   │   │   ├── application/   # Camada de Aplicação (Use Cases)
│   │   │   └── adapter/       # Camada de Adaptadores (REST, Persistence)
│   │   ├── vendas/            # Módulo: Vendas (PDV, B2B, B2C)
│   │   ├── estoque/           # Módulo: Gestão de Estoque
│   │   ├── compras/           # Módulo: Compras e Recebimento
│   │   ├── fiscal/            # Módulo: Emissão Fiscal (NFCe)
│   │   ├── integracoes/       # Módulo: Integrações Externas (Mercado Livre)
│   │   └── shared/            # Componentes Compartilhados (Value Objects)
│   ├── src/main/resources/
│   │   ├── application.properties
│   │   └── db/migration/      # Flyway migrations
│   └── pom.xml
├── frontend/                   # Frontend Angular 19+
│   ├── src/app/
│   │   ├── core/              # Auth, Tenant, Interceptors, Guards
│   │   ├── shared/            # Components, Models, Pipes reutilizáveis
│   │   ├── features/          # Features organizadas por domínio
│   │   │   ├── produtos/
│   │   │   ├── pdv/
│   │   │   ├── vendas/
│   │   │   ├── estoque/
│   │   │   └── compras/
│   │   ├── layout/            # Layouts (Main, Auth)
│   │   ├── app.component.ts
│   │   ├── app.config.ts      # Standalone config
│   │   └── app.routes.ts      # Standalone routing
│   └── package.json
├── docs/                       # Documentação (PRD, Arquitetura, Epics)
├── scripts/                    # Scripts de automação
└── .gitignore
```

### Arquitetura Backend: Spring Modulith + Hexagonal

O backend utiliza **Spring Modulith** para separação de bounded contexts via packages, combinado com **Arquitetura Hexagonal** (Ports & Adapters):

- **Domain**: Entidades, Value Objects, Domain Events, Ports (interfaces)
- **Application**: Implementações de Use Cases (Services)
- **Adapter**: Controllers REST (in) e Repositories (out)

### Arquitetura Frontend: Angular Standalone Components

O frontend utiliza **Angular 19+** com **Standalone Components** (sem NgModules):

- **Standalone Components**: Cada component importa suas próprias dependências
- **Signals**: State management reativo nativo do Angular
- **Lazy Loading**: Feature modules carregados sob demanda
- **Angular Material + Tailwind CSS**: UI components + utility-first CSS

## Tecnologias

### Backend

| Tecnologia | Versão | Propósito |
|------------|--------|-----------|
| Java | 21 LTS | Linguagem de programação |
| Spring Boot | 3.5+ | Framework backend |
| Spring Modulith | 1.1+ | Bounded contexts, events |
| Spring Data JDBC | 3.5+ | Acesso a dados (não JPA) |
| Spring Security | 6.3+ | OAuth2, JWT, RBAC |
| PostgreSQL | 17+ ou 18 | Banco de dados relacional |
| Redis | 8.0+ | Cache, retry queue (Redisson) |
| Flyway | 10+ | Database migrations |
| Testcontainers | 1.19+ | Testes de integração |
| ArchUnit | 1.2+ | Validação de arquitetura |

### Frontend

| Tecnologia | Versão | Propósito |
|------------|--------|-----------|
| Angular | 19+ (LTS) | Framework frontend |
| TypeScript | 5.7+ | Linguagem de programação |
| Angular Material | 19+ | UI components |
| Tailwind CSS | 3.4+ | Utility-first CSS |
| RxJS | 7.8+ | Programação reativa |
| date-fns | 3.0+ | Manipulação de datas |
| Playwright | 1.40+ | Testes E2E |

### Padrões Arquiteturais

- **Hexagonal Architecture (Ports & Adapters)**: Domain no centro, adapters isolados
- **Event-Driven**: Comunicação entre módulos via Spring ApplicationEventPublisher
- **Multi-tenancy (Schema-per-tenant)**: Isolamento completo de dados por tenant
- **NFCe Retry Queue**: Redisson DelayedQueue com exponential backoff (10 tentativas)
- **Value Objects**: Money, ProdutoId, ClienteId, TenantId (tipos fortes)
- **Repository Pattern**: Interfaces em domain.port.out, implementações em adapter.out

## Testes

### Executar Todos os Testes (Backend)

```bash
cd backend
mvn test
```

### Executar Testes de Arquitetura (ArchUnit)

```bash
cd backend
mvn test -Dtest=ArchitectureTests
```

### Executar Testes Frontend (Unit)

```bash
cd frontend
npm test
```

### Executar Testes E2E (Playwright)

```bash
cd frontend
npm run e2e
```

## Documentação

Para mais detalhes, consulte a documentação completa em `docs/`:

- **Product Brief**: `docs/brief/brief.md`
- **PRD (Product Requirements Document)**: `docs/prd/prd.md`
- **Architecture**: `docs/architecture/` (19 documentos)
- **Epics**: `docs/epics/` (6 épicos)
- **Stories**: `docs/stories/` (41 user stories)

## Licença

Este projeto é proprietário. Todos os direitos reservados.

## Contato

Para dúvidas ou suporte, entre em contato com a equipe de desenvolvimento.

---

**Gerado com**: Claude Code + BMAD v6
**Data**: 2025-01-30
