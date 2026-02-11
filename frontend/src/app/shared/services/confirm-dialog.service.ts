import { Injectable } from '@angular/core';
import { MatDialog } from '@angular/material/dialog';
import { Observable, map } from 'rxjs';
import {
  ConfirmDialogComponent,
  ConfirmDialogData,
  ConfirmDialogType
} from '../components/confirm-dialog/confirm-dialog.component';

@Injectable({
  providedIn: 'root'
})
export class ConfirmDialogService {
  constructor(private dialog: MatDialog) {}

  confirm(data: ConfirmDialogData): Observable<boolean> {
    const dialogRef = this.dialog.open(ConfirmDialogComponent, {
      width: '480px',
      panelClass: 'confirm-dialog-panel',
      data: {
        ...data,
        type: data.type || 'warning',
        cancelText: data.cancelText || 'Cancelar',
        confirmText: data.confirmText || 'Confirmar'
      }
    });

    return dialogRef.afterClosed().pipe(
      map(result => result === true)
    );
  }

  confirmDanger(data: Omit<ConfirmDialogData, 'type'>): Observable<boolean> {
    return this.confirm({ ...data, type: 'danger' });
  }

  confirmInfo(data: Omit<ConfirmDialogData, 'type'>): Observable<boolean> {
    return this.confirm({ ...data, type: 'info' });
  }
}
