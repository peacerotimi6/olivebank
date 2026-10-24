
import { AuthService } from 'src/app/services/auth.service';
import { environment } from 'src/environment/environment';
import { Component } from '@angular/core';
import { Router } from '@angular/router';

@Component({
  selector: 'app-header',
  templateUrl: './header.component.html',
  styleUrls: ['./header.component.css'],
})
export class HeaderComponent {
  private authTokenName = environment.tokenName;

  constructor(
    private authService: AuthService,
    private router: Router
  ) {}

  isLoggedIn() {
    return this.authService.isLoggedIn();
  }

  checkScreenSize() {
    return window.innerWidth < 768;
  }

  logout(): void {
    // Clear the local login token immediately.
    localStorage.removeItem(this.authTokenName);

    // Navigate to the login page without waiting for the backend.
    this.router.navigate(['/login']);

    // Notify the backend if possible.
    this.authService.logOutUser().subscribe({
      error: (error: any) => {
        console.warn('Backend logout request failed:', error);
      },
    });
  }
}
