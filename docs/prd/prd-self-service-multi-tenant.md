---
stepsCompleted: [1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11]
inputDocuments:
  - 'd:\workspace\estoque-central\docs\analysis\product-brief-Estoque Central-2025-12-21.md'
  - 'd:\workspace\estoque-central\docs\brief\brief.md'
  - 'd:\workspace\estoque-central\docs\MULTI-TENANCY.md'
  - 'd:\workspace\estoque-central\docs\stories\1-3-postgresql-multi-tenancy-setup.md'
  - 'd:\workspace\estoque-central\docs\architecture\02-high-level-architecture.md'
  - 'd:\workspace\estoque-central\docs\architecture\09-database-schema.md'
  - 'd:\workspace\estoque-central\docs\stories\1-4-google-oauth-authentication.md'
documentCounts:
  briefs: 2
  research: 0
  brainstorming: 0
  projectDocs: 5
workflowType: 'prd'
workflowStatus: 'completed'
lastStep: 11
project_name: 'Estoque Central'
user_name: 'poly'
date: '2025-12-21'
completedDate: '2025-12-21'
---

# Product Requirements Document - Estoque Central

**Author:** poly
**Date:** 2025-12-21

## Executive Summary

O Estoque Central enfrenta um gargalo crítico de escalabilidade: a criação manual de tenants para novos clientes impede o crescimento ágil da plataforma. Esta funcionalidade transforma o processo de onboarding de empresas, permitindo que novos clientes se auto-cadastrem via interface web, com criação automática de tenant e schema PostgreSQL isolado. Além disso, habilita usuários a participarem de múltiplas empresas simultaneamente, com troca de contexto fluida sem necessidade de múltiplos logins.

**Problema Atual:**
Cada novo cliente (empresa) precisa ter seu tenant e schema PostgreSQL criados manualmente via API, gerando:
- Gargalo operacional: atraso no onboarding de novos clientes
- Falta de autonomia: clientes dependem da equipe técnica para começar
- Limitação de escala: impossível crescer rapidamente com processo manual
- Experiência fragmentada: usuários que atuam em múltiplas empresas precisam fazer múltiplos logins

**Solução Proposta:**
Plataforma de self-service multi-tenant onboarding que permite:
1. **Criação autônoma de empresas**: Após login via Google OAuth, usuários sem empresa são direcionados para tela de cadastro que cria automaticamente tenant + schema isolado
2. **Gestão inteligente de contexto**: Usuários vinculados a múltiplas empresas escolhem qual acessar após login, com capacidade de trocar contexto via avatar (sem re-login)
3. **Sistema de convites e colaboração**: Admins convidam colaboradores por email, com gestão de permissões flexíveis
4. **Tratamento robusto de erros**: Processo crítico com loading states, registro de falhas, e mecanismos de proteção

### What Makes This Special

**Zero-Friction Onboarding**
De login a empresa operacional em minutos (não dias). Elimina completamente a barreira técnica de entrada, permitindo que empresas comecem a usar o sistema imediatamente após autenticação Google.

**True Multi-Tenancy**
Isolamento completo por schema PostgreSQL (não apenas lógico). Cada empresa tem seu próprio schema de banco de dados, garantindo segurança máxima, compliance com LGPD, e possibilidade de backup/restore independente por cliente.

**Contexto Fluido**
Troca entre empresas sem re-autenticação. Usuários como Joaquin (que gerencia pizzaria e sorveteria) podem alternar entre contextos com um clique, mantendo produtividade sem fricção de múltiplos logins.

**Escalabilidade Desacoplada**
Arquitetura que permite crescimento comercial exponencial sem necessidade de crescimento proporcional da equipe técnica. Cada novo cliente se auto-provisiona, removendo o gargalo operacional.

## Project Classification

**Technical Type:** SaaS B2B Platform
**Domain:** General Software
**Complexity:** Low (leveraging existing infrastructure)
**Project Context:** Brownfield - extending existing system

**Contexto Técnico Existente:**

O Estoque Central já possui a fundação técnica necessária para esta funcionalidade:

- **Multi-tenancy Infrastructure**: Schema-per-tenant PostgreSQL com `TenantContext`, `TenantInterceptor`, e `AbstractRoutingDataSource` parcialmente implementados (Story 1.3)
- **Authentication**: Google OAuth 2.0 com JWT customizado contendo `tenantId` + `roles` já funcional (Story 1.4)
- **Tech Stack**: Spring Boot 3.3+ (Hexagonal Architecture + Spring Modulith), Angular 17+, PostgreSQL 16+, Redis
- **Database Schema**: Tabela `public.tenants` para metadata, schemas isolados `tenant_{uuid}` para dados de negócio

**Esta funcionalidade adiciona:**
- Interface web para criação self-service de empresas (frontend Angular)
- Endpoint público para cadastro de tenant sem autenticação prévia
- Fluxo de seleção/troca de empresa no frontend
- Sistema de gestão de colaboradores e convites
- Tratamento de erros e estados de provisionamento

A implementação aproveita toda infraestrutura existente, focando em expor e orquestrar capacidades já disponíveis através de uma experiência de usuário intuitiva e autônoma.


## Success Criteria

### User Success

**Onboarding Success:**
- **Time-to-first-company**: 95% dos usuários criam sua primeira empresa em menos de 10 minutos após login
- **Completion rate**: 90% dos usuários que iniciam o fluxo de criação de empresa completam com sucesso
- **Error rate**: Menos de 1% de falhas na criação de tenant/schema
- **Self-sufficiency**: 95% dos usuários completam criação de empresa + convite de colaboradores sem suporte técnico

**Engagement & Adoption:**
- **Multi-company adoption**: 30% dos usuários ativos criam 2+ empresas nos primeiros 90 dias
- **Company switching**: Usuários multi-empresa trocam de contexto 5+ vezes por semana (indicando uso real)
- **Collaboration success**: 80% dos usuários convidam pelo menos 1 colaborador nos primeiros 30 dias
- **Invitation acceptance**: 70% dos convites são aceitos em até 7 dias (quando sistema de notificações for implementado)

