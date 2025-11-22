# Story 5.1: Mercado Livre OAuth2 Authentication

**Epic**: 5 - Marketplace Integration - Mercado Livre
**Story ID**: 5.1
**Status**: drafted
**Created**: 2025-11-21
**Updated**: 2025-11-21

---

## User Story

Como **gerente de loja**,
Eu quero **autenticar minha conta Mercado Livre via OAuth2 com refresh automático de tokens**,
Para que **o sistema possa gerenciar meus anúncios e pedidos automaticamente sem re-autenticação manual (FR13)**.

---

## Context & Business Value

Implementa autenticação OAuth2 com Mercado Livre usando fluxo Authorization Code. Armazena access_token e refresh_token criptografados, com refresh automático antes da expiração (6h). Base para todas as demais integrações ML.

**Valor de Negócio:**
- **Segurança**: OAuth2 evita armazenar senha do usuário
- **Automação**: Refresh automático elimina intervenção manual
- **Compliance**: Tokens criptografados (NFR14 - AES-256)
- **Multi-Tenant**: Cada tenant conecta sua própria conta ML

---

## Acceptance Criteria

### AC1: Tabela marketplace_connections Criada
- [ ] Migration cria `marketplace_connections`:
  - id, tenant_id, marketplace (MERCADO_LIVRE), user_id_marketplace (ML user ID)
  - access_token (ENCRYPTED), refresh_token (ENCRYPTED), token_expires_at
  - status (CONNECTED, DISCONNECTED, ERROR), last_sync_at, error_message
  - data_criacao, data_atualizacao
- [ ] Índices: idx_mc_tenant_id, idx_mc_marketplace
- [ ] Constraint: UNIQUE (tenant_id, marketplace) - 1 conexão ML por tenant

### AC2: OAuth2 Flow - Iniciar Autenticação
- [ ] Endpoint `GET /api/integrations/mercadolivre/auth/init`
- [ ] Gera authorization_url com callback redirect_uri
- [ ] URL: `https://auth.mercadolivre.com.br/authorization?response_type=code&client_id={APP_ID}&redirect_uri={CALLBACK}&state={TENANT_ID_ENCRYPTED}`
- [ ] Retorna authorization_url para frontend redirecionar

### AC3: OAuth2 Flow - Callback e Troca de Code por Token
- [ ] Endpoint `GET /api/integrations/mercadolivre/auth/callback?code={code}&state={state}`
- [ ] Valida state (decripta e verifica tenant_id)
- [ ] Troca code por access_token via POST ML API /oauth/token
- [ ] Salva access_token, refresh_token (criptografados), expires_at
- [ ] Busca user_id do ML via GET /users/me
- [ ] Cria/atualiza registro em marketplace_connections
- [ ] Retorna HTTP 302 redirect para frontend com sucesso

### AC4: Refresh Automático de Token
- [ ] @Scheduled job roda a cada 1 hora
- [ ] Query conexões ML com token_expires_at < NOW() + 30min
- [ ] Para cada conexão, chama POST ML API /oauth/token com grant_type=refresh_token
- [ ] Atualiza access_token, refresh_token, expires_at
- [ ] Se refresh falha: atualiza status=ERROR, notifica usuário

### AC5: Criptografia de Tokens (NFR14)
- [ ] Tokens criptografados com AES-256 (reusa CryptoService Story 4.1)
- [ ] @Convert aplicado nos campos access_token, refresh_token
- [ ] Chave armazenada em Azure Key Vault

### AC6: Frontend - Painel de Integração ML
- [ ] Component `MercadoLivreIntegrationComponent` (admin/gerente)
- [ ] Card "Mercado Livre" exibe status: Conectado (verde) ou Desconectado (cinza)
- [ ] Se desconectado: Botão "Conectar com Mercado Livre"
- [ ] Ao clicar: abre popup OAuth ML (chamada GET /auth/init)
- [ ] Após callback bem-sucedido: exibe toast "Conectado com sucesso!"
- [ ] Se conectado: exibe user_id ML, última sincronização, botão "Desconectar"

### AC7: Endpoint POST /api/integrations/mercadolivre/disconnect
- [ ] Revoga tokens no ML (POST ML API /applications/{app_id}/tokens/{user_id})
- [ ] Atualiza status=DISCONNECTED
- [ ] Retorna HTTP 200

### AC8: Service MercadoLivreApiClient
- [ ] Classe wrapper para chamadas ML API
- [ ] Método `get(endpoint, tenantId)` busca access_token, adiciona ao header Authorization
- [ ] Método `post(endpoint, body, tenantId)`
- [ ] Tratamento de erro 401 (token expirado): tenta refresh e retry automático
- [ ] Timeout configurável (default: 10s)

---

## Tasks & Subtasks

### Task 1: Registrar App no Mercado Livre Developers
- [ ] Criar app em https://developers.mercadolivre.com.br
- [ ] Obter APP_ID e SECRET_KEY
- [ ] Configurar redirect_uri: https://{domain}/api/integrations/mercadolivre/auth/callback
- [ ] Armazenar credentials em Azure Key Vault (env vars em dev)

### Task 2: Criar Migration marketplace_connections
- [ ] V060__create_marketplace_connections_table.sql

### Task 3: Criar Entidade e Repository
- [ ] MarketplaceConnection.java
- [ ] Enum Marketplace (MERCADO_LIVRE)
- [ ] @Convert para access_token, refresh_token
- [ ] MarketplaceConnectionRepository

### Task 4: Implementar MercadoLivreOAuthService
- [ ] Método `getAuthorizationUrl(tenantId)` retorna URL
- [ ] Método `handleCallback(code, state)` troca code por token
- [ ] Método `refreshToken(connectionId)` atualiza tokens

