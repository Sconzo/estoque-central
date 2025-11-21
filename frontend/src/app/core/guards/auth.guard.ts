import { Injectable } from '@angular/core';
import { Router, UrlTree } from '@angular/router';
import { Observable } from 'rxjs';
import { AuthService } from '../auth/auth.service';

/**
 * AuthGuard - Protects routes requiring authentication
 *
 * Redirects unauthenticated users to login page.
 */
@Injectable({
  providedIn: 'root'
})
export class AuthGuard {

  constructor(
    private authService: AuthService,
    private router: Router
  ) {}

  canActivate(): boolean | UrlTree | Observable<boolean | UrlTree> | Promise<boolean | UrlTree> {
    if (this.authService.isAuthenticated()) {
      return true;
    }

    // Redirect to login page
    return this.router.createUrlTree(['/login']);
  }
}
