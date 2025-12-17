import { Component, Input, Output, EventEmitter } from '@angular/core';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-primary-button',
  standalone: true,
  imports: [CommonModule, MatButtonModule, MatIconModule, MatProgressSpinnerModule],
  template: `
    <button
      mat-raised-button
      [color]="color"
      [disabled]="disabled || loading"
      (click)="onClick.emit()"
      [attr.aria-label]="ariaLabel"
      [attr.aria-busy]="loading">

      <mat-spinner *ngIf="loading" diameter="16" aria-hidden="true"></mat-spinner>
      <mat-icon *ngIf="icon && !loading" aria-hidden="true">{{ icon }}</mat-icon>
      <span>{{ loading ? loadingText : label }}</span>
    </button>
  `,
  styles: [`
    button {
      min-height: 40px;

      mat-spinner {
        display: inline-block;
        margin-right: 8px;
      }

      mat-icon {
        margin-right: 8px;
      }
    }
  `]
})
export class PrimaryButtonComponent {
  @Input() label: string = 'Confirmar';
  @Input() loadingText: string = 'Processando...';
  @Input() icon?: string;
  @Input() color: 'primary' | 'accent' | 'warn' = 'primary';
  @Input() disabled: boolean = false;
  @Input() loading: boolean = false;
  @Input() ariaLabel?: string;
  @Output() onClick = new EventEmitter<void>();
}
