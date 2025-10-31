# Project Brief: Estoque Central

---

## Executive Summary

**Estoque Central** é um sistema ERP omnichannel focado em gestão de estoque para pequenas e médias empresas brasileiras que operam múltiplos canais de venda - desde distribuidoras B2B até varejos com lojas físicas e presença em marketplaces.

### O Problema

Empresas que vendem simultaneamente através de marketplaces (Mercado Livre, Shopee, iFood, Amazon), lojas físicas e operações de distribuição enfrentam desafios críticos:

- **Overselling:** Vender o mesmo produto em múltiplos canais sem atualização de estoque em tempo real, resultando em cancelamentos, avaliações negativas e perda de reputação
- **Falta de visibilidade:** Impossibilidade de saber quanto estoque está disponível considerando pedidos pendentes em diferentes canais
- **Retrabalho manual:** Atualização manual de planilhas Excel para controlar vendas de cada canal, consumindo horas diárias de trabalho operacional
- **Compliance fiscal complexo:** Emissão de notas fiscais (NFCe/SAT) desconectada do controle de estoque, gerando inconsistências contábeis
- **Multi-depósito sem controle:** Produtos distribuídos em loja, almoxarifado e CD sem rastreabilidade de movimentações
- **Barreira à expansão:** Impossibilidade de escalar para novos canais sem contratar mais pessoas para controle manual

### A Solução

Sistema ERP web unificado que centraliza:

**Gestão de Estoque Multi-Depósito:**
- Cadastro e controle de múltiplos locais de armazenamento (loja, depósito, CD)
- Rastreabilidade completa de movimentações entre estoques com histórico auditável
- Alertas de estoque mínimo por produto/categoria

**Integração Omnichannel:**
- Integração nativa com API do Mercado Livre (MVP) com sincronização de:
  - Produtos e preços
  - Estoque disponível
  - Pedidos recebidos
- Preparado para expansão: Shopee, iFood, Amazon (roadmap)

**Interfaces de Venda Especializadas:**
- **PDV (Point of Sale):** Interface otimizada para toque/tablet para operadores de caixa em varejo, com:
  - Emissão de NFCe/SAT integrada
  - Fluxo rápido de vendas (código de barras, busca, finalização)
  - Controle de formas de pagamento
- **Ordem de Venda:** Interface desktop para operações B2B de distribuição, com:
  - Cotações e pedidos complexos
  - Gestão de clientes corporativos
  - Condições comerciais diferenciadas

**Gestão de Compras:**
- CRUD de fornecedores
- Ordens de compra com entrada de estoque
- Controle de custos para cálculo de margem

**Controle de Acesso:**
- Sistema de usuários com profiles e roles
- Permissões granulares (ex: caixa só acessa PDV, gerente acessa relatórios)

### Mercado-Alvo

**Perfil Primário:** PMEs brasileiras com faturamento entre R$ 50k - R$ 500k/mês que:
- Já vendem em pelo menos 2 canais diferentes
- Possuem entre 100 - 5.000 SKUs
- Têm 2-20 funcionários
- Atualmente usam planilhas ou sistemas básicos sem integração

**Segmentos específicos:**
- Mercadinhos e minimercados com vendas presenciais + Mercado Livre
- Pet shops com loja física + múltiplos marketplaces
- Distribuidoras de materiais (construção, elétrica) com B2B + marketplaces
- Lojas de produtos naturais/suplementos

### Proposta de Valor

"**Venda em todos os canais sem medo de overselling, com estoque sempre sincronizado e nota fiscal automatizada**"

**Benefícios mensuráveis esperados:**
- Redução de 80%+ do tempo gasto em controle manual de estoque
- Eliminação de overselling e cancelamentos por falta de produto
- Aumento de 20-30% nas vendas por visibilidade real de estoque disponível
- Compliance fiscal 100% sem retrabalho

**Diferenciais competitivos:**
- Único sistema acessível que combina PDV fiscal + integração marketplace + multi-depósito
- Projetado para realidade brasileira (fiscal, marketplaces locais)
- Arquitetura preparada para expansão modular (financeiro, produção)

---

## Problem Statement

### Estado Atual: O Caos da Gestão Multi-Canal

Pequenas e médias empresas brasileiras que expandiram suas operações para múltiplos canais de venda (lojas físicas, marketplaces, distribuição B2B) enfrentam um problema estrutural: **não existe uma solução integrada e acessível que unifique gestão de estoque, vendas omnichannel e compliance fiscal**.

**Cenário típico atual:**

Uma empresa opera com:
- **Planilha Excel #1:** Controle de estoque atualizado manualmente
- **Planilha Excel #2:** Vendas do Mercado Livre (baixadas e lançadas manualmente)
- **Planilha Excel #3:** Vendas da loja física
- **Sistema A:** Software de PDV isolado para emissão de NFCe
- **Sistema B:** Painel do Mercado Livre para gestão de pedidos
- **Whatsapp/Papel:** Controle de movimentações entre depósitos

**Resultado:** Cada venda requer atualização manual em múltiplos sistemas, informações desencontradas, e horas diárias de retrabalho.

### Pain Points Críticos

**1. Overselling e Perda de Reputação**
- Cliente compra no Mercado Livre um produto que acabou de ser vendido na loja física
- Sistema não atualiza estoque entre canais
- Empresa precisa cancelar pedido, gerando avaliação negativa
- Reputação no marketplace afetada, reduzindo visibilidade futura
- **Impacto:** 15-30% dos cancelamentos em marketplaces são por falta de estoque

**2. Invisibilidade de Estoque Real**
- Impossível saber em tempo real quanto estoque está disponível
- Produtos "presos" em pedidos pendentes não são reservados
- Não há visão consolidada entre múltiplos depósitos
- Decisões de compra são feitas "no escuro"
- **Impacto:** Compras excessivas (capital parado) ou rupturas (vendas perdidas)

**3. Retrabalho Manual Insustentável**
- Operador precisa baixar planilha do Mercado Livre diariamente
- Lançamento manual de cada venda em controle de estoque
- Conferência manual entre sistemas diferentes
- Processo se torna gargalo conforme empresa cresce
- **Impacto:** 2-4 horas/dia de trabalho operacional para empresas com 50+ pedidos/dia

**4. Compliance Fiscal Desconectado**
- NFCe emitida no PDV não dá baixa automática no estoque
- Necessidade de conciliação manual diária
- Risco de divergências contábeis em auditorias
- Fechamento mensal complexo e demorado
- **Impacto:** Risco fiscal + 1-2 dias/mês extras para fechamento

**5. Barreira à Expansão**
- Adicionar novo marketplace requer contratar pessoa para controle manual
- Impossível escalar sem aumentar proporcionalmente equipe operacional
- Expansão para novos canais é inviável operacionalmente
- **Impacto:** Oportunidades de crescimento não capturadas

**6. Decisões Sem Dados**
- Impossível saber qual produto vende mais em qual canal
- Não há dados para negociar com fornecedores
- Margens calculadas manualmente ou estimadas
- **Impacto:** Decisões estratégicas baseadas em intuição, não dados

### Por que Soluções Existentes Falham

**ERPs Tradicionais (TOTVS, SAP Business One):**
- ❌ Custo proibitivo (R$ 500+ por usuário/mês)
- ❌ Complexidade excessiva (meses de implantação)
- ❌ Não possuem integração nativa com marketplaces brasileiros
- ❌ PDV e marketplace são módulos separados caros

**Sistemas de PDV (Vend, SisVendas):**
- ❌ Focados apenas em loja física
- ❌ Não integram com marketplaces
- ❌ Não suportam múltiplos depósitos

**Integradores de Marketplace (Bling, Tiny):**
- ❌ Não possuem PDV com emissão fiscal
- ❌ Controle de estoque limitado
- ❌ Interface não otimizada para operação de caixa

**Planilhas Excel:**
- ❌ Sem integração
- ❌ Propenso a erros
- ❌ Não escala

### Urgência e Importância

**Por que resolver agora:**

1. **Crescimento do omnichannel:** Empresas que não integram canais perdem competitividade
2. **Marketplaces punem overselling:** Algoritmos priorizam vendedores com baixo cancelamento
3. **Custos operacionais crescentes:** Salários e rotatividade tornam controle manual inviável
4. **Compliance fiscal rigoroso:** SEFAZ aumentando fiscalização eletrônica
5. **Janela de oportunidade:** Mercado carente de soluções acessíveis para PMEs

**Custo de não resolver:**
- Perda de 20-40% de potencial de vendas por rupturas de estoque
- Custos operacionais 2-3x maiores que necessário
- Risco de multas fiscais
- Impossibilidade de crescer sem aumentar headcount proporcionalmente

---

## Proposed Solution

### Conceito Central: ERP Unificado Omnichannel-First

**Estoque Central** é um sistema ERP web desenvolvido especificamente para empresas brasileiras omnichannel, com três pilares fundamentais:

1. **Single Source of Truth para Inventário:** Todos os canais consultam e atualizam o mesmo estoque em tempo real
2. **Integrações Nativas com Marketplaces:** Sincronização bidirecional automática via APIs oficiais
3. **Compliance Fiscal Brasileiro Integrado:** Emissão de NFCe/SAT conectada ao fluxo de vendas e estoque

### Abordagem Arquitetural

**Sistema Web Centralizado:**
- Aplicação web acessível de qualquer dispositivo (desktop, tablet, smartphone)
- Backend centralizado gerencia estado único do estoque
- Interfaces especializadas para cada tipo de operação (PDV touchscreen vs Ordem de Venda desktop)

**Fluxo de Sincronização:**

```
[Mercado Livre API] ←→ [Estoque Central] ←→ [PDV Loja Física]
                              ↕
                    [Múltiplos Depósitos]
                              ↕
                   [Sistema de Compras]
```

**Atualização em tempo real:**
- Venda no PDV → Estoque atualizado → Mercado Livre sincronizado automaticamente
- Venda no Mercado Livre → Pedido importado → Estoque reservado/baixado → Separação no depósito
- Transferência entre depósitos → Rastreada com origem/destino → Histórico auditável

### Diferenciais da Solução

**1. Omnichannel Nativo (não "frankenstein"):**
- Diferente de integradores que conectam sistemas separados, Estoque Central é **um único sistema** que entende todos os canais nativamente
- Não há "sincronização" entre sistemas - há apenas uma fonte de verdade
- Elimina latência, conflitos e inconsistências de soluções integradas via middleware

**2. Interfaces Especializadas por Contexto:**
- **PDV para Varejo:** Interface touchscreen otimizada para velocidade (3-5 toques para finalizar venda), com leitor de código de barras, impressora fiscal integrada
- **Ordem de Venda para Distribuição:** Interface desktop com recursos avançados (múltiplos itens, descontos complexos, condições de pagamento, histórico de cliente)
- Mesma base de dados, experiências otimizadas para cada caso de uso

**3. Compliance Fiscal como Feature Core:**
- NFCe/SAT não é "add-on" - é parte integral do fluxo de venda
- Venda no PDV → Emissão fiscal → Baixa de estoque em **uma transação atômica**
- Garante consistência contábil por design, não por processos manuais

**4. Multi-Depósito Inteligente:**
- Não apenas "múltiplas localizações", mas gestão real de fluxo entre depósitos
- Transferências rastreadas com motivo, responsável e timestamp
- Permite estratégias como "reservar estoque da loja para marketplace" ou "priorizar depósito central"

**5. Preparado para Expansão Modular:**
- Arquitetura permite adicionar módulos (Financeiro, Produção, CRM) sem reescrever core
- Cada módulo se integra ao estoque/vendas que já é a base sólida
- Cliente começa simples (estoque + vendas) e expande conforme cresce

### Por Que Esta Solução Terá Sucesso

**1. Resolve Dor Imediata e Crítica:**
- Overselling é um problema **agora**, não teórico
- Empresas estão perdendo dinheiro **hoje** com controle manual
- Solução entrega valor mensurável desde o dia 1

**2. Posicionamento de Mercado Único:**
- ERPs tradicionais: muito caros e complexos para PMEs
- PDVs simples: não integram marketplace
- Integradores: não têm PDV fiscal
- **Estoque Central:** único no sweet spot de "completo + acessível + omnichannel"

**3. Barreira de Entrada Protege Vantagem:**
- Integração com Mercado Livre API requer expertise técnica
- Compliance fiscal brasileiro (NFCe/SAT) é complexo
- Multi-tenancy com performance requer arquitetura sólida
- Competidores precisariam de meses/anos para replicar

**4. Modelo de Receita Recorrente Escalável:**
- SaaS mensal gera receita previsível
- Custo marginal baixo para adicionar clientes (multi-tenancy)
- Upsell natural (começam com 1 canal, expandem para múltiplos)

**5. Network Effects de Integrações:**
- Cada nova integração de marketplace aumenta valor para todos os clientes
- Clientes têm incentivo para ficar (switching cost de reconfigurar integrações)
- Dados agregados permitem insights de mercado valiosos no futuro

### Visão de Alto Nível do Produto

**Para o Dono da Empresa:**
- Dashboard consolidado: vendas de todos os canais, estoque total, produtos em ruptura
- Relatórios de performance por canal (qual marketplace vende mais?)
- Alertas automáticos (estoque mínimo atingido, pedido urgente no ML)

**Para o Operador de Caixa (Varejo):**
- Interface PDV simples e rápida
- Leitor de código de barras funciona imediatamente
- Impressora fiscal emite nota automaticamente
- Não precisa "dar baixa manual" - tudo automático

**Para o Vendedor (Distribuição):**
- Cria Ordem de Venda consultando estoque em tempo real
- Vê histórico de compras do cliente
- Aplica tabela de preços específica do cliente
- Envia pedido para separação no depósito correto

**Para o Estoquista:**
- Recebe notificação de pedidos para separação
- Faz transferência entre depósitos registrando origem/destino
- Dá entrada de mercadoria de ordens de compra
- Faz inventário com importação de planilha ou contagem por app

**Para o Comprador:**
- Vê relatório de produtos com estoque baixo
- Cria ordem de compra para fornecedor
- Quando mercadoria chega, dá entrada no depósito correto
- Sistema calcula custo médio automaticamente

---

## Target Users

### Segmento Primário: Varejo Omnichannel (Loja Física + Marketplace)

