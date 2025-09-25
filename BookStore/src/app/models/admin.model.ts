export interface AdminUser {
  id: string;
  username: string;
  email: string;
  fullName: string;
  passwordHash: string;
  role: 'super_admin' | 'admin' | 'moderator' | 'editor';
  permissions: {
    users: boolean;
    books: boolean;
    orders: boolean;
    categories: boolean;
    coupons: boolean;
    reviews: boolean;
    analytics: boolean;
  };
  lastLogin?: string;
  failedLoginAttempts: number;
  lockedUntil?: string;
  isActive: boolean;
  twoFactorEnabled: boolean;
  twoFactorSecret?: string;
  createdBy?: string;
  createdAt: string;
  updatedAt: string;
}

export interface AdminSession {
  id: string;
  adminId: string;
  sessionToken: string;
  ipAddress: string;
  userAgent: string;
  expiresAt: string;
  isActive: boolean;
  createdAt: string;
  updatedAt: string;
}

export interface AdminLoginRequest {
  username: string;
  password: string;
  twoFactorCode?: string;
}

export interface AdminLoginResponse {
  success: boolean;
  token?: string;
  user?: Omit<AdminUser, 'passwordHash' | 'twoFactorSecret'>;
  message?: string;
}
