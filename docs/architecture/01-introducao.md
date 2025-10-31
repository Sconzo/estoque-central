# 1. Introdução

Este documento descreve a arquitetura fullstack completa do **Estoque Central**, um sistema ERP omnichannel brasileiro para gestão de inventário, integrando vendas físicas (PDV), vendas online (B2B/B2C), marketplaces (Mercado Livre), e emissão de documentos fiscais (NFCe via Focus NFe).

O sistema é multi-tenant com isolamento de dados por schema PostgreSQL, implementado como um monorepo híbrido com backend Java/Spring Boot e frontend Angular standalone.

---

## 1.1. Starter Template ou Projeto Existente

**Status:** Projeto greenfield com scaffolding customizado

Este é um **projeto novo (greenfield)** que será desenvolvido do zero. Não existe código-base legado ou sistema anterior.

### **Scaffolding Customizado**

O projeto utiliza scaffolding customizado combinando:

1. **Backend (Java/Spring Boot):**
   - Geração inicial via **Spring Initializr** (start.spring.io)
   - Estrutura customizada com **Spring Modulith** para bounded contexts
   - Arquitetura hexagonal (ports & adapters) organizada por módulos de domínio

2. **Frontend (Angular):**
   - Geração via **Angular CLI** (v17+)
   - **Standalone components** (sem NgModules)
   - Estrutura feature-based com lazy loading

3. **Infraestrutura:**
   - Docker Compose para desenvolvimento local
   - Azure Bicep para provisionamento de infraestrutura cloud
   - Scripts de automação para setup e deployment

### **Decisões de Scaffolding**

- ✅ **Single Maven Module + Spring Modulith (packages)**: Evita complexidade desnecessária de Maven multi-module para MVP
- ✅ **Monorepo híbrido**: Backend (Maven) e frontend (npm) no mesmo repositório Git, mas com builds independentes
- ✅ **Schema-per-tenant**: Isolamento total de dados por tenant (não row-level com tenant_id)
- ✅ **Angular Standalone Components**: Sem NgModules, component-level lazy loading
- ✅ **Spring Data JDBC**: Queries explícitas, sem Hibernate ORM magic

---

## 1.2. Architecture Change Log

Registro de mudanças significativas na arquitetura durante o desenvolvimento:

| Data | Versão | Mudança | Razão |
|------|--------|---------|-------|
| 2025-01-27 | v1.0 | Arquitetura inicial definida | Greenfield project kickoff |
| 2025-01-27 | v1.1 | Escolha de **Single Maven Module** em vez de Maven Multi-Module | Simplificar build para MVP, Spring Modulith suficiente para boundaries |
| 2025-01-27 | v1.2 | Confirmação de **Schema-per-tenant** como estratégia multi-tenancy | Melhor isolamento de dados, compliance com LGPD, performance |
| - | - | _Mudanças futuras serão registradas aqui_ | - |

---

## 1.3. Architectural Decisions Summary

Esta seção consolida todas as decisões arquiteturais críticas do projeto para facilitar revisão e onboarding.

### **Core Architecture Decisions**

