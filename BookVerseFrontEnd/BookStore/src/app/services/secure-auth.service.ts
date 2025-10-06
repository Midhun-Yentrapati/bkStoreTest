import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, throwError } from 'rxjs';
import { map, catchError } from 'rxjs/operators';
import { LoggerService } from './logger.service';
import { InputSanitizerService } from './input-sanitizer.service';
import { environment } from '../config/environment';

@Injectable({
  providedIn: 'root'
})
export class SecureAuthService {
  private readonly apiUrl = environment.authUrl;

  constructor(
    private http: HttpClient,
    private logger: LoggerService,
    private sanitizer: InputSanitizerService
  ) {}

  login(credentials: { identifier: string; password: string }): Observable<any> {
    const sanitizedCredentials = {
      identifier: this.sanitizer.sanitizeInput(credentials.identifier),
      password: credentials.password
    };

    if (!this.sanitizer.validateEmail(sanitizedCredentials.identifier)) {
      return throwError(() => new Error('Invalid email format'));
    }

    this.logger.info('Login attempt initiated');

    return this.http.post<any>(`${this.apiUrl}/login`, sanitizedCredentials).pipe(
      map(response => {
        if (response?.success) {
          this.logger.info('Login successful');
          return response;
        }
        throw new Error('Login failed');
      }),
      catchError(error => {
        this.logger.error('Login failed');
        return throwError(() => new Error('Authentication failed'));
      })
    );
  }

  register(userData: any): Observable<any> {
    const sanitizedData = {
      fullName: this.sanitizer.sanitizeInput(userData.fullName),
      username: this.sanitizer.sanitizeInput(userData.username),
      email: this.sanitizer.sanitizeInput(userData.email),
      mobileNumber: this.sanitizer.sanitizeInput(userData.mobileNumber),
      password: userData.password
    };

    if (!this.sanitizer.validateEmail(sanitizedData.email)) {
      return throwError(() => new Error('Invalid email format'));
    }

    const passwordValidation = this.sanitizer.validatePassword(sanitizedData.password);
    if (!passwordValidation.valid) {
      return throwError(() => new Error(passwordValidation.errors.join(', ')));
    }

    this.logger.info('Registration attempt initiated');

    return this.http.post<any>(`${this.apiUrl}/register`, sanitizedData).pipe(
      map(response => {
        if (response?.success) {
          this.logger.info('Registration successful');
          return response;
        }
        throw new Error('Registration failed');
      }),
      catchError(error => {
        this.logger.error('Registration failed');
        return throwError(() => new Error('Registration failed'));
      })
    );
  }

  logout(): Observable<any> {
    this.logger.info('Logout initiated');
    return this.http.post(`${this.apiUrl}/logout`, {}).pipe(
      catchError(error => {
        this.logger.warn('Logout request failed');
        return throwError(() => error);
      })
    );
  }
}