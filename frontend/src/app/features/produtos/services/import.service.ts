import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import {
  ImportPreviewResponse,
  ImportConfirmResponse,
  ImportLog
} from '../models/import.model';
import { ProductType } from '../models/product.model';
import { environment } from '../../../../environments/environment';

/**
 * ImportService - HTTP client for product import API
 *
 * Provides methods for CSV import workflow:
 * 1. Upload and preview CSV
 * 2. Confirm import and persist products
 * 3. Download CSV templates
 * 4. Retrieve import logs
 */
@Injectable({
  providedIn: 'root'
})
export class ImportService {
  private readonly apiUrl = `${environment.apiUrl}/api/products`;

  constructor(private http: HttpClient) {}

  /**
   * Phase 1: Upload CSV and get preview with validation
   *
   * @param file CSV file
   * @param tenantId tenant ID
   * @returns Observable of preview response
   */
  preview(file: File, tenantId: string): Observable<ImportPreviewResponse> {
    const formData = new FormData();
    formData.append('file', file);
    formData.append('tenantId', tenantId);

    return this.http.post<ImportPreviewResponse>(
      `${this.apiUrl}/import/preview`,
      formData
    );
  }

  /**
   * Phase 2: Confirm import and persist valid products
   *
   * @param importLogId import log ID from preview
   * @param tenantId tenant ID
   * @returns Observable of confirm response
   */
  confirmImport(importLogId: string, tenantId: string): Observable<ImportConfirmResponse> {
    const params = new HttpParams()
      .set('importLogId', importLogId)
      .set('tenantId', tenantId);

    return this.http.post<ImportConfirmResponse>(
      `${this.apiUrl}/import/confirm`,
      null,
      { params }
    );
  }

  /**
   * Download CSV template for specific product type
   *
   * @param productType product type (SIMPLE, COMPOSITE, etc.)
   * @returns Observable of CSV template as blob
   */
  downloadTemplate(productType: ProductType): Observable<Blob> {
    const params = new HttpParams().set('type', productType);

    return this.http.get(
      `${this.apiUrl}/import/template`,
      {
        params,
        responseType: 'blob'
      }
    );
  }

  /**
   * Get import log details
   *
   * @param importLogId import log ID
   * @returns Observable of import log
   */
  getImportLog(importLogId: string): Observable<ImportLog> {
    return this.http.get<ImportLog>(`${this.apiUrl}/import-logs/${importLogId}`);
  }
}
