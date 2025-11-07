-- V014__create_payment_tables.sql
-- Story 4.4: Payment Integration
-- Creates tables for payment processing, transactions, and refunds

-- ============================================================
-- Table: payments
-- Stores payment transactions
-- ============================================================
CREATE TABLE IF NOT EXISTS payments (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID NOT NULL,

    -- Order reference
    order_id UUID NOT NULL REFERENCES orders(id),

    -- Payment identification
    payment_number VARCHAR(50) NOT NULL,
    external_payment_id VARCHAR(255),  -- Gateway payment ID (Stripe, PayPal, etc.)

    -- Payment details
    payment_method VARCHAR(50) NOT NULL,
    payment_provider VARCHAR(50) NOT NULL,

    -- Amount
    amount NUMERIC(15, 2) NOT NULL,
    currency VARCHAR(3) NOT NULL DEFAULT 'BRL',

    -- Status
    status VARCHAR(30) NOT NULL DEFAULT 'PENDING',

    -- Payment method details (card, pix, boleto, etc.)
    card_brand VARCHAR(50),
    card_last_digits VARCHAR(4),
    card_holder_name VARCHAR(200),

    pix_key VARCHAR(200),
    pix_qr_code TEXT,
    pix_qr_code_image_url TEXT,

    boleto_barcode VARCHAR(100),
    boleto_url TEXT,
    boleto_due_date DATE,

    -- Metadata
    gateway_response JSONB,
    payment_metadata JSONB,

    -- Important timestamps
    authorized_at TIMESTAMP,
    captured_at TIMESTAMP,
    cancelled_at TIMESTAMP,
    failed_at TIMESTAMP,
    expires_at TIMESTAMP,

    -- Failure details
    failure_code VARCHAR(100),
    failure_message TEXT,

    -- Audit fields
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by UUID,
    updated_by UUID,

    -- Constraints
    CONSTRAINT unique_payment_number_per_tenant UNIQUE (tenant_id, payment_number),
    CONSTRAINT check_payment_status CHECK (status IN (
        'PENDING', 'AUTHORIZED', 'CAPTURED', 'FAILED',
        'CANCELLED', 'REFUNDED', 'PARTIALLY_REFUNDED', 'EXPIRED'
    )),
    CONSTRAINT check_payment_method CHECK (payment_method IN (
        'CREDIT_CARD', 'DEBIT_CARD', 'PIX', 'BOLETO',
        'BANK_TRANSFER', 'PAYPAL', 'WALLET', 'CASH'
    )),
    CONSTRAINT check_positive_amount CHECK (amount > 0)
);

-- ============================================================
-- Table: payment_installments
-- Stores installment details for split payments
-- ============================================================
CREATE TABLE IF NOT EXISTS payment_installments (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID NOT NULL,

    -- Payment reference
    payment_id UUID NOT NULL REFERENCES payments(id) ON DELETE CASCADE,

    -- Installment details
    installment_number INTEGER NOT NULL,
    total_installments INTEGER NOT NULL,

    -- Amount
    amount NUMERIC(15, 2) NOT NULL,

    -- Status
    status VARCHAR(30) NOT NULL DEFAULT 'PENDING',

    -- Dates
    due_date DATE NOT NULL,
    paid_at TIMESTAMP,

    -- External reference
    external_installment_id VARCHAR(255),

    -- Audit fields
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    -- Constraints
    CONSTRAINT check_installment_number CHECK (
        installment_number > 0 AND
        installment_number <= total_installments
    ),
    CONSTRAINT check_installment_status CHECK (status IN (
        'PENDING', 'PAID', 'OVERDUE', 'CANCELLED'
    )),
    CONSTRAINT unique_installment_per_payment UNIQUE (payment_id, installment_number)
);

