# Story 4.1: Customer Management (Gestão de Clientes PF e PJ)

**Epic**: 4 - Sales Channels - PDV & B2B
**Story ID**: 4.1
**Status**: approved
**Created**: 2025-11-21
**Updated**: 2025-11-21

---

## User Story

Como **vendedor**,
Eu quero **CRUD completo de clientes (Pessoa Física e Jurídica) com dados fiscais necessários para emissão de NFCe/NFe**,
Para que **eu possa registrar vendas com dados corretos para compliance fiscal**.

---

## Context & Business Value

Esta story implementa o cadastro completo de clientes (customers) com todos os dados necessários para emissão de documentos fiscais (NFCe, NFe) e gestão de relacionamento comercial. Suporta tanto Pessoa Física quanto Jurídica.

**Valor de Negócio:**
- **Compliance Fiscal**: Armazena dados obrigatórios para emissão de NFCe/NFe
- **CRM Básico**: Histórico de compras e relacionamento com cliente
- **B2B**: Dados completos de PJ para Ordens de Venda
- **Agilidade PDV**: Busca rápida por CPF/CNPJ no PDV

**Contexto Arquitetural:**
- **Dual Type**: Suporta PF (CPF) e PJ (CNPJ) na mesma tabela
- **Soft Delete**: Clientes inativos mantêm histórico de vendas
- **Indexação**: CPF/CNPJ indexados para busca rápida (< 500ms NFR3)
- **Criptografia**: CPF/CNPJ criptografados em repouso (NFR14 - AES-256)

---

## Acceptance Criteria

### AC1: Tabela customers Criada
- [ ] Migration cria tabela `customers` no schema tenant com colunas:
  - `id` (UUID, PK)
  - `tenant_id` (UUID, FK para tenants)
  - `tipo_pessoa` (VARCHAR(2), NOT NULL) - 'PF' ou 'PJ'
  - `cpf` (VARCHAR(14), NULLABLE, ENCRYPTED) - formato: 000.000.000-00
  - `cnpj` (VARCHAR(18), NULLABLE, ENCRYPTED) - formato: 00.000.000/0000-00
  - `nome` (VARCHAR(200), NOT NULL) - nome completo (PF) ou razão social (PJ)
  - `nome_fantasia` (VARCHAR(200), NULLABLE) - apenas PJ
  - `inscricao_estadual` (VARCHAR(20), NULLABLE) - apenas PJ
  - `inscricao_municipal` (VARCHAR(20), NULLABLE) - apenas PJ
  - `email` (VARCHAR(100), ENCRYPTED)
  - `telefone` (VARCHAR(20))
  - `celular` (VARCHAR(20))
  - `endereco_logradouro` (VARCHAR(200))
  - `endereco_numero` (VARCHAR(10))
  - `endereco_complemento` (VARCHAR(100))
  - `endereco_bairro` (VARCHAR(100))
  - `endereco_cidade` (VARCHAR(100))
  - `endereco_uf` (CHAR(2))
  - `endereco_cep` (VARCHAR(10))
  - `observacoes` (TEXT)
  - `ativo` (BOOLEAN, DEFAULT true)
  - `data_criacao` (TIMESTAMP)
  - `data_atualizacao` (TIMESTAMP)
- [ ] Índices criados: `idx_customers_tenant_id`, `idx_customers_cpf`, `idx_customers_cnpj`, `idx_customers_nome`
- [ ] Constraint: `UNIQUE (tenant_id, cpf)` para CPF único por tenant
- [ ] Constraint: `UNIQUE (tenant_id, cnpj)` para CNPJ único por tenant
- [ ] Constraint: `CHECK (tipo_pessoa IN ('PF', 'PJ'))`
- [ ] Constraint: `CHECK ((tipo_pessoa = 'PJ' AND cnpj IS NOT NULL) OR (tipo_pessoa = 'PF' AND cpf IS NOT NULL))`

### AC2: Criptografia de Dados Sensíveis (NFR14)
- [ ] CPF, CNPJ e email criptografados em repouso usando AES-256
- [ ] Implementação com JPA AttributeConverter ou Spring Data JDBC custom converters
- [ ] Chave de criptografia armazenada em Azure Key Vault
- [ ] Decriptação transparente ao ler dados

