/**
 * Supplier Models - TypeScript interfaces for supplier management
 * Story 3.1: Supplier Management
 */

export enum SupplierType {
  INDIVIDUAL = 'INDIVIDUAL',
  BUSINESS = 'BUSINESS'
}

export enum SupplierStatus {
  ACTIVE = 'ACTIVE',
  INACTIVE = 'INACTIVE',
  BLOCKED = 'BLOCKED',
  PENDING_APPROVAL = 'PENDING_APPROVAL'
}

export enum TaxRegime {
  SIMPLES_NACIONAL = 'SIMPLES_NACIONAL',
  LUCRO_PRESUMIDO = 'LUCRO_PRESUMIDO',
  LUCRO_REAL = 'LUCRO_REAL',
  MEI = 'MEI',
  OUTROS = 'OUTROS'
}

export interface SupplierResponse {
  id: string;
  supplierCode: string;
  supplierType: SupplierType;

  // Business details (PJ)
  companyName: string;
  tradeName?: string;
  cnpj?: string;

  // Individual details (PF)
  firstName?: string;
  lastName?: string;
  cpf?: string;

  // Contact
  email?: string;
  phone?: string;
  mobile?: string;
  website?: string;

  // Address
  street?: string;
  number?: string;
  complement?: string;
  neighborhood?: string;
  city?: string;
  state?: string;
  postalCode?: string;
  country?: string;
  fullAddress?: string;

  // Fiscal
  stateRegistration?: string;
  municipalRegistration?: string;
  taxRegime?: TaxRegime;
  icmsTaxpayer?: boolean;

  // Bank
  bankName?: string;
  bankCode?: string;
  bankBranch?: string;
  bankAccount?: string;
  bankAccountType?: string;
  pixKey?: string;

  // Business
  paymentTerms?: string;
  defaultPaymentMethod?: string;
  creditLimit?: number;
  averageDeliveryDays?: number;
  minimumOrderValue?: number;

  // Classification
  status: SupplierStatus;
  supplierCategory?: string;
  rating?: number;
  isPreferred?: boolean;

  // Notes
  notes?: string;
  internalNotes?: string;

  // Audit
  createdAt: string;
  updatedAt: string;
  ativo: boolean;
}

export interface CreateSupplierRequest {
  supplierCode: string;
  supplierType: SupplierType;

  // Business details
  companyName: string;
  tradeName?: string;
  cnpj?: string;

  // Individual details
  firstName?: string;
  lastName?: string;
  cpf?: string;

  // Contact
  email?: string;
  phone?: string;
  mobile?: string;
  website?: string;

  // Address
  street?: string;
  number?: string;
  complement?: string;
  neighborhood?: string;
  city?: string;
  state?: string;
  postalCode?: string;
  country?: string;

  // Fiscal
  stateRegistration?: string;
  municipalRegistration?: string;
  taxRegime?: TaxRegime;
  icmsTaxpayer?: boolean;

  // Bank
  bankName?: string;
  bankCode?: string;
  bankBranch?: string;
  bankAccount?: string;
  bankAccountType?: string;
  pixKey?: string;

  // Business
  paymentTerms?: string;
  defaultPaymentMethod?: string;
  creditLimit?: number;
  averageDeliveryDays?: number;
  minimumOrderValue?: number;

  // Classification
  supplierCategory?: string;
  rating?: number;
  isPreferred?: boolean;

  // Notes
  notes?: string;
  internalNotes?: string;
}

export interface UpdateSupplierRequest extends CreateSupplierRequest {
  // Same fields as Create
}

export interface SupplierFilters {
  search?: string;
  status?: SupplierStatus;
  ativo?: boolean;
  page?: number;
  size?: number;
  sort?: string;
}

export interface ViaCepResponse {
  cep: string;
  logradouro: string;
  complemento: string;
  bairro: string;
  localidade: string;
  uf: string;
  erro?: boolean;
}

export interface AddressFromCep {
  street: string;
  neighborhood: string;
  city: string;
  state: string;
}

// Brazilian states
export const BRAZILIAN_STATES = [
  { value: 'AC', label: 'Acre' },
  { value: 'AL', label: 'Alagoas' },
  { value: 'AP', label: 'Amapá' },
  { value: 'AM', label: 'Amazonas' },
  { value: 'BA', label: 'Bahia' },
  { value: 'CE', label: 'Ceará' },
  { value: 'DF', label: 'Distrito Federal' },
  { value: 'ES', label: 'Espírito Santo' },
  { value: 'GO', label: 'Goiás' },
  { value: 'MA', label: 'Maranhão' },
  { value: 'MT', label: 'Mato Grosso' },
  { value: 'MS', label: 'Mato Grosso do Sul' },
  { value: 'MG', label: 'Minas Gerais' },
  { value: 'PA', label: 'Pará' },
  { value: 'PB', label: 'Paraíba' },
  { value: 'PR', label: 'Paraná' },
  { value: 'PE', label: 'Pernambuco' },
  { value: 'PI', label: 'Piauí' },
  { value: 'RJ', label: 'Rio de Janeiro' },
  { value: 'RN', label: 'Rio Grande do Norte' },
  { value: 'RS', label: 'Rio Grande do Sul' },
  { value: 'RO', label: 'Rondônia' },
  { value: 'RR', label: 'Roraima' },
  { value: 'SC', label: 'Santa Catarina' },
  { value: 'SP', label: 'São Paulo' },
  { value: 'SE', label: 'Sergipe' },
  { value: 'TO', label: 'Tocantins' }
];

// Supplier categories
export const SUPPLIER_CATEGORIES = [
  { value: 'ELECTRONICS', label: 'Eletrônicos' },
  { value: 'FOOD', label: 'Alimentos' },
  { value: 'CLOTHING', label: 'Vestuário' },
  { value: 'FURNITURE', label: 'Móveis' },
  { value: 'CONSTRUCTION', label: 'Construção' },
  { value: 'OFFICE', label: 'Material de Escritório' },
  { value: 'GENERAL', label: 'Geral' },
  { value: 'OTHER', label: 'Outros' }
];
