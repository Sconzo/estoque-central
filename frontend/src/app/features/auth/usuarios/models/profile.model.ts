import { Role } from './role.model';

/**
 * Profile - Represents a user profile with associated roles
 */
export interface Profile {
  id: string;
  tenantId: string;
  nome: string;
  descricao: string;
  ativo: boolean;
  dataCriacao: string;
  dataAtualizacao: string;
  roles?: Role[];
}

/**
 * ProfileCreateRequest - Request DTO for creating a new profile
 */
export interface ProfileCreateRequest {
  nome: string;
  descricao: string;
}

/**
 * ProfileUpdateRequest - Request DTO for updating a profile
 */
export interface ProfileUpdateRequest {
  nome: string;
  descricao: string;
}

/**
 * AssignRolesRequest - Request DTO for assigning roles to a profile
 */
export interface AssignRolesRequest {
  roleIds: string[];
}