**Perfil Firmográfico:**
- **Tipo de negócio:** Varejo com presença física e vendas online
- **Segmentos verticais:** Mercadinhos, pet shops, farmácias, lojas de materiais, lojas de produtos naturais/suplementos, perfumarias
- **Faturamento mensal:** R$ 50k - R$ 300k
- **Número de SKUs:** 200 - 3.000 produtos
- **Equipe:** 3-15 funcionários (1-2 donos/gerentes, 2-8 operadores de caixa/vendedores, 1-3 estoquistas)
- **Localização:** Capitais e cidades médias (50k+ habitantes)
- **Tempo de operação:** 2-10 anos no mercado físico, 6 meses - 3 anos em marketplaces

**Perfil Demográfico (Decisor):**
- **Cargo:** Dono, sócio ou gerente geral
- **Idade:** 30-55 anos
- **Educação:** Ensino médio completo a superior (administração, gestão)
- **Familiaridade tecnológica:** Média (usa smartphone, WhatsApp Business, planilhas Excel básicas)

**Comportamentos e Workflows Atuais:**

*Manhã (8h-12h):*
- Abre loja física, confere caixa
- Checa pedidos do Mercado Livre no painel web
- Imprime pedidos e passa para estoquista separar
- Atende clientes no balcão usando PDV básico
- Atualiza planilha de vendas manualmente quando tem tempo

*Tarde (13h-18h):*
- Continua atendimento na loja física
- Recebe fornecedor e confere mercadoria
- Anota entrada de produtos em caderno ou planilha
- Atualiza preços no Mercado Livre manualmente
- Checa se produtos vendidos online ainda têm estoque

*Noite (após fechamento):*
- Fecha caixa, confere vendas do dia
- Baixa relatório de vendas do ML
- Tenta conciliar vendas online + físicas na planilha
- Identifica produtos que acabaram e precisa repor
- 2-3x por semana: passa 1-2h atualizando controles

**Necessidades Específicas e Pain Points:**

1. **Overselling é pesadelo recorrente:**
   - "Vendi no ML um produto que acabou de sair na loja e não atualizei"
   - Medo constante de cancelamento e avaliação negativa
   - Precisa pausar anúncios manualmente quando estoque fica baixo

2. **Retrabalho administrativo rouba tempo do negócio:**
   - "Passo mais tempo atualizando planilha do que vendendo"
   - Quer focar em atendimento, compras estratégicas, marketing
   - Tarefas operacionais consomem energia pós-expediente

3. **Invisibilidade do estoque real:**
   - "Não sei se posso aceitar pedido grande porque não sei quanto tenho disponível"
   - Estoque está espalhado (depósito, loja, reservado para pedidos)
   - Decisões de reposição são "no chute"

4. **Compliance fiscal é complexo:**
   - PDV emite NFCe mas não integra com controle
   - Precisa conciliar notas com estoque manualmente
   - Medo de auditoria da SEFAZ

5. **Quer crescer mas não consegue:**
   - Tem oportunidade de vender em mais marketplaces (Shopee, Amazon)
   - Mas não tem estrutura operacional para controlar mais canais
   - Contratar pessoa só para controle é inviável financeiramente

**Objetivos/Goals:**
- ✅ Vender em múltiplos canais sem risco de overselling
- ✅ Reduzir tempo gasto em controles manuais para quase zero
- ✅ Ter visão clara de estoque disponível a qualquer momento
- ✅ Garantir compliance fiscal sem esforço adicional
- ✅ Escalar para novos canais sem aumentar headcount proporcionalmente
- ✅ Tomar decisões de compra baseadas em dados, não intuição

**Persona Exemplo: Carla, dona de pet shop**

*"Tenho uma pet shop há 5 anos. Comecei vendendo no ML há 1 ano e já representa 30% do faturamento, mas é um caos. Toda semana tenho que cancelar algum pedido porque vendeu na loja e não atualizei no ML. Passo 2h toda noite conferindo vendas e atualizando planilhas. Queria vender na Shopee também mas não consigo acompanhar nem o ML direito. Preciso de algo que unifique tudo, senão vou enlouquecer."*

---

### Segmento Secundário: Distribuidoras B2B com Marketplace

**Perfil Firmográfico:**
- **Tipo de negócio:** Distribuição/atacado com expansão para varejo online
- **Segmentos verticais:** Distribuidoras de materiais de construção/elétrica, alimentos, cosméticos, peças, produtos de limpeza
- **Faturamento mensal:** R$ 100k - R$ 500k
- **Número de SKUs:** 500 - 5.000 produtos
- **Equipe:** 5-20 funcionários (2-3 vendedores B2B, 2-5 estoquistas, 1-2 compradores, 1 financeiro, demais logística)
- **Localização:** Centros de distribuição em cidades médias/grandes
- **Tempo de operação:** 5-15 anos no mercado B2B, 1-3 anos em marketplaces

**Perfil Demográfico (Decisor):**
- **Cargo:** Sócio-diretor, gerente comercial ou gerente de operações
- **Idade:** 35-60 anos
- **Educação:** Superior (administração, engenharia, logística)
- **Familiaridade tecnológica:** Média-alta (usa ERPs básicos, sistemas de gestão)

**Comportamentos e Workflows Atuais:**

*Operação B2B (core business):*
- Vendedores visitam clientes (lojas, restaurantes, obras)
- Tiram pedidos por WhatsApp, telefone ou presencial
- Enviam pedidos para digitação no sistema
- Separação no CD e entrega com caminhão próprio
- Faturamento com nota fiscal de venda

*Operação Marketplace (novo canal):*
- Pessoa dedicada monitora pedidos do Mercado Livre
- Imprime pedidos e envia para separação
- Estoque do ML é o mesmo do B2B (conflitos frequentes)
- Embala e despacha via Correios/transportadora
- Atualiza tracking no painel do ML

*Controle atual:*
- Sistema legado para pedidos B2B (Access, ERP antigo)
- Planilhas para controlar marketplace
- Estoque atualizado manualmente (ou nem atualizado)
- Comprador usa relatórios básicos + experiência para repor

**Necessidades Específicas e Pain Points:**

1. **Canais competem pelo mesmo estoque:**
   - "Cliente B2B grande pede 100 unidades, mas 50 estão 'presas' em anúncios do ML"
   - Difícil priorizar: B2B (margem menor, volume maior) vs Marketplace (margem maior, varejo)

2. **Pedidos B2B são complexos:**
   - Tabelas de preço diferenciadas por cliente
   - Condições de pagamento variadas (boleto, prazo, cartão)
   - Pedidos grandes com múltiplos itens
   - Precisa de sistema robusto, não "PDV simples"

3. **Visibilidade multi-depósito crítica:**
   - Produtos em CD principal, filial, loja física (se tiver)
   - Transferências entre locais são frequentes
   - Precisa rastrear onde cada produto está

4. **Oportunidade de marketplaces sub-explorada:**
   - ML funciona bem mas operação é manual demais
   - Poderia vender muito mais se tivesse integração
   - Outros marketplaces (Shopee, Amazon) ficam de fora por limitação operacional

5. **Margem é apertada, controle precisa ser preciso:**
   - Erro de custo ou overselling impacta rentabilidade
   - Necessita custo médio automático, margens calculadas
   - Relatórios de performance por canal para decisões estratégicas

**Objetivos/Goals:**
- ✅ Unificar gestão de estoque entre operação B2B e marketplaces
- ✅ Permitir vendedores B2B consultarem estoque real em tempo real
- ✅ Automatizar processamento de pedidos de marketplace
- ✅ Rastrear produtos entre múltiplos depósitos
- ✅ Expandir para novos marketplaces sem contratar equipe adicional
- ✅ Obter dados para decidir alocação de estoque entre canais

**Persona Exemplo: Roberto, sócio de distribuidora de material elétrico**

*"Temos uma distribuidora há 12 anos vendendo para lojas e eletricistas. Há 2 anos começamos no Mercado Livre e já faz quase 25% do faturamento. O problema é que o estoque é o mesmo - quando um cliente B2B grande faz pedido, às vezes não tenho e está anunciado no ML. Ou pior: vendo no ML e não tenho porque saiu para cliente B2B. Precisamos de um sistema que gerencie tudo junto, mostre estoque real e permita expandir para Amazon sem virar bagunça."*

---

## Goals & Success Metrics

### Business Objectives

**Objetivo 1: Validar Product-Market Fit no Segmento Varejo Omnichannel**
- **Meta (6 meses):** 30 clientes pagantes ativos usando o sistema em produção
- **Métrica de validação:** Taxa de retenção mensal > 85% (churn < 15%)
- **Critério de sucesso:** NPS > 40 indicando satisfação e recomendação

**Objetivo 2: Comprovar Redução de Overselling como Proposta de Valor Central**
- **Meta (3 meses pós-onboarding):** Redução de 90%+ em cancelamentos por falta de estoque
- **Métrica de validação:** Comparar cancelamentos pré vs pós-implementação
- **Critério de sucesso:** Clientes reportam melhoria na reputação do marketplace (score ML aumentado)

**Objetivo 3: Demonstrar ROI Mensurável em Redução de Tempo Operacional**
- **Meta:** Economia de 70%+ do tempo gasto em controle manual de estoque
- **Métrica de validação:** Tempo semanal dedicado a controles (baseline vs após implementação)
- **Critério de sucesso:** Cliente economiza 8-10h/semana em tarefas administrativas

**Objetivo 4: Estabelecer Integração com Mercado Livre como Diferencial Competitivo**
- **Meta (MVP):** Sincronização bidirecional funcional de estoque, produtos e pedidos
- **Métrica de validação:**
  - Latência de sincronização < 5 minutos
  - Taxa de erro de sincronização < 1%
- **Critério de sucesso:** Cliente confia no sistema e não faz controle manual paralelo

**Objetivo 5: Validar Modelo de Receita SaaS Sustentável**
- **Meta (12 meses):** MRR de R$ 30k (30 clientes x R$ 1k/mês como referência inicial)
- **Métrica de validação:**
  - CAC (Custo de Aquisição de Cliente) < R$ 2.000
  - LTV:CAC ratio > 3:1
- **Critério de sucesso:** Margem bruta > 70% (típica de SaaS)

### User Success Metrics

**Métrica 1: Eliminação de Overselling**
- **Definição:** % de redução em cancelamentos de pedidos por falta de estoque
- **Target:** Redução de 90%+ em 3 meses
- **Método de medição:** Comparar taxa de cancelamento ML (pré vs pós) via API do Mercado Livre

**Métrica 2: Redução de Trabalho Manual**
- **Definição:** Horas semanais economizadas em controle/atualização de estoque
- **Target:** Economia de 8+ horas/semana
- **Método de medição:** Pesquisa com cliente (baseline ao onboarding, follow-up mensal)

**Métrica 3: Aumento de Visibilidade de Estoque**
- **Definição:** Usuário consegue responder "quanto tenho disponível do produto X?" instantaneamente
- **Target:** 100% das consultas respondidas em < 3 segundos
- **Método de medição:** Performance do sistema + pesquisa de satisfação

**Métrica 4: Compliance Fiscal Sem Retrabalho**
- **Definição:** % de NFCe emitidas que dão baixa automática no estoque
- **Target:** 100% (zero exceções ou ajustes manuais)
- **Método de medição:** Logs do sistema + auditoria de reconciliação

**Métrica 5: Habilitação para Expansão de Canais**
- **Definição:** Clientes que adicionaram novo canal de venda após adotar o sistema
- **Target:** 30% dos clientes expandem para 2º marketplace em 6 meses
- **Método de medição:** Tracking de configuração de integrações no sistema

**Métrica 6: Qualidade de Decisões de Compra**
- **Definição:** Redução de rupturas de estoque + redução de estoque parado
- **Target:**
  - Rupturas: -50%
  - Giro de estoque: +20%
- **Método de medição:** Relatórios de estoque do sistema (antes vs depois)

### Key Performance Indicators (KPIs)

**KPIs de Produto:**

1. **Uptime do Sistema: 99.5%+**
   - Definição: Disponibilidade da aplicação web
   - Target: < 3.6 horas de downtime por mês
   - Importância: Sistema crítico para operação diária do cliente

2. **Latência de Sincronização ML < 5 minutos**
   - Definição: Tempo entre venda no PDV e atualização de estoque no Mercado Livre
   - Target: 95% das sincronizações em < 5min
   - Importância: Previne overselling efetivamente

3. **Taxa de Erro de Sincronização < 1%**
   - Definição: % de operações de sincronização que falharam
   - Target: 99%+ de sucesso
   - Importância: Confiabilidade é crítica para adoção

4. **Performance do PDV: Checkout < 30 segundos**
   - Definição: Tempo médio entre scan do primeiro item e emissão de NFCe
   - Target: < 30s para transação típica (5 itens)
   - Importância: Experiência de caixa deve ser rápida

5. **Precisão de Estoque: 99%+**
   - Definição: Acuracidade entre estoque físico e estoque no sistema
   - Target: 99%+ de acuracidade em auditorias
   - Importância: Estoque incorreto invalida proposta de valor

**KPIs de Negócio:**

6. **Monthly Recurring Revenue (MRR): R$ 30k em 12 meses**
   - Definição: Receita recorrente mensal
   - Target inicial: R$ 30k (30 clientes)
   - Importância: Viabilidade financeira do produto

7. **Churn Rate < 5% mensal**
   - Definição: % de clientes que cancelam o serviço por mês
   - Target: < 5% (melhor classe: < 2%)
   - Importância: Retenção é mais importante que aquisição

8. **Net Promoter Score (NPS) > 40**
   - Definição: Likelihood de recomendar (0-10, NPS = %promoters - %detractors)
   - Target: > 40 (considerado excelente para B2B SaaS)
   - Importância: Indicador de satisfação e potencial de growth orgânico

9. **Time to Value < 2 semanas**
   - Definição: Tempo entre signup e primeira venda processada no sistema
   - Target: Cliente operacional em < 14 dias
   - Importância: Onboarding lento aumenta churn

10. **Customer Acquisition Cost (CAC) < R$ 2.000**
    - Definição: Custo total de marketing/vendas por cliente adquirido
    - Target: < R$ 2.000 por cliente
    - Importância: Sustentabilidade do modelo de crescimento

**KPIs de Uso (Indicadores de Adoção):**

11. **Daily Active Users / Total Users > 80%**
    - Definição: % de usuários que usam sistema diariamente
    - Target: > 80% (sistema crítico deve ser usado todo dia)
    - Importância: Baixa adoção indica problema de UX ou value prop

12. **Transações Processadas por Cliente > 100/mês**
    - Definição: Média de vendas (PDV + Ordem de Venda) por cliente/mês
    - Target: > 100 transações/mês
    - Importância: Volume indica depth of adoption

