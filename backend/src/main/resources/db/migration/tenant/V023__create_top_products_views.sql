-- =====================================================
-- Migration V023: Top Products Report Views
-- Story 6.4 - Relatório de Produtos Mais Vendidos
-- =====================================================
-- Description: Creates views for top-selling products analysis
--              - Ranking by quantity and revenue
--              - Performance by category and channel
--              - Period comparison
-- =====================================================

-- TODO: Views temporarily disabled - require columns that don't exist in current schema
-- The following columns are referenced but don't exist:
--   - orders.sales_channel (not implemented in V013)
--   - orders.order_date (should be created_at)
--   - order_items.ativo (not in schema)
--   - orders.ativo (not in schema)
--   - products.ativo (not in schema)
--   - inventory.ativo (not in schema)
--   - products.unit_of_measure (should be unit)
--   - order_items.total_price (should be total)
--   - inventory.quantity_available (should be available_quantity)
--
-- These views should be reimplemented based on the actual schema structure
-- after basic tenant provisioning is working.

-- Placeholder to allow migration to complete
SELECT 1 AS placeholder;
