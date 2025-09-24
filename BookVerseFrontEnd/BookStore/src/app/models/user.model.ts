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
}

export interface AdminUser {
    id: string;
    username: string;
    email: string;
    passwordHash: string;
}

export interface User {
    id: string;
    username: string;
    email: string;
    passwordHash: string; // Although not displayed, it's part of the data
}
