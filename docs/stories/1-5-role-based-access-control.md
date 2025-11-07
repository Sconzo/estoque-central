# Story 1.5: Role-Based Access Control (RBAC)

**Epic**: 1 - Foundation & Core Infrastructure
**Story ID**: 1.5
**Status**: drafted
**Created**: 2025-11-04
**Updated**: 2025-11-04

---

## User Story

Como **administrador de sistema**,
Eu quero **sistema de permissões com Roles, Profiles e Users**,
Para que **eu possa controlar quem acessa cada módulo do sistema**.

---

## Context & Business Value

Esta story implementa um sistema de controle de acesso baseado em roles (RBAC) usando o modelo **Roles → Profiles → Users**. Cada usuário tem um Profile que agrupa múltiplas Roles, permitindo controle granular de permissões sem duplicar configurações.

**Valor de Negócio:**
- **Segurança**: Acesso granular a módulos críticos (Estoque, Compras, Vendas, PDV)
- **Flexibilidade**: Perfis customizáveis por tenant (ex: "Gerente Loja" pode ter roles diferentes em cada tenant)
- **Auditoria**: Rastreamento completo de quem acessa o quê
- **Conformidade**: Atende requisitos LGPD e SOC 2 para controle de acesso

**Contexto Arquitetural:**
- **Spring Security Method Security**: Anotações `@PreAuthorize` protegem endpoints
- **Custom Role Checker**: Middleware valida roles em cada requisição
- **Tenant-Specific Roles**: Roles são definidas globalmente, mas atribuídas por tenant via Profiles
- **Hierarquia**: Role → Profile → Usuario (Many-to-Many → One-to-Many)

---

## Acceptance Criteria

### AC1: Tabelas RBAC Criadas
- [ ] Migration `V004__create_rbac_tables.sql` cria tabelas no schema **public**:
  - `roles` (id, nome, descricao, categoria)
  - `profiles` (id, tenant_id, nome, descricao)
  - `profile_roles` (profile_id, role_id)
- [ ] Migration `V004__update_usuarios_add_profile.sql` atualiza tabela `usuarios` nos schemas **tenant**:
  - Adiciona coluna `profile_id UUID` referenciando `public.profiles.id`
  - Índice em `profile_id`
- [ ] Roles padrão inseridas: `ADMIN`, `GERENTE`, `VENDEDOR`, `ESTOQUISTA`, `OPERADOR_PDV`

**Validação**:
```sql
SELECT * FROM public.roles;
-- Retorna 5+ roles padrão
```

### AC2: Endpoints de Gerenciamento de Roles
- [ ] Endpoint `GET /api/roles` lista todas as roles disponíveis (filtro opcional por categoria)
- [ ] Endpoint `POST /api/roles` cria nova role customizada (apenas ADMIN)
- [ ] Endpoint `PUT /api/roles/{id}` atualiza role (nome, descrição)
- [ ] Endpoint `DELETE /api/roles/{id}` desativa role (soft delete)

**Validação**:
```bash
curl http://localhost:8080/api/roles \
  -H "Authorization: Bearer <admin-jwt>"
# Retorna HTTP 200 com array de roles
```

### AC3: Endpoints de Gerenciamento de Profiles
- [ ] Endpoint `GET /api/profiles` lista profiles do tenant atual
- [ ] Endpoint `POST /api/profiles` cria novo profile e associa roles (ex: {"nome": "Gerente Loja", "roleIds": [1,2,3]})
- [ ] Endpoint `PUT /api/profiles/{id}` atualiza profile (nome, descrição, roles)
- [ ] Endpoint `DELETE /api/profiles/{id}` desativa profile (soft delete)
- [ ] Endpoint `GET /api/profiles/{id}/roles` retorna todas as roles do profile

**Validação**:
```bash
curl -X POST http://localhost:8080/api/profiles \
  -H "Authorization: Bearer <admin-jwt>" \
  -H "Content-Type: application/json" \
  -d '{"nome": "Gerente", "roleIds": ["ADMIN", "GERENTE", "VENDEDOR"]}'
# Retorna HTTP 201 com profile criado
```

### AC4: Atribuição de Profile a Usuário
- [ ] Endpoint `PUT /api/users/{id}/profile` atribui profile a usuário
- [ ] Endpoint `GET /api/users/{id}` retorna usuário com profile e roles expandidas
- [ ] Usuário sem profile não pode acessar endpoints protegidos (retorna HTTP 403)
- [ ] Mudança de profile invalida JWT (forçar novo login ou refresh token)

