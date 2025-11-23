import { Component, EventEmitter, Output } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { CustomerService } from '../../services/customer.service';
import { CustomerType, Customer } from '../../models/customer.model';

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
      <div class="modal-content" (click)="$event.stopPropagation()">
        <h3>Cadastro Rápido de Cliente</h3>
        <form [formGroup]="quickForm" (ngSubmit)="onSubmit()">
          <div class="form-group">
            <label>Tipo *</label>
            <select formControlName="customerType" class="form-control">
              <option [value]="CustomerType.INDIVIDUAL">Pessoa Física</option>
              <option [value]="CustomerType.BUSINESS">Pessoa Jurídica</option>
            </select>
          </div>

          <div class="form-group">
            <label>{{ isPF ? 'Nome Completo' : 'Razão Social' }} *</label>
            <input type="text" formControlName="name" class="form-control" />
          </div>

          <div class="form-group">
            <label>{{ isPF ? 'CPF' : 'CNPJ' }} *</label>
            <input type="text" formControlName="taxId" class="form-control" />
          </div>

          <div class="form-group">
            <label>Telefone</label>
            <input type="text" formControlName="phone" class="form-control" />
          </div>

          <div class="form-actions">
            <button type="button" (click)="close()" class="btn btn-outline">Cancelar</button>
            <button type="submit" class="btn btn-primary" [disabled]="loading || quickForm.invalid">
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
    }
    .btn-primary {
      background: #0d6efd;
      color: white;
      border: none;
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
    private customerService: CustomerService
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
        this.customerCreated.emit(customer);
        this.loading = false;
      },
      error: (err) => {
        alert('Erro ao criar cliente: ' + (err.error?.message || 'Erro desconhecido'));
        this.loading = false;
      }
    });
  }

  close(): void {
    this.closed.emit();
  }
}
