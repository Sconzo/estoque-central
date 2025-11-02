# Story 1.4: Google OAuth 2.0 Authentication

**Epic**: 1 - Foundation & Core Infrastructure
**Story ID**: 1.4
**Status**: drafted
**Created**: 2025-02-11
**Updated**: 2025-02-11

---

## User Story

Como **usuário**,
Eu quero **autenticação via Google OAuth 2.0**,
Para que **eu possa fazer login de forma segura sem gerenciar senhas**.

---

## Context & Business Value

Esta story implementa autenticação OAuth 2.0 usando Google como Identity Provider, eliminando a necessidade de gerenciar senhas localmente. O sistema emite JWT tokens customizados contendo tenant ID e roles, permitindo que requisições subsequentes sejam autorizadas sem consultar Google a cada vez.

**Valor de Negócio:**
- Elimina gestão de senhas (reduz risco de vazamento de credentials)
- UX familiar (botão "Entrar com Google" reconhecido por usuários)
- Reduz atrito no onboarding (usuários não precisam criar nova conta)
- Conformidade com LGPD (dados de autenticação delegados ao Google)

**Contexto Arquitetural:**
- **OAuth 2.0 Authorization Code Flow**: Fluxo padrão para web apps [Source: docs/architecture/15-security-and-performance.md:24-46]
- **JWT customizado**: Backend emite token próprio com tenantId + roles após validar Google token [Source: docs/architecture/15-security-and-performance.md:48-65]
- **Stateless authentication**: Sessões armazenadas no JWT, não no servidor [Source: docs/architecture/15-security-and-performance.md:94]

---

## Acceptance Criteria

### AC1: Spring Security OAuth2 Client Configurado
- [ ] Dependência `spring-boot-starter-oauth2-client` adicionada ao `pom.xml`
- [ ] `application.properties` configurado com Google Client ID e Secret (via env vars)
- [ ] OAuth2 registration configurado com scopes: `openid`, `profile`, `email`
- [ ] Redirect URI registrado no Google Cloud Console: `http://localhost:8080/login/oauth2/code/google`

**Validação**: `mvn dependency:tree | grep oauth2-client` retorna dependência

### AC2: Backend OAuth Flow Implementado
- [ ] Endpoint `POST /api/auth/google/callback` recebe authorization code do frontend
- [ ] Service `GoogleAuthService` troca código por tokens via Google Token Endpoint
- [ ] ID Token do Google é validado (assinatura, issuer, audience, expiration)
- [ ] Dados do usuário extraídos do ID Token: `sub`, `email`, `name`, `picture`
- [ ] Usuário criado/atualizado na tabela `usuarios` (tenant-specific schema)

**Validação**:
```bash
curl -X POST http://localhost:8080/api/auth/google/callback \
  -H "Content-Type: application/json" \
  -d '{"code": "test-code", "tenantId": "uuid"}'
# Retorna HTTP 200 com JWT
```

### AC3: JWT Customizado Gerado
- [ ] Biblioteca JJWT (io.jsonwebtoken) configurada para gerar/validar JWTs
- [ ] Service `JwtService` implementado com métodos `generateToken()` e `validateToken()`
- [ ] JWT payload contém: `sub` (user ID), `tenantId`, `email`, `roles` (array), `iat`, `exp`
- [ ] JWT assinado com HS256 usando secret de 256+ bits (env var `JWT_SECRET`)
- [ ] Token expira em 24 horas (86400 segundos)

**Validação**: Decodificar JWT em https://jwt.io e verificar payload

### AC4: JWT Filter Protege Endpoints
- [ ] Filter `JwtAuthenticationFilter` implementado extends `OncePerRequestFilter`
- [ ] Filter extrai JWT do header `Authorization: Bearer <token>`
- [ ] Filter valida JWT (assinatura, expiration) e popula `SecurityContext`
- [ ] Filter extrai `tenantId` do JWT e seta em `TenantContext` (integração com Story 1.3)
- [ ] Filter registrado antes de `UsernamePasswordAuthenticationFilter` na chain
- [ ] Endpoints `/api/auth/**` e `/actuator/health` permitidos sem autenticação

