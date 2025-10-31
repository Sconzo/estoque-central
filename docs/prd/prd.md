# Estoque Central Product Requirements Document (PRD)

---

## Goals and Background Context

### Goals

- Eliminar overselling entre múltiplos canais de venda (loja física e marketplaces)
- Reduzir em 70%+ o tempo operacional gasto em controle manual de estoque
- Proporcionar visibilidade em tempo real de estoque disponível considerando todos os canais
- Garantir compliance fiscal automático com emissão de NFCe integrada ao fluxo de vendas
- Habilitar expansão para novos marketplaces sem aumentar headcount proporcionalmente
- Fornecer dados confiáveis para decisões estratégicas de compra e precificação

### Background Context

O **Estoque Central** surge para resolver um problema crítico enfrentado por PMEs brasileiras omnichannel: a gestão fragmentada de estoque entre loja física e marketplaces resulta em overselling, retrabalho manual extensivo e perda de oportunidades de crescimento. Atualmente, empresas que vendem simultaneamente no Mercado Livre e em lojas físicas precisam atualizar manualmente planilhas Excel, reconciliar vendas de múltiplos sistemas e enfrentam cancelamentos frequentes devido à falta de sincronização em tempo real.

Este PRD define um sistema ERP web unificado que centraliza gestão de estoque multi-depósito, interfaces de venda especializadas (PDV touchscreen para varejo e Ordem de Venda para distribuição B2B), gestão de compras com recebimento mobile, e integração nativa com marketplaces brasileiros (iniciando com Mercado Livre no MVP). O diferencial competitivo está na abordagem "omnichannel-first" com single source of truth para inventário, eliminando a necessidade de sincronização entre sistemas separados e garantindo compliance fiscal brasileiro como feature core, não add-on.

### Change Log

| Date | Version | Description | Author |
|------|---------|-------------|--------|
| 2025-10-25 | 1.0 | Criação inicial do PRD baseado no Project Brief | John (PM Agent) |

---

## Requirements

### Functional

**Autenticação e Controle de Acesso:**

- **FR1:** O sistema deve permitir autenticação exclusivamente via Google OAuth 2.0, armazenando Google ID, email e nome do usuário
- **FR2:** O sistema deve implementar controle de acesso baseado em três camadas (Role, Profile, User) com validação de permissões tanto no backend (middleware em cada rota) quanto no frontend (guards Angular e diretivas de visibilidade)

**Gestão de Produtos:**

- **FR3:** O sistema deve suportar cadastro de três tipos de produtos: Simples (SKU único), Variantes (produto pai com matriz de variações como Cor x Tamanho, limitado a 3 atributos e 100 variantes por produto), e Compostos/Kit (BOM com lista de componentes)
- **FR4:** O sistema deve permitir importação de produtos via CSV/Excel com preview e validação, suportando produtos simples, variantes e compostos
- **FR5:** O sistema deve implementar categorias hierárquicas ilimitadas (árvore de categorias com self-reference) para organização de produtos
- **FR26:** O sistema deve prevenir produtos inativos de aparecerem em interfaces de venda (PDV, Ordem de Venda) e em sincronizações com marketplaces

**Gestão de Estoque Multi-Depósito:**

- **FR6:** O sistema deve gerenciar múltiplos locais de estoque com rastreamento de quantidade disponível, quantidade reservada e quantidade disponível para venda (disponível - reservada) por SKU/variante
- **FR7:** O sistema deve permitir transferências entre estoques com registro de origem, destino, motivo, responsável e atualização automática de histórico de movimentações
- **FR18:** O sistema deve alertar produtos com estoque abaixo do mínimo configurado (por produto/local)
- **FR20:** O sistema deve manter histórico completo de movimentações de estoque incluindo: tipo (entrada, saída, transferência, ajuste, venda, compra, reserva, montagem BOM), quantidade, data/hora, usuário responsável, documento relacionado (ID venda/compra/transferência) e saldos anterior/posterior

