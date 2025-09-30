# Admin Role-Based System Testing Guide for Postman

##  Setup Instructions

### 1. Start the Application
- Make sure your Spring Boot application is running on port 8080
- Check: http://localhost:8080/actuator/health

### 2. Database Setup
- Run the admin_role_system_setup.sql script first
- This creates the admin_users table and sample data

##  Postman Collection Setup

### Base URL: http://localhost:8080

##  Authentication Endpoints

### 1. Register New Admin User
**POST** `/api/auth/register`
```json
{
    "username": "testadmin",
    "email": "test@admin.com",
    "password": "password123",
    "adminRole": "ADMIN",
    "fullName": "Test Administrator",
    "employeeId": "EMP001",
    "department": "IT",
    "permissions": ["USER_READ", "USER_WRITE", "BOOK_READ", "BOOK_WRITE"]
}
```

### 2. Login (Get JWT Token)
**POST** `/api/auth/login`
```json
{
    "username": "testadmin",
    "password": "password123"
}
```
**Response:** You'll get a JWT token - copy this for other requests!

### 3. Get Admin Profile
**GET** `/api/auth/profile`
**Headers:** `Authorization: Bearer YOUR_JWT_TOKEN`

##  Protected Endpoints Testing

### Admin Activity Logs (Requires Authentication)

#### 1. Log Activity
**POST** `/api/admin/log-activity`
**Headers:** `Authorization: Bearer YOUR_JWT_TOKEN`
```json
{
    "adminId": "your-admin-id",
    "adminUsername": "testadmin",
    "actionType": "CREATE",
    "actionDescription": "Created new book",
    "status": "SUCCESS",
    "serviceName": "BookService",
    "endpoint": "/api/books",
    "httpMethod": "POST",
    "requestIp": "192.168.1.1",
    "userAgent": "PostmanRuntime/7.28.0",
    "resourceId": "book-123",
    "resourceType": "BOOK"
}
```

#### 2. Get All Activity Logs
**GET** `/api/admin/activity-logs`
**Headers:** `Authorization: Bearer YOUR_JWT_TOKEN`

#### 3. Get Activity Logs by Admin
**GET** `/api/admin/activity-logs/admin/{adminId}`
**Headers:** `Authorization: Bearer YOUR_JWT_TOKEN`

### Analytics Endpoints (Requires Authentication)

#### 1. Get Recent Sales
**GET** `/api/analytics/daily-sales/recent`
**Headers:** `Authorization: Bearer YOUR_JWT_TOKEN`

#### 2. Get Dashboard Data
**GET** `/api/analytics/dashboard?startDate=2025-08-01&endDate=2025-09-10`
**Headers:** `Authorization: Bearer YOUR_JWT_TOKEN`

##  Testing Scenarios

### Scenario 1: Test Authentication
1. Register a new admin user
2. Login to get JWT token
3. Use token to access protected endpoints

### Scenario 2: Test Role-Based Access
1. Create users with different roles (SUPER_ADMIN, ADMIN, MANAGER)
2. Test that each role can only access permitted endpoints

### Scenario 3: Test Without Authentication
1. Try accessing protected endpoints without JWT token
2. Should get 401 Unauthorized

### Scenario 4: Test Invalid Token
1. Use an expired or invalid JWT token
2. Should get 401 Unauthorized

##  Sample JWT Token Structure
When you decode a JWT token, you'll see:
```json
{
  "sub": "testadmin",
  "adminRole": "ADMIN",
  "permissions": ["USER_READ", "USER_WRITE", "BOOK_READ", "BOOK_WRITE"],
  "email": "test@admin.com",
  "fullName": "Test Administrator",
  "employeeId": "EMP001",
  "iat": 1694352000,
  "exp": 1694438400
}
```

##  Expected Responses

### Successful Login Response:
```json
{
    "jwt": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "message": "Login successful"
}
```

### Error Response (No Auth):
```json
{
    "timestamp": "2025-09-10T10:30:00.000+00:00",
    "status": 401,
    "error": "Unauthorized",
    "message": "Full authentication is required to access this resource",
    "path": "/api/admin/activity-logs"
}
```

##  Quick Test Checklist
- [ ] Application starts successfully
- [ ] Database tables created
- [ ] Can register admin user
- [ ] Can login and get JWT token
- [ ] Can access protected endpoints with valid token
- [ ] Cannot access protected endpoints without token
- [ ] Role-based permissions work correctly
- [ ] Activity logging works
- [ ] Analytics endpoints work

##  Troubleshooting
- If 401 errors: Check JWT token in Authorization header
- If 403 errors: Check user permissions
- If 500 errors: Check database connection and table structure
- If connection refused: Make sure application is running on port 8080
