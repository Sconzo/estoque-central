import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../../environments/environment';
import {
  DashboardSummary,
  CriticalStockProduct,
  TopProduct,
  MonthlySales,
  RecentActivity
} from '../../../shared/models/dashboard.model';

@Injectable({
  providedIn: 'root'
})
export class DashboardService {
  private http = inject(HttpClient);
  private apiUrl = `${environment.apiUrl}/api/dashboard`;

  getSummary(): Observable<DashboardSummary> {
    return this.http.get<DashboardSummary>(`${this.apiUrl}/summary`);
  }

  getCriticalStock(limit?: number): Observable<CriticalStockProduct[]> {
    let params = new HttpParams();
    if (limit) {
      params = params.set('limit', limit.toString());
    }
    return this.http.get<CriticalStockProduct[]>(`${this.apiUrl}/stock/critical`, { params });
  }

  getTotalActiveProducts(): Observable<{ count: number }> {
    return this.http.get<{ count: number }>(`${this.apiUrl}/products/count`);
  }

  getTopProducts(limit?: number): Observable<TopProduct[]> {
    let params = new HttpParams();
    if (limit) {
      params = params.set('limit', limit.toString());
    }
    return this.http.get<TopProduct[]>(`${this.apiUrl}/products/top`, { params });
  }

  getMonthlySales(): Observable<MonthlySales> {
    return this.http.get<MonthlySales>(`${this.apiUrl}/sales/monthly`);
  }

  getActiveCustomersCount(): Observable<{ count: number }> {
    return this.http.get<{ count: number }>(`${this.apiUrl}/customers/active-count`);
  }

  getRecentActivities(limit?: number): Observable<RecentActivity[]> {
    let params = new HttpParams();
    if (limit) {
      params = params.set('limit', limit.toString());
    }
    return this.http.get<RecentActivity[]>(`${this.apiUrl}/activities/recent`, { params });
  }
}
