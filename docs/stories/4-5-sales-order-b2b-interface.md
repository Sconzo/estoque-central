# Story 4.5: Sales Order B2B Interface (Ordem de Venda)

**Epic**: 4 - Sales Channels - PDV & B2B
**Story ID**: 4.5
**Status**: approved
**Created**: 2025-11-21
**Updated**: 2025-11-21

---

## User Story

Como **vendedor B2B**,
Eu quero **interface desktop de Ordem de Venda com consulta de estoque em tempo real e reserva automática**,
Para que **eu possa formalizar pedidos de clientes empresariais com garantia de disponibilidade (FR10)**.

---

## Context & Business Value

Interface desktop para operações B2B com múltiplos itens, consulta de estoque por local em tempo real, reserva automática ao confirmar e emissão posterior de NFe (fora do MVP). Diferente do PDV (touchscreen, NFCe imediata), Ordem de Venda reserva estoque para faturamento futuro.

---

## Acceptance Criteria

### AC1: Tabela sales_orders Criada
- [ ] Migration cria `sales_orders`:
  - id, tenant_id, order_number (SO-YYYYMM-0001), customer_id, stock_location_id
  - status (DRAFT, CONFIRMED, INVOICED, CANCELLED), order_date, delivery_date_expected
  - payment_terms (À VISTA, 7 DIAS, 14 DIAS, 30 DIAS, 60 DIAS, 90 DIAS)
  - total_amount, notes, created_by_user_id, data_criacao
- [ ] Tabela `sales_order_items`:
  - id, sales_order_id, product_id, variant_id, quantity_ordered, quantity_reserved, unit_price, total_price
- [ ] Índices: idx_so_tenant_id, idx_so_order_number, idx_so_customer_id, idx_so_status

### AC2: Endpoint POST /api/sales-orders (Criar Ordem)
- [ ] Status inicial: DRAFT
- [ ] order_number gerado automaticamente
- [ ] total_amount calculado automaticamente
- [ ] Validação: ao menos 1 item
- [ ] Retorna HTTP 201 com ordem criada

### AC3: Endpoint PUT /api/sales-orders/{id}/confirm (Confirmar Ordem)
- [ ] Transição: DRAFT → CONFIRMED
- [ ] Reserva automática de estoque (Story 4.6)
- [ ] Validação: estoque disponível >= quantidade
- [ ] Se estoque insuficiente: retorna HTTP 409 com detalhes
- [ ] Atualiza `quantity_reserved` nos itens

### AC4: Endpoints CRUD e Consulta
- [ ] GET /api/sales-orders (lista paginada com filtros: customer, status, período)
- [ ] GET /api/sales-orders/{id} (detalhes completos)
- [ ] PUT /api/sales-orders/{id} (editar, somente DRAFT)
- [ ] DELETE /api/sales-orders/{id} (cancelar, libera reservas se CONFIRMED)

### AC5: Endpoint GET /api/stock/availability (Consulta Tempo Real)
- [ ] Recebe: product_id, stock_location_id
- [ ] Retorna: quantity_available, quantity_reserved, quantity_for_sale
- [ ] Performance: < 500ms (NFR3)
- [ ] Usado pela interface ao adicionar itens

### AC6: Frontend - Lista de Ordens de Venda
- [ ] Component `SalesOrderListComponent` (desktop)
- [ ] Tabela: Order Number, Cliente, Data, Previsão Entrega, Status, Valor Total, Ações
- [ ] Filtros: cliente (autocomplete), status (dropdown), período (datepicker range)
- [ ] Badges visuais: DRAFT (cinza), CONFIRMED (azul), INVOICED (verde), CANCELLED (vermelho)
- [ ] Botão "Nova Ordem de Venda"
- [ ] Ações: Ver, Editar (DRAFT), Confirmar (DRAFT), Cancelar

### AC7: Frontend - Formulário de Ordem de Venda
- [ ] Component `SalesOrderFormComponent` com 2 seções:
  1. **Dados Gerais**: Cliente* (autocomplete), Local de Estoque*, Data do Pedido*, Previsão de Entrega, Prazo de Pagamento*, Observações
  2. **Itens**: Tabela editável inline
- [ ] Adicionar item:
  - Produto* (autocomplete)
  - Exibe estoque disponível em tempo real no local selecionado
  - Quantidade* (validação: <= estoque disponível para venda)
  - Preço Unitário* (default: preço cadastro do produto, editável)
  - Total (calculado automaticamente)
- [ ] Linha de total exibe valor total da ordem
- [ ] Botões: Salvar como Rascunho, Confirmar Pedido (reserva estoque), Cancelar

### AC8: Consulta de Estoque em Tempo Real
- [ ] Ao selecionar produto na grid de itens:
  - Busca automática `GET /api/stock/availability?product_id=X&location_id=Y`
  - Exibe badge: "Disponível: X unidades" (verde se > 10, amarelo se 1-10, vermelho se 0)
