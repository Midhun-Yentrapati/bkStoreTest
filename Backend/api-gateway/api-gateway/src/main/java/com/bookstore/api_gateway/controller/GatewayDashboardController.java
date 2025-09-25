package com.bookstore.api_gateway.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.http.ResponseEntity;
import org.springframework.http.MediaType;
import reactor.core.publisher.Mono;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Gateway Dashboard Controller
 * 
 * Provides API endpoints to view and monitor API Gateway routes
 * Access: http://localhost:8090/gateway/dashboard
 */
@RestController
public class GatewayDashboardController {

    @Autowired
    private RouteLocator routeLocator;

    /**
     * Gateway Dashboard HTML Page
     */
    @GetMapping(value = "/gateway/dashboard", produces = MediaType.TEXT_HTML_VALUE)
    public Mono<String> dashboard() {
        return Mono.just(getDashboardHtml());
    }

    /**
     * API endpoint to get route information as JSON
     */
    @GetMapping("/gateway/api/routes")
    public Mono<ResponseEntity<List<Map<String, Object>>>> getRoutes() {
        return routeLocator.getRoutes()
            .map(route -> {
                Map<String, Object> routeInfo = new HashMap<>();
                routeInfo.put("id", route.getId());
                routeInfo.put("uri", route.getUri().toString());
                routeInfo.put("predicates", route.getPredicate().toString());
                routeInfo.put("filters", route.getFilters().stream()
                    .map(filter -> filter.toString())
                    .collect(Collectors.toList()));
                routeInfo.put("order", route.getOrder());
                routeInfo.put("metadata", route.getMetadata());
                return routeInfo;
            })
            .collectList()
            .map(ResponseEntity::ok)
            .onErrorReturn(ResponseEntity.ok(Collections.emptyList()));
    }

    /**
     * API endpoint to get gateway health and statistics
     */
    @GetMapping("/gateway/api/health")
    public Mono<ResponseEntity<Map<String, Object>>> getGatewayHealth() {
        Map<String, Object> health = new HashMap<>();
        health.put("status", "UP");
        health.put("timestamp", new Date());
        health.put("gatewayPort", "8090");
        health.put("eurekaServer", "http://localhost:8761");
        
        return routeLocator.getRoutes()
            .count()
            .map(count -> {
                health.put("totalRoutes", count);
                return ResponseEntity.ok(health);
            })
            .onErrorReturn(ResponseEntity.ok(health));
    }

    /**
     * API endpoint to get service endpoints for testing
     */
    @GetMapping("/gateway/api/endpoints")
    public Mono<ResponseEntity<Map<String, Object>>> getServiceEndpoints() {
        Map<String, Object> endpoints = new HashMap<>();
        
        // Infrastructure Services
        Map<String, String> infrastructure = new HashMap<>();
        infrastructure.put("Eureka Server", "http://localhost:8761");
        infrastructure.put("API Gateway", "http://localhost:8090");
        infrastructure.put("Gateway Dashboard", "http://localhost:8090/gateway/dashboard");
        
        // Business Services
        Map<String, String> services = new HashMap<>();
        services.put("User Authentication", "http://localhost:8081");
        services.put("Book Catalogue", "http://localhost:8082");
        services.put("Admin BackOffice", "http://localhost:8084");
        
        // Swagger UIs
        Map<String, String> swagger = new HashMap<>();
        swagger.put("User Auth API", "http://localhost:8081/swagger-ui/index.html");
        swagger.put("Book Catalogue API", "http://localhost:8082/swagger-ui/index.html");
        swagger.put("Admin BackOffice API", "http://localhost:8084/swagger-ui/index.html");
        
        // Health Checks
        Map<String, String> health = new HashMap<>();
        health.put("User Auth Health", "http://localhost:8081/api/test/health");
        health.put("Book Catalogue Health", "http://localhost:8082/api/books/health");
        health.put("Admin BackOffice Health", "http://localhost:8084/api/test/health");
        
        endpoints.put("infrastructure", infrastructure);
        endpoints.put("services", services);
        endpoints.put("swagger", swagger);
        endpoints.put("health", health);
        
        return Mono.just(ResponseEntity.ok(endpoints));
    }

