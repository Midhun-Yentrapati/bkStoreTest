# Swagger API Documentation Setup

## Overview
Swagger has been successfully integrated into the BookVerse Book Catalog Service. This provides interactive API documentation for all REST endpoints.

## Access URLs

### Swagger UI (Interactive Documentation)
- **URL**: `http://localhost:8080/swagger-ui/index.html`
- **Alternative URL**: `http://localhost:8080/swagger-ui.html` (redirects to above)
- **Description**: Interactive web interface to explore and test API endpoints

### OpenAPI JSON Specification
- **URL**: `http://localhost:8080/v3/api-docs`
- **Description**: Raw OpenAPI 3.0 specification in JSON format

## Features Added

### 1. Dependencies
- **SpringDoc OpenAPI 3**: Latest version for Spring Boot 3.x compatibility
- Automatic integration with Spring Boot web layer

### 2. Configuration
- Custom OpenAPI configuration with API metadata
- Server information for development and production
- Contact and license information

### 3. API Documentation
- **Books API**: Complete CRUD operations with search and analytics
- **Categories API**: Category management operations  
- **Inventory Alerts API**: Inventory monitoring and alerting

### 4. Enhanced Documentation Features
- Operation summaries and descriptions
- Parameter documentation
- Response status codes and schemas
- Request/Response examples
- Error response documentation

## How to Use

### Starting the Application
1. Navigate to the project directory:
   ```bash
   cd BookVerseBackEnd/book-catalog
   ```

2. Start the Spring Boot application:
   ```bash
   ./mvnw spring-boot:run
   ```
   or
   ```bash
   mvn spring-boot:run
   ```

### Accessing Swagger UI
1. Open your web browser
2. Navigate to: `http://localhost:8080/swagger-ui.html`
3. Explore the available endpoints organized by tags:
   - **Books**: Book management operations
   - **Categories**: Category management operations
   - **Inventory Alerts**: Inventory alert operations

### Testing Endpoints
1. Click on any endpoint to expand it
2. Click "Try it out" to enable the test interface
3. Fill in required parameters
4. Click "Execute" to send the request
5. View the response body, headers, and status code

## Configuration Details

### Application Properties
The following properties have been added to `application.properties`:

```properties
# Swagger/OpenAPI Configuration
springdoc.api-docs.path=/api-docs
springdoc.swagger-ui.path=/swagger-ui.html
springdoc.swagger-ui.enabled=true
springdoc.swagger-ui.operationsSorter=method
springdoc.swagger-ui.tagsSorter=alpha
springdoc.swagger-ui.tryItOutEnabled=true
springdoc.swagger-ui.filter=true
```

### Annotations Used
- `@Tag`: Groups related operations
- `@Operation`: Describes individual endpoints
- `@ApiResponse`: Documents response codes and schemas
- `@Parameter`: Describes request parameters
- `@Schema`: Defines data models

## Benefits

1. **Interactive Testing**: Test API endpoints directly from the browser
2. **Comprehensive Documentation**: Auto-generated docs stay in sync with code
3. **Developer Experience**: Easy API exploration for frontend developers
4. **Integration Ready**: Standard OpenAPI format for API gateways and tools
5. **Professional Presentation**: Clean, organized API documentation

## Next Steps

1. **Security Documentation**: Add authentication/authorization documentation when implemented
2. **Examples**: Add more request/response examples for complex operations
3. **Custom Styling**: Customize Swagger UI appearance if needed
4. **API Versioning**: Document API versions as the service evolves

## Troubleshooting

### Common Issues
1. **404 on Swagger UI**: Ensure the application is running on port 8080
2. **Empty API List**: Check that controllers have proper annotations
3. **CORS Issues**: Verify CORS configuration in application.properties

### Support
For issues or questions about the API documentation, refer to the SpringDoc OpenAPI documentation or contact the development team. 