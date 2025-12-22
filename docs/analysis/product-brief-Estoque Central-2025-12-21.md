---
stepsCompleted: [1, 2, 3, 4, 5]
inputDocuments: []
workflowType: 'product-brief'
lastStep: 5
project_name: 'Estoque Central'
user_name: 'poly'
date: '2025-12-21'
---

# Product Brief: Estoque Central

**Date:** 2025-12-21
**Author:** poly

---

## Executive Summary

O Estoque Central enfrenta um gargalo crítico de escalabilidade: a criação manual de tenants para novos clientes impede o crescimento ágil da plataforma. Esta funcionalidade transforma o processo de onboarding, permitindo que empresas se auto-cadastrem via interface web, com criação automática de tenant e schema isolado. Além disso, habilita usuários a participarem de múltiplas empresas simultaneamente, com troca de contexto fluida, sem necessidade de múltiplos logins.

A solução remove barreiras operacionais, acelera time-to-value para novos clientes, e posiciona o Estoque Central como uma plataforma verdadeiramente self-service e escalável.

---

## Core Vision

### Problem Statement

Atualmente, a criação de novos tenants no Estoque Central é um processo manual que requer intervenção técnica via API. Cada novo cliente (exemplo: empresa do Joaquin) precisa ter seu tenant e schema PostgreSQL criados manualmente, gerando:
- **Gargalo operacional**: Atraso no onboarding de novos clientes
- **Falta de autonomia**: Clientes dependem da equipe técnica para começar
- **Limitação de escala**: Impossível crescer rapidamente com processo manual
- **Experiência fragmentada**: Usuários que atuam em múltiplas empresas precisam fazer múltiplos logins

### Problem Impact

**Para o negócio:**
- Perda de potenciais clientes por demora no onboarding
- Custo operacional alto para ativação de cada cliente
- Impossibilidade de escalar comercialmente sem escalar equipe técnica

**Para os usuários:**
- Empresas não conseguem começar a usar imediatamente
- Usuários multi-empresa têm experiência ruim (re-login constante)
- Falta de autonomia na gestão de colaboradores

### Why Existing Solutions Fall Short

Embora exista um endpoint de API para criação de tenants, ele:
- Não está exposto em interface de usuário
- Requer conhecimento técnico para ser utilizado
- Não resolve o fluxo completo de onboarding (convites, permissões, multi-tenancy)
- Não permite troca de contexto entre empresas de forma fluida

### Proposed Solution

**Self-Service Multi-Tenant Onboarding Platform**

Uma experiência completa de self-service que permite:

1. **Criação autônoma de empresas**: Após login via Google OAuth, usuários sem empresa são direcionados para tela de cadastro que cria automaticamente tenant + schema isolado

2. **Gestão inteligente de contexto**: Usuários vinculados a múltiplas empresas escolhem qual acessar após login, com capacidade de trocar contexto a qualquer momento via avatar (sem re-login)

3. **Sistema de convites e colaboração**: Admins convidam colaboradores por email, com sistema de notificações in-app para aceitar/recusar convites

4. **Permissões flexíveis**: Modelo de perfis customizáveis onde admins definem o que cada colaborador pode fazer, com suporte a múltiplos admins por tenant

5. **Tratamento robusto de erros**: Processo crítico com loading states, registro de falhas no schema público, e mecanismos de proteção (ex: tenant órfão congelado)

### Key Differentiators

- **Zero-friction onboarding**: De login a empresa operacional em minutos, não dias
- **True multi-tenancy**: Isolamento completo por schema PostgreSQL, não apenas lógico
- **Contexto fluido**: Troca entre empresas sem re-autenticação, mantendo produtividade
- **Escalabilidade técnica e comercial**: Arquitetura que suporta crescimento exponencial sem intervenção manual
- **Compliance-ready**: Tratamento adequado de tenant órfão considerando LGPD

## Target Users

### Primary Users

**Persona 1: Joaquin - O Empreendedor Multi-Negócios**

**Contexto:**
- Dono de uma pizzaria em São Paulo e uma sorveteria em Campinas
- 15 funcionários total (8 na pizzaria, 7 na sorveteria)
- Não é técnico, mas consegue usar ferramentas web modernas
- Gerencia ambos negócios ativamente, viajando entre as cidades

**Problema Atual:**
- Hoje usa planilhas separadas ou sistemas diferentes para cada negócio
- Perde tempo fazendo login/logout quando precisa trocar de contexto
- Não consegue começar a usar Estoque Central imediatamente - precisa esperar aprovação/setup manual
- Dificuldade em dar acesso aos gerentes de cada unidade

**Momento "Aha!":**
Quando ele percebe que pode criar ambas empresas sozinho em minutos, trocar entre elas com um clique, e dar acesso aos gerentes sem complicação técnica.

**Sucesso para Joaquin:**
- Cadastrar suas duas empresas em menos de 10 minutos
- Convidar seus gerentes sem precisar de suporte técnico
- Trocar entre pizzaria e sorveteria sem re-login
- No futuro próximo: publicar produtos nos marketplaces com um clique