**Validação**: Requisição sem JWT retorna HTTP 401, com JWT válido retorna HTTP 200

### AC5: Endpoint GET /api/auth/me Implementado
- [ ] Controller `AuthController` criado com endpoint `GET /api/auth/me`
- [ ] Endpoint retorna dados do usuário autenticado: `id`, `email`, `name`, `tenantId`, `roles`
- [ ] Endpoint protegido por `@PreAuthorize("isAuthenticated()")`
- [ ] Usuário extraído de `SecurityContextHolder.getContext().getAuthentication()`

**Validação**:
```bash
curl http://localhost:8080/api/auth/me \
  -H "Authorization: Bearer <jwt-token>"
# Retorna HTTP 200 com user data JSON
```

### AC6: Frontend Login com Google
- [ ] Componente Angular `LoginComponent` criado em `app/features/auth/`
- [ ] Botão "Entrar com Google" renderizado com Google branding guidelines
- [ ] Clique no botão inicia OAuth flow via redirect ou popup
- [ ] Biblioteca `angular-oauth2-oidc` configurada para Google Provider
- [ ] Após callback OAuth, frontend envia code para `POST /api/auth/google/callback`
- [ ] JWT recebido do backend armazenado em `localStorage` ou `sessionStorage`
- [ ] Frontend redireciona para dashboard após login bem-sucedido

**Validação**: Clicar em "Entrar com Google" redireciona para Google, após autorizar volta logado

### AC7: Logout Implementado
- [ ] Endpoint `POST /api/auth/logout` criado (opcional, pois JWT é stateless)
- [ ] Frontend remove JWT de `localStorage` ao fazer logout
- [ ] Frontend redireciona para `/login` após logout
- [ ] Opcional: Backend mantém blacklist de tokens revogados (Redis) para logout forçado

**Validação**:
```bash
# Frontend
localStorage.removeItem('jwt_token');
window.location.href = '/login';
```

---

## Tasks & Subtasks

### Task 1: Configurar Spring Security OAuth2 Client
**AC: #1**
- [ ] Adicionar dependência ao `pom.xml`:
  ```xml
  <dependency>
      <groupId>org.springframework.boot</groupId>
      <artifactId>spring-boot-starter-oauth2-client</artifactId>
  </dependency>
  ```
- [ ] Configurar `application.properties`:
  ```properties
  spring.security.oauth2.client.registration.google.client-id=${GOOGLE_OAUTH_CLIENT_ID}
  spring.security.oauth2.client.registration.google.client-secret=${GOOGLE_OAUTH_CLIENT_SECRET}
  spring.security.oauth2.client.registration.google.redirect-uri={baseUrl}/login/oauth2/code/google
  spring.security.oauth2.client.registration.google.scope=openid,profile,email
  spring.security.oauth2.client.provider.google.issuer-uri=https://accounts.google.com
  ```
- [ ] Registrar Redirect URI no Google Cloud Console
- [ ] Testar configuração: `mvn clean compile`

### Task 2: Implementar GoogleAuthService
**AC: #2**
- [ ] Criar `com.estoquecentral.auth.application.GoogleAuthService`
- [ ] Método `authenticateWithGoogle(String code, String tenantId) -> String jwt`:
  1. Usar `RestTemplate` ou `WebClient` para trocar code por tokens:
     ```java
     POST https://oauth2.googleapis.com/token
     {
       "code": "<authorization-code>",
       "client_id": "<client-id>",
       "client_secret": "<client-secret>",
       "redirect_uri": "<redirect-uri>",
       "grant_type": "authorization_code"
     }
     ```
  2. Receber `access_token` e `id_token` do Google
  3. Validar `id_token` (verificar assinatura usando Google public keys)
  4. Extrair dados: `sub` (Google user ID), `email`, `name`, `picture`
  5. Chamar `UserService.findOrCreateUser(googleId, email, name, tenantId)`
  6. Chamar `JwtService.generateToken(user)`
  7. Retornar JWT
