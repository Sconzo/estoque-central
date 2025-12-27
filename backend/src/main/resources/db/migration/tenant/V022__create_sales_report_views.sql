-- =====================================================
-- Migration V022: Sales Report Views
-- Story 6.3 - Relatório de Vendas por Período e Canal
-- =====================================================
-- Description: Creates views for sales reporting by period and channel
--              - Sales by period (day, week, month)
--              - Sales by channel with breakdown
--              - Sales performance trends
-- =====================================================

-- TODO: Views temporarily disabled - require columns that don't exist in current schema
-- The following columns are referenced but don't exist:
--   - orders.sales_channel (not implemented in V013)
--   - orders.total_items (calculated field, not stored)
--   - customers.full_name (use first_name + last_name or company_name instead)
--
-- These views should be reimplemented based on the actual schema structure
-- after basic tenant provisioning is working.

-- Placeholder to allow migration to complete
SELECT 1 AS placeholder;
