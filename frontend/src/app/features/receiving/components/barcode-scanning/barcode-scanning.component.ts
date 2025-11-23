import { Component, OnInit, OnDestroy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, Router } from '@angular/router';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatBadgeModule } from '@angular/material/badge';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { MatDialog, MatDialogModule } from '@angular/material/dialog';
import { BarcodeScannerComponent } from '../barcode-scanner/barcode-scanner.component';
import { ReceivingQuantityModalComponent } from '../receiving-quantity-modal/receiving-quantity-modal.component';
import { ManualEntryModalComponent } from '../manual-entry-modal/manual-entry-modal.component';
import { ReceivingService, ReceivingOrderDetail, ReceivingItemDetail } from '../../services/receiving.service';
import { Subscription } from 'rxjs';

@Component({
  selector: 'app-barcode-scanning',
  standalone: true,
  imports: [
    CommonModule,
    MatButtonModule,
    MatIconModule,
    MatBadgeModule,
    MatSnackBarModule,
    MatDialogModule,
    BarcodeScannerComponent
  ],
  templateUrl: './barcode-scanning.component.html',
  styleUrls: ['./barcode-scanning.component.scss']
})
export class BarcodeScanningComponent implements OnInit, OnDestroy {
  orderDetail: ReceivingOrderDetail | null = null;
  loading: boolean = false;
  scannedItemsCount: number = 0;
  private queueSubscription?: Subscription;

  // TODO: Get from auth service
  private tenantId = '00000000-0000-0000-0000-000000000001';
  private orderId: string = '';

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private receivingService: ReceivingService,
    private snackBar: MatSnackBar,
    private dialog: MatDialog
  ) {}

  ngOnInit(): void {
    // Get order ID from route
    this.orderId = this.route.snapshot.paramMap.get('id') || '';

    if (!this.orderId) {
      this.snackBar.open('Ordem de compra não encontrada', 'Fechar', { duration: 3000 });
      this.router.navigate(['/receiving']);
      return;
    }

    // Load order details
    this.loadOrderDetails();

    // Subscribe to queue changes
    this.queueSubscription = this.receivingService.getQueue().subscribe(queue => {
      this.scannedItemsCount = queue.length;
    });
  }

  ngOnDestroy(): void {
    this.queueSubscription?.unsubscribe();
  }

  loadOrderDetails(): void {
    this.loading = true;

    this.receivingService.getReceivingDetails(this.tenantId, this.orderId).subscribe({
      next: (details) => {
        this.orderDetail = details;
        this.loading = false;
      },
      error: (err) => {
        console.error('Error loading order details:', err);
        this.snackBar.open('Erro ao carregar detalhes da ordem', 'Fechar', { duration: 3000 });
        this.loading = false;
        this.router.navigate(['/receiving']);
      }
    });
  }

  onBarcodeDetected(barcode: string): void {
    if (!this.orderDetail) {
      return;
    }

    // Find item by barcode
    const item = this.receivingService.findItemByBarcode(this.orderDetail, barcode);

    if (!item) {
      // Item not found
      this.showError('Produto não encontrado nesta ordem');
      if (navigator.vibrate) {
        navigator.vibrate([100, 50, 100]); // Error vibration pattern
      }
      return;
    }

    // Check if item has pending quantity
    if (item.quantityPending <= 0) {
      this.showWarning('Todos os itens deste produto já foram recebidos');
      return;
    }

    // Show quantity confirmation modal
    this.showQuantityModal(item);
  }

  onPermissionDenied(): void {
    this.snackBar.open(
      'Permissão de câmera negada. Por favor, permita o acesso à câmera.',
      'Fechar',
      { duration: 5000 }
    );
  }

  onCamerasFound(devices: MediaDeviceInfo[]): void {
    console.log(`Found ${devices.length} cameras`);
  }

  openManualEntry(): void {
    if (!this.orderDetail) {
      return;
    }

    const dialogRef = this.dialog.open(ManualEntryModalComponent, {
      width: '90vw',
      maxWidth: '500px',
      data: { orderDetail: this.orderDetail }
    });

    dialogRef.afterClosed().subscribe(result => {
      if (result && result.item && result.quantity) {
        // Add to receiving queue
        this.receivingService.addItem({
          purchaseOrderItemId: result.item.id,
          productId: result.item.productId,
          productName: result.item.productName,
          sku: result.item.sku,
          barcode: result.item.barcode,
          quantityToReceive: result.quantity,
          unitCost: result.item.unitCost
        });

        this.showSuccess(`${result.item.productName} (${result.quantity} un.) adicionado à fila`);
      }
    });
  }

  openSummary(): void {
    // Navigate to summary screen
    this.router.navigate(['/receiving/summary', this.orderId]);
  }

  goBack(): void {
    this.router.navigate(['/receiving']);
  }

  private showQuantityModal(item: ReceivingItemDetail): void {
    const dialogRef = this.dialog.open(ReceivingQuantityModalComponent, {
      width: '90vw',
      maxWidth: '400px',
      data: { item }
    });

    dialogRef.afterClosed().subscribe(quantity => {
      if (quantity !== null && quantity > 0) {
        // Add to receiving queue
        this.receivingService.addItem({
          purchaseOrderItemId: item.id,
          productId: item.productId,
          productName: item.productName,
          sku: item.sku,
          barcode: item.barcode,
          quantityToReceive: quantity,
          unitCost: item.unitCost
        });

        this.showSuccess(`${item.productName} (${quantity} un.) adicionado à fila`);
        this.showFlashFeedback();
      }
    });
  }

  private showFlashFeedback(): void {
    // TODO: Add green flash overlay animation
    // Could be implemented with a temporary overlay element
  }

  private showSuccess(message: string): void {
    this.snackBar.open(message, 'Fechar', {
      duration: 2000,
      panelClass: ['success-snackbar']
    });
  }

  private showWarning(message: string): void {
    this.snackBar.open(message, 'Fechar', {
      duration: 3000,
      panelClass: ['warning-snackbar']
    });
  }

  private showError(message: string): void {
    this.snackBar.open(message, 'Fechar', {
      duration: 3000,
      panelClass: ['error-snackbar']
    });
  }
}
