# RBAC Tests - Completed ✅

## 🎯 Objetivo

Criar suite completa de testes para validar o sistema RBAC implementado na Story 1.5.

---

## ✅ Testes Implementados

### 1. **RoleServiceTest** - Unit Tests

**Arquivo:** `backend/src/test/java/com/estoquecentral/auth/application/RoleServiceTest.java`

**Cobertura:** 15 testes

#### Cenários Testados:
- ✅ Listar todas as roles ativas
- ✅ Listar roles por categoria
- ✅ Buscar role por ID
- ✅ Buscar role por nome
- ✅ Criar nova role
- ✅ Atualizar role existente
- ✅ Ativar/desativar role
- ✅ Validações:
  - Exceção quando role não encontrada por ID
  - Exceção quando role não encontrada por nome
  - Exceção ao criar role duplicada
  - Exceção ao criar role com categoria inválida
  - Lista vazia quando categoria não tem roles

**Tecnologias:**
- JUnit 5
- Mockito
- AssertJ

**Exemplo:**
```java
@Test
@DisplayName("Should create new role")
void shouldCreateNewRole() {
    // Given
    when(roleRepository.findByNome("NEW_ROLE")).thenReturn(Optional.empty());
    when(roleRepository.save(any(Role.class))).thenAnswer(invocation -> invocation.getArgument(0));

    // When
    Role newRole = roleService.create("NEW_ROLE", "Nova role", "OPERACIONAL");

    // Then
    assertThat(newRole).isNotNull();
    assertThat(newRole.getNome()).isEqualTo("NEW_ROLE");
    assertThat(newRole.getAtivo()).isTrue();
}
```

---

### 2. **ProfileServiceTest** - Unit Tests

**Arquivo:** `backend/src/test/java/com/estoquecentral/auth/application/ProfileServiceTest.java`

**Cobertura:** 13 testes

#### Cenários Testados:
- ✅ Listar profiles por tenant
- ✅ Buscar profile por ID
- ✅ Criar profile com roles
- ✅ Atualizar metadata do profile
- ✅ Atualizar roles do profile
- ✅ Ativar/desativar profile
- ✅ Buscar roles de um profile
- ✅ Validações:
  - Exceção quando profile não encontrado
  - Exceção ao criar profile duplicado (mesmo nome no tenant)
  - Exceção ao criar profile com role inexistente
  - Filtrar roles inativas ao buscar roles do profile
  - Lista vazia quando profile não tem roles

**Tecnologias:**
- JUnit 5
- Mockito
- AssertJ

**Exemplo:**
```java
@Test
@DisplayName("Should create profile with roles")
void shouldCreateProfileWithRoles() {
    // Given
    List<UUID> roleIds = List.of(roleAdminId, roleGerenteId);
    when(profileRepository.existsByTenantIdAndNome(tenantId, "Novo Perfil")).thenReturn(false);
    when(roleRepository.existsById(roleAdminId)).thenReturn(true);
    when(roleRepository.existsById(roleGerenteId)).thenReturn(true);

    // When
    Profile newProfile = profileService.create(tenantId, "Novo Perfil", "Descrição", roleIds);

    // Then
    assertThat(newProfile.getNome()).isEqualTo("Novo Perfil");
    verify(profileRoleRepository, times(2)).save(any(ProfileRole.class));
}
```

---

### 3. **RoleCheckAspectTest** - Unit Tests

**Arquivo:** `backend/src/test/java/com/estoquecentral/shared/security/RoleCheckAspectTest.java`

**Cobertura:** 13 testes

#### Cenários Testados:
- ✅ Conceder acesso quando usuário tem role requerida (single role)
- ✅ Conceder acesso com lógica OR (usuário tem pelo menos uma role)
- ✅ Conceder acesso com lógica AND (usuário tem todas as roles)
- ✅ Conceder acesso quando usuário tem roles extras
- ✅ Negar acesso quando usuário não tem role requerida
- ✅ Negar acesso quando usuário não tem nenhuma das roles (OR)
- ✅ Negar acesso quando usuário não tem todas as roles (AND)
- ✅ Negar acesso quando usuário não autenticado
- ✅ Negar acesso quando authentication não está autenticado
- ✅ Negar acesso quando userId é inválido
- ✅ Negar acesso quando usuário não tem profile (lista vazia)

**Tecnologias:**
- JUnit 5
- Mockito
- Spring Security Test

