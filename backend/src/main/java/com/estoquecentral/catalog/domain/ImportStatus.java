package com.estoquecentral.catalog.domain;

/**
 * ImportStatus - Status of a CSV import operation
 *
 * <p>Import lifecycle:
 * <ol>
 *   <li>PREVIEW - File uploaded and validated, preview shown to user</li>
 *   <li>PROCESSING - User confirmed import, records being persisted</li>
 *   <li>COMPLETED - All valid records persisted successfully</li>
 *   <li>FAILED - System error during import (e.g., database connection lost)</li>
 * </ol>
 */
public enum ImportStatus {
    /**
     * PREVIEW - File validated, preview shown to user
     * No data persisted yet. User can review errors before confirming.
     */
    PREVIEW,

    /**
     * PROCESSING - Import in progress
     * Records are being persisted to database.
     */
    PROCESSING,

    /**
     * COMPLETED - Import finished successfully
     * All valid records have been persisted. Some rows may have been skipped due to validation errors.
     */
    COMPLETED,

    /**
     * FAILED - System error during import
     * Unexpected error (database failure, etc.). No records were persisted.
     */
    FAILED
}