**Validação**:
```bash
curl -X PUT http://localhost:8080/api/users/{userId}/profile \
  -H "Authorization: Bearer <admin-jwt>" \
  -H "Content-Type: application/json" \
  -d '{"profileId": "uuid-do-profile"}'
# Retorna HTTP 200
```

### AC5: Anotação @RequiresRole Funciona
- [ ] Annotation `@RequiresRole("ESTOQUE")` criada em `com.estoquecentral.shared.security`
- [ ] Aspect `RoleCheckAspect` intercepta métodos anotados e valida roles do usuário
- [ ] Se usuário não tem role, retorna HTTP 403 com mensagem clara
- [ ] Controllers usam `@RequiresRole` em endpoints:
  - `@RequiresRole("ESTOQUE")` em `/api/estoque/**`
  - `@RequiresRole("COMPRAS")` em `/api/compras/**`
  - `@RequiresRole("ADMIN")` em `/api/roles/**`, `/api/profiles/**`

**Validação**:
```bash
# Usuário com role VENDEDOR tenta acessar /api/estoque
curl http://localhost:8080/api/estoque/movimentacoes \
  -H "Authorization: Bearer <vendedor-jwt>"
# Retorna HTTP 403 Forbidden
```

### AC6: Middleware Valida Roles em Requisições
- [ ] `RoleAuthorizationFilter` criado (extends `OncePerRequestFilter`)
- [ ] Filter extrai roles do JWT e popula `SecurityContext` com `GrantedAuthority`
- [ ] Filter registrado na chain ANTES de `JwtAuthenticationFilter`
- [ ] Spring Security `@PreAuthorize("hasRole('ADMIN')")` funciona corretamente

**Validação**: Teste de integração valida que usuário com profile "Caixa" (role "OPERADOR_PDV") não acessa `/api/compras`

### AC7: Frontend - Guard de Roles
- [ ] Guard Angular `RoleGuard` criado em `app/core/guards`
- [ ] Guard bloqueia navegação para rotas se usuário não tem role:
  ```typescript
  {
    path: 'estoque',
    component: EstoqueComponent,
    canActivate: [RoleGuard],
    data: { requiredRole: 'ESTOQUE' }
  }
  ```
- [ ] Guard redireciona para `/403` se acesso negado

**Validação**: Navegar para `/estoque` sem role redireciona para `/403`

### AC8: Frontend - Diretiva *hasRole
- [ ] Diretiva `HasRoleDirective` criada
- [ ] Diretiva esconde elementos HTML se usuário não tem role:
  ```html
  <button *hasRole="'ADMIN'" (click)="deletar()">
    Deletar
  </button>
  ```
- [ ] Diretiva suporta múltiplas roles (OR logic): `*hasRole="['ADMIN', 'GERENTE']"`

**Validação**: Usuário sem role ADMIN não vê botão "Deletar" no UI

---

## Tasks & Subtasks

### Task 1: Criar Tabelas RBAC (Migrations)
**AC: #1**
- [ ] Criar `V004__create_rbac_tables.sql` no schema **public**:
  ```sql
  CREATE TABLE IF NOT EXISTS roles (
      id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
      nome VARCHAR(100) UNIQUE NOT NULL,
      descricao TEXT,
      categoria VARCHAR(50) CHECK (categoria IN ('GESTAO', 'OPERACIONAL', 'SISTEMA')),
      ativo BOOLEAN DEFAULT true NOT NULL,
      data_criacao TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL
  );

  CREATE TABLE IF NOT EXISTS profiles (
      id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
      tenant_id UUID NOT NULL REFERENCES public.tenants(id) ON DELETE CASCADE,
      nome VARCHAR(100) NOT NULL,
      descricao TEXT,
      ativo BOOLEAN DEFAULT true NOT NULL,
      data_criacao TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
      UNIQUE (tenant_id, nome)
  );

  CREATE TABLE IF NOT EXISTS profile_roles (
      profile_id UUID NOT NULL REFERENCES profiles(id) ON DELETE CASCADE,
      role_id UUID NOT NULL REFERENCES roles(id) ON DELETE CASCADE,
      PRIMARY KEY (profile_id, role_id)
  );

  CREATE INDEX idx_profiles_tenant_id ON profiles(tenant_id);
  CREATE INDEX idx_profile_roles_profile_id ON profile_roles(profile_id);

  -- Inserir roles padrão
  INSERT INTO roles (nome, descricao, categoria) VALUES
    ('ADMIN', 'Administrador com acesso total', 'SISTEMA'),
    ('GERENTE', 'Gerente com acesso a relatórios e configurações', 'GESTAO'),
    ('VENDEDOR', 'Vendedor com acesso a vendas B2B/B2C', 'OPERACIONAL'),
    ('ESTOQUISTA', 'Estoquista com acesso a estoque e compras', 'OPERACIONAL'),
    ('OPERADOR_PDV', 'Operador de PDV (caixa)', 'OPERACIONAL');
  ```

