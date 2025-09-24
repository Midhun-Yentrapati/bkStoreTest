import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, of, throwError } from 'rxjs';
import { Address } from '../models/address.model';
import { AuthService } from './auth.service';

@Injectable({
  providedIn: 'root'
})
export class AddressService {
  private apiUrl = 'http://localhost:3000/addresses';

  constructor(
    private http: HttpClient,
    private authService: AuthService
  ) { }

  /**
   * Get all addresses for current user
   */
  getAddresses(): Observable<Address[]> {
    const userId = this.authService.getCurrentCustomer()?.id;
    if (!userId) {
      return throwError(() => new Error('User not logged in'));
    }
    return this.http.get<Address[]>(`${this.apiUrl}?userId=${userId}`);
  }

  /**
   * Get a specific address by ID
   */
  getAddressById(id: string): Observable<Address> {
    return this.http.get<Address>(`${this.apiUrl}/${id}`);
  }

  /**
   * Add a new address
   */
  addAddress(address: Omit<Address, 'id'>): Observable<Address> {
    const currentUser = this.authService.getCurrentCustomer();
    if (!currentUser) {
      return throwError(() => new Error('User not logged in'));
    }

    const newAddress: Address = {
      ...address,
      id: crypto.randomUUID(),
      userId: currentUser?.id,
      createdAt: new Date(),
      updatedAt: new Date()
    };

    return this.http.post<Address>(this.apiUrl, newAddress);
  }

  /**
   * Update an existing address
   */
  updateAddress(id: string, address: Address): Observable<Address> {
    const updatedAddress: Address = {
      ...address,
      updatedAt: new Date()
    };
    return this.http.put<Address>(`${this.apiUrl}/${id}`, updatedAddress);
  }

  /**
   * Delete an address (soft delete)
   */
  deleteAddress(id: string): Observable<void> {
    return this.http.patch<void>(`${this.apiUrl}/${id}`, { 
      isActive: false,
      updatedAt: new Date()
    });
  }

  /**
   * Permanently delete an address
   */
  permanentDeleteAddress(id: string): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${id}`);
  }

  /**
   * Set an address as default
   */
  setDefaultAddress(id: string): Observable<Address> {
    return new Observable(observer => {
      this.getAddresses().subscribe(addresses => {
        const updatePromises = addresses.map(addr => {
          const updatedAddr = { 
            ...addr, 
            isDefault: addr.id === id,
            updatedAt: new Date()
          };
          return this.http.put<Address>(`${this.apiUrl}/${addr.id}`, updatedAddr).toPromise();
        });

        Promise.all(updatePromises).then(results => {
          const defaultAddress = results.find(addr => addr?.id === id);
          if (defaultAddress) {
            observer.next(defaultAddress);
            observer.complete();
          } else {
            observer.error('Failed to set default address');
          }
        }).catch(error => {
          observer.error(error);
        });
      });
    });
  }

  /**
   * Get the default address
   */
  getDefaultAddress(): Observable<Address | null> {
    return new Observable(observer => {
      this.getAddresses().subscribe(addresses => {
        const defaultAddress = addresses.find(addr => addr.isDefault) || null;
        observer.next(defaultAddress);
        observer.complete();
      });
    });
  }

  /**
   * Validate pincode
   */
  validatePincode(pincode: string): Observable<{isValid: boolean, location?: string}> {
    // Simulate pincode validation
    const isValid = /^\d{6}$/.test(pincode);
    return of({
      isValid,
      location: isValid ? 'Serviceable Area' : undefined
    });
  }

  /**
   * Get address suggestions based on pincode
   */
  getAddressSuggestions(pincode: string): Observable<{city: string, state: string}[]> {
    // Simulate address suggestions based on pincode
    const suggestions = [
      { city: 'Sample City', state: 'Sample State' }
    ];
    return of(suggestions);
  }

  /**
   * Geocode address to get coordinates
   */
  geocodeAddress(address: string): Observable<{latitude: number, longitude: number} | null> {
    // Simulate geocoding service
    return of({
      latitude: 12.9716,
      longitude: 77.5946
    });
  }

  /**
   * Get addresses by type
   */
  getAddressesByType(type: Address['addressType']): Observable<Address[]> {
    return new Observable(observer => {
      this.getAddresses().subscribe(addresses => {
        const filteredAddresses = addresses.filter(addr => addr.addressType === type);
        observer.next(filteredAddresses);
        observer.complete();
      });
    });
  }

  /**
   * Search addresses
   */
  searchAddresses(query: string): Observable<Address[]> {
    const userId = this.authService.getCurrentCustomer()?.id;
    const searchParams = `?q=${encodeURIComponent(query)}${userId ? `&userId=${userId}` : ''}`;
    return this.http.get<Address[]>(`${this.apiUrl}/search${searchParams}`);
  }
} 