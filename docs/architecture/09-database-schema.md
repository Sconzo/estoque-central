# 9. Database Schema

## 9.1. Schema-per-Tenant Strategy

```sql
-- Master schema (public)
CREATE TABLE public.tenants (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    nome VARCHAR(255) NOT NULL,
    schema_name VARCHAR(255) UNIQUE NOT NULL,
    cnpj VARCHAR(14),
    email VARCHAR(255) NOT NULL,
    ativo BOOLEAN DEFAULT true,
    data_criacao TIMESTAMP DEFAULT NOW(),
    data_atualizacao TIMESTAMP DEFAULT NOW()
);

CREATE INDEX idx_tenants_schema_name ON public.tenants(schema_name);
```

## 9.2. Tenant Schema (tenant_{uuid})

```sql
-- Cada tenant possui este schema completo

-- Usuarios
CREATE TABLE usuarios (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    nome VARCHAR(255) NOT NULL,
    email VARCHAR(255) UNIQUE NOT NULL,
    google_id VARCHAR(255) UNIQUE,
    role VARCHAR(50) NOT NULL CHECK (role IN ('ADMIN', 'GERENTE', 'OPERADOR_PDV', 'VENDEDOR', 'ESTOQUISTA')),
    ativo BOOLEAN DEFAULT true,
    ultimo_login TIMESTAMP,
    created_at TIMESTAMP DEFAULT NOW(),
    updated_at TIMESTAMP DEFAULT NOW()
);

-- Categorias
CREATE TABLE categorias (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    nome VARCHAR(255) NOT NULL,
    descricao TEXT,
    ativa BOOLEAN DEFAULT true,
    created_at TIMESTAMP DEFAULT NOW()
);

-- Produtos
CREATE TABLE produtos (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tipo VARCHAR(20) NOT NULL CHECK (tipo IN ('SIMPLES', 'VARIANTE_PAI', 'VARIANTE_FILHO', 'COMPOSTO')),
    sku VARCHAR(100) UNIQUE NOT NULL,
    nome VARCHAR(255) NOT NULL,
    descricao TEXT,
    categoria_id UUID REFERENCES categorias(id),
    preco_centavos BIGINT NOT NULL,
    custo_centavos BIGINT DEFAULT 0,
    codigo_barras VARCHAR(50),
    ncm VARCHAR(8),
    cfop VARCHAR(4),
    unidade_medida VARCHAR(10) DEFAULT 'UN',
    ativo BOOLEAN DEFAULT true,
    produto_pai_id UUID REFERENCES produtos(id),
    atributos JSONB,
    created_at TIMESTAMP DEFAULT NOW(),
    updated_at TIMESTAMP DEFAULT NOW()
);

CREATE INDEX idx_produtos_sku ON produtos(sku);
CREATE INDEX idx_produtos_tipo ON produtos(tipo);

-- Produtos Compostos (Kits)
CREATE TABLE produtos_compostos_itens (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    produto_composto_id UUID NOT NULL REFERENCES produtos(id),
    produto_item_id UUID NOT NULL REFERENCES produtos(id),
    quantidade INT NOT NULL CHECK (quantidade > 0),
    UNIQUE(produto_composto_id, produto_item_id)
);

-- Estoque
CREATE TABLE estoque (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    produto_id UUID UNIQUE NOT NULL REFERENCES produtos(id),
    quantidade_disponivel INT DEFAULT 0,
    quantidade_reservada INT DEFAULT 0,
    custo_medio_ponderado_centavos BIGINT DEFAULT 0,
    localizacao VARCHAR(50),
    updated_at TIMESTAMP DEFAULT NOW()
);

-- Movimentações de Estoque
CREATE TABLE movimentacoes_estoque (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    produto_id UUID NOT NULL REFERENCES produtos(id),
    tipo VARCHAR(30) NOT NULL CHECK (tipo IN ('ENTRADA_COMPRA', 'SAIDA_VENDA', 'AJUSTE_ENTRADA', 'AJUSTE_SAIDA')),
    quantidade INT NOT NULL,
    custo_unitario_centavos BIGINT,
    referencia VARCHAR(255),
    observacao TEXT,
    usuario_id UUID REFERENCES usuarios(id),
    created_at TIMESTAMP DEFAULT NOW()
);

CREATE INDEX idx_movimentacoes_produto ON movimentacoes_estoque(produto_id);

-- Clientes
CREATE TABLE clientes (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tipo VARCHAR(20) CHECK (tipo IN ('PESSOA_FISICA', 'PESSOA_JURIDICA')),
    nome VARCHAR(255) NOT NULL,
    cpf_cnpj VARCHAR(14),
    email VARCHAR(255),
    telefone VARCHAR(20),
    endereco JSONB,
    ativo BOOLEAN DEFAULT true,
    created_at TIMESTAMP DEFAULT NOW()
);

-- Vendas
CREATE TABLE vendas (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    numero VARCHAR(50) UNIQUE NOT NULL,
    tipo VARCHAR(20) NOT NULL CHECK (tipo IN ('PDV', 'B2B', 'B2C', 'MERCADO_LIVRE')),
    status VARCHAR(20) NOT NULL CHECK (status IN ('PENDENTE', 'PAGO', 'CANCELADA', 'ESTORNADA')),
    cliente_id UUID REFERENCES clientes(id),
    subtotal_centavos BIGINT NOT NULL,
    desconto_centavos BIGINT DEFAULT 0,
    total_centavos BIGINT NOT NULL,
    forma_pagamento VARCHAR(20),
    usuario_id UUID NOT NULL REFERENCES usuarios(id),
    observacao TEXT,
    created_at TIMESTAMP DEFAULT NOW(),
    updated_at TIMESTAMP DEFAULT NOW()
);

CREATE INDEX idx_vendas_numero ON vendas(numero);
CREATE INDEX idx_vendas_status ON vendas(status);

-- Itens de Venda
CREATE TABLE itens_venda (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    venda_id UUID NOT NULL REFERENCES vendas(id) ON DELETE CASCADE,
    produto_id UUID NOT NULL REFERENCES produtos(id),
    quantidade INT NOT NULL CHECK (quantidade > 0),
    preco_unitario_centavos BIGINT NOT NULL,
    desconto_centavos BIGINT DEFAULT 0,
    subtotal_centavos BIGINT NOT NULL
);

CREATE INDEX idx_itens_venda_venda ON itens_venda(venda_id);

-- NFCe
CREATE TABLE nfce (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    venda_id UUID UNIQUE NOT NULL REFERENCES vendas(id),
    numero VARCHAR(50),
    serie VARCHAR(10),
    chave VARCHAR(44) UNIQUE,
    status VARCHAR(30) NOT NULL CHECK (status IN ('PENDENTE', 'EMITINDO', 'AUTORIZADA', 'REJEITADA', 'CANCELADA', 'FALHA_PERMANENTE')),
    data_emissao TIMESTAMP,
    tentativas_emissao INT DEFAULT 0,
    ultima_tentativa TIMESTAMP,
    proxima_tentativa TIMESTAMP,
    erro TEXT,
    xml_url VARCHAR(500),
    pdf_url VARCHAR(500),
    created_at TIMESTAMP DEFAULT NOW(),
    updated_at TIMESTAMP DEFAULT NOW()
);

CREATE INDEX idx_nfce_venda ON nfce(venda_id);
CREATE INDEX idx_nfce_status ON nfce(status);

-- Fornecedores
CREATE TABLE fornecedores (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    nome VARCHAR(255) NOT NULL,
    cnpj VARCHAR(14),
    email VARCHAR(255),
    telefone VARCHAR(20),
    endereco JSONB,
    ativo BOOLEAN DEFAULT true,
    created_at TIMESTAMP DEFAULT NOW()
);

-- Compras
CREATE TABLE compras (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    numero VARCHAR(50) UNIQUE NOT NULL,
    fornecedor_id UUID NOT NULL REFERENCES fornecedores(id),
    status VARCHAR(30) CHECK (status IN ('RASCUNHO', 'ENVIADA', 'CONFIRMADA', 'RECEBIDA_PARCIAL', 'RECEBIDA_TOTAL', 'CANCELADA')),
    total_centavos BIGINT NOT NULL,
    data_emissao DATE NOT NULL,
    data_previsao_entrega DATE,
    observacao TEXT,
    created_at TIMESTAMP DEFAULT NOW()
);

-- Itens de Compra
CREATE TABLE itens_compra (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    compra_id UUID NOT NULL REFERENCES compras(id) ON DELETE CASCADE,
    produto_id UUID NOT NULL REFERENCES produtos(id),
    quantidade INT NOT NULL CHECK (quantidade > 0),
    quantidade_recebida INT DEFAULT 0,
    custo_unitario_centavos BIGINT NOT NULL,
    subtotal_centavos BIGINT NOT NULL
);

-- Integração Mercado Livre
CREATE TABLE integracoes_ml (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    produto_id UUID UNIQUE NOT NULL REFERENCES produtos(id),
    ml_item_id VARCHAR(50) UNIQUE NOT NULL,
    ml_permalink VARCHAR(500),
    titulo VARCHAR(255),
    preco_ml_centavos BIGINT,
    estoque INT,
    status VARCHAR(30),
    ultima_sync TIMESTAMP,
    erro TEXT,
    created_at TIMESTAMP DEFAULT NOW()
);
```

## 9.3. Flyway Migration Strategy

```
db/migration/
├── V001__criar_schema_master.sql
├── V002__criar_tabela_tenants.sql
├── V003__criar_funcao_novo_tenant.sql
├── V004__criar_tabelas_tenant_base.sql
└── V005__criar_indices.sql
```

**Multi-tenant Migration:**
```java
// Rodar migrations em todos os schemas de tenants
flyway.setSchemas("tenant_123e4567");
flyway.migrate();
```
