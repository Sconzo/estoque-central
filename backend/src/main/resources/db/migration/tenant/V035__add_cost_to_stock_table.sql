-- Story 3.4: Add cost column to inventory table for weighted average cost tracking
-- Migration: V035__add_cost_to_stock_table.sql

ALTER TABLE inventory ADD COLUMN IF NOT EXISTS cost DECIMAL(10,2) NOT NULL DEFAULT 0.00;

-- Comment
COMMENT ON COLUMN inventory.cost IS 'Weighted average cost of product at this location (Story 3.4)';

-- Create index for cost-based queries
CREATE INDEX IF NOT EXISTS idx_inventory_cost ON inventory(cost) WHERE cost > 0;
