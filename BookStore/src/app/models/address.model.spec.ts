import { Address } from './address.model';

describe('Address', () => {
  let mockAddress: Address;

  beforeEach(() => {
    mockAddress = {
      id: 'addr1',
      name: 'John Doe',
      phone: '1234567890',
      alternatePhone: '0987654321',
      email: 'john@example.com',
      pincode: '12345',
      address: '123 Test Street',
      locality: 'Test Area',
      city: 'Test City',
      state: 'Test State',
      country: 'Test Country',
      landmark: 'Near Test Landmark',
      addressType: 'Home',
      isDefault: true,
      isActive: true,
      coordinates: {
        latitude: 40.7128,
        longitude: -74.0060
      },
      createdAt: new Date('2023-01-01'),
      updatedAt: new Date('2023-12-01'),
      userId: 'user1'
    };
  });

  it('should create a valid Address instance', () => {
    expect(mockAddress).toBeTruthy();
    expect(mockAddress.id).toBe('addr1');
    expect(mockAddress.name).toBe('John Doe');
    expect(mockAddress.addressType).toBe('Home');
  });

  it('should have all required properties', () => {
    expect(mockAddress.name).toBeDefined();
    expect(mockAddress.phone).toBeDefined();
    expect(mockAddress.pincode).toBeDefined();
    expect(mockAddress.address).toBeDefined();
    expect(mockAddress.locality).toBeDefined();
    expect(mockAddress.city).toBeDefined();
    expect(mockAddress.state).toBeDefined();
    expect(mockAddress.addressType).toBeDefined();
  });

  it('should have optional properties', () => {
    expect(mockAddress.id).toBeDefined();
    expect(mockAddress.alternatePhone).toBeDefined();
    expect(mockAddress.email).toBeDefined();
    expect(mockAddress.country).toBeDefined();
    expect(mockAddress.landmark).toBeDefined();
    expect(mockAddress.isDefault).toBeDefined();
    expect(mockAddress.isActive).toBeDefined();
    expect(mockAddress.coordinates).toBeDefined();
    expect(mockAddress.createdAt).toBeDefined();
    expect(mockAddress.updatedAt).toBeDefined();
    expect(mockAddress.userId).toBeDefined();
  });

  it('should have correct data types for required properties', () => {
    expect(typeof mockAddress.name).toBe('string');
    expect(typeof mockAddress.phone).toBe('string');
    expect(typeof mockAddress.pincode).toBe('string');
    expect(typeof mockAddress.address).toBe('string');
    expect(typeof mockAddress.locality).toBe('string');
    expect(typeof mockAddress.city).toBe('string');
    expect(typeof mockAddress.state).toBe('string');
    expect(typeof mockAddress.addressType).toBe('string');
  });

  it('should have valid address type values', () => {
    const validAddressTypes = ['Home', 'Work', 'Other'];
    expect(validAddressTypes).toContain(mockAddress.addressType);
  });

  it('should have correct coordinates structure', () => {
    if (mockAddress.coordinates) {
      expect(typeof mockAddress.coordinates.latitude).toBe('number');
      expect(typeof mockAddress.coordinates.longitude).toBe('number');
    }
  });

  it('should work with minimal required properties', () => {
    const minimalAddress: Address = {
      name: 'Jane Doe',
      phone: '0987654321',
      pincode: '54321',
      address: '456 Test Ave',
      locality: 'Test Locality',
      city: 'Test City',
      state: 'Test State',
      addressType: 'Work'
    };

    expect(minimalAddress).toBeTruthy();
    expect(minimalAddress.name).toBe('Jane Doe');
    expect(minimalAddress.id).toBeUndefined();
  });

  it('should handle boolean flags correctly', () => {
    expect(mockAddress.isDefault).toBe(true);
    expect(mockAddress.isActive).toBe(true);
  });
}); 