-- Story 4.3: NFCe Emission and Stock Decrease - Sales table
-- Tabela de vendas (PDV e B2B)

CREATE TABLE sales (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID NOT NULL,

    -- Sale identification
    sale_number VARCHAR(50) NOT NULL, -- SALE-YYYYMM-9999
    customer_id UUID NOT NULL,
    stock_location_id UUID,

    -- Payment
    payment_method VARCHAR(20) NOT NULL, -- DINHEIRO, DEBITO, CREDITO, PIX
    payment_amount_received NUMERIC(15, 2),
    change_amount NUMERIC(15, 2),

    -- Totals
    total_amount NUMERIC(15, 2) NOT NULL,
    discount NUMERIC(15, 2) DEFAULT 0,

    -- NFCe
    nfce_status VARCHAR(20) NOT NULL DEFAULT 'PENDING', -- PENDING, EMITTED, FAILED, CANCELLED
    nfce_key VARCHAR(44), -- Chave de acesso da NFCe (44 dígitos)
    nfce_xml TEXT, -- XML da NFCe
    nfce_error_message TEXT, -- Mensagem de erro se falhar

    -- Audit
    created_by_user_id UUID,
    sale_date TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    data_criacao TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    data_atualizacao TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    -- Constraints
    CONSTRAINT fk_sales_customer FOREIGN KEY (customer_id) REFERENCES customers(id),
    CONSTRAINT fk_sales_location FOREIGN KEY (stock_location_id) REFERENCES locations(id),
    CONSTRAINT unique_sale_number_per_tenant UNIQUE (tenant_id, sale_number)
);

-- Indexes
CREATE INDEX idx_sales_tenant_id ON sales(tenant_id);
CREATE INDEX idx_sales_sale_number ON sales(sale_number);
CREATE INDEX idx_sales_nfce_status ON sales(nfce_status);
CREATE INDEX idx_sales_customer_id ON sales(customer_id);
CREATE INDEX idx_sales_sale_date ON sales(sale_date);
CREATE INDEX idx_sales_created_by ON sales(created_by_user_id);

-- Comments
COMMENT ON TABLE sales IS 'Story 4.3: Vendas processadas via PDV ou B2B';
COMMENT ON COLUMN sales.sale_number IS 'Número sequencial da venda: SALE-YYYYMM-9999';
COMMENT ON COLUMN sales.nfce_status IS 'Status da emissão da NFCe: PENDING (aguardando), EMITTED (emitida), FAILED (falha), CANCELLED (cancelada)';
COMMENT ON COLUMN sales.nfce_key IS 'Chave de acesso da NFCe (44 dígitos) retornada pela SEFAZ';
