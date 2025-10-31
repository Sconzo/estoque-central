# 7. External APIs

## 7.1. Google OAuth 2.0

**Propósito:** Autenticação de usuários

**Endpoints:**
- Authorization: `https://accounts.google.com/o/oauth2/v2/auth`
- Token: `https://oauth2.googleapis.com/token`
- UserInfo: `https://openidconnect.googleapis.com/v1/userinfo`

**Fluxo:**
1. Frontend redireciona para Google
2. Usuário autentica
3. Google redireciona de volta com code
4. Backend troca code por tokens
5. Backend cria JWT customizado

## 7.2. Mercado Livre API

**Propósito:** Sincronização de produtos e importação de pedidos

**Base URL:** `https://api.mercadolibre.com`

**Endpoints Principais:**
- `POST /oauth/token` - Obter access token
- `POST /items` - Criar anúncio
- `PUT /items/:id` - Atualizar anúncio
- `GET /orders/search` - Buscar pedidos

**Rate Limits:** 5-10 req/s

**Retry Strategy:** 3 tentativas com backoff exponencial

## 7.3. Focus NFe API

**Propósito:** Emissão de NFCe

**Base URL:** `https://api.focusnfe.com.br`

**Endpoints:**
- `POST /v2/nfce` - Emitir NFCe
- `GET /v2/nfce/:ref` - Consultar status
- `DELETE /v2/nfce/:ref` - Cancelar

**Retry Strategy:**
- Até 10 tentativas via Redisson DelayedQueue
- Backoff: 30s, 1m, 2m, 4m, 8m, 16m, 32m, 1h, 2h, 4h
- Após 10 falhas: FALHA_PERMANENTE, notificar gerente

**Webhook:** Backend recebe callbacks em `/webhooks/focusnfe`
