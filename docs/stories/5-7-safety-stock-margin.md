# Story 5.7: Configurable Safety Stock Margin

**Epic**: 5 - Marketplace Integration - Mercado Livre
**Story ID**: 5.7
**Status**: drafted
**Created**: 2025-11-21

---

## User Story

Como **gerente de loja**,
Eu quero **margem de segurança configurável para anúncios em marketplaces (ex: anunciar 90% do estoque real)**,
Para que **eu tenha buffer contra overselling considerando latência de sincronização (FR25)**.

---

## Context & Business Value

Implementa margem de segurança configurável aplicada ao sincronizar estoque com ML. Permite configuração por marketplace, categoria ou produto específico. Exemplo: anunciar 90% do estoque real para compensar latência.

---

## Acceptance Criteria

### AC1: Tabela safety_margin_rules Criada
- [ ] `safety_margin_rules`: id, tenant_id, marketplace, product_id (nullable), category_id (nullable), margin_percentage (default: 100), priority (1-3)
- [ ] Priority: 1=produto específico, 2=categoria, 3=marketplace global
- [ ] Constraint: margin_percentage between 0 and 100

### AC2: Lógica de Aplicação de Margem
- [ ] Ao sincronizar estoque (Story 5.4):
  1. Busca regra aplicável (priority 1 → 2 → 3)
  2. Calcula: `quantity_to_publish = quantity_available * (margin_percentage / 100)`
  3. Arredonda para baixo (floor)
  4. Envia para ML
- [ ] Exemplo: estoque=100, margin=90% → publica 90 no ML

### AC3: Endpoint POST /api/safety-margins (Criar Regra)
- [ ] Permite criar regra global (marketplace), por categoria ou por produto
- [ ] Validação: margin_percentage entre 0-100
- [ ] Requer permissão ADMIN ou MANAGER

### AC4: Endpoint GET /api/safety-margins (Listar Regras)
- [ ] Lista todas as regras do tenant
- [ ] Filtros: marketplace, produto, categoria

### AC5: Endpoint PUT /api/safety-margins/{id} (Editar)
- [ ] Atualiza margin_percentage
- [ ] Triggera re-sync de estoque afetado

### AC6: Endpoint DELETE /api/safety-margins/{id} (Deletar)
- [ ] Remove regra
- [ ] Triggera re-sync (volta para 100% ou regra de fallback)

### AC7: Frontend - Configuração de Margens de Segurança
- [ ] Component `SafetyMarginConfigComponent` (admin/gerente)
- [ ] Tabela: Nível (Global/Categoria/Produto), Nome, Margem %, Ações
- [ ] Botão "Nova Regra" abre modal:
  - Radio: Global / Por Categoria / Por Produto
  - Se categoria: dropdown categorias
  - Se produto: autocomplete produto
  - Slider: Margem % (0-100, default: 100)
- [ ] Botões: Editar, Deletar

### AC8: Indicador Visual de Margem
- [ ] Tela de sincronização ML exibe margem aplicada:
  - "Estoque Real: 100 | Anunciado no ML: 90 (margem 90%)"
- [ ] Tooltip explica margem de segurança

---

## Tasks
1. Migration safety_margin_rules
2. SafetyMarginRule entity + repository
3. SafetyMarginService com lógica de seleção de regra
4. Integration com StockSyncWorker (Story 5.4)
5. SafetyMarginController endpoints
6. Frontend config component
7. Testes (incluindo prioridade de regras)

---

**Story criada por**: PM Agent
**Data**: 2025-11-21
