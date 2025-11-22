# Story 3.1: Supplier Management (Gestão de Fornecedores)

**Epic**: 3 - Purchasing & Inventory Replenishment
**Story ID**: 3.1
**Status**: approved
**Created**: 2025-11-21
**Updated**: 2025-11-21

---

## User Story

Como **gerente de compras**,
Eu quero **CRUD completo de fornecedores com dados fiscais necessários para emissão de documentos**,
Para que **eu possa gerenciar relacionamento com fornecedores e garantir compliance fiscal**.

---

## Context & Business Value

Esta story implementa o cadastro completo de fornecedores (suppliers) com todos os dados necessários para emissão de documentos fiscais e gestão de relacionamento comercial. É a base para o fluxo de compras e recebimento de mercadorias.

**Valor de Negócio:**
- **Compliance Fiscal**: Armazena dados necessários para emissão de documentos fiscais (CNPJ, IE, endereço)
- **Gestão de Relacionamento**: Centraliza informações de contato e condições comerciais
- **Rastreabilidade**: Permite vincular Ordens de Compra e movimentações de estoque a fornecedores
- **Organização**: Separação clara entre fornecedores ativos e inativos

**Contexto Arquitetural:**
- **Pessoa Jurídica**: Foco inicial em PJ (CNPJ), suporte a PF (CPF) pode ser adicionado futuro
- **Soft Delete**: Fornecedores inativos não aparecem em seleções mas mantêm histórico
- **Validação**: CNPJ único por tenant, validação de formato

---

## Acceptance Criteria

### AC1: Tabela suppliers Criada
- [ ] Migration cria tabela `suppliers` no schema tenant com colunas:
  - `id` (UUID, PK)
  - `tenant_id` (UUID, FK para tenants)
  - `tipo_pessoa` (VARCHAR(2), DEFAULT 'PJ') - 'PJ' ou 'PF'
  - `cnpj` (VARCHAR(18), NULLABLE) - formato: 00.000.000/0000-00
  - `cpf` (VARCHAR(14), NULLABLE) - formato: 000.000.000-00
  - `razao_social` (VARCHAR(200), NOT NULL)
  - `nome_fantasia` (VARCHAR(200))
  - `inscricao_estadual` (VARCHAR(20))
  - `inscricao_municipal` (VARCHAR(20))
  - `email` (VARCHAR(100))
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
- [ ] Índices criados: `idx_suppliers_tenant_id`, `idx_suppliers_cnpj`, `idx_suppliers_razao_social`
- [ ] Constraint: `UNIQUE (tenant_id, cnpj)` para CNPJ único por tenant
- [ ] Constraint: `CHECK (tipo_pessoa IN ('PJ', 'PF'))`
- [ ] Constraint: `CHECK ((tipo_pessoa = 'PJ' AND cnpj IS NOT NULL) OR (tipo_pessoa = 'PF' AND cpf IS NOT NULL))`

### AC2: Endpoints CRUD de Suppliers
- [ ] `POST /api/suppliers` cria fornecedor com payload completo
- [ ] `GET /api/suppliers` retorna lista paginada com filtros:
  - `nome` (busca em razão social ou nome fantasia)
  - `cnpj` (busca exata)
  - `ativo` (true/false/all, default: true)
- [ ] `GET /api/suppliers/{id}` retorna detalhes de um fornecedor
- [ ] `PUT /api/suppliers/{id}` edita fornecedor
- [ ] `DELETE /api/suppliers/{id}` marca como inativo (soft delete)
- [ ] Paginação: default 20 por página, max 100

### AC3: Validações de Dados Fiscais
- [ ] Validação de formato CNPJ (com dígitos verificadores)
- [ ] Validação de formato CPF (com dígitos verificadores) se tipo_pessoa = PF
- [ ] Validação de CEP (formato 00000-000)
- [ ] Validação de UF (lista fixa de estados brasileiros)
- [ ] CNPJ único por tenant (retorna HTTP 409 se duplicado)
- [ ] Email formato válido (regex)
- [ ] Telefone/celular formato brasileiro (DDD + número)

### AC4: Frontend - Lista de Fornecedores
- [ ] Component Angular `SupplierListComponent` criado
- [ ] Tabela com colunas: CNPJ, Razão Social, Nome Fantasia, Cidade/UF, Telefone, Status, Ações
- [ ] Filtros: busca por nome, CNPJ, status (ativo/inativo/todos)
- [ ] Paginação com Angular Material Paginator
- [ ] Botão "Novo Fornecedor" abre modal de cadastro
- [ ] Ações inline: Editar (ícone lápis), Inativar (ícone lixeira com confirmação)
- [ ] Badge visual para status: verde (Ativo), cinza (Inativo)

