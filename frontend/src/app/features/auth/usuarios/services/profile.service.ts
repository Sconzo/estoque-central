import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../../../environments/environment';
import { Profile, ProfileCreateRequest, ProfileUpdateRequest, AssignRolesRequest } from '../models/profile.model';
import { Role } from '../models/role.model';

/**
 * ProfileService - Service for profile management
 *
 * Handles communication with /api/profiles endpoints
 */
@Injectable({
  providedIn: 'root'
})
export class ProfileService {
  private readonly apiUrl = `${environment.apiUrl}/api/profiles`;

  constructor(private http: HttpClient) {}

  /**
   * Lists all profiles for the current tenant
   *
   * @returns Observable of profile array
   */
  listAll(): Observable<Profile[]> {
    return this.http.get<Profile[]>(this.apiUrl);
  }

  /**
   * Gets a profile by ID with roles
   *
   * @param id profile ID
   * @returns Observable of profile with roles
   */
  getById(id: string): Observable<Profile> {
    return this.http.get<Profile>(`${this.apiUrl}/${id}`);
  }

  /**
   * Gets roles for a specific profile
   *
   * @param id profile ID
   * @returns Observable of role array
   */
  getRoles(id: string): Observable<Role[]> {
    return this.http.get<Role[]>(`${this.apiUrl}/${id}/roles`);
  }

  /**
   * Creates a new profile
   *
   * @param request profile creation data
   * @returns Observable of created profile
   */
  create(request: ProfileCreateRequest): Observable<Profile> {
    return this.http.post<Profile>(this.apiUrl, request);
  }

  /**
   * Updates a profile
   *
   * @param id profile ID
   * @param request update data
   * @returns Observable of updated profile
   */
  update(id: string, request: ProfileUpdateRequest): Observable<Profile> {
    return this.http.put<Profile>(`${this.apiUrl}/${id}`, request);
  }

  /**
   * Assigns roles to a profile
   *
   * @param id profile ID
   * @param request role assignment data
   * @returns Observable of updated profile
   */
  assignRoles(id: string, request: AssignRolesRequest): Observable<Profile> {
    return this.http.put<Profile>(`${this.apiUrl}/${id}/roles`, request);
  }

  /**
   * Deactivates a profile (soft delete)
   *
   * @param id profile ID
   * @returns Observable of void
   */
  delete(id: string): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${id}`);
  }
}
