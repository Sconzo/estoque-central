import { Injectable } from '@angular/core';
import { MatSnackBar, MatSnackBarConfig } from '@angular/material/snack-bar';

/**
 * FeedbackService - Centralized user feedback using MatSnackBar
 *
 * Provides consistent feedback patterns across the application:
 * - Success: 3s duration, green, checkmark icon
 * - Error: 5s duration, red, X icon, optional retry action
 * - Warning: 4s duration, amber, warning icon
 * - Info: 3s duration, blue, info icon
 *
 * All snackbars include ARIA live regions for screen reader accessibility.
 */
@Injectable({
  providedIn: 'root'
})
export class FeedbackService {
  constructor(private snackBar: MatSnackBar) {}

  /**
   * Show success message
   *
   * @param message - Success message to display
   * @param duration - Optional duration in ms (default: 3000)
   *
   * @example
   * ```typescript
   * this.feedback.showSuccess('Produto salvo com sucesso!');
   * ```
   */
  showSuccess(message: string, duration: number = 3000): void {
    const config: MatSnackBarConfig = {
      duration,
      horizontalPosition: 'end',
      verticalPosition: 'top',
      panelClass: ['success-snackbar'],
      politeness: 'polite' // ARIA: aria-live="polite"
    };

    this.snackBar.open(`✓ ${message}`, 'Fechar', config);
  }

  /**
   * Show error message with optional retry action
   *
   * @param message - Error message to display
   * @param retryFn - Optional callback to execute when user clicks "Tentar Novamente"
   * @param duration - Optional duration in ms (default: 5000)
   *
   * @example
   * ```typescript
   * this.feedback.showError(
   *   'Erro ao salvar produto',
   *   () => this.saveProduct()
   * );
   * ```
   */
  showError(message: string, retryFn?: () => void, duration: number = 5000): void {
    const config: MatSnackBarConfig = {
      duration,
      horizontalPosition: 'end',
      verticalPosition: 'top',
      panelClass: ['error-snackbar'],
      politeness: 'assertive' // ARIA: aria-live="assertive"
    };

    const actionLabel = retryFn ? 'Tentar Novamente' : 'Fechar';
    const snackBarRef = this.snackBar.open(`✕ ${message}`, actionLabel, config);

    if (retryFn) {
      snackBarRef.onAction().subscribe(() => {
        retryFn();
      });
    }
  }

  /**
   * Show warning message
   *
   * @param message - Warning message to display
   * @param duration - Optional duration in ms (default: 4000)
   *
   * @example
   * ```typescript
   * this.feedback.showWarning('Estoque baixo para este produto');
   * ```
   */
  showWarning(message: string, duration: number = 4000): void {
    const config: MatSnackBarConfig = {
      duration,
      horizontalPosition: 'end',
      verticalPosition: 'top',
      panelClass: ['warning-snackbar'],
      politeness: 'polite' // ARIA: aria-live="polite"
    };

    this.snackBar.open(`⚠ ${message}`, 'Fechar', config);
  }

  /**
   * Show info message
   *
   * @param message - Info message to display
   * @param duration - Optional duration in ms (default: 3000)
   *
   * @example
   * ```typescript
   * this.feedback.showInfo('Dados sincronizados com sucesso');
   * ```
   */
  showInfo(message: string, duration: number = 3000): void {
    const config: MatSnackBarConfig = {
      duration,
      horizontalPosition: 'end',
      verticalPosition: 'top',
      panelClass: ['info-snackbar'],
      politeness: 'polite' // ARIA: aria-live="polite"
    };

    this.snackBar.open(`ℹ ${message}`, 'Fechar', config);
  }

  /**
   * Dismiss all active snackbars
   *
   * @example
   * ```typescript
   * this.feedback.dismissAll();
   * ```
   */
  dismissAll(): void {
    this.snackBar.dismiss();
  }
}