**Retention & Value:**
- **D7 retention**: 85% dos usuários que criam empresa retornam na semana seguinte
- **D30 retention**: 70% dos usuários mantêm uso ativo após 30 dias
- **Feature adoption**: 60% dos usuários multi-empresa utilizam troca de contexto sem re-login semanalmente

### Business Success

**3 Meses (Curto Prazo):**
- **Redução de fricção operacional**: Zero intervenções manuais para criação de tenant (vs. 100% manual hoje)
- **Time-to-activation**: Reduzir de dias para minutos o tempo de ativação de novos clientes
- **Cost efficiency**: Eliminar 100% do tempo de equipe técnica gasto em provisionamento manual
- **Quality assurance**: Menos de 0.5% de tenants órfãos ou com problemas de provisionamento

**12 Meses (Médio Prazo):**
- **Growth acceleration**: 3x no número de novas empresas criadas mensalmente
- **User acquisition**: 500+ empresas ativas usando self-service onboarding
- **Multi-tenancy adoption**: 25% da base de usuários opera 2+ empresas
- **Viral growth**: 15% de crescimento via indicações (usuários convidam outros donos de empresa)
- **Platform readiness**: Infraestrutura suportando 1000+ tenants simultâneos sem degradação

**Strategic Impact:**
- **Market positioning**: Tornar Estoque Central referência em "ease of onboarding" para pequenas empresas
- **Scalability unlock**: Habilitar crescimento exponencial sem aumentar equipe de operações proporcionalmente
- **Competitive advantage**: Diferencial claro vs. concorrentes que requerem setup manual/demorado

### Technical Success

**Performance & Reliability:**
- **Tenant creation success rate**: > 99% de sucesso na criação de tenant/schema
- **Schema provisioning time**: < 30 segundos (P95) para criação completa do schema PostgreSQL
- **Context switch latency**: < 500ms para troca de contexto entre empresas
- **System availability**: 99.5% uptime durante horário comercial

**Data Integrity & Security:**
- **Zero data leakage**: Isolamento completo entre tenants (validado por testes de integração)
- **Schema isolation**: Cada tenant com schema PostgreSQL dedicado, sem compartilhamento de dados
- **Error logging**: Críticos erros registrados no schema público para análise
- **LGPD compliance**: Tratamento adequado de tenant órfão considerando proteção de dados

**Operational Excellence:**
- **Critical errors**: < 5 erros críticos registrados por semana
- **Support tickets**: Redução de 90% em tickets relacionados a onboarding vs. baseline manual
- **Failed provisioning recovery**: Mecanismo de proteção para tenants órfãos ou provisionamento incompleto

### Measurable Outcomes

**KPIs Primários:**
- **Sign-up to company created**: < 10 minutos (P95)
- **New companies per month**: Crescimento de 20% mês-a-mês
- **DAU/MAU ratio**: > 40% (indicando uso frequente)
- **Failed provisioning rate**: < 1%

**KPIs Secundários:**
- **Weekly company switches** (usuários multi-empresa): Média de 5+ trocas/semana
- **Collaboration invites sent per company**: Média de 3 convites nos primeiros 30 dias
- **Churn rate**: < 10% mensal

**Go/No-Go Decision Points:**
- Se < 80% completion rate → investigar fricções no fluxo
- Se > 2% error rate → pausar e corrigir problemas de infraestrutura
- Se < 60% D7 retention → revisar proposta de valor e onboarding

## Product Scope

### MVP - Minimum Viable Product

**Core Features (Deve funcionar agora):**

1. **Criação Self-Service de Empresa (Tenant)**
   - Tela de cadastro de empresa após login Google OAuth (para usuários sem empresa)
   - Campos básicos: Nome da empresa, CNPJ (opcional), dados de contato
   - Criação automática de tenant + schema PostgreSQL isolado
   - Loading state durante provisionamento
   - Registro de erros críticos no schema público
   - Usuário criador automaticamente vira admin da empresa

2. **Seleção e Troca de Contexto de Empresa**
   - Tela de seleção de empresa após login (para usuários vinculados a múltiplas empresas)
   - Opção "Criar nova empresa" sempre disponível
   - Troca de contexto via avatar (menu dropdown) sem re-login
   - Atualização de contexto em tempo real (usuário pode dar F5 e ver mudanças)
   - Latência < 500ms para troca de contexto

3. **Gestão de Colaboradores**
   - Admin convida colaboradores por email
   - Vinculação automática com status "ativo" (sem fluxo de aprovação no MVP)
   - Colaborador vê empresa vinculada após login ou refresh
   - Admin pode remover colaboradores
   - Admin pode promover colaboradores para admin
   - Suporte a múltiplos admins por tenant

4. **Gestão de Empresa**
   - Admin pode editar dados da empresa (nome, CNPJ, contato)
   - Admin pode deletar empresa (com proteção contra tenant órfão)
   - Visualização de lista de colaboradores e seus perfis

5. **Sistema de Permissões**
   - Integração com sistema existente: Usuário → Perfil → Roles
   - Permissões customizáveis via perfis já implementados
   - Validação de permissões por contexto de tenant ativo

**MVP Success Criteria:**
- 99%+ de taxa de sucesso na criação de tenant/schema
- < 10 minutos para criar empresa (P95)
- < 500ms latência de troca de contexto
- Zero intervenções manuais necessárias
- 90%+ dos usuários completam criação de empresa sem suporte
- 85%+ retention D7

### Growth Features (Post-MVP)

**Fase 2 - Colaboração Aprimorada (3-6 meses):**
- Sistema completo de notificações in-app e email
- Fluxo de aprovação de convites (aceitar/recusar) - uso dos campos de status preparados no MVP
- Status avançados de colaboradores (pendente, inativo, bloqueado)
- Auditoria e histórico de ações por empresa
- Email automático ao criar empresa
- Notificação de adição como colaborador
- Alertas de mudanças em permissões

