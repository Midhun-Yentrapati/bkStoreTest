package com.bookstore2.Bookstore2.Service;

import com.bookstore2.Bookstore2.Models.AdminActivityLog;
import com.bookstore2.Bookstore2.Repository.AdminActivityLogRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class AdminService {

    @Autowired
    private AdminActivityLogRepository adminActivityLogRepository;

    public AdminActivityLog logActivity(String adminId, String adminUsername, String actionType,
                                       String actionDescription, String status, String serviceName,
                                       String endpoint, String httpMethod, String requestIp,
                                       String userAgent, String requestPayload, Integer responseStatus,
                                       Long executionTimeMs, String errorMessage, String resourceId,
                                       String resourceType, String sessionId) {
        AdminActivityLog log = new AdminActivityLog();
        log.setId(UUID.randomUUID().toString());
        log.setAdminId(adminId);
        log.setAdminUsername(adminUsername);
        log.setActionType(actionType);
        log.setActionDescription(actionDescription);
        log.setStatus(status);
        log.setServiceName(serviceName);
        log.setEndpoint(endpoint);
        log.setHttpMethod(httpMethod);
        log.setRequestIp(requestIp);
        log.setUserAgent(userAgent);
        log.setRequestPayload(requestPayload);
        log.setResponseStatus(responseStatus);
        log.setExecutionTimeMs(executionTimeMs);
        log.setErrorMessage(errorMessage);
        log.setResourceId(resourceId);
        log.setResourceType(resourceType);
        log.setSessionId(sessionId);

        return adminActivityLogRepository.save(log);
    }

    public List<AdminActivityLog> getAllActivityLogs() {
        return adminActivityLogRepository.findAllByOrderByCreatedAtDesc();
    }

    public List<AdminActivityLog> getActivityLogsByAdminId(String adminId) {
        return adminActivityLogRepository.findByAdminIdOrderByCreatedAtDesc(adminId);
    }

    public Page<AdminActivityLog> getActivityLogsByAdminId(String adminId, Pageable pageable) {
        return adminActivityLogRepository.findByAdminIdOrderByCreatedAtDesc(adminId, pageable);
    }

    public List<AdminActivityLog> getActivityLogsByActionType(String actionType) {
        return adminActivityLogRepository.findByActionTypeOrderByCreatedAtDesc(actionType);
    }

    public List<AdminActivityLog> getActivityLogsByStatus(String status) {
        return adminActivityLogRepository.findByStatusOrderByCreatedAtDesc(status);
    }

    public List<AdminActivityLog> getActivityLogsByDateRange(LocalDateTime startDate, LocalDateTime endDate) {
        return adminActivityLogRepository.findByCreatedAtBetweenOrderByCreatedAtDesc(startDate, endDate);
    }

    public List<AdminActivityLog> getRecentActivities() {
        LocalDateTime since = LocalDateTime.now().minusDays(7); // Last 7 days
        return adminActivityLogRepository.findRecentActivities(since);
    }

    public List<Object[]> getTopActiveAdmins() {
        LocalDateTime since = LocalDateTime.now().minusDays(30); // Last 30 days
        return adminActivityLogRepository.findTopActiveAdmins(since);
    }

    public List<Object[]> getMostCommonActions() {
        LocalDateTime since = LocalDateTime.now().minusDays(30); // Last 30 days
        return adminActivityLogRepository.findMostCommonActions(since);
    }

    public List<AdminActivityLog> getLogsWithErrors() {
        return adminActivityLogRepository.findLogsWithErrors();
    }

    public List<AdminActivityLog> getSlowOperations(Long minDurationMs) {
        return adminActivityLogRepository.findSlowOperations(minDurationMs);
    }

    public Map<String, Object> getActivityStatistics() {
        Map<String, Object> stats = new HashMap<>();
        
        LocalDateTime thirtyDaysAgo = LocalDateTime.now().minusDays(30);
        LocalDateTime now = LocalDateTime.now();
        
        long totalActivities = adminActivityLogRepository.countByCreatedAtBetween(thirtyDaysAgo, now);
        long successfulActivities = adminActivityLogRepository.countByStatus("SUCCESS");
        long errorActivities = adminActivityLogRepository.countByStatus("ERROR");
        Double averageExecutionTime = adminActivityLogRepository.findAverageExecutionTime();
        
        stats.put("totalActivities", totalActivities);
        stats.put("successfulActivities", successfulActivities);
        stats.put("errorActivities", errorActivities);
        stats.put("averageExecutionTime", averageExecutionTime);
        stats.put("successRate", totalActivities > 0 ? (double) successfulActivities / totalActivities * 100 : 0);
        
        return stats;
    }
} 