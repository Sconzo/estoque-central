# Story 4.4: NFCe Retry Queue and Failure Management

**Epic**: 4 - Sales Channels - PDV & B2B
**Story ID**: 4.4
**Status**: approved
**Created**: 2025-11-21
**Updated**: 2025-11-21

---

## User Story

Como **gerente de loja**,
Eu quero **fila de retry automática para emissões NFCe falhas com interface de gestão de falhas permanentes**,
Para que **problemas temporais não impeçam compliance fiscal e eu possa resolver manualmente casos críticos (FR22)**.

---

## Context & Business Value

Implementa fila de retry com até 10 tentativas automáticas em intervalo crescente (1min, 2min, 5min, 10min, 30min...). Após limite, marca como "Falha Permanente" e exige intervenção manual via interface de gestão. Impede fechamento de caixa com pendências (FR22).

---

## Acceptance Criteria

### AC1: Fila Redis com Redisson DelayedQueue
- [ ] Configuração Redis (Azure Cache for Redis em prod)
- [ ] Fila implementada com Redisson DelayedQueue
- [ ] Payload: { sale_id, attempt_count, next_retry_at }

### AC2: Worker de Processamento de Retry
- [ ] @Scheduled job roda a cada 1 minuto
- [ ] Consome vendas da fila com nfce_status=PENDING
- [ ] Tenta emitir NFCe novamente (NfceService.emitNfce())
- [ ] Sucesso: atualiza nfce_status=EMITTED, remove da fila
- [ ] Falha: incrementa attempt_count, reenfileira com delay maior
- [ ] Após 10 tentativas: atualiza nfce_status=FAILED, notifica gerente/admin

### AC3: Intervalo Crescente de Retry
- [ ] Tentativas: 1min, 2min, 5min, 10min, 15min, 30min, 1h, 2h, 4h, 8h
- [ ] Cálculo: `delay = Math.min(Math.pow(2, attempt) * 60seconds, 8hours)`

### AC4: Interface de Gestão "Vendas Pendentes NFCe"
- [ ] Component `PendingSalesComponent` (admin/gerente)
- [ ] Lista vendas com nfce_status=PENDING ou FAILED
- [ ] Colunas: Sale Number, Cliente, Data, Valor, Status, Tentativas, Último Erro, Ações
- [ ] Filtros: período, status (PENDING/FAILED)
- [ ] Badge: amarelo (PENDING), vermelho (FAILED)

### AC5: Ações Manuais na Interface
- [ ] **Retry Manual**: força nova tentativa imediata
- [ ] **Cancelar com Estorno**: cancela venda, estorna estoque, marca resolvida
- [ ] **Contingência Offline**: marca como emitida em contingência (DPEC/FS-DA) - manual externo
- [ ] **Marcar como Resolvido**: resolve externamente, atualiza status=EMITTED (exige justificativa)

### AC6: Notificação de Falhas Permanentes
- [ ] Após 10 tentativas, envia notificação para gerente/admin via email (futura: push)
- [ ] Assunto: "URGENTE: Falha permanente NFCe - [sale_number]"
- [ ] Conteúdo: link direto para interface de gestão, erro detalhado

### AC7: Bloqueio de Fechamento de Caixa
- [ ] Endpoint `GET /api/sales/pending-fiscal` retorna vendas PENDING/FAILED
- [ ] Frontend de "Fechamento de Caixa" (futura) consulta endpoint
- [ ] Se houver vendas pendentes: exibe alerta, bloqueia fechamento até resolver

### AC8: Endpoint POST /api/sales/{id}/retry
- [ ] Força retry manual imediato
- [ ] Requer permissão ADMIN ou MANAGER
- [ ] Retorna HTTP 200 se sucesso, 400 se falha (com erro detalhado)

### AC9: Endpoint POST /api/sales/{id}/cancel-with-refund
- [ ] Cancela venda, estorna estoque, atualiza nfce_status=CANCELLED
- [ ] Cria movimentações REVERSAL em stock_movements
- [ ] Requer justificativa obrigatória

---

## Tasks & Subtasks

### Task 1: Configurar Redis e Redisson
- [ ] Adicionar dependência redisson-spring-boot-starter
- [ ] Configurar connection string (Azure Cache for Redis)
- [ ] Bean RedissonClient

