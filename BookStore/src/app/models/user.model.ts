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
}