13. **Integrações Ativas > 90% dos clientes**
    - Definição: % de clientes com pelo menos 1 marketplace integrado
    - Target: > 90% (integração é core value prop)
    - Importância: Clientes sem integração não veem valor completo

---

## MVP Scope

### Core Features (Must Have)

**1. Gestão de Usuários e Autenticação**

**Sistema de Permissões (3 camadas):**
- **Role (Papel):** Define acesso a uma seção/módulo específico do sistema
  - Exemplos: "Vendas", "Estoque", "Compras", "Relatórios", "Configurações", "PDV", "Integração Marketplace"
  - CRUD de Roles com nome e descrição
- **Profile (Perfil):** Agrupamento de múltiplos Roles
  - Exemplos:
    - Profile "Gerente": Roles [Vendas, Estoque, Compras, Relatórios, Configurações]
    - Profile "Caixa": Roles [PDV]
    - Profile "Estoquista": Roles [Estoque, Recebimento Mobile]
    - Profile "Vendedor B2B": Roles [Vendas, Relatórios]
  - CRUD de Profiles com seleção de Roles
- **User (Usuário):** Associado a um Profile
  - Campos: nome, email, Google ID, profile associado, foto (do Google), status (ativo/inativo)
  - Usuário herda permissões do Profile

**Autenticação:**
- **Google OAuth 2.0 EXCLUSIVAMENTE:**
  - Botão "Login with Google"
  - Fluxo OAuth para autorização
  - Armazenar Google ID, email, nome, foto
  - Sessão via JWT ou session token
  - Logout
- **Primeiro Acesso:**
  - Admin cria usuário no sistema com email
  - Usuário faz login com Google (valida se email coincide)
  - Atribui Profile ao usuário
- **Segurança:**
  - Middleware valida Profile/Roles em cada rota
  - Não permitir acesso a módulos sem Role correspondente
- **Rationale:** OAuth com Google elimina gerenciamento de senhas, 2FA automático se usuário tiver configurado no Google, mais seguro e melhor UX

---

**2. Gestão de Produtos (Multi-Tipo)**

**Tipos de Produtos:**

**A. Produto Simples:**
- SKU único, sem variações
- Exemplo: Controle remoto modelo XYZ
- Campos:
  - Nome, SKU, código de barras (EAN)
  - Descrição, categoria (FK com hierarquia)
  - Preço de venda, custo
  - Unidade de medida (un, kg, cx, etc.)
  - Controla estoque? (boolean)
  - Status (ativo/inativo)

**B. Produto com Variantes (Matriz):**
- Produto pai com múltiplas variantes (cada variante = SKU único)
- Exemplo: Camisa Polo
  - Variantes: Cor (Azul, Preto, Branco) x Tamanho (P, M, G, GG)
  - Cada combinação é um SKU: CAMISA-POLO-AZUL-P, CAMISA-POLO-AZUL-M, etc.
- **Estrutura:**
  - **Produto Pai:** Nome genérico, categoria, descrição base
  - **Atributos de Variação:** Definir eixos (ex: Cor, Tamanho)
  - **Valores dos Atributos:** Azul/Preto/Branco, P/M/G/GG
  - **Variantes (SKUs):** Cada combinação gera SKU com:
    - SKU único, código de barras próprio
    - Preço, custo (pode herdar do pai ou ser específico)
    - Estoque controlado por variante
- **Interface:**
  - Cadastro de produto com opção "Produto com Variantes"
  - Definir atributos e valores
  - Gerar matriz automaticamente ou cadastrar variantes manualmente
  - Importação via CSV deve suportar variantes

**C. Produto Composto / Kit (Bill of Materials - BOM):**
- Produto final formado por componentes (outros produtos do sistema)
- Exemplo: Kit Churrasco = 1x Carvão + 1x Acendedor + 2x Espeto
- **Estrutura:**
  - **Produto Composto:** SKU próprio, pode ter código de barras
  - **BOM (Receita):** Lista de componentes
    - Componente (FK para Produto), Quantidade necessária
  - **Controle de Estoque:**
    - Estoque do produto composto é calculado baseado em componentes disponíveis
    - Ex: Se preciso 2x Espeto e tenho 10 espetos, posso montar 5 kits
    - OU estoque pode ser físico (kits pré-montados) - configurável
  - **Venda:**
    - Ao vender kit, dá baixa nos componentes (se BOM virtual)
    - Ou dá baixa no kit montado (se BOM físico)
- **Rationale:** Comum em distribuidoras que vendem kits/combos; manufatura leve (montagem)

**CRUD de Produtos:**
- Cadastro manual com seleção de tipo
- Importação CSV/Excel:
  - Produtos simples: planilha flat
  - Produtos com variantes: planilha com colunas de atributos
  - Produtos compostos: planilha separada para BOM
  - Preview e validação antes de importar
- Busca e filtros por tipo, categoria, SKU, nome, status
- **Rationale:** Produtos com variantes e BOM são necessidades reais; aumenta complexidade mas é diferencial competitivo

---

**3. Gestão de Categorias (Hierárquica)**
- **Estrutura de Árvore:**
  - Categoria pode ter categoria pai (self-reference)
  - Hierarquia ilimitada (ex: Eletrônicos > Informática > Notebooks > Gamers)
- **CRUD de Categorias:**
  - Nome, descrição, categoria pai (nullable)
  - Status (ativo/inativo)
- **Visualização:**
  - Árvore de categorias (com expand/collapse)
  - Breadcrumb ao cadastrar produto (ex: Eletrônicos > Informática > Notebooks)
- **Relatórios:**
  - Produtos por categoria (com opção de incluir subcategorias)
- **Rationale:** Hierarquia facilita organização de catálogos grandes; necessário para importação de marketplaces que usam categorias hierárquicas

---

**4. Gestão de Estoques (Multi-Depósito)**
- **CRUD de Locais de Estoque:**
  - Nome (ex: "Loja Centro", "CD Principal")
  - Endereço (opcional)
  - **Responsável:** FK para User (quem gerencia esse estoque)
  - Status (ativo/inativo)
- **Controle de Estoque por Produto x Local:**
  - Quantidade disponível (por SKU/variante)
  - Quantidade reservada (pedidos pendentes)
  - Quantidade disponível para venda = disponível - reservada
  - **Para Produtos Compostos:** Estoque calculado ou físico (configurável)
- **Alertas de Estoque Mínimo:**
  - Configurar estoque mínimo por produto/variante
  - Listagem de produtos abaixo do mínimo
- **Histórico de Movimentações:**
  - Tipo (entrada, saída, transferência, ajuste, venda, compra, montagem BOM)
  - Quantidade, data/hora, usuário responsável
  - Documento relacionado (ID venda, compra, transferência)
  - Saldo anterior, saldo posterior
- **Rationale:** Responsável por local permite auditoria e controle; essencial para multi-depósito

---

**5. Movimentações entre Estoques**
- **Transferência de Produtos:**
  - Selecionar produto (ou variante), quantidade
  - Estoque origem, estoque destino
  - Motivo/observação
  - Data/hora, usuário responsável
- **Validações:**
  - Estoque origem tem quantidade disponível?
  - Não permitir transferir para mesmo local
- **Atualização Automática:**
  - Saída no estoque origem, entrada no destino
  - Histórico registrado
- **Rationale:** Funcionalidade explicitamente solicitada

---

**6. Gestão de Clientes e Fornecedores**

**CRUD de Clientes:**
- **Pessoa Física:**
  - Nome, CPF, email, telefone
  - Endereço completo (rua, número, bairro, cidade, UF, CEP)
  - Data de nascimento (opcional)
- **Pessoa Jurídica:**
  - Razão social, nome fantasia, CNPJ
  - Inscrição estadual, inscrição municipal
  - Email, telefone, contato (nome pessoa de contato)
  - Endereço completo
- **Campos Comuns:**
  - Tipo (PF/PJ), status (ativo/inativo)
  - Observações
- **Rationale:** Necessário para Ordem de Venda, NF-e futura, histórico de compras

**CRUD de Fornecedores:**
- Mesma estrutura de clientes PJ (razão social, CNPJ, contato, endereço)
- Campos adicionais:
  - Prazo de entrega padrão (dias)
  - Condições de pagamento padrão
- **Rationale:** Necessário para Ordens de Compra

---

**7. PDV (Point of Sale) - Interface de Venda para Varejo**
- **Interface Touchscreen Otimizada:**
  - Layout simples, botões grandes
  - Suporte a leitor de código de barras USB/Bluetooth
  - Busca rápida de produtos (por nome, SKU, código de barras)
  - **Produtos com Variantes:** Ao scannear/buscar produto pai, apresentar grade de variantes para seleção
- **Fluxo de Venda:**
  - Adicionar itens ao carrinho (scan ou busca manual)
  - Editar quantidade, remover item
  - Aplicar desconto (% ou valor) por item ou total
  - Selecionar cliente (opcional, busca rápida ou cadastro simplificado)
  - Selecionar forma de pagamento (dinheiro, débito, crédito, PIX)
  - Finalizar venda
- **Emissão de NFCe/SAT:**
  - Integração com impressora fiscal (via driver/API - ex: Elgin, Bematech)
  - Emissão automática ao finalizar venda
  - Baixa automática de estoque após emissão bem-sucedida
  - **Para Produtos Compostos:** Baixa dos componentes conforme BOM
  - Tratamento de erro (impressora offline, rejeição SEFAZ)
  - Reimpressão de cupom
- **Seleção de Estoque:**
  - PDV vinculado a um local de estoque (configuração)
  - Vendas dão baixa no estoque configurado
- **Rationale:** Core feature para varejo; NFCe é obrigatório legalmente

---

**8. Ordem de Venda - Interface para Operação B2B/Distribuição**
- **Interface Desktop:**
  - Layout tradicional, formulário detalhado
  - Múltiplos itens por pedido
- **Funcionalidades:**
  - Selecionar cliente (busca ou cadastro rápido)
  - Adicionar produtos com quantidade, preço unitário
  - **Produtos com Variantes:** Selecionar variante específica
  - **Produtos Compostos:** Sistema valida se há componentes suficientes
  - Desconto por item ou total
  - Condições de pagamento (à vista, prazo, parcelado)
  - Observações/notas
  - Status (rascunho, confirmado, separando, faturado, cancelado)
- **Consulta de Estoque em Tempo Real:**
  - Ao adicionar produto, mostrar estoque disponível (por local)
  - Alertar se quantidade > disponível
- **Reserva de Estoque:**
  - Ao confirmar pedido, reservar quantidade no estoque selecionado
  - Estoque reservado não disponível para outros canais
- **Emissão de NF-e:**
  - MVP: Exportar dados para XML (importar em sistema fiscal externo)
  - Fase 2: Integração completa com SEFAZ
- **Rationale:** Necessário para distribuição B2B

---

**9. Gestão de Compras**

**CRUD de Fornecedores:** (já coberto na seção 6)

**Ordem de Compra:**
- Selecionar fornecedor
- Adicionar produtos (simples, variantes ou compostos), quantidade, preço de custo unitário
- Data prevista de entrega, observações
- Status (pendente, recebida parcial, recebida total, cancelada)

**Recebimento de Mercadoria - Interface Mobile/Web Mobile:**
- **Contexto:** Estoquista usa celular/tablet para dar entrada de mercadoria no depósito
- **Interface "Irmã do PDV":**
  - Layout mobile-first (touchscreen)
  - Leitor de código de barras via câmera do celular (HTML5 ou app nativo)
  - Busca manual se código de barras não funcionar
- **Fluxo de Recebimento:**
  - Selecionar Ordem de Compra pendente
  - Para cada produto da OC:
    - Scannear código de barras ou buscar manualmente
    - Informar quantidade recebida
    - Se quantidade diferente do esperado, permitir ajuste e adicionar observação
  - Selecionar local de estoque destino
  - Confirmar recebimento
- **Ações Automáticas:**
  - Dar entrada no estoque (quantidade recebida)
  - Atualizar custo do produto (custo médio ponderado)
  - Atualizar status da OC (recebida parcial ou total)
  - Registrar histórico de movimentações
- **Validações:**
  - Não permitir receber mais que o pedido (ou alertar se ultrapassar)
  - Validar se produto pertence à OC
- **Rationale:** Mobilidade facilita operação (estoquista não precisa ir ao computador); código de barras via celular elimina necessidade de scanner dedicado

---

**10. Integração com Mercado Livre (MVP Priority)**
- **Autenticação OAuth2 do Mercado Livre:**
  - Conectar conta ML via OAuth
  - Armazenar e atualizar tokens automaticamente
- **Sincronização de Produtos:**
  - **ML → Estoque Central:** Importar anúncios existentes
    - Produto simples: 1 anúncio = 1 produto
    - Produto com variantes: 1 anúncio ML (com variações) = 1 produto pai + variantes
  - **Estoque Central → ML:** Publicar produtos como anúncios
    - Mapear atributos (título, descrição, preço, categoria ML)
    - **Produtos com variantes:** Criar anúncio ML com variações
    - **Produtos compostos:** Anunciar como produto simples (estoque calculado)
- **Sincronização de Estoque (Bidirecional):**
  - **Estoque Central → ML:** Atualizar quantidade disponível
  - **ML → Estoque Central:** Webhook ou polling para capturar vendas
  - Frequência: a cada venda ou batch a cada 5 min
- **Importação de Pedidos:**
  - Buscar pedidos novos via API
  - Criar registro interno de venda
  - Reservar/dar baixa no estoque configurado
  - **Produtos com Variantes:** Identificar variante vendida
  - **Produtos Compostos:** Baixar componentes
  - Atualizar status no ML (enviado, entregue)
- **Configurações:**
  - Mapear local de estoque para ML
  - Margem de segurança (ex: anunciar 90% do estoque real)
- **Rationale:** Diferencial competitivo; suporte a variantes é essencial (maioria dos produtos em marketplaces tem variações)

---

**11. Dashboard e Relatórios Essenciais**
- **Dashboard Principal:**
  - Vendas do dia (total, por canal)
  - Estoque total em valor
  - Produtos em ruptura (abaixo do mínimo)
  - Pedidos pendentes (ML + OVs)
- **Relatórios Básicos:**
  - Movimentações de estoque (filtros: período, produto, local, tipo)
  - Vendas por período (dia, semana, mês, canal)
  - Produtos mais vendidos (por unidade ou valor)
  - Estoque atual (por local, consolidado, com suporte a variantes e compostos)
  - Compras por fornecedor
  - Curva ABC de produtos
