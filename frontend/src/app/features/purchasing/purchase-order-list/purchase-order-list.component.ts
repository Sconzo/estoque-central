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
import { MatDatepickerModule } from '@angular/material/datepicker';
import { MatNativeDateModule } from '@angular/material/core';
import { MatMenuModule } from '@angular/material/menu';
import { MatCardModule } from '@angular/material/card';
import { PurchaseOrderService } from '../services/purchase-order.service';
import { PurchaseOrderResponse, PurchaseOrderStatus } from '../../../shared/models/purchase-order.model';
import { PurchaseOrderFormComponent } from '../purchase-order-form/purchase-order-form.component';
import { ConfirmDialogService } from '../../../shared/services/confirm-dialog.service';

/**
 * PurchaseOrderListComponent - List and manage purchase orders
 * Story 3.2: Purchase Order Creation - AC4, AC6
 */
@Component({
  selector: 'app-purchase-order-list',
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
    MatSnackBarModule,
    MatDatepickerModule,
    MatNativeDateModule,
    MatMenuModule,
    MatCardModule
  ],
  templateUrl: './purchase-order-list.component.html',
  styleUrls: ['./purchase-order-list.component.css']
})
export class PurchaseOrderListComponent implements OnInit {
  private fb = inject(FormBuilder);
  private purchaseOrderService = inject(PurchaseOrderService);
  private snackBar = inject(MatSnackBar);
  private dialog = inject(MatDialog);
  private confirmDialog = inject(ConfirmDialogService);

  filterForm!: FormGroup;
  purchaseOrders = signal<PurchaseOrderResponse[]>([]);
  loading = signal(false);
  totalElements = signal(0);
  pageSize = 20;
  pageIndex = 0;

  displayedColumns: string[] = [
    'poNumber',
    'orderDate',
    'expectedDeliveryDate',
    'status',
    'totalAmount',
    'actions'
  ];

  statusOptions = [
    { value: '', label: 'Todos' },
    { value: 'DRAFT', label: 'Rascunho' },
    { value: 'SENT_TO_SUPPLIER', label: 'Enviado' },
    { value: 'PARTIALLY_RECEIVED', label: 'Parcialmente Recebido' },
    { value: 'RECEIVED', label: 'Recebido' },
    { value: 'CANCELLED', label: 'Cancelado' }
  ];

  ngOnInit(): void {
    this.initFilterForm();
    this.loadPurchaseOrders();
  }

  initFilterForm(): void {
    this.filterForm = this.fb.group({
      poNumber: [''],
      status: [''],
      orderDateFrom: [''],
      orderDateTo: ['']
    });
  }

  loadPurchaseOrders(): void {
    this.loading.set(true);
    const formValue = this.filterForm.value;

    this.purchaseOrderService.getPurchaseOrders({
      order_number: formValue.poNumber || undefined,
      status: formValue.status || undefined,
      order_date_from: formValue.orderDateFrom ? this.formatDate(formValue.orderDateFrom) : undefined,
      order_date_to: formValue.orderDateTo ? this.formatDate(formValue.orderDateTo) : undefined,
      page: this.pageIndex,
      size: this.pageSize,
      sort: 'createdAt,desc'
    }).subscribe({
      next: (response) => {
        this.purchaseOrders.set(response.content);
        this.totalElements.set(response.totalElements);
        this.loading.set(false);
      },
      error: (err) => {
        console.error('Error loading purchase orders:', err);
        this.snackBar.open('Erro ao carregar ordens de compra', 'Fechar', { duration: 3000 });
        this.loading.set(false);
      }
    });
  }

  formatDate(date: Date): string {
    if (!date) return '';
    const year = date.getFullYear();
    const month = String(date.getMonth() + 1).padStart(2, '0');
    const day = String(date.getDate()).padStart(2, '0');
    return `${year}-${month}-${day}`;
  }

  applyFilters(): void {
    this.pageIndex = 0;
    this.loadPurchaseOrders();
  }

  clearFilters(): void {
    this.filterForm.reset();
    this.pageIndex = 0;
    this.loadPurchaseOrders();
  }

  onPageChange(event: PageEvent): void {
    this.pageIndex = event.pageIndex;
    this.pageSize = event.pageSize;
    this.loadPurchaseOrders();
  }

