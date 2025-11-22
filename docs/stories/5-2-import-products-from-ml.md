# Story 5.2: Import Products from Mercado Livre

**Epic**: 5 - Marketplace Integration - Mercado Livre
**Story ID**: 5.2
**Status**: drafted
**Created**: 2025-11-21

---

## User Story

Como **gerente de loja**,
Eu quero **importar anúncios existentes do Mercado Livre para o sistema**,
Para que **eu não precise recadastrar produtos que já vendo no ML (FR13)**.

---

## Context & Business Value

Importa anúncios ML existentes criando produtos no sistema com mapeamento bidirecional. Suporta variantes ML (atributos como Cor/Tamanho).

---

## Acceptance Criteria

### AC1: Tabela marketplace_listings Criada
- [ ] `marketplace_listings`: id, tenant_id, product_id, variant_id, marketplace, listing_id_marketplace, title, price, quantity, status, last_sync_at

### AC2: Endpoint GET /api/integrations/mercadolivre/listings (Preview)
- [ ] Busca anúncios ML via API (GET /users/me/items/search)
- [ ] Retorna lista com: listing_id, title, price, quantity, thumbnail, já_importado (boolean)

### AC3: Endpoint POST /api/integrations/mercadolivre/import-listings
- [ ] Recebe: listing_ids[] (selecionados pelo usuário)
- [ ] Para cada listing:
  1. GET /items/{id} busca detalhes completos
  2. Se tem variantes: cria produto tipo VARIANT + variants
  3. Se simples: cria produto tipo SIMPLE
  4. Cria registro marketplace_listings vinculando
  5. Sincroniza estoque inicial
- [ ] Retorna: {imported: 10, skipped: 2, errors: []}

### AC4: Mapeamento de Variantes ML
- [ ] ML usa attributes (COLOR, SIZE, etc.)
- [ ] Sistema converte para product_attributes + product_variants
- [ ] Exemplo: COLOR=Azul,Preto + SIZE=P,M,G → 6 variantes

### AC5: Frontend - Importação de Produtos ML
- [ ] Component `MercadoLivreImportComponent`
- [ ] Botão "Importar Produtos do ML"
- [ ] Lista anúncios com checkboxes (marca já importados como disabled)
- [ ] Botão "Importar Selecionados"
- [ ] Progress bar durante importação
- [ ] Toast com resultado

---

## Tasks
1. Migration marketplace_listings
2. MarketplaceListing entity
3. MercadoLivreProductImportService
4. MercadoLivreController endpoints
5. Frontend import component
6. Testes

---

**Story criada por**: PM Agent
**Data**: 2025-11-21