- **Rationale:** Visibilidade e tomada de decisão baseada em dados

---

### Out of Scope for MVP

**Integrações com Outros Marketplaces:**
- ❌ Shopee, iFood, Amazon, B2W
- **Quando:** Fase 2
- **Rationale:** Foco em validar arquitetura com ML primeiro

**Módulo Financeiro Completo:**
- ❌ Contas a pagar/receber, fluxo de caixa, conciliação bancária
- **Quando:** Fase 3
- **Rationale:** Escopo MVP é estoque + vendas

**CRM Avançado:**
- ❌ Pipeline de vendas, tabelas de preço diferenciadas por cliente
- **Quando:** Fase 2/3
- **Rationale:** CRUD de clientes básico é suficiente para MVP

**Produção Complexa:**
- ❌ Ordens de produção programadas, controle de matéria-prima
- **Quando:** Fase 4+ (BOM básico já está no MVP para kits/combos)
- **Rationale:** BOM para kits é suficiente; manufatura completa é outro produto

**Features Avançadas de Estoque:**
- ❌ Lote e validade, número de série, inventário cíclico automatizado
- **Quando:** Fase 2
- **Rationale:** Nice-to-have; aumenta complexidade significativamente

**E-commerce Próprio:**
- ❌ Loja online integrada
- **Quando:** Fase 3+
- **Rationale:** Marketplaces são prioridade

**App Mobile Nativo:**
- ❌ App iOS/Android nativo
- **Quando:** Fase 2
- **Rationale:** Web mobile responsivo é suficiente para MVP

**Modo Offline:**
- ❌ PDV/Recebimento funcionam sem internet
- **Quando:** Fase 2 (se validado)
- **Rationale:** Complexidade arquitetural enorme; premissa de internet confiável

**NF-e Completa:**
- ❌ Emissão e envio direto para SEFAZ de NF-e
- **Quando:** Fase 2
- **Rationale:** MVP tem NFCe + export XML para NF-e

---

### MVP Success Criteria

**O MVP será considerado bem-sucedido se:**

✅ Cliente consegue fazer login via Google OAuth
✅ Cliente consegue cadastrar produtos simples, com variantes e compostos
✅ Cliente consegue importar produtos via CSV
✅ Cliente consegue organizar produtos em categorias hierárquicas
✅ Cliente consegue cadastrar clientes e fornecedores
✅ Cliente consegue fazer venda no PDV (com variantes e compostos) com emissão de NFCe e baixa automática
✅ Cliente consegue fazer Ordem de Venda B2B com reserva de estoque
✅ Cliente consegue criar Ordem de Compra e receber mercadoria via celular (scanning código de barras)
✅ Cliente consegue transferir produtos entre estoques
✅ Cliente consegue conectar Mercado Livre e sincronizar produtos/estoque (incluindo variantes)
✅ Pedidos do ML com variantes são importados corretamente e dão baixa no estoque
✅ Estoque do ML é atualizado quando há venda no PDV (evita overselling)
✅ Cliente confia nos dados e para de usar planilhas
✅ Sistema é estável (uptime > 99%) e rápido (PDV < 30s)

---

## Post-MVP Vision

### Phase 2 Features (Meses 6-12)

**Prioridade: Expandir Integrações Omnichannel**

**1. Integrações com Novos Marketplaces**
- **Shopee:** Integração completa (produtos, estoque, pedidos)
  - Rationale: 2º maior marketplace BR em volume; clientes já pedem
  - Complexidade: Média (arquitetura já validada com ML)
  - Impacto: Aumenta value prop significativamente

- **Amazon Brasil:** Integração completa
  - Rationale: Alto ticket médio; clientes B2B têm interesse
  - Complexidade: Alta (API mais rigorosa, requisitos de seller)
  - Impacto: Diferenciador competitivo forte

- **iFood (para segmento Food & Beverage):**
  - Rationale: Mercadinhos, distribuidoras de alimentos
  - Complexidade: Média
  - Impacto: Abre novo segmento vertical

**Arquitetura de Integrações:**
- Sistema de "conectores" plugáveis
- Interface comum para todas as integrações
- Facilita adicionar novos marketplaces no futuro

**2. Compliance Fiscal Completo**

**NF-e (Nota Fiscal Eletrônica) Completa:**
- Emissão e envio direto para SEFAZ
- Suporte a diferentes tipos (NF-e venda, NF-e devolução, Carta de Correção)
- Manifesto do Destinatário
- Cancelamento e inutilização

**CT-e (Conhecimento de Transporte Eletrônico):**
- Para empresas que fazem entrega própria
- Integração com rastreamento de entrega

**Integração com Certificado Digital A1/A3:**
- Suporte a certificados para assinatura de documentos fiscais

**3. Features Avançadas de Estoque**

**Controle de Lote e Validade:**
- Produtos perecíveis ou com validade (alimentos, cosméticos, medicamentos)
- FEFO (First Expired, First Out) automático
- Alertas de produtos próximos do vencimento

**Número de Série:**
- Rastreabilidade individual (eletrônicos, equipamentos)
- Garantia por número de série

**Inventário Cíclico:**
- Contagem rotativa programada
- App mobile para contagem
- Comparação automática (contado vs sistema)
- Ajustes com aprovação

**4. Modo Offline (PDV e Recebimento Mobile)**
- PDV funciona sem internet
- Sincronização automática quando conexão retorna
- Controle de conflitos (venda simultânea online/offline)

**5. Automação e Regras de Negócio**
- **Regras Automáticas:**
  - "Se estoque < mínimo → criar sugestão de ordem de compra"
  - "Se produto não vende há X dias → alertar para promoção"
  - "Se pedido ML > R$ X → notificar gerente"
- **Notificações Customizáveis:**
  - Email, SMS, WhatsApp, push notification
  - Por role/perfil

### Phase 3: Módulo Financeiro (Ano 2)

**Contas a Pagar:**
- Cadastro de contas, boletos, fornecedores
- Agendamento de pagamentos
- Conciliação bancária
- Integração com bancos (API Open Banking)

**Contas a Receber:**
- Controle de recebíveis (boleto, cartão, PIX)
- Cobrança automática
- Relatório de inadimplência

**Fluxo de Caixa:**
- Projeção de caixa (entradas vs saídas)
- DRE (Demonstrativo de Resultado)
- Análise de rentabilidade por produto/canal

**Integração com Vendas:**
- Ordem de Venda gera conta a receber automaticamente
- Ordem de Compra gera conta a pagar
- Fechamento de caixa PDV integra com fluxo

### Phase 3: CRM e Gestão Comercial Avançada (Ano 2)

**Pipeline de Vendas:**
- Funil de vendas para B2B
- Oportunidades, propostas, follow-up
- Integração com Ordem de Venda

**Tabelas de Preço Diferenciadas:**
- Por cliente, por categoria de cliente
- Descontos por volume
- Condições comerciais específicas

**Histórico de Relacionamento:**
- Todas as interações com cliente
- Compras passadas, ticket médio, frequência
- Segmentação de clientes (ABC)

**Comissões de Vendedores:**
- Cálculo automático por venda
- Relatórios de performance

### Long-Term Vision (Anos 2-3)

**1. ERP Modular Completo**
- **Core:** Estoque, Vendas, Compras
- **Módulo Financeiro:** Contas a pagar/receber, fluxo de caixa
- **Módulo Produção:** Ordens de produção, controle de matéria-prima, MRP
- **Módulo CRM:** Pipeline, oportunidades, comissões
- **Módulo RH:** Folha de pagamento, ponto (parceria?)
- **Módulo Contábil:** Integração com contabilidade

**Modelo de Negócio:**
- Pricing por módulo
- Upsell natural conforme empresa cresce

**2. E-commerce Próprio Integrado**
- Loja online white-label integrada ao estoque
- Cliente vende em: ML + Shopee + Amazon + Site próprio
- Estoque unificado entre todos os canais

**3. Analytics e Inteligência de Negócio**
- Análise preditiva de demanda (IA/ML)
- Sugestões automáticas de reposição
- Identificação de tendências
- Curva ABC dinâmica
- Análise de margem por produto/categoria/canal
- Export para Power BI, Metabase

**4. Integrações Expandidas**
- Gateways de Pagamento (PagSeguro, Mercado Pago, Stripe)
- ERPs Contábeis (Conta Azul, Omie)
- Logística (Correios, transportadoras)
- Fornecedores (EDI)

**5. Mobile Apps Nativos**
- App Gerencial (iOS/Android)
- App Vendedor Externo (offline-first)
- App Estoquista

**6. Marketplace de Integrações (Ecosystem)**
- App Store do Estoque Central
- Integrações desenvolvidas por terceiros
- API pública documentada

**7. Expansão Internacional**
- LATAM: Argentina, Chile, México, Colômbia
- Adaptar compliance fiscal por país
- Integrações com marketplaces locais
- Multi-moeda, multi-idioma

### Expansion Opportunities

**1. Vertical-Specific Solutions**
- Estoque Central para Restaurantes
- Estoque Central para Farmácias
- Estoque Central para Moda/Varejo

**2. Serviços de Valor Agregado**
- Onboarding Assistido
- Treinamento e Certificação
- Suporte Premium
- Growth Services (consultoria de expansão)

**3. White-Label para ISVs**
- Vender plataforma para revendedores
- Revenue share

### Roadmap Visual

```
MVP (Meses 0-6):
├─ Estoque multi-depósito
├─ PDV + NFCe
├─ Ordem de Venda
├─ Compras + Recebimento Mobile
├─ Integração Mercado Livre
└─ Produtos (simples, variantes, BOM)

Phase 2 (Meses 6-12):
├─ Shopee, Amazon, iFood
├─ NF-e completa
├─ Lote/Validade/Série
├─ Modo Offline
└─ Automações

Phase 3 (Ano 2):
├─ Módulo Financeiro
├─ CRM Avançado
├─ Tabelas de preço
├─ E-commerce próprio
└─ Analytics avançados

Long-Term (Anos 2-3):
├─ ERP Modular Completo
├─ Mobile Apps Nativos
├─ Marketplace de Integrações
├─ Expansão Internacional
└─ Soluções Verticais
```

---

## Technical Considerations

### Platform Requirements

**Target Platforms:**
- **Web Application (Primary):**
  - Desktop/Laptop (Ordem de Venda, Configurações, Relatórios)
  - Tablet (PDV - interface touchscreen)
  - Mobile/Smartphone (Recebimento de Mercadoria)
- **Cross-platform:** Funciona em Windows, macOS, Linux (via browser)

**Browser/OS Support:**
- **Browsers Modernos (últimas 2 versões):**
  - Google Chrome (prioridade - 70%+ dos usuários B2B)
  - Microsoft Edge (Chromium)
  - Firefox
  - Safari (para usuários macOS/iOS)
- **Mobile Browsers:**
  - Chrome Mobile (Android)
  - Safari (iOS)

**Performance Requirements:**

**PDV (Crítico):**
- Carregamento inicial: < 3 segundos
- Busca de produto: < 500ms
- Adicionar item ao carrinho: < 200ms
- Checkout completo (scan → NFCe emitida): < 30 segundos
- Interface 60fps (touchscreen responsivo)

**Recebimento Mobile:**
- Carregamento: < 5 segundos
- Scanner código de barras via câmera: < 2s para reconhecer
- Busca de produto: < 1 segundo

**Aplicação Geral:**
- Listagens/Relatórios: < 2 segundos
- CRUD operations: < 1 segundo para salvar
- Dashboard: < 3 segundos com dados agregados

**Integrações:**
- Sincronização ML: Latência < 5 minutos
- Emissão NFCe: < 10 segundos

**Uptime:**
- SLA: 99.5% (< 3.6h downtime/mês)

---

### Technology Stack (Definida)

#### Frontend: Angular + Angular Material

**Angular (versão 17+):**
- Framework enterprise-grade com estrutura opinada
- TypeScript nativo (type safety)
- Dependency injection built-in
- RxJS para programação reativa
- Router robusto com guards para controle de acesso (Roles)
- CLI poderoso (scaffolding, build, test)

**Angular Material:**
- Componentes prontos seguindo Material Design
- Acessibilidade (a11y) built-in
- Temas customizáveis
- Componentes essenciais: Tables, Forms, Dialogs, Menus, Datepickers
- Responsive layout (Flex Layout ou CSS Grid)

**Bibliotecas Complementares:**
- **RxJS:** Gerenciamento de estado reativo, streams de dados
- **NgRx (opcional):** State management se aplicação crescer (Redux-like para Angular)
- **Angular CDK:** Utilities para comportamentos avançados (Drag & Drop, Virtual Scrolling)
- **ZXing ou ngx-barcode-scanner:** Leitura de código de barras via câmera (mobile)
- **ngx-mask:** Máscaras para inputs (CPF, CNPJ, telefone, CEP)
- **Chart.js ou ngx-charts:** Gráficos para dashboard

**Build e Deploy:**
- Angular CLI para build otimizado (AOT compilation, tree-shaking, lazy loading)
- PWA (Progressive Web App) para melhor experiência mobile
- Service Worker para cache estratégico

---

#### Backend: Java 21/25 + Spring Boot

**Java 21 (LTS recomendado) ou Java 25:**
- Virtual Threads (Project Loom) - melhor performance para I/O bound operations
- Pattern Matching, Records (código mais conciso)
- Performance superior e baixo consumo de memória

**Spring Boot 3.x:**
- Framework enterprise Java mais popular
- Convenção sobre configuração (rapid development)
- Ecosystem maduro com centenas de integrações

**Spring Modules Essenciais:**

**Spring Boot Starter Web:**
- REST APIs (Controllers, DTOs, ResponseEntity)
- Validação com Bean Validation (Jakarta Validation)
- Exception handling global

**Spring Security:**
- Autenticação via Google OAuth 2.0 (Spring Security OAuth2 Client)
- JWT generation e validation
- Method-level security (@PreAuthorize para Roles)
- CORS configuration
- CSRF protection

**Spring Data JPA:**
- ORM com Hibernate
- Repositories (JpaRepository) para CRUD
- Query Methods, Specifications para queries complexas
- Transactions (@Transactional)
- Suporte a hierarquias (categorias) via @ManyToOne self-reference

**Spring Boot Starter Validation:**
- Validações de input (@NotNull, @Size, @Email, @Pattern para CPF/CNPJ)

