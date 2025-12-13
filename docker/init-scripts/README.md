# PostgreSQL Init Scripts

Este diretório contém scripts SQL que são executados **automaticamente** quando o container PostgreSQL é criado pela primeira vez.

## 📁 Arquivos

- `01-init-dev-data.sql` - Cria dados de desenvolvimento (tenant, schema, tabelas, usuário admin)

## 🚀 Como usar

### Primeira vez (container novo)

```bash
docker-compose up -d postgres
```

Os scripts rodam automaticamente! ✅

### Se o container já existe

Os scripts **SÓ rodam na primeira criação do volume**. Se você já tem um volume PostgreSQL, precisa recriá-lo:

```bash
# Parar e remover containers
docker-compose down

# Remover o volume do PostgreSQL (⚠️ APAGA TODOS OS DADOS!)
docker volume rm estoque-central-postgres-data

# Recriar tudo
docker-compose up -d postgres
```

## 📝 O que o script cria

1. ✅ **Tenant padrão**: `00000000-0000-0000-0000-000000000000`
2. ✅ **Schema do tenant**: `tenant_00000000_0000_0000_0000_000000000000`
3. ✅ **Tabela usuarios** no schema `public` (fix temporário multi-tenancy)
4. ✅ **Usuário admin** pré-criado para login com Google

## ⚠️ IMPORTANTE

- **Este script é APENAS para desenvolvimento!**
- **NÃO use em produção!**
- Edite o arquivo `01-init-dev-data.sql` para adicionar seu próprio Google ID se necessário

## 🔧 Adicionar novos scripts

Crie novos arquivos com prefixo numérico para controlar a ordem:
- `01-init-dev-data.sql` ← já existe
- `02-seed-produtos.sql` ← seu novo script
- `03-seed-categorias.sql` ← outro script

Os scripts rodam em ordem alfabética.

## 🐛 Troubleshooting

**Script não executou?**
- Verifique se o volume foi recriado (`docker volume ls`)
- Veja os logs: `docker logs estoque-central-postgres`
- O script só roda **UMA VEZ** quando o volume é criado pela primeira vez
