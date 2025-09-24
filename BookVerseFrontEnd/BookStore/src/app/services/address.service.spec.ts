import { TestBed } from '@angular/core/testing';
import { HttpClientTestingModule, HttpTestingController } from '@angular/common/http/testing';
import { AddressService } from './address.service';
import { Address } from '../models/address.model';
import { AuthService } from './auth.service';

describe('AddressService', () => {
  let service: AddressService;
  let httpMock: HttpTestingController;
  let authService: AuthService;

  const mockAddress1: Address = {
    id: '1',
    userId: 'user1',
    name: 'John Doe',
    phone: '1234567890',
    pincode: '12345',
    address: '123 Test St',
    locality: 'Test Locality',
    city: 'Test City',
    state: 'Test State',
    country: 'Test Country',
    addressType: 'Home',
    isDefault: true
  };

  const mockAddress2: Address = {
    id: '2',
    userId: 'user1',
    name: 'Jane Smith',
    phone: '9876543210',
    pincode: '54321',
    address: '456 Updated St',
    locality: 'Updated Locality',
    city: 'Updated City',
    state: 'Updated State',
    country: 'Updated Country',
    addressType: 'Work',
    isDefault: false
  };

  const mockAddresses: Address[] = [
    mockAddress1,
    mockAddress2
  ];

  beforeEach(() => {
    const authServiceSpy = jasmine.createSpyObj('AuthService', ['getCurrentCustomer']);
    
    TestBed.configureTestingModule({
      imports: [HttpClientTestingModule],
      providers: [
        AddressService,
        { provide: AuthService, useValue: authServiceSpy }
      ]
    });
    service = TestBed.inject(AddressService);
    httpMock = TestBed.inject(HttpTestingController);
    authService = TestBed.inject(AuthService) as jasmine.SpyObj<AuthService>;
    
    // Setup default return value
    (authService.getCurrentCustomer as jasmine.Spy).and.returnValue({
      id: 'user1',
      fullName: 'Test User',
      username: 'testuser',
      email: 'test@example.com',
      mobileNumber: '1234567890'
    });
  });

  afterEach(() => {
    httpMock.verify();
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });

  describe('getAddresses', () => {
    it('should get addresses for current user', () => {
      service.getAddresses().subscribe(addresses => {
        expect(addresses).toEqual([mockAddress1, mockAddress2]);
      });

      const req = httpMock.expectOne('http://localhost:3000/addresses?userId=user1');
      expect(req.request.method).toBe('GET');
      req.flush([mockAddress1, mockAddress2]);
    });

    it('should throw error when user is not logged in', () => {
      (authService.getCurrentCustomer as jasmine.Spy).and.returnValue(null);

      service.getAddresses().subscribe({
        error: (error) => {
          expect(error.message).toBe('User not logged in');
        }
      });
    });
  });

  describe('addAddress', () => {
    it('should add new address successfully', () => {
      const newAddress = { ...mockAddress1, id: undefined };
      
      service.addAddress(newAddress).subscribe(address => {
        expect(address).toEqual(mockAddress1);
      });

      const req = httpMock.expectOne('http://localhost:3000/addresses');
      expect(req.request.method).toBe('POST');
      expect(req.request.body).toEqual(newAddress);
      req.flush(mockAddress1);
    });

    it('should handle errors when adding address', () => {
      const newAddress = { ...mockAddress1, id: undefined };
      
      service.addAddress(newAddress).subscribe({
        error: (error) => {
          expect(error).toBeTruthy();
        }
      });

      const req = httpMock.expectOne('http://localhost:3000/addresses');
      req.error(new ErrorEvent('Bad Request'), { status: 400 });
    });
  });

  describe('updateAddress', () => {
    it('should update address successfully', () => {
      const updatedAddress = { ...mockAddress1, street: '456 Updated St' };
      
      service.updateAddress('1', updatedAddress).subscribe(address => {
        expect(address).toEqual(updatedAddress);
      });

      const req = httpMock.expectOne('http://localhost:3000/addresses/1');
      expect(req.request.method).toBe('PUT');
      expect(req.request.body).toEqual(updatedAddress);
      req.flush(updatedAddress);
    });

    it('should handle errors when updating address', () => {
      const updatedAddress = { ...mockAddress1, street: '456 Updated St' };
      
      service.updateAddress('1', updatedAddress).subscribe({
        error: (error) => {
          expect(error).toBeTruthy();
        }
      });

      const req = httpMock.expectOne('http://localhost:3000/addresses/1');
      req.error(new ErrorEvent('Not found'), { status: 404 });
    });
  });

  describe('deleteAddress', () => {
    it('should delete address successfully', () => {
      service.deleteAddress('1').subscribe(response => {
        expect(response).toBeTruthy();
      });

      const req = httpMock.expectOne('http://localhost:3000/addresses/1');
      expect(req.request.method).toBe('DELETE');
      req.flush({ success: true });
    });

    it('should handle errors when deleting address', () => {
      service.deleteAddress('1').subscribe({
        error: (error) => {
          expect(error).toBeTruthy();
        }
      });

      const req = httpMock.expectOne('http://localhost:3000/addresses/1');
      req.error(new ErrorEvent('Not found'), { status: 404 });
    });
  });

  describe('setDefaultAddress', () => {
    it('should set address as default', () => {
      service.setDefaultAddress('1').subscribe(address => {
        expect(address).toBeTruthy();
        expect(address.id).toBe('1');
      });

      const req = httpMock.expectOne('http://localhost:3000/addresses?userId=user1');
      expect(req.request.method).toBe('GET');
      req.flush([mockAddress1, mockAddress2]);

      const updateReq1 = httpMock.expectOne('http://localhost:3000/addresses/1');
      expect(updateReq1.request.method).toBe('PUT');
      updateReq1.flush({ ...mockAddress1, isDefault: true });

      const updateReq2 = httpMock.expectOne('http://localhost:3000/addresses/2');
      expect(updateReq2.request.method).toBe('PUT');
      updateReq2.flush({ ...mockAddress2, isDefault: false });
    });

    it('should handle error when setting default address', () => {
      service.setDefaultAddress('999').subscribe({
        error: (error) => {
          expect(error).toBe('Failed to set default address');
        }
      });

      const req = httpMock.expectOne('http://localhost:3000/addresses?userId=user1');
      expect(req.request.method).toBe('GET');
      req.flush([]);
    });
  });

  describe('getAddressById', () => {
    it('should get address by ID successfully', () => {
      service.getAddressById('1').subscribe(address => {
        expect(address).toEqual(mockAddress1);
      });

      const req = httpMock.expectOne('http://localhost:3000/addresses/1');
      expect(req.request.method).toBe('GET');
      req.flush(mockAddress1);
    });

    it('should handle errors when getting address by ID', () => {
      service.getAddressById('999').subscribe({
        error: (error) => {
          expect(error).toBeTruthy();
        }
      });

      const req = httpMock.expectOne('http://localhost:3000/addresses/999');
      req.error(new ErrorEvent('Not found'), { status: 404 });
    });
  });

  describe('error handling', () => {
    it('should handle network errors gracefully', () => {
      service.getAddresses().subscribe({
        error: (error) => {
          expect(error).toBeTruthy();
        }
      });

      const req = httpMock.expectOne('http://localhost:3000/addresses?userId=user1');
      req.error(new ErrorEvent('Network error'));
    });

    it('should handle server errors gracefully', () => {
      service.getAddresses().subscribe({
        error: (error) => {
          expect(error).toBeTruthy();
        }
      });

      const req = httpMock.expectOne('http://localhost:3000/addresses?userId=user1');
      req.error(new ErrorEvent('Internal Server Error'), { status: 500 });
    });
  });
}); 