**Spring Boot Actuator:**
- Healthcheck endpoints
- Metrics (integração com Azure Monitor)
- Info, readiness, liveness probes

**Spring Boot Starter Cache:**
- Abstração de cache (@Cacheable, @CacheEvict)
- Integração com Redis (Azure Cache for Redis)

**Spring WebSocket (futuro):**
- Para sincronização real-time de estoque

**Bibliotecas Adicionais Java:**
- **MapStruct:** Mapeamento DTO ↔ Entity (type-safe, performance)
- **Lombok:** Reduzir boilerplate (@Data, @Builder, @Slf4j)
- **Flyway ou Liquibase:** Database migrations versionadas
- **REST Assured:** Testes de API
- **JUnit 5 + Mockito:** Unit e integration tests
- **SpringDoc OpenAPI (Swagger):** Documentação automática de API
- **Jackson:** JSON serialization/deserialization
- **OkHttp ou RestTemplate:** Client HTTP para integrações (ML API, SEFAZ)

**Integrações Específicas Brasil:**
- **Biblioteca NFe Java:** Para geração de XML NF-e/NFCe
- **Java-Fiscal:** Validação CPF/CNPJ, cálculos fiscais
- **API Mercado Livre (SDK Java ou HTTP client):** Integração ML

---

#### Database: PostgreSQL

**PostgreSQL (versão 15+):**
- ACID compliance (crítico para transações de estoque)
- JSONB para flexibilidade (BOM, variantes, metadados de integrações)
- Full-text search (produtos)
- CTEs (Common Table Expressions) para categorias hierárquicas
- Window functions para relatórios complexos
- Excelente performance transacional e analítica

**Schema Design Highlights:**
- **Multi-tenancy:** Row-level com `tenant_id` em todas as tabelas (RLS - Row Level Security opcional)
- **Audit trail:** Triggers ou JPA @EntityListeners para histórico de mudanças
- **Soft deletes:** Flag `deleted_at` ao invés de DELETE físico
- **Indexes:** Estratégicos em FKs, campos de busca (SKU, nome, código de barras)
- **Constraints:** Foreign keys, unique constraints, check constraints

**Hibernate/JPA Entities:**
- `@Entity` para tabelas
- `@OneToMany`, `@ManyToOne` para relacionamentos
- `@Inheritance` para produtos (Simples, Variante, Composto) - SINGLE_TABLE ou JOINED strategy
- `@JsonbType` (hibernate-types library) para campos JSONB

**Connection Pooling:**
- HikariCP (padrão do Spring Boot) com configuração otimizada

---

#### Hosting/Infrastructure: Microsoft Azure

**Compute:**
- **Azure App Service (recomendado para MVP):**
  - Managed platform (PaaS)
  - Deploy direto via Git, Docker, ou Azure DevOps
  - Auto-scaling horizontal
  - Suporte Java built-in
  - Slots de deployment (staging + production)
  - **Vantagens:** Simplicidade, menos DevOps overhead

- **Azure Kubernetes Service - AKS (futuro):**
  - Para containerização avançada e microservices
  - Escalabilidade granular

- **Azure Container Instances (alternativa):**
  - Para deployments simples de containers

**Database:**
- **Azure Database for PostgreSQL - Flexible Server:**
  - Managed PostgreSQL (backups automáticos, updates, alta disponibilidade)
  - Scaling vertical (compute) e horizontal (read replicas)
  - Suporte a extensões (pg_trgm para full-text search, ltree para hierarquias)
  - Zone redundancy para alta disponibilidade
  - Point-in-time restore

**Cache:**
- **Azure Cache for Redis (Premium ou Basic):**
  - Cache de queries, sessões JWT
  - Message queue (pub/sub) para jobs assíncronos
  - Geo-replication (se necessário)

**Storage:**
- **Azure Blob Storage:**
  - Arquivos CSV de importação
  - XMLs fiscais (armazenamento obrigatório 5 anos)
  - Imagens de produtos (futuro)
  - Hot tier para acesso frequente, Cool tier para XMLs antigos

**CDN:**
- **Azure CDN:**
  - Distribuir assets estáticos do Angular (JS, CSS, images)
  - Melhor latência para usuários em diferentes regiões BR

**Monitoring & Logging:**
- **Azure Monitor + Application Insights:**
  - Telemetria automática (requests, exceptions, dependencies)
  - Dashboards customizados
  - Alertas (uptime, response time, errors)
  - Distributed tracing (para rastrear requests por microservices futuros)

- **Azure Log Analytics:**
  - Logs centralizados
  - Queries KQL (Kusto Query Language)

**Security:**
- **Azure Key Vault:**
  - Armazenar secrets (DB passwords, API keys ML, certificado digital A1)
  - Integração nativa com App Service

- **Azure Active Directory (AAD):**
  - Integração futura para autenticação corporativa
  - Atualmente usar Google OAuth, mas AAD pode ser adicionado depois

**Networking:**
- **Azure Virtual Network (VNet):**
  - Isolar recursos (DB, Redis) em rede privada
  - Public IP apenas para App Service (via Application Gateway ou Front Door)

- **Azure Application Gateway (opcional):**
  - WAF (Web Application Firewall)
  - Load balancing, SSL termination

**DevOps:**
- **GitHub:**
  - **Repositório:** GitHub (monorepo ou multi-repo)
  - **CI/CD:** GitHub Actions (pipelines automatizadas)
    - Build: Compile Angular (ng build), Java (Maven)
    - Test: Run JUnit, Jasmine/Karma, integration tests
    - Docker: Build e push para Azure Container Registry
    - Deploy: Deploy to Azure App Service (staging → production)
  - **Packages:** GitHub Packages para armazenar Docker images (alternativa ao ACR)
  - **Projects:** GitHub Projects para gerenciamento de projeto
  - **Secrets:** GitHub Secrets para armazenar credenciais Azure

- **Workflows:**
  - Pull Request: Rodar testes e lint automaticamente
  - Merge to `develop`: Deploy automático para Staging
  - Merge to `main`: Deploy para Production (com aprovação manual opcional)
  - Release Tags: Criar releases versionadas automaticamente

**Backup & DR:**
- **Automated backups:** Azure Database for PostgreSQL (retenção configurável)
- **Geo-redundancy:** Habilitar replicação para outra região (se SLA crítico)
- **Disaster Recovery Plan:** Backup de Blob Storage em região secundária

**Cost Optimization:**
- **App Service:** Tier Basic para MVP (upgrade para Standard/Premium conforme escala)
- **PostgreSQL:** Tier Burstable (B-series) para início, General Purpose depois
- **Redis:** Basic tier para MVP
- **Reserved Instances:** Economia de 30-50% se commit de 1-3 anos (após PMF validado)

---

### Architecture Considerations

**Repository Structure:**

**Monorepo (recomendado):**
```
estoque-central/
├── frontend/                    # Angular application
│   ├── src/
│   │   ├── app/
│   │   │   ├── core/           # Singleton services (Auth, HTTP interceptors)
│   │   │   ├── shared/         # Shared components, directives, pipes
│   │   │   ├── features/       # Feature modules
│   │   │   │   ├── auth/
│   │   │   │   ├── products/
│   │   │   │   ├── inventory/
│   │   │   │   ├── sales/      # PDV + Ordem de Venda
│   │   │   │   ├── purchases/
│   │   │   │   ├── integrations/
│   │   │   │   └── reports/
│   │   │   └── app-routing.module.ts
│   │   ├── assets/
│   │   └── environments/
│   ├── angular.json
│   └── package.json
│
├── backend/                     # Spring Boot application
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/
│   │   │   │   └── com/estoquecentral/
│   │   │   │       ├── EstoqueCentralApplication.java
│   │   │   │       ├── config/         # Security, Cache, etc.
│   │   │   │       ├── domain/         # Entities/Models
│   │   │   │       │   ├── user/
│   │   │   │       │   ├── product/
│   │   │   │       │   ├── inventory/
│   │   │   │       │   ├── sale/
│   │   │   │       │   └── purchase/
│   │   │   │       ├── repository/     # JPA Repositories
│   │   │   │       ├── service/        # Business logic
│   │   │   │       ├── controller/     # REST Controllers
│   │   │   │       ├── dto/            # Data Transfer Objects
│   │   │   │       ├── mapper/         # MapStruct mappers
│   │   │   │       ├── integration/    # ML, Google OAuth, Fiscal
│   │   │   │       ├── security/       # JWT, OAuth config
│   │   │   │       └── exception/      # Global exception handlers
│   │   │   └── resources/
│   │   │       ├── application.yml
│   │   │       ├── application-dev.yml
│   │   │       ├── application-prod.yml
│   │   │       └── db/migration/       # Flyway migrations
│   │   └── test/
│   ├── pom.xml (Maven) ou build.gradle (Gradle)
│   └── Dockerfile
│
├── docker-compose.yml           # Para desenvolvimento local
├── .azure/                      # Azure DevOps pipelines
│   ├── frontend-pipeline.yml
│   └── backend-pipeline.yml
└── README.md
```

**Alternativa: Multi-repo** (frontend e backend separados) - válido se times forem independentes.

---

**Service Architecture:**

**Modulith com Arquitetura Hexagonal**

**Conceito: Modulith (Modular Monolith)**
- Aplicação Spring Boot única, mas estruturada como módulos independentes
- Cada módulo tem boundaries bem definidos e baixo acoplamento
- Módulos se comunicam via interfaces/eventos (não acessam diretamente uns aos outros)
- Preparado para evoluir para microservices se necessário (módulos podem ser extraídos)
- **Biblioteca:** Spring Modulith para enforçar boundaries e gerar documentação

**Arquitetura Hexagonal (Ports & Adapters)**

**Estrutura por Módulo:**
```
estoque-central/
└── backend/
    └── src/main/java/com/estoquecentral/
        ├── auth/                      # Módulo de Autenticação
        │   ├── domain/               # Camada de Domínio (Core)
        │   │   ├── model/           # Entidades de negócio (User, Profile, Role)
        │   │   ├── port/            # Interfaces (Ports)
        │   │   │   ├── in/          # Use Cases (input ports)
        │   │   │   │   └── AuthenticateUserUseCase.java
        │   │   │   └── out/         # Repository interfaces (output ports)
        │   │   │       └── UserRepository.java
        │   │   └── service/         # Lógica de negócio (implementa use cases)
        │   │       └── AuthService.java
        │   ├── application/          # Camada de Aplicação
        │   │   ├── adapter/         # Adapters
        │   │   │   ├── in/          # Input adapters (REST controllers)
        │   │   │   │   └── web/
        │   │   │   │       └── AuthController.java
        │   │   │   └── out/         # Output adapters (JPA, external APIs)
        │   │   │       └── persistence/
        │   │   │           ├── UserJpaRepository.java
        │   │   │           └── UserRepositoryAdapter.java
        │   │   └── config/          # Configurações do módulo
        │   └── infrastructure/       # Infraestrutura (se necessário)
        │
        ├── products/                  # Módulo de Produtos
        │   ├── domain/
        │   │   ├── model/           # Product, Variant, BOM
        │   │   ├── port/
        │   │   │   ├── in/          # CreateProductUseCase, etc.
        │   │   │   └── out/         # ProductRepository
        │   │   └── service/
        │   └── application/
        │       └── adapter/
        │           ├── in/web/      # ProductController
        │           └── out/persistence/
        │
        ├── inventory/                 # Módulo de Estoque
        │   ├── domain/
        │   │   ├── model/           # Stock, StockLocation, Movement
        │   │   ├── port/
        │   │   └── service/
        │   └── application/
        │
        ├── sales/                     # Módulo de Vendas (PDV + OV)
        │   ├── domain/
        │   │   ├── model/           # Sale, SaleItem, Order
        │   │   ├── port/
        │   │   └── service/
        │   └── application/
        │
        ├── purchases/                 # Módulo de Compras
        │   ├── domain/
        │   └── application/
        │
        ├── integrations/              # Módulo de Integrações
        │   ├── domain/
        │   └── application/
        │       └── adapter/
        │           └── out/
        │               ├── mercadolivre/
        │               ├── google/
        │               └── fiscal/
        │
        └── shared/                    # Código compartilhado
            ├── domain/               # Value Objects, exceções comuns
            ├── events/               # Domain Events para comunicação entre módulos
            └── infrastructure/       # Utilities, configurações globais
```

**Princípios da Arquitetura Hexagonal:**

1. **Domain (Core):**
   - Lógica de negócio pura, sem dependências externas
   - Entities, Value Objects, Domain Services
   - Define Ports (interfaces) para comunicação

2. **Ports (Interfaces):**
   - **Input Ports (Use Cases):** Contratos que definem o que a aplicação faz
   - **Output Ports (Repositories, APIs externas):** Contratos para infraestrutura

3. **Adapters:**
   - **Input Adapters:** REST Controllers, GraphQL, CLI - implementam entrada
   - **Output Adapters:** JPA Repositories, HTTP Clients - implementam saída

4. **Dependency Rule:**
   - Dependências apontam para dentro (Domain não conhece Adapters)
   - Domain → Ports ← Adapters
   - Inversão de dependência via interfaces

**Comunicação Entre Módulos:**
- **Domain Events:** Módulos publicam eventos (Spring ApplicationEventPublisher)
  - Exemplo: `ProductCreatedEvent` publicado por products, consumido por inventory
- **Shared Interfaces:** Módulos expõem Use Cases via interfaces (não implementação)
- **Async quando possível:** Eventos assíncronos para desacoplamento

**Vantagens:**
- ✅ Testabilidade alta (Domain isolado, fácil de mockar)
- ✅ Flexibilidade (trocar adapters sem mudar Domain)
- ✅ Escalabilidade evolutiva (módulos podem virar microservices)
- ✅ Boundaries claros (Spring Modulith valida que módulos não vazam)
- ✅ ACID transactions cross-módulos (monolito)

**Ferramentas:**
- **Spring Modulith:** Validação de módulos, documentação, event publishing
- **ArchUnit:** Testes automatizados para enforçar regras arquiteturais
- **jMolecules:** Anotações para expressar conceitos DDD/Hexagonal

**Comunicação Frontend ↔ Backend:**
- RESTful API (JSON)
- Authentication: JWT no header `Authorization: Bearer <token>`
- CORS configurado para permitir frontend

**Transição Futura para Microservices (Fase 2+):**
- Cada módulo (auth, products, inventory, sales) pode ser extraído como serviço independente
- Domain Events migram para message broker (RabbitMQ, Kafka, Azure Service Bus)
- Módulos já têm boundaries definidos (migração facilitada)