-- ============================================================
-- Table: payment_refunds
-- Stores refund transactions
-- ============================================================
CREATE TABLE IF NOT EXISTS payment_refunds (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID NOT NULL,

    -- Payment reference
    payment_id UUID NOT NULL REFERENCES payments(id),
    order_id UUID NOT NULL REFERENCES orders(id),

    -- Refund identification
    refund_number VARCHAR(50) NOT NULL,
    external_refund_id VARCHAR(255),  -- Gateway refund ID

    -- Amount
    amount NUMERIC(15, 2) NOT NULL,
    currency VARCHAR(3) NOT NULL DEFAULT 'BRL',

    -- Refund details
    reason VARCHAR(100),
    notes TEXT,

    -- Status
    status VARCHAR(30) NOT NULL DEFAULT 'PENDING',

    -- Gateway response
    gateway_response JSONB,

    -- Important timestamps
    requested_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    processed_at TIMESTAMP,
    failed_at TIMESTAMP,

    -- Failure details
    failure_code VARCHAR(100),
    failure_message TEXT,

    -- Audit fields
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by UUID,
    updated_by UUID,

    -- Constraints
    CONSTRAINT unique_refund_number_per_tenant UNIQUE (tenant_id, refund_number),
    CONSTRAINT check_refund_status CHECK (status IN (
        'PENDING', 'PROCESSING', 'COMPLETED', 'FAILED', 'CANCELLED'
    )),
    CONSTRAINT check_positive_refund_amount CHECK (amount > 0)
);

-- ============================================================
-- Table: payment_webhooks
-- Stores webhook events from payment gateways
-- ============================================================
CREATE TABLE IF NOT EXISTS payment_webhooks (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID NOT NULL,

    -- Webhook details
    provider VARCHAR(50) NOT NULL,
    event_type VARCHAR(100) NOT NULL,
    event_id VARCHAR(255),

    -- Related entities
    payment_id UUID REFERENCES payments(id),
    order_id UUID REFERENCES orders(id),

    -- Payload
    payload JSONB NOT NULL,
    headers JSONB,

    -- Processing
    status VARCHAR(30) NOT NULL DEFAULT 'PENDING',
    processed_at TIMESTAMP,
    processing_attempts INTEGER NOT NULL DEFAULT 0,
    last_processing_error TEXT,

    -- Audit fields
    received_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    -- Constraints
    CONSTRAINT check_webhook_status CHECK (status IN (
        'PENDING', 'PROCESSING', 'PROCESSED', 'FAILED', 'IGNORED'
    ))
);

-- ============================================================
-- Indexes
-- ============================================================

-- Payments indexes
CREATE INDEX idx_payments_tenant ON payments(tenant_id);
CREATE INDEX idx_payments_order ON payments(order_id);
CREATE INDEX idx_payments_status ON payments(status);
CREATE INDEX idx_payments_method ON payments(payment_method);
CREATE INDEX idx_payments_provider ON payments(payment_provider);
CREATE INDEX idx_payments_external_id ON payments(external_payment_id) WHERE external_payment_id IS NOT NULL;
CREATE INDEX idx_payments_created ON payments(created_at DESC);
CREATE INDEX idx_payments_expires ON payments(expires_at) WHERE expires_at IS NOT NULL AND status = 'PENDING';

-- Payment installments indexes
CREATE INDEX idx_payment_installments_tenant ON payment_installments(tenant_id);
CREATE INDEX idx_payment_installments_payment ON payment_installments(payment_id);
CREATE INDEX idx_payment_installments_status ON payment_installments(status);
CREATE INDEX idx_payment_installments_due_date ON payment_installments(due_date);

-- Payment refunds indexes
CREATE INDEX idx_payment_refunds_tenant ON payment_refunds(tenant_id);
CREATE INDEX idx_payment_refunds_payment ON payment_refunds(payment_id);
CREATE INDEX idx_payment_refunds_order ON payment_refunds(order_id);
CREATE INDEX idx_payment_refunds_status ON payment_refunds(status);
CREATE INDEX idx_payment_refunds_external_id ON payment_refunds(external_refund_id) WHERE external_refund_id IS NOT NULL;

-- Payment webhooks indexes
CREATE INDEX idx_payment_webhooks_tenant ON payment_webhooks(tenant_id);
CREATE INDEX idx_payment_webhooks_provider ON payment_webhooks(provider);
CREATE INDEX idx_payment_webhooks_event_type ON payment_webhooks(event_type);
CREATE INDEX idx_payment_webhooks_payment ON payment_webhooks(payment_id) WHERE payment_id IS NOT NULL;
CREATE INDEX idx_payment_webhooks_order ON payment_webhooks(order_id) WHERE order_id IS NOT NULL;
CREATE INDEX idx_payment_webhooks_status ON payment_webhooks(status);
CREATE INDEX idx_payment_webhooks_received ON payment_webhooks(received_at DESC);

-- ============================================================
-- Triggers for updated_at
-- ============================================================
CREATE TRIGGER trigger_payments_updated_at
    BEFORE UPDATE ON payments
    FOR EACH ROW
    EXECUTE FUNCTION update_inventory_timestamp();

