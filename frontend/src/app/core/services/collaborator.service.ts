import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';

/**
 * Service for collaborator management operations.
 *
 * Supports Epic 10 (Collaborators and RBAC).
 *
 * @since 1.0
 */
@Injectable({
  providedIn: 'root'
})
export class CollaboratorService {
  private readonly apiUrl = `${environment.apiUrl}/api/companies`;

  constructor(private http: HttpClient) {}

  /**
   * Invites a user to join a company as a collaborator (Epic 10).
   */
  inviteCollaborator(companyId: string, request: InviteCollaboratorRequest): Observable<CollaboratorResponse> {
    return this.http.post<CollaboratorResponse>(
      `${this.apiUrl}/${companyId}/collaborators`,
      request
    );
  }

  /**
   * Lists all collaborators for a company (Epic 10).
   */
  listCollaborators(companyId: string): Observable<CollaboratorResponse[]> {
    return this.http.get<CollaboratorResponse[]>(`${this.apiUrl}/${companyId}/collaborators`);
  }

  /**
   * Removes a collaborator from a company (Epic 10).
   */
  removeCollaborator(companyId: string, userId: string): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${companyId}/collaborators/${userId}`);
  }

  /**
   * Promotes a collaborator to ADMIN role (Epic 10).
   */
  promoteToAdmin(companyId: string, userId: string): Observable<void> {
    return this.http.post<void>(
      `${this.apiUrl}/${companyId}/collaborators/${userId}/promote`,
      {}
    );
  }

  /**
   * Updates a collaborator's role (Epic 10).
   */
  updateRole(companyId: string, userId: string, role: string): Observable<void> {
    return this.http.put<void>(
      `${this.apiUrl}/${companyId}/collaborators/${userId}/role`,
      { role }
    );
  }
}

/**
 * Request interface for inviting a collaborator (Story 10.1 - AC2).
 * Matches backend InviteCollaboratorRequest.
 */
export interface InviteCollaboratorRequest {
  email: string;
  role: string;
}

/**
 * Response interface for collaborator details (Story 10.2 - AC1).
 * Matches backend CollaboratorDetailDTO with user information.
 */
export interface CollaboratorResponse {
  id: string;
  companyId: string;
  userId: string;
  userName: string;
  userEmail: string;
  role: string;
  invitedAt: string;
  acceptedAt: string | null;
  active: boolean;
}
