import { Injectable } from '@angular/core';
import { HttpInterceptor, HttpRequest, HttpHandler, HttpEvent, HttpErrorResponse } from '@angular/common/http';
import { Observable, throwError } from 'rxjs';
import { catchError, timeout } from 'rxjs/operators';
import { LoggerService } from '../services/logger.service';
import { environment } from '../config/environment';

@Injectable()
export class SecurityInterceptor implements HttpInterceptor {

  constructor(private logger: LoggerService) {}

  intercept(req: HttpRequest<any>, next: HttpHandler): Observable<HttpEvent<any>> {
    let secureReq = req;

    // Add security headers
    secureReq = req.clone({
      setHeaders: {
        'X-Content-Type-Options': 'nosniff',
        'X-Frame-Options': 'DENY',
        'X-XSS-Protection': '1; mode=block',
        'Referrer-Policy': 'strict-origin-when-cross-origin'
      }
    });

    // Ensure HTTPS in production
    if (environment.production && !secureReq.url.startsWith('https://')) {
      this.logger.error('Insecure HTTP request blocked in production');
      return throwError(() => new Error('Insecure request blocked'));
    }

    // Add timeout and error handling
    return next.handle(secureReq).pipe(
      timeout(30000), // 30 second timeout
      catchError((error: HttpErrorResponse) => {
        this.logger.error('HTTP request failed', {
          status: error.status,
          url: error.url,
          message: error.message
        });
        return throwError(() => error);
      })
    );
  }
}