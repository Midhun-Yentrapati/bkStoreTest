import { Injectable } from '@angular/core';
import { HttpInterceptor, HttpRequest, HttpHandler, HttpEvent } from '@angular/common/http';
import { Observable } from 'rxjs';

@Injectable()
export class PublicApiInterceptor implements HttpInterceptor {

  intercept(req: HttpRequest<any>, next: HttpHandler): Observable<HttpEvent<any>> {
    // For public book endpoints, don't add authentication headers
    if (this.isPublicBookEndpoint(req.url)) {
      const publicReq = req.clone({
        setHeaders: {
          'Content-Type': 'application/json'
        }
      });
      return next.handle(publicReq);
    }

    return next.handle(req);
  }

  private isPublicBookEndpoint(url: string): boolean {
    const publicEndpoints = [
      '/api/books',
      '/api/categories',
      '/api/reviews/book'
    ];
    
    return publicEndpoints.some(endpoint => url.includes(endpoint)) && 
           (url.includes('GET') || !url.includes('POST') && !url.includes('PUT') && !url.includes('DELETE'));
  }
}