- [ ] Criar `V005__update_usuarios_add_profile.sql` para schemas **tenant**:
  ```sql
  ALTER TABLE usuarios ADD COLUMN profile_id UUID REFERENCES public.profiles(id);
  CREATE INDEX idx_usuarios_profile_id ON usuarios(profile_id);

  -- Remover coluna antiga 'role' (VARCHAR) - será substituída por profile
  ALTER TABLE usuarios DROP COLUMN role;
  ```

### Task 2: Criar Entities de Domain
**AC: #1, #2, #3**
- [ ] Criar `com.estoquecentral.auth.domain.Role`:
  ```java
  @Table("public.roles")
  public class Role {
      @Id private UUID id;
      private String nome;
      private String descricao;
      private String categoria; // GESTAO, OPERACIONAL, SISTEMA
      private Boolean ativo;
      private Instant dataCriacao;
  }
  ```

- [ ] Criar `com.estoquecentral.auth.domain.Profile`:
  ```java
  @Table("public.profiles")
  public class Profile {
      @Id private UUID id;
      private UUID tenantId;
      private String nome;
      private String descricao;
      private Boolean ativo;
      private Instant dataCriacao;
  }
  ```

- [ ] Criar `com.estoquecentral.auth.domain.ProfileRole` (join table):
  ```java
  @Table("public.profile_roles")
  public class ProfileRole {
      private UUID profileId;
      private UUID roleId;
  }
  ```

- [ ] Atualizar `Usuario.java`:
  ```java
  @Table("usuarios")
  public class Usuario {
      // ... campos existentes
      private UUID profileId; // FK para public.profiles

      // Remover campo: private String role;
  }
  ```

### Task 3: Criar Repositories
**AC: #2, #3**
- [ ] Criar `RoleRepository extends CrudRepository<Role, UUID>`:
  - `List<Role> findByAtivoTrue()`
  - `Optional<Role> findByNome(String nome)`
  - `List<Role> findByCategoria(String categoria)`

- [ ] Criar `ProfileRepository extends CrudRepository<Profile, UUID>`:
  - `List<Profile> findByTenantIdAndAtivoTrue(UUID tenantId)`
  - `Optional<Profile> findByTenantIdAndNome(UUID tenantId, String nome)`

- [ ] Criar `ProfileRoleRepository extends CrudRepository<ProfileRole, UUID>`:
  - `List<ProfileRole> findByProfileId(UUID profileId)`
  - `void deleteByProfileId(UUID profileId)`

### Task 4: Criar Services
**AC: #2, #3, #4**
- [ ] Criar `RoleService`:
  - `List<Role> listAll()`
  - `List<Role> listByCategory(String category)`
  - `Role create(RoleCreateRequest request)` (apenas ADMIN)
  - `Role update(UUID id, RoleUpdateRequest request)`
  - `void deactivate(UUID id)` (soft delete)

- [ ] Criar `ProfileService`:
  - `List<Profile> listByTenant(UUID tenantId)`
  - `Profile create(UUID tenantId, ProfileCreateRequest request)` (nome + roleIds)
  - `Profile update(UUID id, ProfileUpdateRequest request)`
  - `void deactivate(UUID id)`
  - `List<Role> getRolesByProfile(UUID profileId)` (join profile_roles + roles)
  - `void assignRolesToProfile(UUID profileId, List<UUID> roleIds)` (substitui roles existentes)

- [ ] Atualizar `UserService`:
  - `void assignProfile(UUID userId, UUID profileId)`
  - `List<Role> getUserRoles(UUID userId)` (via profile)

### Task 5: Criar Controllers
**AC: #2, #3, #4**
- [ ] Criar `RoleController`:
  ```java
  @RestController
  @RequestMapping("/api/roles")
  @RequiresRole("ADMIN")
  class RoleController {
      @GetMapping
      ResponseEntity<List<RoleDTO>> listar(@RequestParam(required = false) String categoria);

      @PostMapping
      ResponseEntity<RoleDTO> criar(@RequestBody RoleCreateRequest request);

      @PutMapping("/{id}")
      ResponseEntity<RoleDTO> atualizar(@PathVariable UUID id, @RequestBody RoleUpdateRequest request);

      @DeleteMapping("/{id}")
      ResponseEntity<Void> desativar(@PathVariable UUID id);
  }
  ```