- [ ] Adicionar tratamento de erros: código inválido, token expirado, usuário bloqueado

### Task 3: Criar Entidade Usuario e Repository
**AC: #2, #5**
- [ ] Criar `com.estoquecentral.auth.domain.Usuario`:
  ```java
  @Table("usuarios")
  public class Usuario {
      @Id UUID id;
      String googleId; // sub do Google (UNIQUE)
      String email;
      String nome;
      String pictureUrl;
      UUID tenantId; // Foreign key (lógica) para tenants
      String role; // "ADMIN", "GERENTE", "VENDEDOR", "ESTOQUISTA"
      Boolean ativo;
      Instant dataCriacao;
      Instant dataAtualizacao;
  }
  ```
- [ ] Criar `com.estoquecentral.auth.adapter.out.UsuarioRepository extends CrudRepository`
- [ ] Método `Optional<Usuario> findByGoogleId(String googleId)`
- [ ] Método `Optional<Usuario> findByEmail(String email)`

### Task 4: Implementar UserService
**AC: #2, #5**
- [ ] Criar `com.estoquecentral.auth.application.UserService`
- [ ] Método `findOrCreateUser(String googleId, String email, String nome, UUID tenantId) -> Usuario`:
  1. Buscar usuário existente por `googleId`
  2. Se encontrado: atualizar `nome`, `pictureUrl`, `dataAtualizacao`
  3. Se não encontrado: criar novo usuário com role padrão "VENDEDOR"
  4. Salvar/atualizar via `UsuarioRepository`
  5. Retornar entidade `Usuario`
- [ ] Método `getUserById(UUID id) -> Usuario` (para endpoint `/api/auth/me`)
- [ ] Validar que `tenantId` existe na tabela `public.tenants`

### Task 5: Implementar JwtService
**AC: #3**
- [ ] Adicionar dependência JJWT ao `pom.xml`:
  ```xml
  <dependency>
      <groupId>io.jsonwebtoken</groupId>
      <artifactId>jjwt-api</artifactId>
      <version>0.12.5</version>
  </dependency>
  <dependency>
      <groupId>io.jsonwebtoken</groupId>
      <artifactId>jjwt-impl</artifactId>
      <version>0.12.5</version>
      <scope>runtime</scope>
  </dependency>
  <dependency>
      <groupId>io.jsonwebtoken</groupId>
      <artifactId>jjwt-jackson</artifactId>
      <version>0.12.5</version>
      <scope>runtime</scope>
  </dependency>
  ```
- [ ] Criar `com.estoquecentral.auth.application.JwtService`
- [ ] Método `generateToken(Usuario usuario) -> String`:
  ```java
  return Jwts.builder()
      .subject(usuario.getId().toString())
      .claim("tenantId", usuario.getTenantId().toString())
      .claim("email", usuario.getEmail())
      .claim("roles", List.of(usuario.getRole()))
      .issuedAt(new Date())
      .expiration(new Date(System.currentTimeMillis() + 86400000)) // 24h
      .signWith(getSigningKey())
      .compact();
  ```
- [ ] Método `validateToken(String token) -> Claims` (lança exceção se inválido)
- [ ] Método `getUserIdFromToken(String token) -> UUID`
- [ ] Método `getTenantIdFromToken(String token) -> UUID`
- [ ] Método privado `getSigningKey()` lê `JWT_SECRET` de env var

### Task 6: Implementar JwtAuthenticationFilter
**AC: #4**
- [ ] Criar `com.estoquecentral.auth.adapter.in.security.JwtAuthenticationFilter extends OncePerRequestFilter`
- [ ] Override `doFilterInternal()`:
  1. Extrair header: `String authHeader = request.getHeader("Authorization")`
  2. Se header ausente ou não começa com "Bearer ": skip (continue chain)
  3. Extrair token: `String jwt = authHeader.substring(7)`
  4. Validar token via `JwtService.validateToken(jwt)`
  5. Se válido:
     - Extrair userId e tenantId do token
     - Setar `TenantContext.setTenantId(tenantId)` (integração Story 1.3)
     - Buscar usuario via `UserService.getUserById(userId)`
     - Criar `UsernamePasswordAuthenticationToken` com authorities (roles)
     - Setar em `SecurityContextHolder.getContext().setAuthentication(...)`
  6. Continue filter chain
