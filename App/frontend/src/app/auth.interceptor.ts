
import { Injectable } from '@angular/core';
import {
  HttpRequest,
  HttpHandler,
  HttpEvent,
  HttpInterceptor,
  HttpErrorResponse
} from '@angular/common/http';
import { catchError, Observable, throwError } from 'rxjs';
import { Router } from '@angular/router';
import { AuthService } from './services/auth.service';
import { environment } from 'src/environment/environment';
import { ToastService } from 'angular-toastify';
import { jwtDecode } from 'jwt-decode';

@Injectable()
export class AuthInterceptor implements HttpInterceptor {

  constructor(
    private authService: AuthService,
    private router: Router,
    private toastService: ToastService
  ) {}

  intercept(
    request: HttpRequest<unknown>,
    next: HttpHandler
  ): Observable<HttpEvent<unknown>> {

    const token = localStorage.getItem(environment.tokenName);

    if (token) {
      try {
        const decodedToken: any = jwtDecode(token);

        if (
          decodedToken &&
          decodedToken.exp &&
          decodedToken.exp * 1000 > Date.now()
        ) {
          // Add the JWT token to the request.
          // CORS response headers are handled by the backend.
          const authRequest = request.clone({
            setHeaders: {
              Authorization: `Bearer ${token}`
            }
          });

          return next.handle(authRequest).pipe(
            catchError((error: HttpErrorResponse) => {
              if (error.status === 401) {
                console.error(
                  'Unauthorized access. Redirecting to login page...'
                );

                localStorage.clear();
                this.router.navigate(['/login']);
                this.toastService.error(
                  'Unauthorized access. Please log in again.'
                );
              }

              return throwError(() => error);
            })
          );

        } else {
          // Token has expired.
          console.error(
            'Session expired. Redirecting to login page...'
          );

          localStorage.clear();
          this.router.navigate(['/login']);
          this.toastService.error(
            'Session expired. Please log in again.'
          );

          return next.handle(request);
        }

      } catch (error) {
        // Token is invalid.
        console.error('Error decoding JWT token:', error);
        console.error(
          'Invalid token. Redirecting to login page...'
        );

        localStorage.clear();
        this.router.navigate(['/login']);
        this.toastService.error(
          'Invalid token. Please log in again.'
        );

        return next.handle(request);
      }

    } else {
      // No token is present.
      return next.handle(request).pipe(
        catchError((error: HttpErrorResponse) => {
          if (error.status === 401) {
            console.error(
              'Unauthorized access. Redirecting to login page...'
            );

            this.router.navigate(['/login']);
            this.toastService.error(
              'Unauthorized access. Please log in again.'
            );
          }

          return throwError(() => error);
        })
      );
    }
  }
}
