/**
 * Customer Type Enum
 * Story 4.1: Customer Management
 */
export enum CustomerType {
  INDIVIDUAL = 'INDIVIDUAL',
  BUSINESS = 'BUSINESS'
}

/**
 * Customer - Main customer entity (PF and PJ)
 * Story 4.1: Customer Management
 */
export interface Customer {
  id: string;
  tenantId: string;
  customerType: CustomerType;
  firstName?: string;
  lastName?: string;
  cpf?: string;
  companyName?: string;
  cnpj?: string;
  tradeName?: string;
  email?: string;
  phone?: string;
  mobile?: string;
  birthDate?: string;
  stateRegistration?: string;
  customerSegment?: string;
  loyaltyTier?: string;
  creditLimit?: number;
  acceptsMarketing?: boolean;
  preferredLanguage?: string;
  notes?: string;
  ativo: boolean;
  isDefaultConsumer?: boolean;
  createdAt?: string;
  updatedAt?: string;
  fullName?: string;
  displayName?: string;
}

/**
 * Customer Request DTO
 */
export interface CustomerRequest {
  customerType: CustomerType;
  firstName?: string;
  lastName?: string;
  cpf?: string;
  companyName?: string;
  cnpj?: string;
  tradeName?: string;
  email?: string;
  phone?: string;
  mobile?: string;
  birthDate?: string;
  stateRegistration?: string;
  customerSegment?: string;
  loyaltyTier?: string;
  creditLimit?: number;
  acceptsMarketing?: boolean;
  preferredLanguage?: string;
  notes?: string;
}

/**
 * Customer Quick DTO (for autocomplete)
 */
export interface CustomerQuick {
  id: string;
  customerType: CustomerType;
  cpf?: string;
  cnpj?: string;
  displayName: string;
  email?: string;
  phone?: string;
}

/**
 * Paginated response
 */
export interface PagedCustomers {
  content: Customer[];
  totalElements: number;
  totalPages: number;
  size: number;
  number: number;
}
