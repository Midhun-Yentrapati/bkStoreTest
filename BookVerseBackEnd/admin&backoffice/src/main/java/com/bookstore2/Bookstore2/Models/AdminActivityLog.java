package com.bookstore2.Bookstore2.Models;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "admin_activity_logs")
public class AdminActivityLog {

    @Id
    @Column(name = "id", length = 36)
    private String id;

    @Column(name = "admin_id", nullable = false)
    private String adminId;

    @Column(name = "admin_username", nullable = false, length = 100)
    private String adminUsername;

    @Column(name = "action_type", nullable = false, length = 50)
    private String actionType;

    @Column(name = "action_description", nullable = false, length = 1000)
    private String actionDescription;

    @Column(name = "status", nullable = false, length = 20)
    private String status = "SUCCESS";

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "service_name", length = 100)
    private String serviceName;

    @Column(name = "endpoint", length = 500)
    private String endpoint;

    @Column(name = "http_method", length = 10)
    private String httpMethod;

    @Column(name = "request_ip", length = 45)
    private String requestIp;

    @Column(name = "user_agent", length = 500)
    private String userAgent;

    @Column(name = "request_payload", columnDefinition = "TEXT")
    private String requestPayload;

    @Column(name = "response_status")
    private Integer responseStatus;

    @Column(name = "execution_time_ms")
    private Long executionTimeMs;

    @Column(name = "error_message", columnDefinition = "TEXT")
    private String errorMessage;

    @Column(name = "resource_id", length = 100)
    private String resourceId;

    @Column(name = "resource_type", length = 50)
    private String resourceType;

    @Column(name = "session_id", length = 255)
    private String sessionId;

    public AdminActivityLog() {
        this.createdAt = LocalDateTime.now();
    }

    public AdminActivityLog(String id, String adminId, String adminUsername, String actionType,
                           String actionDescription, String status, String serviceName, String endpoint,
                           String httpMethod, String requestIp, String userAgent, String requestPayload,
                           Integer responseStatus, Long executionTimeMs, String errorMessage,
                           String resourceId, String resourceType, String sessionId) {
        this.id = id;
        this.adminId = adminId;
        this.adminUsername = adminUsername;
        this.actionType = actionType;
        this.actionDescription = actionDescription;
        this.status = status;
        this.serviceName = serviceName;
        this.endpoint = endpoint;
        this.httpMethod = httpMethod;
        this.requestIp = requestIp;
        this.userAgent = userAgent;
        this.requestPayload = requestPayload;
        this.responseStatus = responseStatus;
        this.executionTimeMs = executionTimeMs;
        this.errorMessage = errorMessage;
        this.resourceId = resourceId;
        this.resourceType = resourceType;
        this.sessionId = sessionId;
        this.createdAt = LocalDateTime.now();
    }

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getAdminId() { return adminId; }
    public void setAdminId(String adminId) { this.adminId = adminId; }
    public String getAdminUsername() { return adminUsername; }
    public void setAdminUsername(String adminUsername) { this.adminUsername = adminUsername; }
    public String getActionType() { return actionType; }
    public void setActionType(String actionType) { this.actionType = actionType; }
    public String getActionDescription() { return actionDescription; }
    public void setActionDescription(String actionDescription) { this.actionDescription = actionDescription; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public String getServiceName() { return serviceName; }
    public void setServiceName(String serviceName) { this.serviceName = serviceName; }
    public String getEndpoint() { return endpoint; }
    public void setEndpoint(String endpoint) { this.endpoint = endpoint; }
    public String getHttpMethod() { return httpMethod; }
    public void setHttpMethod(String httpMethod) { this.httpMethod = httpMethod; }
    public String getRequestIp() { return requestIp; }
    public void setRequestIp(String requestIp) { this.requestIp = requestIp; }
    public String getUserAgent() { return userAgent; }
    public void setUserAgent(String userAgent) { this.userAgent = userAgent; }
    public String getRequestPayload() { return requestPayload; }
    public void setRequestPayload(String requestPayload) { this.requestPayload = requestPayload; }
    public Integer getResponseStatus() { return responseStatus; }
    public void setResponseStatus(Integer responseStatus) { this.responseStatus = responseStatus; }
    public Long getExecutionTimeMs() { return executionTimeMs; }
    public void setExecutionTimeMs(Long executionTimeMs) { this.executionTimeMs = executionTimeMs; }
    public String getErrorMessage() { return errorMessage; }
    public void setErrorMessage(String errorMessage) { this.errorMessage = errorMessage; }
    public String getResourceId() { return resourceId; }
    public void setResourceId(String resourceId) { this.resourceId = resourceId; }
    public String getResourceType() { return resourceType; }
    public void setResourceType(String resourceType) { this.resourceType = resourceType; }
    public String getSessionId() { return sessionId; }
    public void setSessionId(String sessionId) { this.sessionId = sessionId; }
}