- [ ] Tratar exceções: token inválido, expirado, usuário não encontrado

### Task 7: Configurar Spring Security
**AC: #4, #6**
- [ ] Criar `com.estoquecentral.auth.adapter.in.security.SecurityConfig`
- [ ] Bean `SecurityFilterChain`:
  ```java
  @Bean
  SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
      return http
          .csrf(csrf -> csrf.disable()) // Stateless JWT
          .cors(cors -> cors.configurationSource(corsConfig()))
          .authorizeHttpRequests(auth -> auth
              .requestMatchers("/api/auth/**").permitAll()
              .requestMatchers("/actuator/health").permitAll()
              .requestMatchers("/api/**").authenticated()
          )
          .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class)
          .sessionManagement(session -> session
              .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
          )
          .build();
  }
  ```
- [ ] Bean `CorsConfigurationSource` para permitir frontend localhost:4200

### Task 8: Criar AuthController
**AC: #2, #5, #7**
- [ ] Criar `com.estoquecentral.auth.adapter.in.AuthController`
- [ ] Endpoint `POST /api/auth/google/callback`:
  ```java
  @PostMapping("/google/callback")
  ResponseEntity<LoginResponse> googleCallback(@RequestBody GoogleCallbackRequest request) {
      String jwt = googleAuthService.authenticateWithGoogle(request.getCode(), request.getTenantId());
      return ResponseEntity.ok(new LoginResponse(jwt));
  }
  ```
- [ ] Endpoint `GET /api/auth/me`:
  ```java
  @GetMapping("/me")
  @PreAuthorize("isAuthenticated()")
  ResponseEntity<UserDTO> getCurrentUser(Authentication auth) {
      UUID userId = UUID.fromString(auth.getName());
      Usuario usuario = userService.getUserById(userId);
      return ResponseEntity.ok(UserDTO.fromEntity(usuario));
  }
  ```
- [ ] Endpoint `POST /api/auth/logout` (opcional):
  ```java
  @PostMapping("/logout")
  ResponseEntity<Void> logout() {
      // Stateless - apenas retorna 200
      // Frontend remove JWT do localStorage
      return ResponseEntity.ok().build();
  }
  ```
- [ ] DTOs: `GoogleCallbackRequest`, `LoginResponse`, `UserDTO`

### Task 9: Criar LoginComponent Angular
**AC: #6**
- [ ] Criar componente: `ng g c features/auth/login`
- [ ] Instalar biblioteca: `npm install angular-oauth2-oidc`
- [ ] Configurar `OAuthService` em `app.config.ts`:
  ```typescript
  import { AuthConfig } from 'angular-oauth2-oidc';

  const authConfig: AuthConfig = {
    issuer: 'https://accounts.google.com',
    redirectUri: window.location.origin + '/auth/callback',
    clientId: '<GOOGLE_CLIENT_ID>',
    scope: 'openid profile email',
    responseType: 'code',
  };
  ```
- [ ] Template HTML: botão "Entrar com Google" com ícone
- [ ] Método `loginWithGoogle()`:
  1. `this.oauthService.initCodeFlow()`
  2. Redireciona para Google
  3. Google retorna para `/auth/callback?code=...`
- [ ] Componente `AuthCallbackComponent`:
  1. Extrai code da URL
  2. Chama backend: `POST /api/auth/google/callback {code, tenantId}`
  3. Salva JWT em `localStorage.setItem('jwt_token', jwt)`
  4. Redireciona para `/dashboard`

### Task 10: Implementar AuthService Angular
**AC: #6, #7**
- [ ] Criar service: `ng g s core/auth/auth`
- [ ] Método `login(code: string, tenantId: string): Observable<string>` (retorna JWT)
- [ ] Método `logout(): void`:
  ```typescript
  logout() {
    localStorage.removeItem('jwt_token');
    this.router.navigate(['/login']);
  }
  ```
