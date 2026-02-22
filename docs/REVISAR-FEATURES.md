# Features para Revisão — Status Inconsistente

> Gerado em: 2026-02-19
> Objetivo: Listar stories marcadas como `done`/`completed` cujo código real está incompleto, nunca foi wired no frontend, ou foi implementado apenas como stub/mock.

---

## 🔴 Crítico — Implementação Stub/Mock

### 4-3 — Emissão NFCe e Baixa de Estoque
**Status documentado:** `completed`
**Situação real:** Stub com mock, sem integração SEFAZ

**Evidências:**
- `NfceService.java` tem `nfce.enabled: false` por padrão
- URL hardcoded: `http://localhost:9090/nfce` (demo local)
- O pacote `fiscal/` contém apenas `package-info.java` — sem implementação real
- `nfce.api.token: demo-token` — token fictício
- Quando `nfce.enabled=false`, retorna chave NFCe fictícia: `35251112345678901234550010001234561001234567`
- **Nenhuma tela frontend de emissão NFCe existe** — o PDV emite silenciosamente na venda

**O que falta:**
- Contratação e configuração de um provedor fiscal real (Focus NFe / NFe.io / outro)
- Configuração de certificado digital A1/A3
- Tela de gestão/monitoramento de NFCe emitidas
- Testes de homologação com SEFAZ

---

## 🟡 Alerta — Frontend Nunca Wired (Corrigido em 2026-02-19)

As stories abaixo tinham backend + componentes Angular implementados, mas **nenhuma rota estava registrada no `app.routes.ts`** e o menu lateral não as exibia. As rotas foram adicionadas nesta sessão, mas o comportamento real ainda precisa ser validado em produção.

### 3-5 — Transferências entre Estoques
**Status documentado:** `done` (sprint-status) / `completed` (story file: `3-5-stock-adjustment.md`)
**Problema adicional:** O sprint-status.yaml referencia `3-5-transferencias-entre-estoques` mas o arquivo de story é `3-5-stock-adjustment.md` (Ajuste de Estoque) — escopo diferente.

**Componentes Angular existiam mas sem rota:**
- `StockTransferHistoryComponent` → `/estoque/transferencias`
- `StockTransferFormComponent` → `/estoque/transferencias/nova`

**O que foi corrigido:** Rotas adicionadas em `app.routes.ts`, item "Transferências" adicionado ao menu Estoque.
**O que ainda falta validar:** Testar o fluxo completo de criação e listagem de transferências.

---

### 4-4 — Fila de Retry NFCe e Gestão de Falhas
**Status documentado:** `done`

**Componente existia mas sem rota:**
- `PendingSalesComponent` → `/vendas/pendentes`

**O que foi corrigido:** Rota adicionada, item "NFCe Pendentes" adicionado ao menu Vendas.
**O que ainda falta validar:** A tela de retry depende da story 4-3 (emissão real) estar funcionando.

---

### 4-5 — Interface Ordem de Venda B2B
**Status documentado:** `done`

**Componentes existiam mas sem rota:**
- `SalesOrderListComponent` → `/vendas/ordens`
- `SalesOrderFormComponent` → `/vendas/ordens/nova` e `/vendas/ordens/:id/editar`

**O que foi corrigido:** Rotas adicionadas, item "Ordens de Venda" adicionado ao menu Vendas.
**O que ainda falta validar:** Fluxo completo de criação, confirmação e cancelamento de ordens.

---

### 4-6 — Reserva e Liberação de Estoque em Ordens de Venda
**Status documentado:** `done`

**Situação:** Backend de reserva existe (campo `reserved_quantity` em `inventory`, lógica em `SalesOrderService`). O `ExpiringOrdersCardComponent` é um card de dashboard sem rota própria.
**O que falta validar:** Testar se a liberação automática após 7 dias realmente funciona (scheduler/job backend).

---

## 🟡 Alerta — Stories Pendentes mas com Entradas no Sprint-Status

As stories abaixo têm `**Status**: pending` no arquivo de story mas o sprint-status as lista diferente:

| Story | Status no arquivo | Status no sprint-status |
|---|---|---|
| 10-7 backend-rbac-system | `pending` | `backlog` |
| 10-11 backend-permissions-validation | `pending` | `backlog` |
| 10-12 frontend-dashboard-metrics | `pending` | `backlog` |

Essas são legitimamente não implementadas — sem ação necessária além de confirmar que permanecem como backlog.

---

## 🟡 Alerta — Scope Mismatch entre Epic e Story

| Epic descreve | Story file cobre |
|---|---|
| Epic 3, Story 3.5: "Transferências entre Estoques" | `3-5-stock-adjustment.md`: Ajuste de Estoque |

A funcionalidade de transferências está no `StockTransferController`/`StockTransferService` do backend e nos componentes de frontend, mas não há uma story documentada que cubra o fluxo completo de transferências — foi implementada sem story formal.

---

## ✅ Verificados como OK

| Story | Evidência |
|---|---|
| 4-3 backend NfceRetryWorker | `NfceRetryWorker.java` com lógica de retry existe |
| 5-1 a 5-7 Mercado Livre | Backend + frontend completos e wired |
| 7-7 Azure Deploy | `.github/workflows/backend-ci-cd.yml` referencia Azure Container Apps reais |
| 7-8 CI/CD GitHub Actions | `backend-ci-cd.yml` + `frontend-ci-cd.yml` existem e são funcionais |
| Epic 8 (Self-service empresa) | Componentes e backend presentes e wired |
| Epic 9 (Multi-empresa contexto) | Componentes e backend presentes e wired |
| Epic 10 (Colaboradores, exceto 10-7/11/12) | Componentes e backend presentes |

---

## Ações Recomendadas

1. **4-3 NFCe**: Decidir se vai integrar com provedor fiscal real ou manter como funcionalidade futura. Atualizar status para `in-progress` ou criar uma nova story de "Integração SEFAZ real".
2. **3-5 / 4-4 / 4-5 / 4-6**: Fazer smoke test das telas recém-wired para confirmar que o backend responde corretamente.
3. **3-5 scope mismatch**: Criar story formal para "Transferências entre Estoques" ou renomear a 3-5 para refletir o que foi implementado.
4. **sprint-status.yaml**: O arquivo em `docs/sprint-artifacts/sprint-status.yaml` está desatualizado (gerado 2025-12-22). Usar `docs/output/sprint-status.yaml` como fonte de verdade.