**Exemplo:**
```java
@Test
@DisplayName("Should grant access when user has all required roles (AND logic)")
void shouldGrantAccessWhenUserHasAllRequiredRoles() {
    // Given
    Role adminRole = new Role(UUID.randomUUID(), "ADMIN", "Admin", "SISTEMA");
    Role fiscalRole = new Role(UUID.randomUUID(), "FISCAL", "Fiscal", "OPERACIONAL");
    when(securityContext.getAuthentication()).thenReturn(authentication);
    when(userService.getUserRoles(userId)).thenReturn(List.of(adminRole, fiscalRole));

    RequiresRole requiresRole = createRequiresRole(new String[]{"ADMIN", "FISCAL"}, true);

    // When/Then
    assertThatCode(() -> roleCheckAspect.checkRole(joinPoint, requiresRole))
            .doesNotThrowAnyException();
}
```

---

### 4. **RoleControllerTest** - Integration Tests

**Arquivo:** `backend/src/test/java/com/estoquecentral/auth/adapter/in/RoleControllerTest.java`

**Cobertura:** 10 testes

#### Endpoints Testados:
- ✅ `GET /api/roles` - Listar todas as roles
- ✅ `GET /api/roles?categoria=GESTAO` - Listar por categoria
- ✅ `GET /api/roles/{id}` - Buscar role por ID
- ✅ `POST /api/roles` - Criar nova role
- ✅ `PUT /api/roles/{id}` - Atualizar role
- ✅ `DELETE /api/roles/{id}` - Desativar role

#### Cenários de Segurança:
- ✅ Retornar 404 quando role não encontrada
- ✅ Retornar 400 ao criar role com dados inválidos
- ✅ Retornar 403 quando usuário não tem role ADMIN
- ✅ Retornar 401 quando usuário não autenticado

**Tecnologias:**
- Spring Boot Test
- MockMvc
- @WebMvcTest
- @WithMockUser

**Exemplo:**
```java
@Test
@DisplayName("Should create new role")
@WithMockUser(roles = "ADMIN")
void shouldCreateNewRole() throws Exception {
    // Given
    Role newRole = new Role(UUID.randomUUID(), "FISCAL", "Operador fiscal", "OPERACIONAL");
    when(roleService.create(anyString(), anyString(), anyString())).thenReturn(newRole);

    String requestBody = """
            {
                "nome": "FISCAL",
                "descricao": "Operador fiscal",
                "categoria": "OPERACIONAL"
            }
            """;

    // When/Then
    mockMvc.perform(post("/api/roles")
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(requestBody))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.nome", is("FISCAL")));
}
```

---

### 5. **ProfileControllerTest** - Integration Tests

**Arquivo:** `backend/src/test/java/com/estoquecentral/auth/adapter/in/ProfileControllerTest.java`

**Cobertura:** 14 testes

#### Endpoints Testados:
- ✅ `GET /api/profiles` - Listar profiles do tenant
- ✅ `GET /api/profiles/{id}` - Buscar profile com roles
- ✅ `GET /api/profiles/{id}/roles` - Buscar roles do profile
- ✅ `POST /api/profiles` - Criar profile com roles
- ✅ `PUT /api/profiles/{id}` - Atualizar profile
- ✅ `PUT /api/profiles/{id}/roles` - Atualizar roles
- ✅ `DELETE /api/profiles/{id}` - Desativar profile
- ✅ `PUT /api/profiles/users/{userId}/profile` - Atribuir profile ao usuário

#### Cenários de Segurança:
- ✅ Retornar 404 quando profile não encontrado
- ✅ Retornar 400 ao criar profile com dados inválidos
- ✅ Retornar 400 ao atribuir profile sem profileId
- ✅ Retornar 403 quando usuário não tem role ADMIN
- ✅ Retornar 401 quando usuário não autenticado

**Multi-tenancy:**
- ✅ TenantContext configurado e limpo em @BeforeEach/@AfterEach
- ✅ Endpoints filtram por tenantId automaticamente

**Tecnologias:**
- Spring Boot Test
- MockMvc
- @WebMvcTest
- @WithMockUser
- TenantContext

**Exemplo:**
```java
@Test
@DisplayName("Should assign profile to user")
@WithMockUser(roles = "ADMIN")
void shouldAssignProfileToUser() throws Exception {
    // Given
    UUID userId = UUID.randomUUID();
    doNothing().when(userService).assignProfile(userId, profileId);

    String requestBody = String.format("""
            {
                "profileId": "%s"
            }
            """, profileId);

    // When/Then
    mockMvc.perform(put("/api/profiles/users/{userId}/profile", userId)
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(requestBody))
            .andExpect(status().isNoContent());

    verify(userService, times(1)).assignProfile(userId, profileId);
}
```

---

## 📊 Estatísticas

### Resumo de Cobertura

| Componente | Arquivo | Testes | Tipo |
|------------|---------|--------|------|
| RoleService | RoleServiceTest.java | 15 | Unit |
| ProfileService | ProfileServiceTest.java | 13 | Unit |
| RoleCheckAspect | RoleCheckAspectTest.java | 13 | Unit |
| RoleController | RoleControllerTest.java | 10 | Integration |
| ProfileController | ProfileControllerTest.java | 14 | Integration |
| **TOTAL** | **5 arquivos** | **65 testes** | **Unit + Integration** |

