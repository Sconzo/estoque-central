# Story 6.7: Automatic Minimum Stock Alerts

**Epic**: 6 - Reporting & Analytics
**Story ID**: 6.7
**Status**: drafted
**Created**: 2025-11-21

---

## User Story

Como **comprador**,
Eu quero **alertas automáticos de produtos abaixo do estoque mínimo enviados diariamente**,
Para que **eu saiba quais produtos repor sem monitorar manualmente (FR18)**.

---

## Context & Business Value

Job agendado roda diariamente às 08:00 AM identificando produtos com estoque < mínimo configurado. Envia notificação consolidada por email listando produtos em ruptura, facilitando decisão de compra.

---

## Acceptance Criteria

### AC1: @Scheduled Job - Diariamente às 08:00 AM
- [ ] Query produtos com quantity_for_sale < minimum_quantity
- [ ] Agrupa por local de estoque
- [ ] Gera lista: produto, local, estoque atual, mínimo, quantidade a comprar sugerida (mínimo - atual)

### AC2: Endpoint GET /api/stock/below-minimum (FR18)
- [ ] Retorna produtos abaixo do mínimo em tempo real
- [ ] Filtros: local, categoria
- [ ] Usado pelo dashboard (Story 6.1) e pela notificação

### AC3: Notificação por Email
- [ ] Destinatários: usuários com role COMPRADOR ou GERENTE
- [ ] Assunto: "Alerta de Estoque - [N] produtos abaixo do mínimo"
- [ ] Corpo HTML:
  - Resumo: "X produtos precisam de reposição"
  - Tabela: Produto, Local, Estoque Atual, Mínimo, Sugestão de Compra
  - Link para sistema: "Gerenciar Estoque"
- [ ] Envia apenas se houver produtos abaixo do mínimo (não envia email vazio)

### AC4: Configuração de Estoque Mínimo por Produto/Local
- [ ] Já existe em Story 2.7 (campo minimum_quantity em stock)
- [ ] Frontend permite editar (inline ou modal)

### AC5: Histórico de Alertas (Opcional)
- [ ] Tabela `stock_alerts`: id, tenant_id, alert_date, products_count, sent_to_users, data_criacao
- [ ] Registra cada envio de alerta para auditoria

### AC6: Frontend - Alertas de Ruptura Card (Dashboard)
- [ ] Card no dashboard (Story 6.1) exibe contador de produtos abaixo do mínimo
- [ ] Link "Ver Detalhes" abre lista completa
- [ ] Badge vermelho se > 0

### AC7: Frontend - Lista de Produtos em Ruptura
- [ ] Component `BelowMinimumStockComponent`
- [ ] Tabela: Produto, Local, Estoque Atual, Mínimo, Déficit, Ações
- [ ] Ação: "Criar Ordem de Compra" (pre-preenche OC com produto e quantidade sugerida)
- [ ] Filtros: local, categoria
- [ ] Exportação CSV

---

## Tasks
1. StockAlertScheduledJob (@Scheduled cron daily 08:00)
2. EmailService.sendStockAlert(products)
3. Email template HTML
4. Endpoint GET /api/stock/below-minimum
5. stock_alerts table migration (opcional)
6. Frontend BelowMinimumStockComponent
7. Integration com PurchaseOrder create (pre-fill)
8. Testes

---

## Technical Notes

**Scheduled Job:**
```java
@Component
public class StockAlertScheduledJob {
    @Scheduled(cron = "0 0 8 * * ?") // 08:00 AM diariamente
    public void checkAndNotifyBelowMinimum() {
        List<UUID> tenants = tenantRepository.findAllIds();

        for (UUID tenantId : tenants) {
            TenantContext.setCurrentTenant(tenantId);

            List<StockBelowMinimumDTO> products = stockRepository
                .findBelowMinimum(tenantId);

            if (!products.isEmpty()) {
                List<User> buyers = userRepository
                    .findByRoles(List.of("COMPRADOR", "GERENTE"));

                emailService.sendStockAlert(buyers, products);

                // Registrar alerta enviado
                stockAlertRepository.save(new StockAlert(
                    tenantId, LocalDate.now(), products.size(),
                    buyers.stream().map(User::getEmail).collect(Collectors.toList())
                ));
            }
        }
    }
}
```

---

**Story criada por**: PM Agent
**Data**: 2025-11-21