CREATE TRIGGER trigger_payment_installments_updated_at
    BEFORE UPDATE ON payment_installments
    FOR EACH ROW
    EXECUTE FUNCTION update_inventory_timestamp();

CREATE TRIGGER trigger_payment_refunds_updated_at
    BEFORE UPDATE ON payment_refunds
    FOR EACH ROW
    EXECUTE FUNCTION update_inventory_timestamp();

-- ============================================================
-- Function: Generate unique payment number
-- ============================================================
CREATE OR REPLACE FUNCTION generate_payment_number(tenant_uuid UUID)
RETURNS VARCHAR AS $$
DECLARE
    current_year VARCHAR(4);
    payment_count INTEGER;
    payment_num VARCHAR(50);
BEGIN
    current_year := EXTRACT(YEAR FROM CURRENT_TIMESTAMP)::VARCHAR;

    -- Count payments in current year for this tenant
    SELECT COUNT(*) + 1
    INTO payment_count
    FROM payments
    WHERE tenant_id = tenant_uuid
      AND EXTRACT(YEAR FROM created_at) = EXTRACT(YEAR FROM CURRENT_TIMESTAMP);

    -- Format: PAY-2025-00001
    payment_num := 'PAY-' || current_year || '-' || LPAD(payment_count::TEXT, 5, '0');

    RETURN payment_num;
END;
$$ LANGUAGE plpgsql;

-- ============================================================
-- Function: Generate unique refund number
-- ============================================================
CREATE OR REPLACE FUNCTION generate_refund_number(tenant_uuid UUID)
RETURNS VARCHAR AS $$
DECLARE
    current_year VARCHAR(4);
    refund_count INTEGER;
    refund_num VARCHAR(50);
BEGIN
    current_year := EXTRACT(YEAR FROM CURRENT_TIMESTAMP)::VARCHAR;

    -- Count refunds in current year for this tenant
    SELECT COUNT(*) + 1
    INTO refund_count
    FROM payment_refunds
    WHERE tenant_id = tenant_uuid
      AND EXTRACT(YEAR FROM created_at) = EXTRACT(YEAR FROM CURRENT_TIMESTAMP);

    -- Format: REF-2025-00001
    refund_num := 'REF-' || current_year || '-' || LPAD(refund_count::TEXT, 5, '0');

    RETURN refund_num;
END;
$$ LANGUAGE plpgsql;

-- ============================================================
-- Function: Process payment authorization
-- ============================================================
CREATE OR REPLACE FUNCTION authorize_payment(payment_uuid UUID)
RETURNS VOID AS $$
BEGIN
    -- Update payment status
    UPDATE payments
    SET status = 'AUTHORIZED',
        authorized_at = CURRENT_TIMESTAMP,
        updated_at = CURRENT_TIMESTAMP
    WHERE id = payment_uuid
      AND status = 'PENDING';

    -- Update order payment status
    UPDATE orders
    SET payment_status = 'AUTHORIZED',
        updated_at = CURRENT_TIMESTAMP
    WHERE id = (SELECT order_id FROM payments WHERE id = payment_uuid);
END;
$$ LANGUAGE plpgsql;

-- ============================================================
-- Function: Process payment capture
-- ============================================================
CREATE OR REPLACE FUNCTION capture_payment(payment_uuid UUID)
RETURNS VOID AS $$
DECLARE
    order_uuid UUID;
BEGIN
    -- Update payment status
    UPDATE payments
    SET status = 'CAPTURED',
        captured_at = CURRENT_TIMESTAMP,
        updated_at = CURRENT_TIMESTAMP
    WHERE id = payment_uuid
      AND status IN ('PENDING', 'AUTHORIZED');

    -- Get order ID
    SELECT order_id INTO order_uuid
    FROM payments
    WHERE id = payment_uuid;

    -- Update order payment status and confirm order
    UPDATE orders
    SET payment_status = 'CAPTURED',
        paid_at = CURRENT_TIMESTAMP,
        status = CASE
            WHEN status = 'PENDING' THEN 'CONFIRMED'
            ELSE status
        END,
        updated_at = CURRENT_TIMESTAMP
    WHERE id = order_uuid;

    -- Add order status history if status changed
    INSERT INTO order_status_history (
        tenant_id, order_id, from_status, to_status, comment
    )
    SELECT
        o.tenant_id,
        o.id,
        'PENDING',
        'CONFIRMED',
        'Order confirmed after payment capture'
    FROM orders o
    WHERE o.id = order_uuid
      AND o.status = 'CONFIRMED';
