/**
 * Request payload for Google OAuth callback.
 */
export interface GoogleCallbackRequest {
  idToken: string;
  tenantId: string;
}