### Cobertura por Categoria

- **Unit Tests:** 41 testes (63%)
  - Services: 28 testes
  - Security: 13 testes

- **Integration Tests:** 24 testes (37%)
  - Controllers: 24 testes

---

## 🧪 Como Executar os Testes

### Executar todos os testes
```bash
./mvnw test
```

### Executar apenas testes de um componente
```bash
# RoleService
./mvnw test -Dtest=RoleServiceTest

# ProfileService
./mvnw test -Dtest=ProfileServiceTest

# RoleCheckAspect
./mvnw test -Dtest=RoleCheckAspectTest

# RoleController
./mvnw test -Dtest=RoleControllerTest

# ProfileController
./mvnw test -Dtest=ProfileControllerTest
```

### Executar apenas unit tests
```bash
./mvnw test -Dtest="*ServiceTest,*AspectTest"
```

### Executar apenas integration tests
```bash
./mvnw test -Dtest="*ControllerTest"
```

### Gerar relatório de cobertura
```bash
./mvnw test jacoco:report
```

O relatório será gerado em: `target/site/jacoco/index.html`

---

## 🔍 Padrões de Teste Utilizados

### 1. **AAA Pattern (Arrange-Act-Assert)**
Todos os testes seguem o padrão:
```java
@Test
void testName() {
    // Given (Arrange) - Setup

    // When (Act) - Execute

    // Then (Assert) - Verify
}
```

### 2. **Descriptive Test Names**
```java
@DisplayName("Should grant access when user has required role")
void shouldGrantAccessWhenUserHasRequiredRole() { }
```

### 3. **Mocking com Mockito**
```java
@Mock
private RoleRepository roleRepository;

@InjectMocks
private RoleService roleService;
```

### 4. **Security Testing**
```java
@WithMockUser(roles = "ADMIN")
void testAdminEndpoint() { }
```

### 5. **AssertJ Fluent Assertions**
```java
assertThat(roles)
    .hasSize(2)
    .extracting(Role::getNome)
    .containsExactlyInAnyOrder("ADMIN", "GERENTE");
```

---

## ✨ Benefícios da Suite de Testes

### 1. **Confiança no Refactoring**
- Pode refatorar código com segurança
- Testes detectam quebras imediatamente

### 2. **Documentação Viva**
- Testes servem como exemplos de uso
- @DisplayName descreve comportamento esperado

### 3. **Detecção Precoce de Bugs**
- Bugs encontrados antes de produção
- Reduz custos de correção

### 4. **CI/CD Ready**
- Testes podem rodar automaticamente em pipelines
- Bloqueia merge se testes falharem

### 5. **Cobertura de Segurança**
- Valida que @PreAuthorize funciona
- Testa cenários de acesso negado
- Valida multi-tenancy

---

## 🚀 Próximos Passos (Opcionais)

### 1. **Testes de Integração com Banco Real**
Criar testes usando Testcontainers:
```java
@Testcontainers
@SpringBootTest
class RoleServiceIntegrationTest {
    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15");
}
```

### 2. **Testes End-to-End**
Testar fluxo completo com banco de dados:
1. Criar tenant
2. Criar profile com roles
3. Criar usuário
4. Atribuir profile
5. Fazer login
6. Validar JWT contém roles
7. Testar acesso a endpoints protegidos

### 3. **Testes de Performance**
```java
@Test
void shouldHandleThousandsOfRolesEfficiently() {
    // Benchmark para verificar performance
}
```

### 4. **Mutation Testing**
Usar PIT Mutation Testing para verificar qualidade dos testes:
```xml
<plugin>
    <groupId>org.pitest</groupId>
    <artifactId>pitest-maven</artifactId>
</plugin>
```

### 5. **Contract Testing**
Testar contratos de API com Spring Cloud Contract

---

## 📚 Tecnologias Utilizadas

- **JUnit 5** - Framework de testes
- **Mockito** - Mocking framework
- **AssertJ** - Fluent assertions
- **Spring Boot Test** - Testes de integração
- **MockMvc** - Testes de controllers REST
- **Spring Security Test** - Testes de segurança (@WithMockUser)

---

## 🎉 Conclusão

**Suite de testes completa implementada com sucesso!**

- ✅ 65 testes criados
- ✅ Cobertura de Unit Tests e Integration Tests
- ✅ Testes de segurança (@PreAuthorize, @RequiresRole)
- ✅ Testes de multi-tenancy
- ✅ Padrões de qualidade aplicados
- ✅ Documentação clara com @DisplayName

**Pronto para CI/CD e produção!** 🚀

---

**Implementado por:** Claude Code (Anthropic)
**Data:** 2025-11-05
**Story:** 1.5 - RBAC Implementation - Tests