---

**Persona 2: Maria - A Dona de Loja Única**

**Contexto:**
- Dona de uma boutique de roupas no centro de Curitiba
- 5 colaboradores (3 vendedoras, 1 estoquista, 1 caixa)
- Não tem conhecimento técnico profundo
- Ela mesma opera o sistema junto com a equipe

**Problema Atual:**
- Usa planilhas Excel que ficam desatualizadas
- Não tem controle fino de quem pode fazer o quê no estoque
- Quer começar a usar o Estoque Central mas tem medo de processos complicados

**Momento "Aha!":**
Quando ela faz login com Google, cria sua empresa em 3 minutos, convida as vendedoras, e já começa a cadastrar produtos.

**Sucesso para Maria:**
- Zero friction para começar a usar
- Convidar equipe por email de forma simples
- Definir que só ela e a gerente podem aprovar saídas de estoque
- Futuramente: integrar com Mercado Livre/Shopee para vender online

---

### Secondary Users

**Persona 3: Carlos - O Operador/Colaborador**

**Contexto:**
- Gerente da pizzaria do Joaquin
- Responsável por controle de estoque, pedidos a fornecedores
- Também ajuda um amigo que tem um bar (poderia ser colaborador em 2 empresas)

**Problema Atual:**
- Não tem autonomia - depende do Joaquin para tudo
- Se quisesse ajudar o amigo do bar, precisaria de outro login/senha

**Momento "Aha!":**
Quando recebe convite por email, aceita com um clique, e já está operando na pizzaria. Depois recebe convite do bar, aceita, e pode trocar entre os dois contextos facilmente.

**Sucesso para Carlos:**
- Receber convite e começar a trabalhar rapidamente
- Ter as permissões certas (não pode deletar empresa, mas pode gerenciar estoque)
- Trocar entre pizzaria e bar sem complicação

---

### User Journey

**Discovery → Onboarding (0-10 minutos):**
1. Joaquin descobre Estoque Central via anúncio/indicação
2. Clica em "Começar grátis", faz login com Google
3. Sistema detecta: "Você não tem empresa cadastrada"
4. Tela de criação: Nome da empresa, dados básicos
5. Loading... Tenant criado! Schema PostgreSQL provisionado!
6. "Bem-vindo ao Estoque Central! Vamos cadastrar seus produtos?"

**Core Usage (Dia-a-dia):**
1. Joaquin acessa pela manhã, escolhe "Pizzaria SP"
2. Verifica estoque, vê que falta mussarela
3. Convida novo gerente pelo email
4. À tarde, clica no avatar → "Trocar empresa" → "Sorveteria Campinas"
5. Sem re-login, já está no contexto da sorveteria
6. Verifica relatórios, aprova pedido de sorvete

**Success Moment (Primeiros 30 dias):**
- Joaquin cadastrou 2 empresas
- Convidou 4 colaboradores (2 por empresa)
- Já tem controle total de estoque das duas unidades
- Economia de 10h/semana que gastava com planilhas
- No futuro: integra com iFood e começa a vender online

**Long-term (3-6 meses):**
- Colaboradores usam diariamente sem problemas
- Joaquin abre terceira empresa (hamburgueria) - cadastra em 5 minutos
- Sistema se torna parte essencial da operação
- Indicou para 3 amigos empreendedores

## Success Metrics

### User Success Metrics

**Onboarding Success:**
- **Time-to-first-company**: 95% dos usuários criam sua primeira empresa em menos de 10 minutos após login
- **Completion rate**: 90% dos usuários que iniciam o fluxo de criação de empresa completam com sucesso
- **Error rate**: Menos de 1% de falhas na criação de tenant/schema

**Engagement Indicators:**
- **Multi-company adoption**: 30% dos usuários ativos criam 2+ empresas nos primeiros 90 dias
- **Company switching**: Usuários multi-empresa trocam de contexto 5+ vezes por semana (indicando uso real)
- **Collaboration success**: 80% dos usuários convidam pelo menos 1 colaborador nos primeiros 30 dias
- **Invitation acceptance**: 70% dos convites são aceitos em até 7 dias

**Value Realization:**
- **Retention**: 85% dos usuários que criam empresa retornam na semana seguinte
- **Feature adoption**: 60% dos usuários utilizam troca de empresa sem re-login semanalmente
- **Self-sufficiency**: 95% dos usuários completam criação de empresa + convite de colaboradores sem suporte técnico

---

### Business Objectives

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

---

### Key Performance Indicators

**Acquisition & Activation:**
- **Sign-up to company created**: < 10 minutos (P95)
- **New companies per month**: Crescimento de 20% mês-a-mês
- **Failed provisioning rate**: < 1%

**Engagement & Retention:**
- **DAU/MAU ratio**: > 40% (indicando uso frequente)
- **Weekly company switches (multi-empresa users)**: Média de 5+ trocas/semana
- **Collaboration invites sent per company**: Média de 3 convites nos primeiros 30 dias
- **D7 retention**: > 85%
- **D30 retention**: > 70%

