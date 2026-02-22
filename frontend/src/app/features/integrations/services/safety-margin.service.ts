import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../../environments/environment';

/**
 * SafetyMarginService - API client for Safety Margin configuration
 * Story 5.7: Configurable Safety Stock Margin
 */

export interface SafetyMarginRule {
  id: string;
  marketplace: string;
  priority: 'PRODUCT' | 'CATEGORY' | 'GLOBAL';
  productId?: string;
  productName?: string;
  categoryId?: string;
  categoryName?: string;
  marginPercentage: number;
  createdAt: string;
  updatedAt: string;
}

export interface CreateSafetyMarginRuleRequest {
  marketplace: string;
  priority: 'PRODUCT' | 'CATEGORY' | 'GLOBAL';
  productId?: string;
  categoryId?: string;
  marginPercentage: number;
}

export interface UpdateSafetyMarginRuleRequest {
  marginPercentage: number;
}

@Injectable({
  providedIn: 'root'
})
export class SafetyMarginService {
  private http = inject(HttpClient);
  private apiUrl = `${environment.apiUrl}/api/safety-margins`;

  /**
   * List all safety margin rules
   * AC4: GET /api/safety-margins
   */
  listRules(marketplace?: string): Observable<SafetyMarginRule[]> {
    let params = new HttpParams();
    if (marketplace) {
      params = params.set('marketplace', marketplace);
    }
    return this.http.get<SafetyMarginRule[]>(this.apiUrl, { params });
  }

  /**
   * Create new safety margin rule
   * AC3: POST /api/safety-margins
   */
  createRule(request: CreateSafetyMarginRuleRequest): Observable<SafetyMarginRule> {
    return this.http.post<SafetyMarginRule>(this.apiUrl, request);
  }

  /**
   * Update safety margin rule
   * AC5: PUT /api/safety-margins/{id}
   */
  updateRule(id: string, request: UpdateSafetyMarginRuleRequest): Observable<SafetyMarginRule> {
    return this.http.put<SafetyMarginRule>(`${this.apiUrl}/${id}`, request);
  }

  /**
   * Delete safety margin rule
   * AC6: DELETE /api/safety-margins/{id}
   */
  deleteRule(id: string): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${id}`);
  }
}
