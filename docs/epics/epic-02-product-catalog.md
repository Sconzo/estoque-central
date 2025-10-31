# Epic 2: Product Catalog & Inventory Foundation

**Objetivo:** Implementar a gestão completa de catálogo de produtos suportando três tipos (Simples, Variantes, Compostos/BOM) organizados em categorias hierárquicas, além de gerenciamento multi-depósito com rastreamento de quantidade disponível, quantidade reservada e histórico auditável de todas as movimentações. Este épico entrega a base de dados mestre de produtos e estoque que todos os fluxos de vendas e compras dependem.

---

## Story 2.1: Hierarchical Product Categories

Como **gerente de loja**,
Eu quero **categorias hierárquicas ilimitadas (árvore de categorias)**,
Para que **eu possa organizar produtos em estrutura lógica (ex: Eletrônicos > Informática > Notebooks)**.

**Critérios de Aceitação:**
1. Tabela `categories` criada com colunas: id, name, description, parent_category_id (nullable, self-reference), status
2. Endpoint `POST /api/categories` cria categoria com parent opcional
3. Endpoint `GET /api/categories/tree` retorna árvore completa em formato JSON hierárquico
4. Endpoint `PUT /api/categories/{id}` edita categoria (não permite criar ciclos na árvore)
5. Endpoint `DELETE /api/categories/{id}` marca categoria como inativa (soft delete) se não tiver produtos associados
6. UI Angular exibe árvore de categorias com expand/collapse
7. Breadcrumb exibe caminho completo (ex: "Eletrônicos / Informática / Notebooks") ao cadastrar produto

---

## Story 2.2: Simple Products CRUD

Como **gerente de loja**,
Eu quero **cadastrar produtos simples (SKU único sem variações)**,
Para que **eu possa gerenciar catálogo básico de produtos**.

**Critérios de Aceitação:**
1. Tabela `products` criada: id, tenant_id, type (SIMPLE), name, sku, barcode, description, category_id, price, cost, unit, controls_inventory (boolean), status
2. Endpoint `POST /api/products` cria produto simples com validações (SKU único por tenant)
3. Endpoint `GET /api/products` retorna lista paginada com filtros: nome, SKU, categoria, status
4. Endpoint `GET /api/products/{id}` retorna detalhes de um produto
5. Endpoint `PUT /api/products/{id}` edita produto (não permite mudar type após criação)
6. Endpoint `DELETE /api/products/{id}` marca produto como inativo (soft delete)
7. UI Angular permite cadastro manual com form validado (campos obrigatórios, SKU único)
8. Busca rápida por nome ou SKU retorna resultados em < 500ms (NFR3)

---

## Story 2.3: Products with Variants (Matrix)

Como **gerente de loja**,
Eu quero **produtos com variantes (matriz Cor x Tamanho)**,
Para que **eu possa gerenciar produtos que têm múltiplas combinações de atributos**.

**Critérios de Aceitação:**
1. Tabelas criadas: `product_attributes` (id, product_id, attribute_name), `product_attribute_values` (id, attribute_id, value), `product_variants` (id, product_id, sku, barcode, price, cost, combination_json)
2. Endpoint `POST /api/products` com `type=VARIANT` cria produto pai + define atributos (ex: Cor: [Azul, Preto], Tamanho: [P, M, G])
3. Endpoint `POST /api/products/{id}/variants` gera matriz de variantes automaticamente ou permite cadastro manual
4. Cada variante tem SKU único, código de barras próprio, preço/custo (herda do pai ou específico)
5. Validação: máximo 3 atributos e 100 variantes por produto (FR3)
6. Endpoint `GET /api/products/{id}/variants` retorna todas as variantes de um produto pai
7. UI exibe grade visual de variantes (tabela Cor x Tamanho) para fácil visualização
8. Estoque é controlado por variante individual (não pelo produto pai)

---

## Story 2.4: Composite Products / Kits (BOM)

Como **gerente de loja**,
Eu quero **produtos compostos/kits com BOM (Bill of Materials)**,
Para que **eu possa vender kits formados por múltiplos componentes (ex: Kit Churrasco = Carvão + Acendedor + Espetos)**.

**Critérios de Aceitação:**
1. Tabelas criadas: `product_components` (id, product_id, component_product_id, quantity_required)
2. Endpoint `POST /api/products` com `type=COMPOSITE` cria produto composto
3. Endpoint `POST /api/products/{id}/bom` define lista de componentes (product_id + quantity)
4. Validação: componente não pode ser outro produto composto (evita recursão infinita)
5. Configuração por produto: BOM virtual (baixa componentes na venda) ou BOM físico (kit pré-montado tem estoque próprio)
6. Endpoint `GET /api/products/{id}/bom` retorna lista de componentes
7. Se BOM virtual, estoque disponível do kit é calculado baseado em componentes (ex: se preciso 2 espetos e tenho 10, posso montar 5 kits)
8. UI permite buscar e adicionar componentes com quantidade, exibe estoque disponível calculado para kit

---

## Story 2.5: Product CSV Import