### AC5: Frontend - Formulário de Cadastro/Edição
- [ ] Component `SupplierFormComponent` com formulário reativo
- [ ] Radio button: Pessoa Jurídica / Pessoa Física (alterna campos CNPJ/CPF)
- [ ] Campos PJ: CNPJ (máscara), Razão Social*, Nome Fantasia, IE, IM
- [ ] Campos PF: CPF (máscara), Nome Completo*
- [ ] Campos contato: Email (validação), Telefone (máscara), Celular (máscara)
- [ ] Campos endereço: CEP (máscara + busca ViaCEP), Logradouro, Número, Complemento, Bairro, Cidade, UF (dropdown),
- [ ] Campo observações (textarea)
- [ ] Validação inline com mensagens de erro claras
- [ ] Botões: Salvar (desabilitado se form inválido), Cancelar
- [ ] Busca automática de endereço por CEP usando API ViaCEP

### AC6: Integração com ViaCEP
- [ ] Service `CepService` implementado no frontend
- [ ] Ao preencher CEP válido, busca automaticamente endereço
- [ ] Preenche automaticamente: Logradouro, Bairro, Cidade, UF
- [ ] Permite edição manual após preenchimento automático
- [ ] Tratamento de erro: CEP não encontrado exibe mensagem clara
- [ ] Debounce de 500ms para evitar múltiplas requisições

---

## Tasks & Subtasks

### Task 1: Criar Migration de suppliers
- [ ] Criar migration `V040__create_suppliers_table.sql`
- [ ] Definir estrutura completa com campos fiscais e endereço
- [ ] Criar índices e constraints (CNPJ único, check tipo_pessoa)
- [ ] Testar migration: `mvn flyway:migrate`

### Task 2: Criar Entidade e Repository
- [ ] Criar `Supplier.java` em `purchasing.domain`
- [ ] Criar `SupplierRepository` extends `CrudRepository`
- [ ] Método `findByTenantIdAndAtivo()`
- [ ] Método customizado para busca por nome ou CNPJ

### Task 3: Implementar SupplierService
- [ ] Criar `SupplierService` com métodos CRUD
- [ ] Validação de CNPJ/CPF (algoritmo dígitos verificadores)
- [ ] Método `softDelete()` marca como inativo
- [ ] Método `search()` com filtros (nome, CNPJ, ativo)

### Task 4: Criar SupplierController
- [ ] Criar endpoints CRUD REST
- [ ] DTOs: `SupplierRequestDTO`, `SupplierResponseDTO`
- [ ] Tratamento de erros (409 para CNPJ duplicado, 400 para validações)
- [ ] Paginação com Pageable do Spring

### Task 5: Frontend - SupplierListComponent
- [ ] Criar component com tabela de fornecedores
- [ ] Implementar filtros (busca, status)
- [ ] Paginação com Material Paginator
- [ ] Modal de cadastro/edição

### Task 6: Frontend - SupplierFormComponent
- [ ] Criar formulário reativo com validações
- [ ] Implementar máscaras (CNPJ, CPF, CEP, telefone)
- [ ] Integração com ViaCEP para busca de endereço
- [ ] Validação de CNPJ/CPF no frontend (pipe customizado)

### Task 7: Frontend - CepService e Validators
- [ ] Criar `CepService` para integração ViaCEP
- [ ] Criar validators customizados: `cnpjValidator`, `cpfValidator`
- [ ] Criar pipes de máscara: `CnpjPipe`, `CpfPipe`, `CepPipe`

### Task 8: Testes

#### Testing

- [ ] Teste de integração: criação de fornecedor com dados completos
- [ ] Teste: validação de CNPJ duplicado retorna 409
- [ ] Teste: validação de CNPJ inválido retorna 400
- [ ] Teste: soft delete marca como inativo
- [ ] Teste: busca por nome retorna resultados corretos
- [ ] Teste: integração ViaCEP retorna endereço

---

## Definition of Done (DoD)

- [ ] Migration executada com sucesso
- [ ] Entidade Supplier e Repository criados
- [ ] SupplierService implementado com validações
- [ ] SupplierController com endpoints REST
- [ ] Frontend lista fornecedores com filtros e paginação
- [ ] Frontend permite criar/editar fornecedor com validações
- [ ] Integração ViaCEP funciona corretamente
- [ ] Validação de CNPJ/CPF implementada
- [ ] Testes de integração passando
- [ ] Code review aprovado
- [ ] Documentação técnica atualizada

---

## Dependencies & Blockers

**Depende de:**
- Story 1.3 (Multi-tenancy) - Fornecedores são tenant-specific