- [ ] Ao editar quantidade: valida se <= disponível
- [ ] Feedback visual imediato (não bloqueia, mas alerta)

### AC9: Histórico do Cliente em Sidebar
- [ ] Sidebar contextual exibe ao selecionar cliente:
  - Últimas 5 ordens de venda
  - Total comprado nos últimos 12 meses
  - Ordens pendentes (CONFIRMED não faturadas)
- [ ] Link "Ver histórico completo"

---

## Tasks & Subtasks

### Task 1: Criar Migrations de sales_orders e items
- [ ] V051__create_sales_orders_table.sql
- [ ] V052__create_sales_order_items_table.sql

### Task 2: Criar Entidades e Repositories
- [ ] SalesOrder.java, SalesOrderItem.java
- [ ] Enum SalesOrderStatus, PaymentTerms
- [ ] SalesOrderRepository, SalesOrderItemRepository

### Task 3: Implementar SalesOrderNumberGenerator
- [ ] Formato: SO-YYYYMM-9999

### Task 4: Implementar SalesOrderService
- [ ] Métodos: create(), update(), confirm(), cancel()
- [ ] Confirmar ordem: valida estoque, reserva (Story 4.6)
- [ ] Cancelar ordem: libera reservas

### Task 5: Implementar StockAvailabilityService
- [ ] Método `getAvailability(productId, locationId)`
- [ ] Retorna: available, reserved, for_sale

### Task 6: Criar SalesOrderController
- [ ] Endpoints CRUD + confirm + stock/availability
- [ ] DTOs: SalesOrderRequestDTO, SalesOrderResponseDTO

### Task 7: Frontend - SalesOrderListComponent
- [ ] Lista com filtros e paginação
- [ ] Badges de status

### Task 8: Frontend - SalesOrderFormComponent
- [ ] Formulário reativo multi-seção
- [ ] Autocomplete cliente e produto
- [ ] Consulta de estoque em tempo real
- [ ] Validações inline

### Task 9: Frontend - CustomerHistorySidebarComponent
- [ ] Sidebar com histórico do cliente
- [ ] Estatísticas e últimas ordens

### Task 10: Testes

#### Testing

- [ ] Teste: criação de ordem DRAFT
- [ ] Teste: confirmação reserva estoque
- [ ] Teste: confirmação com estoque insuficiente retorna 409
- [ ] Teste: cancelamento libera reservas
- [ ] Teste: consulta availability retorna valores corretos

---

## Definition of Done (DoD)

- [ ] Migrations executadas
- [ ] Entidades SalesOrder e SalesOrderItem criadas
- [ ] SalesOrderService implementado
- [ ] SalesOrderController com endpoints
- [ ] Frontend lista ordens com filtros
- [ ] Frontend permite criar/editar/confirmar ordem
- [ ] Consulta de estoque em tempo real funciona
- [ ] Reserva automática ao confirmar (Story 4.6)
- [ ] Sidebar de histórico do cliente
- [ ] Testes passando
- [ ] Code review aprovado

---

## Dependencies & Blockers

**Depende de:**
- Story 4.1 (Customers) - Ordem precisa de cliente
- Story 2.7 (Stock Control) - Consulta estoque
- Story 2.2 (Products) - Lista produtos

**Bloqueia:**
- Story 4.6 (Reserva de Estoque) - Ordem CONFIRMED reserva estoque

---

## Change Log

| Data       | Autor                  | Alteração                                                         |
|------------|------------------------|-------------------------------------------------------------------|
| 2025-11-21 | Claude Code (PM)       | Story drafted                                                     |
| 2025-11-21 | Sarah (PO)             | Migration versions corrigidas de V050-V051 para V051-V052 (validação épico) |
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
- ✅ Migration versions corrected to V051-V052
- ✅ Template compliance achieved

**Notes**:
- Excellent B2B workflow (DRAFT → CONFIRMED with stock reservation)
- Real-time stock availability check (< 500ms NFR3) properly integrated
- FR10 compliance (reserva automática) correctly specified
- Customer history sidebar adds business value
- Order number generation (SO-YYYYMM-9999) consistent with other entities
- Ready for development

**Technical Highlights**:
- Status workflow: DRAFT → CONFIRMED → INVOICED → CANCELLED
- Stock reservation on confirm (Story 4.6 integration)
- Real-time availability: `GET /api/stock/availability?product_id=X&location_id=Y`
- Payment terms support (À VISTA, 7/14/30/60/90 DIAS)
- Multi-item order with inline editing

**UX Highlights**:
- Desktop-optimized (vs PDV touchscreen)
- Inline stock availability badges (verde > 10, amarelo 1-10, vermelho 0)
- Customer history in sidebar (últimas 5 ordens, total 12 meses)
- Autocomplete for customer and product selection

---

**Story criada por**: Claude Code Assistant (Product Manager)
**Data**: 2025-11-21
**Baseado em**: Epic 4, docs/epics/epic-04-sales-channels.md
