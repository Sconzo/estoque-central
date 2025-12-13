import { Component, OnInit, AfterViewInit } from '@angular/core';
import { Router } from '@angular/router';
import { CommonModule } from '@angular/common';
import { AuthService } from '../../../core/auth/auth.service';
import { environment } from '../../../../environments/environment';

declare const google: any;

/**
 * LoginComponent - Google OAuth 2.0 login page
 *
 * Displays "Sign in with Google" button and handles OAuth flow.
 */
@Component({
  selector: 'app-login',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './login.component.html',
  styleUrls: ['./login.component.scss']
})
export class LoginComponent implements OnInit, AfterViewInit {
  isLoading = false;
  errorMessage: string | null = null;

  // Get Google Client ID from environment
  private readonly GOOGLE_CLIENT_ID = environment.googleClientId;

  // IMPORTANT: Replace with actual tenant ID (or get from subdomain/user selection)
  private readonly TENANT_ID = '00000000-0000-0000-0000-000000000000';

  constructor(
    private authService: AuthService,
    private router: Router
  ) {}

  ngOnInit(): void {
    // Check if already authenticated
    if (this.authService.isAuthenticated()) {
      this.router.navigate(['/dashboard']);
      return;
    }
  }

  ngAfterViewInit(): void {
    // Load Google Sign-In script after view is initialized
    this.loadGoogleSignInScript();
  }

  /**
   * Loads Google Sign-In JavaScript library.
   */
  private loadGoogleSignInScript(): void {
    if (typeof google !== 'undefined' && google.accounts) {
      this.initializeGoogleSignIn();
      return;
    }

    const script = document.createElement('script');
    script.src = 'https://accounts.google.com/gsi/client';
    script.async = true;
    script.defer = true;
    script.onload = () => {
      this.initializeGoogleSignIn();
    };
    document.head.appendChild(script);
  }

  /**
   * Initializes Google Sign-In button.
   */
  private initializeGoogleSignIn(): void {
    google.accounts.id.initialize({
      client_id: this.GOOGLE_CLIENT_ID,
      callback: this.handleGoogleSignIn.bind(this)
    });

    google.accounts.id.renderButton(
      document.getElementById('google-signin-button'),
      {
        theme: 'outline',
        size: 'large',
        text: 'signin_with',
        shape: 'rectangular',
        logo_alignment: 'left'
      }
    );
  }

  /**
   * Handles Google Sign-In response.
   *
   * @param response Google Sign-In response containing credential (ID token)
   */
  private handleGoogleSignIn(response: any): void {
    if (!response.credential) {
      this.showError('Falha ao obter credenciais do Google');
      return;
    }

    this.isLoading = true;
    this.errorMessage = null;

    // Call backend to validate Google token and get JWT
    this.authService.login(response.credential, this.TENANT_ID)
      .subscribe({
        next: () => {
          console.log('Login successful!');
          this.router.navigate(['/dashboard']);
        },
        error: (error) => {
          console.error('Login failed', error);
          this.showError('Falha na autenticação. Tente novamente.');
          this.isLoading = false;
        }
      });
  }

  /**
   * Displays error message.
   *
   * @param message Error message
   */
  private showError(message: string): void {
    this.errorMessage = message;
    setTimeout(() => {
      this.errorMessage = null;
    }, 5000);
  }
}
