# MySQL Setup Guide for Book Catalog Service

## Prerequisites
- MySQL Server installed and running
- MySQL root user with password `root123`

## Database Setup

### 1. Create Database
Run the following SQL commands in MySQL:

```sql
-- Create database
CREATE DATABASE IF NOT EXISTS catalog_schema CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- Use the database
USE catalog_schema;

-- Grant privileges (if needed)
GRANT ALL PRIVILEGES ON catalog_schema.* TO 'root'@'localhost';
FLUSH PRIVILEGES;
```

### 2. Verify Connection
Test the connection with these credentials:
- **Host**: localhost
- **Port**: 3306
- **Database**: catalog_schema
- **Username**: root
- **Password**: root123

### 3. Application Configuration
The service is configured with:
```properties
spring.datasource.url=jdbc:mysql://localhost:3306/catalog_schema
spring.datasource.username=root
spring.datasource.password=root123
spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver
spring.jpa.hibernate.ddl-auto=update
```

## Starting the Service

1. Ensure MySQL is running
2. Create the database using the SQL script above
3. Start the book-catalog service:
   ```bash
   cd Backend/book-catalog
   mvn spring-boot:run
   ```

## Troubleshooting

### Common Issues:
1. **Connection refused**: Ensure MySQL is running on port 3306
2. **Access denied**: Verify root user password is `root123`
3. **Database doesn't exist**: Run the database creation script
4. **Driver not found**: MySQL connector is included in pom.xml

### Verify Setup:
- Service should start on port 8082
- Check logs for successful database connection
- Access Swagger UI: http://localhost:8082/swagger-ui.html
- Health check: http://localhost:8082/actuator/health

## Database Schema
Tables will be automatically created by Hibernate when the service starts:
- BOOKS
- CATEGORIES  
- BOOK_CATEGORIES
- BOOK_IMAGES
- BOOK_REVIEWS
- INVENTORY_ALERTS
