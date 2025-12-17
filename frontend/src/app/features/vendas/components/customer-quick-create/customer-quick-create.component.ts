import { Component, EventEmitter, Output } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { CustomerService } from '../../services/customer.service';
import { CustomerType, Customer } from '../../models/customer.model';
import { FeedbackService } from '../../../../shared/services/feedback.service';

/**
 * CustomerQuickCreateComponent - Quick customer creation modal
 * Story 4.1: Customer Management - AC9
 */
@Component({
  selector: 'app-customer-quick-create',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  template: `
    <div class="modal-overlay" (click)="close()">
      <div class="modal-content" (click)="$event.stopPropagation()" role="dialog" aria-labelledby="modal-title" aria-modal="true">
        <h3 id="modal-title">Cadastro Rápido de Cliente</h3>
        <form [formGroup]="quickForm" (ngSubmit)="onSubmit()">
          <div class="form-group">
            <label for="customerType">Tipo <span class="required">*</span></label>
            <select
              id="customerType"
              formControlName="customerType"
              class="form-control"
              required
              aria-label="Tipo de pessoa do cliente"
              aria-required="true">
              <option [value]="CustomerType.INDIVIDUAL">Pessoa Física</option>
              <option [value]="CustomerType.BUSINESS">Pessoa Jurídica</option>
            </select>
          </div>

          <div class="form-group">
            <label for="name">{{ isPF ? 'Nome Completo' : 'Razão Social' }} <span class="required">*</span></label>
            <input
              id="name"
              type="text"
              formControlName="name"
              class="form-control"
              required
              [attr.aria-label]="isPF ? 'Nome completo do cliente' : 'Razão social do cliente'"
              aria-required="true"
              [attr.aria-describedby]="hasError('name') ? 'name-error' : null" />
            <span id="name-error" class="error-message" role="alert" *ngIf="hasError('name')">
              {{ isPF ? 'Nome completo é obrigatório' : 'Razão social é obrigatória' }}
            </span>
          </div>

          <div class="form-group">
            <label for="taxId">{{ isPF ? 'CPF' : 'CNPJ' }} <span class="required">*</span></label>
            <input
              id="taxId"
              type="text"
              formControlName="taxId"
              class="form-control"
              required
              [attr.aria-label]="isPF ? 'CPF do cliente' : 'CNPJ do cliente'"
              aria-required="true"
              [attr.aria-describedby]="hasError('taxId') ? 'taxId-error' : null" />
            <span id="taxId-error" class="error-message" role="alert" *ngIf="hasError('taxId')">
              {{ isPF ? 'CPF é obrigatório' : 'CNPJ é obrigatório' }}
            </span>
          </div>

          <div class="form-group">
            <label for="phone">Telefone</label>
            <input
              id="phone"
              type="text"
              formControlName="phone"
              class="form-control"
              aria-label="Telefone do cliente" />
          </div>

          <div class="form-actions">
            <button
              type="button"
              (click)="close()"
              class="btn btn-outline"
              aria-label="Cancelar e fechar cadastro rápido">
              Cancelar
            </button>
            <button
              type="submit"
              class="btn btn-primary"
              [disabled]="loading || quickForm.invalid"
              [attr.aria-label]="loading ? 'Salvando cliente...' : 'Salvar cliente'">
              {{ loading ? 'Salvando...' : 'Salvar' }}
            </button>
          </div>
        </form>
      </div>
    </div>
  `,
  styles: [`
    .modal-overlay {
      position: fixed;
      top: 0;
      left: 0;
      right: 0;
      bottom: 0;
      background: rgba(0,0,0,0.5);
      display: flex;
      align-items: center;
      justify-content: center;
      z-index: 2000;
    }
    .modal-content {
      background: white;
      padding: 2rem;
      border-radius: 8px;
      width: 90%;
      max-width: 500px;
    }
    .form-group {
      margin-bottom: 1rem;
    }
    .form-control {
      width: 100%;
      padding: 0.5rem;
      border: 1px solid #ddd;
      border-radius: 4px;
    }
    .form-control.ng-invalid.ng-touched {
      border-color: #dc2626;
    }
    .required {
      color: #dc2626;
    }
    .error-message {
      display: block;
      color: #dc2626;
      font-size: 0.875rem;
      margin-top: 0.25rem;
    }
    .form-actions {
      display: flex;
      gap: 1rem;
      justify-content: flex-end;
      margin-top: 1.5rem;
    }
    .btn {
      padding: 0.5rem 1rem;
      border-radius: 4px;
      cursor: pointer;
      min-height: 44px;
    }
    .btn-primary {
      background: #0d6efd;
      color: white;
      border: none;
    }
    .btn-primary:disabled {
      opacity: 0.5;
      cursor: not-allowed;
    }
    .btn-outline {
      background: white;
      border: 1px solid #6c757d;
    }
  `]
})
export class CustomerQuickCreateComponent {
  @Output() customerCreated = new EventEmitter<Customer>();
  @Output() closed = new EventEmitter<void>();

  quickForm!: FormGroup;
  loading = false;
  readonly CustomerType = CustomerType;

  constructor(
    private fb: FormBuilder,
    private customerService: CustomerService,
    private feedback: FeedbackService
  ) {
    this.initForm();
  }

  get isPF(): boolean {
    return this.quickForm.get('customerType')?.value === CustomerType.INDIVIDUAL;
  }

  initForm(): void {
    this.quickForm = this.fb.group({
      customerType: [CustomerType.INDIVIDUAL, Validators.required],
      name: ['', Validators.required],
      taxId: ['', Validators.required],
      phone: ['']
    });
  }

  onSubmit(): void {
    if (this.quickForm.invalid) return;

    this.loading = true;
    const formValue = this.quickForm.value;
    const isPF = formValue.customerType === CustomerType.INDIVIDUAL;

    const request = {
      customerType: formValue.customerType,
      ...(isPF ? {
        firstName: formValue.name.split(' ')[0],
        lastName: formValue.name.split(' ').slice(1).join(' '),
        cpf: formValue.taxId
      } : {
        companyName: formValue.name,
        cnpj: formValue.taxId
      }),
      phone: formValue.phone || undefined
    };

    this.customerService.create(request).subscribe({
      next: (customer) => {
        this.feedback.showSuccess('Cliente criado com sucesso!');
        this.customerCreated.emit(customer);
        this.loading = false;
      },
      error: (err) => {
        const errorMessage = err.error?.message || 'Erro desconhecido';
        this.feedback.showError(`Erro ao criar cliente: ${errorMessage}`, () => this.onSubmit());
        this.loading = false;
      }
    });
  }

  close(): void {
    this.closed.emit();
  }

  hasError(fieldName: string): boolean {
    const field = this.quickForm.get(fieldName);
    return !!(field && field.invalid && field.touched);
  }
}