---

### Integration Requirements

**Google OAuth 2.0:**
- **Spring Security OAuth2 Client**
- Fluxo: Frontend redireciona para Google → Google callback → Backend valida → Gera JWT
- Armazenar: Google ID, email, nome, foto
- Validar email contra usuário pré-cadastrado no sistema

**Mercado Livre API:**
- **OAuth 2.0** com refresh token (armazenar em DB criptografado)
- **Webhooks:** ML envia notificações de vendas (endpoint POST no backend)
- **Polling fallback:** Job agendado (Spring @Scheduled) busca pedidos a cada 5 min
- **Rate limiting:** Respeitar limites da API ML
- **HTTP Client:** RestTemplate ou WebClient (Spring reactive)
- **Retry logic:** Spring Retry ou resilience4j para tolerância a falhas

**Impressora Fiscal (NFCe/SAT):**
- **Opções:**
  1. **SDK Java do fabricante** (Elgin, Bematech, Epson)
  2. **Serviço cloud fiscal** (NFeio, PlugNotas, FocusNFe)
     - Vantagem: Sem gerenciar certificado digital, SEFAZ
     - Backend envia dados da venda via API → Serviço emite NFCe → Retorna XML/PDF
  3. **Driver local + Bridge API:** Aplicação local (Java Swing/JavaFX?) que expõe REST API local para impressora
- **Recomendação MVP:** Serviço cloud fiscal (reduz complexidade)

**Barcode Scanner:**
- **Desktop/Tablet:** USB/Bluetooth emula keyboard (sem integração especial)
- **Mobile:** HTML5 Camera API + biblioteca JavaScript (ZXing, QuaggaJS)

---

### Security/Compliance

**Segurança:**
- **HTTPS/TLS:** Todo tráfego criptografado (Azure App Service fornece certificado SSL grátis)
- **Authentication:** Google OAuth 2.0 + JWT
- **Authorization:** Role-based (@PreAuthorize no Spring)
- **SQL Injection:** Prepared statements via JPA (automático)
- **CSRF:** Spring Security CSRF tokens (ou desabilitar se SPA pura com JWT)
- **XSS:** Angular sanitiza HTML por padrão
- **Rate Limiting:** Spring Security rate limiter ou Azure API Management
- **Secrets:** Azure Key Vault (nunca commitar secrets no código)
- **Audit Logs:** Todas operações críticas registradas (venda, transferência, compra)

**Compliance Fiscal:**
- **NFCe/SAT:** XML assinado digitalmente
- **Armazenamento XMLs:** Azure Blob Storage (retenção 5 anos)
- **Certificado Digital:** Azure Key Vault (se A1) ou HSM (se A3)
- **Backup:** Redundante, geo-replicado

**LGPD:**
- Consentimento para coleta de dados pessoais
- Direito ao esquecimento (soft delete + anonymização)
- Criptografia de dados sensíveis em repouso (TDE no PostgreSQL)
- Privacy policy e terms of service

**Multi-tenancy:**
- **Row-level security:** Coluna `tenant_id` em todas as tabelas
- **JPA Filter:** `@Filter` do Hibernate para garantir queries sempre filtrarem por tenant
- **Segurança:** Middleware valida tenant do JWT contra recursos acessados

---

### Build & Deployment

#### Containerização com Docker

**Docker para Aplicação e Banco de Dados**

Toda a stack rodará em containers Docker, facilitando desenvolvimento local e deploy consistente.

**docker-compose.yml (Desenvolvimento Local):**
```yaml
version: '3.8'

services:
  postgres:
    image: postgres:15-alpine
    container_name: estoque-central-db
    environment:
      POSTGRES_DB: estoque_central_dev
      POSTGRES_USER: postgres
      POSTGRES_PASSWORD: postgres
    ports:
      - "5432:5432"
    volumes:
      - postgres_data:/var/lib/postgresql/data
    networks:
      - estoque-network

  redis:
    image: redis:7-alpine
    container_name: estoque-central-redis
    ports:
      - "6379:6379"
    networks:
      - estoque-network

  backend:
    build:
      context: ./backend
      dockerfile: Dockerfile
    container_name: estoque-central-api
    environment:
      SPRING_PROFILES_ACTIVE: dev
      SPRING_DATASOURCE_URL: jdbc:postgresql://postgres:5432/estoque_central_dev
      SPRING_REDIS_HOST: redis
    ports:
      - "8080:8080"
    depends_on:
      - postgres
      - redis
    networks:
      - estoque-network
    volumes:
      - ./backend:/app  # Hot reload em dev

  frontend:
    build:
      context: ./frontend
      dockerfile: Dockerfile.dev
    container_name: estoque-central-frontend
    ports:
      - "4200:4200"
    volumes:
      - ./frontend:/app
      - /app/node_modules  # Não sobrescrever node_modules do container
    networks:
      - estoque-network
    command: npm start

volumes:
  postgres_data:

networks:
  estoque-network:
    driver: bridge
```

**Backend Dockerfile (Multi-stage):**
```dockerfile
# Stage 1: Build
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN mvn clean package -DskipTests

# Stage 2: Runtime
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
```

**Frontend Dockerfile (Production):**
```dockerfile
# Stage 1: Build
FROM node:20-alpine AS build
WORKDIR /app
COPY package*.json ./
RUN npm ci
COPY . .
RUN npm run build -- --configuration=production

# Stage 2: Serve with nginx
FROM nginx:alpine
COPY --from=build /app/dist/estoque-central /usr/share/nginx/html
COPY nginx.conf /etc/nginx/nginx.conf
EXPOSE 80
CMD ["nginx", "-g", "daemon off;"]
```

**Comandos Docker Úteis:**
```bash
# Desenvolvimento local
docker-compose up -d          # Subir todos os serviços
docker-compose logs -f backend  # Ver logs do backend
docker-compose down           # Parar todos os serviços

# Rebuild após mudanças
docker-compose up -d --build

# Rodar migrations
docker-compose exec backend ./mvnw flyway:migrate
```

---

#### CI/CD com GitHub Actions

**Pipelines Automatizadas**

**.github/workflows/backend-ci.yml:**
```yaml
name: Backend CI/CD

on:
  push:
    branches: [main, develop]
    paths:
      - 'backend/**'
  pull_request:
    branches: [main]
    paths:
      - 'backend/**'

jobs:
  test:
    runs-on: ubuntu-latest
    services:
      postgres:
        image: postgres:15-alpine
        env:
          POSTGRES_DB: test_db
          POSTGRES_USER: postgres
          POSTGRES_PASSWORD: postgres
        ports:
          - 5432:5432
        options: >-
          --health-cmd pg_isready
          --health-interval 10s
          --health-timeout 5s
          --health-retries 5

    steps:
      - uses: actions/checkout@v4

      - name: Set up JDK 21
        uses: actions/setup-java@v4
        with:
          java-version: '21'
          distribution: 'temurin'
          cache: maven

      - name: Run tests
        run: |
          cd backend
          ./mvnw clean verify

      - name: Upload coverage
        uses: codecov/codecov-action@v3
        with:
          files: ./backend/target/site/jacoco/jacoco.xml

  build-and-push:
    needs: test
    if: github.ref == 'refs/heads/main'
    runs-on: ubuntu-latest

    steps:
      - uses: actions/checkout@v4

      - name: Login to Azure Container Registry
        uses: docker/login-action@v3
        with:
          registry: ${{ secrets.ACR_LOGIN_SERVER }}
          username: ${{ secrets.ACR_USERNAME }}
          password: ${{ secrets.ACR_PASSWORD }}

      - name: Build and push Docker image
        uses: docker/build-push-action@v5
        with:
          context: ./backend
          push: true
          tags: |
            ${{ secrets.ACR_LOGIN_SERVER }}/estoque-central-api:latest
            ${{ secrets.ACR_LOGIN_SERVER }}/estoque-central-api:${{ github.sha }}
          cache-from: type=registry,ref=${{ secrets.ACR_LOGIN_SERVER }}/estoque-central-api:latest
          cache-to: type=inline

  deploy:
    needs: build-and-push
    runs-on: ubuntu-latest

    steps:
      - name: Deploy to Azure App Service
        uses: azure/webapps-deploy@v2
        with:
          app-name: estoque-central-api
          publish-profile: ${{ secrets.AZURE_WEBAPP_PUBLISH_PROFILE }}
          images: ${{ secrets.ACR_LOGIN_SERVER }}/estoque-central-api:${{ github.sha }}

      - name: Run database migrations
        run: |
          # Executar migrations via Azure CLI ou script remoto
          echo "Running Flyway migrations..."
```

**.github/workflows/frontend-ci.yml:**
```yaml
name: Frontend CI/CD

on:
  push:
    branches: [main, develop]
    paths:
      - 'frontend/**'
  pull_request:
    branches: [main]
    paths:
      - 'frontend/**'

jobs:
  test:
    runs-on: ubuntu-latest

    steps:
      - uses: actions/checkout@v4

      - name: Setup Node.js
        uses: actions/setup-node@v4
        with:
          node-version: '20'
          cache: 'npm'
          cache-dependency-path: frontend/package-lock.json

      - name: Install dependencies
        run: |
          cd frontend
          npm ci

      - name: Lint
        run: |
          cd frontend
          npm run lint

      - name: Test
        run: |
          cd frontend
          npm run test:ci

      - name: Build
        run: |
          cd frontend
          npm run build -- --configuration=production

  build-and-deploy:
    needs: test
    if: github.ref == 'refs/heads/main'
    runs-on: ubuntu-latest

    steps:
      - uses: actions/checkout@v4

      - name: Setup Node.js
        uses: actions/setup-node@v4
        with:
          node-version: '20'
          cache: 'npm'
          cache-dependency-path: frontend/package-lock.json

      - name: Build
        run: |
          cd frontend
          npm ci
          npm run build -- --configuration=production

      - name: Deploy to Azure Static Web App
        uses: Azure/static-web-apps-deploy@v1
        with:
          azure_static_web_apps_api_token: ${{ secrets.AZURE_STATIC_WEB_APPS_API_TOKEN }}
          repo_token: ${{ secrets.GITHUB_TOKEN }}
          action: "upload"
          app_location: "frontend"
          output_location: "dist/estoque-central"
```

**Secrets necessários no GitHub:**
- `ACR_LOGIN_SERVER`: URL do Azure Container Registry
- `ACR_USERNAME` e `ACR_PASSWORD`: Credenciais do ACR
- `AZURE_WEBAPP_PUBLISH_PROFILE`: Profile de publicação do App Service
- `AZURE_STATIC_WEB_APPS_API_TOKEN`: Token para deploy do frontend

---

#### Environments

**Development (Local):**
- Docker Compose com hot reload
- Banco PostgreSQL local (container)
- Redis local (container)
- Frontend: `ng serve` (porta 4200)
- Backend: Spring Boot DevTools (porta 8080)

**Staging (Azure):**
- Azure App Service (deployment slot "staging")
- Azure Database for PostgreSQL (database staging)
- Azure Cache for Redis (staging instance)
- Deploy automático via GitHub Actions (branch `develop`)

**Production (Azure):**
- Azure App Service (slot production)
- Azure Database for PostgreSQL (production, geo-redundante)
- Azure Cache for Redis (production)
- Deploy via GitHub Actions (branch `main`) com aprovação manual
- Blue-Green deployment via slots

---

#### Database Migrations

**Flyway (versionamento de schema):**

**Estrutura:**
```
backend/src/main/resources/db/migration/
├── V1__create_users_table.sql
├── V2__create_products_table.sql
├── V3__create_inventory_table.sql
└── V4__create_sales_table.sql
```

**Execução:**
- **Local:** `docker-compose exec backend ./mvnw flyway:migrate`
- **CI/CD:** GitHub Actions executa migrations após deploy
- **Rollback:** Flyway não suporta rollback automático (criar migration reversa manualmente)

**Alternativa:** Liquibase (suporta rollback, XML/YAML)

---

### Performance Optimization

**Frontend (Angular):**
- **Lazy Loading:** Módulos carregados sob demanda (produtos, vendas, etc.)
- **OnPush Change Detection:** Para componentes que não mudam frequentemente
- **Virtual Scrolling (CDK):** Para listas grandes (milhares de produtos)
- **PWA + Service Worker:** Cache de assets, offline básico
- **AOT Compilation:** Build otimizado (padrão em production)
- **Tree-shaking:** Remover código não utilizado

**Backend (Spring Boot):**
- **Connection Pooling:** HikariCP otimizado (max pool size baseado em load)
- **Caching:** Redis para queries frequentes (@Cacheable)
  - Lista de produtos
  - Estoque por produto (invalidar ao vender/comprar)
- **Database Indexes:** Estratégicos em colunas de busca e FKs
- **N+1 Query Problem:** Usar `@EntityGraph` ou `JOIN FETCH` em queries
- **Async Processing:** Jobs assíncronos (sincronização ML, envio emails) via `@Async` ou Spring Batch
- **Pagination:** Sempre paginar listagens grandes (Spring Data Pageable)

**Database (PostgreSQL):**
- **Indexes:** B-tree em FKs, GIN em JSONB, GiST para full-text search
- **Query optimization:** EXPLAIN ANALYZE para queries lentas
- **Partitioning (futuro):** Particionar tabelas grandes por data (movimentações, vendas)

---

### Monitoring & Observability

**Application Insights (Azure):**
- Telemetria automática (requests, exceptions, dependencies)
- Custom events e metrics (venda realizada, overselling detectado)
- Alertas (error rate > 5%, response time > 2s, uptime < 99%)

**Logging:**
- **SLF4J + Logback** (padrão Spring Boot)
- **Structured logging:** JSON format para facilitar queries
- **Log Analytics:** Centralizar logs no Azure
- **Log Levels:** INFO (produção), DEBUG (desenvolvimento)
- **Correlation ID:** Rastrear request do frontend ao backend ao DB

**Healthchecks:**
- Spring Boot Actuator `/actuator/health`
- Checks: DB connection, Redis connection, disk space
- Liveness e Readiness probes (para Kubernetes futuro)

**Metrics:**
- **Custom metrics:** Produtos vendidos/hora, sincronizações ML/min
- **Dashboards:** Azure Monitor dashboards com KPIs principais

---

### Testing Strategy

**Frontend (Angular):**
- **Unit Tests:** Jasmine + Karma (componentes, services, pipes)
- **E2E Tests:** Protractor (deprecated) → migrar para Cypress ou Playwright
- **Coverage Target:** > 70%