END;
$$ LANGUAGE plpgsql;

-- ============================================================
-- Function: Process payment refund
-- ============================================================
CREATE OR REPLACE FUNCTION process_refund(
    payment_uuid UUID,
    refund_amount NUMERIC(15, 2),
    refund_reason VARCHAR(100) DEFAULT NULL,
    user_id UUID DEFAULT NULL
)
RETURNS UUID AS $$
DECLARE
    refund_uuid UUID;
    payment_amount NUMERIC(15, 2);
    total_refunded NUMERIC(15, 2);
    order_uuid UUID;
    tenant_uuid UUID;
    new_payment_status VARCHAR(30);
    new_order_status VARCHAR(30);
BEGIN
    -- Get payment details
    SELECT p.amount, p.order_id, p.tenant_id
    INTO payment_amount, order_uuid, tenant_uuid
    FROM payments p
    WHERE p.id = payment_uuid;

    IF payment_amount IS NULL THEN
        RAISE EXCEPTION 'Payment not found: %', payment_uuid;
    END IF;

    -- Calculate total already refunded
    SELECT COALESCE(SUM(amount), 0)
    INTO total_refunded
    FROM payment_refunds
    WHERE payment_id = payment_uuid
      AND status = 'COMPLETED';

    -- Validate refund amount
    IF (total_refunded + refund_amount) > payment_amount THEN
        RAISE EXCEPTION 'Refund amount exceeds payment amount';
    END IF;

    -- Create refund record
    refund_uuid := gen_random_uuid();

    INSERT INTO payment_refunds (
        id, tenant_id, payment_id, order_id,
        refund_number, amount, reason, status,
        created_by
    )
    VALUES (
        refund_uuid,
        tenant_uuid,
        payment_uuid,
        order_uuid,
        generate_refund_number(tenant_uuid),
        refund_amount,
        refund_reason,
        'COMPLETED',
        user_id
    );

    -- Update payment status
    total_refunded := total_refunded + refund_amount;

    IF total_refunded >= payment_amount THEN
        new_payment_status := 'REFUNDED';
        new_order_status := 'REFUNDED';
    ELSE
        new_payment_status := 'PARTIALLY_REFUNDED';
        new_order_status := NULL;  -- Don't change order status for partial refund
    END IF;

    UPDATE payments
    SET status = new_payment_status,
        updated_at = CURRENT_TIMESTAMP
    WHERE id = payment_uuid;

    -- Update order if full refund
    IF new_order_status IS NOT NULL THEN
        UPDATE orders
        SET payment_status = new_payment_status,
            status = new_order_status,
            updated_at = CURRENT_TIMESTAMP
        WHERE id = order_uuid;

        -- Add order status history
        INSERT INTO order_status_history (
            tenant_id, order_id, from_status, to_status,
            comment, changed_by
        )
        SELECT
            tenant_uuid,
            order_uuid,
            o.status,
            'REFUNDED',
            'Full refund processed',
            user_id
        FROM orders o
        WHERE o.id = order_uuid;
    END IF;

    RETURN refund_uuid;
END;
$$ LANGUAGE plpgsql;

-- ============================================================
-- Function: Expire old pending payments
-- ============================================================
CREATE OR REPLACE FUNCTION expire_old_payments()
RETURNS INTEGER AS $$
DECLARE
    expired_count INTEGER;
BEGIN
    UPDATE payments
    SET status = 'EXPIRED',
        updated_at = CURRENT_TIMESTAMP
    WHERE status = 'PENDING'
      AND expires_at IS NOT NULL
      AND expires_at < CURRENT_TIMESTAMP;

    GET DIAGNOSTICS expired_count = ROW_COUNT;
    RETURN expired_count;
END;
$$ LANGUAGE plpgsql;

-- ============================================================
-- View: Payment summary
-- ============================================================
CREATE OR REPLACE VIEW v_payment_summary AS
SELECT
    p.id AS payment_id,
    p.payment_number,
    p.external_payment_id,
    o.order_number,
    o.customer_id,
    CASE
        WHEN c.customer_type = 'INDIVIDUAL'
            THEN c.first_name || ' ' || c.last_name
        ELSE c.company_name
    END AS customer_name,
    p.payment_method,
    p.payment_provider,
    p.amount,
    p.currency,
    p.status,
    p.card_brand,
    p.card_last_digits,
    p.created_at AS payment_date,
    p.authorized_at,
    p.captured_at,
    p.cancelled_at,
    p.expires_at,
    COALESCE(
        (SELECT SUM(amount)
         FROM payment_refunds
         WHERE payment_id = p.id
           AND status = 'COMPLETED'),
        0
    ) AS total_refunded
