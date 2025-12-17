import { Component, Input } from '@angular/core';
import { MatCardModule } from '@angular/material/card';
import { MatIconModule } from '@angular/material/icon';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-metric-card',
  standalone: true,
  imports: [CommonModule, MatCardModule, MatIconModule],
  template: `
    <mat-card
      class="metric-card"
      [style.border-left]="'4px solid ' + color"
      role="region"
      [attr.aria-label]="title + ' metric'">

      <mat-card-header>
        <mat-icon [style.color]="color" aria-hidden="true">{{ icon }}</mat-icon>
        <mat-card-title>{{ title }}</mat-card-title>
      </mat-card-header>

      <mat-card-content>
        <div class="metric-value" [style.color]="color" aria-live="polite">
          {{ value }}
        </div>
        <div
          class="metric-change"
          [class.positive]="changePercent > 0"
          [class.negative]="changePercent < 0"
          *ngIf="changePercent !== undefined">
          <span [attr.aria-label]="'Mudança do período anterior: ' + changePercent + '%'">
            {{ changePercent > 0 ? '↑' : '↓' }} {{ changePercent > 0 ? '+' : '' }}{{ changePercent }}%
          </span>
        </div>
      </mat-card-content>
    </mat-card>
  `,
  styles: [`
    .metric-card {
      margin: 16px;

      mat-card-header {
        display: flex;
        align-items: center;
        gap: 8px;
        margin-bottom: 16px;

        mat-icon {
          font-size: 24px;
          width: 24px;
          height: 24px;
        }
      }

      .metric-value {
        font-size: 32px;
        font-weight: 600;
        margin-bottom: 8px;
      }

      .metric-change {
        font-size: 14px;

        &.positive { color: #2E7D32; }
        &.negative { color: #C62828; }
      }
    }
  `]
})
export class MetricCardComponent {
  @Input() title!: string;
  @Input() value!: string;
  @Input() changePercent?: number;
  @Input() icon: string = 'info';
  @Input() color: string = '#6A1B9A';
}