**Technical Health:**
- **Tenant creation success rate**: > 99%
- **Schema provisioning time**: < 30 segundos (P95)
- **Context switch latency**: < 500ms
- **Critical errors logged (public schema)**: < 5 por semana

**Business Impact:**
- **Support tickets related to onboarding**: Redução de 90% vs. baseline
- **Revenue per active company**: Crescimento de 15% trimestre-a-trimestre (futura monetização)
- **Churn rate**: < 10% mensal

## MVP Scope

### Core Features

**1. Criação Self-Service de Empresa (Tenant)**
- Tela de cadastro de empresa após login Google OAuth (para usuários sem empresa)
- Campos básicos: Nome da empresa, CNPJ (opcional), dados de contato
- Criação automática de tenant + schema PostgreSQL isolado
- Loading state durante provisionamento
- Registro de erros críticos no schema público
- Usuário criador automaticamente vira admin da empresa

**2. Seleção e Troca de Contexto de Empresa**
- Tela de seleção de empresa após login (para usuários vinculados a múltiplas empresas)
- Opção "Criar nova empresa" sempre disponível
- Troca de contexto via avatar (menu dropdown) sem re-login
- Atualização de contexto em tempo real (usuário pode dar F5 e ver mudanças)
- Latência < 500ms para troca de contexto

**3. Gestão de Colaboradores**
- Admin convida colaboradores por email
- Vinculação automática com status "ativo" (sem fluxo de aprovação)
- Colaborador vê empresa vinculada após login ou refresh
- Admin pode remover colaboradores
- Admin pode promover colaboradores para admin
- Suporte a múltiplos admins por tenant

**4. Gestão de Empresa**
- Admin pode editar dados da empresa (nome, CNPJ, contato)
- Admin pode deletar empresa (com proteção contra tenant órfão)
- Visualização de lista de colaboradores e seus perfis

**5. Sistema de Permissões**
- Integração com sistema existente: Usuário → Perfil → Roles
- Permissões customizáveis via perfis já implementados
- Validação de permissões por contexto de tenant ativo

---

### Out of Scope for MVP

**Sistema de Notificações:**
- Notificações in-app (centro de notificações)
- Email de convite para colaboradores
- Status "pendente" e "inativo" de convite (preparados no modelo, não usados no MVP)

**Comunicação Proativa:**
- Email automático ao criar empresa
- Notificação de adição como colaborador
- Alertas de mudanças em permissões

**Funcionalidades Avançadas:**
- Transferência de ownership de empresa
- Auditoria detalhada de ações (quem criou, quando, mudanças)
- Limites de empresas ou colaboradores por usuário
- Billing/cobrança por empresa

---

### MVP Success Criteria

**Critérios Técnicos:**
- 99%+ de taxa de sucesso na criação de tenant/schema
- < 10 minutos para criar empresa (P95)
- < 500ms latência de troca de contexto
- Zero intervenções manuais necessárias

**Critérios de Usuário:**
- 90%+ dos usuários completam criação de empresa sem suporte
- 85%+ retention D7 (usuários retornam após criar empresa)
- 30%+ dos usuários criam 2+ empresas em 90 dias
- 80%+ dos admins convidam pelo menos 1 colaborador em 30 dias

**Critérios de Negócio:**
- Redução de 100% em tempo de equipe técnica para provisionamento
- 3x crescimento em novas empresas criadas mensalmente
- < 0.5% de tenants órfãos ou com problemas

**Go/No-Go Decision Points:**
- Se < 80% completion rate → investigar fricções no fluxo
- Se > 2% error rate → pausar e corrigir problemas de infraestrutura
- Se < 60% D7 retention → revisar proposta de valor e onboarding

---

### Future Vision

**Fase 2 - Colaboração Aprimorada (3-6 meses):**
- Sistema completo de notificações in-app e email
- Fluxo de aprovação de convites (aceitar/recusar)
- Status avançados de colaboradores (pendente, inativo, bloqueado)
- Auditoria e histórico de ações por empresa

**Fase 3 - Integrações & Marketplace (6-12 meses):**
- Integração com marketplaces (Mercado Livre, Shopee, Amazon)
- Publicação de produtos em múltiplos canais com 1 clique
- Sincronização automática de estoque entre canais
- Integração com delivery (iFood, Rappi, Uber Eats)

**Fase 4 - Ecossistema & Escala (12-24 meses):**
- API pública para integrações customizadas
- SDK para desenvolvedores third-party
- Marketplace de apps/plugins
- Integrações com ERP, contabilidade, fiscal
- Analytics avançados e BI integrado
- Mobile app (iOS/Android)
- White-label para revendedores

**Expansão de Mercado:**
- Suporte a outros países (internacionalização)
- Segmentos verticais específicos (farmácias, restaurantes, e-commerce)
- Features enterprise para empresas 100+ colaboradores
- Multi-currency e multi-idioma