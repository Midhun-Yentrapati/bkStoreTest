export interface UserModel {
    id: string;
    fullName: string;
    username: string;
    email: string;
    mobileNumber: string;
    profilePicture?: string;
    password?: string;
    dateOfBirth?: string;
    bio?: string;
    createdAt?: string;
    lastUpdated?: string;
    userRole?: string; // Added to match backend response
    userType?: string; // Added for backward compatibility
    isActive: boolean; // Added to match template usage
    
    // Additional fields from backend User entity
    profilePictureUrl?: string; // Backend field name
    employeeId?: string; // For admin users
    department?: string; // For admin users
    managerId?: string; // For admin users
    permissions?: string[]; // User permissions
    hireDate?: string; // For admin users
    salary?: number; // For admin users (if needed)
    
    // Account verification fields
    isEmailVerified?: boolean;
    isMobileVerified?: boolean;
    emailVerificationToken?: string;
    mobileVerificationOtp?: string;
    
    // Security fields
    lastLoginAt?: string;
    loginAttempts?: number;
    isAccountLocked?: boolean;
    passwordResetToken?: string;
    passwordResetExpiry?: string;
    
    // Two-factor authentication
    isTwoFactorEnabled?: boolean;
    twoFactorSecret?: string;
    
    // Preferences
    preferredLanguage?: string;
    timezone?: string;
    notificationPreferences?: {
        email: boolean;
        sms: boolean;
        push: boolean;
    };
    
    // Shopping preferences
    defaultPaymentMethod?: string;
    defaultShippingAddressId?: string;
    defaultBillingAddressId?: string;
}

// Enhanced user profile interface
export interface UserProfile extends UserModel {
    addresses?: any[]; // User addresses
    orders?: any[]; // User order history
    wishlist?: any[]; // User wishlist
    cart?: any[]; // User cart items
    reviews?: any[]; // User reviews
    totalSpent?: number;
    totalOrders?: number;
    memberSince?: string;
    loyaltyPoints?: number;
}

// User analytics interface for admin dashboard
export interface UserAnalytics {
    totalUsers: number;
    activeUsers: number;
    newUsersThisMonth: number;
    usersByRole: { [key: string]: number };
    usersByType: { [key: string]: number };
    topSpenders: { id: string; name: string; totalSpent: number }[];
    userGrowthTrend: { month: string; newUsers: number; activeUsers: number }[];
    retentionMetrics: {
        dailyActiveUsers: number;
        weeklyActiveUsers: number;
        monthlyActiveUsers: number;
        retentionRate: number;
    };
}

// User utility functions
export class UserUtils {
    static getDisplayName(user: UserModel): string {
        return user.fullName || user.username || user.email;
    }
    
    static getInitials(user: UserModel): string {
        const name = user.fullName || user.username;
        return name.split(' ').map(n => n[0]).join('').toUpperCase().substring(0, 2);
    }
    
    static getRoleDisplayName(role: string): string {
        switch (role?.toUpperCase()) {
            case 'SUPER_ADMIN': return 'Super Admin';
            case 'ADMIN': return 'Admin';
            case 'MANAGER': return 'Manager';
            case 'CUSTOMER': return 'Customer';
            case 'MODERATOR': return 'Moderator';
            case 'SUPPORT': return 'Support';
            default: return role || 'User';
        }
    }
    
    static getTypeDisplayName(type: string): string {
        switch (type?.toUpperCase()) {
            case 'ADMIN': return 'Admin';
            case 'CUSTOMER': return 'Customer';
            default: return type || 'User';
        }
    }
    
    static isAdmin(user: UserModel): boolean {
        return ['SUPER_ADMIN', 'ADMIN', 'MANAGER'].includes(user.userRole?.toUpperCase() || '');
    }
    
    static canManageUsers(user: UserModel): boolean {
        return ['SUPER_ADMIN', 'ADMIN'].includes(user.userRole?.toUpperCase() || '');
    }
    
    static getAccountStatus(user: UserModel): 'active' | 'inactive' | 'locked' | 'pending' {
        if (user.isAccountLocked) return 'locked';
        if (!user.isActive) return 'inactive';
        if (!user.isEmailVerified) return 'pending';
        return 'active';
    }
    
    static getStatusColor(user: UserModel): string {
        const status = UserUtils.getAccountStatus(user);
        switch (status) {
            case 'active': return 'success';
            case 'inactive': return 'secondary';
            case 'locked': return 'danger';
            case 'pending': return 'warning';
            default: return 'secondary';
        }
    }
}
