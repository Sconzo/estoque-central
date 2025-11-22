import { Component, OnInit, inject, signal, computed } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { MatDialogRef, MAT_DIALOG_DATA, MatDialogModule } from '@angular/material/dialog';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { MatButtonModule } from '@angular/material/button';
import { MatRadioModule } from '@angular/material/radio';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { SupplierService } from '../services/supplier.service';
import { CepService } from '../../../shared/services/cep.service';
import { cnpjValidator } from '../../../shared/validators/cnpj.validator';
import { cpfValidator } from '../../../shared/validators/cpf.validator';
import {
  SupplierType,
  SupplierResponse,
  CreateSupplierRequest,
  TaxRegime,
  BRAZILIAN_STATES,
  SUPPLIER_CATEGORIES
} from '../../../shared/models/supplier.model';

/**
 * SupplierFormComponent - Form for creating/editing suppliers
 * Story 3.1: Supplier Management - AC5, AC6
 */
@Component({
  selector: 'app-supplier-form',
  standalone: true,
  imports: [
    CommonModule,
    ReactiveFormsModule,
    MatDialogModule,
    MatFormFieldModule,
    MatInputModule,
    MatSelectModule,
    MatButtonModule,
    MatRadioModule,
    MatSnackBarModule,
    MatProgressSpinnerModule
  ],
  templateUrl: './supplier-form.component.html',
  styleUrls: ['./supplier-form.component.css']
})
export class SupplierFormComponent implements OnInit {
  private fb = inject(FormBuilder);
  private dialogRef = inject(MatDialogRef<SupplierFormComponent>);
  private data = inject(MAT_DIALOG_DATA, { optional: true });
  private supplierService = inject(SupplierService);
  private cepService = inject(CepService);
  private snackBar = inject(MatSnackBar);

  supplierForm!: FormGroup;
  loading = signal(false);
  searchingCep = signal(false);
  isEditMode = signal(false);
  supplierId?: string;

  // Enums and constants
  SupplierType = SupplierType;
  brazilianStates = BRAZILIAN_STATES;
  supplierCategories = SUPPLIER_CATEGORIES;
  taxRegimes = [
    { value: TaxRegime.SIMPLES_NACIONAL, label: 'Simples Nacional' },
    { value: TaxRegime.LUCRO_PRESUMIDO, label: 'Lucro Presumido' },
    { value: TaxRegime.LUCRO_REAL, label: 'Lucro Real' },
    { value: TaxRegime.MEI, label: 'MEI' },
    { value: TaxRegime.OUTROS, label: 'Outros' }
  ];

  // Computed signals
  selectedType = computed(() => {
    return this.supplierForm?.get('supplierType')?.value as SupplierType;
  });

  ngOnInit(): void {
    this.initForm();

    if (this.data?.supplier) {
      this.isEditMode.set(true);
      this.supplierId = this.data.supplier.id;
      this.loadSupplierData(this.data.supplier);
    }

    // Listen to supplier type changes to update validators
    this.supplierForm.get('supplierType')?.valueChanges.subscribe((type) => {
      this.updateDocumentValidators(type);
    });

    // Listen to CEP changes for auto-fill
    this.supplierForm.get('postalCode')?.valueChanges.subscribe((cep) => {
      if (cep && cep.replace(/\D/g, '').length === 8) {
        this.searchAddressByCep(cep);
      }
    });
  }

  initForm(): void {
    this.supplierForm = this.fb.group({
      // Basic info
      supplierCode: ['', [Validators.required, Validators.maxLength(50)]],
      supplierType: [SupplierType.BUSINESS, Validators.required],

      // Business (PJ)
      cnpj: [''],
      companyName: ['', [Validators.required, Validators.maxLength(200)]],
      tradeName: ['', Validators.maxLength(200)],
      stateRegistration: ['', Validators.maxLength(50)],
      municipalRegistration: ['', Validators.maxLength(50)],

      // Individual (PF)
      cpf: [''],
      firstName: ['', Validators.maxLength(100)],
      lastName: ['', Validators.maxLength(100)],

      // Contact
      email: ['', [Validators.email, Validators.maxLength(200)]],
      phone: ['', Validators.maxLength(20)],
      mobile: ['', Validators.maxLength(20)],
      website: ['', Validators.maxLength(255)],

      // Address
      postalCode: [''],
      street: ['', Validators.maxLength(255)],
      number: ['', Validators.maxLength(20)],
      complement: ['', Validators.maxLength(100)],
      neighborhood: ['', Validators.maxLength(100)],
      city: ['', Validators.maxLength(100)],
      state: [''],
      country: ['Brasil'],

      // Fiscal
      taxRegime: [''],
      icmsTaxpayer: [true],

      // Bank
      bankName: ['', Validators.maxLength(100)],
      bankCode: ['', Validators.maxLength(10)],
      bankBranch: ['', Validators.maxLength(20)],
      bankAccount: ['', Validators.maxLength(30)],
      bankAccountType: [''],
      pixKey: ['', Validators.maxLength(200)],

      // Business details
      paymentTerms: ['', Validators.maxLength(100)],
      defaultPaymentMethod: [''],
      creditLimit: [null],
      averageDeliveryDays: [null, Validators.min(0)],
      minimumOrderValue: [null],

      // Classification
      supplierCategory: [''],
      rating: [null, [Validators.min(1), Validators.max(5)]],
      isPreferred: [false],

      // Notes
      notes: [''],
      internalNotes: ['']
    });

    // Set initial validators for BUSINESS type
    this.updateDocumentValidators(SupplierType.BUSINESS);
  }