- [ ] Criar `ProfileController`:
  ```java
  @RestController
  @RequestMapping("/api/profiles")
  @RequiresRole("ADMIN") // Apenas ADMIN pode gerenciar profiles
  class ProfileController {
      @GetMapping
      ResponseEntity<List<ProfileDTO>> listar();

      @PostMapping
      ResponseEntity<ProfileDTO> criar(@RequestBody ProfileCreateRequest request);

      @PutMapping("/{id}")
      ResponseEntity<ProfileDTO> atualizar(@PathVariable UUID id, @RequestBody ProfileUpdateRequest request);

      @DeleteMapping("/{id}")
      ResponseEntity<Void> desativar(@PathVariable UUID id);

      @GetMapping("/{id}/roles")
      ResponseEntity<List<RoleDTO>> listarRoles(@PathVariable UUID id);
  }
  ```

- [ ] Atualizar `AuthController` / criar `UserController`:
  ```java
  @PutMapping("/users/{id}/profile")
  @RequiresRole("ADMIN")
  ResponseEntity<Void> assignProfile(
      @PathVariable UUID id,
      @RequestBody AssignProfileRequest request
  ) {
      userService.assignProfile(id, request.getProfileId());
      return ResponseEntity.ok().build();
  }

  @GetMapping("/users/{id}")
  ResponseEntity<UserDTO> getUser(@PathVariable UUID id) {
      // Retorna user com profile e roles expandidas
  }
  ```

### Task 6: Implementar @RequiresRole Annotation
**AC: #5**
- [ ] Criar anotação:
  ```java
  package com.estoquecentral.shared.security;

  @Target({ElementType.METHOD, ElementType.TYPE})
  @Retention(RetentionPolicy.RUNTIME)
  public @interface RequiresRole {
      String[] value(); // Array de roles (OR logic)
      boolean requireAll() default false; // Se true, AND logic
  }
  ```

- [ ] Criar Aspect:
  ```java
  @Aspect
  @Component
  class RoleCheckAspect {
      @Autowired JwtService jwtService;
      @Autowired UserService userService;

      @Before("@annotation(requiresRole)")
      public void checkRole(JoinPoint joinPoint, RequiresRole requiresRole) {
          Authentication auth = SecurityContextHolder.getContext().getAuthentication();
          if (auth == null || !auth.isAuthenticated()) {
              throw new AccessDeniedException("User not authenticated");
          }

          UUID userId = UUID.fromString(auth.getName());
          List<Role> userRoles = userService.getUserRoles(userId);
          List<String> roleNames = userRoles.stream().map(Role::getNome).toList();

          String[] requiredRoles = requiresRole.value();
          boolean hasAccess;

          if (requiresRole.requireAll()) {
              // AND logic: user must have ALL roles
              hasAccess = roleNames.containsAll(Arrays.asList(requiredRoles));
          } else {
              // OR logic: user must have AT LEAST ONE role
              hasAccess = Arrays.stream(requiredRoles).anyMatch(roleNames::contains);
          }

          if (!hasAccess) {
              throw new AccessDeniedException(
                  "User does not have required role(s): " + Arrays.toString(requiredRoles)
              );
          }
      }
  }
  ```

### Task 7: Criar RoleAuthorizationFilter (Middleware)
**AC: #6**
- [ ] Criar filter:
  ```java
  @Component
  class RoleAuthorizationFilter extends OncePerRequestFilter {
      @Autowired JwtService jwtService;
      @Autowired UserService userService;

      @Override
      protected void doFilterInternal(HttpServletRequest request, ...) {
          Authentication auth = SecurityContextHolder.getContext().getAuthentication();

          if (auth != null && auth.isAuthenticated()) {
              UUID userId = UUID.fromString(auth.getName());
              List<Role> roles = userService.getUserRoles(userId);

              // Converter roles para GrantedAuthority
              List<GrantedAuthority> authorities = roles.stream()
                  .map(role -> new SimpleGrantedAuthority("ROLE_" + role.getNome()))
                  .collect(Collectors.toList());

              // Atualizar Authentication com authorities
              UsernamePasswordAuthenticationToken newAuth =
                  new UsernamePasswordAuthenticationToken(
                      auth.getPrincipal(),
                      auth.getCredentials(),
                      authorities
                  );

              SecurityContextHolder.getContext().setAuthentication(newAuth);
          }

          filterChain.doFilter(request, response);
      }
  }
  ```