FROM payments p
INNER JOIN orders o ON p.order_id = o.id
INNER JOIN customers c ON o.customer_id = c.id;

-- ============================================================
-- View: Pending payments requiring action
-- ============================================================
CREATE OR REPLACE VIEW v_pending_payments AS
SELECT
    p.id AS payment_id,
    p.payment_number,
    o.order_number,
    CASE
        WHEN c.customer_type = 'INDIVIDUAL'
            THEN c.first_name || ' ' || c.last_name
        ELSE c.company_name
    END AS customer_name,
    p.payment_method,
    p.amount,
    p.status,
    p.created_at,
    p.expires_at,
    EXTRACT(EPOCH FROM (CURRENT_TIMESTAMP - p.created_at)) / 3600 AS hours_since_created,
    CASE
        WHEN p.expires_at IS NOT NULL AND p.expires_at < CURRENT_TIMESTAMP
            THEN true
        ELSE false
    END AS is_expired,
    CASE
        WHEN p.status = 'PENDING' AND p.payment_method = 'PIX'
            THEN 'AWAITING_PIX_PAYMENT'
        WHEN p.status = 'PENDING' AND p.payment_method = 'BOLETO'
            THEN 'AWAITING_BOLETO_PAYMENT'
        WHEN p.status = 'AUTHORIZED'
            THEN 'READY_TO_CAPTURE'
        ELSE 'OTHER'
    END AS action_required
FROM payments p
INNER JOIN orders o ON p.order_id = o.id
INNER JOIN customers c ON o.customer_id = c.id
WHERE p.status IN ('PENDING', 'AUTHORIZED')
ORDER BY p.created_at ASC;

-- ============================================================
-- View: Refund summary
-- ============================================================
CREATE OR REPLACE VIEW v_refund_summary AS
SELECT
    r.id AS refund_id,
    r.refund_number,
    r.external_refund_id,
    p.payment_number,
    o.order_number,
    CASE
        WHEN c.customer_type = 'INDIVIDUAL'
            THEN c.first_name || ' ' || c.last_name
        ELSE c.company_name
    END AS customer_name,
    r.amount,
    r.currency,
    r.status,
    r.reason,
    r.requested_at,
    r.processed_at,
    p.payment_method,
    p.amount AS original_payment_amount
FROM payment_refunds r
INNER JOIN payments p ON r.payment_id = p.id
INNER JOIN orders o ON r.order_id = o.id
INNER JOIN customers c ON o.customer_id = c.id;

-- ============================================================
-- Initial data: Create sample payment
-- ============================================================

DO $$
DECLARE
    sample_order_id UUID;
    sample_tenant_id UUID;
    sample_order_total NUMERIC(15, 2);
    new_payment_id UUID;
    new_payment_number VARCHAR(50);
BEGIN
    -- Get the sample order
    SELECT o.id, o.tenant_id, o.total
    INTO sample_order_id, sample_tenant_id, sample_order_total
    FROM orders o
    WHERE o.order_number = 'ORD-2025-00001'
    LIMIT 1;

    IF sample_order_id IS NOT NULL THEN
        -- Generate payment number
        new_payment_number := generate_payment_number(sample_tenant_id);
        new_payment_id := gen_random_uuid();

        -- Create captured payment
        INSERT INTO payments (
            id, tenant_id, order_id, payment_number,
            external_payment_id, payment_method, payment_provider,
            amount, currency, status, card_brand, card_last_digits,
            authorized_at, captured_at
        )
        VALUES (
            new_payment_id,
            sample_tenant_id,
            sample_order_id,
            new_payment_number,
            'ch_stripe_' || substring(gen_random_uuid()::text, 1, 24),
            'CREDIT_CARD',
            'STRIPE',
            sample_order_total,
            'BRL',
            'CAPTURED',
            'Visa',
            '4242',
            CURRENT_TIMESTAMP - INTERVAL '5 minutes',
            CURRENT_TIMESTAMP
        );
    END IF;
END $$;
