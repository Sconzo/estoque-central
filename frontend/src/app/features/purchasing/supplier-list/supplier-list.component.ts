import { Component, OnInit, signal, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, ReactiveFormsModule } from '@angular/forms';
import { MatTableModule } from '@angular/material/table';
import { MatPaginatorModule, PageEvent } from '@angular/material/paginator';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatChipsModule } from '@angular/material/chips';
import { MatSelectModule } from '@angular/material/select';
import { MatDialog, MatDialogModule } from '@angular/material/dialog';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { SupplierService } from '../services/supplier.service';
import { SupplierResponse, SupplierStatus } from '../../../shared/models/supplier.model';
import { SupplierFormComponent } from '../supplier-form/supplier-form.component';
import { ConfirmDialogService } from '../../../shared/services/confirm-dialog.service';

/**
 * SupplierListComponent - List and manage suppliers
 * Story 3.1: Supplier Management - AC4
 */
@Component({
  selector: 'app-supplier-list',
  standalone: true,
  imports: [
    CommonModule,
    ReactiveFormsModule,
    MatTableModule,
    MatPaginatorModule,
    MatFormFieldModule,
    MatInputModule,
    MatButtonModule,
    MatIconModule,
    MatChipsModule,
    MatSelectModule,
    MatDialogModule,
    MatSnackBarModule
  ],
  templateUrl: './supplier-list.component.html',
  styleUrls: ['./supplier-list.component.css']
})
export class SupplierListComponent implements OnInit {
  private fb = inject(FormBuilder);
  private supplierService = inject(SupplierService);
  private snackBar = inject(MatSnackBar);
  private dialog = inject(MatDialog);
  private confirmDialog = inject(ConfirmDialogService);

  filterForm!: FormGroup;
  suppliers = signal<SupplierResponse[]>([]);
  loading = signal(false);
  totalElements = signal(0);
  pageSize = 20;
  pageIndex = 0;

  displayedColumns: string[] = [
    'supplierCode',
    'cnpj',
    'companyName',
    'tradeName',
    'city',
    'phone',
    'status',
    'actions'
  ];

  statusOptions = [
    { value: '', label: 'Todos' },
    { value: 'ACTIVE', label: 'Ativo' },
    { value: 'INACTIVE', label: 'Inativo' }
  ];

  ngOnInit(): void {
    this.initFilterForm();
    this.loadSuppliers();
  }

  initFilterForm(): void {
    this.filterForm = this.fb.group({
      search: [''],
      status: [''],
      ativo: [true]
    });
  }

  loadSuppliers(): void {
    this.loading.set(true);
    const formValue = this.filterForm.value;

    this.supplierService.getSuppliers({
      search: formValue.search || undefined,
      status: formValue.status || undefined,
      ativo: formValue.ativo,
      page: this.pageIndex,
      size: this.pageSize,
      sort: 'companyName'
    }).subscribe({
      next: (response) => {
        this.suppliers.set(response.content);
        this.totalElements.set(response.totalElements);
        this.loading.set(false);
      },
      error: (err) => {
        console.error('Error loading suppliers:', err);
        this.snackBar.open('Erro ao carregar fornecedores', 'Fechar', { duration: 3000 });
        this.loading.set(false);
      }
    });
  }

  applyFilters(): void {
    this.pageIndex = 0;
    this.loadSuppliers();
  }

  clearFilters(): void {
    this.filterForm.reset({ ativo: true });
    this.pageIndex = 0;
    this.loadSuppliers();
  }

  onPageChange(event: PageEvent): void {
    this.pageIndex = event.pageIndex;
    this.pageSize = event.pageSize;
    this.loadSuppliers();
  }

  getStatusColor(status: SupplierStatus): string {
    switch (status) {
      case SupplierStatus.ACTIVE: return 'primary';
      case SupplierStatus.INACTIVE: return 'warn';
      default: return '';
    }
  }

  getStatusLabel(status: SupplierStatus): string {
    switch (status) {
      case SupplierStatus.ACTIVE: return 'Ativo';
      case SupplierStatus.INACTIVE: return 'Inativo';
      case SupplierStatus.BLOCKED: return 'Bloqueado';
      case SupplierStatus.PENDING_APPROVAL: return 'Pendente';
      default: return status;
    }
  }

  openCreateDialog(): void {
    const dialogRef = this.dialog.open(SupplierFormComponent, {
      width: '900px',
      maxHeight: '90vh',
      disableClose: true
    });

    dialogRef.afterClosed().subscribe(result => {
      if (result) {
        this.loadSuppliers();
      }
    });
  }

  editSupplier(supplier: SupplierResponse): void {
    const dialogRef = this.dialog.open(SupplierFormComponent, {
      width: '900px',
      maxHeight: '90vh',
      disableClose: true,
      data: { supplier }
    });

    dialogRef.afterClosed().subscribe(result => {
      if (result) {
        this.loadSuppliers();
      }
    });
  }

  deleteSupplier(supplier: SupplierResponse): void {
    this.confirmDialog.confirmDanger({
      title: 'Inativar Fornecedor',
      message: `Deseja realmente inativar o fornecedor ${supplier.companyName}?`
    }).subscribe(confirmed => {
      if (!confirmed) return;

      this.supplierService.deleteSupplier(supplier.id).subscribe({
        next: () => {
          this.snackBar.open('Fornecedor inativado com sucesso', 'Fechar', { duration: 3000 });
          this.loadSuppliers();
        },
        error: (err) => {
          console.error('Error deleting supplier:', err);
          this.snackBar.open('Erro ao inativar fornecedor', 'Fechar', { duration: 3000 });
        }
      });
    });
  }
}
