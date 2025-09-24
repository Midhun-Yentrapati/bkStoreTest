import { UserModel } from './user.model';

describe('UserModel', () => {
  let mockUser: UserModel;

  beforeEach(() => {
    mockUser = {
      id: '1',
      fullName: 'John Doe',
      username: 'johndoe',
      email: 'john@example.com',
      mobileNumber: '1234567890',
      profilePicture: 'profile.jpg',
      password: 'password123',
      dateOfBirth: '1990-01-01',
      bio: 'Test bio',
      createdAt: '2023-01-01',
      lastUpdated: '2023-12-01'
    };
  });

  it('should create a valid UserModel instance', () => {
    expect(mockUser).toBeTruthy();
    expect(mockUser.id).toBe('1');
    expect(mockUser.fullName).toBe('John Doe');
    expect(mockUser.username).toBe('johndoe');
  });

  it('should have all required properties', () => {
    expect(mockUser.id).toBeDefined();
    expect(mockUser.fullName).toBeDefined();
    expect(mockUser.username).toBeDefined();
    expect(mockUser.email).toBeDefined();
    expect(mockUser.mobileNumber).toBeDefined();
  });

  it('should have optional properties', () => {
    expect(mockUser.profilePicture).toBeDefined();
    expect(mockUser.password).toBeDefined();
    expect(mockUser.dateOfBirth).toBeDefined();
    expect(mockUser.bio).toBeDefined();
    expect(mockUser.createdAt).toBeDefined();
    expect(mockUser.lastUpdated).toBeDefined();
  });

  it('should have correct data types for required properties', () => {
    expect(typeof mockUser.id).toBe('string');
    expect(typeof mockUser.fullName).toBe('string');
    expect(typeof mockUser.username).toBe('string');
    expect(typeof mockUser.email).toBe('string');
    expect(typeof mockUser.mobileNumber).toBe('string');
  });

  it('should have correct data types for optional properties', () => {
    expect(typeof mockUser.profilePicture).toBe('string');
    expect(typeof mockUser.password).toBe('string');
    expect(typeof mockUser.dateOfBirth).toBe('string');
    expect(typeof mockUser.bio).toBe('string');
    expect(typeof mockUser.createdAt).toBe('string');
    expect(typeof mockUser.lastUpdated).toBe('string');
  });

  it('should work with minimal required properties', () => {
    const minimalUser: UserModel = {
      id: '2',
      fullName: 'Jane Doe',
      username: 'janedoe',
      email: 'jane@example.com',
      mobileNumber: '0987654321'
    };

    expect(minimalUser).toBeTruthy();
    expect(minimalUser.id).toBe('2');
    expect(minimalUser.profilePicture).toBeUndefined();
  });
}); 