**PDV (Point of Sale):**

- **FR8:** O sistema deve fornecer interface PDV touchscreen otimizada com suporte a leitor de código de barras USB/Bluetooth, busca rápida de produtos e seleção de variantes via grade visual
- **FR9:** O sistema deve suportar vendas de produtos compostos no PDV, dando baixa automática nos componentes conforme BOM (virtual ou físico configurável)
- **FR22:** O sistema deve implementar fila de retry para emissões fiscais NFCe falhas com até 10 tentativas automáticas em intervalo crescente; após limite, marcar como "Falha Permanente - Intervenção Necessária"; interface de gestão "Vendas Pendentes de Emissão Fiscal" deve permitir: retry manual, cancelamento com estorno de estoque, emissão via contingência offline ou marcação como resolvido externamente; sistema deve notificar gerente/admin em falhas permanentes e impedir fechamento de caixa com vendas pendentes não resolvidas

**Ordem de Venda (B2B):**

- **FR10:** O sistema deve fornecer interface de Ordem de Venda para operações B2B com consulta de estoque em tempo real por local e reserva automática de estoque ao confirmar pedido
- **FR24:** O sistema deve liberar automaticamente reservas de estoque de Ordens de Venda não faturadas após 7 dias (prazo configurável por tenant)

**Gestão de Compras:**

- **FR11:** O sistema deve implementar gestão de compras com CRUD de fornecedores, criação de ordens de compra e interface de recebimento mobile com scanner de código de barras via câmera do celular
- **FR12:** O sistema deve calcular e atualizar custo médio ponderado automaticamente ao receber mercadoria via Ordem de Compra
- **FR19:** O sistema deve suportar CRUD de clientes (Pessoa Física/Jurídica) e fornecedores com dados completos necessários para emissão fiscal (CPF/CNPJ, inscrições, endereço completo)

**Integração Mercado Livre:**

- **FR13:** O sistema deve integrar com Mercado Livre via OAuth2 com sincronização bidirecional de produtos (incluindo variantes via mapeamento de atributos ML), estoque e pedidos
- **FR14:** O sistema deve processar pedidos do Mercado Livre identificando variantes vendidas, reservando/baixando estoque automaticamente e atualizando status de envio/entrega no ML
- **FR15:** O sistema deve sincronizar estoque com Mercado Livre automaticamente após vendas no PDV ou Ordem de Venda para prevenir overselling (latência máxima 5 minutos)
- **FR23:** O sistema deve processar cancelamentos importados do Mercado Livre atualizando estoque automaticamente (estorno de baixa ou liberação de reserva)
- **FR25:** O sistema deve implementar margem de segurança configurável para anúncios em marketplaces (ex: anunciar 90% do estoque real disponível, configurável por marketplace/categoria/produto)

**Relatórios e Dashboard:**

- **FR16:** O sistema deve fornecer dashboard com vendas do dia por canal, estoque total em valor, produtos em ruptura (abaixo do mínimo) e pedidos pendentes (ML + Ordens de Venda)
- **FR17:** O sistema deve gerar relatórios de: movimentações de estoque (filtros por período/produto/local/tipo), vendas por período/canal, produtos mais vendidos (unidade e valor), estoque atual por local/consolidado e curva ABC de produtos

**Arquitetura Multi-Tenant:**

- **FR21:** O sistema deve implementar arquitetura multi-tenancy com isolamento completo de dados por empresa/tenant (schema-per-tenant ou row-level security), garantindo que nenhum tenant acesse dados de outro

### Non-Functional

**Performance e Disponibilidade:**