### AC3: Endpoints CRUD de Customers
- [ ] `POST /api/customers` cria cliente com payload completo
- [ ] `GET /api/customers` retorna lista paginada com filtros:
  - `nome` (busca em nome ou nome_fantasia)
  - `cpf` (busca exata)
  - `cnpj` (busca exata)
  - `tipo_pessoa` (PF/PJ/all)
  - `ativo` (true/false/all, default: true)
- [ ] `GET /api/customers/{id}` retorna detalhes de um cliente
- [ ] `GET /api/customers/search?q={query}` busca rápida por nome, CPF ou CNPJ (< 500ms NFR3)
- [ ] `PUT /api/customers/{id}` edita cliente
- [ ] `DELETE /api/customers/{id}` marca como inativo (soft delete)
- [ ] Paginação: default 20 por página, max 100

### AC4: Validações de Dados Fiscais
- [ ] Validação de formato CPF com dígitos verificadores
- [ ] Validação de formato CNPJ com dígitos verificadores
- [ ] CPF único por tenant (retorna HTTP 409 se duplicado)
- [ ] CNPJ único por tenant (retorna HTTP 409 se duplicado)
- [ ] Validação de CEP (formato 00000-000)
- [ ] Validação de UF (lista fixa de estados brasileiros)
- [ ] Email formato válido (regex)
- [ ] Telefone/celular formato brasileiro (DDD + número)

### AC5: Cliente "Consumidor Final" Padrão
- [ ] Migration cria cliente padrão "Consumidor Final" para vendas no PDV sem identificação
- [ ] CPF: 000.000.000-00 (reservado)
- [ ] Nome: "Consumidor Final"
- [ ] Cliente não pode ser deletado ou editado (flag `is_default_consumer = true`)
- [ ] Usado automaticamente em vendas PDV sem cliente informado

### AC6: Frontend - Lista de Clientes
- [ ] Component Angular `CustomerListComponent` criado
- [ ] Tabela com colunas: CPF/CNPJ, Nome/Razão Social, Nome Fantasia (PJ), Cidade/UF, Telefone, Status, Ações
- [ ] Filtros: busca por nome, CPF/CNPJ, tipo pessoa, status
- [ ] Paginação com Angular Material Paginator
- [ ] Botão "Novo Cliente" abre modal de cadastro
- [ ] Ações inline: Editar (ícone lápis), Inativar (ícone lixeira com confirmação)
- [ ] Badge visual para status: verde (Ativo), cinza (Inativo)
- [ ] Badge visual para tipo: azul (PF), roxo (PJ)

### AC7: Frontend - Formulário de Cadastro/Edição
- [ ] Component `CustomerFormComponent` com formulário reativo
- [ ] Radio button: Pessoa Física / Pessoa Jurídica (alterna campos CPF/CNPJ)
- [ ] Campos PF: CPF* (máscara), Nome Completo*
- [ ] Campos PJ: CNPJ* (máscara), Razão Social*, Nome Fantasia, IE, IM
- [ ] Campos contato: Email (validação), Telefone (máscara), Celular (máscara)
- [ ] Campos endereço: CEP (máscara + busca ViaCEP), Logradouro, Número, Complemento, Bairro, Cidade, UF (dropdown)
- [ ] Campo observações (textarea)
- [ ] Validação inline com mensagens de erro claras
- [ ] Botões: Salvar (desabilitado se form inválido), Cancelar
- [ ] Busca automática de endereço por CEP usando API ViaCEP

### AC8: Frontend - Busca Rápida (para PDV e Ordem de Venda)
- [ ] Component `CustomerQuickSearchComponent` reutilizável
- [ ] Input com autocomplete (debounce 300ms)
- [ ] Busca por: nome, CPF ou CNPJ (parcial)
- [ ] Resultados exibem: CPF/CNPJ, Nome, Cidade/UF
- [ ] Seleção retorna objeto cliente completo
- [ ] Performance: resultados em < 500ms (NFR3)
- [ ] Botão "Cadastro Rápido" abre modal simplificado (apenas dados essenciais)

### AC9: Frontend - Cadastro Rápido (para PDV)
- [ ] Modal `CustomerQuickCreateComponent` com campos mínimos:
  - Tipo Pessoa* (radio PF/PJ)
  - CPF ou CNPJ* (com validação)
  - Nome/Razão Social*
  - Telefone (opcional)
- [ ] Validação: CPF/CNPJ único
- [ ] Ao salvar, retorna cliente criado para uso imediato
- [ ] UX otimizada para velocidade (max 3 campos obrigatórios)