### Task 5: Implementar TokenRefreshScheduledJob
- [ ] @Scheduled(fixedDelay = 3600000) // 1 hora
- [ ] Query conexões próximas à expiração
- [ ] Chama refreshToken() para cada

### Task 6: Implementar MercadoLivreApiClient
- [ ] Wrapper RestTemplate com interceptor de auth
- [ ] Retry automático em 401
- [ ] Timeout 10s

### Task 7: Criar MercadoLivreController
- [ ] GET /auth/init
- [ ] GET /auth/callback
- [ ] POST /disconnect

### Task 8: Frontend - MercadoLivreIntegrationComponent
- [ ] Card com status de conexão
- [ ] Botão conectar (abre popup OAuth)
- [ ] Tratamento de callback

### Task 9: Testes
- [ ] Teste: authorization_url gerada corretamente
- [ ] Teste: callback troca code por token
- [ ] Teste: tokens salvos criptografados
- [ ] Teste: refresh automático atualiza tokens
- [ ] Teste: erro 401 triggera refresh e retry

---

## Definition of Done (DoD)

- [ ] Migration executada
- [ ] Entidade MarketplaceConnection criada
- [ ] OAuth2 flow completo (init + callback)
- [ ] Tokens criptografados (AES-256)
- [ ] Refresh automático implementado
- [ ] MercadoLivreApiClient wrapper criado
- [ ] Frontend permite conectar/desconectar
- [ ] Testes passando
- [ ] Code review aprovado

---

## Dependencies & Blockers

**Depende de:**
- Story 1.3 (Multi-tenancy) - Conexões são tenant-specific

**Bloqueia:**
- Stories 5.2-5.7 (todas dependem de autenticação)

---

## Technical Notes

**OAuth2 Flow:**
```java
@Service
public class MercadoLivreOAuthService {
    private static final String AUTH_URL = "https://auth.mercadolivre.com.br/authorization";
    private static final String TOKEN_URL = "https://api.mercadolibre.com/oauth/token";

    public String getAuthorizationUrl(UUID tenantId) {
        String state = encryptState(tenantId); // Encrypt tenant ID for security
        return UriComponentsBuilder.fromHttpUrl(AUTH_URL)
            .queryParam("response_type", "code")
            .queryParam("client_id", appId)
            .queryParam("redirect_uri", redirectUri)
            .queryParam("state", state)
            .toUriString();
    }

    @Transactional
    public void handleCallback(String code, String state) {
        UUID tenantId = decryptState(state);
        TenantContext.setCurrentTenant(tenantId);

        // Trocar code por access_token
        TokenResponse tokenResponse = restTemplate.postForObject(TOKEN_URL,
            Map.of(
                "grant_type", "authorization_code",
                "client_id", appId,
                "client_secret", secretKey,
                "code", code,
                "redirect_uri", redirectUri
            ),
            TokenResponse.class
        );

        // Buscar user_id do ML
        String userId = getUserId(tokenResponse.getAccessToken());

        // Salvar conexão
        MarketplaceConnection connection = new MarketplaceConnection();
        connection.setTenantId(tenantId);
        connection.setMarketplace(Marketplace.MERCADO_LIVRE);
        connection.setUserIdMarketplace(userId);
        connection.setAccessToken(tokenResponse.getAccessToken());
        connection.setRefreshToken(tokenResponse.getRefreshToken());
        connection.setTokenExpiresAt(LocalDateTime.now().plusSeconds(tokenResponse.getExpiresIn()));
        connection.setStatus(ConnectionStatus.CONNECTED);
        connectionRepository.save(connection);
    }

    @Transactional
    public void refreshToken(UUID connectionId) {
        MarketplaceConnection conn = connectionRepository.findById(connectionId)
            .orElseThrow();

        TokenResponse tokenResponse = restTemplate.postForObject(TOKEN_URL,
            Map.of(
                "grant_type", "refresh_token",
                "client_id", appId,
                "client_secret", secretKey,
                "refresh_token", conn.getRefreshToken()
            ),
            TokenResponse.class
        );

        conn.setAccessToken(tokenResponse.getAccessToken());
        conn.setRefreshToken(tokenResponse.getRefreshToken());
        conn.setTokenExpiresAt(LocalDateTime.now().plusSeconds(tokenResponse.getExpiresIn()));
        connectionRepository.save(conn);
    }
}
```

**API Client com Retry Automático:**
```java
@Service
public class MercadoLivreApiClient {
    public <T> T get(String endpoint, Class<T> responseType, UUID tenantId) {
        MarketplaceConnection conn = getConnection(tenantId);

        try {
            return restTemplate.exchange(
                ML_API_BASE + endpoint,
                HttpMethod.GET,
                new HttpEntity<>(createHeaders(conn.getAccessToken())),
                responseType
            ).getBody();
        } catch (HttpClientErrorException e) {
            if (e.getStatusCode() == HttpStatus.UNAUTHORIZED) {
                // Token expirado, refresh e retry
                oauthService.refreshToken(conn.getId());
                conn = getConnection(tenantId); // Reload
                return restTemplate.exchange(
                    ML_API_BASE + endpoint,
                    HttpMethod.GET,
                    new HttpEntity<>(createHeaders(conn.getAccessToken())),
                    responseType
                ).getBody();
            }
            throw e;
        }
    }
}
```

---

**Story criada por**: Claude Code Assistant (Product Manager)
**Data**: 2025-11-21
**Baseado em**: Epic 5, docs/epics/epic-05-marketplace-integration.md
