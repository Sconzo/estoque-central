# Story 5.3: Publish Products to Mercado Livre

**Epic**: 5 - Marketplace Integration - Mercado Livre
**Story ID**: 5.3
**Status**: drafted
**Created**: 2025-11-21

---

## User Story

Como **gerente de loja**,
Eu quero **publicar produtos do sistema como anúncios no Mercado Livre com suporte a variantes**,
Para que **eu possa vender meus produtos no marketplace (FR13)**.

---

## Context & Business Value

Publica produtos do sistema como anúncios ML, mapeando variantes corretamente. Cria listings e armazena listing_id para sincronização futura.

---

## Acceptance Criteria

### AC1: Endpoint POST /api/integrations/mercadolivre/publish
- [ ] Recebe: product_ids[] (produtos a publicar)
- [ ] Para cada produto:
  1. Monta payload ML conforme schema (title, description, price, category_id, pictures, attributes)
  2. Se VARIANT: mapeia variantes para variations ML
  3. POST /items no ML
  4. Salva listing_id em marketplace_listings
  5. Sincroniza estoque com margem de segurança (Story 5.7)
- [ ] Retorna: {published: 5, errors: []}

### AC2: Mapeamento de Categoria ML
- [ ] Usuário seleciona categoria ML via predictor API: GET /sites/MLB/category_predictor/predict?title={title}
- [ ] Sistema sugere categoria, usuário confirma/altera
- [ ] Salva category_id em marketplace_listings

### AC3: Mapeamento de Atributos Obrigatórios
- [ ] Para variantes: mapeia COLOR, SIZE conforme product_attributes
- [ ] Outros atributos opcionais podem ser mapeados manualmente

### AC4: Upload de Imagens
- [ ] Envia imagens para ML (POST /pictures com multipart/form-data)
- [ ] ML retorna picture_ids, usado em variations[].picture_ids

### AC5: Frontend - Publicação de Produtos
- [ ] Component `MercadoLivrePublishComponent`
- [ ] Lista produtos não publicados
- [ ] Wizard: 1) Selecionar produtos, 2) Configurar categoria/atributos, 3) Preview, 4) Publicar
- [ ] Progress bar durante publicação

---

## Tasks
1. MercadoLivrePublishService
2. Category predictor integration
3. Image upload to ML
4. Variants mapping logic
5. Frontend publish wizard
6. Testes

---

**Story criada por**: PM Agent
**Data**: 2025-11-21