**Fase 3 - Governance & Scale (6-12 meses):**
- Transferência de ownership de empresa
- Limites de empresas ou colaboradores por usuário
- Billing/cobrança por empresa
- Analytics avançados e BI integrado
- API pública para integrações customizadas

### Vision (Future)

**Fase 4 - Integrações & Marketplace (12-18 meses):**
- Integração com marketplaces (Mercado Livre, Shopee, Amazon)
- Publicação de produtos em múltiplos canais com 1 clique
- Sincronização automática de estoque entre canais
- Integração com delivery (iFood, Rappi, Uber Eats)

**Fase 5 - Ecossistema & Enterprise (18-24 meses):**
- SDK para desenvolvedores third-party
- Marketplace de apps/plugins
- Integrações com ERP, contabilidade, fiscal
- Mobile app (iOS/Android)
- White-label para revendedores
- Features enterprise para empresas 100+ colaboradores
- Multi-currency e multi-idioma

**Expansão de Mercado:**
- Suporte a outros países (internacionalização)
- Segmentos verticais específicos (farmácias, restaurantes, e-commerce)

## User Journeys

### Journey 1: Joaquin Silva - Dominando Dois Negócios com Um Clique

Joaquin acorda às 5h da manhã para abrir a pizzaria em São Paulo. Antes de começar o dia, ele precisa verificar o estoque de mussarela da pizzaria E da sorveteria em Campinas - mas hoje ele está cansado de abrir duas planilhas diferentes e fazer login em sistemas separados.

Enquanto toma café, ele descobre o Estoque Central através de um amigo empreendedor. O que chama sua atenção: "Você mesmo cadastra sua empresa em minutos". Cético mas curioso, ele clica em "Começar grátis" e faz login com sua conta Google.

O sistema detecta que ele não tem empresa cadastrada e mostra uma tela simples: "Vamos cadastrar sua primeira empresa". Joaquin preenche "Pizzaria Don Giovanni", CNPJ, telefone. Clica em "Criar". Uma barra de progresso aparece: "Criando seu espaço isolado...". Em 15 segundos, pronto: "Bem-vindo à Pizzaria Don Giovanni! Vamos começar?"

O momento de transformação vem quando ele percebe: "Espera, posso criar a sorveteria também?". Ele clica no avatar no canto superior direito, vê "Trocar empresa" e "Criar nova empresa". Seleciona "Criar nova empresa", preenche "Sorveteria Gelato di Campinas", e em mais 15 segundos tem sua segunda empresa operacional.

**O verdadeiro "Aha!" moment**: Joaquin clica no avatar novamente, vê "Pizzaria Don Giovanni" e "Sorveteria Gelato di Campinas". Clica em "Pizzaria" - a tela muda instantaneamente. Clica em "Sorveteria" - troca de novo, sem pedir senha, sem re-login. Ele pensa: "É isso. Acabei de economizar 2 horas por semana só nisso".

Seis meses depois, Joaquin opera ambos negócios fluidamente. Pela manhã, gerencia estoque da pizzaria. À tarde, sem sair do sistema, troca para a sorveteria com um clique. Convidou seus dois gerentes (um para cada negócio) e eles operam de forma autônoma. Joaquin agora pensa em abrir uma hamburgueria - e sabe que vai levar 2 minutos para cadastrá-la.

---

### Journey 2: Maria Santos - De Planilhas ao Controle em 5 Minutos

Maria está no balcão da sua boutique "Estilo & Charme" em Curitiba, atendendo uma cliente, quando outra cliente pergunta: "Vocês têm essa blusa no tamanho M?". Maria olha para a prateleira, não tem certeza. Ela busca na planilha Excel do celular - última atualização foi... semana passada. "Acho que sim, deixa eu verificar no estoque". A cliente desiste e sai. Maria sente a frustração: mais uma venda perdida por desorganização.

À noite, depois de fechar a loja, Maria pesquisa "sistema de estoque simples" no Google. Ela tem medo de ferramentas complicadas - já tentou um sistema antes que exigia "configuração técnica" e desistiu no meio. Mas o Estoque Central promete: "Cadastre sua empresa em minutos, sem complicação".

Ela clica em "Começar grátis" e faz login com a conta Google da boutique. O sistema mostra: "Olá Maria! Vamos cadastrar sua primeira empresa?". Ela pensa: "Primeira empresa? Eu só tenho uma...". Preenche "Estilo & Charme", CNPJ, telefone. Clica em "Criar empresa".

**O momento de alívio**: Uma barra de progresso simples aparece: "Criando seu espaço... quase lá!". Em 20 segundos, a tela muda para o dashboard: "Bem-vinda à Estilo & Charme! Pronta para começar?". Maria não acredita - "É só isso? Já posso usar?".

O verdadeiro **"Aha!" moment** vem quando ela vê na tela: "Convide sua equipe". Maria pensa em suas 3 vendedoras - Juliana, Carla e Ana. Ela clica em "Convidar colaborador", digita o email de Juliana, seleciona perfil "Vendedora". Clica em "Enviar convite". Uma mensagem aparece: "Juliana foi adicionada à equipe! Ela verá a empresa no próximo login."

No dia seguinte, Juliana faz login com o Google dela, e automaticamente vê "Estilo & Charme" disponível. Clica e já está dentro, cadastrando produtos. Maria observa enquanto toma café: "Eu não precisei ensinar nada. Ela entrou e começou a trabalhar".

Três meses depois, o estoque está atualizado em tempo real. Quando uma cliente pergunta sobre tamanho, qualquer vendedora consulta no celular e responde na hora. Maria convida mais duas vendedoras sem esforço. Ela agora pensa em abrir uma segunda loja - e sabe que será tão simples quanto foi da primeira vez.

---

### Journey 3: Carlos Mendes - Liberdade para Trabalhar em Múltiplas Empresas

Carlos é gerente da Pizzaria Don Giovanni, do Joaquin. Ele é bom no que faz - controla estoque, faz pedidos de ingredientes, gerencia a equipe do turno da noite. Mas ele tem uma frustração: seu amigo Roberto abriu um bar e pediu ajuda para organizar o estoque. Carlos quer ajudar, mas tem medo de criar confusão entre os sistemas.

