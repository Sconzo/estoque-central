import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { MatCardModule } from '@angular/material/card';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatChipsModule } from '@angular/material/chips';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { MercadoLivreService, ConnectionStatusResponse } from '../services/mercadolivre.service';
import { ActivatedRoute, Router } from '@angular/router';

/**
 * MercadoLivreIntegrationComponent - Mercado Livre OAuth2 integration UI
 * Story 5.1: Mercado Livre OAuth2 Authentication - AC6
 *
 * Displays connection status and provides OAuth2 flow initiation
 */
@Component({
  selector: 'app-mercadolivre-integration',
  standalone: true,
  imports: [
    CommonModule,
    MatCardModule,
    MatButtonModule,
    MatIconModule,
    MatChipsModule,
    MatProgressSpinnerModule,
    MatSnackBarModule
  ],
  template: `
    <mat-card>
      <mat-card-header>
        <mat-card-title>
          <div class="title-container">
            <img src="assets/mercadolivre-logo.png"
                 alt="Mercado Livre"
                 class="ml-logo"
                 onerror="this.style.display='none'" />
            <mat-icon class="marketplace-icon">store</mat-icon>
            <span>Integração Mercado Livre</span>
          </div>
        </mat-card-title>
        <mat-card-subtitle>
          Conecte sua conta Mercado Livre para sincronizar produtos, estoque e pedidos
        </mat-card-subtitle>
      </mat-card-header>

      <mat-card-content>
        @if (loading) {
          <div class="loading-container">
            <mat-spinner diameter="40"></mat-spinner>
            <p>Carregando status da conexão...</p>
          </div>
        }

        @if (!loading && connectionStatus) {
          <div class="status-container">
            <!-- Connection Status Badge -->
            <div class="status-badge">
              @if (connectionStatus.connected) {
                <mat-chip class="status-chip connected">
                  <mat-icon>check_circle</mat-icon>
                  Conectado
                </mat-chip>
              } @else {
                <mat-chip class="status-chip disconnected">
                  <mat-icon>cloud_off</mat-icon>
                  Desconectado
                </mat-chip>
              }
            </div>

            <!-- Connection Details (when connected) -->
            @if (connectionStatus.connected && connectionStatus.userIdMarketplace) {
              <div class="connection-details">
                <div class="detail-row">
                  <mat-icon>account_circle</mat-icon>
                  <span class="detail-label">ID do Usuário ML:</span>
                  <span class="detail-value">{{ connectionStatus.userIdMarketplace }}</span>
                </div>

                @if (connectionStatus.lastSyncAt) {
                  <div class="detail-row">
                    <mat-icon>sync</mat-icon>
                    <span class="detail-label">Última Sincronização:</span>
                    <span class="detail-value">{{ connectionStatus.lastSyncAt | date: 'dd/MM/yyyy HH:mm' }}</span>
                  </div>
                }

                @if (connectionStatus.tokenExpiresAt) {
                  <div class="detail-row">
                    <mat-icon>schedule</mat-icon>
                    <span class="detail-label">Token Expira em:</span>
                    <span class="detail-value">{{ connectionStatus.tokenExpiresAt | date: 'dd/MM/yyyy HH:mm' }}</span>
                  </div>
                }
              </div>
            }

            <!-- Error Message -->
            @if (connectionStatus.errorMessage) {
              <div class="error-container">
                <mat-icon>error</mat-icon>
                <span>{{ connectionStatus.errorMessage }}</span>
              </div>
            }

            <!-- Disconnected State Info -->
            @if (!connectionStatus.connected) {
              <div class="info-container">
                <mat-icon>info</mat-icon>
                <div class="info-content">
                  <h4>Benefícios da Integração:</h4>
                  <ul>
                    <li>Sincronização automática de produtos</li>
                    <li>Atualização de estoque em tempo real</li>
                    <li>Importação automática de pedidos</li>
                    <li>Gestão centralizada de anúncios</li>
                  </ul>
                </div>
              </div>
            }
          </div>
        }
      </mat-card-content>

      <mat-card-actions>
        @if (!loading && connectionStatus) {
          @if (connectionStatus.connected) {
            <button mat-raised-button color="primary" (click)="navigateToImport()">
              <mat-icon>cloud_download</mat-icon>
              Importar Produtos
            </button>
            <button mat-raised-button color="accent" (click)="navigateToOrders()">
              <mat-icon>shopping_cart</mat-icon>
              Ver Pedidos
            </button>
            <button mat-raised-button color="warn" (click)="disconnect()">
              <mat-icon>link_off</mat-icon>
              Desconectar
            </button>
            <button mat-button (click)="refreshStatus()">
              <mat-icon>refresh</mat-icon>
              Atualizar Status
            </button>
          } @else {
            <button mat-raised-button color="primary" (click)="connect()" [disabled]="connecting">
              @if (connecting) {
                <mat-spinner diameter="20"></mat-spinner>
              } @else {
                <mat-icon>link</mat-icon>
              }
              Conectar com Mercado Livre
            </button>
          }
        }
      </mat-card-actions>
    </mat-card>
  `,
  styles: [`
    mat-card {
      margin: 16px;
      max-width: 800px;
    }

    .title-container {
      display: flex;
      align-items: center;
      gap: 12px;
    }

    .ml-logo {
      height: 32px;
      width: auto;
    }

    .marketplace-icon {
      color: #3483fa;
    }

    .loading-container {
      display: flex;
      flex-direction: column;
      align-items: center;
      padding: 40px;
      gap: 16px;
    }

    .status-container {
      padding: 16px 0;
    }

    .status-badge {
      margin-bottom: 24px;
    }

    .status-chip {
      font-size: 14px;
      font-weight: 500;
      padding: 8px 16px;
    }

    .status-chip.connected {
      background-color: #4caf50;
      color: white;
    }

    .status-chip.disconnected {
      background-color: #9e9e9e;
      color: white;
    }

    .status-chip mat-icon {
      margin-right: 8px;
    }

    .connection-details {
      background-color: #f5f5f5;
      border-radius: 8px;
      padding: 16px;
      margin-bottom: 16px;
    }

    .detail-row {
      display: flex;
      align-items: center;
      gap: 12px;
      margin-bottom: 12px;
    }

    .detail-row:last-child {
      margin-bottom: 0;
    }

    .detail-row mat-icon {
      color: #666;
      font-size: 20px;
      width: 20px;
      height: 20px;
    }

    .detail-label {
      font-weight: 500;
      color: #666;
      min-width: 180px;
    }

    .detail-value {
      color: #333;
    }

    .error-container {
      display: flex;
      align-items: center;
      gap: 12px;
      padding: 12px;
      background-color: #ffebee;
      border-left: 4px solid #f44336;
      border-radius: 4px;
      margin-bottom: 16px;
    }

    .error-container mat-icon {
      color: #f44336;
    }

    .info-container {
      display: flex;
      gap: 12px;
      padding: 16px;
      background-color: #e3f2fd;
      border-left: 4px solid #2196f3;
      border-radius: 4px;
    }

    .info-container mat-icon {
      color: #2196f3;
      margin-top: 4px;
    }

    .info-content h4 {
      margin: 0 0 8px 0;
      color: #1976d2;
    }

    .info-content ul {
      margin: 0;
      padding-left: 20px;
    }

    .info-content li {
      margin-bottom: 4px;
      color: #555;
    }

    mat-card-actions {
      display: flex;
      gap: 8px;
      padding: 16px;
    }

    mat-card-actions button mat-spinner {
      display: inline-block;
      margin-right: 8px;
    }
  `]
})
export class MercadoLivreIntegrationComponent implements OnInit {
  private mlService = inject(MercadoLivreService);
  private snackBar = inject(MatSnackBar);
  private route = inject(ActivatedRoute);
  private router = inject(Router);