- **NFR1:** O sistema deve manter uptime mínimo de 99.5% (máximo 3.6 horas de downtime por mês)
- **NFR2:** O PDV deve completar checkout (scan do primeiro item até emissão de NFCe) em menos de 30 segundos para transação típica de 5 itens
- **NFR3:** A busca de produtos deve retornar resultados em menos de 500ms
- **NFR6:** O carregamento inicial do PDV deve ocorrer em menos de 3 segundos
- **NFR7:** A interface PDV deve manter 60fps para garantir responsividade em touchscreen
- **NFR8:** O recebimento mobile deve carregar em menos de 5 segundos e reconhecer código de barras via câmera em menos de 2 segundos
- **NFR9:** Operações CRUD devem salvar em menos de 1 segundo
- **NFR17:** O sistema deve suportar importação em lote de até 1000 produtos em menos de 30 segundos com feedback de progresso

**Integrações:**

- **NFR4:** A sincronização com Mercado Livre deve ter latência máxima de 5 minutos em 95% das operações
- **NFR5:** A taxa de erro de sincronização com marketplaces deve ser inferior a 1% (99%+ de taxa de sucesso)

**Compatibilidade:**

- **NFR10:** O sistema deve funcionar nos navegadores modernos (últimas 2 versões): Chrome, Edge Chromium, Firefox e Safari
- **NFR11:** O sistema deve ser responsivo para desktop (Ordem de Venda, Configurações, Relatórios), tablet (PDV touchscreen) e mobile (Recebimento de Mercadoria)

**Confiabilidade e Compliance:**

- **NFR12:** A precisão de estoque deve ser de 99%+ em auditorias (acuracidade entre estoque físico e sistema), suportada por processos de inventário periódico
- **NFR13:** O sistema deve processar vendas com emissão de NFCe de forma atômica (venda registrada e estoque baixado em transação única; NFCe em fila de retry se falhar)
- **NFR16:** O sistema deve reter logs de auditoria fiscal (emissões, cancelamentos, inutilizações) por 5 anos conforme exigência SPED

**Segurança e Proteção de Dados:**

- **NFR14:** O sistema deve criptografar dados sensíveis (CPF, CNPJ, emails, dados bancários) em repouso usando AES-256
- **NFR15:** O sistema deve realizar backup automático diário com retenção de 30 dias e capacidade de recuperação (RPO: 24h, RTO: 4h)
- **NFR18:** O sistema deve implementar conformidade com LGPD incluindo: exportação de dados pessoais sob demanda, direito ao esquecimento (anonimização de dados após solicitação), registro de consentimento e base legal para tratamento de dados

---

## User Interface Design Goals

### Overall UX Vision

O Estoque Central deve oferecer experiências especializadas por contexto de uso, não uma interface única genérica. O PDV prioriza velocidade e minimalismo para operadores de caixa (3-5 toques para finalizar venda), a Ordem de Venda oferece robustez para vendedores B2B com visão completa de cliente e estoque, e o Recebimento Mobile otimiza para operação com uma mão em ambiente de depósito. A UX deve ser intuitiva o suficiente para usuários com familiaridade tecnológica média (usam Excel e WhatsApp Business), eliminando curva de aprendizado através de affordances claras e fluxos lineares.

### Key Interaction Paradigms

- **PDV:** Fluxo scan-to-pay otimizado com teclado numérico virtual, autocomplete agressivo na busca de produtos, e confirmações visuais imediatas (feedback haptic em tablets)
- **Ordem de Venda:** Formulário desktop tradicional com tabelas editáveis inline, consulta de estoque em tempo real sem trocar de tela, e histórico de cliente em sidebar contextual
- **Recebimento Mobile:** Camera-first para scanning de código de barras, entrada de quantidade via numpad touch otimizado, e confirmação por swipe gesture
- **Geral:** Notificações não-intrusivas (toast messages), validações inline (não bloqueantes), e estados de loading explícitos (skeleton screens, não spinners genéricos)

### Core Screens and Views

