import { Injectable } from '@angular/core';
import { CanActivate } from '@angular/router';

@Injectable({
  providedIn: 'root'
})
export class PublicGuard implements CanActivate {
  canActivate(): boolean {
    // Always allow access to public routes
    return true;
  }
}