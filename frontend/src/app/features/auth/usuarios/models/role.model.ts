/**
 * Role - Represents a system role/permission
 */
export interface Role {
  id: string;
  nome: string;
  descricao: string;
  categoria: string;
  ativo: boolean;
  dataCriacao: string;
}