- [ ] Método `getToken(): string | null` (lê de localStorage)
- [ ] Método `isAuthenticated(): boolean` (verifica se token existe e não expirou)
- [ ] Método `getCurrentUser(): Observable<User>` (chama `GET /api/auth/me`)

### Task 11: Implementar HTTP Interceptor Angular
**AC: #6**
- [ ] Criar interceptor: `ng g interceptor core/auth/jwt`
- [ ] Interceptor adiciona header `Authorization: Bearer <token>` em todas as requisições:
  ```typescript
  intercept(req: HttpRequest<any>, next: HttpHandler): Observable<HttpEvent<any>> {
    const token = this.authService.getToken();
    if (token) {
      req = req.clone({
        setHeaders: { Authorization: `Bearer ${token}` }
      });
    }
    return next.handle(req);
  }
  ```
- [ ] Registrar interceptor em `app.config.ts`

### Task 12: Criar Migration para Tabela usuarios
**AC: #2**
- [ ] Criar migration: `backend/src/main/resources/db/migration/tenant/V003__create_usuarios_table.sql`
- [ ] SQL:
  ```sql
  CREATE TABLE IF NOT EXISTS usuarios (
      id UUID PRIMARY KEY,
      google_id VARCHAR(255) UNIQUE NOT NULL,
      email VARCHAR(255) UNIQUE NOT NULL,
      nome VARCHAR(255) NOT NULL,
      picture_url VARCHAR(500),
      role VARCHAR(50) NOT NULL CHECK (role IN ('ADMIN', 'GERENTE', 'VENDEDOR', 'ESTOQUISTA')),
      ativo BOOLEAN DEFAULT true NOT NULL,
      data_criacao TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
      data_atualizacao TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL
  );

  CREATE INDEX idx_usuarios_google_id ON usuarios(google_id);
  CREATE INDEX idx_usuarios_email ON usuarios(email);
  CREATE INDEX idx_usuarios_ativo ON usuarios(ativo) WHERE ativo = true;
  ```
- [ ] Migration será aplicada em TODOS os schemas de tenant via FlywayMultiTenantConfig

### Task 13: Testes de Integração
**AC: #2, #4, #5**
- [ ] Teste: `GoogleAuthServiceTest`:
  - Mock Google Token Endpoint com WireMock
  - Validar que code é trocado por token corretamente
  - Validar criação de novo usuário
  - Validar atualização de usuário existente
- [ ] Teste: `JwtServiceTest`:
  - Validar geração de token com payload correto
  - Validar validação de token válido
  - Validar rejeição de token expirado
  - Validar rejeição de token com assinatura inválida
- [ ] Teste: `JwtAuthenticationFilterTest`:
  - Validar que request com token válido popula SecurityContext
  - Validar que request sem token continua chain sem autenticação
  - Validar que request com token inválido retorna 401
- [ ] Teste: `AuthControllerTest`:
  - Validar `POST /api/auth/google/callback` retorna JWT
  - Validar `GET /api/auth/me` retorna dados do usuário autenticado
  - Validar `GET /api/auth/me` sem token retorna 401