- [ ] Registrar filter em `SecurityConfig`:
  ```java
  http.addFilterAfter(roleAuthorizationFilter, JwtAuthenticationFilter.class)
  ```

### Task 8: Criar Frontend RoleGuard
**AC: #7**
- [ ] Criar guard:
  ```bash
  ng g guard core/guards/role
  ```

- [ ] Implementar:
  ```typescript
  @Injectable({
    providedIn: 'root'
  })
  export class RoleGuard implements CanActivate {
    constructor(
      private authService: AuthService,
      private router: Router
    ) {}

    canActivate(route: ActivatedRouteSnapshot): boolean {
      const requiredRole = route.data['requiredRole'];
      const userRoles = this.authService.getUserRoles();

      if (userRoles.includes(requiredRole) || userRoles.includes('ADMIN')) {
        return true;
      }

      this.router.navigate(['/403']);
      return false;
    }
  }
  ```

- [ ] Configurar rotas:
  ```typescript
  {
    path: 'estoque',
    component: EstoqueComponent,
    canActivate: [RoleGuard],
    data: { requiredRole: 'ESTOQUE' }
  }
  ```

### Task 9: Criar Frontend *hasRole Directive
**AC: #8**
- [ ] Criar diretiva:
  ```bash
  ng g directive shared/directives/has-role
  ```

- [ ] Implementar:
  ```typescript
  @Directive({
    selector: '[hasRole]',
    standalone: true
  })
  export class HasRoleDirective implements OnInit {
    @Input() hasRole!: string | string[];

    constructor(
      private templateRef: TemplateRef<any>,
      private viewContainer: ViewContainerRef,
      private authService: AuthService
    ) {}

    ngOnInit() {
      const requiredRoles = Array.isArray(this.hasRole) ? this.hasRole : [this.hasRole];
      const userRoles = this.authService.getUserRoles();

      const hasAccess = requiredRoles.some(role =>
        userRoles.includes(role) || userRoles.includes('ADMIN')
      );

      if (hasAccess) {
        this.viewContainer.createEmbeddedView(this.templateRef);
      } else {
        this.viewContainer.clear();
      }
    }
  }
  ```

- [ ] Uso no template:
  ```html
  <button *hasRole="'ADMIN'" (click)="deletar()">
    Deletar
  </button>

  <div *hasRole="['ADMIN', 'GERENTE']">
    Conteúdo restrito
  </div>
  ```

### Task 10: Atualizar JWT para incluir Roles
**AC: #4, #6**
- [ ] Atualizar `JwtService.generateToken()`:
  ```java
  public String generateToken(Usuario usuario) {
      List<Role> roles = userService.getUserRoles(usuario.getId());
      List<String> roleNames = roles.stream().map(Role::getNome).toList();

      return Jwts.builder()
          .subject(usuario.getId().toString())
          .claim("tenantId", usuario.getTenantId().toString())
          .claim("email", usuario.getEmail())
          .claim("roles", roleNames) // Array de role names
          .claim("profileId", usuario.getProfileId().toString())
          .issuedAt(new Date())
          .expiration(new Date(System.currentTimeMillis() + 86400000))
          .signWith(signingKey, Jwts.SIG.HS256)
          .compact();
  }
  ```

### Task 11: Criar Testes de Integração
**AC: #5, #6**
- [ ] Teste: `RoleCheckAspectTest`:
  - Validar que método com `@RequiresRole("ADMIN")` bloqueia usuário sem role
  - Validar que usuário com role ADMIN consegue acessar
  - Validar OR logic (user com GERENTE acessa endpoint com `@RequiresRole({"ADMIN", "GERENTE"})`)
  - Validar AND logic com `requireAll=true`

- [ ] Teste: `ProfileServiceTest`:
  - Validar criação de profile com múltiplas roles
  - Validar atualização de roles de um profile
  - Validar que profiles de tenant A não são visíveis para tenant B

- [ ] Teste: `UserServiceTest`:
  - Validar atribuição de profile a usuário
  - Validar que `getUserRoles()` retorna roles do profile correto

---

## Technical Implementation Notes

### RBAC Model: Roles → Profiles → Users

