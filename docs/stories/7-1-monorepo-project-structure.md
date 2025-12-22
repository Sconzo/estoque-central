# Story 7.1: Configuração de Monorepo e Estrutura de Projeto

**Epic**: 7 - Infraestrutura Multi-Tenant e Deploy
**Story ID**: 7.1
**Status**: Ready for Review
**Created**: 2025-12-22
**Updated**: 2025-12-22

---

## User Story

As a **Developer**,
I want **a monorepo structure with backend (Spring Boot) and frontend (Angular) properly configured**,
So that **I can develop both parts of the application in a unified codebase following hexagonal architecture**.

---

## Context & Business Value

Esta story estabelece a fundação do projeto de self-service multi-tenant, criando a estrutura que suportará os épicos 7-10. Aproveit human a infraestrutura já existente (épicos 1-6) e adiciona módulos específicos para multi-tenancy.

**Valor de Negócio:**
- Ambiente padronizado permite desenvolvimento ágil da feature de auto-cadastro de empresas
- Estrutura modular facilita manutenção e evolução
- Base sólida para escalabilidade multi-tenant

**Contexto Arquitetural:**
- Monorepo híbrido: Backend (Maven) + Frontend (Angular)
- Hexagonal Architecture + Spring Modulith para bounded contexts
- Schema-per-tenant isolation strategy

---

## Acceptance Criteria

### AC1: Projeto com Estrutura Completa

**Given** a new project initialization
**When** the monorepo is set up
**Then** the repository contains:
- `/backend` folder with Spring Boot 3.3+ Maven project (Java 21)
- `/frontend` folder with Angular 17+ project (standalone components)
- `/docs` folder with architecture and PRD documents
- `/.github/workflows` folder for CI/CD configurations
- `/docker` folder with Dockerfiles for backend and frontend
**And** backend follows Hexagonal Architecture with packages: `domain/`, `application/`, `adapter/in/`, `adapter/out/`
**And** backend uses Spring Modulith structure with bounded contexts as packages
**And** frontend uses feature-based structure with `core/`, `shared/`, `features/`, `layout/`
**And** root `.gitignore` includes Java, Angular, and IDE-specific entries
**And** `README.md` documents getting started instructions

### AC2: Backend Dependencies Configuradas

**Given** backend dependencies
**When** `pom.xml` is configured
**Then** it includes:
- Spring Boot 3.3+ parent
- Spring Data JDBC (not JPA)
- Spring Security + OAuth2 Client
- PostgreSQL driver
- Redis (Redisson or Spring Data Redis)
- Flyway for migrations
- Spring Modulith
**And** Java version is set to 21

### AC3: Frontend Dependencies Configuradas

**Given** frontend dependencies
**When** `package.json` is configured
**Then** it includes:
- Angular 17+ with standalone components
- Angular Material 18+
- RxJS for reactive programming
- TypeScript 5+
**And** Angular CLI is configured for standalone components

---

## Tasks & Subtasks

### Task 1: Verificar Estrutura Existente
**Subtasks:**
1. [x] Verificar se backend/ já existe (de épicos anteriores)
2. [x] Verificar se frontend/ já existe
3. [x] Identificar o que precisa ser adicionado vs. aproveitado

### Task 2: Configurar Módulos Multi-Tenant no Backend
**Subtasks:**
1. [x] Adicionar pacote `com.estoquecentral.tenant/domain/`, `.application/`, `.adapter/`
2. [x] Adicionar pacote `com.estoquecentral.company/domain/`, `.application/`, `.adapter/`
3. [x] Criar `package-info.java` para novos módulos Spring Modulith

### Task 3: Configurar Estrutura Frontend para Multi-Tenant
**Subtasks:**
1. [x] Criar `src/app/features/tenant/` (gestão de tenant/contexto)
2. [x] Criar `src/app/features/company/` (criação e gestão de empresa)
3. [x] Criar `src/app/features/collaborators/` (gestão de colaboradores)
4. [x] Adicionar serviços: `TenantService`, `CompanyService`, `CollaboratorService` em core/

### Task 4: Atualizar Documentação
**Subtasks:**
1. [x] Atualizar README.md com informações sobre multi-tenancy
2. [x] Documentar novos módulos em docs/architecture/