**Backend (Java/Spring):**
- **Unit Tests:** JUnit 5 + Mockito (service layer, mappers)
- **Integration Tests:** Spring Boot Test (@SpringBootTest com TestContainers para PostgreSQL)
- **API Tests:** REST Assured ou MockMvc
- **Coverage Target:** > 80%

**Smoke Tests (Production):**
- Após deploy, rodar testes críticos (login, criar produto, fazer venda)

---

## Constraints & Assumptions

### Constraints (Limitações)

**Budget:**
- **Status:** Não especificado
- **Necessário definir:**
  - Orçamento total para desenvolvimento (6-12 meses)
  - Budget para infraestrutura Azure (mensal)
  - Budget para ferramentas e serviços (Mercado Livre API, serviços fiscais, etc.)
- **Impacto:** Determina tamanho de equipe, prazo realista, tier de serviços Azure

**Timeline:**
- **MVP Target:** 6 meses (estimativa inicial)
- **Premissa:** Equipe dedicada full-time
- **Riscos:** Scope definido é ambicioso para 6 meses (produtos com variantes + BOM + integrações + fiscal)
- **Recomendação:** Validar prazo com equipe técnica após estimar complexidade

**Resources (Equipe):**
- **Não especificado ainda**
- **Estimativa mínima MVP:**
  - 2-3 Desenvolvedores Full-Stack (Angular + Java Spring Boot)
  - 1 DevOps/Infra (Azure, CI/CD)
  - 1 Product Owner/BA (meio período)
  - 1 Designer UI/UX (consultoria/freelance)
  - QA pode ser responsabilidade dos devs no início
- **Premissa:** Equipe com experiência em Angular, Spring Boot e Azure

**Technical Constraints:**
- **Internet obrigatória:** Sistema web requer conexão (modo offline é Fase 2)
- **Browser moderno:** Não suporta IE11 ou browsers antigos
- **Impressora fiscal:** Depende de hardware específico (SAT/NFCe)
- **Certificado digital:** Necessário para emissão fiscal (custo adicional para cliente)

---

### Key Assumptions (Premissas Críticas)

**Sobre o Mercado:**

**A1: Existe demanda suficiente de PMEs com dor de overselling omnichannel**
- **Validar:** Entrevistas com 20+ potenciais clientes antes de iniciar desenvolvimento
- **Risco se falso:** Product-market fit inexistente
- **Mitigação:** Fase de discovery/validação antes de MVP completo

**A2: PMEs pagarão R$ 500-1.500/mês por solução SaaS**
- **Validar:** Pesquisa de willingness-to-pay
- **Risco se falso:** Modelo de negócio inviável
- **Mitigação:** Testar pricing com early adopters

**A3: Integração com Mercado Livre é diferencial competitivo suficiente**
- **Validar:** Analisar concorrentes (Bling, Tiny, etc.) - quais têm ML?
- **Risco se falso:** Comoditização rápida
- **Mitigação:** Focar em UX superior + múltiplos marketplaces rápido

**Sobre Usuários:**

**A4: Usuários têm familiaridade básica com tecnologia**
- **Premissa:** Donos de loja que já vendem no Mercado Livre têm competência digital mínima
- **Risco se falso:** Curva de aprendizado alta → churn
- **Mitigação:** Onboarding guiado, tutoriais em vídeo

**A5: Usuários têm acesso confiável à internet**
- **Premissa:** Operação em cidades médias/grandes com internet banda larga/4G
- **Risco se falso:** PDV não funciona sem internet
- **Mitigação:** Modo offline em Fase 2 se validado como crítico

**A6: Usuários aceitam autenticação exclusiva via Google**
- **Premissa:** Google é ubíquo, mais seguro que email/senha
- **Risco se falso:** Barreira de entrada
- **Mitigação:** Permitir criar conta Google durante onboarding

**Sobre Tecnologia:**

**A7: API do Mercado Livre é estável e documentada**
- **Validar:** POC de integração antes do MVP
- **Risco se falso:** Atrasos significativos
- **Mitigação:** Contingência com polling se webhooks falharem

**A8: Serviços cloud fiscais funcionam conforme esperado**
- **Validar:** POC com serviço fiscal escolhido
- **Risco se falso:** Compliance fiscal comprometido
- **Mitigação:** Plano B (integração direta SEFAZ)

**A9: PostgreSQL com row-level tenant_id é suficiente para multi-tenancy**
- **Premissa:** Até 500-1000 clientes, schema simples funciona
- **Risco se falso:** Performance degrada
- **Mitigação:** Monitoring, plano de migração para schema-per-tenant

**A10: Arquitetura Modulith + Hexagonal facilita evolução**
- **Premissa:** Boundaries claros permitem refactoring e transição para microservices
- **Risco se falso:** Overhead arquitetural desnecessário
- **Mitigação:** Spring Modulith + ArchUnit enforçam disciplina

**Sobre Produto:**

**A11: Produtos com variantes e BOM são necessários no MVP**
- **Decisão:** Mantido porque é comum em marketplaces
- **Risco:** Aumenta significativamente complexidade

**A12: Clientes migrarão dados de planilhas para o sistema**
- **Premissa:** Importação CSV facilita onboarding
- **Risco se falso:** Barreira de entrada, churn alto
- **Mitigação:** Onboarding assistido, ferramentas robustas

**A13: NFCe via serviço cloud é aceitável**
- **Premissa:** Clientes aceitam depender de terceiro para fiscal
- **Risco se falso:** Resistência, preferência por controle total
- **Mitigação:** Oferecer integração direta em tier premium (Fase 2)

**Sobre Negócio:**

**A14: CAC < R$ 2.000 é atingível via marketing digital**
- **Premissa:** Funil de inbound marketing
- **Risco se falso:** Crescimento lento, burn rate alto
- **Mitigação:** Testar canais (Google Ads, Facebook, parcerias)

**A15: Churn < 5%/mês com produto bem executado**
- **Premissa:** Dor é real, solução resolve, clientes ficam
- **Risco se falso:** Leaky bucket
- **Mitigação:** Customer success proativo, NPS tracking

**A16: Expansion revenue via novos marketplaces é significativo**
- **Premissa:** Cliente começa com 1 marketplace, adiciona 2-3
- **Risco se falso:** Upsell limitado
- **Mitigação:** Módulos adicionais (Financeiro, CRM) como upsell

---

### Assumptions Requiring Validation

**Pré-MVP (Discovery Phase):**
1. ✅ Entrevistas com 20+ potenciais clientes
2. ✅ POC Integração Mercado Livre
3. ✅ POC Serviço Fiscal
4. ✅ Análise competitiva profunda
5. ✅ Definir pricing inicial

**Durante MVP:**
6. ⏳ Beta com 5-10 early adopters
7. ⏳ Medir time-to-value real
8. ⏳ Medir NPS após 1 mês de uso

**Pós-MVP:**
9. 📊 CAC/LTV reais
10. 📊 Churn rate
11. 📊 Expansion revenue

---

## Risks & Open Questions

### Key Risks

**Technical Risks:**

**R1: Complexidade de Produtos com Variantes e BOM**
- **Descrição:** Implementar produtos com variantes (matriz cor/tamanho) e BOM (kits) adiciona complexidade significativa ao modelo de dados e UI
- **Impacto:** Alto - pode atrasar MVP em 1-2 meses
- **Probabilidade:** Médio
- **Mitigação:**
  - Considerar remover BOM do MVP (manter apenas variantes)
  - POC de modelagem de dados antes de iniciar
  - Dedicar sprint específico para produtos complexos

**R2: Integração com Mercado Livre API**
- **Descrição:** API do ML pode ter limitações não documentadas, rate limits agressivos, ou instabilidade
- **Impacto:** Crítico - diferencial competitivo principal
- **Probabilidade:** Médio
- **Mitigação:**
  - POC completo de integração antes de iniciar MVP
  - Implementar retry logic e circuit breaker
  - Fallback para polling se webhooks falharem
  - Contingência: Integração manual/batch se API crítica falhar

**R3: Compliance Fiscal Brasileiro**
- **Descrição:** Regulamentações fiscais mudam, diferentes estados têm regras diferentes, integração com SEFAZ pode falhar
- **Impacto:** Crítico - sem fiscal, produto não é usável
- **Probabilidade:** Médio-Alto
- **Mitigação:**
  - Usar serviço cloud fiscal (NFeio, PlugNotas) - delega complexidade
  - Ter consultoria fiscal para validar implementação
  - Testes extensivos com XMLs em homologação SEFAZ
  - Plano B: Partner com fornecedor de solução fiscal

**R4: Performance e Scalability**
- **Descrição:** Sistema pode não escalar com centenas de clientes simultâneos fazendo vendas
- **Impacto:** Alto - downtime perde vendas para clientes
- **Probabilidade:** Baixo (com arquitetura adequada)
- **Mitigação:**
  - Load testing antes de lançar
  - Monitoramento Application Insights
  - Auto-scaling no Azure App Service
  - Cache Redis para queries frequentes
  - Database indexing estratégico

**R5: Multi-tenancy Data Leakage**
- **Descrição:** Bug pode expor dados de um tenant para outro (compliance LGPD crítico)
- **Impacto:** Crítico - perda de confiança, legal issues
- **Probabilidade:** Baixo (com testes adequados)
- **Mitigação:**
  - Hibernate @Filter para garantir tenant_id em todas as queries
  - Testes automatizados para tenant isolation
  - Code review rigoroso em queries
  - Audit logs para detectar acessos indevidos

---

**Product/Market Risks:**

**R6: Overselling não é dor suficiente para pagar SaaS**
- **Descrição:** Clientes podem tolerar overselling e não pagar por solução
- **Impacto:** Crítico - sem PMF, negócio não existe
- **Probabilidade:** Médio
- **Mitigação:**
  - Discovery interviews pré-MVP (20+ clientes potenciais)
  - Beta gratuito com 5-10 clientes para validar value prop
  - Medir métricas de uso (DAU, feature adoption) para ver se resolvem problema

**R7: Competição de incumbents (Bling, Tiny, etc.)**
- **Descrição:** Concorrentes estabelecidos podem lançar features similares rapidamente
- **Impacto:** Alto - reduz diferencial competitivo
- **Probabilidade:** Médio
- **Mitigação:**
  - Focar em UX superior (não apenas features)
  - Lançar rápido (MVP em 6 meses)
  - Roadmap agressivo (Shopee, Amazon em Fase 2)
  - Customer success forte para retenção

**R8: Churn alto devido a complexidade de onboarding**
- **Descrição:** Clientes abandonam durante setup (migração de dados, configuração)
- **Impacto:** Alto - leaky bucket, CAC desperdiçado
- **Probabilidade:** Alto
- **Mitigação:**
  - Onboarding assistido (call de setup incluso)
  - Importação CSV robusta e validada
  - Tutoriais em vídeo passo-a-passo
  - Quick wins (integrar ML primeiro, outras features depois)
  - Medir time-to-first-value e otimizar

---

**Business Risks:**

**R9: CAC > LTV (modelo de negócio insustentável)**
- **Descrição:** Custo para adquirir cliente é maior que valor que gera ao longo da vida
- **Impacto:** Crítico - burn cash sem retorno
- **Probabilidade:** Médio
- **Mitigação:**
  - Começar com canais orgânic

os (SEO, content marketing)
  - Parcerias com contadores/consultores (referral)
  - Aumentar LTV via upsell (novos marketplaces, módulos)
  - Medir CAC desde o início, ajustar canais que não performam

**R10: Budget insuficiente para completar MVP**
- **Descrição:** Scope ambicioso pode requerer mais tempo/recursos que orçado
- **Impacto:** Crítico - MVP incompleto ou time demitido
- **Probabilidade:** Médio
- **Mitigação:**
  - Definir budget claramente upfront
  - Scope phasing: lançar com features essenciais, adicionar restante depois
  - Runway de 12-18 meses (não 6)

**R11: Dificuldade de contratar talento (Angular + Spring Boot + Azure)**
- **Descrição:** Stack específica pode limitar pool de candidatos
- **Impacto:** Médio - atrasa desenvolvimento
- **Probabilidade:** Baixo-Médio
- **Mitigação:**
  - Stack popular facilita contratação
  - Aceitar devs que sabem parte da stack (treinar no resto)
  - Considerar nearshore/remoto para ampliar pool

---

### Open Questions

**Product:**

**Q1: Modelo de pricing exato?**
- Opções:
  - Flat fee mensal (ex: R$ 499/mês)
  - Por usuário (ex: R$ 199/usuário/mês)
  - Por canal (ex: R$ 299 base + R$ 99/marketplace adicional)
  - Freemium (grátis até X pedidos/mês, pago depois)
- **Decisão necessária:** Antes de lançar MVP
- **Stakeholders:** Product, Business

**Q2: Qual serviço de fiscal cloud usar?**
- Opções: NFeio, PlugNotas, FocusNFe, WebmaniaBR
- **Critérios:** Confiabilidade, custo, API documentation, suporte
- **Decisão necessária:** Sprint 1 do MVP
- **Stakeholders:** Tech Lead, Product

**Q3: BOM é realmente necessário no MVP?**
- **Tradeoff:** Complexidade vs valor para clientes de distribuição
- **Alternativa:** Lançar sem BOM, adicionar em Fase 2 se demanda validada
- **Decisão necessária:** Planning do MVP
- **Stakeholders:** Product, Tech Lead

**Q4: Recebimento mobile - app nativo ou web mobile?**
- **Contexto:** Definido como web mobile no MVP
- **Questão:** Performance de camera via browser é suficiente?
- **Validação:** POC de barcode scanner via HTML5
- **Decisão necessária:** Sprint 2-3 do MVP

---

**Technical:**

**Q5: Maven ou Gradle?**
- **Contexto:** Ambos funcionam com Spring Boot
- **Decisão:** Equipe deve escolher baseado em preferência/experiência
- **Deadline:** Antes de setup inicial do projeto

**Q6: Liquibase ou Flyway para migrations?**
- **Flyway:** Mais simples, SQL puro
- **Liquibase:** Mais poderoso, suporta rollback, XML/YAML
- **Decisão necessária:** Sprint 1
- **Stakeholders:** Tech Lead

**Q7: Monorepo ou multi-repo?**
- **Monorepo:** Frontend + Backend no mesmo repositório
- **Multi-repo:** Repositórios separados
- **Recomendação:** Monorepo (facilita compartilhamento de types, deploys atômicos)
- **Decisão necessária:** Antes de criar repositório