"Se eu fizer login no bar do Roberto, vou perder acesso à pizzaria do Joaquin? Vou ter que ficar fazendo login e logout o tempo todo?" - Carlos pensa enquanto toma café antes de começar o turno.

Um dia, Joaquin diz: "Carlos, adicionei você como gerente no sistema novo. Faz login com seu Gmail e me diz o que achou". Carlos faz login com sua conta Google pessoal. O sistema mostra: "Olá Carlos! Você tem acesso a estas empresas: Pizzaria Don Giovanni". Ele clica e já está dentro, vendo o estoque de mussarela e molho de tomate.

**O momento de transformação**: Carlos fala pro Roberto: "Cara, me adiciona no teu sistema também". Roberto adiciona o email do Carlos como colaborador no "Bar do Beco". No dia seguinte, Carlos faz login no Estoque Central e vê algo diferente: "Escolha sua empresa: Pizzaria Don Giovanni | Bar do Beco".

Ele clica em "Pizzaria" - vê o estoque de pizzas. Depois clica no avatar no canto da tela, seleciona "Bar do Beco" - em menos de 1 segundo, a tela muda e ele vê o estoque de cervejas e petiscos do Roberto. **Sem pedir senha de novo. Sem fazer logout. Sem confusão.**

"Cara, isso é perfeito!" - Carlos pensa. De manhã, ele gerencia a pizzaria. À tarde, ajuda o Roberto remotamente, trocando de contexto com um clique. Quando o Joaquin pergunta algo sobre a pizzaria, Carlos troca de volta instantaneamente.

Seis meses depois, Carlos opera em 3 empresas: a pizzaria do Joaquin, o bar do Roberto, e agora também ajuda uma amiga com um food truck. Ele não precisa lembrar 3 senhas diferentes, não precisa fazer múltiplos logins. Apenas um clique no avatar, escolhe a empresa, e trabalha. Ele se tornou o "gerente freelancer" mais eficiente que conhece.

---

### Journey Requirements Summary

Essas três jornadas revelam as capacidades essenciais do sistema de self-service multi-tenant onboarding:

**1. Criação Self-Service de Empresa (Tenant)**
- **Interface**: Formulário simples pós-login Google OAuth com campos básicos (nome empresa, CNPJ opcional, contato)
- **Feedback**: Loading state durante provisionamento (15-30 segundos), barra de progresso com mensagens claras
- **Backend**: Criação automática de tenant na tabela `public.tenants` + schema PostgreSQL isolado `tenant_{uuid}`
- **Autorização**: Usuário criador automaticamente vira admin da empresa
- **Confirmação**: Dashboard operacional imediato após provisionamento bem-sucedido

**2. Gestão Inteligente de Contexto Multi-Empresa**
- **Tela de seleção**: Quando usuário possui 2+ empresas, mostra lista de empresas disponíveis após login
- **Opção criar nova**: Botão "Criar nova empresa" sempre visível para usuários multi-empresa
- **Menu avatar**: Dropdown no canto superior com lista de empresas + opção de troca
- **Troca instantânea**: Latência < 500ms para alternar contexto, sem re-autenticação
- **Persistência**: Contexto mantido em sessão, sobrevive a F5/refresh

**3. Sistema de Colaboração Simplificada**
- **Convite por email**: Admin digita email do colaborador, seleciona perfil (vendedor, gerente, etc.)
- **Vinculação automática**: Status "ativo" imediato (sem fluxo de aprovação no MVP)
- **Discovery**: Colaborador vê empresa vinculada automaticamente no próximo login/refresh
- **Gestão de equipe**: Admin pode remover colaboradores, promover para admin, visualizar lista completa
- **Multi-admin**: Suporte a múltiplos administradores por tenant

**4. Gestão de Empresas**
- **Edição de dados**: Nome, CNPJ, telefone, endereço podem ser atualizados por admin
- **Visualização de equipe**: Lista de colaboradores com perfis e status
- **Deleção de empresa**: Proteção contra tenant órfão, validação de dados antes de excluir
- **Auditoria básica**: Registro de ações críticas para troubleshooting

**5. Sistema de Permissões Flexível**
- **Integração existente**: Aproveita sistema Usuário → Perfil → Roles já implementado
- **Validação por contexto**: Permissões aplicadas baseadas no tenant ativo
- **Perfis customizáveis**: Admin pode definir permissões específicas por perfil
- **Escalabilidade**: Arquitetura suporta perfis complexos para fases futuras

## SaaS B2B Platform - Specific Requirements

### Multi-Tenancy Architecture

**Tenant Isolation Model:**

O Estoque Central implementa isolamento completo através de **schema-per-tenant PostgreSQL**, garantindo máxima segurança, compliance e independência operacional por cliente:

- **Schema PostgreSQL isolado**: Cada empresa (tenant) possui schema dedicado `tenant_{uuid}` com tabelas completas de negócio
- **Metadata centralizada**: Tabela `public.tenants` armazena configurações, status e metadados de todos os tenants
- **Tenant routing**: JWT customizado contém `tenantId` + `roles`, permitindo roteamento automático de requests para schema correto
- **Context management**: `TenantContext` + `TenantInterceptor` + `AbstractRoutingDataSource` orquestram isolamento em runtime (Story 1.3)

**Shared Resources Strategy:**

Recursos compartilhados utilizados com namespacing seguro:

- **Redis**: Cache compartilhado com prefixos `tenant:{tenantId}:*` para garantir isolamento lógico
- **Object Storage**: Estrutura de pastas `{tenantId}/produtos/`, `{tenantId}/relatorios/` para arquivos/imagens
- **Application Layer**: Instâncias compartilhadas do Spring Boot com roteamento dinâmico por request

**Tenant Lifecycle Management:**