---

## Tasks & Subtasks

### Task 1: Criar Migration de customers
- [ ] Criar migration `V047__create_customers_table.sql`
- [ ] Definir estrutura completa com campos fiscais e endereço
- [ ] Criar índices e constraints (CPF/CNPJ único, check tipo_pessoa)
- [ ] Inserir cliente padrão "Consumidor Final"
- [ ] Testar migration: `mvn flyway:migrate`

### Task 2: Implementar Criptografia AES-256 (NFR14)
- [ ] Criar `CryptoService` com métodos `encrypt()` e `decrypt()`
- [ ] Configurar chave de criptografia via Azure Key Vault (environment variable em dev)
- [ ] Criar `@Converter` JPA para CPF, CNPJ, Email
- [ ] Testar criptografia/decriptografia roundtrip

### Task 3: Criar Entidade e Repository
- [ ] Criar `Customer.java` em `sales.domain`
- [ ] Aplicar `@Convert` nos campos sensíveis (CPF, CNPJ, email)
- [ ] Criar `CustomerRepository` extends `CrudRepository`
- [ ] Método `findByTenantIdAndAtivo()`
- [ ] Método customizado para busca por nome, CPF ou CNPJ
- [ ] Método `searchQuick(query)` otimizado para autocomplete

### Task 4: Implementar CustomerService
- [ ] Criar `CustomerService` com métodos CRUD
- [ ] Validação de CPF/CNPJ (algoritmo dígitos verificadores)
- [ ] Método `softDelete()` marca como inativo
- [ ] Método `search()` com filtros (nome, CPF, CNPJ, tipo, ativo)
- [ ] Método `quickSearch()` para autocomplete (LIMIT 10, ordenado por relevância)
- [ ] Método `getDefaultConsumer()` retorna "Consumidor Final"

### Task 5: Criar CustomerController
- [ ] Criar endpoints CRUD REST
- [ ] Endpoint `GET /api/customers/search?q={query}` para busca rápida
- [ ] DTOs: `CustomerRequestDTO`, `CustomerResponseDTO`, `CustomerQuickDTO`
- [ ] Tratamento de erros (409 para CPF/CNPJ duplicado, 400 para validações)
- [ ] Paginação com Pageable do Spring

### Task 6: Frontend - CustomerListComponent
- [ ] Criar component com tabela de clientes
- [ ] Implementar filtros (busca, tipo, status)
- [ ] Paginação com Material Paginator
- [ ] Badges visuais para status e tipo
- [ ] Modal de cadastro/edição

### Task 7: Frontend - CustomerFormComponent
- [ ] Criar formulário reativo com validações
- [ ] Implementar máscaras (CPF, CNPJ, CEP, telefone)
- [ ] Integração com ViaCEP para busca de endereço
- [ ] Validação de CPF/CNPJ no frontend (pipe customizado)
- [ ] Alternância de campos PF/PJ

### Task 8: Frontend - CustomerQuickSearchComponent
- [ ] Criar component reutilizável de autocomplete
- [ ] Debounce de 300ms para evitar múltiplas requisições
- [ ] Service: `CustomerService.quickSearch(query)`
- [ ] Exibição de resultados formatados
- [ ] Botão "Cadastro Rápido"

### Task 9: Frontend - CustomerQuickCreateComponent
- [ ] Modal simplificado com campos mínimos
- [ ] Validação de CPF/CNPJ único (feedback em tempo real)
- [ ] UX otimizada para velocidade

### Task 10: Testes

#### Testing

- [ ] Teste de integração: criação de cliente PF com dados completos
- [ ] Teste: criação de cliente PJ com dados completos
- [ ] Teste: validação de CPF duplicado retorna 409
- [ ] Teste: validação de CNPJ inválido retorna 400
- [ ] Teste: soft delete marca como inativo
- [ ] Teste: busca por nome retorna resultados corretos
- [ ] Teste: criptografia/decriptografia de CPF funciona
- [ ] Teste: quickSearch retorna max 10 resultados em < 500ms

---

## Definition of Done (DoD)

