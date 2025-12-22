import { Component } from '@angular/core';

/**
 * Tenant management feature module.
 *
 * Placeholder component for Epic 9 - Multi-company context management.
 * Will contain company selection, context switching, and tenant settings.
 *
 * @since 1.0
 */
@Component({
  selector: 'app-tenant',
  standalone: true,
  template: `
    <div class="tenant-container">
      <h2>Tenant Management</h2>
      <p>Feature will be implemented in subsequent stories (Epic 9)</p>
    </div>
  `,
  styles: [`
    .tenant-container {
      padding: 20px;
    }
  `]
})
export class TenantComponent {}
