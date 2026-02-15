import { ApplicationConfig, provideZoneChangeDetection, LOCALE_ID, APP_INITIALIZER } from '@angular/core';
import { provideRouter, Router } from '@angular/router';
import { provideHttpClient, withInterceptorsFromDi } from '@angular/common/http';
import { HTTP_INTERCEPTORS } from '@angular/common/http';
import { provideAnimationsAsync } from '@angular/platform-browser/animations/async';
import { registerLocaleData } from '@angular/common';
import localePt from '@angular/common/locales/pt';
import { MAT_DATE_LOCALE } from '@angular/material/core';

import { routes } from './app.routes';
import { JwtInterceptor } from './core/interceptors/jwt.interceptor';
import { TenantInterceptor } from './core/interceptors/tenant.interceptor';
import { TenantService } from './core/services/tenant.service';
import { AuthService } from './core/auth/auth.service';
import { LoginComponent } from './features/auth/login/login.component';

// Registrar locale pt-BR
registerLocaleData(localePt);

/**
 * Story 9.4 - AC2: Page Refresh Handling
 * Initializer function to restore tenant context on page refresh
 */
export function initializeTenantContext(
  tenantService: TenantService,
  authService: AuthService,
  router: Router
): () => Promise<void> {
  return () => {
    return new Promise<void>((resolve) => {
      // Check if user is authenticated
      const isAuthenticated = authService.isAuthenticated();

      if (isAuthenticated) {
        // Check if tenant context exists in localStorage
        const hasTenantContext = tenantService.hasTenantContext();

        console.log('🔄 App initializing - authenticated:', isAuthenticated, 'has tenant:', hasTenantContext);

        if (!hasTenantContext) {
          // AC2: No tenant context - redirect to select-company
          console.log('⚠️ No tenant context found - redirecting to /select-company');
          router.navigate(['/select-company']);
        } else {
          // AC2: Tenant context exists - restore from localStorage
          const tenantId = tenantService.getCurrentTenant();
          console.log('✅ Tenant context restored:', tenantId);
        }
      } else {
        console.log('🔓 User not authenticated - skipping tenant initialization');
      }

      resolve();
    });
  };
}

export const appConfig: ApplicationConfig = {
  providers: [
    provideZoneChangeDetection({ eventCoalescing: true }),
    provideRouter(routes),
    provideHttpClient(withInterceptorsFromDi()),
    provideAnimationsAsync(),
    { provide: LOCALE_ID, useValue: 'pt-BR' },
    { provide: MAT_DATE_LOCALE, useValue: 'pt-BR' },
    {
      provide: HTTP_INTERCEPTORS,
      useClass: JwtInterceptor,
      multi: true
    },
    {
      provide: HTTP_INTERCEPTORS,
      useClass: TenantInterceptor,
      multi: true
    },
    // Story 9.4 - AC2: Page refresh handling with APP_INITIALIZER
    {
      provide: APP_INITIALIZER,
      useFactory: initializeTenantContext,
      deps: [TenantService, AuthService, Router],
      multi: true
    }
  ]
};
