import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';

/**
 * TenantSettingsService - Manages tenant-specific configuration settings
 * Story 4.6: Stock Reservation and Automatic Release - AC5
 */
export interface TenantSetting {
  id: string;
  tenantId: string;
  settingKey: string;
  settingValue: string;
  createdAt: string;
  updatedAt: string;
}

export interface AutoReleaseDaysResponse {
  days: number;
}

export interface UpdateAutoReleaseDaysRequest {
  days: number;
}

@Injectable({
  providedIn: 'root'
})
export class TenantSettingsService {
  private apiUrl = `${environment.apiUrl}/settings`;

  constructor(private http: HttpClient) {}

  /**
   * Get auto-release days setting for sales orders
   * GET /api/settings/sales-order-release-days
   */
  getAutoReleaseDays(): Observable<AutoReleaseDaysResponse> {
    return this.http.get<AutoReleaseDaysResponse>(`${this.apiUrl}/sales-order-release-days`);
  }

  /**
   * Update auto-release days setting for sales orders
   * PUT /api/settings/sales-order-release-days
   */
  updateAutoReleaseDays(days: number): Observable<{ message: string; days: string }> {
    return this.http.put<{ message: string; days: string }>(
      `${this.apiUrl}/sales-order-release-days`,
      { days }
    );
  }

  /**
   * Get all settings for current tenant
   * GET /api/settings
   */
  getAllSettings(): Observable<TenantSetting[]> {
    return this.http.get<TenantSetting[]>(this.apiUrl);
  }

  /**
   * Get a specific setting value
   * GET /api/settings/{key}
   */
  getSetting(key: string): Observable<{ key: string; value: string }> {
    return this.http.get<{ key: string; value: string }>(`${this.apiUrl}/${key}`);
  }

  /**
   * Set a setting value
   * PUT /api/settings/{key}
   */
  setSetting(key: string, value: string): Observable<{ message: string; key: string; value: string }> {
    return this.http.put<{ message: string; key: string; value: string }>(
      `${this.apiUrl}/${key}`,
      { value }
    );
  }

  /**
   * Delete a setting
   * DELETE /api/settings/{key}
   */
  deleteSetting(key: string): Observable<{ message: string; key: string }> {
    return this.http.delete<{ message: string; key: string }>(`${this.apiUrl}/${key}`);
  }
}
