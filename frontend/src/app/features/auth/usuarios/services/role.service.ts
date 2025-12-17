import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../../../environments/environment';
import { Role } from '../models/role.model';

/**
 * RoleService - Service for role management
 *
 * Handles communication with /api/roles endpoints
 */
@Injectable({
  providedIn: 'root'
})
export class RoleService {
  private readonly apiUrl = `${environment.apiUrl}/api/roles`;

  constructor(private http: HttpClient) {}

  /**
   * Lists all available roles
   *
   * @returns Observable of role array
   */
  listAll(): Observable<Role[]> {
    return this.http.get<Role[]>(this.apiUrl);
  }

  /**
   * Gets a role by ID
   *
   * @param id role ID
   * @returns Observable of role
   */
  getById(id: string): Observable<Role> {
    return this.http.get<Role>(`${this.apiUrl}/${id}`);
  }
}