- **Criação**: Provisionamento automático via endpoint público → cria registro em `public.tenants` → executa DDL para criar schema isolado → retorna confirmação
- **Ativação**: Status `ativo` imediato após provisionamento bem-sucedido
- **Inativação**: Admin pode inativar empresa (status `inativo`), bloqueando acesso sem deletar dados
- **Deleção**: Admin pode deletar empresa com proteção contra tenant órfão (validação de integridade antes de DROP SCHEMA)

**Performance & Scalability:**

- **Schema provisioning time**: < 30 segundos (P95) para criação completa de schema PostgreSQL
- **Context switch latency**: < 500ms para alternar entre tenants (troca de DataSource + cache invalidation)
- **Concurrent tenants**: Arquitetura suporta 1000+ tenants simultâneos sem degradação
- **Database connection pooling**: Pool dedicado por tenant com limites configuráveis

### RBAC - Role-Based Access Control

**Permission Model:**

Sistema de permissões flexível e escalável baseado em **Usuário → Perfil → Roles**:

- **Usuário**: Pessoa autenticada via Google OAuth 2.0
- **Perfil**: Agrupamento de roles (ex: Admin, Gerente, Vendedor)
- **Roles**: Permissões granulares (ex: `estoque.ler`, `produto.editar`, `relatorio.visualizar`)

**MVP Profiles:**

Três perfis pré-definidos para cobrir casos de uso principais:

1. **Admin**
   - Permissões: Acesso completo ao tenant (CRUD em todas entidades)
   - Capacidades: Criar/editar/deletar empresa, gerenciar colaboradores, promover outros admins
   - Quantity: Múltiplos admins permitidos por tenant

2. **Gerente**
   - Permissões: Leitura completa + escrita em estoque, produtos, movimentações
   - Capacidades: Gerenciar estoque, criar relatórios, visualizar colaboradores (sem editar)
   - Restrições: Não pode alterar dados da empresa ou gerenciar permissões

3. **Vendedor**
   - Permissões: Leitura em produtos/estoque + escrita em vendas/movimentações de saída
   - Capacidades: Registrar vendas, consultar disponibilidade, gerar relatórios de vendas
   - Restrições: Não pode editar produtos, ajustar estoque manualmente, ou acessar configurações

**Permission Validation:**

- **Context-aware**: Permissões validadas sempre no contexto do tenant ativo
- **Enforcement layer**: Spring Security + interceptors validam roles antes de cada operação
- **Audit trail**: Operações sensíveis registradas com `userId` + `tenantId` + timestamp

**Growth Phase - Custom Profiles:**

Post-MVP, admins poderão criar perfis customizados:

- **Profile builder UI**: Interface para selecionar roles granulares e criar perfis personalizados
- **Department-specific profiles**: Ex: Comprador (foco em fornecedores/compras), Contador (foco em relatórios fiscais)
- **Dynamic role assignment**: Permissões atualizadas em runtime sem necessidade de re-login

### Subscription Tiers & Billing

**MVP Strategy - No Billing:**

Para validar product-market fit rapidamente, o MVP opera sem sistema de cobrança:

- **Free unlimited access**: Todas empresas criadas têm acesso completo a todas funcionalidades
- **No feature gating**: Sem limitação de produtos, usuários, transações ou storage
- **Focus on validation**: Prioridade em provar valor do produto antes de monetizar

**Growth Phase - Freemium Model (6-12 meses):**

Introdução de tiers para monetização escalável:

1. **Free Tier**
   - Limite: 1 empresa, 3 colaboradores, 100 produtos
   - Funcionalidades: Core features (estoque, movimentações, relatórios básicos)

2. **Professional Tier** (R$ 99/mês por empresa)
   - Limite: Empresas ilimitadas, 10 colaboradores por empresa, 1000 produtos
   - Funcionalidades: Relatórios avançados, integrações básicas, suporte prioritário

3. **Enterprise Tier** (R$ 299/mês por empresa)
   - Limite: Ilimitado
   - Funcionalidades: API pública, webhooks, SSO, SLA 99.9%, white-label

**Billing Infrastructure (Future):**

- **Payment gateway**: Integração com Stripe/PagSeguro para cobrança recorrente
- **Usage metering**: Tracking de produtos, colaboradores, storage para enforcement de limites
- **Upgrade/downgrade flow**: Self-service para mudança de plano com proration

### Integration Architecture

**MVP - Minimal External Dependencies:**

Para reduzir complexidade e time-to-market, MVP limita integrações externas:

- **Email**: Sistema de convites e notificações postponed para Growth Phase
  - MVP: Convites sem email (vinculação automática com status `ativo`)
  - Growth: SMTP/SendGrid para emails transacionais

- **Object Storage**: Upload local de imagens no MVP
  - Growth: Migração para S3/CloudFlare R2

- **Authentication**: Google OAuth 2.0 já implementado (Story 1.4) ✅

**Growth Phase - Core Integrations:**

Integrações essenciais para escala:

- **Email service**: SendGrid/AWS SES para notificações transacionais
- **Object storage**: S3-compatible storage para imagens de produtos
- **Analytics**: Amplitude/Mixpanel para product analytics
- **Monitoring**: Sentry (error tracking) + Datadog (APM)

**Vision Phase - Ecosystem Integrations:**

Marketplace e ecossistema de integrações:

- **E-commerce**: Mercado Livre, Shopee, Amazon (sincronização de estoque)
- **Delivery**: iFood, Rappi, Uber Eats (gestão de cardápio + estoque)
- **Accounting**: ContaAzul, Omie (exportação de movimentações para fiscal)
- **ERP**: SAP, TOTVS (integração bidirecional para grandes clientes)

**API Strategy:**

- **MVP**: API interna apenas (frontend Angular consome backend REST)
- **Growth**: API pública documentada (OpenAPI) para integrações third-party
- **Vision**: SDKs (JavaScript, Python, PHP) + Webhooks + Marketplace de apps

### Compliance & Data Governance

**LGPD Compliance:**

Estratégia de proteção de dados pessoais alinhada com Lei Geral de Proteção de Dados:

**MVP - Foundation:**

