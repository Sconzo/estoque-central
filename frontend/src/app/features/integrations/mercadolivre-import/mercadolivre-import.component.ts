import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { MatCardModule } from '@angular/material/card';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatCheckboxModule } from '@angular/material/checkbox';
import { MatProgressBarModule } from '@angular/material/progress-bar';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { MatTableModule } from '@angular/material/table';
import { SelectionModel } from '@angular/cdk/collections';
import { MercadoLivreService, ListingPreview } from '../services/mercadolivre.service';

/**
 * MercadoLivreImportComponent - Import products from Mercado Livre
 * Story 5.2: Import Products from Mercado Livre - AC5
 */
@Component({
  selector: 'app-mercadolivre-import',
  standalone: true,
  imports: [
    CommonModule,
    MatCardModule,
    MatButtonModule,
    MatIconModule,
    MatCheckboxModule,
    MatProgressBarModule,
    MatSnackBarModule,
    MatTableModule
  ],
  template: `
    <mat-card>
      <mat-card-header>
        <mat-card-title>
          <mat-icon>cloud_download</mat-icon>
          Importar Produtos do Mercado Livre
        </mat-card-title>
        <mat-card-subtitle>
          Selecione os anúncios que deseja importar para o sistema
        </mat-card-subtitle>
      </mat-card-header>

      <mat-card-content>
        @if (loading) {
          <div class="loading-container">
            <mat-progress-bar mode="indeterminate"></mat-progress-bar>
            <p>Carregando anúncios...</p>
          </div>
        }

        @if (!loading && listings.length === 0) {
          <div class="empty-state">
            <mat-icon>inventory_2</mat-icon>
            <p>Nenhum anúncio encontrado no Mercado Livre</p>
          </div>
        }

        @if (!loading && listings.length > 0) {
          <table mat-table [dataSource]="listings" class="listings-table">
            <!-- Checkbox Column -->
            <ng-container matColumnDef="select">
              <th mat-header-cell *matHeaderCellDef>
                <mat-checkbox
                  (change)="$event ? toggleAll() : null"
                  [checked]="selection.hasValue() && isAllSelected()"
                  [indeterminate]="selection.hasValue() && !isAllSelected()">
                </mat-checkbox>
              </th>
              <td mat-cell *matCellDef="let row">
                <mat-checkbox
                  (click)="$event.stopPropagation()"
                  (change)="$event ? selection.toggle(row) : null"
                  [checked]="selection.isSelected(row)"
                  [disabled]="row.alreadyImported">
                </mat-checkbox>
              </td>
            </ng-container>

            <!-- Thumbnail Column -->
            <ng-container matColumnDef="thumbnail">
              <th mat-header-cell *matHeaderCellDef>Foto</th>
              <td mat-cell *matCellDef="let listing">
                @if (listing.thumbnail) {
                  <img [src]="listing.thumbnail" alt="Thumbnail" class="thumbnail" />
                } @else {
                  <mat-icon>image</mat-icon>
                }
              </td>
            </ng-container>

            <!-- Title Column -->
            <ng-container matColumnDef="title">
              <th mat-header-cell *matHeaderCellDef>Título</th>
              <td mat-cell *matCellDef="let listing">
                {{ listing.title }}
                @if (listing.hasVariations) {
                  <mat-icon class="variation-icon" matTooltip="Produto com variações">
                    tune
                  </mat-icon>
                }
              </td>
            </ng-container>

            <!-- Price Column -->
            <ng-container matColumnDef="price">
              <th mat-header-cell *matHeaderCellDef>Preço</th>
              <td mat-cell *matCellDef="let listing">
                {{ listing.price | currency: 'BRL' }}
              </td>
            </ng-container>

            <!-- Quantity Column -->
            <ng-container matColumnDef="quantity">
              <th mat-header-cell *matHeaderCellDef>Estoque</th>
              <td mat-cell *matCellDef="let listing">{{ listing.quantity }}</td>
            </ng-container>

            <!-- Status Column -->
            <ng-container matColumnDef="status">
              <th mat-header-cell *matHeaderCellDef>Status</th>
              <td mat-cell *matCellDef="let listing">
                @if (listing.alreadyImported) {
                  <span class="status-badge imported">Já Importado</span>
                } @else {
                  <span class="status-badge new">Novo</span>
                }
              </td>
            </ng-container>

            <tr mat-header-row *matHeaderRowDef="displayedColumns"></tr>
            <tr mat-row *matRowDef="let row; columns: displayedColumns;"
                [class.already-imported]="row.alreadyImported"></tr>
          </table>
        }

        @if (importing) {
          <div class="importing-overlay">
            <mat-progress-bar mode="indeterminate"></mat-progress-bar>
            <p>Importando produtos... Aguarde...</p>
          </div>
        }
      </mat-card-content>

      <mat-card-actions>
        <button mat-raised-button color="primary" (click)="importSelected()"
                [disabled]="selection.isEmpty() || importing">
          <mat-icon>download</mat-icon>
          Importar Selecionados ({{ selection.selected.length }})
        </button>
        <button mat-button (click)="loadListings()" [disabled]="loading || importing">
          <mat-icon>refresh</mat-icon>
          Atualizar
        </button>
      </mat-card-actions>
    </mat-card>
  `,
  styles: [`
    mat-card {
      margin: 16px;
    }

    mat-card-title {
      display: flex;
      align-items: center;
      gap: 8px;
    }

    .loading-container {
      padding: 40px 20px;
      text-align: center;
    }

    .loading-container mat-progress-bar {
      margin-bottom: 16px;
    }

    .empty-state {
      display: flex;
      flex-direction: column;
      align-items: center;
      padding: 60px 20px;
      color: #666;
    }

    .empty-state mat-icon {
      font-size: 64px;
      width: 64px;
      height: 64px;
      margin-bottom: 16px;
      color: #999;
    }

    .listings-table {
      width: 100%;
      margin-top: 16px;
    }

    .thumbnail {
      width: 50px;
      height: 50px;
      object-fit: cover;
      border-radius: 4px;
    }

    .variation-icon {
      font-size: 16px;
      width: 16px;
      height: 16px;
      vertical-align: middle;
      margin-left: 8px;
      color: #ff9800;
    }

    .status-badge {
      padding: 4px 12px;
      border-radius: 12px;
      font-size: 12px;
      font-weight: 500;
    }

    .status-badge.imported {
      background-color: #e0e0e0;
      color: #666;
    }

    .status-badge.new {
      background-color: #4caf50;
      color: white;
    }

    .already-imported {
      opacity: 0.6;
      background-color: #f5f5f5;
    }

    .importing-overlay {
      position: relative;
      padding: 20px;
      background-color: rgba(255, 255, 255, 0.95);
      border-radius: 4px;
      margin-top: 16px;
      text-align: center;
    }

    mat-card-actions {
      display: flex;
      gap: 8px;
      padding: 16px;
    }
  `]
})
export class MercadoLivreImportComponent implements OnInit {
  private mlService = inject(MercadoLivreService);
  private snackBar = inject(MatSnackBar);