**Bloqueia:**
- Story 3.2 (Ordem de Compra) - OC precisa de fornecedor associado

---

## Technical Notes

**Validação de CNPJ (Dígitos Verificadores):**
```java
public class CnpjValidator {
    public static boolean isValid(String cnpj) {
        // Remove formatação
        cnpj = cnpj.replaceAll("[^0-9]", "");

        if (cnpj.length() != 14) return false;

        // Verifica CNPJs conhecidos como inválidos
        if (cnpj.matches("(\\d)\\1{13}")) return false;

        // Calcula primeiro dígito verificador
        int[] multiplicadores1 = {5,4,3,2,9,8,7,6,5,4,3,2};
        int soma = 0;
        for (int i = 0; i < 12; i++) {
            soma += Character.getNumericValue(cnpj.charAt(i)) * multiplicadores1[i];
        }
        int digito1 = (soma % 11 < 2) ? 0 : 11 - (soma % 11);

        if (Character.getNumericValue(cnpj.charAt(12)) != digito1) return false;

        // Calcula segundo dígito verificador
        int[] multiplicadores2 = {6,5,4,3,2,9,8,7,6,5,4,3,2};
        soma = 0;
        for (int i = 0; i < 13; i++) {
            soma += Character.getNumericValue(cnpj.charAt(i)) * multiplicadores2[i];
        }
        int digito2 = (soma % 11 < 2) ? 0 : 11 - (soma % 11);

        return Character.getNumericValue(cnpj.charAt(13)) == digito2;
    }
}
```

**Payload de Request (Criar Fornecedor PJ):**
```json
{
  "tipo_pessoa": "PJ",
  "cnpj": "12.345.678/0001-90",
  "razao_social": "Distribuidora ABC Ltda",
  "nome_fantasia": "ABC Distribuidora",
  "inscricao_estadual": "123.456.789.012",
  "email": "contato@abcdistribuidora.com.br",
  "telefone": "(11) 3456-7890",
  "celular": "(11) 98765-4321",
  "endereco_cep": "01310-100",
  "endereco_logradouro": "Avenida Paulista",
  "endereco_numero": "1000",
  "endereco_complemento": "Sala 500",
  "endereco_bairro": "Bela Vista",
  "endereco_cidade": "São Paulo",
  "endereco_uf": "SP",
  "observacoes": "Fornecedor preferencial para eletrônicos"
}
```

**Response de Sucesso:**
```json
{
  "id": "123e4567-e89b-12d3-a456-426614174000",
  "tipo_pessoa": "PJ",
  "cnpj": "12.345.678/0001-90",
  "razao_social": "Distribuidora ABC Ltda",
  "nome_fantasia": "ABC Distribuidora",
  "inscricao_estadual": "123.456.789.012",
  "email": "contato@abcdistribuidora.com.br",
  "telefone": "(11) 3456-7890",
  "celular": "(11) 98765-4321",
  "endereco_completo": "Avenida Paulista, 1000, Sala 500 - Bela Vista - São Paulo/SP - 01310-100",
  "ativo": true,
  "data_criacao": "2025-11-21T10:00:00Z",
  "data_atualizacao": "2025-11-21T10:00:00Z"
}
```

**Integração ViaCEP:**
```typescript
// cep.service.ts
export class CepService {
  searchCep(cep: string): Observable<Address> {
    const cepClean = cep.replace(/\D/g, '');
    if (cepClean.length !== 8) {
      return throwError(() => new Error('CEP inválido'));
    }

    return this.http.get<ViaCepResponse>(
      `https://viacep.com.br/ws/${cepClean}/json/`
    ).pipe(
      map(response => {
        if (response.erro) {
          throw new Error('CEP não encontrado');
        }
        return {
          logradouro: response.logradouro,
          bairro: response.bairro,
          cidade: response.localidade,
          uf: response.uf
        };
      }),
      catchError(err => throwError(() => new Error('Erro ao buscar CEP')))
    );
  }
}
```

---

## Change Log

| Data       | Autor                  | Alteração                                                         |
|------------|------------------------|-------------------------------------------------------------------|
| 2025-11-21 | Claude Code (PM)       | Story drafted                                                     |
| 2025-11-21 | Sarah (PO)             | Migration version corrigida de V020 para V040 (validação épico)  |
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
- ✅ Migration version corrected to V040
- ✅ Template compliance achieved

**Notes**:
- Excellent CNPJ/CPF validation implementation
- ViaCEP integration well-designed
- Ready for development

---

**Story criada por**: Claude Code Assistant (Product Manager)
**Data**: 2025-11-21
**Baseado em**: Epic 3, docs/epics/epic-03-purchasing.md