1. **Login** - Tela única com botão "Entrar com Google"
2. **Dashboard Principal** - Visão gerencial com cards de vendas do dia, estoque crítico, pedidos pendentes
3. **PDV (Touchscreen)** - Interface fullscreen otimizada para tablet com carrinho, busca e pagamento
4. **Ordem de Venda** - Formulário desktop com seleção de cliente, grid de produtos e totalizadores
5. **Catálogo de Produtos** - Listagem com filtros por categoria, tipo, status e busca avançada
6. **Cadastro de Produto** - Wizard multi-step para produtos simples/variantes/compostos
7. **Gestão de Estoque** - Visão multi-depósito com drill-down por local/produto
8. **Transferência entre Estoques** - Formulário simplificado origem/destino com validações
9. **Recebimento Mobile** - Interface mobile-first com scanner e lista de OCs pendentes
10. **Ordens de Compra** - CRUD desktop com entrada de produtos e fornecedores
11. **Integração Mercado Livre** - Painel de configuração OAuth, sincronização e mapeamento
12. **Vendas Pendentes NFCe** - Interface de gestão de filas de retry com ações de resolução
13. **Relatórios** - Filtros interativos com preview de dados e exportação CSV/PDF
14. **Configurações** - Gestão de usuários, profiles, roles e locais de estoque

### Accessibility

**WCAG AA** - Contraste mínimo de 4.5:1 para textos, suporte a navegação por teclado (tab order lógico), labels ARIA para screen readers, e tamanhos de touch target mínimo de 44x44px conforme guidelines mobile.

### Branding

**Material Design com identidade brasileira** - Cores primárias em tons de azul corporativo (confiança/tecnologia), verde para indicadores positivos (estoque ok, sincronização bem-sucedida) alinhado com expectativa cultural brasileira. Tipografia: Roboto (padrão Material) por ser familiar e legível em telas de diferentes resoluções. Ícones: Material Icons com customizações para contexto brasileiro (ex: ícone de NFCe, Mercado Livre).

### Target Devices and Platforms

**Web Responsive + Progressive Web App (PWA):**
- **Desktop/Laptop (primário):** Ordem de Venda, Configurações, Relatórios, Cadastros (1366x768+ resolução mínima)
- **Tablet 10" touchscreen (primário):** PDV fullscreen em modo landscape (1280x800 típico)
- **Smartphone (Android/iOS) (primário):** Recebimento Mobile com câmera para scanning (375x667+ resolução mínima)

**Browsers:** Chrome/Edge Chromium (prioridade 1), Firefox/Safari (prioridade 2)

---

## Technical Assumptions

Baseado nos requisitos levantados e decisões técnicas do time, seguem as escolhas que guiarão a arquitetura:

### Repository Structure

**Monorepo** - Repositório único contendo frontend (Angular), backend (Spring Boot), e módulos compartilhados (DTOs, contracts).

**Rationale:**
- Facilita refactoring cross-stack (mudança em contract impacta front e back simultaneamente)
- Simplifica versionamento (uma tag = deploy completo)
- Reduz overhead de coordenação entre repos
- Ferramentas modernas (Nx, Maven multi-module) resolvem problemas de performance

**Trade-off:** Menos flexibilidade para deploy independente de componentes, mas para MVP isso não é necessário.

### Service Architecture

**Monolito Modular com Spring Modulith** - Aplicação backend única organizada em módulos bem definidos (Produtos, Estoque, Vendas, Compras, Integrações, Fiscal) usando Spring Modulith para garantir boundaries entre módulos, com possibilidade futura de extrair módulos para microserviços se necessário.

**Rationale:**
- MVP não justifica complexidade de microserviços (overhead de comunicação, distributed transactions, observability)
- Spring Modulith força boundaries claros entre módulos (package-private, events entre módulos)
- Facilita extração futura para microserviços (módulos já são loosely coupled)
- Melhor performance (chamadas in-process vs network)
- Documentação automática de módulos e dependências

**Trade-off:** Escalabilidade horizontal limitada, mas para 30 clientes MVP isso é non-issue.

### Testing Requirements

