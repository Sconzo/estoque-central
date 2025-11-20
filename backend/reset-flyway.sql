-- Script to reset Flyway migration history
-- Run this if you have Flyway validation errors due to migration file changes

-- Drop flyway_schema_history from public schema
DROP TABLE IF EXISTS public.flyway_schema_history CASCADE;

-- Optionally, also clean tenant schemas if needed
-- You can uncomment and replace {schema_name} with actual tenant schema names
-- DROP TABLE IF EXISTS tenant_{uuid}.flyway_schema_history CASCADE;

-- After running this script, restart the application
-- Flyway will recreate the history table and run all migrations fresh