  listings: ListingPreview[] = [];
  selection = new SelectionModel<ListingPreview>(true, []);
  displayedColumns = ['select', 'thumbnail', 'title', 'price', 'quantity', 'status'];
  loading = false;
  importing = false;

  ngOnInit(): void {
    this.loadListings();
  }

  loadListings(): void {
    this.loading = true;
    this.mlService.getListings().subscribe({
      next: (listings) => {
        this.listings = listings;
        this.loading = false;
      },
      error: (error) => {
        console.error('Error loading listings:', error);
        this.snackBar.open('Erro ao carregar anúncios', 'Fechar', { duration: 3000 });
        this.loading = false;
      }
    });
  }

  isAllSelected(): boolean {
    const numSelected = this.selection.selected.length;
    const numAvailable = this.listings.filter(l => !l.alreadyImported).length;
    return numSelected === numAvailable;
  }

  toggleAll(): void {
    if (this.isAllSelected()) {
      this.selection.clear();
    } else {
      this.listings
        .filter(listing => !listing.alreadyImported)
        .forEach(listing => this.selection.select(listing));
    }
  }

  importSelected(): void {
    if (this.selection.isEmpty()) {
      return;
    }

    this.importing = true;
    const listingIds = this.selection.selected.map(l => l.listingId);

    this.mlService.importListings({ listingIds }).subscribe({
      next: (response) => {
        this.importing = false;
        this.selection.clear();

        let message = `Importação concluída! Importados: ${response.imported}`;
        if (response.skipped > 0) {
          message += `, Ignorados: ${response.skipped}`;
        }
        if (response.errors.length > 0) {
          message += `, Erros: ${response.errors.length}`;
          console.error('Import errors:', response.errors);
        }

        this.snackBar.open(message, 'Fechar', { duration: 5000 });
        this.loadListings(); // Reload to update "already imported" status
      },
      error: (error) => {
        console.error('Error importing listings:', error);
        this.snackBar.open('Erro ao importar produtos', 'Fechar', { duration: 3000 });
        this.importing = false;
      }
    });
  }
}
