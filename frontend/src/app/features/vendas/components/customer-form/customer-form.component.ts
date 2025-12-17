import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router, ActivatedRoute } from '@angular/router';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatRadioModule } from '@angular/material/radio';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { CustomerService } from '../../services/customer.service';
import { CustomerType, CustomerRequest } from '../../models/customer.model';

/**
 * CustomerFormComponent - Create/Edit customer form
 * Story 4.1: Customer Management - AC7
 * Refactored with Material Design (UX-4)
 */
@Component({
  selector: 'app-customer-form',
  standalone: true,
  imports: [
    CommonModule,
    ReactiveFormsModule,
    MatFormFieldModule,
    MatInputModule,
    MatRadioModule,
    MatButtonModule,
    MatIconModule
  ],
  templateUrl: './customer-form.component.html',
  styleUrls: ['./customer-form.component.scss']
})
export class CustomerFormComponent implements OnInit {
  customerForm!: FormGroup;
  loading = false;
  error: string | null = null;
  isEditMode = false;
  customerId: string | null = null;
  readonly CustomerType = CustomerType;

  constructor(
    private fb: FormBuilder,
    private customerService: CustomerService,
    private router: Router,
    private route: ActivatedRoute
  ) {}

  ngOnInit(): void {
    this.initForm();
    this.customerId = this.route.snapshot.paramMap.get('id');
    if (this.customerId) {
      this.isEditMode = true;
      this.loadCustomer();
    }
  }

  initForm(): void {
    this.customerForm = this.fb.group({
      customerType: [CustomerType.INDIVIDUAL, Validators.required],
      firstName: [''],
      lastName: [''],
      cpf: [''],
      companyName: [''],
      cnpj: ['', Validators.required],
      tradeName: [''],
      email: ['', Validators.email],
      phone: [''],
      mobile: [''],
      birthDate: [''],
      stateRegistration: [''],
      customerSegment: [''],
      loyaltyTier: [''],
      creditLimit: [null],
      acceptsMarketing: [true],
      preferredLanguage: ['pt-BR'],
      notes: ['']
    });

    this.customerForm.get('customerType')?.valueChanges.subscribe(type => {
      this.updateValidators(type);
    });
    this.updateValidators(CustomerType.INDIVIDUAL);
  }

  updateValidators(type: CustomerType): void {
    const firstNameCtrl = this.customerForm.get('firstName');
    const lastNameCtrl = this.customerForm.get('lastName');
    const cpfCtrl = this.customerForm.get('cpf');
    const companyNameCtrl = this.customerForm.get('companyName');
    const cnpjCtrl = this.customerForm.get('cnpj');

    if (type === CustomerType.INDIVIDUAL) {
      firstNameCtrl?.setValidators([Validators.required]);
      lastNameCtrl?.setValidators([Validators.required]);
      cpfCtrl?.setValidators([]);
      companyNameCtrl?.clearValidators();
      cnpjCtrl?.clearValidators();
    } else {
      firstNameCtrl?.clearValidators();
      lastNameCtrl?.clearValidators();
      cpfCtrl?.clearValidators();
      companyNameCtrl?.setValidators([Validators.required]);
      cnpjCtrl?.setValidators([Validators.required]);
    }

    firstNameCtrl?.updateValueAndValidity();
    lastNameCtrl?.updateValueAndValidity();
    cpfCtrl?.updateValueAndValidity();
    companyNameCtrl?.updateValueAndValidity();
    cnpjCtrl?.updateValueAndValidity();
  }

  loadCustomer(): void {
    if (!this.customerId) return;

    this.loading = true;
    this.customerService.getById(this.customerId).subscribe({
      next: (customer) => {
        this.customerForm.patchValue(customer);
        this.loading = false;
      },
      error: (err) => {
        console.error('Error loading customer:', err);
        this.error = 'Erro ao carregar cliente';
        this.loading = false;
      }
    });
  }

  onSubmit(): void {
    if (this.customerForm.invalid) {
      this.customerForm.markAllAsTouched();
      return;
    }

    this.loading = true;
    this.error = null;

    const customerData: CustomerRequest = this.customerForm.value;

    const request = this.isEditMode && this.customerId
      ? this.customerService.update(this.customerId, customerData)
      : this.customerService.create(customerData);

    request.subscribe({
      next: () => {
        this.router.navigate(['/customers']);
      },
      error: (err) => {
        console.error('Error saving customer:', err);
        this.error = err.error?.message || 'Erro ao salvar cliente';
        this.loading = false;
      }
    });
  }

  cancel(): void {
    this.router.navigate(['/customers']);
  }
}
