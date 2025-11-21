/**
 * User model representing authenticated user data.
 */
export interface User {
  id: string;
  email: string;
  nome: string;
  tenantId: string;
  profileId?: string;
  roles: string[];
  ativo: boolean;
  pictureUrl?: string;
}
