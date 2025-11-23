import { Routes } from '@angular/router';
import { ReceivingOrderSelectionComponent } from './components/receiving-order-selection/receiving-order-selection.component';
import { BarcodeScanningComponent } from './components/barcode-scanning/barcode-scanning.component';
import { ReceivingSummaryComponent } from './components/receiving-summary/receiving-summary.component';

export const RECEIVING_ROUTES: Routes = [
  {
    path: '',
    component: ReceivingOrderSelectionComponent
  },
  {
    path: 'scan/:id',
    component: BarcodeScanningComponent
  },
  {
    path: 'summary/:id',
    component: ReceivingSummaryComponent
  }
];