### Task 14: Documentar Autenticação no README
**AC: N/A - Documentação**
- [ ] Adicionar seção "Autenticação" ao README.md:
  ```markdown
  ## Autenticação

  O sistema usa **Google OAuth 2.0** para autenticação.

  ### Setup Google OAuth

  1. Criar projeto no Google Cloud Console
  2. Habilitar "Google+ API"
  3. Criar OAuth 2.0 Client ID (Web application)
  4. Configurar Redirect URI: `http://localhost:8080/login/oauth2/code/google`
  5. Copiar Client ID e Secret para `.env`:
     \`\`\`
     GOOGLE_OAUTH_CLIENT_ID=your-client-id.apps.googleusercontent.com
     GOOGLE_OAUTH_CLIENT_SECRET=your-client-secret
     JWT_SECRET=your-256-bit-secret-key
     \`\`\`

  ### Login via API

  1. Usuário clica "Entrar com Google" no frontend
  2. Frontend redireciona para Google OAuth
  3. Usuário autoriza e Google retorna code
  4. Frontend envia code para backend:
     \`\`\`bash
     curl -X POST http://localhost:8080/api/auth/google/callback \
       -H "Content-Type: application/json" \
       -d '{"code": "...", "tenantId": "uuid"}'
     \`\`\`
  5. Backend retorna JWT:
     \`\`\`json
     {"token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."}
     \`\`\`
  6. Frontend armazena JWT e inclui em todas as requisições:
     \`\`\`bash
     curl http://localhost:8080/api/produtos \
       -H "Authorization: Bearer <jwt-token>"
     \`\`\`

  ### Dados do Usuário Autenticado

  \`\`\`bash
  curl http://localhost:8080/api/auth/me \
    -H "Authorization: Bearer <jwt-token>"
  \`\`\`

  Resposta:
  \`\`\`json
  {
    "id": "uuid",
    "email": "user@example.com",
    "nome": "João Silva",
    "tenantId": "uuid",
    "role": "GERENTE",
    "ativo": true
  }
  \`\`\`

  ### Logout

  \`\`\`typescript
  // Frontend
  localStorage.removeItem('jwt_token');
  window.location.href = '/login';
  \`\`\`
  ```

---

## Technical Implementation Notes

### OAuth 2.0 Authorization Code Flow

**MANDATORY** - Seguir fluxo padrão OAuth 2.0:

1. **Frontend** → Google: Redirect com client_id, redirect_uri, scope
2. **Google** → User: Tela de autorização
3. **User** → Google: Credenciais + autoriza
4. **Google** → Frontend: Redirect com authorization code
5. **Frontend** → Backend: POST code
6. **Backend** → Google: Trocar code por access_token + id_token
7. **Backend** → Backend: Validar id_token, criar/atualizar usuário
8. **Backend** → Frontend: JWT customizado
9. **Frontend** armazena JWT em localStorage
10. **Frontend** inclui JWT em todas as requisições: `Authorization: Bearer <token>`

[Source: docs/architecture/15-security-and-performance.md:24-46]

### JWT Structure (Customizado)

**MANDATORY** - JWT deve conter claims específicos:

```json
{
  "sub": "user-uuid",              // Usuario.id
  "tenantId": "tenant-uuid",       // Usuario.tenantId
  "email": "user@example.com",     // Usuario.email
  "roles": ["GERENTE"],            // Usuario.role (array para futuro multi-role)
  "iat": 1699900000,               // Issued at (timestamp)
  "exp": 1699986400                // Expiration (iat + 24h)
}
```

**Importante**: NÃO usar Google ID Token diretamente. Backend deve emitir JWT próprio após validar Google token, pois:
- Google token não contém `tenantId` (essencial para multi-tenancy)
- Google token não contém `roles` (essencial para RBAC)
- Google token tem TTL curto (1 hora), nosso JWT tem 24h

[Source: docs/architecture/15-security-and-performance.md:48-65]

### Security Best Practices

1. **JWT Secret**: Mínimo 256 bits (32 caracteres), armazenado em env var
2. **Token Storage**: Frontend armazena em `localStorage` (não em cookies para evitar CSRF)
3. **HTTPS Only**: Em produção, JWT só transmitido via HTTPS
4. **Token Expiration**: 24 horas (86400 segundos)
5. **Google ID Token Validation**: SEMPRE validar assinatura usando Google public keys
6. **CORS**: Configurar CORS para permitir apenas origem do frontend

[Source: docs/architecture/15-security-and-performance.md:67-104]

### Integration with Story 1.3 (Multi-Tenancy)

**CRITICAL** - JwtAuthenticationFilter deve integrar com TenantContext:

```java
@Override
protected void doFilterInternal(HttpServletRequest request, ...) {
    String jwt = extractJwtFromHeader(request);

    if (jwt != null && jwtService.validateToken(jwt)) {
        UUID userId = jwtService.getUserIdFromToken(jwt);
        UUID tenantId = jwtService.getTenantIdFromToken(jwt);

        // INTEGRAÇÃO COM STORY 1.3
        TenantContext.setTenantId(tenantId.toString());

        // Popula SecurityContext
        Usuario usuario = userService.getUserById(userId);
        Authentication auth = createAuthentication(usuario);
        SecurityContextHolder.getContext().setAuthentication(auth);
    }

    filterChain.doFilter(request, response);

    // TenantContext.clear() já é chamado por TenantInterceptor
}
```

**Importante**: Tenant ID vem do JWT (não do header X-Tenant-ID), pois usuário já está associado a um tenant fixo.

### Google OAuth Configuration

**Pre-requisites**:
1. Google Cloud Project criado
2. OAuth 2.0 Client ID criado (Web application)
3. Authorized redirect URIs configurado:
   - Desenvolvimento: `http://localhost:8080/login/oauth2/code/google`
   - Produção: `https://app.estoque-central.com/login/oauth2/code/google`
4. Client ID e Secret copiados para `.env`

**Scopes necessários**:
- `openid`: ID Token com sub (Google user ID)
- `profile`: Nome e foto do usuário
- `email`: Email verificado

### ID Token Validation

**CRITICAL** - Validar ID Token do Google:

```java
// NÃO fazer isso (inseguro):
String email = jwt.getClaim("email"); // ❌ Aceita qualquer JWT forjado

// FAZER isso (seguro):
DecodedJWT idToken = JWT.decode(googleIdToken);

// 1. Validar issuer
assert idToken.getIssuer().equals("https://accounts.google.com");

// 2. Validar audience (deve ser nosso client ID)
assert idToken.getAudience().contains(googleClientId);

// 3. Validar expiration
assert idToken.getExpiresAt().after(new Date());

// 4. Validar assinatura usando Google public keys
// (usar biblioteca que faz isso automaticamente, ex: google-api-client)
GoogleIdTokenVerifier verifier = new GoogleIdTokenVerifier.Builder(...)
    .setAudience(Collections.singletonList(googleClientId))
    .build();
GoogleIdToken verified = verifier.verify(googleIdToken);
```

[Source: Google OAuth Documentation]

---

## Dev Notes

### Learnings from Previous Story

**From Story 1.3 - PostgreSQL Multi-Tenancy (Status: completed)**

- **TenantContext Integration**: JwtAuthenticationFilter deve setar `TenantContext.setTenantId()` ao validar JWT, permitindo que repositories acessem automaticamente o schema correto do tenant.

- **Migration V003**: Tabela `usuarios` deve ser criada em CADA schema de tenant (não no public schema), pois usuários são tenant-specific.

- **FlywayMultiTenantConfig**: Nova migration V003__create_usuarios_table.sql será aplicada automaticamente em todos os schemas de tenant existentes no próximo startup.

- **Security Context**: Usuario.tenantId é imutável após criação. Usuário pertence a UM tenant apenas. Se precisar acessar múltiplos tenants, criar múltiplos usuários.

[Source: docs/stories/1-3-postgresql-multi-tenancy-setup.md, docs/MULTI-TENANCY.md]

### Angular Version Compatibility

- **Angular 19+**: Standalone components (sem NgModules)
- **angular-oauth2-oidc**: Versão 17.x é compatível com Angular 19
- **RxJS 7.8+**: Já instalado, usar para Observables no AuthService

### JWT Library Choice

**JJWT 0.12.x** escolhido por:
- ✅ API fluente e moderna
- ✅ Suporte a HS256, RS256, ES256
- ✅ Validação automática de expiration, issuer, audience
- ✅ Zero dependencies (auto-contained)
- ✅ Ativamente mantido

Alternativas (não escolhidas):
- ❌ `java-jwt` (Auth0): Menos features de validação automática
- ❌ `nimbus-jose-jwt`: API mais complexa

### Google OAuth Library Choice

**Spring Security OAuth2 Client** integrado:
- ✅ Configuração via `application.properties` (zero código)
- ✅ Suporte a múltiplos providers (Google, GitHub, Facebook)
- ✅ Token refresh automático
- ✅ Integração nativa com Spring Security

