-- Apply all pending tenant migrations
-- Schema: tenant_00000000_0000_0000_0000_000000000000

SET search_path TO tenant_00000000_0000_0000_0000_000000000000, public;
SET client_min_messages TO WARNING;

-- V004: Update usuarios
\ir backend/src/main/resources/db/migration/tenant/V004__update_usuarios_add_profile.sql

-- V005: Categories
\ir backend/src/main/resources/db/migration/tenant/V005__create_categories_table.sql

-- V006: Products
\ir backend/src/main/resources/db/migration/tenant/V006__create_products_table.sql

-- V007: Inventory
\ir backend/src/main/resources/db/migration/tenant/V007__create_inventory_tables.sql

-- V008: Product variants
\ir backend/src/main/resources/db/migration/tenant/V008__create_product_variants_tables.sql

-- V009: Locations
\ir backend/src/main/resources/db/migration/tenant/V009__create_locations_table.sql

-- V010: Alerts
\ir backend/src/main/resources/db/migration/tenant/V010__create_alerts_and_notifications.sql

-- V011: Customers
\ir backend/src/main/resources/db/migration/tenant/V011__create_customers_tables.sql

-- V012: Cart
\ir backend/src/main/resources/db/migration/tenant/V012__create_cart_tables.sql

-- V013: Orders
\ir backend/src/main/resources/db/migration/tenant/V013__create_orders_tables.sql

-- V014: Payment
\ir backend/src/main/resources/db/migration/tenant/V014__create_payment_tables.sql

-- V015: Suppliers
\ir backend/src/main/resources/db/migration/tenant/V015__create_suppliers_tables.sql

-- V016: Purchase orders
\ir backend/src/main/resources/db/migration/tenant/V016__create_purchase_orders_tables.sql

-- V020: Dashboard views
\ir backend/src/main/resources/db/migration/tenant/V020__create_dashboard_views.sql

-- V027: Product components
\ir backend/src/main/resources/db/migration/tenant/V027__create_product_components_table.sql

-- V028: BOM type
\ir backend/src/main/resources/db/migration/tenant/V028__add_bom_type_to_products.sql

-- V029: Import logs
\ir backend/src/main/resources/db/migration/tenant/V029__create_import_logs_table.sql

-- V061: Marketplace listings (already created)
-- \ir backend/src/main/resources/db/migration/tenant/V061__create_marketplace_listings_table.sql

-- V062: Marketplace sync logs
\ir backend/src/main/resources/db/migration/tenant/V062__create_marketplace_sync_logs_table.sql

-- V063: Marketplace sync queue
\ir backend/src/main/resources/db/migration/tenant/V063__create_marketplace_sync_queue_table.sql

-- V065: Safety margin rules
\ir backend/src/main/resources/db/migration/tenant/V065__create_safety_margin_rules_table.sql

-- Done
SELECT 'Migration completed!' as status;
SELECT COUNT(*) as total_tables FROM pg_tables WHERE schemaname = 'tenant_00000000_0000_0000_0000_000000000000';