| Category | Decision | Version | Rationale | Status |
|----------|----------|---------|-----------|--------|
| **Backend Framework** | Spring Boot | 3.5+ | Maturidade enterprise, ecossistema rico, starter template, Spring Modulith | ✅ Approved |
| **Backend Language** | Java | 21 LTS | Type safety, Virtual Threads para concorrência, ecossistema maduro | ✅ Approved |
| **Backend Architecture** | Spring Modulith | 1.1+ | Boundaries claros, event-driven, extração futura p/ microserviços, ArchUnit tests | ✅ Approved |
| **Backend Data Access** | Spring Data JDBC | 3.5+ | Menos overhead que Hibernate, queries explícitas, controle total, mapping DDD | ✅ Approved |
| **Backend Security** | Spring Security | 6.3+ | OAuth2 integration, JWT support, enterprise-grade | ✅ Approved |
| **Frontend Framework** | Angular | 19+ (LTS) ou 20 | Signals, standalone components, enterprise support, TypeScript native | ✅ Approved |
| **Frontend Language** | TypeScript | 5.8+ | Type safety crítico para domínio complexo | ✅ Approved |
| **Frontend UI Library** | Angular Material | 19+ | Components prontos, acessibilidade, tema customizável | ✅ Approved |
| **Frontend State** | Angular Signals | 19+ | Reactive state management nativo, performance | ✅ Approved |
| **Database** | PostgreSQL | 17+ ou 18 | ACID garantido, JSON support, full-text search, performance, custo Azure | ✅ Approved |
| **Cache & Queue** | Redis | 8.0+ | Cache in-memory, DelayedQueue (NFCe retry), pub/sub, Redisson client | ✅ Approved |
| **Multi-tenancy Strategy** | Schema-per-tenant | - | Isolamento total de dados, LGPD compliance (deletar schema = esquecimento), backups independentes, AbstractRoutingDataSource | ✅ Approved |
| **Authentication** | Google OAuth 2.0 | - | UX simplicity (sem senha), familiar para usuários BR, Spring Security integration | ✅ Approved |
| **Authorization** | JWT + RBAC (3 layers) | - | Stateless tokens, Role → Profile → User hierarquia, validação frontend + backend | ✅ Approved |
| **API Pattern** | REST (Spring MVC) | - | Maturidade, tooling, OpenAPI support, familiar para integrações | ✅ Approved |
| **Database Migration** | Flyway | 10+ | Versionamento SQL, multi-tenancy support (executar em cada schema) | ✅ Approved |
| **Build Tool (Backend)** | Maven | 3.9+ | Padrão enterprise, plugins maduros, convenção sobre configuração | ✅ Approved |
| **Build Tool (Frontend)** | npm + Angular CLI | 10+ | Tooling oficial Angular, ecossistema npm | ✅ Approved |
| **Runtime (Frontend Build)** | Node.js | 22 LTS ou 24 LTS | Runtime para build Angular, versões LTS para estabilidade | ✅ Approved |
| **Repository Structure** | Monorepo híbrido | - | Refactoring cross-stack facilitado, versionamento único (tag = deploy completo) | ✅ Approved |
| **Containerization** | Docker multi-stage | 24+ | Ambiente consistente dev/prod, portabilidade cloud, multi-stage otimiza tamanho | ✅ Approved |
| **Cloud Provider** | Azure | - | Decisão de negócio, familiaridade do time, Application Insights para Java | ✅ Approved |
| **Cloud Compute** | Azure Container Apps | - | Managed containers, auto-scaling, pricing por consumo | ✅ Approved |
| **Cloud Database** | Azure Database for PostgreSQL | Flexible Server | Managed service, backups automáticos, HA disponível | ✅ Approved |
| **Cloud Cache** | Azure Cache for Redis | - | Managed Redis, clustering, persistence | ✅ Approved |
| **Cloud CDN** | Azure Front Door | - | CDN global, WAF integrado, load balancing | ✅ Approved |
| **IaC** | Azure Bicep | - | Native Azure IaC, type-safe, menos verbose que ARM templates | ✅ Approved |
| **CI/CD** | GitHub Actions | - | Integração GitHub, workflows YAML, Azure deployment actions | ✅ Approved |
| **Monitoring & APM** | Azure Application Insights | - | APM nativo Azure, distributed tracing, metrics, alertas | ✅ Approved |
| **Logging** | SLF4J + Logback | 1.4+ | Padrão Java logging, structured logging (JSON), integração Application Insights | ✅ Approved |
| **Testing (Backend Unit)** | JUnit 5 + Mockito | 5.10+, 5.8+ | Padrão Java, maturidade, Spring support | ✅ Approved |
| **Testing (Backend Integration)** | Testcontainers | 1.19+ | PostgreSQL real em testes (não H2 mock), Redis real | ✅ Approved |
| **Testing (Architecture)** | ArchUnit | 1.2+ | Enforce hexagonal boundaries, Spring Modulith rules | ✅ Approved |
| **Testing (Frontend Unit)** | Jasmine + Karma | - | Padrão Angular, built-in | ✅ Approved |
| **Testing (E2E)** | Playwright | 1.40+ | Cross-browser, fast, debugging tools | ✅ Approved |