**NÃO usar `google-api-client`**: Biblioteca antiga, preferir Spring Security OAuth2.

### Project Structure Notes

```
backend/src/main/java/com/estoquecentral/
├── auth/
│   ├── domain/
│   │   └── Usuario.java (entity)
│   ├── application/
│   │   ├── GoogleAuthService.java
│   │   ├── UserService.java
│   │   └── JwtService.java
│   ├── adapter/
│   │   ├── in/
│   │   │   ├── AuthController.java
│   │   │   ├── security/
│   │   │   │   ├── SecurityConfig.java
│   │   │   │   └── JwtAuthenticationFilter.java
│   │   │   └── dto/
│   │   │       ├── GoogleCallbackRequest.java
│   │   │       ├── LoginResponse.java
│   │   │       └── UserDTO.java
│   │   └── out/
│   │       └── UsuarioRepository.java

frontend/src/app/features/auth/
├── login/
│   ├── login.component.ts
│   ├── login.component.html
│   └── login.component.scss
├── callback/
│   ├── auth-callback.component.ts
│   └── auth-callback.component.html
└── auth.service.ts
```

### References

- **OAuth 2.0 RFC**: https://datatracker.ietf.org/doc/html/rfc6749
- **Google OAuth Documentation**: https://developers.google.com/identity/protocols/oauth2
- **Spring Security OAuth2**: https://docs.spring.io/spring-security/reference/servlet/oauth2/login/index.html
- **JJWT Documentation**: https://github.com/jwtk/jjwt
- **angular-oauth2-oidc**: https://github.com/manfredsteyer/angular-oauth2-oidc

[Source: docs/prd/prd.md, docs/architecture/15-security-and-performance.md]

---

## Definition of Done (DoD)

- [ ] Spring Security OAuth2 Client configurado
- [ ] GoogleAuthService troca code por token e valida ID Token
- [ ] JwtService gera e valida JWT customizado com tenantId + roles
- [ ] JwtAuthenticationFilter protege endpoints e popula SecurityContext
- [ ] Tabela `usuarios` criada via migration V003 em schemas tenant
- [ ] UserService cria/atualiza usuários no tenant correto
- [ ] AuthController endpoints funcionando: `/google/callback`, `/me`, `/logout`
- [ ] Frontend LoginComponent com botão "Entrar com Google"
- [ ] Frontend AuthService gerencia JWT em localStorage
- [ ] HTTP Interceptor adiciona header Authorization em requisições
- [ ] Testes de integração passando (GoogleAuth, JWT, Filter, Controller)
- [ ] README documentado com setup Google OAuth
- [ ] Code review aprovado pelo SM
- [ ] Todas as tasks/subtasks marcadas como concluídas

---

## Dependencies & Blockers

**Dependências:**
- ✅ Story 1.1 (Project Scaffolding) - Backend Spring Boot estruturado
- ✅ Story 1.2 (Docker Containerization) - Ambiente de desenvolvimento configurado
- ✅ Story 1.3 (Multi-Tenancy) - TenantContext disponível para integração

**Blockers Conhecidos:**
- ⚠️ Google Cloud Project precisa ser criado antes de implementação
- ⚠️ OAuth Client ID/Secret precisam ser configurados em `.env`

**Next Stories:**
- Story 1.5 (RBAC) depende desta story para autenticação base
- Story 2.x (Product Catalog) depende de autenticação para proteger endpoints

---

## Change Log

- **2025-02-11**: Story drafted pelo assistente Claude Code

---

## Dev Agent Record

### Context Reference

<!-- Path(s) to story context XML will be added here by context workflow -->

### Agent Model Used

Claude 3.5 Sonnet (claude-sonnet-4-5-20250929)

### Debug Log References

### Completion Notes List

### File List

---

**Story criada por**: Claude Code Assistant
**Data**: 2025-02-11
**Baseado em**: Epic 1 (Story 1.4), docs/architecture/15-security-and-performance.md, Stories 1.1-1.3 learnings