  updateDocumentValidators(type: SupplierType): void {
    const cnpjControl = this.supplierForm.get('cnpj');
    const cpfControl = this.supplierForm.get('cpf');
    const companyNameControl = this.supplierForm.get('companyName');
    const firstNameControl = this.supplierForm.get('firstName');

    if (type === SupplierType.BUSINESS) {
      // PJ: CNPJ required
      cnpjControl?.setValidators([Validators.required, cnpjValidator()]);
      cpfControl?.clearValidators();
      companyNameControl?.setValidators([Validators.required, Validators.maxLength(200)]);
      firstNameControl?.clearValidators();
    } else {
      // PF: CPF required
      cpfControl?.setValidators([Validators.required, cpfValidator()]);
      cnpjControl?.clearValidators();
      firstNameControl?.setValidators([Validators.required, Validators.maxLength(100)]);
      companyNameControl?.setValidators([Validators.required, Validators.maxLength(200)]);
    }

    cnpjControl?.updateValueAndValidity();
    cpfControl?.updateValueAndValidity();
    companyNameControl?.updateValueAndValidity();
    firstNameControl?.updateValueAndValidity();
  }

  searchAddressByCep(cep: string): void {
    this.searchingCep.set(true);

    this.cepService.searchCep(cep).subscribe({
      next: (address) => {
        this.supplierForm.patchValue({
          street: address.street,
          neighborhood: address.neighborhood,
          city: address.city,
          state: address.state
        });
        this.searchingCep.set(false);
        this.snackBar.open('Endereço encontrado!', 'Fechar', { duration: 2000 });
      },
      error: (error) => {
        this.searchingCep.set(false);
        this.snackBar.open(error.message || 'Erro ao buscar CEP', 'Fechar', { duration: 3000 });
      }
    });
  }

  loadSupplierData(supplier: SupplierResponse): void {
    this.supplierForm.patchValue({
      supplierCode: supplier.supplierCode,
      supplierType: supplier.supplierType,
      cnpj: supplier.cnpj,
      companyName: supplier.companyName,
      tradeName: supplier.tradeName,
      stateRegistration: supplier.stateRegistration,
      municipalRegistration: supplier.municipalRegistration,
      cpf: supplier.cpf,
      firstName: supplier.firstName,
      lastName: supplier.lastName,
      email: supplier.email,
      phone: supplier.phone,
      mobile: supplier.mobile,
      website: supplier.website,
      postalCode: supplier.postalCode,
      street: supplier.street,
      number: supplier.number,
      complement: supplier.complement,
      neighborhood: supplier.neighborhood,
      city: supplier.city,
      state: supplier.state,
      country: supplier.country,
      taxRegime: supplier.taxRegime,
      icmsTaxpayer: supplier.icmsTaxpayer,
      bankName: supplier.bankName,
      bankCode: supplier.bankCode,
      bankBranch: supplier.bankBranch,
      bankAccount: supplier.bankAccount,
      bankAccountType: supplier.bankAccountType,
      pixKey: supplier.pixKey,
      paymentTerms: supplier.paymentTerms,
      defaultPaymentMethod: supplier.defaultPaymentMethod,
      creditLimit: supplier.creditLimit,
      averageDeliveryDays: supplier.averageDeliveryDays,
      minimumOrderValue: supplier.minimumOrderValue,
      supplierCategory: supplier.supplierCategory,
      rating: supplier.rating,
      isPreferred: supplier.isPreferred,
      notes: supplier.notes,
      internalNotes: supplier.internalNotes
    });
  }

  onSubmit(): void {
    if (!this.supplierForm.valid) {
      this.snackBar.open('Preencha todos os campos obrigatórios', 'Fechar', { duration: 3000 });
      return;
    }

    this.loading.set(true);
    const request: CreateSupplierRequest = this.supplierForm.value;

    const operation = this.isEditMode()
      ? this.supplierService.updateSupplier(this.supplierId!, request)
      : this.supplierService.createSupplier(request);

    operation.subscribe({
      next: (response) => {
        this.loading.set(false);
        const message = this.isEditMode()
          ? 'Fornecedor atualizado com sucesso!'
          : 'Fornecedor criado com sucesso!';
        this.snackBar.open(message, 'Fechar', { duration: 3000 });
        this.dialogRef.close(response);
      },
      error: (error) => {
        this.loading.set(false);
        console.error('Error saving supplier:', error);
        this.snackBar.open('Erro ao salvar fornecedor', 'Fechar', { duration: 3000 });
      }
    });
  }

  onCancel(): void {
    this.dialogRef.close();
  }

  // Helper methods for form validation
  hasError(fieldName: string, errorType: string): boolean {
    const field = this.supplierForm.get(fieldName);
    return !!(field?.hasError(errorType) && (field?.dirty || field?.touched));
  }

  getErrorMessage(fieldName: string): string {
    const field = this.supplierForm.get(fieldName);
    if (!field) return '';

    if (field.hasError('required')) return 'Campo obrigatório';
    if (field.hasError('email')) return 'Email inválido';
    if (field.hasError('cnpj')) return field.getError('cnpj').message;
    if (field.hasError('cpf')) return field.getError('cpf').message;
    if (field.hasError('min')) return `Valor mínimo: ${field.getError('min').min}`;
    if (field.hasError('max')) return `Valor máximo: ${field.getError('max').max}`;
    if (field.hasError('maxlength')) return `Máximo ${field.getError('maxlength').requiredLength} caracteres`;

    return '';
  }
}