  connectionStatus: ConnectionStatusResponse | null = null;
  loading = false;
  connecting = false;

  ngOnInit(): void {
    this.loadStatus();
    this.checkCallbackStatus();
  }

  /**
   * Load current connection status
   */
  loadStatus(): void {
    this.loading = true;
    this.mlService.getStatus().subscribe({
      next: (status) => {
        this.connectionStatus = status;
        this.loading = false;
      },
      error: (error) => {
        console.error('Error loading status:', error);
        this.snackBar.open('Erro ao carregar status da integração', 'Fechar', { duration: 3000 });
        this.loading = false;
      }
    });
  }

  /**
   * Check if returning from OAuth callback
   */
  checkCallbackStatus(): void {
    this.route.queryParams.subscribe(params => {
      if (params['status'] === 'success') {
        this.snackBar.open('Conectado com Mercado Livre com sucesso!', 'Fechar', { duration: 5000 });
        this.router.navigate([], { queryParams: {} }); // Clean URL
        this.loadStatus(); // Refresh status
      } else if (params['status'] === 'error') {
        const message = params['message'] || 'Erro ao conectar com Mercado Livre';
        this.snackBar.open(message, 'Fechar', { duration: 5000 });
        this.router.navigate([], { queryParams: {} }); // Clean URL
      }
    });
  }

