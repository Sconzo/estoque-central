-- V029: Create import_logs table for product CSV import tracking
--
-- Tracks CSV import operations with status and error details

CREATE TABLE IF NOT EXISTS import_logs (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID NOT NULL,
    user_id UUID NOT NULL,

    -- File metadata
    file_name VARCHAR(255) NOT NULL,

    -- Row statistics
    total_rows INTEGER NOT NULL DEFAULT 0,
    success_rows INTEGER NOT NULL DEFAULT 0,
    error_rows INTEGER NOT NULL DEFAULT 0,

    -- Status: PREVIEW, PROCESSING, COMPLETED, FAILED
    status VARCHAR(20) NOT NULL,

    -- Error details stored as JSONB (row-level errors)
    error_details JSONB,

    -- Timestamps
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    -- Constraints
    CONSTRAINT chk_import_status CHECK (status IN ('PREVIEW', 'PROCESSING', 'COMPLETED', 'FAILED')),
    CONSTRAINT chk_row_counts CHECK (
        total_rows >= 0 AND
        success_rows >= 0 AND
        error_rows >= 0 AND
        success_rows + error_rows <= total_rows
    )
);

-- Index for querying user's import history
CREATE INDEX idx_import_logs_user_created ON import_logs(user_id, created_at DESC);

-- Index for filtering by status
CREATE INDEX idx_import_logs_status ON import_logs(status);

-- Index for tenant isolation
CREATE INDEX idx_import_logs_tenant ON import_logs(tenant_id);

COMMENT ON TABLE import_logs IS 'Tracks product CSV import operations with row-level error details';
COMMENT ON COLUMN import_logs.error_details IS 'JSONB array of errors per row: [{row: 1, errors: ["SKU required"]}, ...]';
COMMENT ON COLUMN import_logs.status IS 'Import lifecycle: PREVIEW (validation only), PROCESSING (in-progress), COMPLETED (success), FAILED (system error)';
