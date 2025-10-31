# Docker Validation Tests - Story 1.2

Este documento lista os testes de validação que devem ser executados para verificar todos os Acceptance Criteria da Story 1.2: Docker Containerization.

## ⚠️ Pré-requisitos

- Docker 24+ rodando
- Docker Compose V2 instalado
- Repositório clonado em um ambiente limpo

## 🧪 Testes de Validação

### Test 1: Limpar ambiente

```bash
cd "C:\Users\rspol\dev\ERP v6"
docker-compose down -v
docker rmi estoque-central:latest 2>/dev/null || true
```

**Validação**: Ambiente limpo, sem containers ou imagens antigas.

---

### Test 2: Build da imagem Docker

```bash
docker build -f docker/backend.Dockerfile -t estoque-central:latest .
```

**Validação (AC1)**:
- ✅ Build completa sem erros
- ✅ Stage 1 (builder) usa eclipse-temurin:21-jdk
- ✅ Stage 2 (runtime) usa eclipse-temurin:21-jre-alpine
- ✅ JAR é copiado do builder para runtime

---

### Test 3: Verificar tamanho da imagem

```bash
docker images estoque-central:latest
```

**Validação (AC2)**:
- ✅ SIZE < 300MB
- ✅ Imagem usa Alpine Linux como base

**Exemplo de saída esperada**:
```
REPOSITORY          TAG       IMAGE ID       CREATED          SIZE
estoque-central     latest    abc123def456   2 minutes ago    250MB
```

---

### Test 4: Verificar layers otimizadas

```bash
docker history estoque-central:latest
```

**Validação (AC2)**:
- ✅ Layers separadas para dependências Maven (pom.xml)
- ✅ Layers separadas para código-fonte
- ✅ Apenas runtime dependencies na imagem final

---

### Test 5: Configurar variáveis de ambiente

```bash
cp .env.example .env
# Edite .env se necessário com credenciais reais
```

**Validação (AC4)**:
- ✅ Arquivo .env.example existe
- ✅ Contém todas as variáveis obrigatórias

---

### Test 6: Iniciar ambiente com docker-compose

```bash
docker-compose up -d
```

**Validação (AC3)**:
- ✅ 3 containers iniciam: app, postgres, redis
- ✅ Nenhum erro de build ou startup

---

### Test 7: Aguardar health checks

```bash
# Aguardar ~40 segundos para app inicializar
sleep 40

# Verificar status dos containers
docker-compose ps
```

**Validação (AC3, AC5)**:
- ✅ Todos os containers com status "Up"
- ✅ Coluna HEALTH mostra "healthy" para todos

**Exemplo de saída esperada**:
```
NAME                       STATUS          PORTS                    HEALTH
estoque-central-app        Up 1 minute     0.0.0.0:8080->8080/tcp   healthy
estoque-central-postgres   Up 1 minute     0.0.0.0:5432->5432/tcp   healthy
estoque-central-redis      Up 1 minute     0.0.0.0:6379->6379/tcp   healthy
```

---

### Test 8: Testar health check endpoint

```bash
curl http://localhost:8080/actuator/health
```

**Validação (AC5)**:
- ✅ HTTP 200 OK
- ✅ JSON response: `{"status":"UP"}`
- ✅ Componentes db e redis aparecem como UP

**Exemplo de saída esperada**:
```json
{
  "status": "UP",
  "components": {
    "db": {
      "status": "UP",
      "details": {
        "database": "PostgreSQL",
        "validationQuery": "isValid()"
      }
    },
    "redis": {
      "status": "UP",
      "details": {
        "version": "8.0.0"
      }
    }
  }
}
```

---

### Test 9: Verificar logs (sem erros de conexão)

```bash
docker-compose logs app | grep -i error
docker-compose logs app | grep -i exception
```

**Validação (AC5)**:
- ✅ Nenhum erro de conexão PostgreSQL
- ✅ Nenhum erro de conexão Redis
- ✅ Application started successfully

---

### Test 10: Simular falha PostgreSQL

```bash
# Parar PostgreSQL
docker-compose stop postgres

# Aguardar ~5 segundos
sleep 5

# Testar health check
curl http://localhost:8080/actuator/health
```

**Validação (AC5)**:
- ✅ HTTP 503 Service Unavailable
- ✅ JSON response: `{"status":"DOWN"}`
- ✅ Componente db aparece como DOWN