Como **gerente de loja**,
Eu quero **importar produtos via CSV/Excel com preview e validação**,
Para que **eu possa cadastrar centenas de produtos rapidamente sem entrada manual**.

**Critérios de Aceitação:**
1. Endpoint `POST /api/products/import/preview` recebe arquivo CSV, valida e retorna preview (primeiras 10 linhas + erros)
2. Validações: SKU duplicado, categoria inexistente, preço/custo inválidos, campos obrigatórios vazios
3. Endpoint `POST /api/products/import/confirm` confirma importação e persiste produtos válidos
4. Suporte a produtos simples, variantes (colunas de atributos) e compostos (planilha separada para BOM)
5. Importação de 1000 produtos completa em < 30s com feedback de progresso (NFR17)
6. UI Angular permite upload de arquivo, exibe preview com erros highlight em vermelho e botão "Confirmar Importação"
7. Template CSV de exemplo disponível para download (`/api/products/import/template`)

---

## Story 2.6: Stock Locations Management

Como **gerente de loja**,
Eu quero **CRUD de locais de estoque (multi-depósito)**,
Para que **eu possa gerenciar múltiplos locais de armazenamento (loja, CD, depósito)**.

**Critérios de Aceitação:**
1. Tabela `stock_locations` criada: id, tenant_id, name, address, responsible_user_id, status
2. Endpoint `POST /api/stock-locations` cria local de estoque
3. Endpoint `GET /api/stock-locations` retorna lista de locais ativos
4. Endpoint `PUT /api/stock-locations/{id}` edita local
5. Endpoint `DELETE /api/stock-locations/{id}` marca como inativo (soft delete) se não houver estoque alocado
6. Campo `responsible_user_id` associa usuário responsável pelo local (para auditoria)
7. UI permite CRUD com form simples (nome, endereço, responsável)

---

## Story 2.7: Multi-Warehouse Stock Control

Como **gerente de loja**,
Eu quero **controle de estoque por produto x local com quantidade disponível e reservada**,
Para que **eu saiba exatamente quanto estoque tenho em cada depósito**.

**Critérios de Aceitação:**
1. Tabela `stock` criada: id, tenant_id, product_id (ou variant_id), stock_location_id, quantity_available, quantity_reserved, minimum_quantity
2. Endpoint `GET /api/stock` retorna estoque consolidado ou por local com filtros
3. Campo calculado `quantity_for_sale = quantity_available - quantity_reserved` (FR6)
4. Para produtos compostos com BOM virtual: estoque é calculado dinamicamente baseado em componentes
5. Endpoint `PUT /api/stock/{product_id}/minimum` define estoque mínimo (threshold para alertas)
6. Endpoint `GET /api/stock/below-minimum` retorna produtos em ruptura (FR18)
7. UI exibe tabela de estoque com colunas: Produto, Local, Disponível, Reservado, Disponível para Venda, Mínimo
8. UI destaca em vermelho produtos abaixo do mínimo

---

## Story 2.8: Stock Movement History

Como **auditor**,
Eu quero **histórico completo e imutável de todas as movimentações de estoque**,
Para que **eu possa rastrear qualquer alteração de estoque para auditoria**.

**Critérios de Aceitação:**
1. Tabela `stock_movements` criada: id, tenant_id, product_id, stock_location_id, type (ENTRY, EXIT, TRANSFER, ADJUSTMENT, SALE, PURCHASE, RESERVE, RELEASE, BOM_ASSEMBLY), quantity, user_id, document_id (FK para venda/compra/transferência), reason, timestamp, balance_before, balance_after
2. Toda alteração de estoque (compra, venda, transferência, ajuste) cria registro em `stock_movements`
3. Movimentações são imutáveis (insert-only, sem update ou delete)
4. Endpoint `GET /api/stock/movements` retorna histórico com filtros: período, produto, local, tipo, usuário
5. UI exibe timeline de movimentações com filtros e paginação
6. Cada movimentação exibe: tipo, quantidade, usuário responsável, data/hora, saldos antes/depois
7. Teste valida que saldo após última movimentação = estoque atual

---

## Story 2.9: Stock Transfer Between Locations

Como **estoquista**,
Eu quero **transferir produtos entre locais de estoque com rastreabilidade**,
Para que **eu possa movimentar mercadoria entre loja, depósito e CD mantendo histórico**.

**Critérios de Aceitação:**
1. Tabela `stock_transfers` criada: id, tenant_id, product_id, origin_location_id, destination_location_id, quantity, reason, user_id, status, created_at
2. Endpoint `POST /api/stock/transfers` cria transferência
3. Validação: estoque origem tem quantidade disponível suficiente
4. Transferência atualiza estoque: saída na origem (`quantity_available -= quantity`) e entrada no destino (`quantity_available += quantity`)
5. Duas movimentações criadas em `stock_movements`: EXIT (origem) e ENTRY (destino) linkadas ao transfer_id
6. Endpoint `GET /api/stock/transfers` retorna histórico de transferências
7. UI permite selecionar produto, origem, destino, quantidade, motivo e confirmar transferência
8. UI exibe confirmação visual após transferência bem-sucedida
