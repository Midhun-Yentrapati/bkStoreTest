import { Injectable } from '@angular/core';
import { Observable, of } from 'rxjs';

@Injectable({
  providedIn: 'root'
})
export class ImageUploadService {

  constructor() { }

  /**
   * Handle image upload - either URL or file
   * For now, this returns the URL as-is or converts file to data URL
   * In production, you would upload the file to a server and return the server URL
   */
  processImage(input: string | File): Observable<string> {
    if (typeof input === 'string') {
      // It's a URL, return as-is
      return of(input);
    } else {
      // It's a file, convert to data URL for preview
      // In production, you would upload to server here
      return new Observable(observer => {
        const reader = new FileReader();
        reader.onload = (e) => {
          observer.next(e.target?.result as string);
          observer.complete();
        };
        reader.onerror = (error) => {
          observer.error(error);
        };
        reader.readAsDataURL(input);
      });
    }
  }

  /**
   * Validate image URL
   */
  isValidImageUrl(url: string): boolean {
    const urlPattern = /^https?:\/\/.+\.(jpg|jpeg|png|gif|webp)$/i;
    return urlPattern.test(url);
  }

  /**
   * Validate image file
   */
  isValidImageFile(file: File): boolean {
    const validTypes = ['image/jpeg', 'image/png', 'image/gif', 'image/webp'];
    const maxSize = 5 * 1024 * 1024; // 5MB
    
    return validTypes.includes(file.type) && file.size <= maxSize;
  }
} 