```
┌────────┐
│  Role  │ (Global, defined in public schema)
│ ADMIN  │
│GERENTE │
│VENDEDOR│
└───┬────┘
    │ Many-to-Many
    ▼
┌────────────┐
│  Profile   │ (Tenant-specific, in public schema with tenant_id)
│"Gerente Loja"│
└──────┬─────┘
       │ One-to-Many
       ▼
┌──────────┐
│ Usuario  │ (Tenant-specific, in tenant schema)
└──────────┘
```

**Example**:
- **Role**: `ESTOQUE` (permissão para acessar módulo de estoque)
- **Profile**: "Gerente Loja" (tem roles: `ESTOQUE`, `VENDAS`, `RELATORIOS`)
- **Usuario**: João Silva (profile: "Gerente Loja")

### Why Profiles?

**Sem Profiles** (direto User → Roles):
- ❌ Difícil manter: Adicionar nova role exige atualizar TODOS os usuários
- ❌ Inconsistente: Usuários com mesmo cargo podem ter roles diferentes por erro
- ❌ Não escalável: 100 usuários "Gerente" = 100 registros duplicados

**Com Profiles**:
- ✅ Fácil manter: Atualizar Profile "Gerente" aplica a TODOS os usuários com esse profile
- ✅ Consistente: Todos "Gerentes" têm exatamente as mesmas roles
- ✅ Escalável: 1 Profile "Gerente" serve 100+ usuários

### Spring Security Integration

O sistema usa DUAS formas de autorização:

1. **@PreAuthorize (Spring nativo)**:
   ```java
   @PreAuthorize("hasRole('ADMIN')")
   public void deletar() { }
   ```
   - Usa `GrantedAuthority` do `SecurityContext`
   - Populado por `RoleAuthorizationFilter`

2. **@RequiresRole (customizado)**:
   ```java
   @RequiresRole("ADMIN")
   public void deletar() { }
   ```
   - Usa Aspect (AOP)
   - Mais flexível: suporta OR/AND logic

**Recomendação**: Usar `@RequiresRole` para consistência no código.

### JWT Claims Structure

```json
{
  "sub": "user-uuid",
  "tenantId": "tenant-uuid",
  "email": "user@example.com",
  "profileId": "profile-uuid",
  "roles": ["ADMIN", "GERENTE"], // Array de role names
  "iat": 1699900000,
  "exp": 1699986400
}
```

### Security Considerations

1. **Princípio do Menor Privilégio**: Usuários começam sem profile (acesso zero)
2. **Separação de Duties**: ADMIN pode gerenciar profiles, mas GERENTE não pode elevar próprios privilégios
3. **Audit Trail**: Mudanças de profile/roles devem ser logadas
4. **Token Invalidation**: Mudança de profile invalida JWT (forçar re-login ou refresh token)

---

## Definition of Done (DoD)

- [ ] Tabelas `roles`, `profiles`, `profile_roles` criadas no schema public
- [ ] Tabela `usuarios` atualizada com coluna `profile_id`
- [ ] Roles padrão (ADMIN, GERENTE, VENDEDOR, ESTOQUISTA, OPERADOR_PDV) inseridas
- [ ] Endpoints CRUD para Roles funcionando
- [ ] Endpoints CRUD para Profiles funcionando
- [ ] Endpoint de atribuição de profile a usuário funcionando
- [ ] Anotação `@RequiresRole` implementada e funcionando
- [ ] `RoleAuthorizationFilter` populando `SecurityContext` com roles
- [ ] Frontend `RoleGuard` bloqueando rotas
- [ ] Frontend `*hasRole` directive escondendo elementos
- [ ] JWT contém array `roles`
- [ ] Testes de integração passando (RoleCheckAspect, ProfileService, UserService)
- [ ] Code review aprovado

---

## Dependencies & Blockers

**Dependências:**
- ✅ Story 1.4 (Google OAuth Authentication) - Usuários e JWT implementados

**Blockers Conhecidos:**
- Nenhum

**Next Stories:**
- Story 2.1 (Product Catalog) - Usará `@RequiresRole("ADMIN")` para proteger criação de produtos

---

## Change Log

- **2025-11-04**: Story drafted

---

## Dev Agent Record

### Agent Model Used

Claude 3.5 Sonnet (claude-sonnet-4-5-20250929)

---

**Story criada por**: Claude Code Assistant
**Data**: 2025-11-04
**Baseado em**: Epic 1 (Story 1.5), docs/architecture/15-security-and-performance.md, Story 1.4 learnings
