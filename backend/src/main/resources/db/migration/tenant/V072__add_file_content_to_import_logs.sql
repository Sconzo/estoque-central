-- V072: Add file_content column to import_logs for two-phase import
--
-- Stores the raw CSV content during preview so it can be re-parsed during confirm

ALTER TABLE import_logs ADD COLUMN file_content TEXT;

COMMENT ON COLUMN import_logs.file_content IS 'Raw CSV content stored during preview phase, cleared after confirm';
