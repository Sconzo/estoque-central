-- ============================================================================
-- Migration V002: Create Tenant Schema (Applied to each tenant schema)
-- ============================================================================
-- Purpose: Creates the complete business schema for a tenant
--
-- This migration is applied to EACH tenant schema (tenant_{uuid}).
-- It creates all business tables: usuarios, produtos, vendas, estoque, etc.
--
-- IMPORTANT: This migration runs in the TENANT schema, not public schema.
-- ============================================================================

-- ============================================================================
-- Table: usuarios (Users)
-- ============================================================================
CREATE TABLE IF NOT EXISTS usuarios (
    id UUID PRIMARY KEY,
    nome VARCHAR(255) NOT NULL,
    email VARCHAR(255) UNIQUE NOT NULL,
    google_id VARCHAR(255) UNIQUE,
    picture_url VARCHAR(500),
    role VARCHAR(50) NOT NULL CHECK (role IN ('ADMIN', 'GERENTE', 'VENDEDOR', 'ESTOQUISTA')),
    ativo BOOLEAN DEFAULT true NOT NULL,
    data_criacao TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
    data_atualizacao TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL
);

CREATE INDEX idx_usuarios_email ON usuarios(email);
CREATE INDEX idx_usuarios_google_id ON usuarios(google_id);
CREATE INDEX idx_usuarios_ativo ON usuarios(ativo) WHERE ativo = true;

COMMENT ON TABLE usuarios IS 'Usuários do tenant com autenticação Google OAuth';

-- ============================================================================
-- Table: categorias (Product Categories)
-- ============================================================================
CREATE TABLE IF NOT EXISTS categorias (
    id UUID PRIMARY KEY,
    nome VARCHAR(255) NOT NULL,
    descricao TEXT,
    ativa BOOLEAN DEFAULT true NOT NULL,
    data_criacao TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
    data_atualizacao TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL
);

CREATE INDEX idx_categorias_ativa ON categorias(ativa) WHERE ativa = true;

COMMENT ON TABLE categorias IS 'Categorias de produtos';

-- ============================================================================
-- Table: produtos (Products)
-- ============================================================================
CREATE TABLE IF NOT EXISTS produtos (
    id UUID PRIMARY KEY,
    tipo VARCHAR(20) NOT NULL CHECK (tipo IN ('SIMPLES', 'VARIANTE', 'COMPOSTO')),
    sku VARCHAR(100) UNIQUE NOT NULL,
    nome VARCHAR(255) NOT NULL,
    descricao TEXT,

    -- Preços em centavos (evita problemas de arredondamento)
    preco_centavos BIGINT NOT NULL CHECK (preco_centavos >= 0),
    custo_centavos BIGINT NOT NULL CHECK (custo_centavos >= 0),

    -- Relacionamentos
    categoria_id UUID REFERENCES categorias(id),
    produto_pai_id UUID REFERENCES produtos(id), -- Para variantes

    -- Controle
    ativo BOOLEAN DEFAULT true NOT NULL,
    data_criacao TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
    data_atualizacao TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL
);

CREATE INDEX idx_produtos_sku ON produtos(sku);
CREATE INDEX idx_produtos_categoria ON produtos(categoria_id);
CREATE INDEX idx_produtos_tipo ON produtos(tipo);
CREATE INDEX idx_produtos_ativo ON produtos(ativo) WHERE ativo = true;

COMMENT ON TABLE produtos IS 'Catálogo de produtos (simples, variantes, kits)';
COMMENT ON COLUMN produtos.preco_centavos IS 'Preço de venda em centavos';
COMMENT ON COLUMN produtos.custo_centavos IS 'Custo de aquisição em centavos';

-- ============================================================================
-- Table: estoque (Inventory)
-- ============================================================================
CREATE TABLE IF NOT EXISTS estoque (
    id UUID PRIMARY KEY,
    produto_id UUID UNIQUE NOT NULL REFERENCES produtos(id),

    -- Quantidades
    quantidade_disponivel INTEGER DEFAULT 0 NOT NULL CHECK (quantidade_disponivel >= 0),
    quantidade_reservada INTEGER DEFAULT 0 NOT NULL CHECK (quantidade_reservada >= 0),

    -- Custo médio ponderado em centavos
    custo_medio_ponderado_centavos BIGINT DEFAULT 0 NOT NULL,

    data_atualizacao TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL
);