- **Data isolation**: Schema-per-tenant garante que dados de uma empresa nunca vazam para outra ✅
- **Minimal data collection**: Coleta apenas dados essenciais (nome, email via Google OAuth, dados de empresa)
- **Secure storage**: Senhas nunca armazenadas (OAuth delegado), dados sensíveis em HTTPS
- **Error logging**: Erros críticos registrados em `public.tenants` sem expor PII (Personally Identifiable Information)

**Growth Phase - Advanced LGPD:**

- **Right to access**: Endpoint para usuário exportar todos seus dados pessoais (JSON/CSV)
- **Right to deletion**: Fluxo para usuário solicitar exclusão completa de dados (soft delete + anonimização)
- **Data retention policies**: Configuração de TTL para dados inativos (ex: empresas deletadas após 90 dias)
- **Consent management**: Tracking de consentimento para cookies, analytics, marketing

**Audit Trail & Observability:**

- **MVP**: Logging básico de operações críticas (criação/deleção de empresa, mudança de permissões)
- **Growth**: Audit log completo com `userId` + `tenantId` + `action` + `timestamp` + `changedFields`
- **Vision**: Compliance dashboard para admins visualizarem histórico de ações sensíveis

**Security Measures:**

- **Authentication**: Google OAuth 2.0 com JWT (HS256) contendo `tenantId` + `roles` + `exp`
- **Authorization**: Spring Security validando roles por endpoint/ação
- **Data encryption**: TLS 1.3 em trânsito, PostgreSQL com encryption at rest
- **SQL injection protection**: JPA/Hibernate com prepared statements
- **XSS protection**: Angular sanitization automática + CSP headers
- **Rate limiting**: Nginx rate limiting para proteção contra abuse (Growth Phase)

**Regulatory Readiness:**

- **Data residency**: Dados hospedados no Brasil (AWS São Paulo) para compliance LGPD
- **Data portability**: Formato JSON/CSV para exportação de dados
- **Incident response**: Processo documentado para breach notification (< 72h conforme LGPD)
- **Privacy policy**: Termos de uso + política de privacidade clara e acessível

## Implementation Strategy & Risk Mitigation

### MVP Development Approach

**Strategy:** Platform + Experience MVP - Construir fundação técnica sólida com experiência de usuário zero-friction.

**Rationale:**
- Infraestrutura multi-tenancy já 70% implementada (Story 1.3 + 1.4)
- Google OAuth funcional reduz tempo de desenvolvimento
- Foco em orquestrar capacidades existentes via UX intuitiva
- Time-to-market reduzido aproveitando brownfield foundation

**Resource Requirements:**

- **Team Size**: 2-3 desenvolvedores (1 backend Spring Boot, 1 frontend Angular, 0.5 fullstack)
- **Skills críticas**: Spring Boot + PostgreSQL + Angular Signals + Multi-tenancy patterns
- **Timeline estimado**: 6-8 semanas para MVP funcional
- **Infrastructure**: PostgreSQL 16+, Redis, Nginx (já existente)

### Risk Analysis & Mitigation

**Technical Risks:**

1. **Schema provisioning performance < 30s**
   - Risco: DDL PostgreSQL pode ser lento com muitas tabelas
   - Mitigação: Scripts DDL otimizados + template schema + connection pooling adequado
   - Fallback: Provisioning assíncrono com callback/polling

2. **Context switch latency > 500ms**
   - Risco: Troca de DataSource + invalidação de cache pode ser lenta
   - Mitigação: Redis cache warm-up + AbstractRoutingDataSource otimizado
   - Fallback: Aceitar 1s latency no MVP, otimizar pós-launch

3. **Data leakage entre tenants**
   - Risco: Bug no TenantInterceptor pode vazar dados
   - Mitigação: Testes de integração rigorosos + code review obrigatório
   - Fallback: Audit trail detecta anomalias rapidamente

**Market Risks:**

1. **Baixa adoption rate (< 80% completion)**
   - Risco: UX complexa ou confusa afasta usuários
   - Mitigação: User testing com 5-10 beta testers antes de launch
   - Pivot: Simplificar ainda mais (ex: remover criação multi-empresa no MVP)

2. **Churn alto (> 20% mensal)**
   - Risco: Produto não entrega valor percebido
   - Mitigação: Onboarding guiado + product analytics (Mixpanel)
   - Pivot: Entrevistas com churned users para identificar gaps

**Resource Risks:**

1. **Equipe menor que planejado**
   - Risco: Perda de desenvolvedor ou budget reduzido
   - Mitigação: Priorizar apenas Journey 2 (Maria - single business owner) no MVP
   - Fallback: Multi-empresa (Journey 1 Joaquin) vira Growth Phase

2. **Timeline estoura 50%+**
   - Risco: Complexidade subestimada ou bugs críticos
   - Mitigação: Sprints semanais com checkpoint de progresso
   - Fallback: Lançar "Soft MVP" sem gestão de colaboradores (apenas criação de empresa)

### Go/No-Go Criteria

**MVP Launch Readiness:**

- ✅ 99%+ taxa de sucesso em criação de tenant (100 testes)
- ✅ < 10 minutos para criar empresa (P95)
- ✅ < 500ms latência de troca de contexto (P95)
- ✅ Zero data leakage (validado por testes de segurança)
- ✅ 5+ beta testers completaram onboarding com sucesso
- ✅ Error logging funcional + alertas configurados

**Post-Launch Success Gates:**

- **Week 1**: 50+ empresas criadas, < 5% error rate
- **Week 4**: 80%+ completion rate, 70%+ D7 retention
- **Week 12**: 200+ empresas ativas, 60%+ D30 retention

Se qualquer gate falhar, pausar growth marketing e iterar no produto.

## Technical Architecture Summary

### System Architecture Overview

**Architecture Pattern:** Hexagonal Architecture (Ports & Adapters) + Spring Modulith

O Estoque Central segue Clean Architecture com separação clara de responsabilidades:

```
┌─────────────────────────────────────────────────────────┐
│                    Frontend Layer                        │
│  Angular 17+ (Standalone Components + Signals)          │
│  - Company Registration UI                              │
│  - Company Selection & Context Switcher                 │
│  - Collaborator Management UI                           │
└─────────────────────────────────────────────────────────┘
                         ▼ HTTPS/REST
┌─────────────────────────────────────────────────────────┐
│                   API Gateway Layer                      │
│  Spring Boot 3.3+ REST Controllers                      │
│  - TenantInterceptor (injeta tenantId no context)       │
│  - Spring Security (valida JWT + roles)                 │
└─────────────────────────────────────────────────────────┘
                         ▼
┌─────────────────────────────────────────────────────────┐
│                  Application Layer                       │
│  Use Cases / Application Services                       │
│  - CreateCompanyUseCase                                 │
│  - SwitchCompanyContextUseCase                          │
│  - InviteCollaboratorUseCase                            │
└─────────────────────────────────────────────────────────┘
                         ▼
┌─────────────────────────────────────────────────────────┐
│                    Domain Layer                          │
│  Core Business Logic (Framework-agnostic)               │
│  - Company (Aggregate)                                  │
│  - User, Collaborator, Profile, Role (Entities)        │
│  - TenantProvisioner (Domain Service)                   │
└─────────────────────────────────────────────────────────┘
                         ▼
┌─────────────────────────────────────────────────────────┐
│                Infrastructure Layer                      │
│  - PostgreSQL (AbstractRoutingDataSource)               │
│  - Redis (Cache)                                        │
│  - Google OAuth 2.0 (Authentication)                    │
└─────────────────────────────────────────────────────────┘
```

### Core Technical Components

**Backend Stack:**

- **Framework**: Spring Boot 3.3+ com Java 21
- **Architecture**: Hexagonal Architecture + Spring Modulith para modularização
- **Database**: PostgreSQL 16+ com schema-per-tenant isolation
- **Cache**: Redis para session management e performance optimization
- **Security**: Spring Security + Google OAuth 2.0 + JWT
- **API**: REST com JSON (OpenAPI documentation em Growth Phase)

**Frontend Stack:**

- **Framework**: Angular 17+ (standalone components)
- **State Management**: Angular Signals para reatividade
- **Styling**: Tailwind CSS ou Angular Material
- **Build**: Vite ou esbuild para fast builds
- **Auth**: OAuth 2.0 Authorization Code Flow com PKCE

**Infrastructure:**

- **Database**: PostgreSQL 16+ (AWS RDS ou self-hosted)
- **Cache**: Redis 7+ (ElastiCache ou self-hosted)
- **Hosting**: AWS (EC2, ECS, ou Lambda@Edge para Angular SSR)
- **CDN**: CloudFront para assets estáticos
- **Monitoring**: CloudWatch Logs + Sentry (error tracking)

### Multi-Tenancy Implementation Details

**Database Routing Strategy:**

```java
// Simplified architecture flow
1. Request → TenantInterceptor extrai tenantId do JWT
2. TenantContext.setCurrentTenant(tenantId)
3. AbstractRoutingDataSource.determineCurrentLookupKey() → retorna tenantId
4. Hibernate usa DataSource correto para query
5. Query executa em schema isolado: tenant_{uuid}
```

**Key Classes:**

- **TenantContext**: ThreadLocal storage para tenantId ativo
- **TenantInterceptor**: Spring MVC interceptor que injeta contexto
- **AbstractRoutingDataSource**: Roteamento dinâmico de conexões PostgreSQL
- **TenantProvisioner**: Domain service para criação de tenant + schema

**Schema Provisioning Flow:**

```
1. POST /api/public/companies (endpoint público)
2. CreateCompanyUseCase valida dados
3. TenantProvisioner.createTenant():
   a. INSERT INTO public.tenants (name, uuid, status, created_at)
   b. CREATE SCHEMA tenant_{uuid}
   c. Execute DDL script para criar tabelas no novo schema
   d. Seed data inicial (perfis padrão, configurações)
4. Retorna JWT com tenantId + role=ADMIN
5. Frontend redireciona para dashboard da nova empresa
```

### Authentication & Authorization Flow

**OAuth 2.0 + JWT Flow:**

```
┌──────────┐                                      ┌─────────────┐
│  User    │                                      │   Google    │
│ Browser  │                                      │   OAuth     │
└──────────┘                                      └─────────────┘
     │                                                    │
     │  1. Click "Login com Google"                      │
     ├──────────────────────────────────────────────────▶│
     │                                                    │
     │  2. Redirect to /oauth2/authorize/google          │
     │◀──────────────────────────────────────────────────┤
     │                                                    │
     │  3. User consente acesso                          │
     ├──────────────────────────────────────────────────▶│
     │                                                    │
     │  4. Redirect /login/oauth2/code/google?code=XXX   │
     │◀──────────────────────────────────────────────────┤
     │                                                    │
     │  5. Backend troca code por access_token           │
     │                                                    │
     │  6. Backend cria/atualiza User, gera JWT          │
     │     JWT payload: { sub: email, tenantId, roles }  │
     │◀──────────────────────────────────────────────────┤
     │                                                    │
     │  7. Retorna JWT + lista de empresas vinculadas    │
     │                                                    │
     │  8. Se 0 empresas → redirect /create-company      │
     │     Se 1 empresa → auto-select + redirect /dashboard
     │     Se 2+ empresas → redirect /select-company     │
     │                                                    │
```

**JWT Payload Structure:**

```json
{
  "sub": "user@gmail.com",
  "tenantId": "550e8400-e29b-41d4-a716-446655440000",
  "roles": ["ADMIN", "ESTOQUE_EDITAR", "PRODUTO_CRIAR"],
  "iat": 1703001600,
  "exp": 1703088000
}
```

**Permission Enforcement:**

- **Controller level**: `@PreAuthorize("hasRole('ADMIN')")`
- **Method level**: `@PreAuthorize("hasAuthority('ESTOQUE_EDITAR')")`
- **Domain level**: Entity/Aggregate valida invariantes de negócio
- **Database level**: Row-Level Security (RLS) como camada adicional de proteção (Growth Phase)

### Data Model - Core Entities

