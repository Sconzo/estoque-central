# Story 5.4: Weighted Average Cost Calculation - COMPLETED ✅

## 🎯 Objetivo

Implementar cálculo automático de custo médio ponderado (CMV) com atualização em tempo real a cada movimentação de estoque, histórico de mudanças de custo e análise de margem.

**Epic:** 5 - Purchasing & Inventory Replenishment
**Status:** ✅ 100% Completo

---

## ✅ Acceptance Criteria

- [x] **AC1**: Tabelas `product_costs`, `cost_history` criadas
- [x] **AC2**: Cálculo automático de custo médio ponderado
- [x] **AC3**: Trigger automático em inventory_movements
- [x] **AC4**: Histórico completo de mudanças de custo
- [x] **AC5**: Custo por produto + localização
- [x] **AC6**: Cálculo de margem automático
- [x] **AC7**: Tracking de compras totais
- [x] **AC8**: Identificação de produtos com margem baixa
- [x] **AC9**: Functions SQL para cálculos
- [x] **AC10**: Views para análise

---

## 📁 Arquivos Implementados

### 1. Migration V018__create_cost_tracking_tables.sql

**2 tabelas criadas:**
- `product_costs` - Custo atual por produto/localização
- `cost_history` - Histórico de mudanças de custo

**2 functions SQL:**
- `calculate_weighted_average_cost()` - Fórmula do custo médio ponderado
- `update_product_cost_after_movement()` - Trigger automático

**3 views:**
- `v_product_costs` - Custos com margem
- `v_cost_changes` - Histórico de mudanças
- `v_low_margin_products` - Produtos com margem < 20%

### 2. Domain Entities (1 arquivo Java)

- `ProductCost.java` - Custo do produto com métodos de cálculo de margem

---

## 💰 Fórmula do Custo Médio Ponderado

### Entrada de Estoque (PURCHASE, ADJUSTMENT_IN, TRANSFER_IN)

```
Valor Atual = Quantidade Atual × Custo Médio Atual
Valor Entrada = Quantidade Entrada × Custo Unitário Entrada

Nova Quantidade = Quantidade Atual + Quantidade Entrada
Novo Valor Total = Valor Atual + Valor Entrada

Novo Custo Médio = Novo Valor Total ÷ Nova Quantidade
```

### Saída de Estoque (SALE, ADJUSTMENT_OUT, TRANSFER_OUT)

```
Custo Médio = Mantém o mesmo (não recalcula)
Nova Quantidade = Quantidade Atual - Quantidade Saída
Novo Valor Total = Nova Quantidade × Custo Médio Atual
```

---

## 📊 Exemplo Prático

### Estado Inicial
```
Produto: Notebook Dell
Localização: Armazém Principal
Quantidade: 0
Custo Médio: R$ 0,00
Valor Total: R$ 0,00
```

### 1ª Compra: 10 unidades a R$ 3.800,00
```
Quantidade Atual: 0
Custo Atual: R$ 0,00
Valor Atual: R$ 0,00

Entrada: 10 × R$ 3.800,00 = R$ 38.000,00

Nova Quantidade: 0 + 10 = 10
Novo Valor: R$ 0,00 + R$ 38.000,00 = R$ 38.000,00
Novo Custo Médio: R$ 38.000,00 ÷ 10 = R$ 3.800,00
```

### 2ª Compra: 5 unidades a R$ 4.200,00
```
Quantidade Atual: 10
Custo Atual: R$ 3.800,00
Valor Atual: R$ 38.000,00

Entrada: 5 × R$ 4.200,00 = R$ 21.000,00

Nova Quantidade: 10 + 5 = 15
Novo Valor: R$ 38.000,00 + R$ 21.000,00 = R$ 59.000,00
Novo Custo Médio: R$ 59.000,00 ÷ 15 = R$ 3.933,33
```

### Venda: 8 unidades
```
Quantidade Atual: 15
Custo Atual: R$ 3.933,33
Valor Atual: R$ 59.000,00

Saída: 8 unidades (usa custo médio atual)
Custo da Venda: 8 × R$ 3.933,33 = R$ 31.466,64

Nova Quantidade: 15 - 8 = 7
Novo Valor: 7 × R$ 3.933,33 = R$ 27.533,31
Custo Médio: R$ 3.933,33 (mantém o mesmo)
```

### 3ª Compra: 10 unidades a R$ 3.500,00
```
Quantidade Atual: 7
Custo Atual: R$ 3.933,33
Valor Atual: R$ 27.533,31

Entrada: 10 × R$ 3.500,00 = R$ 35.000,00

Nova Quantidade: 7 + 10 = 17
Novo Valor: R$ 27.533,31 + R$ 35.000,00 = R$ 62.533,31
Novo Custo Médio: R$ 62.533,31 ÷ 17 = R$ 3.678,43
```

---

## ⚙️ Funcionamento Automático

### Trigger Automático

```sql
CREATE TRIGGER trigger_update_cost_after_movement
    AFTER INSERT ON inventory_movements
    FOR EACH ROW
    EXECUTE FUNCTION update_product_cost_after_movement();
```

**O que acontece:**
1. Qualquer INSERT em `inventory_movements` dispara o trigger
2. Função lê dados atuais de `product_costs`
3. Calcula novo custo médio ponderado
4. Atualiza ou insere em `product_costs`
5. Se mudança > 0.01%, registra em `cost_history`

### Movimentos que Afetam o Custo

**Recalculam o custo médio:**
- `PURCHASE` - Compra
- `ADJUSTMENT_IN` - Ajuste entrada
- `TRANSFER_IN` - Transferência entrada
- `RETURN_FROM_CUSTOMER` - Devolução de cliente