CREATE INDEX idx_estoque_produto ON estoque(produto_id);

COMMENT ON TABLE estoque IS 'Controle de estoque com custo médio ponderado';
COMMENT ON COLUMN estoque.quantidade_disponivel IS 'Quantidade disponível para venda';
COMMENT ON COLUMN estoque.quantidade_reservada IS 'Quantidade reservada em pedidos';

-- ============================================================================
-- Table: movimentacoes_estoque (Inventory Movements)
-- ============================================================================
CREATE TABLE IF NOT EXISTS movimentacoes_estoque (
    id UUID PRIMARY KEY,
    produto_id UUID NOT NULL REFERENCES produtos(id),
    tipo VARCHAR(20) NOT NULL CHECK (tipo IN ('ENTRADA', 'SAIDA', 'AJUSTE', 'RESERVA', 'LIBERACAO')),
    quantidade INTEGER NOT NULL,
    custo_unitario_centavos BIGINT NOT NULL,

    -- Referências
    referencia_tipo VARCHAR(50), -- 'VENDA', 'COMPRA', 'AJUSTE_MANUAL'
    referencia_id UUID, -- ID da venda/compra

    -- Metadados
    usuario_id UUID REFERENCES usuarios(id),
    observacao TEXT,
    data_movimentacao TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL
);

CREATE INDEX idx_movimentacoes_produto ON movimentacoes_estoque(produto_id);
CREATE INDEX idx_movimentacoes_tipo ON movimentacoes_estoque(tipo);
CREATE INDEX idx_movimentacoes_data ON movimentacoes_estoque(data_movimentacao DESC);

COMMENT ON TABLE movimentacoes_estoque IS 'Histórico de todas as movimentações de estoque';

-- ============================================================================
-- Table: clientes (Customers)
-- ============================================================================
CREATE TABLE IF NOT EXISTS clientes (
    id UUID PRIMARY KEY,
    tipo VARCHAR(20) NOT NULL CHECK (tipo IN ('PF', 'PJ')), -- Pessoa Física / Jurídica
    nome VARCHAR(255) NOT NULL,
    cpf_cnpj VARCHAR(18) UNIQUE,
    email VARCHAR(255),
    telefone VARCHAR(20),

    -- Endereço
    logradouro VARCHAR(255),
    numero VARCHAR(20),
    complemento VARCHAR(100),
    bairro VARCHAR(100),
    cidade VARCHAR(100),
    estado VARCHAR(2),
    cep VARCHAR(9),

    ativo BOOLEAN DEFAULT true NOT NULL,
    data_criacao TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
    data_atualizacao TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL
);

CREATE INDEX idx_clientes_cpf_cnpj ON clientes(cpf_cnpj);
CREATE INDEX idx_clientes_email ON clientes(email);
CREATE INDEX idx_clientes_ativo ON clientes(ativo) WHERE ativo = true;

COMMENT ON TABLE clientes IS 'Cadastro de clientes (PF e PJ)';

-- ============================================================================
-- Table: vendas (Sales)
-- ============================================================================
CREATE TABLE IF NOT EXISTS vendas (
    id UUID PRIMARY KEY,
    numero VARCHAR(50) UNIQUE NOT NULL, -- Número sequencial por tenant
    tipo VARCHAR(20) NOT NULL CHECK (tipo IN ('PDV', 'B2B', 'B2C', 'MERCADO_LIVRE')),
    status VARCHAR(20) NOT NULL CHECK (status IN ('RASCUNHO', 'CONFIRMADA', 'PAGA', 'CANCELADA')),

    -- Cliente
    cliente_id UUID REFERENCES clientes(id),

    -- Valores em centavos
    subtotal_centavos BIGINT NOT NULL CHECK (subtotal_centavos >= 0),
    desconto_centavos BIGINT DEFAULT 0 NOT NULL CHECK (desconto_centavos >= 0),
    total_centavos BIGINT NOT NULL CHECK (total_centavos >= 0),

    -- Fiscal
    nfce_chave VARCHAR(44), -- Chave NFCe (44 dígitos)
    nfce_numero VARCHAR(20),
    nfce_serie VARCHAR(10),
    nfce_emitida_em TIMESTAMP,

    -- Metadados
    usuario_id UUID REFERENCES usuarios(id),
    observacoes TEXT,
    data_venda TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
    data_criacao TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
    data_atualizacao TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL
);

