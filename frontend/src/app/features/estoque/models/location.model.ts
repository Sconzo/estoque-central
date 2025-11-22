/**
 * LocationType - Type of stock location
 */
export enum LocationType {
  WAREHOUSE = 'WAREHOUSE',
  STORE = 'STORE',
  DISTRIBUTION_CENTER = 'DISTRIBUTION_CENTER',
  SUPPLIER = 'SUPPLIER',
  CUSTOMER = 'CUSTOMER',
  TRANSIT = 'TRANSIT',
  QUARANTINE = 'QUARANTINE'
}

/**
 * Location - Stock storage location
 */
export interface Location {
  id: string;
  tenantId: string;
  code: string;
  name: string;
  description?: string;
  type: LocationType;
  address?: string;
  city?: string;
  state?: string;
  postalCode?: string;
  country?: string;
  phone?: string;
  email?: string;
  managerName?: string;
  managerId?: string;
  isDefault?: boolean;
  allowNegativeStock?: boolean;
  ativo: boolean;
  createdAt: string;
  updatedAt: string;
}

/**
 * CreateLocationRequest - DTO for creating location
 */
export interface CreateLocationRequest {
  name: string;
  code: string;
  description?: string;
  type: LocationType;
  address?: string;
  city?: string;
  state?: string;
  postalCode?: string;
  country?: string;
  phone?: string;
  email?: string;
  managerId?: string;
}

/**
 * UpdateLocationRequest - DTO for updating location
 */
export interface UpdateLocationRequest {
  name?: string;
  description?: string;
  address?: string;
  city?: string;
  state?: string;
  postalCode?: string;
  country?: string;
  phone?: string;
  email?: string;
  managerId?: string;
}

/**
 * Location Type labels for UI
 */
export const LOCATION_TYPE_LABELS: Record<LocationType, string> = {
  [LocationType.WAREHOUSE]: 'Depósito',
  [LocationType.STORE]: 'Loja',
  [LocationType.DISTRIBUTION_CENTER]: 'Centro de Distribuição',
  [LocationType.SUPPLIER]: 'Fornecedor',
  [LocationType.CUSTOMER]: 'Cliente',
  [LocationType.TRANSIT]: 'Trânsito',
  [LocationType.QUARANTINE]: 'Quarentena'
};

/**
 * Location Type colors for UI
 */
export const LOCATION_TYPE_COLORS: Record<LocationType, string> = {
  [LocationType.WAREHOUSE]: 'primary',
  [LocationType.STORE]: 'success',
  [LocationType.DISTRIBUTION_CENTER]: 'info',
  [LocationType.SUPPLIER]: 'warning',
  [LocationType.CUSTOMER]: 'secondary',
  [LocationType.TRANSIT]: 'dark',
  [LocationType.QUARANTINE]: 'danger'
};