### **External Integrations**

| Integration | API/Service | Version | Authentication | Purpose |
|-------------|-------------|---------|----------------|---------|
| **Autenticação Usuário** | Google OAuth 2.0 | - | OAuth2 Authorization Code Flow | Login sem senha, delegação autenticação |
| **Marketplace** | Mercado Livre API | v2.0 | OAuth2 | Sync produtos, importação pedidos, atualização estoque |
| **Emissão Fiscal** | Focus NFe API | v2 | API Key | Emissão NFCe, consulta status, cancelamento |

### **Key Architectural Patterns**

| Pattern | Description | Reference |
|---------|-------------|-----------|
| **Hexagonal Architecture (Ports & Adapters)** | Domain no centro, adapters em/out isolados | `11-backend-architecture.md`, `17-coding-standards.md` |
| **Event-Driven (Spring Modulith)** | Comunicação entre módulos via events assíncronos | `11-backend-architecture.md:260-286` |
| **NFCe Retry Queue com Exponential Backoff** | Fila Redisson DelayedQueue, 10 tentativas, 30s → 4h | `08-core-workflows.md:28-72` |
| **Schema-per-Tenant com Routing Datasource** | AbstractRoutingDataSource + TenantContext ThreadLocal | `11-backend-architecture.md:204-258` |
| **Money Value Object** | Tipo forte para valores monetários (centavos em Long) | `04-data-models.md:127-149` |
| **Strongly-Typed IDs** | ProdutoId, ClienteId, etc ao invés de UUID raw | `04-data-models.md:152-176` |
| **Repository Pattern (Port Out)** | Interfaces em `domain.port.out`, implementações em `adapter.out.persistence` | `11-backend-architecture.md:28-74` |
| **Use Case / Application Service** | Orquestração lógica de negócio em `application` | `11-backend-architecture.md:76-119` |
| **Batch Import (Preview + Confirm)** | Two-phase import com validação collect-all-errors | `17-coding-standards.md:706-1037` |

### **Key NFR Targets**

| NFR | Target | Measurement | Rationale |
|-----|--------|-------------|-----------|
| **Availability** | 99.5% uptime | (successful_requests / total_requests) * 100 | SLA para clientes, max 3.6h downtime/mês |
| **Performance (p95 latency)** | < 200ms | percentile(request_duration, 95) | UX responsiva, PDV rápido |
| **PDV Checkout Time** | < 30s | Scan item → NFCe emitida (5 itens típicos) | Velocidade crítica para caixa |
| **Database Query Performance** | < 50ms (p95) | percentile(db_query_duration, 95) | Evitar bottleneck em transações |
| **ML Sync Latency** | < 5 min | time_diff(venda_local, sync_ml) | Prevenir overselling marketplace |
| **Stock Accuracy** | > 99% | (estoque_sistema == estoque_físico) / total_auditorias | Confiabilidade crítica para negócio |
| **Error Rate** | < 0.1% | (failed_requests / total_requests) * 100 | Confiabilidade operacional |
| **Backup RPO** | 24h | Time since last successful backup | Tolerância a perda de dados |
| **Backup RTO** | 4h | Time to restore from backup | Tempo máximo de indisponibilidade |

### **Version Verification**

> **Última verificação**: 2025-01-30 (via WebSearch)
> **Próxima revisão**: 2025-04-30 (trimestral)

Todas as versões de tecnologias foram verificadas via WebSearch em 2025-01-30 para garantir uso de versões atuais, seguras e com suporte LTS quando aplicável.

---

## 1.4. Visão Geral do Sistema

### **Contexto de Negócio**

O **Estoque Central** é um sistema ERP para pequenas e médias empresas brasileiras que precisam:

- Gerenciar inventário unificado entre loja física e online
- Integrar vendas de marketplace (Mercado Livre)
- Emitir documentos fiscais (NFCe) com retry automático
- Controlar compras com custo médio ponderado
- Operar PDV com ou sem internet (offline-first)

### **Principais Funcionalidades**

1. **Gestão de Produtos**: Produtos simples, variantes, compostos (kits)
2. **Controle de Estoque**: Movimentações, reservas, custo médio ponderado
3. **Vendas Multi-canal**: PDV, B2B, B2C com regras de pricing diferentes
4. **Compras**: Ordens de compra, recebimento de mercadoria
5. **Integração Mercado Livre**: Sync de produtos, importação de pedidos
6. **Emissão Fiscal**: NFCe com retry queue (até 10 tentativas)
7. **Multi-tenancy**: Isolamento completo por tenant

### **Usuários do Sistema**

- **Gerente/Admin**: Configuração, relatórios, gestão de usuários
- **Operador de PDV**: Vendas presenciais com emissão de NFCe
- **Estoquista**: Recebimento de mercadoria, contagem de estoque
- **Vendedor B2B**: Pedidos online para clientes empresariais

---

## 1.4. Objetivos Arquiteturais

### **Requisitos Não-Funcionais Críticos**

| Requisito | Meta | Como Atingimos |
|-----------|------|----------------|
| **Multi-tenancy** | Isolamento total de dados | Schema-per-tenant PostgreSQL |
| **Performance** | < 200ms p95 para leitura | Indexes, Redis cache, Connection pooling |
| **Disponibilidade** | 99.5% uptime | Azure Container Apps auto-scaling |
| **Resiliência** | Retry automático de NFCe | Redisson DelayedQueue com backoff exponencial |
| **Segurança** | OAuth2 + JWT | Google OAuth + custom JWT para API |
| **Escalabilidade** | Suportar 50+ tenants | Horizontal scaling, schema isolation |
| **Manutenibilidade** | Boundaries claros | Spring Modulith + Hexagonal Architecture |

### **Princípios Arquiteturais**

1. **Domain-Driven Design (DDD)**: Bounded contexts como Spring Modulith modules
2. **Hexagonal Architecture**: Domínio isolado de frameworks (ports & adapters)
3. **Event-Driven Communication**: Comunicação assíncrona entre módulos via Spring ApplicationEventPublisher
4. **API-First**: OpenAPI spec como contrato, geração automática de clients
5. **Infrastructure as Code**: Azure Bicep para provisionamento declarativo
6. **Testability**: Testes em todas as camadas (unit, integration, e2e)

---

## 1.5. Escopo Deste Documento

Este documento de arquitetura cobre:

- ✅ Arquitetura de alto nível (componentes, integrações)
- ✅ Tech stack completo (frontend, backend, infra)
- ✅ Modelos de dados (TypeScript interfaces + PostgreSQL schema)
- ✅ API specification (OpenAPI 3.0)
- ✅ Arquitetura de frontend (Angular standalone components)
- ✅ Arquitetura de backend (Spring Modulith hexagonal)
- ✅ Estratégia de deployment (Azure Container Apps)
- ✅ Segurança, performance, testing, monitoring
- ✅ Coding standards para agentes de desenvolvimento

**Fora do escopo:**
- ❌ Regras de negócio detalhadas (ver PRD)
- ❌ User stories e critérios de aceitação (ver Epics)
- ❌ Design de UX/UI (ver Figma prototypes)
- ❌ Plano de projeto e cronograma

---

## 1.6. Público-Alvo

Este documento é destinado a:

- **Desenvolvedores**: Implementar features seguindo a arquitetura
- **Arquitetos**: Revisar decisões técnicas
- **Agentes de IA**: Seguir padrões e conventions (especialmente seção "Coding Standards")
- **DevOps/SRE**: Entender deployment e operação
- **Tech Leads**: Onboarding de novos membros

---

**Próximas Seções:**

- **Seção 2**: High Level Architecture
- **Seção 3**: Tech Stack
- **Seção 4**: Data Models
- ... (total de 19 seções)
