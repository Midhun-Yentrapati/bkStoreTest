package com.bookstore2.Bookstore2.Controller;

import com.bookstore2.Bookstore2.Models.AdminActivityLog;
import com.bookstore2.Bookstore2.Service.AdminService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AdminControllerTest {

    @Mock
    private AdminService adminService;

    @InjectMocks
    private AdminController adminController;

    private AdminActivityLog sampleActivityLog;
    private List<AdminActivityLog> sampleActivityLogs;
    private Map<String, Object> sampleActivityData;

    @BeforeEach
    void setUp() {
        sampleActivityLog = new AdminActivityLog();
        sampleActivityLog.setId("log-123");
        sampleActivityLog.setAdminId("admin123");
        sampleActivityLog.setAdminUsername("testadmin");
        sampleActivityLog.setActionType("LOGIN");
        sampleActivityLog.setActionDescription("User logged in successfully");
        sampleActivityLog.setStatus("SUCCESS");
        sampleActivityLog.setServiceName("AuthService");
        sampleActivityLog.setEndpoint("/api/auth/login");
        sampleActivityLog.setHttpMethod("POST");
        sampleActivityLog.setRequestIp("192.168.1.1");
        sampleActivityLog.setUserAgent("Mozilla/5.0");
        sampleActivityLog.setResponseStatus(200);
        sampleActivityLog.setExecutionTimeMs(150L);
        sampleActivityLog.setResourceId("user123");
        sampleActivityLog.setResourceType("USER");
        sampleActivityLog.setSessionId("session123");

        AdminActivityLog log2 = new AdminActivityLog();
        log2.setId("log-456");
        log2.setAdminId("admin123");
        log2.setAdminUsername("testadmin");
        log2.setActionType("CREATE");
        log2.setActionDescription("Created new book");
        log2.setStatus("SUCCESS");
        log2.setServiceName("BookService");
        log2.setEndpoint("/api/books");
        log2.setHttpMethod("POST");
        log2.setRequestIp("192.168.1.1");
        log2.setUserAgent("Mozilla/5.0");
        log2.setResponseStatus(201);
        log2.setExecutionTimeMs(300L);
        log2.setResourceId("book456");
        log2.setResourceType("BOOK");
        log2.setSessionId("session123");

        sampleActivityLogs = Arrays.asList(sampleActivityLog, log2);

        sampleActivityData = new HashMap<>();
        sampleActivityData.put("adminId", "admin123");
        sampleActivityData.put("adminUsername", "testadmin");
        sampleActivityData.put("actionType", "LOGIN");
        sampleActivityData.put("actionDescription", "User logged in successfully");
        sampleActivityData.put("status", "SUCCESS");
        sampleActivityData.put("serviceName", "AuthService");
        sampleActivityData.put("endpoint", "/api/auth/login");
        sampleActivityData.put("httpMethod", "POST");
        sampleActivityData.put("requestIp", "192.168.1.1");
        sampleActivityData.put("userAgent", "Mozilla/5.0");
        sampleActivityData.put("requestPayload", "{\"username\":\"testadmin\"}");
        sampleActivityData.put("responseStatus", 200);
        sampleActivityData.put("executionTimeMs", 150L);
        sampleActivityData.put("errorMessage", null);
        sampleActivityData.put("resourceId", "user123");
        sampleActivityData.put("resourceType", "USER");
        sampleActivityData.put("sessionId", "session123");
    }

    @Test
    void testLogActivity_Success() {
        when(adminService.logActivity(anyString(), anyString(), anyString(), anyString(), anyString(),
                anyString(), anyString(), anyString(), anyString(), anyString(), anyString(),
                anyInt(), anyLong(), anyString(), anyString(), anyString(), anyString()))
                .thenReturn(sampleActivityLog);

        ResponseEntity<AdminActivityLog> response = adminController.logActivity(sampleActivityData);

        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("log-123", response.getBody().getId());
        assertEquals("admin123", response.getBody().getAdminId());
        assertEquals("LOGIN", response.getBody().getActionType());
        verify(adminService, times(1)).logActivity(anyString(), anyString(), anyString(), anyString(), anyString(),
                anyString(), anyString(), anyString(), anyString(), anyString(), anyString(),
                anyInt(), anyLong(), anyString(), anyString(), anyString(), anyString());
    }

    @Test
    void testLogActivity_Exception() {
        when(adminService.logActivity(anyString(), anyString(), anyString(), anyString(), anyString(),
                anyString(), anyString(), anyString(), anyString(), anyString(), anyString(),
                anyInt(), anyLong(), anyString(), anyString(), anyString(), anyString()))
                .thenThrow(new RuntimeException("Service error"));

        ResponseEntity<AdminActivityLog> response = adminController.logActivity(sampleActivityData);

        assertNotNull(response);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNull(response.getBody());
    }

    @Test
    void testGetActivityLogsByAdminId() {
        String adminId = "admin123";
        when(adminService.getActivityLogsByAdminId(adminId)).thenReturn(sampleActivityLogs);

        ResponseEntity<List<AdminActivityLog>> response = adminController.getActivityLogsByAdminId(adminId);

        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(2, response.getBody().size());
        assertEquals("admin123", response.getBody().get(0).getAdminId());
        verify(adminService, times(1)).getActivityLogsByAdminId(adminId);
    }

    @Test
    void testGetActivityLogsByAdminIdPage() {
        String adminId = "admin123";
        Pageable pageable = PageRequest.of(0, 10);
        Page<AdminActivityLog> page = new PageImpl<>(sampleActivityLogs, pageable, 2);
        when(adminService.getActivityLogsByAdminId(adminId, pageable)).thenReturn(page);

        ResponseEntity<Page<AdminActivityLog>> response = adminController.getActivityLogsByAdminIdPage(adminId, pageable);

        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(2, response.getBody().getContent().size());
        assertEquals(2, response.getBody().getTotalElements());
        verify(adminService, times(1)).getActivityLogsByAdminId(adminId, pageable);
    }

    @Test
    void testGetActivityLogsByActionType() {
        String actionType = "LOGIN";
        when(adminService.getActivityLogsByActionType(actionType)).thenReturn(Arrays.asList(sampleActivityLog));

        ResponseEntity<List<AdminActivityLog>> response = adminController.getActivityLogsByActionType(actionType);

        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().size());
        assertEquals("LOGIN", response.getBody().get(0).getActionType());
        verify(adminService, times(1)).getActivityLogsByActionType(actionType);
    }

    @Test
    void testGetActivityLogsByStatus() {
        String status = "SUCCESS";
        when(adminService.getActivityLogsByStatus(status)).thenReturn(sampleActivityLogs);

        ResponseEntity<List<AdminActivityLog>> response = adminController.getActivityLogsByStatus(status);

        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(2, response.getBody().size());
        assertTrue(response.getBody().stream().allMatch(log -> "SUCCESS".equals(log.getStatus())));
        verify(adminService, times(1)).getActivityLogsByStatus(status);
    }

    @Test
    void testGetActivityLogsByDateRange() {
        LocalDateTime startDate = LocalDateTime.now().minusDays(7);
        LocalDateTime endDate = LocalDateTime.now();
        when(adminService.getActivityLogsByDateRange(startDate, endDate)).thenReturn(sampleActivityLogs);

        ResponseEntity<List<AdminActivityLog>> response = adminController.getActivityLogsByDateRange(startDate, endDate);

        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(2, response.getBody().size());
        verify(adminService, times(1)).getActivityLogsByDateRange(startDate, endDate);
    }

    @Test
    void testGetRecentActivities() {
        when(adminService.getRecentActivities()).thenReturn(sampleActivityLogs);

        ResponseEntity<List<AdminActivityLog>> response = adminController.getRecentActivities();

        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(2, response.getBody().size());
        verify(adminService, times(1)).getRecentActivities();
    }

    @Test
    void testGetTopActiveAdmins() {
        List<Object[]> topAdmins = Arrays.asList(
            new Object[]{"admin123", "testadmin", 5L},
            new Object[]{"admin456", "admin2", 3L}
        );
        when(adminService.getTopActiveAdmins()).thenReturn(topAdmins);

        ResponseEntity<List<Object[]>> response = adminController.getTopActiveAdmins();

        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(2, response.getBody().size());
        assertEquals("admin123", response.getBody().get(0)[0]);
        verify(adminService, times(1)).getTopActiveAdmins();
    }

    @Test
    void testGetMostCommonActions() {
        List<Object[]> commonActions = Arrays.asList(
            new Object[]{"LOGIN", 10L},
            new Object[]{"CREATE", 5L}
        );
        when(adminService.getMostCommonActions()).thenReturn(commonActions);

        ResponseEntity<List<Object[]>> response = adminController.getMostCommonActions();

        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(2, response.getBody().size());
        assertEquals("LOGIN", response.getBody().get(0)[0]);
        verify(adminService, times(1)).getMostCommonActions();
    }

    @Test
    void testGetLogsWithErrors() {
        AdminActivityLog errorLog = new AdminActivityLog();
        errorLog.setId("error-log-123");
        errorLog.setAdminId("admin123");
        errorLog.setActionType("DELETE");
        errorLog.setStatus("ERROR");
        errorLog.setErrorMessage("Resource not found");
        
        List<AdminActivityLog> errorLogs = Arrays.asList(errorLog);
        when(adminService.getLogsWithErrors()).thenReturn(errorLogs);

        ResponseEntity<List<AdminActivityLog>> response = adminController.getLogsWithErrors();

        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().size());
        assertEquals("ERROR", response.getBody().get(0).getStatus());
        verify(adminService, times(1)).getLogsWithErrors();
    }

    @Test
    void testGetSlowOperations() {
        Long minDurationMs = 1000L;
        AdminActivityLog slowLog = new AdminActivityLog();
        slowLog.setId("slow-log-123");
        slowLog.setAdminId("admin123");
        slowLog.setActionType("REPORT_GENERATION");
        slowLog.setExecutionTimeMs(1500L);
        
        List<AdminActivityLog> slowLogs = Arrays.asList(slowLog);
        when(adminService.getSlowOperations(minDurationMs)).thenReturn(slowLogs);

        ResponseEntity<List<AdminActivityLog>> response = adminController.getSlowOperations(minDurationMs);

        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().size());
        assertTrue(response.getBody().get(0).getExecutionTimeMs() >= minDurationMs);
        verify(adminService, times(1)).getSlowOperations(minDurationMs);
    }

    @Test
    void testGetActivityStatistics() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalActivities", 100L);
        stats.put("successCount", 80L);
        stats.put("errorCount", 10L);
        stats.put("failedCount", 10L);
        stats.put("averageExecutionTime", 250.5);
        
        when(adminService.getActivityStatistics()).thenReturn(stats);

        ResponseEntity<Map<String, Object>> response = adminController.getActivityStatistics();

        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(5, response.getBody().size());
        assertEquals(100L, response.getBody().get("totalActivities"));
        assertEquals(80L, response.getBody().get("successCount"));
        verify(adminService, times(1)).getActivityStatistics();
    }
}