  getStatusColor(status: PurchaseOrderStatus): string {
    switch (status) {
      case PurchaseOrderStatus.DRAFT: return 'accent';
      case PurchaseOrderStatus.SENT_TO_SUPPLIER: return 'primary';
      case PurchaseOrderStatus.PARTIALLY_RECEIVED: return 'warn';
      case PurchaseOrderStatus.RECEIVED: return 'primary';
      case PurchaseOrderStatus.CANCELLED: return '';
      default: return '';
    }
  }

  getStatusLabel(status: PurchaseOrderStatus): string {
    switch (status) {
      case PurchaseOrderStatus.DRAFT: return 'Rascunho';
      case PurchaseOrderStatus.PENDING_APPROVAL: return 'Aguardando Aprovação';
      case PurchaseOrderStatus.APPROVED: return 'Aprovado';
      case PurchaseOrderStatus.SENT_TO_SUPPLIER: return 'Enviado';
      case PurchaseOrderStatus.PARTIALLY_RECEIVED: return 'Parcialmente Recebido';
      case PurchaseOrderStatus.RECEIVED: return 'Recebido';
      case PurchaseOrderStatus.CANCELLED: return 'Cancelado';
      case PurchaseOrderStatus.CLOSED: return 'Fechado';
      default: return status;
    }
  }

  openCreateDialog(): void {
    const dialogRef = this.dialog.open(PurchaseOrderFormComponent, {
      width: '1000px',
      maxHeight: '90vh',
      disableClose: true
    });

    dialogRef.afterClosed().subscribe(result => {
      if (result) {
        this.loadPurchaseOrders();
      }
    });
  }

  viewDetails(po: PurchaseOrderResponse): void {
    const dialogRef = this.dialog.open(PurchaseOrderFormComponent, {
      width: '1000px',
      maxHeight: '90vh',
      disableClose: true,
      data: { purchaseOrder: po, readonly: true }
    });
  }

  editPurchaseOrder(po: PurchaseOrderResponse): void {
    if (po.status !== PurchaseOrderStatus.DRAFT) {
      this.snackBar.open('Apenas ordens em rascunho podem ser editadas', 'Fechar', { duration: 3000 });
      return;
    }

    const dialogRef = this.dialog.open(PurchaseOrderFormComponent, {
      width: '1000px',
      maxHeight: '90vh',
      disableClose: true,
      data: { purchaseOrder: po }
    });

    dialogRef.afterClosed().subscribe(result => {
      if (result) {
        this.loadPurchaseOrders();
      }
    });
  }

  sendToSupplier(po: PurchaseOrderResponse): void {
    this.confirmDialog.confirmInfo({
      title: 'Enviar para Fornecedor',
      message: `Deseja enviar a ordem ${po.poNumber} para o fornecedor?`
    }).subscribe(confirmed => {
      if (!confirmed) return;

      this.purchaseOrderService.updateStatus(po.id, {
        status: PurchaseOrderStatus.SENT_TO_SUPPLIER
      }).subscribe({
        next: () => {
          this.snackBar.open('Ordem enviada com sucesso', 'Fechar', { duration: 3000 });
          this.loadPurchaseOrders();
        },
        error: (err) => {
          console.error('Error updating status:', err);
          this.snackBar.open('Erro ao enviar ordem', 'Fechar', { duration: 3000 });
        }
      });
    });
  }

  cancelPurchaseOrder(po: PurchaseOrderResponse): void {
    this.confirmDialog.confirmDanger({
      title: 'Cancelar Ordem de Compra',
      message: `Deseja realmente cancelar a ordem ${po.poNumber}?`
    }).subscribe(confirmed => {
      if (!confirmed) return;

      this.purchaseOrderService.updateStatus(po.id, {
        status: PurchaseOrderStatus.CANCELLED
      }).subscribe({
        next: () => {
          this.snackBar.open('Ordem cancelada com sucesso', 'Fechar', { duration: 3000 });
          this.loadPurchaseOrders();
        },
        error: (err) => {
          console.error('Error cancelling order:', err);
          this.snackBar.open('Erro ao cancelar ordem', 'Fechar', { duration: 3000 });
        }
      });
    });
  }

  canEdit(po: PurchaseOrderResponse): boolean {
    return po.status === PurchaseOrderStatus.DRAFT;
  }

  canSend(po: PurchaseOrderResponse): boolean {
    return po.status === PurchaseOrderStatus.DRAFT;
  }

  canCancel(po: PurchaseOrderResponse): boolean {
    return po.status === PurchaseOrderStatus.DRAFT ||
           po.status === PurchaseOrderStatus.SENT_TO_SUPPLIER;
  }
}