  /**
   * AC6: Initialize OAuth2 flow
   * Opens popup to Mercado Livre authorization page
   */
  connect(): void {
    this.connecting = true;

    this.mlService.initAuth().subscribe({
      next: (response) => {
        // Open OAuth popup
        const width = 600;
        const height = 700;
        const left = (screen.width - width) / 2;
        const top = (screen.height - height) / 2;

        window.open(
          response.authorization_url,
          'MercadoLivreAuth',
          `width=${width},height=${height},left=${left},top=${top}`
        );

        this.connecting = false;

        // Poll for status changes (user may close popup)
        this.pollStatusAfterAuth();
      },
      error: (error) => {
        console.error('Error initiating auth:', error);
        this.snackBar.open('Erro ao iniciar autenticação', 'Fechar', { duration: 3000 });
        this.connecting = false;
      }
    });
  }

  /**
   * AC7: Disconnect Mercado Livre
   */
  disconnect(): void {
    if (!confirm('Tem certeza que deseja desconectar do Mercado Livre?')) {
      return;
    }

    this.mlService.disconnect().subscribe({
      next: () => {
        this.snackBar.open('Desconectado do Mercado Livre', 'Fechar', { duration: 3000 });
        this.loadStatus();
      },
      error: (error) => {
        console.error('Error disconnecting:', error);
        this.snackBar.open('Erro ao desconectar', 'Fechar', { duration: 3000 });
      }
    });
  }

  /**
   * Refresh connection status
   */
  refreshStatus(): void {
    this.loadStatus();
  }

  /**
   * Navigate to import products page
   * Story 5.2: AC5
   */
  navigateToImport(): void {
    this.router.navigate(['/integracoes/mercadolivre/importar']);
  }

  /**
   * Navigate to orders page
   * Story 5.5: AC6
   */
  navigateToOrders(): void {
    this.router.navigate(['/integracoes/mercadolivre/pedidos']);
  }

  /**
   * Poll status after OAuth flow initiated
   * Checks every 2 seconds for 60 seconds if connection was established
   */
  private pollStatusAfterAuth(): void {
    let attempts = 0;
    const maxAttempts = 30; // 60 seconds total

    const interval = setInterval(() => {
      attempts++;

      this.mlService.getStatus().subscribe({
        next: (status) => {
          if (status.connected) {
            this.connectionStatus = status;
            this.snackBar.open('Conectado com sucesso!', 'Fechar', { duration: 3000 });
            clearInterval(interval);
          } else if (attempts >= maxAttempts) {
            clearInterval(interval);
          }
        },
        error: () => {
          if (attempts >= maxAttempts) {
            clearInterval(interval);
          }
        }
      });
    }, 2000);
  }
}
