import { Component, Inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { MAT_DIALOG_DATA, MatDialogModule, MatDialogRef } from '@angular/material/dialog';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';

export type ConfirmDialogType = 'warning' | 'danger' | 'info';

export interface ConfirmDialogData {
  title: string;
  message: string;
  type?: ConfirmDialogType;
  confirmText?: string;
  cancelText?: string;
  icon?: string;
}

const TYPE_CONFIG: Record<ConfirmDialogType, { icon: string; color: string }> = {
  warning: { icon: 'warning', color: '#F9A825' },
  danger: { icon: 'delete_forever', color: '#C62828' },
  info: { icon: 'info', color: '#0277BD' }
};

@Component({
  selector: 'app-confirm-dialog',
  standalone: true,
  imports: [
    CommonModule,
    MatDialogModule,
    MatButtonModule,
    MatIconModule
  ],
  template: `
    <div class="dialog-container">
      <div class="color-bar" [style.background]="typeColor"></div>
      <div class="content">
        <div class="header">
          <mat-icon [style.color]="typeColor">{{ typeIcon }}</mat-icon>
          <span class="title">{{ data.title }}</span>
        </div>
        <p class="message">{{ data.message }}</p>
        <div class="actions">
          <button mat-button (click)="onCancel()">
            {{ data.cancelText || 'Cancelar' }}
          </button>
          <button mat-stroked-button
            [style.color]="typeColor"
            [style.border-color]="typeColor"
            (click)="onConfirm()">
            {{ data.confirmText || 'Confirmar' }}
          </button>
        </div>
      </div>
    </div>
  `,
  styles: [`
    :host {
      display: block;
    }

    .dialog-container {
      display: flex;
      overflow: hidden;
    }

    .color-bar {
      width: 6px;
      flex-shrink: 0;
    }

    .content {
      flex: 1;
      padding: 20px 24px;
    }

    .header {
      display: flex;
      align-items: center;
      gap: 10px;
      font-size: 16px;
      font-weight: 500;
      margin-bottom: 8px;
      color: #333;
    }

    .message {
      font-size: 14px;
      color: #555;
      line-height: 1.6;
      margin: 0 0 20px;
      white-space: pre-line;
    }

    .actions {
      display: flex;
      justify-content: flex-end;
      gap: 8px;
    }
  `]
})
export class ConfirmDialogComponent {
  readonly typeIcon: string;
  readonly typeColor: string;
  readonly buttonColor: 'warn' | 'primary' | 'accent';

  constructor(
    public dialogRef: MatDialogRef<ConfirmDialogComponent>,
    @Inject(MAT_DIALOG_DATA) public data: ConfirmDialogData
  ) {
    const type = data.type || 'warning';
    const config = TYPE_CONFIG[type];
    this.typeIcon = data.icon || config.icon;
    this.typeColor = config.color;
    this.buttonColor = type === 'danger' ? 'warn' : 'primary';
  }

  onConfirm(): void {
    this.dialogRef.close(true);
  }

  onCancel(): void {
    this.dialogRef.close(false);
  }
}
