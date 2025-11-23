import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router, ActivatedRoute } from '@angular/router';
import { MatCardModule } from '@angular/material/card';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatListModule } from '@angular/material/list';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { ReceivingService, ReceivingItem } from '../../services/receiving.service';

@Component({
  selector: 'app-receiving-summary',
  standalone: true,
  imports: [CommonModule, MatCardModule, MatButtonModule, MatIconModule, MatListModule, MatSnackBarModule, MatProgressSpinnerModule],
  template: `
    <div class="summary-container">
      <header>
        <button mat-icon-button (click)="continueScanning()"><mat-icon>arrow_back</mat-icon></button>
        <h2>Resumo de Recebimento</h2>
      </header>

      <div class="items-list">
        @for (item of items; track item.purchaseOrderItemId) {
          <mat-card>
            <mat-card-content>
              <div class="item-row">
                <div class="item-info">
                  <strong>{{ item.productName }}</strong>
                  <span>SKU: {{ item.sku }}</span>
                  <span>Qtd: {{ item.quantityToReceive }}</span>
                </div>
                <button mat-icon-button (click)="removeItem(item.purchaseOrderItemId)">
                  <mat-icon>delete</mat-icon>
                </button>
              </div>
            </mat-card-content>
          </mat-card>
        }
      </div>

      <footer>
        <div class="totals">
          <span>Total de itens: {{ items.length }}</span>
          <span>Valor total: {{ formatCurrency(totalValue) }}</span>
        </div>
        <button mat-raised-button color="primary" (click)="finalize()" [disabled]="items.length === 0 || isProcessing">
          @if (isProcessing) {
            <mat-spinner diameter="20" style="display: inline-block; margin-right: 8px;"></mat-spinner>
          }
          {{ isProcessing ? 'Processando...' : 'Finalizar Recebimento' }}
        </button>
        <button mat-button (click)="continueScanning()">Continuar Escaneando</button>
      </footer>
    </div>
  `,
  styles: [`
    .summary-container { padding: 16px; }
    header { display: flex; align-items: center; gap: 16px; margin-bottom: 24px; }
    .items-list { margin-bottom: 24px; display: flex; flex-direction: column; gap: 12px; }
    .item-row { display: flex; justify-content: space-between; align-items: center; }
    .item-info { display: flex; flex-direction: column; gap: 4px; }
    footer { position: sticky; bottom: 0; background: white; padding: 16px; box-shadow: 0 -2px 4px rgba(0,0,0,0.1); }
    .totals { margin-bottom: 16px; display: flex; flex-direction: column; gap: 8px; font-weight: 500; }
    button[mat-raised-button] { width: 100%; margin-bottom: 8px; }
  `]
})
export class ReceivingSummaryComponent implements OnInit {
  items: ReceivingItem[] = [];
  totalValue: number = 0;
  isProcessing: boolean = false;
  private orderId: string = '';

  constructor(
    private router: Router,
    private route: ActivatedRoute,
    private receivingService: ReceivingService,
    private snackBar: MatSnackBar
  ) {}

  ngOnInit(): void {
    this.orderId = this.route.snapshot.paramMap.get('id') || '';
    this.receivingService.getQueue().subscribe(queue => {
      this.items = queue;
      this.totalValue = this.receivingService.getTotalValue();
    });
  }

  removeItem(id: string): void {
    this.receivingService.removeItem(id);
  }

  continueScanning(): void {
    this.router.navigate(['/receiving/scan', this.orderId]);
  }

  finalize(): void {
    if (this.items.length === 0 || this.isProcessing) {
      return;
    }

    this.isProcessing = true;

    // Get tenantId from session/auth (TODO: implement proper auth context)
    const tenantId = sessionStorage.getItem('tenantId') || 'default-tenant';
    const receivingDate = new Date().toISOString().split('T')[0];

    this.receivingService.finalizeReceiving(tenantId, this.orderId, receivingDate, null)
      .subscribe({
        next: (response) => {
          this.snackBar.open('Recebimento finalizado com sucesso!', 'Fechar', {
            duration: 3000,
            panelClass: 'success-snackbar'
          });
          this.receivingService.clearQueue();
          this.router.navigate(['/receiving']);
        },
        error: (error) => {
          this.isProcessing = false;
          const errorMessage = error.error?.message || 'Erro ao processar recebimento. Tente novamente.';
          this.snackBar.open(errorMessage, 'Fechar', {
            duration: 5000,
            panelClass: 'error-snackbar'
          });
        }
      });
  }

  formatCurrency(value: number): string {
    return new Intl.NumberFormat('pt-BR', { style: 'currency', currency: 'BRL' }).format(value);
  }
}
