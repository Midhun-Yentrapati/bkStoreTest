# 🚀 BookStore Microservices Architecture - Startup Guide

## 📋 Infrastructure Overview

Your BookStore application now has a complete microservices architecture with:

### **Infrastructure Servers:**
1. **Eureka Server** (Port 8761) - Service Discovery Registry
2. **API Gateway** (Port 8080) - Single Entry Point & Load Balancer  
3. **Config Server** (Port 8888) - Centralized Configuration Management

### **Business Services:**
1. **User Authentication Service** (Port 8081) - JWT Authentication & User Management
2. **Admin BackOffice Service** (Port 8084) - Admin Operations & Audit Logging
3. **Book Catalogue Service** (Port 8082) - Book Management & Inventory

---

## 🚀 Startup Sequence (CRITICAL ORDER)

### **Step 1: Start Eureka Server (FIRST)**
```bash
cd "c:\Angular Projects\BookStore (3)\Backend\eureka-server\eureka-server"
./mvnw spring-boot:run
```
- **URL**: http://localhost:8761
- **Dashboard**: http://localhost:8761 (Eureka Console)
- **Wait**: Until you see "Started EurekaServerApplication"

### **Step 2: Start Config Server (SECOND)**
```bash
cd "c:\Angular Projects\BookStore (3)\Backend\config-server\config-server"
./mvnw spring-boot:run
```
- **URL**: http://localhost:8888
- **Health Check**: http://localhost:8888/actuator/health
- **Wait**: Until you see "Started ConfigServerApplication"

### **Step 3: Start API Gateway (THIRD)**
```bash
cd "c:\Angular Projects\BookStore (3)\Backend\api-gateway\api-gateway"
./mvnw spring-boot:run
```
- **URL**: http://localhost:8080
- **Health Check**: http://localhost:8080/actuator/health
- **Wait**: Until you see "Started ApiGatewayApplication"

### **Step 4: Start Business Services (ANY ORDER)**

**User Authentication Service:**
```bash
cd "c:\Angular Projects\BookStore (3)\Backend\user-authentication-service"
./mvnw spring-boot:run
```
- **Direct URL**: http://localhost:8081
- **Via Gateway**: http://localhost:8080/api/auth/**, http://localhost:8080/api/users/**

**Admin BackOffice Service:**
```bash
cd "c:\Angular Projects\BookStore (3)\Backend\admin-backoffice-service"
./mvnw spring-boot:run
```
- **Direct URL**: http://localhost:8084
- **Via Gateway**: http://localhost:8080/api/admin/**

**Book Catalogue Service:**
```bash
cd "c:\Angular Projects\BookStore (3)\Backend\book-catalogue-service"
./mvnw spring-boot:run
```
- **Direct URL**: http://localhost:8082
- **Via Gateway**: http://localhost:8080/api/books/**

---

## 🔗 API Gateway Routes Configuration

The API Gateway automatically routes requests to appropriate services:

### **Authentication Routes:**
- `http://localhost:8080/api/auth/**` → User Authentication Service (8081)
- `http://localhost:8080/api/users/**` → User Authentication Service (8081)

### **Admin Routes:**
- `http://localhost:8080/api/admin/**` → Admin BackOffice Service (8084)

### **Book Routes:**
- `http://localhost:8080/api/books/**` → Book Catalogue Service (8082)

### **Test Routes:**
- `http://localhost:8080/api/test/**` → User Authentication Service (8081)

---

## 🧪 Testing Your Setup

### **1. Verify Eureka Dashboard:**
- Open: http://localhost:8761
- Should show all registered services

### **2. Test API Gateway Health:**
```bash
curl http://localhost:8080/actuator/health
```

### **3. Test Service Discovery:**
```bash
# Via API Gateway
curl http://localhost:8080/api/test/health

# Direct to service
curl http://localhost:8081/api/test/health
```

### **4. Test Authentication Flow:**
```bash
# Register a user via API Gateway
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "username": "testuser",
    "email": "test@example.com",
    "password": "TestPassword123!",
    "fullName": "Test User"
  }'

# Login via API Gateway
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "usernameOrEmail": "testuser",
    "password": "TestPassword123!"
  }'
```

---

## 🔧 Configuration Management

### **Config Server Features:**
- **Local File System**: Configurations stored in `src/main/resources/config/`
- **Service-Specific**: Each service has its own configuration file
- **Environment Profiles**: Support for dev/prod configurations
- **Dynamic Refresh**: Configuration changes without restart

### **Configuration Files:**
- `application.yml` - Default configuration for all services
- `user-authentication-service.yml` - Authentication service specific
- `admin-backoffice-service.yml` - Admin service specific  
- `book-catalogue-service.yml` - Book service specific

---

## 🛡️ Security & CORS

### **CORS Configuration:**
- **Allowed Origins**: http://localhost:4200, http://localhost:3000
- **Allowed Methods**: GET, POST, PUT, DELETE, OPTIONS
- **Credentials**: Enabled for JWT token handling

### **JWT Authentication:**
- **Access Token**: 24 hours expiration
- **Refresh Token**: 7 days expiration
- **Secret Key**: Configurable via Config Server

---

## 📊 Monitoring & Health Checks

### **Available Endpoints:**
- **Eureka Dashboard**: http://localhost:8761
- **Gateway Actuator**: http://localhost:8080/actuator
- **Config Server Health**: http://localhost:8888/actuator/health
- **Service Health**: http://localhost:8080/api/test/health

### **Service Registration:**
All services automatically register with Eureka and are discoverable via:
- Service names in Eureka dashboard
- Load-balanced routing through API Gateway
- Health monitoring and failover

---

## 🚨 Troubleshooting

### **Common Issues:**

1. **Eureka Connection Refused:**
   - Ensure Eureka Server is started first
   - Check port 8761 is not in use

2. **Gateway Routing Issues:**
   - Verify all services are registered in Eureka
   - Check service names match configuration

3. **Database Connection Issues:**
   - User Auth Service uses MySQL (ensure MySQL is running)
   - Other services use H2 (no external dependencies)

4. **CORS Issues:**
   - All CORS is handled by API Gateway
   - Angular should call http://localhost:8080/api/**

---

## 🎯 Next Steps

### **For Angular Integration:**
1. Update Angular services to use API Gateway (port 8080)
2. All API calls should go through: `http://localhost:8080/api/**`
3. JWT tokens will be handled automatically
4. CORS is pre-configured

### **For Production:**
1. Switch Config Server to Git repository
2. Enable security on Config Server
3. Configure MySQL for all services
4. Set up proper SSL certificates
5. Configure load balancing

---

## ✅ Success Indicators

**Your microservices architecture is working when:**
- ✅ Eureka Dashboard shows all 3 business services registered
- ✅ API Gateway routes requests to correct services
- ✅ Config Server provides centralized configuration
- ✅ JWT authentication works via API Gateway
- ✅ Angular can call http://localhost:8080/api/** successfully

**🎉 Congratulations! Your BookStore now has enterprise-grade microservices architecture!**