---

### Test 11: Restaurar PostgreSQL

```bash
# Reiniciar PostgreSQL
docker-compose start postgres

# Aguardar ~10 segundos para reconectar
sleep 10

# Testar health check novamente
curl http://localhost:8080/actuator/health
```

**Validação (AC5)**:
- ✅ HTTP 200 OK
- ✅ JSON response: `{"status":"UP"}`
- ✅ Componente db volta para UP

---

### Test 12: Verificar usuário não-root

```bash
docker exec -it estoque-central-app whoami
```

**Validação (AC6)**:
- ✅ Output: `spring` (não `root`)

---

### Test 13: Inspecionar container em execução

```bash
docker exec -it estoque-central-app ps aux
```

**Validação (AC6)**:
- ✅ Processo Java roda como usuário `spring` (UID != 0)

---

### Test 14: Verificar .dockerignore

```bash
# Build e verificar que arquivos desnecessários não foram copiados
docker build -f docker/backend.Dockerfile -t estoque-central:test --progress=plain . 2>&1 | grep -E "(COPY|ADD)"
```

**Validação (AC6)**:
- ✅ Arquivos em .dockerignore não são copiados (.git, target/, node_modules/, docs/, etc.)

---

### Test 15: Testar variáveis de ambiente customizadas

```bash
# Editar .env
echo "LOG_LEVEL=DEBUG" >> .env

# Reiniciar container
docker-compose restart app

# Aguardar reinicialização
sleep 30

# Verificar logs mostram nível DEBUG
docker-compose logs app | grep DEBUG
```

**Validação (AC4)**:
- ✅ Novas variáveis de .env são aplicadas
- ✅ Backend reflete as configurações

---

### Test 16: Testar README instruções

```bash
# Limpar tudo
docker-compose down -v

# Seguir instruções do README "Executar com Docker" passo a passo
```

**Validação (AC7)**:
- ✅ Seguir README permite iniciar ambiente completo
- ✅ Comandos úteis funcionam corretamente
- ✅ Troubleshooting é relevante e útil

---

### Test 17: Verificar networks Docker

```bash
docker network inspect estoque-central-network
```

**Validação (AC3)**:
- ✅ Network `estoque-central-network` existe
- ✅ 3 containers conectados: app, postgres, redis

---

### Test 18: Verificar volumes Docker

```bash
docker volume ls | grep estoque-central
```

**Validação (AC3)**:
- ✅ Volume `estoque-central-postgres-data` existe
- ✅ Volume `estoque-central-redis-data` existe

---

### Test 19: Testar persistência de dados

```bash
# Criar dados no PostgreSQL via aplicação
# (exemplo: criar um produto via API)

# Parar containers
docker-compose down

# Reiniciar (volumes persistem)
docker-compose up -d

# Aguardar health checks
sleep 40

# Verificar que dados ainda existem
```

**Validação (AC3)**:
- ✅ Dados persistem após restart
- ✅ Volumes nomeados funcionam corretamente

---

### Test 20: Security scan (opcional)

```bash
docker scan estoque-central:latest
```

**Validação (AC6)**:
- ✅ Nenhuma vulnerabilidade CRITICAL ou HIGH
- ⚠️ Vulnerabilidades MEDIUM/LOW são aceitáveis

---

## ✅ Checklist Final

Após executar todos os testes, verificar:

- [ ] AC1: Dockerfile Multi-Stage Criado ✅
- [ ] AC2: Imagem Docker Otimizada (< 300MB) ✅
- [ ] AC3: Docker Compose para Ambiente Local ✅
- [ ] AC4: Variáveis de Ambiente Configuráveis ✅
- [ ] AC5: Health Check Endpoint Funcionando ✅
- [ ] AC6: Dockerfile Segue Boas Práticas ✅
- [ ] AC7: README Atualizado com Instruções Docker ✅

## 🎯 Resultado Esperado

Todos os testes devem passar sem erros. Se algum teste falhar:

1. Verificar logs: `docker-compose logs`
2. Verificar configuração: `.env` e `application.properties`
3. Verificar Dockerfile: `docker/backend.Dockerfile`
4. Verificar docker-compose: `docker-compose.yml`

---

**Story**: 1.2 - Docker Containerization
**Created**: 2025-01-30
**Author**: Amelia (Dev Agent)