- [ ] Migration executada com sucesso
- [ ] Cliente padrão "Consumidor Final" criado
- [ ] Criptografia AES-256 implementada para CPF/CNPJ/email
- [ ] Entidade Customer e Repository criados
- [ ] CustomerService implementado com validações
- [ ] CustomerController com endpoints REST
- [ ] Frontend lista clientes com filtros e paginação
- [ ] Frontend permite criar/editar cliente (completo e rápido)
- [ ] Busca rápida com autocomplete funciona
- [ ] Integração ViaCEP funciona corretamente
- [ ] Validação de CPF/CNPJ implementada
- [ ] Testes de integração passando (incluindo criptografia)
- [ ] Performance: busca rápida < 500ms (NFR3)
- [ ] Code review aprovado
- [ ] Documentação técnica atualizada

---

## Dependencies & Blockers

**Depende de:**
- Story 1.3 (Multi-tenancy) - Clientes são tenant-specific

**Bloqueia:**
- Story 4.2 (PDV) - PDV precisa de clientes para vendas
- Story 4.5 (Ordem de Venda B2B) - OV precisa de clientes B2B

---

## Technical Notes

**Criptografia AES-256 com JPA Converter:**
```java
@Component
public class CryptoService {
    private static final String ALGORITHM = "AES/GCM/NoPadding";
    private static final int GCM_TAG_LENGTH = 128;

    @Value("${encryption.key}")
    private String encryptionKey;

    public String encrypt(String plainText) throws Exception {
        if (plainText == null) return null;

        SecretKeySpec keySpec = new SecretKeySpec(
            Base64.getDecoder().decode(encryptionKey), "AES"
        );

        Cipher cipher = Cipher.getInstance(ALGORITHM);
        byte[] iv = new byte[12];
        SecureRandom.getInstanceStrong().nextBytes(iv);
        GCMParameterSpec parameterSpec = new GCMParameterSpec(GCM_TAG_LENGTH, iv);

        cipher.init(Cipher.ENCRYPT_MODE, keySpec, parameterSpec);
        byte[] cipherText = cipher.doFinal(plainText.getBytes(StandardCharsets.UTF_8));

        // Concatenar IV + cipherText
        byte[] encrypted = new byte[iv.length + cipherText.length];
        System.arraycopy(iv, 0, encrypted, 0, iv.length);
        System.arraycopy(cipherText, 0, encrypted, iv.length, cipherText.length);

        return Base64.getEncoder().encodeToString(encrypted);
    }

    public String decrypt(String encryptedText) throws Exception {
        if (encryptedText == null) return null;

        byte[] encrypted = Base64.getDecoder().decode(encryptedText);

        SecretKeySpec keySpec = new SecretKeySpec(
            Base64.getDecoder().decode(encryptionKey), "AES"
        );

        // Extrair IV e cipherText
        byte[] iv = new byte[12];
        System.arraycopy(encrypted, 0, iv, 0, iv.length);
        byte[] cipherText = new byte[encrypted.length - iv.length];
        System.arraycopy(encrypted, iv.length, cipherText, 0, cipherText.length);

        Cipher cipher = Cipher.getInstance(ALGORITHM);
        GCMParameterSpec parameterSpec = new GCMParameterSpec(GCM_TAG_LENGTH, iv);
        cipher.init(Cipher.DECRYPT_MODE, keySpec, parameterSpec);

        byte[] plainText = cipher.doFinal(cipherText);
        return new String(plainText, StandardCharsets.UTF_8);
    }
}

@Converter
public class CpfEncryptionConverter implements AttributeConverter<String, String> {
    @Autowired
    private CryptoService cryptoService;

    @Override
    public String convertToDatabaseColumn(String attribute) {
        try {
            return cryptoService.encrypt(attribute);
        } catch (Exception e) {
            throw new RuntimeException("Erro ao criptografar CPF", e);
        }
    }

    @Override
    public String convertToEntityAttribute(String dbData) {
        try {
            return cryptoService.decrypt(dbData);
        } catch (Exception e) {
            throw new RuntimeException("Erro ao decriptar CPF", e);
        }
    }
}

// Aplicação na entidade
@Entity
@Table(name = "customers")
public class Customer {
    @Convert(converter = CpfEncryptionConverter.class)
    @Column(name = "cpf")
    private String cpf;

    @Convert(converter = CnpjEncryptionConverter.class)
    @Column(name = "cnpj")
    private String cnpj;

    @Convert(converter = EmailEncryptionConverter.class)
    @Column(name = "email")
    private String email;
}
```

