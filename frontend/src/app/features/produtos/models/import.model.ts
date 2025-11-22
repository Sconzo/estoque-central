/**
 * ImportStatus - Status of CSV import operation
 */
export enum ImportStatus {
  PREVIEW = 'PREVIEW',
  PROCESSING = 'PROCESSING',
  COMPLETED = 'COMPLETED',
  FAILED = 'FAILED'
}

/**
 * ProductCsvRow - Represents a single CSV row with validation
 */
export interface ProductCsvRow {
  rowNumber: number;

  // CSV fields
  type: string;
  name: string;
  sku: string;
  barcode?: string;
  description?: string;
  categoryId: string;
  price: number;
  cost?: number;
  unit?: string;
  controlsInventory?: boolean;
  status?: string;
  bomType?: string;

  // Validation
  errors: string[];
  valid: boolean;
}

/**
 * ImportPreviewResponse - Response from preview endpoint
 */
export interface ImportPreviewResponse {
  importLogId: string;
  fileName: string;
  rows: ProductCsvRow[];
  totalRows: number;
  validRows: number;
  invalidRows: number;
}

/**
 * ImportConfirmResponse - Response from confirm endpoint
 */
export interface ImportConfirmResponse {
  importLogId: string;
  totalRows: number;
  successRows: number;
  errorRows: number;
  status: string;
  message: string;
}

/**
 * ImportLog - Import operation log
 */
export interface ImportLog {
  id: string;
  tenantId: string;
  userId: string;
  fileName: string;
  totalRows: number;
  successRows: number;
  errorRows: number;
  status: ImportStatus;
  errorDetails?: string;
  createdAt: string;
}

/**
 * Import Status labels for UI
 */
export const IMPORT_STATUS_LABELS: Record<ImportStatus, string> = {
  [ImportStatus.PREVIEW]: 'Prévia',
  [ImportStatus.PROCESSING]: 'Processando',
  [ImportStatus.COMPLETED]: 'Concluído',
  [ImportStatus.FAILED]: 'Falhou'
};

/**
 * Import Status colors for UI
 */
export const IMPORT_STATUS_COLORS: Record<ImportStatus, string> = {
  [ImportStatus.PREVIEW]: 'info',
  [ImportStatus.PROCESSING]: 'warning',
  [ImportStatus.COMPLETED]: 'success',
  [ImportStatus.FAILED]: 'danger'
};