CREATE INDEX idx_vendas_numero ON vendas(numero);
CREATE INDEX idx_vendas_cliente ON vendas(cliente_id);
CREATE INDEX idx_vendas_status ON vendas(status);
CREATE INDEX idx_vendas_tipo ON vendas(tipo);
CREATE INDEX idx_vendas_data ON vendas(data_venda DESC);

COMMENT ON TABLE vendas IS 'Pedidos de venda multi-canal (PDV, B2B, B2C, Mercado Livre)';

-- ============================================================================
-- Table: itens_venda (Sale Items)
-- ============================================================================
CREATE TABLE IF NOT EXISTS itens_venda (
    id UUID PRIMARY KEY,
    venda_id UUID NOT NULL REFERENCES vendas(id) ON DELETE CASCADE,
    produto_id UUID NOT NULL REFERENCES produtos(id),

    quantidade INTEGER NOT NULL CHECK (quantidade > 0),
    preco_unitario_centavos BIGINT NOT NULL CHECK (preco_unitario_centavos >= 0),
    desconto_centavos BIGINT DEFAULT 0 NOT NULL CHECK (desconto_centavos >= 0),
    total_centavos BIGINT NOT NULL CHECK (total_centavos >= 0),

    data_criacao TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL
);

CREATE INDEX idx_itens_venda_venda ON itens_venda(venda_id);
CREATE INDEX idx_itens_venda_produto ON itens_venda(produto_id);

COMMENT ON TABLE itens_venda IS 'Itens de cada venda';

-- ============================================================================
-- Table: compras (Purchase Orders)
-- ============================================================================
CREATE TABLE IF NOT EXISTS compras (
    id UUID PRIMARY KEY,
    numero VARCHAR(50) UNIQUE NOT NULL,
    status VARCHAR(20) NOT NULL CHECK (status IN ('PENDENTE', 'PARCIAL', 'RECEBIDA', 'CANCELADA')),

    fornecedor_nome VARCHAR(255) NOT NULL,
    fornecedor_cnpj VARCHAR(18),

    total_centavos BIGINT NOT NULL CHECK (total_centavos >= 0),

    data_pedido TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
    data_recebimento TIMESTAMP,

    usuario_id UUID REFERENCES usuarios(id),
    observacoes TEXT,

    data_criacao TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
    data_atualizacao TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL
);

CREATE INDEX idx_compras_numero ON compras(numero);
CREATE INDEX idx_compras_status ON compras(status);
CREATE INDEX idx_compras_data ON compras(data_pedido DESC);

COMMENT ON TABLE compras IS 'Ordens de compra de fornecedores';

-- ============================================================================
-- Table: itens_compra (Purchase Order Items)
-- ============================================================================
CREATE TABLE IF NOT EXISTS itens_compra (
    id UUID PRIMARY KEY,
    compra_id UUID NOT NULL REFERENCES compras(id) ON DELETE CASCADE,
    produto_id UUID NOT NULL REFERENCES produtos(id),

    quantidade_pedida INTEGER NOT NULL CHECK (quantidade_pedida > 0),
    quantidade_recebida INTEGER DEFAULT 0 NOT NULL CHECK (quantidade_recebida >= 0),
    custo_unitario_centavos BIGINT NOT NULL CHECK (custo_unitario_centavos >= 0),
    total_centavos BIGINT NOT NULL CHECK (total_centavos >= 0),

    data_criacao TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL
);

CREATE INDEX idx_itens_compra_compra ON itens_compra(compra_id);
CREATE INDEX idx_itens_compra_produto ON itens_compra(produto_id);

COMMENT ON TABLE itens_compra IS 'Itens de cada ordem de compra';

-- ============================================================================
-- END OF MIGRATION
-- ============================================================================