**Validação de CPF (similar à do Supplier):**
```java
public class CpfValidator {
    public static boolean isValid(String cpf) {
        cpf = cpf.replaceAll("[^0-9]", "");

        if (cpf.length() != 11) return false;
        if (cpf.matches("(\\d)\\1{10}")) return false;

        int[] multiplicadores1 = {10,9,8,7,6,5,4,3,2};
        int soma = 0;
        for (int i = 0; i < 9; i++) {
            soma += Character.getNumericValue(cpf.charAt(i)) * multiplicadores1[i];
        }
        int digito1 = (soma % 11 < 2) ? 0 : 11 - (soma % 11);

        if (Character.getNumericValue(cpf.charAt(9)) != digito1) return false;

        int[] multiplicadores2 = {11,10,9,8,7,6,5,4,3,2};
        soma = 0;
        for (int i = 0; i < 10; i++) {
            soma += Character.getNumericValue(cpf.charAt(i)) * multiplicadores2[i];
        }
        int digito2 = (soma % 11 < 2) ? 0 : 11 - (soma % 11);

        return Character.getNumericValue(cpf.charAt(10)) == digito2;
    }
}
```

**Migration - Cliente Padrão "Consumidor Final":**
```sql
-- V030__create_customers_table.sql
INSERT INTO customers (
    id, tenant_id, tipo_pessoa, cpf, nome,
    is_default_consumer, ativo, data_criacao
)
SELECT
    gen_random_uuid(),
    t.id,
    'PF',
    encrypt_aes256('00000000000'), -- CPF reservado
    'Consumidor Final',
    true,
    true,
    NOW()
FROM tenants t;
```

**Payload de Request (Criar Cliente PF):**
```json
{
  "tipo_pessoa": "PF",
  "cpf": "123.456.789-09",
  "nome": "João da Silva",
  "email": "joao.silva@email.com",
  "celular": "(11) 98765-4321",
  "endereco_cep": "01310-100",
  "endereco_logradouro": "Avenida Paulista",
  "endereco_numero": "1000",
  "endereco_bairro": "Bela Vista",
  "endereco_cidade": "São Paulo",
  "endereco_uf": "SP"
}
```

**Response de Sucesso:**
```json
{
  "id": "123e4567-e89b-12d3-a456-426614174000",
  "tipo_pessoa": "PF",
  "cpf": "123.456.789-09",
  "nome": "João da Silva",
  "email": "joao.silva@email.com",
  "celular": "(11) 98765-4321",
  "endereco_completo": "Avenida Paulista, 1000 - Bela Vista - São Paulo/SP - 01310-100",
  "ativo": true,
  "data_criacao": "2025-11-21T10:00:00Z"
}
```

---

## Change Log

| Data       | Autor                  | Alteração                                                         |
|------------|------------------------|-------------------------------------------------------------------|
| 2025-11-21 | Claude Code (PM)       | Story drafted                                                     |
| 2025-11-21 | Sarah (PO)             | Migration version corrigida de V030 para V047 (validação épico)  |
| 2025-11-21 | Sarah (PO)             | Adicionadas seções Status, Testing, QA Results (template compliance) |

---

## Dev Agent Record

**Agent Model Used:**
Claude 3.5 Sonnet (claude-sonnet-4-5-20250929)

### Debug Log References

### Completion Notes List

### File List

---

## QA Results

**Validation Status**: ✅ Approved
**Validated By**: Sarah (Product Owner)
**Validation Date**: 2025-11-21

**Findings**:
- ✅ Epic alignment verified
- ✅ Acceptance Criteria complete and testable
- ✅ Task breakdown adequate
- ✅ Migration version corrected to V047
- ✅ Template compliance achieved

**Notes**:
- Excellent AES-256 encryption implementation for sensitive data (CPF/CNPJ/email)
- Azure Key Vault integration well-specified (NFR14 compliance)
- Cliente "Consumidor Final" padrão bem projetado
- Quick search with autocomplete < 500ms (NFR3) properly specified
- CPF/CNPJ validation algorithms identical to Supplier story (code reuse opportunity)
- Ready for development

**Security Highlights**:
- AES/GCM/NoPadding with 128-bit tag
- IV randomization per encryption
- Key management via Azure Key Vault
- JPA AttributeConverter transparente

---

**Story criada por**: Claude Code Assistant (Product Manager)
**Data**: 2025-11-21
**Baseado em**: Epic 4, docs/epics/epic-04-sales-channels.md