**Unit + Integration + E2E seletivo** - Pirâmide de testes com foco em:
- **Unit:** Lógica de negócio crítica (cálculo de custo médio, reserva de estoque, BOM) com JUnit 5 + Mockito
- **Integration:** Módulos Spring Modulith, repositories JPA, APIs REST com @SpringBootTest + Testcontainers (PostgreSQL real)
- **E2E:** User journeys críticos (venda no PDV com NFCe, sincronização ML, recebimento mobile) com Playwright ou Cypress
- **Modularity Tests:** Spring Modulith verifica boundaries entre módulos automaticamente
- **Manual:** Testes de integração fiscal (NFCe real com homologação SEFAZ) devido a complexidade

**Rationale:** Cobertura balanceada entre confiança e velocidade de desenvolvimento. Testcontainers garante testes de integração com PostgreSQL real sem mocks.

### Additional Technical Assumptions and Requests

**Frontend:**
- **Framework:** Angular 17+ (Standalone Components, Signals)
- **UI Library:** Angular Material (Material Design 3)
- **State Management:** RxJS + Signals (NgRx apenas se complexidade crescer)
- **Biblioteca de Código de Barras:** ZXing para scanning via câmera mobile
- **PWA:** Service Worker para cache e eventual modo offline (Fase 2)
- **Build:** Angular CLI com Nx para otimizações de monorepo

**Backend:**
- **Runtime:** Java 25 (LTS features, Virtual Threads para concorrência)
- **Framework:** Spring Boot 3.3+ (baseline para Spring Modulith)
- **Modularity:** Spring Modulith (módulos bem definidos, events, ArchUnit tests)
- **Persistência:** Spring Data JDBC (leve, menos overhead que Hibernate, controle explícito)
- **Validação:** Jakarta Validation (Bean Validation) com Hibernate Validator
- **API Style:** REST (Spring MVC com @RestController, HATEOAS se necessário)
- **Build Tool:** Maven 3.9+ (padrão enterprise, XML, ecossistema maduro)

**Banco de Dados:**
- **Primary:** PostgreSQL 15+ (ACID, JSON support, performance)
- **Multi-tenancy:** Schema-per-tenant com AbstractRoutingDataSource do Spring (isolamento completo, backups independentes)
- **Migrations:** Flyway (versionamento SQL, suporte multi-tenancy)
- **Cache:** Redis (sessões, filas de retry NFCe, cache de sincronização ML)

**Integrações:**
- **Mercado Livre SDK:** Cliente HTTP customizado com RestTemplate ou WebClient (SDK oficial é Node.js, faremos adapter Java)
- **NFCe/SAT:** Middleware terceiro via REST (Focus NFe ou NFe.io) - evita complexidade de certificado digital A1/A3 em Java
- **OAuth2:** Spring Security OAuth2 Client com Google Provider

**Message Queue / Workers:**
- **Queue:** Redis com Redisson (distributed locks, queues, pub/sub)
- **Scheduling:** Spring @Scheduled com ShedLock (distributed locking para evitar execução duplicada em múltiplas instâncias)
- **Workers/Background Jobs:**
  - Sincronização ML (polling/webhooks com @Scheduled)
  - Fila de retry NFCe (Redisson DelayedQueue)
  - Jobs agendados (liberação de reservas, alertas de estoque)

**Containerization:**
- **Docker:** Containerização da aplicação com multi-stage builds (build Maven + runtime JRE slim)
- **Docker Compose:** Orquestração local para desenvolvimento (app + PostgreSQL + Redis)
- **Base Image:** Eclipse Temurin JRE 21-alpine (oficial, segura, otimizada)

**Deployment - Azure:**
- **Compute:** Azure Container Apps ou Azure App Service for Containers (deployment com Docker images)
- **Container Registry:** Azure Container Registry (ACR) para armazenar imagens Docker
- **Database:** Azure Database for PostgreSQL - Flexible Server (managed, backups automáticos, HA)
- **Cache:** Azure Cache for Redis (managed, clustering, persistence)
- **Storage:** Azure Blob Storage (uploads de CSVs, exports de relatórios, logs)
- **CDN:** Azure Front Door (CDN + WAF + load balancing global)
- **CI/CD:** GitHub Actions (build Maven, test, build Docker image, push para ACR, deploy container para Azure)

