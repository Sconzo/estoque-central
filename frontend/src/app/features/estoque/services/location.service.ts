import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import {
  Location,
  CreateLocationRequest,
  UpdateLocationRequest
} from '../models/location.model';
import { environment } from '../../../../environments/environment';

/**
 * LocationService - HTTP client for stock location API
 *
 * Provides methods for stock location management:
 * - CRUD operations
 * - Stock allocation validation
 * - List with filters
 */
@Injectable({
  providedIn: 'root'
})
export class LocationService {
  private readonly apiUrl = `${environment.apiUrl}/api/stock-locations`;

  constructor(private http: HttpClient) {}

  /**
   * Creates new location
   *
   * @param request location data
   * @param tenantId tenant ID
   * @returns Observable of created location
   */
  create(request: CreateLocationRequest, tenantId: string): Observable<Location> {
    const params = new HttpParams().set('tenantId', tenantId);
    return this.http.post<Location>(this.apiUrl, request, { params });
  }

  /**
   * Lists all locations
   *
   * @param tenantId tenant ID
   * @param includeInactive if true, includes inactive locations
   * @returns Observable of location list
   */
  listAll(tenantId: string, includeInactive: boolean = false): Observable<Location[]> {
    const params = new HttpParams()
      .set('tenantId', tenantId)
      .set('includeInactive', includeInactive.toString());

    return this.http.get<Location[]>(this.apiUrl, { params });
  }

  /**
   * Gets location by ID
   *
   * @param id location ID
   * @returns Observable of location
   */
  getById(id: string): Observable<Location> {
    return this.http.get<Location>(`${this.apiUrl}/${id}`);
  }

  /**
   * Updates location
   *
   * @param id location ID
   * @param request updated data
   * @returns Observable of updated location
   */
  update(id: string, request: UpdateLocationRequest): Observable<Location> {
    return this.http.put<Location>(`${this.apiUrl}/${id}`, request);
  }

  /**
   * Soft deletes location (marks as inactive)
   *
   * @param id location ID
   * @returns Observable of void
   */
  delete(id: string): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${id}`);
  }

  /**
   * Checks if location has allocated stock
   *
   * @param id location ID
   * @returns Observable of boolean
   */
  hasStock(id: string): Observable<boolean> {
    return this.http.get<boolean>(`${this.apiUrl}/${id}/has-stock`);
  }
}