---

## Definition of Done (DoD)

- [x] Estrutura de pastas criada e validada
- [x] Backend compila sem erros: `mvn clean compile`
- [x] Frontend compila sem erros: `npm run build`
- [x] README.md atualizado
- [ ] Código commitado e push realizado

---

## Dependencies & Blockers

**Dependências:**
- Epic 1 (Foundation) deve estar completo

**Blockers Conhecidos:**
- Nenhum

---

---

## Dev Agent Record

### Implementation Plan

Story 7.1 estabelece a estrutura base multi-tenant no monorepo aproveitando infraestrutura existente:

**Backend**:
1. Módulo `tenant` já existia - adicionado `package-info.java` para Spring Modulith
2. Módulo `company` criado do zero com arquitetura hexagonal completa:
   - Domain: `Company`, `CompanyUser` aggregates
   - Application: `CompanyService`, `CollaboratorService`
   - Adapter: Repositories, DTOs
   - Suporte a Epics 8-10

**Frontend**:
1. Estrutura de features criada: `tenant/`, `company/`, `collaborators/`
2. Serviços core criados: `TenantService`, `CompanyService`, `CollaboratorService`
3. Componentes placeholder para implementações futuras

**Documentação**:
1. README.md atualizado com seção multi-tenancy
2. Arquitetura documentada em `multi-tenant-modules.md`

### Completion Notes

✅ Todos os módulos multi-tenant criados com arquitetura hexagonal
✅ Backend compila sem erros (mvn clean compile)
✅ Frontend compila sem erros (npm run build)
✅ Documentação completa criada
✅ Preparado para implementação dos Epics 8-10

**Decisões Técnicas**:
- CompanyUser auto-aceita convites (pode ser alterado para pending no futuro)
- Módulos documentados via Spring Modulith package-info.java
- Frontend usa Angular Signals para estado reativo
- TenantService persiste contexto em localStorage

---

## File List

### Backend (Java)
- `backend/src/main/java/com/estoquecentral/tenant/package-info.java`
- `backend/src/main/java/com/estoquecentral/company/package-info.java`
- `backend/src/main/java/com/estoquecentral/company/domain/Company.java`
- `backend/src/main/java/com/estoquecentral/company/domain/CompanyUser.java`
- `backend/src/main/java/com/estoquecentral/company/application/CompanyService.java`
- `backend/src/main/java/com/estoquecentral/company/application/CollaboratorService.java`
- `backend/src/main/java/com/estoquecentral/company/adapter/out/CompanyRepository.java`
- `backend/src/main/java/com/estoquecentral/company/adapter/out/CompanyUserRepository.java`
- `backend/src/main/java/com/estoquecentral/company/adapter/in/dto/CompanyDTO.java`
- `backend/src/main/java/com/estoquecentral/company/adapter/in/dto/CreateCompanyRequest.java`
- `backend/src/main/java/com/estoquecentral/company/adapter/in/dto/CollaboratorDTO.java`

### Frontend (TypeScript)
- `frontend/src/app/core/services/tenant.service.ts`
- `frontend/src/app/core/services/company.service.ts`
- `frontend/src/app/core/services/collaborator.service.ts`
- `frontend/src/app/features/tenant/tenant.component.ts`
- `frontend/src/app/features/company/company.component.ts`
- `frontend/src/app/features/collaborators/collaborators.component.ts`

### Documentation
- `README.md` (updated)
- `docs/architecture/multi-tenant-modules.md`

---

## Change Log

- **2025-12-22**: Story criada por poly (PM Agent) baseado em Epic 7, PRD (FR24-FR27)
- **2025-12-22**: Implementação completa por Amelia (Dev Agent):
  - Backend: Módulos tenant e company criados com arquitetura hexagonal
  - Frontend: Features e serviços multi-tenant criados
  - Documentação: README e arquitetura atualizados
  - Builds: Backend e frontend validados com sucesso

---

**Story criada por**: poly (PM Agent)
**Story implementada por**: Amelia (Dev Agent)
**Data**: 2025-12-22
**Baseado em**: Epic 7, PRD (FR24-FR27)