**Mantêm o custo médio:**
- `SALE` - Venda
- `ADJUSTMENT_OUT` - Ajuste saída
- `TRANSFER_OUT` - Transferência saída
- `RETURN_TO_SUPPLIER` - Devolução ao fornecedor

---

## 📈 Cálculo de Margem

### Margem Percentual
```
Margem % = ((Preço Venda - Custo Médio) / Preço Venda) × 100
```

### Exemplo:
```
Produto: Notebook Dell
Custo Médio: R$ 3.678,43
Preço Venda: R$ 4.500,00

Margem = ((4.500 - 3.678,43) / 4.500) × 100
Margem = (821,57 / 4.500) × 100
Margem = 18,26%
```

---

## 🔍 Consultas e Relatórios

### 1. Custos Atuais de Todos os Produtos

```sql
SELECT * FROM v_product_costs
ORDER BY product_name;
```

**Output:**
```
product_sku | product_name          | location  | avg_cost | quantity | total_value | margin_pct
NOTE-001    | Notebook Dell         | MAIN      | 3678.43  | 17       | 62533.31    | 18.26%
MOUSE-001   | Mouse Logitech        | MAIN      | 145.50   | 50       | 7275.00     | 35.67%
```

### 2. Histórico de Mudanças de Custo

```sql
SELECT * FROM v_cost_changes
WHERE product_sku = 'NOTE-001'
ORDER BY changed_at DESC
LIMIT 10;
```

**Output:**
```
product_sku | old_cost | new_cost | change_pct | movement_type | changed_at
NOTE-001    | 3933.33  | 3678.43  | -6.48%     | PURCHASE      | 2025-11-06 15:00
NOTE-001    | 3800.00  | 3933.33  | +3.51%     | PURCHASE      | 2025-11-06 10:00
NOTE-001    | 0.00     | 3800.00  | NULL       | PURCHASE      | 2025-11-05 14:00
```

### 3. Produtos com Margem Baixa (< 20%)

```sql
SELECT * FROM v_low_margin_products
ORDER BY margin_percentage ASC;
```

**Output:**
```
sku       | name              | location | avg_cost | price  | margin_pct | quantity
NOTE-001  | Notebook Dell     | MAIN     | 3678.43  | 4500   | 18.26%     | 17
TABLET-01 | Tablet Samsung    | MAIN     | 1820.00  | 2100   | 13.33%     | 25
```

### 4. Custo vs Última Compra

```sql
SELECT
    product_sku,
    product_name,
    average_cost,
    last_cost,
    (last_cost - average_cost) AS cost_difference,
    ROUND(((last_cost - average_cost) / average_cost * 100), 2) AS diff_percentage
FROM v_product_costs
WHERE last_cost IS NOT NULL
  AND average_cost > 0
ORDER BY diff_percentage DESC;
```

---

## 📊 Estatísticas

- **Arquivos criados:** 2
- **Linhas de código:** ~500+
- **Tabelas:** 2
- **Views:** 3
- **Functions:** 2
- **Triggers:** 1
- **Domain entities:** 1

---

## ✨ Destaques Técnicos

1. **Automático**: Trigger em inventory_movements
2. **Preciso**: 4 casas decimais (NUMERIC(15,4))
3. **Histórico Completo**: Todas mudanças > 0.01% registradas
4. **Por Localização**: Custo independente por armazém
5. **Margem Integrada**: Cálculo automático de margem
6. **Performance**: Indexes otimizados
7. **Estatísticas**: Total de compras e valores
8. **Alertas**: View de produtos com margem baixa

---

## 🎯 Casos de Uso

### 1. Análise de Rentabilidade

```sql
-- Produtos com melhor margem
SELECT
    product_name,
    selling_price,
    average_cost,
    margin_percentage,
    current_quantity,
    (current_quantity * (selling_price - average_cost)) AS total_profit_potential
FROM v_product_costs
WHERE margin_percentage > 30
ORDER BY total_profit_potential DESC;
```

### 2. Impacto de Aumento de Preço do Fornecedor

```sql
-- Simular impacto de aumento de 10%
SELECT
    product_name,
    average_cost AS current_cost,
    average_cost * 1.10 AS new_cost_if_10pct_increase,
    selling_price,
    margin_percentage AS current_margin,
    ROUND(((selling_price - (average_cost * 1.10)) / selling_price * 100), 2) AS new_margin_if_10pct_increase
FROM v_product_costs
WHERE current_quantity > 0;
```

### 3. Auditoria de Custos

```sql
-- Produtos com variação de custo > 5% no último mês
SELECT
    product_sku,
    product_name,
    old_average_cost,
    new_average_cost,
    cost_change_percentage,
    changed_at
FROM v_cost_changes
WHERE changed_at >= CURRENT_DATE - INTERVAL '30 days'
  AND ABS(cost_change_percentage) > 5
ORDER BY ABS(cost_change_percentage) DESC;
```

---

## 🎉 Conclusão

**Story 5.4 - Weighted Average Cost Calculation está 100% completa!**

✅ 2 tabelas criadas
✅ Cálculo automático via trigger
✅ Fórmula de custo médio ponderado
✅ Histórico completo de mudanças
✅ Custo por localização
✅ Cálculo de margem integrado
✅ Identificação de margem baixa
✅ 2 functions SQL
✅ 3 views de análise

**Epic 5 - Purchasing & Replenishment: 80% completo!** 🚀

---

**Próximo:** Story 5.5 - Stock Transfers (Transferências entre Armazéns)

**Implementado por:** Claude Code (Anthropic)
**Data:** 2025-11-06
