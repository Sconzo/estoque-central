-- V064: Add status column to sales table
-- Story 5.6: Process Mercado Livre Cancellations - AC3

ALTER TABLE sales ADD COLUMN IF NOT EXISTS status VARCHAR(20) DEFAULT 'ACTIVE';

-- Add constraint to ensure valid values
ALTER TABLE sales ADD CONSTRAINT sales_status_check
    CHECK (status IN ('ACTIVE', 'CANCELLED'));

-- Create index for filtering by status
CREATE INDEX IF NOT EXISTS idx_sales_status ON sales(status);

-- Update existing sales to ACTIVE (default value)
UPDATE sales SET status = 'ACTIVE' WHERE status IS NULL;

-- Make column NOT NULL after setting defaults
ALTER TABLE sales ALTER COLUMN status SET NOT NULL;

COMMENT ON COLUMN sales.status IS 'Status da venda: ACTIVE (ativa) ou CANCELLED (cancelada)';
