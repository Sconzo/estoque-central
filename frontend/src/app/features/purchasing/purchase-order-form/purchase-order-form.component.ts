import { Component, OnInit, inject, Inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, FormArray, Validators, ReactiveFormsModule } from '@angular/forms';
import { MatDialogRef, MAT_DIALOG_DATA, MatDialogModule } from '@angular/material/dialog';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatSelectModule } from '@angular/material/select';
import { MatDatepickerModule } from '@angular/material/datepicker';
import { MatNativeDateModule } from '@angular/material/core';
import { MatTableModule } from '@angular/material/table';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { PurchaseOrderService } from '../services/purchase-order.service';
import { PurchaseOrderResponse, CreatePurchaseOrderRequest } from '../../../shared/models/purchase-order.model';

/**
 * PurchaseOrderFormComponent - Create/edit purchase orders
 * Story 3.2: Purchase Order Creation - AC3, AC7, AC8
 */
@Component({
  selector: 'app-purchase-order-form',
  standalone: true,
  imports: [
    CommonModule,
    ReactiveFormsModule,
    MatDialogModule,
    MatFormFieldModule,
    MatInputModule,
    MatButtonModule,
    MatIconModule,
    MatSelectModule,
    MatDatepickerModule,
    MatNativeDateModule,
    MatTableModule,
    MatSnackBarModule
  ],
  templateUrl: './purchase-order-form.component.html',
  styleUrls: ['./purchase-order-form.component.css']
})
export class PurchaseOrderFormComponent implements OnInit {
  private fb = inject(FormBuilder);
  private purchaseOrderService = inject(PurchaseOrderService);
  private snackBar = inject(MatSnackBar);

  poForm!: FormGroup;
  isEditMode = false;
  isReadOnly = false;
  loading = false;

  displayedColumns: string[] = ['product', 'quantity', 'unitCost', 'total', 'actions'];

  constructor(
    public dialogRef: MatDialogRef<PurchaseOrderFormComponent>,
    @Inject(MAT_DIALOG_DATA) public data: { purchaseOrder?: PurchaseOrderResponse, readonly?: boolean }
  ) {
    this.isEditMode = !!data?.purchaseOrder;
    this.isReadOnly = !!data?.readonly;
  }

  ngOnInit(): void {
    this.initForm();
    if (this.isEditMode && this.data.purchaseOrder) {
      this.loadPurchaseOrderData(this.data.purchaseOrder);
    } else {
      // Add one empty item by default
      this.addItem();
    }
  }

  initForm(): void {
    this.poForm = this.fb.group({
      supplierId: ['', Validators.required],
      stockLocationId: ['', Validators.required],
      orderDate: [new Date(), Validators.required],
      expectedDeliveryDate: [''],
      notes: [''],
      items: this.fb.array([])
    });

    if (this.isReadOnly) {
      this.poForm.disable();
    }
  }

  get items(): FormArray {
    return this.poForm.get('items') as FormArray;
  }

  createItemFormGroup(): FormGroup {
    const itemGroup = this.fb.group({
      productId: ['', Validators.required],
      variantId: [''],
      quantityOrdered: [1, [Validators.required, Validators.min(0.001)]],
      unitCost: [0, [Validators.required, Validators.min(0)]],
      notes: ['']
    });

    // Subscribe to value changes to calculate total
    itemGroup.valueChanges.subscribe(() => {
      this.calculateTotals();
    });

    return itemGroup;
  }

  addItem(): void {
    this.items.push(this.createItemFormGroup());
  }

  removeItem(index: number): void {
    if (this.items.length > 1) {
      this.items.removeAt(index);
      this.calculateTotals();
    } else {
      this.snackBar.open('É necessário ao menos um item na ordem', 'Fechar', { duration: 3000 });
    }
  }

  getItemTotal(index: number): number {
    const item = this.items.at(index);
    const quantity = item.get('quantityOrdered')?.value || 0;
    const unitCost = item.get('unitCost')?.value || 0;
    return quantity * unitCost;
  }

  calculateTotals(): number {
    let total = 0;
    for (let i = 0; i < this.items.length; i++) {
      total += this.getItemTotal(i);
    }
    return total;
  }

  loadPurchaseOrderData(po: PurchaseOrderResponse): void {
    this.poForm.patchValue({
      supplierId: po.supplier?.id || '',
      stockLocationId: po.stockLocation?.id || '',
      orderDate: new Date(po.orderDate),
      expectedDeliveryDate: po.expectedDeliveryDate ? new Date(po.expectedDeliveryDate) : '',
      notes: po.notes || ''
    });

    // Load items
    if (po.items && po.items.length > 0) {
      this.items.clear();
      po.items.forEach(item => {
        const itemGroup = this.fb.group({
          productId: [item.product.id, Validators.required],
          variantId: [''],
          quantityOrdered: [item.quantityOrdered, [Validators.required, Validators.min(0.001)]],
          unitCost: [item.unitCost, [Validators.required, Validators.min(0)]],
          notes: [item.notes || '']
        });
        this.items.push(itemGroup);
      });
    }
  }

  formatDate(date: Date): string {
    if (!date) return '';
    const d = new Date(date);
    const year = d.getFullYear();
    const month = String(d.getMonth() + 1).padStart(2, '0');
    const day = String(d.getDate()).padStart(2, '0');
    return `${year}-${month}-${day}`;
  }

  onSubmit(): void {
    if (this.poForm.invalid) {
      this.snackBar.open('Por favor, preencha todos os campos obrigatórios', 'Fechar', { duration: 3000 });
      return;
    }

    if (this.items.length === 0) {
      this.snackBar.open('É necessário ao menos um item na ordem', 'Fechar', { duration: 3000 });
      return;
    }

    this.loading = true;
    const formValue = this.poForm.value;

    const request: CreatePurchaseOrderRequest = {
      supplierId: formValue.supplierId,
      stockLocationId: formValue.stockLocationId,
      orderDate: this.formatDate(formValue.orderDate),
      expectedDeliveryDate: formValue.expectedDeliveryDate ? this.formatDate(formValue.expectedDeliveryDate) : undefined,
      notes: formValue.notes,
      items: formValue.items
    };

    this.purchaseOrderService.createPurchaseOrder(request).subscribe({
      next: (response) => {
        this.snackBar.open('Ordem de compra criada com sucesso', 'Fechar', { duration: 3000 });
        this.dialogRef.close(true);
      },
      error: (err) => {
        console.error('Error creating purchase order:', err);
        this.snackBar.open('Erro ao criar ordem de compra', 'Fechar', { duration: 3000 });
        this.loading = false;
      }
    });
  }

  onCancel(): void {
    this.dialogRef.close(false);
  }
}