**Q8: Como fazer hot reload no Docker durante desenvolvimento?**
- **Backend:** Spring Boot DevTools + volume mount funciona?
- **Frontend:** ng serve via docker-compose ou rodar fora do Docker?
- **Decisão necessária:** Setup de ambiente de desenvolvimento

---

**Business:**

**Q9: Qual o GTM (Go-To-Market) strategy?**
- Opções:
  - Inbound (SEO, content marketing, ads)
  - Outbound (cold email, LinkedIn)
  - Partnerships (contadores, consultores ERP)
  - Marketplace (vender via parceiro que tem base)
- **Decisão necessária:** 2-3 meses antes de lançar MVP
- **Stakeholders:** Founders, Marketing

**Q10: Early adopters - quem são e como encontrar?**
- **Critérios:** PMEs que já vendem em ML + loja física, sentindo dor de overselling
- **Canais:** Grupos Facebook de vendedores ML, fóruns, eventos de e-commerce
- **Decisão necessária:** 3-4 meses antes de lançar MVP
- **Stakeholders:** Product, Marketing

**Q11: Onboarding será self-service ou assistido?**
- **Self-service:** Cliente se cadastra, importa produtos, configura sozinho
- **Assistido:** Call de onboarding, ajuda a configurar, migrar dados
- **Tradeoff:** Escala vs taxa de sucesso
- **Recomendação inicial:** Assistido para primeiros 50 clientes (aprender), depois self-service
- **Decisão necessária:** Antes de lançar MVP

**Q12: Suporte - chat, email, telefone?**
- **MVP:** Email + WhatsApp Business (baixo custo, assíncrono)
- **Futuro:** Chat in-app, base de conhecimento, vídeos
- **Decisão necessária:** Antes de lançar MVP

---

**Legal/Compliance:**

**Q13: Termos de serviço e privacy policy - quem redige?**
- **Necessidade:** Obrigatório (LGPD)
- **Opções:** Advogado especialista, template adaptado
- **Decisão necessária:** 1 mês antes de lançar MVP

**Q14: Certificado digital - como cliente fornece?**
- **A1:** Upload de arquivo .pfx + senha (armazenar no Azure Key Vault criptografado)
- **A3:** Cliente usa token físico (complexo - integração local necessária)
- **MVP:** Suportar apenas A1, A3 em Fase 2
- **Decisão necessária:** Sprint de integração fiscal

**Q15: LGPD - DPO necessário?**
- **Contexto:** Processamos dados pessoais (CPF, endereço de clientes finais)
- **Obrigação:** Empresas devem ter DPO se processamento em larga escala
- **Decisão:** Consultar advogado especialista em LGPD

---

### Risk Mitigation Priorities

**Alto Impacto + Alta Probabilidade (AGIR AGORA):**
1. R8 - Onboarding complexity → Investir em UX, tutoriais, assistência
2. R6 - PMF validation → Discovery interviews pré-MVP

**Alto Impacto + Média Probabilidade (MONITORAR PRÓXIMO):**
3. R3 - Compliance fiscal → POC com serviço cloud, consultoria
4. R2 - Mercado Livre API → POC de integração completo
5. R1 - Complexidade produtos → Considerar reduzir scope

**Médio Impacto (PLANEJAR):**
6. R7 - Competição → UX superior, lançamento rápido
7. R4 - Performance → Load testing, monitoring

---

## Next Steps

### Immediate Actions (Semanas 1-2)

**1. Validar Premissas Críticas (Discovery Phase)**
- [ ] **Entrevistas com clientes potenciais** (20+ PMEs)
  - Roteiro: Dores atuais, willingness-to-pay, features prioritárias
  - Perfil: Varejo omnichannel + Distribuidoras B2B
  - Objetivo: Validar PMF antes de investir em desenvolvimento
  - Responsável: Product Owner/Founder

- [ ] **Análise competitiva detalhada**
  - Competidores: Bling, Tiny, Omie, Aton, Conta Azul
  - Análise: Features, pricing, reviews, gaps
  - Objetivo: Identificar diferencial competitivo sustentável
  - Responsável: Product Owner

- [ ] **Definir pricing inicial**
  - Baseado em: Competição, valor percebido, entrevistas
  - Output: Tabela de preços para MVP
  - Responsável: Product + Business

**2. POCs Técnicas**
- [ ] **POC Mercado Livre API**
  - Validar: OAuth, sincronização de produtos/estoque/pedidos, webhooks, rate limits
  - Duração: 3-5 dias
  - Responsável: Tech Lead
  - Critério de sucesso: Conseguir criar anúncio, atualizar estoque, importar pedido

- [ ] **POC Serviço Fiscal Cloud**
  - Testar: NFeio ou PlugNotas
  - Validar: Emissão NFCe em homologação
  - Duração: 2-3 dias
  - Responsável: Tech Lead/Dev
  - Critério de sucesso: XML gerado corretamente, aceito pela SEFAZ de homologação

- [ ] **POC Barcode Scanner Mobile (HTML5 Camera)**
  - Biblioteca: ZXing ou QuaggaJS
  - Validar: Performance, accuracy em diferentes celulares
  - Duração: 1-2 dias
  - Responsável: Frontend Dev

**3. Decisões Pendentes**
- [ ] **Definir budget total para MVP**
  - Incluir: Salários, infraestrutura Azure, serviços terceiros, buffer
  - Prazo: Esta semana
  - Responsável: Founder/CFO

- [ ] **Montar equipe ou confirmar disponibilidade**
  - Roles: 2-3 Full-Stack Devs, 1 DevOps, 1 PO, 1 UX Designer
  - Prazo: Semanas 1-2
  - Responsável: Founder/RH

- [ ] **Escolher serviço fiscal cloud**
  - Baseado em POC + custo + suporte
  - Prazo: Após POC (semana 2)
  - Responsável: Tech Lead + Product

---

### Setup Phase (Semanas 3-4)

**4. Setup de Infraestrutura**
- [ ] **Criar conta Azure + recursos iniciais**
  - Resource Group, App Service, PostgreSQL, Redis, Key Vault, Container Registry
  - Ambientes: Dev, Staging, Production
  - Responsável: DevOps

- [ ] **Criar repositório GitHub + estrutura de monorepo**
  - Setup: frontend/, backend/, docker-compose.yml, .github/workflows/
  - Branch strategy: main (production), develop (staging), feature branches
  - Responsável: Tech Lead

- [ ] **Setup CI/CD com GitHub Actions**
  - Pipelines: Backend (test, build, push, deploy), Frontend (test, build, deploy)
  - Secrets: Configurar no GitHub
  - Responsável: DevOps

- [ ] **Docker development environment**
  - docker-compose.yml funcional
  - PostgreSQL, Redis, Backend, Frontend rodando
  - Documentar setup em README.md
  - Responsável: DevOps + Tech Lead

**5. Setup de Projeto**
- [ ] **Backend: Criar projeto Spring Boot**
  - Java 21, Maven/Gradle, Spring Boot 3.x
  - Estrutura modular (auth, products, inventory, sales, purchases, integrations)
  - Configurar: PostgreSQL, Redis, Flyway, Security, Actuator
  - Responsável: Tech Lead

- [ ] **Frontend: Criar projeto Angular**
  - Angular 17+, Angular Material
  - Estrutura: core/, shared/, features/
  - Configurar: Routing, HTTP interceptors, auth guards
  - Responsável: Frontend Lead

- [ ] **Configurar ferramentas de desenvolvimento**
  - Linters: ESLint (frontend), Checkstyle/SpotBugs (backend)
  - Formatters: Prettier (frontend), Google Java Format (backend)
  - Pre-commit hooks (lint + format automático)
  - Responsável: Tech Lead

**6. Design e UX**
- [ ] **Wireframes de telas críticas**
  - PDV, Ordem de Venda, Cadastro de Produtos (com variantes), Recebimento Mobile
  - Ferramenta: Figma
  - Responsável: UX Designer

- [ ] **Design System / Styleguide**
  - Cores, tipografia, componentes reutilizáveis
  - Baseado em Angular Material (customizar tema)
  - Responsável: UX Designer

---

### Development Phase (Meses 1-6)

**7. Sprint Planning**
- [ ] **Definir sprints (2 semanas cada)**
  - Sprint 0: Setup, POCs, wireframes
  - Sprint 1-2: Auth + Produtos simples + Categorias
  - Sprint 3-4: Produtos com variantes + Estoque + Movimentações
  - Sprint 5-6: PDV básico (sem fiscal ainda)
  - Sprint 7-8: Ordem de Venda + Clientes/Fornecedores
  - Sprint 9-10: Integração Mercado Livre
  - Sprint 11-12: NFCe + Compras + Recebimento Mobile
  - Sprint 13: BOM (se no MVP) ou polish/bugfixes
  - Sprint 14: Testes integrados, performance, staging
  - Sprint 15: Beta com early adopters, ajustes finais
  - Sprint 16: Launch prep, documentação, onboarding

- [ ] **Criar backlog inicial no GitHub Projects**
  - User stories baseadas no MVP Scope
  - Priorização: MoSCoW (Must, Should, Could, Won't)
  - Responsável: Product Owner + Tech Lead

**8. Development Workflow**
- **Daily standups:** 15min, async ou síncrono
- **Sprint planning:** Início de cada sprint
- **Sprint review:** Fim de sprint (demo para stakeholders)
- **Retrospective:** Melhorias de processo

**9. Quality Assurance**
- **Unit tests:** Obrigatório para toda lógica de negócio (>80% coverage backend, >70% frontend)
- **Integration tests:** Flows críticos (venda, compra, integração ML)
- **E2E tests:** Smoke tests para deploy em production
- **Code review:** Todo PR precisa de aprovação de pelo menos 1 dev

---

### Pre-Launch Phase (Mês 5-6)

**10. Beta Testing**
- [ ] **Recrutar 5-10 early adopters**
  - Perfil: PMEs que vendem em ML + loja física
  - Incentivo: Gratuito por 6 meses + influência no roadmap
  - Responsável: Product + Marketing

- [ ] **Onboarding assistido**
  - Call individual com cada beta tester
  - Ajudar a migrar dados, configurar, integrar ML
  - Coletar feedback detalhado
  - Responsável: Product Owner

- [ ] **Medir métricas de beta**
  - Time-to-value, feature adoption, bugs encontrados, NPS
  - Ajustar produto baseado em feedback
  - Responsável: Product

**11. Preparação para Launch**
- [ ] **Legal/Compliance**
  - Termos de serviço + Privacy policy (LGPD)
  - Consultar advogado se necessário
  - Responsável: Founder

- [ ] **Documentação**
  - Guia de onboarding (passo-a-passo)
  - Vídeos tutoriais (importar produtos, integrar ML, fazer venda)
  - FAQ
  - Responsável: Product + Designer

- [ ] **Marketing / GTM**
  - Website (landing page + docs)
  - SEO básico (blog posts sobre overselling, gestão omnichannel)
  - Preparar launch em grupos/fóruns de vendedores
  - Responsável: Marketing/Founder

- [ ] **Suporte**
  - Setup WhatsApp Business
  - Email de suporte
  - Processo de onboarding assistido documentado
  - Responsável: Product

---

### Post-Launch (Mês 7+)

**12. Monitoring & Iteration**
- [ ] **Dashboards de métricas**
  - MRR, CAC, LTV, Churn, NPS
  - DAU, feature adoption, errors
  - Azure Application Insights configurado

- [ ] **Customer Success**
  - Check-ins regulares com clientes (mensais)
  - Identificar churn risks proativamente
  - Coletar feedback para roadmap

- [ ] **Iterate baseado em dados**
  - Análise de features mais/menos usadas
  - Priorizar Fase 2 baseado em demanda real
  - Resolver bugs críticos imediatamente

**13. Growth**
- [ ] **Escalar aquisição**
  - Testar canais (Google Ads, SEO, parcerias)
  - Otimizar based on CAC/LTV

- [ ] **Product expansion**
  - Lançar integração Shopee (Fase 2)
  - Features mais pedidas pelos clientes

---

### Decision Points

**Go/No-Go Gates:**

**Gate 1 (Pós-Discovery - Semana 2):**
- ✅ **Critérios para PROSSEGUIR:**
  - 15+ PMEs entrevistadas validam dor e disposição a pagar
  - POCs técnicas (ML API, Fiscal) bem-sucedidas
  - Budget e equipe definidos
- ❌ **Se NÃO atender:** Pivotar ou cancelar

**Gate 2 (Pós-Sprint 8 - Mês 3):**
- ✅ **Critérios para PROSSEGUIR:**
  - PDV básico + Produtos funcionando
  - Velocidade de desenvolvimento adequada (não 2x atrasado)
  - Equipe performando bem
- ❌ **Se NÃO atender:** Re-scope ou aumentar equipe

**Gate 3 (Pós-Beta - Mês 5):**
- ✅ **Critérios para LANÇAR:**
  - 3+ beta testers usando diariamente e satisfeitos (NPS > 30)
  - Bugs críticos resolvidos
  - Infraestrutura estável (uptime > 99%)
- ❌ **Se NÃO atender:** Estender beta, resolver issues

---

### Success Criteria Summary

**O projeto será considerado bem-sucedido se:**

✅ **Mês 3:** MVP funcional com PDV + Produtos + Estoque (scope parcial)
✅ **Mês 6:** MVP completo com todas features definidas
✅ **Mês 7:** 5+ clientes pagantes usando em produção
✅ **Mês 9:** 20+ clientes pagantes, churn < 10%, NPS > 40
✅ **Mês 12:** 30+ clientes pagantes, MRR R$ 30k+, PMF validado

---

## PM Handoff

Este Project Brief fornece o contexto completo para o **Estoque Central**.

**Próximo passo:** Iniciar a **fase de discovery** (entrevistas + POCs) para validar premissas críticas antes de começar o desenvolvimento.

Após validação bem-sucedida, a equipe de desenvolvimento deve:
1. Revisar este brief completamente
2. Questionar/desafiar premissas
3. Refinar estimativas técnicas
4. Criar backlog detalhado
5. Iniciar Sprint 0 (setup)

**Recomendação:** Agendar workshop de kick-off (1-2 dias) com toda equipe para alinhar visão, arquitetura e planejamento.

---

**Documento gerado com Claude Code - Business Analyst Agent**
**Data:** 2025-10-20
**Versão:** 1.0
**Status:** Pronto para validação e início de discovery phase