    /**
     * Generate HTML content for the dashboard
     */
    private String getDashboardHtml() {
        return "<!DOCTYPE html>\n" +
            "<html lang=\"en\">\n" +
            "<head>\n" +
            "    <meta charset=\"UTF-8\">\n" +
            "    <meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">\n" +
            "    <title>API Gateway Dashboard - BookStore Microservices</title>\n" +
            "    <style>\n" +
            "        * { margin: 0; padding: 0; box-sizing: border-box; }\n" +
            "        body { font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif; background: linear-gradient(135deg, #667eea 0%, #764ba2 100%); min-height: 100vh; color: #333; }\n" +
            "        .container { max-width: 1200px; margin: 0 auto; padding: 20px; }\n" +
            "        .header { background: rgba(255, 255, 255, 0.95); backdrop-filter: blur(10px); border-radius: 15px; padding: 30px; margin-bottom: 30px; box-shadow: 0 8px 32px rgba(0, 0, 0, 0.1); text-align: center; }\n" +
            "        .header h1 { color: #2c3e50; font-size: 2.5em; margin-bottom: 10px; font-weight: 700; }\n" +
            "        .header p { color: #7f8c8d; font-size: 1.1em; }\n" +
            "        .status-bar { display: flex; gap: 20px; margin-bottom: 30px; flex-wrap: wrap; }\n" +
            "        .status-card { flex: 1; background: rgba(255, 255, 255, 0.95); backdrop-filter: blur(10px); border-radius: 12px; padding: 20px; box-shadow: 0 4px 20px rgba(0, 0, 0, 0.1); min-width: 200px; }\n" +
            "        .status-card h3 { color: #2c3e50; margin-bottom: 10px; font-size: 1.1em; }\n" +
            "        .status-value { font-size: 2em; font-weight: bold; color: #27ae60; }\n" +
            "        .dashboard-grid { display: grid; grid-template-columns: 1fr 1fr; gap: 30px; margin-bottom: 30px; }\n" +
            "        @media (max-width: 768px) { .dashboard-grid { grid-template-columns: 1fr; } .status-bar { flex-direction: column; } }\n" +
            "        .card { background: rgba(255, 255, 255, 0.95); backdrop-filter: blur(10px); border-radius: 15px; padding: 25px; box-shadow: 0 8px 32px rgba(0, 0, 0, 0.1); }\n" +
            "        .card h2 { color: #2c3e50; margin-bottom: 20px; font-size: 1.4em; border-bottom: 2px solid #3498db; padding-bottom: 10px; }\n" +
            "        .route-item { background: #f8f9fa; border-radius: 8px; padding: 15px; margin-bottom: 15px; border-left: 4px solid #3498db; }\n" +
            "        .route-id { font-weight: bold; color: #2c3e50; font-size: 1.1em; margin-bottom: 8px; }\n" +
            "        .route-uri { color: #27ae60; font-family: 'Courier New', monospace; margin-bottom: 5px; }\n" +
            "        .route-predicates { color: #e74c3c; font-family: 'Courier New', monospace; font-size: 0.9em; margin-bottom: 5px; }\n" +
            "        .endpoint-item { display: flex; justify-content: space-between; align-items: center; padding: 12px; margin-bottom: 10px; background: #f8f9fa; border-radius: 8px; border-left: 4px solid #9b59b6; }\n" +
            "        .endpoint-name { font-weight: 600; color: #2c3e50; }\n" +
            "        .endpoint-url { font-family: 'Courier New', monospace; color: #3498db; text-decoration: none; padding: 5px 10px; background: rgba(52, 152, 219, 0.1); border-radius: 4px; transition: all 0.3s ease; }\n" +
            "        .endpoint-url:hover { background: rgba(52, 152, 219, 0.2); transform: translateY(-1px); }\n" +
            "        .refresh-btn { background: linear-gradient(45deg, #3498db, #2980b9); color: white; border: none; padding: 12px 24px; border-radius: 8px; cursor: pointer; font-size: 1em; font-weight: 600; transition: all 0.3s ease; margin-bottom: 20px; }\n" +
            "        .refresh-btn:hover { transform: translateY(-2px); box-shadow: 0 4px 15px rgba(52, 152, 219, 0.4); }\n" +
            "        .loading { text-align: center; color: #7f8c8d; font-style: italic; }\n" +
            "        .timestamp { text-align: center; color: #7f8c8d; font-size: 0.9em; margin-top: 20px; }\n" +
            "        .full-width { grid-column: 1 / -1; }\n" +
            "        .status-indicator { display: inline-block; width: 12px; height: 12px; border-radius: 50%; background-color: #27ae60; margin-right: 8px; animation: pulse 2s infinite; }\n" +
            "        @keyframes pulse { 0% { opacity: 1; } 50% { opacity: 0.5; } 100% { opacity: 1; } }\n" +
            "    </style>\n" +
            "</head>\n" +
            "<body>\n" +
            "    <div class=\"container\">\n" +
            "        <div class=\"header\">\n" +
            "            <h1>🚪 API Gateway Dashboard</h1>\n" +
            "            <p>BookStore Microservices Architecture - Real-time Route Monitoring</p>\n" +
            "        </div>\n" +
            "        <div class=\"status-bar\">\n" +
            "            <div class=\"status-card\">\n" +
            "                <h3><span class=\"status-indicator\"></span>Gateway Status</h3>\n" +
            "                <div class=\"status-value\" id=\"gatewayStatus\">UP</div>\n" +
            "            </div>\n" +
            "            <div class=\"status-card\">\n" +
            "                <h3>Total Routes</h3>\n" +
            "                <div class=\"status-value\" id=\"totalRoutes\">-</div>\n" +
            "            </div>\n" +
            "            <div class=\"status-card\">\n" +
            "                <h3>Gateway Port</h3>\n" +
            "                <div class=\"status-value\">8090</div>\n" +
            "            </div>\n" +
            "            <div class=\"status-card\">\n" +
            "                <h3>Eureka Server</h3>\n" +
            "                <div class=\"status-value\">8761</div>\n" +
            "            </div>\n" +
            "        </div>\n" +
            "        <button class=\"refresh-btn\" onclick=\"refreshData()\">🔄 Refresh Data</button>\n" +
            "        <div class=\"dashboard-grid\">\n" +
            "            <div class=\"card\">\n" +
            "                <h2>🛣️ Active Routes</h2>\n" +
            "                <div id=\"routesContainer\" class=\"loading\">Loading routes...</div>\n" +
            "            </div>\n" +
            "            <div class=\"card\">\n" +
            "                <h2>🔗 Service Endpoints</h2>\n" +
            "                <div id=\"endpointsContainer\" class=\"loading\">Loading endpoints...</div>\n" +
            "            </div>\n" +
            "        </div>\n" +
            "        <div class=\"card full-width\">\n" +
            "            <h2>📊 Quick Access Links</h2>\n" +
            "            <div id=\"quickLinksContainer\">\n" +
            "                <div class=\"endpoint-item\">\n" +
            "                    <span class=\"endpoint-name\">Eureka Dashboard</span>\n" +
            "                    <a href=\"http://localhost:8761\" target=\"_blank\" class=\"endpoint-url\">http://localhost:8761</a>\n" +
            "                </div>\n" +
            "                <div class=\"endpoint-item\">\n" +
            "                    <span class=\"endpoint-name\">Gateway Actuator</span>\n" +
            "                    <a href=\"http://localhost:8090/actuator/gateway/routes\" target=\"_blank\" class=\"endpoint-url\">http://localhost:8090/actuator/gateway/routes</a>\n" +
            "                </div>\n" +
            "                <div class=\"endpoint-item\">\n" +
            "                    <span class=\"endpoint-name\">User Auth Swagger</span>\n" +
            "                    <a href=\"http://localhost:8081/swagger-ui/index.html\" target=\"_blank\" class=\"endpoint-url\">http://localhost:8081/swagger-ui/index.html</a>\n" +
            "                </div>\n" +
            "                <div class=\"endpoint-item\">\n" +
            "                    <span class=\"endpoint-name\">Book Catalogue Swagger</span>\n" +
            "                    <a href=\"http://localhost:8082/swagger-ui/index.html\" target=\"_blank\" class=\"endpoint-url\">http://localhost:8082/swagger-ui/index.html</a>\n" +
            "                </div>\n" +
            "                <div class=\"endpoint-item\">\n" +
            "                    <span class=\"endpoint-name\">Admin BackOffice Swagger</span>\n" +
            "                    <a href=\"http://localhost:8084/swagger-ui/index.html\" target=\"_blank\" class=\"endpoint-url\">http://localhost:8084/swagger-ui/index.html</a>\n" +
            "                </div>\n" +
            "            </div>\n" +
            "        </div>\n" +
            "        <div class=\"timestamp\" id=\"lastUpdated\">Last updated: -</div>\n" +
            "    </div>\n" +
            "    <script>\n" +
            "        setInterval(refreshData, 30000);\n" +
            "        refreshData();\n" +
            "        async function refreshData() {\n" +
            "            try {\n" +
            "                await Promise.all([loadRoutes(), loadEndpoints(), loadHealth()]);\n" +
            "                updateTimestamp();\n" +
            "            } catch (error) {\n" +
            "                console.error('Error refreshing data:', error);\n" +
            "            }\n" +
            "        }\n" +
            "        async function loadRoutes() {\n" +
            "            try {\n" +
            "                const response = await fetch('/gateway/api/routes');\n" +
            "                const routes = await response.json();\n" +
            "                const container = document.getElementById('routesContainer');\n" +
            "                if (routes.length === 0) {\n" +
            "                    container.innerHTML = '<div class=\"loading\">No routes configured</div>';\n" +
            "                    return;\n" +
            "                }\n" +
            "                container.innerHTML = routes.map(route => `\n" +
            "                    <div class=\"route-item\">\n" +
            "                        <div class=\"route-id\">${route.id}</div>\n" +
            "                        <div class=\"route-uri\">URI: ${route.uri}</div>\n" +
            "                        <div class=\"route-predicates\">Predicates: ${route.predicates}</div>\n" +
            "                    </div>\n" +
            "                `).join('');\n" +
            "            } catch (error) {\n" +
            "                document.getElementById('routesContainer').innerHTML = '<div class=\"loading\">Error loading routes</div>';\n" +
            "            }\n" +
            "        }\n" +
            "        async function loadEndpoints() {\n" +
            "            try {\n" +
            "                const response = await fetch('/gateway/api/endpoints');\n" +
            "                const endpoints = await response.json();\n" +
            "                const container = document.getElementById('endpointsContainer');\n" +
            "                let html = '';\n" +
            "                if (endpoints.infrastructure) {\n" +
            "                    html += '<h4 style=\"margin-bottom: 10px; color: #2c3e50;\">Infrastructure</h4>';\n" +
            "                    Object.entries(endpoints.infrastructure).forEach(([name, url]) => {\n" +
            "                        html += `<div class=\"endpoint-item\"><span class=\"endpoint-name\">${name}</span><a href=\"${url}\" target=\"_blank\" class=\"endpoint-url\">${url}</a></div>`;\n" +
            "                    });\n" +
            "                }\n" +
            "                if (endpoints.services) {\n" +
            "                    html += '<h4 style=\"margin: 15px 0 10px 0; color: #2c3e50;\">Services</h4>';\n" +
            "                    Object.entries(endpoints.services).forEach(([name, url]) => {\n" +
            "                        html += `<div class=\"endpoint-item\"><span class=\"endpoint-name\">${name}</span><a href=\"${url}\" target=\"_blank\" class=\"endpoint-url\">${url}</a></div>`;\n" +
            "                    });\n" +
            "                }\n" +
            "                container.innerHTML = html;\n" +
            "            } catch (error) {\n" +
            "                document.getElementById('endpointsContainer').innerHTML = '<div class=\"loading\">Error loading endpoints</div>';\n" +
            "            }\n" +
            "        }\n" +
            "        async function loadHealth() {\n" +
            "            try {\n" +
            "                const response = await fetch('/gateway/api/health');\n" +
            "                const health = await response.json();\n" +
            "                document.getElementById('gatewayStatus').textContent = health.status || 'UP';\n" +
            "                document.getElementById('totalRoutes').textContent = health.totalRoutes || '0';\n" +
            "            } catch (error) {\n" +
            "                document.getElementById('gatewayStatus').textContent = 'ERROR';\n" +
            "                document.getElementById('totalRoutes').textContent = '0';\n" +
            "            }\n" +
            "        }\n" +
            "        function updateTimestamp() {\n" +
            "            const now = new Date();\n" +
            "            document.getElementById('lastUpdated').textContent = `Last updated: ${now.toLocaleString()}`;\n" +
            "        }\n" +
            "    </script>\n" +
            "</body>\n" +
            "</html>";
    }
}