**Observability:**
- **Logging:** SLF4J + Logback (structured logs JSON)
- **Monitoring:** Azure Monitor + Application Insights (APM, distributed tracing, metrics)
- **Error Tracking:** Application Insights (exception tracking, alertas)
- **Modularity Observability:** Spring Modulith events tracking

**Segurança:**
- **Secrets:** Azure Key Vault (certificados fiscais, tokens ML, connection strings)
- **Encryption at Rest:** Azure Database encryption enabled, Blob Storage encrypted
- **HTTPS:** Azure App Service com Azure-managed certificate (Let's Encrypt via App Service)
- **WAF:** Azure Front Door WAF (proteção DDoS, SQL injection, OWASP Top 10)
- **Identity:** Azure AD para gerenciamento de service principals e managed identities

**Starter Template:**
Scaffolding customizado baseado em:
- Spring Initializr para estrutura backend (Spring Boot, Spring Data JDBC, Spring Security, Spring Modulith)
- Angular CLI para estrutura frontend
- Maven multi-module para organizar monorepo
- Dockerfile multi-stage para build e runtime otimizado

**Rationale:** Templates prontos trazem dependências desnecessárias e opiniões arquiteturais rígidas. Melhor começar lean e adicionar conforme necessário.

---

### Decisões Críticas - Rationale Detalhado

**Por que Java 25 + Spring Boot (vs Node.js)?**
- ✅ Ecossistema enterprise maduro para ERPs (Spring Data, Spring Security, transações)
- ✅ Type safety rigoroso (melhor para domínio complexo com BOMs, multi-tenancy)
- ✅ Performance e escalabilidade (Virtual Threads do Java 21+ para concorrência)
- ✅ Spring Modulith force boundaries claros entre módulos (melhor manutenibilidade)
- ✅ Facilita contratar devs Java no Brasil (mercado grande)
- ❌ Não compartilha types diretamente com Angular, mas DTOs/OpenAPI resolvem

**Por que Spring Modulith (vs microserviços ou monolito tradicional)?**
- ✅ Módulos verificados em compile-time (ArchUnit tests automáticos)
- ✅ Comunicação entre módulos via events (desacoplamento, auditoria)
- ✅ Documentação automática de dependências entre módulos
- ✅ Preparado para extração futura para microserviços (módulos já são bounded contexts)
- ❌ Framework relativamente novo (2023), mas mantido pelo Spring team

**Por que Spring Data JDBC (vs Hibernate/JPA)?**
- ✅ Menos "magia" (queries explícitas, sem lazy loading surpresas)
- ✅ Performance melhor (menos overhead, sem proxy objects)
- ✅ Mapping mais simples (agregados DDD naturalmente)
- ✅ Menos bugs relacionados a sessões/caching
- ❌ Menos features prontas (sem L2 cache, criteria API limitado), mas para MVP é adequado

**Por que PostgreSQL (vs SQL Server na Azure)?**
- ✅ ACID compliance crítico para transações financeiras/fiscais
- ✅ JSON support para metadados flexíveis (atributos de variantes)
- ✅ Performance excelente para workload relacional
- ✅ Custo menor que SQL Server no Azure
- ✅ Extensions (pg_cron para jobs, PostGIS futuro para logística)

**Por que Schema-per-tenant (vs Row-level security)?**
- ✅ Isolamento total (backup/restore por cliente)
- ✅ Performance previsível (índices não misturados)
- ✅ Compliance LGPD simplificado (deletar schema = deletar todos os dados)
- ✅ Spring AbstractRoutingDataSource suporta bem com ThreadLocal context
- ❌ Mais complexo gerenciar N schemas, mas Flyway + Spring facilitam

**Por que Azure (vs AWS/GCP)?**
- ✅ Decisão do time (familiaridade, requisito de projeto)
- ✅ Integração nativa com Azure AD para identidade
- ✅ App Service simplifica deploy para MVP
- ✅ Application Insights é excelente para observability Java
- ❌ Vendor lock-in, mas para MVP não é preocupação

**Por que Maven (vs Gradle)?**
- ✅ Padrão enterprise consolidado (familiaridade do time)
- ✅ Ecossistema de plugins maduro e estável
- ✅ Convenção sobre configuração (menos decisões, mais produtividade)
- ✅ Melhor suporte em IDEs e ferramentas de CI/CD
- ❌ Builds mais lentos que Gradle, mas para MVP é adequado

**Por que Docker?**
- ✅ Ambiente consistente (dev/staging/prod)
- ✅ Facilita deploy em qualquer cloud (portabilidade)
- ✅ Isolamento de dependências (JRE, bibliotecas nativas)
- ✅ Azure Container Apps oferece auto-scaling e managed containers
- ✅ Multi-stage builds otimizam tamanho da imagem (build layer vs runtime layer)

---

## Epic List

**Epic 1: Foundation & Core Infrastructure**
Estabelecer infraestrutura do projeto (monorepo Maven multi-module + Spring Boot + Angular), multi-tenancy schema-per-tenant, autenticação Google OAuth 2.0, sistema de Roles/Profiles/Users, Dockerfile multi-stage, CI/CD com GitHub Actions e health check endpoint deployável na Azure via container.

**Epic 2: Product Catalog & Inventory Foundation**
Implementar gestão completa de catálogo de produtos (categorias hierárquicas, produtos simples/variantes/compostos com BOM), CRUD de locais de estoque, controle multi-depósito com rastreamento de quantidade disponível/reservada e histórico completo de movimentações.

**Epic 3: Purchasing & Inventory Replenishment**
Habilitar fluxo de reposição de estoque com CRUD de fornecedores, criação de Ordens de Compra, recebimento mobile com scanner de código de barras via câmera, cálculo automático de custo médio ponderado e transferências entre estoques com rastreabilidade.

**Epic 4: Sales Channels - PDV & B2B**
Implementar canais de venda com CRUD de clientes (PF/PJ), interface PDV touchscreen com emissão NFCe e fila de retry para falhas, interface de Ordem de Venda B2B com reserva automática de estoque e consulta em tempo real.

**Epic 5: Marketplace Integration - Mercado Livre**
Integrar com Mercado Livre via OAuth com sincronização bidirecional de produtos (incluindo variantes), estoque e pedidos, processamento de cancelamentos, margem de segurança configurável e prevenção de overselling através de sincronização automática pós-vendas.

**Epic 6: Reporting & Analytics**
Fornecer visibilidade operacional com dashboard gerencial (vendas do dia por canal, estoque crítico, pedidos pendentes), relatórios de movimentações de estoque, vendas por período/canal, produtos mais vendidos, estoque atual por local, curva ABC e alertas de ruptura.

---

## Epic Details

Os épicos estão detalhados em arquivos separados em `docs/prd/`:

- [Epic 1: Foundation & Core Infrastructure](prd/epic-01-foundation.md) - 7 stories
- [Epic 2: Product Catalog & Inventory Foundation](prd/epic-02-product-catalog.md) - 9 stories
- [Epic 3: Purchasing & Inventory Replenishment](prd/epic-03-purchasing.md) - 5 stories
- [Epic 4: Sales Channels - PDV & B2B](prd/epic-04-sales-channels.md) - 6 stories
- [Epic 5: Marketplace Integration - Mercado Livre](prd/epic-05-marketplace-integration.md) - 7 stories
- [Epic 6: Reporting & Analytics](prd/epic-06-reporting.md) - 7 stories

**Total: 6 Épicos, 41 User Stories**

---