### Task 2: Implementar RetryQueueService
- [ ] Método `enqueue(saleId, attemptCount)`
- [ ] Calcula next_retry_at com delay crescente
- [ ] Adiciona à DelayedQueue

### Task 3: Implementar NfceRetryWorker
- [ ] @Scheduled(fixedDelay = 60000) // 1 minuto
- [ ] Consome fila, tenta emitir NFCe
- [ ] Atualiza status conforme resultado

### Task 4: Implementar NotificationService
- [ ] Método `notifyPermanentFailure(sale)`
- [ ] Envia email via SendGrid/Azure Communication Services

### Task 5: Frontend - PendingSalesComponent
- [ ] Lista de vendas pendentes/falhas
- [ ] Filtros e paginação
- [ ] Modal de detalhes com erro completo

### Task 6: Frontend - Ações Manuais
- [ ] Botão Retry Manual
- [ ] Modal Cancelar com Estorno (confirmação + justificativa)
- [ ] Modal Marcar como Resolvido (justificativa)

### Task 7: Implementar SaleController Endpoints Adicionais
- [ ] GET /api/sales/pending-fiscal
- [ ] POST /api/sales/{id}/retry
- [ ] POST /api/sales/{id}/cancel-with-refund

### Task 8: Testes

#### Testing

- [ ] Teste: venda PENDING reenfileirada após falha
- [ ] Teste: após 10 tentativas, status=FAILED
- [ ] Teste: retry manual bem-sucedido atualiza EMITTED
- [ ] Teste: cancelamento estorna estoque corretamente

---

## Definition of Done (DoD)

- [ ] Fila Redis configurada
- [ ] Worker de retry implementado
- [ ] Intervalo crescente funciona corretamente
- [ ] Interface de gestão exibe vendas pendentes
- [ ] Ações manuais (retry, cancelar, resolver) funcionam
- [ ] Notificação de falhas permanentes enviada
- [ ] Bloqueio de fechamento de caixa implementado
- [ ] Testes passando
- [ ] Code review aprovado

---

## Dependencies & Blockers

**Depende de:**
- Story 4.3 (Emissão NFCe) - Enfileira vendas PENDING

**Bloqueia:**
- Nenhuma story, mas é crítico para compliance fiscal (FR22)

---

## Change Log

| Data       | Autor                  | Alteração                                                         |
|------------|------------------------|-------------------------------------------------------------------|
| 2025-11-21 | Claude Code (PM)       | Story drafted                                                     |
| 2025-11-21 | Sarah (PO)             | Adicionadas seções Change Log, Testing, Dev Agent Record, QA Results (template compliance) |

---

## Dev Agent Record

**Agent Model Used:**
Claude 3.5 Sonnet (claude-sonnet-4-5-20250929)

### Debug Log References

### Completion Notes List

### File List

---

## QA Results

**Validation Status**: ✅ Approved
**Validated By**: Sarah (Product Owner)
**Validation Date**: 2025-11-21

**Findings**:
- ✅ Epic alignment verified
- ✅ Acceptance Criteria complete and testable
- ✅ Task breakdown adequate
- ✅ No migration conflicts (uses Redis queue)
- ✅ Template compliance achieved

**Notes**:
- Excellent retry strategy with exponential backoff (1min → 8h max)
- FR22 compliance (bloqueio fechamento caixa com pendências) properly specified
- Redisson DelayedQueue well-chosen for distributed retry
- Interface de gestão allows manual intervention (critical for compliance)
- Email notification for permanent failures prevents silent errors
- Ready for development

**Technical Highlights**:
- 10 retry attempts with increasing intervals: 1min, 2min, 5min, 10min, 15min, 30min, 1h, 2h, 4h, 8h
- Exponential backoff: `delay = Math.min(Math.pow(2, attempt) * 60s, 8h)`
- Azure Cache for Redis (production) for scalability
- Manual actions: Retry, Cancel with Refund, Contingency Offline, Mark as Resolved

**Compliance Highlights**:
- FR22: Prevents cash register closing with pending NFCe
- Audit trail for all manual interventions
- Notification ensures awareness of fiscal issues
- Estorno de estoque on cancellation maintains inventory accuracy

---

**Story criada por**: Claude Code Assistant (Product Manager)
**Data**: 2025-11-21
**Baseado em**: Epic 4, docs/epics/epic-04-sales-channels.md
