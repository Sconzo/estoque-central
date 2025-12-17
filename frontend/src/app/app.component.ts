import { Component, OnInit, Inject, PLATFORM_ID } from '@angular/core';
import { RouterOutlet } from '@angular/router';
import { isPlatformBrowser } from '@angular/common';

/**
 * AppComponent - Root component
 *
 * Implements keyboard detection for accessibility:
 * - Adds 'user-is-tabbing' class to body when Tab is pressed
 * - Removes class when mouse is used
 * - Enables focus-visible styles only for keyboard users
 */
@Component({
  selector: 'app-root',
  imports: [RouterOutlet],
  templateUrl: './app.component.html',
  styleUrl: './app.component.scss'
})
export class AppComponent implements OnInit {
  title = 'frontend';

  constructor(@Inject(PLATFORM_ID) private platformId: Object) {}

  ngOnInit(): void {
    // Only run in browser (not during SSR)
    if (isPlatformBrowser(this.platformId)) {
      this.setupKeyboardDetection();
    }
  }

  /**
   * Setup keyboard vs mouse detection for focus styles
   * Tab key adds 'user-is-tabbing' class
   * Mouse click removes it
   */
  private setupKeyboardDetection(): void {
    // Detect Tab key press
    window.addEventListener('keydown', (event: KeyboardEvent) => {
      if (event.key === 'Tab') {
        document.body.classList.add('user-is-tabbing');
      }
    });

    // Detect mouse usage
    window.addEventListener('mousedown', () => {
      document.body.classList.remove('user-is-tabbing');
    });
  }
}
