package com.bookstore2.Bookstore2.Controller;

import com.bookstore2.Bookstore2.Models.AdminActivityLog;
import com.bookstore2.Bookstore2.Service.AdminService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    @Autowired
    private AdminService adminService;

    @PostMapping("/activity/log")
    public ResponseEntity<AdminActivityLog> logActivity(@RequestBody Map<String, Object> activityData) {
        try {
            AdminActivityLog log = adminService.logActivity(
                (String) activityData.get("adminId"),
                (String) activityData.get("adminUsername"),
                (String) activityData.get("actionType"),
                (String) activityData.get("actionDescription"),
                (String) activityData.get("status"),
                (String) activityData.get("serviceName"),
                (String) activityData.get("endpoint"),
                (String) activityData.get("httpMethod"),
                (String) activityData.get("requestIp"),
                (String) activityData.get("userAgent"),
                (String) activityData.get("requestPayload"),
                activityData.get("responseStatus") != null ? Integer.valueOf(activityData.get("responseStatus").toString()) : null,
                activityData.get("executionTimeMs") != null ? Long.valueOf(activityData.get("executionTimeMs").toString()) : null,
                (String) activityData.get("errorMessage"),
                (String) activityData.get("resourceId"),
                (String) activityData.get("resourceType"),
                (String) activityData.get("sessionId")
            );
            return ResponseEntity.ok(log);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/activity/logs")
    public ResponseEntity<List<AdminActivityLog>> getAllActivityLogs() {
        List<AdminActivityLog> logs = adminService.getAllActivityLogs();
        return ResponseEntity.ok(logs);
    }

    @GetMapping("/activity/logs/admin/{adminId}")
    public ResponseEntity<List<AdminActivityLog>> getActivityLogsByAdminId(@PathVariable String adminId) {
        List<AdminActivityLog> logs = adminService.getActivityLogsByAdminId(adminId);
        return ResponseEntity.ok(logs);
    }

    @GetMapping("/activity/logs/admin/{adminId}/page")
    public ResponseEntity<Page<AdminActivityLog>> getActivityLogsByAdminIdPage(
            @PathVariable String adminId, Pageable pageable) {
        Page<AdminActivityLog> logs = adminService.getActivityLogsByAdminId(adminId, pageable);
        return ResponseEntity.ok(logs);
    }

    @GetMapping("/activity/logs/action/{actionType}")
    public ResponseEntity<List<AdminActivityLog>> getActivityLogsByActionType(@PathVariable String actionType) {
        List<AdminActivityLog> logs = adminService.getActivityLogsByActionType(actionType);
        return ResponseEntity.ok(logs);
    }

    @GetMapping("/activity/logs/status/{status}")
    public ResponseEntity<List<AdminActivityLog>> getActivityLogsByStatus(@PathVariable String status) {
        List<AdminActivityLog> logs = adminService.getActivityLogsByStatus(status);
        return ResponseEntity.ok(logs);
    }

    @GetMapping("/activity/logs/date-range")
    public ResponseEntity<List<AdminActivityLog>> getActivityLogsByDateRange(
            @RequestParam LocalDateTime startDate, @RequestParam LocalDateTime endDate) {
        List<AdminActivityLog> logs = adminService.getActivityLogsByDateRange(startDate, endDate);
        return ResponseEntity.ok(logs);
    }

    @GetMapping("/activity/logs/recent")
    public ResponseEntity<List<AdminActivityLog>> getRecentActivities() {
        List<AdminActivityLog> logs = adminService.getRecentActivities();
        return ResponseEntity.ok(logs);
    }

    @GetMapping("/activity/stats/top-admins")
    public ResponseEntity<List<Object[]>> getTopActiveAdmins() {
        List<Object[]> topAdmins = adminService.getTopActiveAdmins();
        return ResponseEntity.ok(topAdmins);
    }

    @GetMapping("/activity/stats/common-actions")
    public ResponseEntity<List<Object[]>> getMostCommonActions() {
        List<Object[]> commonActions = adminService.getMostCommonActions();
        return ResponseEntity.ok(commonActions);
    }

    @GetMapping("/activity/logs/errors")
    public ResponseEntity<List<AdminActivityLog>> getLogsWithErrors() {
        List<AdminActivityLog> errorLogs = adminService.getLogsWithErrors();
        return ResponseEntity.ok(errorLogs);
    }

    @GetMapping("/activity/logs/slow-operations")
    public ResponseEntity<List<AdminActivityLog>> getSlowOperations(@RequestParam Long minDurationMs) {
        List<AdminActivityLog> slowOps = adminService.getSlowOperations(minDurationMs);
        return ResponseEntity.ok(slowOps);
    }

    @GetMapping("/activity/stats")
    public ResponseEntity<Map<String, Object>> getActivityStatistics() {
        Map<String, Object> stats = adminService.getActivityStatistics();
        return ResponseEntity.ok(stats);
    }
} 