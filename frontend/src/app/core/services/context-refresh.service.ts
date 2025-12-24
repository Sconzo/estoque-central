import { Injectable, effect } from '@angular/core';
import { Subject } from 'rxjs';
import { TenantService } from './tenant.service';

/**
 * ContextRefreshService - Story 9.5: Real-time Context Synchronization
 *
 * Service to help components reactively refresh data when tenant context changes.
 *
 * Story 9.5 - AC2: Data Refresh on Context Switch
 * - Provides Observable for components to listen to context changes
 * - Automatically clears previous tenant's data from component state
 * - Triggers data re-fetch with new tenantId
 *
 * Usage Example:
 * ```typescript
 * export class ProductListComponent implements OnInit {
 *   products: Product[] = [];
 *
 *   constructor(
 *     private contextRefresh: ContextRefreshService,
 *     private productService: ProductService
 *   ) {
 *     // Subscribe to context changes
 *     this.contextRefresh.contextChanged$.subscribe(() => {
 *       this.loadProducts(); // Re-fetch products for new tenant
 *     });
 *   }
 *
 *   ngOnInit() {
 *     this.loadProducts();
 *   }
 *
 *   private loadProducts() {
 *     this.productService.getProducts().subscribe(products => {
 *       this.products = products; // Will show new tenant's products
 *     });
 *   }
 * }
 * ```
 *
 * @since Epic 9 - Story 9.5
 */
@Injectable({
  providedIn: 'root'
})
export class ContextRefreshService {
  // AC2: Observable that emits when tenant context changes
  // Components can subscribe to this to re-fetch their data
  private contextChangedSubject = new Subject<string | null>();
  public contextChanged$ = this.contextChangedSubject.asObservable();

  constructor(private tenantService: TenantService) {
    // Story 9.5 - AC1: Use Angular effect to watch Signal changes
    // Automatically emits when currentTenant$ Signal updates
    effect(() => {
      const newTenantId = this.tenantService.currentTenant$();
      console.log('🔔 Context changed detected (Signal effect):', newTenantId);

      // AC2: Notify all subscribed components to refresh their data
      this.contextChangedSubject.next(newTenantId);
    });
  }

  /**
   * Story 9.5 - AC2: Manually trigger data refresh
   * Components can call this if they need to force a refresh
   */
  triggerRefresh(): void {
    const currentTenant = this.tenantService.getCurrentTenant();
    console.log('🔄 Manual refresh triggered for tenant:', currentTenant);
    this.contextChangedSubject.next(currentTenant);
  }

  /**
   * Story 9.5 - AC2: Get current tenant ID
   * Helper method for components to get current context
   */
  getCurrentTenant(): string | null {
    return this.tenantService.getCurrentTenant();
  }
}