**public.tenants (Metadata centralizada):**

```sql
CREATE TABLE public.tenants (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  name VARCHAR(255) NOT NULL,
  cnpj VARCHAR(18),
  status VARCHAR(20) NOT NULL DEFAULT 'ativo', -- ativo, inativo, deletado
  schema_name VARCHAR(100) NOT NULL UNIQUE,    -- tenant_{uuid}
  created_at TIMESTAMP NOT NULL DEFAULT NOW(),
  updated_at TIMESTAMP,
  created_by UUID REFERENCES public.users(id)
);
```

**public.users (Usuários globais):**

```sql
CREATE TABLE public.users (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  email VARCHAR(255) NOT NULL UNIQUE,
  name VARCHAR(255),
  avatar_url TEXT,
  oauth_provider VARCHAR(50) NOT NULL,        -- google, github, etc
  oauth_provider_id VARCHAR(255),
  created_at TIMESTAMP NOT NULL DEFAULT NOW()
);
```

**public.user_tenants (Vinculação many-to-many):**

```sql
CREATE TABLE public.user_tenants (
  user_id UUID REFERENCES public.users(id),
  tenant_id UUID REFERENCES public.tenants(id),
  profile_id UUID,                             -- referência para perfil no schema do tenant
  status VARCHAR(20) NOT NULL DEFAULT 'ativo', -- ativo, pendente, inativo
  created_at TIMESTAMP NOT NULL DEFAULT NOW(),
  PRIMARY KEY (user_id, tenant_id)
);
```

**tenant_{uuid}.profiles (Perfis isolados por tenant):**

```sql
-- Este schema é criado dinamicamente para cada tenant
CREATE SCHEMA tenant_{uuid};

CREATE TABLE tenant_{uuid}.profiles (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  name VARCHAR(100) NOT NULL,                  -- Admin, Gerente, Vendedor
  description TEXT,
  created_at TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE TABLE tenant_{uuid}.profile_roles (
  profile_id UUID REFERENCES tenant_{uuid}.profiles(id),
  role VARCHAR(100) NOT NULL,                  -- ESTOQUE_EDITAR, PRODUTO_CRIAR, etc
  PRIMARY KEY (profile_id, role)
);
```

### API Endpoints - MVP Essentials

**Public Endpoints (sem autenticação):**

```
POST   /api/public/companies              # Criar empresa (auto-provisioning)
POST   /api/public/auth/google             # OAuth callback
```

**Protected Endpoints (requer JWT):**

```
GET    /api/users/me                       # Dados do usuário logado
GET    /api/users/me/companies             # Listar empresas vinculadas
POST   /api/users/me/companies             # Criar nova empresa
PUT    /api/users/me/context               # Trocar contexto de empresa

GET    /api/companies/current              # Dados da empresa ativa
PUT    /api/companies/current              # Editar empresa ativa
DELETE /api/companies/current              # Deletar empresa ativa

GET    /api/collaborators                  # Listar colaboradores
POST   /api/collaborators                  # Convidar colaborador
DELETE /api/collaborators/:id              # Remover colaborador
PUT    /api/collaborators/:id/profile      # Alterar perfil do colaborador
PUT    /api/collaborators/:id/promote      # Promover para admin

GET    /api/profiles                       # Listar perfis disponíveis
```

### Performance Targets & Constraints

**Response Time (P95):**

- GET endpoints: < 200ms
- POST tenant creation: < 5s (inclui DDL schema)
- Context switch: < 500ms
- Batch operations: < 3s para até 100 registros

**Throughput:**

- 1000+ requests/segundo (carga típica)
- 10,000+ requests/segundo (peak com autoscaling)

**Scalability:**

- Suporte a 1000+ tenants simultâneos sem degradação
- Horizontal scaling via load balancer + multiple app instances
- Database read replicas para queries pesadas (Growth Phase)

**Availability:**

- 99.5% uptime durante horário comercial (MVP)
- 99.9% uptime com multi-AZ deployment (Growth Phase)

### Development & Deployment

**Development Workflow:**

- **Version Control**: Git + GitHub/GitLab
- **Branching**: GitFlow (main, develop, feature/*, hotfix/*)
- **CI/CD**: GitHub Actions ou GitLab CI
- **Code Quality**: SonarQube + ESLint + Prettier
- **Testing**: JUnit 5 + Mockito (backend), Jasmine + Karma (frontend)

**Deployment Strategy:**

- **MVP**: Manual deployment para staging + production
- **Growth**: Automated CI/CD com blue-green deployment
- **Vision**: Canary releases + feature flags (LaunchDarkly)

**Environments:**

- **Local**: Docker Compose com PostgreSQL + Redis
- **Staging**: AWS (EC2/ECS) com database snapshot de produção
- **Production**: AWS multi-AZ com RDS + ElastiCache

---

## Conclusão

Este PRD documenta a funcionalidade de **self-service multi-tenant onboarding** para o Estoque Central, permitindo que empresas se cadastrem autonomamente e usuários gerenciem múltiplas empresas com troca de contexto fluida.

**Principais Entregas MVP:**

1. ✅ Criação self-service de empresa com provisionamento automático de tenant/schema
2. ✅ Seleção e troca de contexto entre empresas sem re-autenticação
3. ✅ Sistema de convites e gestão de colaboradores
4. ✅ RBAC com 3 perfis (Admin, Gerente, Vendedor)
5. ✅ Arquitetura multi-tenancy com isolamento completo por schema PostgreSQL

**Próximos Passos:**

1. **Epics & Stories**: Decompor este PRD em epics executáveis e user stories detalhadas
2. **Technical Specification**: Criar specs técnicas para componentes críticos (TenantProvisioner, Context Switcher)
3. **Design System**: Definir componentes UI reutilizáveis para Angular
4. **Test Strategy**: Planejar cobertura de testes (unit, integration, e2e)
5. **Sprint Planning**: Organizar sprints de 1-2 semanas com checkpoints claros

**Ready for Implementation:** Este PRD está completo e pronto para ser transformado em código. 🚀
