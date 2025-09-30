package com.bookstore2.Bookstore2.Service;

import com.bookstore2.Bookstore2.Models.AdminActivityLog;
import com.bookstore2.Bookstore2.Repository.AdminActivityLogRepository;
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

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AdminServiceTest {

    @Mock
    private AdminActivityLogRepository adminActivityLogRepository;

    @InjectMocks
    private AdminService adminService;

    private AdminActivityLog sampleActivityLog;
    private List<AdminActivityLog> sampleActivityLogs;

    @BeforeEach
    void setUp() {
        // Create sample activity log
        sampleActivityLog = new AdminActivityLog();
        sampleActivityLog.setId(UUID.randomUUID().toString());
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

        // Create sample list
        AdminActivityLog log2 = new AdminActivityLog();
        log2.setId(UUID.randomUUID().toString());
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
    }

    @Test
    void testLogActivity_Success() {
        // Arrange
        when(adminActivityLogRepository.save(any(AdminActivityLog.class))).thenReturn(sampleActivityLog);

        // Act
        AdminActivityLog result = adminService.logActivity(
            "admin123", "testadmin", "LOGIN", "User logged in successfully", "SUCCESS",
            "AuthService", "/api/auth/login", "POST", "192.168.1.1", "Mozilla/5.0",
            "{\"username\":\"testadmin\"}", 200, 150L, null, "user123", "USER", "session123"
        );

        // Assert
        assertNotNull(result);
        assertEquals("admin123", result.getAdminId());
        assertEquals("testadmin", result.getAdminUsername());
        assertEquals("LOGIN", result.getActionType());
        assertEquals("SUCCESS", result.getStatus());
        verify(adminActivityLogRepository, times(1)).save(any(AdminActivityLog.class));
    }

    @Test
    void testLogActivity_WithError() {
        // Arrange
        AdminActivityLog errorLog = new AdminActivityLog();
        errorLog.setId(UUID.randomUUID().toString());
        errorLog.setAdminId("admin123");
        errorLog.setAdminUsername("testadmin");
        errorLog.setActionType("DELETE");
        errorLog.setActionDescription("Failed to delete book");
        errorLog.setStatus("ERROR");
        errorLog.setErrorMessage("Book not found");
        errorLog.setResponseStatus(404);
        errorLog.setExecutionTimeMs(50L);

        when(adminActivityLogRepository.save(any(AdminActivityLog.class))).thenReturn(errorLog);

        // Act
        AdminActivityLog result = adminService.logActivity(
            "admin123", "testadmin", "DELETE", "Failed to delete book", "ERROR",
            "BookService", "/api/books/999", "DELETE", "192.168.1.1", "Mozilla/5.0",
            "{\"bookId\":999}", 404, 50L, "Book not found", "book999", "BOOK", "session123"
        );

        // Assert
        assertNotNull(result);
        assertEquals("ERROR", result.getStatus());
        assertEquals("Book not found", result.getErrorMessage());
        assertEquals(404, result.getResponseStatus());
        verify(adminActivityLogRepository, times(1)).save(any(AdminActivityLog.class));
    }

    @Test
    void testGetActivityLogsByAdminId() {
        // Arrange
        String adminId = "admin123";
        when(adminActivityLogRepository.findByAdminIdOrderByCreatedAtDesc(adminId))
            .thenReturn(sampleActivityLogs);

        // Act
        List<AdminActivityLog> result = adminService.getActivityLogsByAdminId(adminId);

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("admin123", result.get(0).getAdminId());
        verify(adminActivityLogRepository, times(1)).findByAdminIdOrderByCreatedAtDesc(adminId);
    }

    @Test
    void testGetActivityLogsByAdminId_WithPagination() {
        // Arrange
        String adminId = "admin123";
        Pageable pageable = PageRequest.of(0, 10);
        Page<AdminActivityLog> page = new PageImpl<>(sampleActivityLogs, pageable, 2);
        
        when(adminActivityLogRepository.findByAdminIdOrderByCreatedAtDesc(adminId, pageable))
            .thenReturn(page);

        // Act
        Page<AdminActivityLog> result = adminService.getActivityLogsByAdminId(adminId, pageable);

        // Assert
        assertNotNull(result);
        assertEquals(2, result.getContent().size());
        assertEquals(2, result.getTotalElements());
        verify(adminActivityLogRepository, times(1)).findByAdminIdOrderByCreatedAtDesc(adminId, pageable);
    }

    @Test
    void testGetActivityLogsByActionType() {
        // Arrange
        String actionType = "LOGIN";
        when(adminActivityLogRepository.findByActionTypeOrderByCreatedAtDesc(actionType))
            .thenReturn(Arrays.asList(sampleActivityLog));

        // Act
        List<AdminActivityLog> result = adminService.getActivityLogsByActionType(actionType);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("LOGIN", result.get(0).getActionType());
        verify(adminActivityLogRepository, times(1)).findByActionTypeOrderByCreatedAtDesc(actionType);
    }

    @Test
    void testGetActivityLogsByStatus() {
        // Arrange
        String status = "SUCCESS";
        when(adminActivityLogRepository.findByStatusOrderByCreatedAtDesc(status))
            .thenReturn(sampleActivityLogs);

        // Act
        List<AdminActivityLog> result = adminService.getActivityLogsByStatus(status);

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        assertTrue(result.stream().allMatch(log -> "SUCCESS".equals(log.getStatus())));
        verify(adminActivityLogRepository, times(1)).findByStatusOrderByCreatedAtDesc(status);
    }

    @Test
    void testGetActivityLogsByDateRange() {
        // Arrange
        LocalDateTime startDate = LocalDateTime.now().minusDays(7);
        LocalDateTime endDate = LocalDateTime.now();
        when(adminActivityLogRepository.findByCreatedAtBetweenOrderByCreatedAtDesc(startDate, endDate))
            .thenReturn(sampleActivityLogs);

        // Act
        List<AdminActivityLog> result = adminService.getActivityLogsByDateRange(startDate, endDate);

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        verify(adminActivityLogRepository, times(1)).findByCreatedAtBetweenOrderByCreatedAtDesc(startDate, endDate);
    }

    @Test
    void testGetRecentActivities() {
        // Arrange
        LocalDateTime since = LocalDateTime.now().minusHours(24);
        when(adminActivityLogRepository.findRecentActivities(any(LocalDateTime.class)))
            .thenReturn(sampleActivityLogs);

        // Act
        List<AdminActivityLog> result = adminService.getRecentActivities();

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        verify(adminActivityLogRepository, times(1)).findRecentActivities(any(LocalDateTime.class));
    }

    @Test
    void testGetTopActiveAdmins() {
        // Arrange
        List<Object[]> topAdmins = Arrays.asList(
            new Object[]{"admin123", "testadmin", 5L},
            new Object[]{"admin456", "admin2", 3L}
        );
        when(adminActivityLogRepository.findTopActiveAdmins(any(LocalDateTime.class)))
            .thenReturn(topAdmins);

        // Act
        List<Object[]> result = adminService.getTopActiveAdmins();

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("admin123", result.get(0)[0]);
        assertEquals("testadmin", result.get(0)[1]);
        assertEquals(5L, result.get(0)[2]);
        verify(adminActivityLogRepository, times(1)).findTopActiveAdmins(any(LocalDateTime.class));
    }

    @Test
    void testGetMostCommonActions() {
        // Arrange
        List<Object[]> commonActions = Arrays.asList(
            new Object[]{"LOGIN", 10L},
            new Object[]{"CREATE", 5L},
            new Object[]{"UPDATE", 3L}
        );
        when(adminActivityLogRepository.findMostCommonActions(any(LocalDateTime.class)))
            .thenReturn(commonActions);

        // Act
        List<Object[]> result = adminService.getMostCommonActions();

        // Assert
        assertNotNull(result);
        assertEquals(3, result.size());
        assertEquals("LOGIN", result.get(0)[0]);
        assertEquals(10L, result.get(0)[1]);
        verify(adminActivityLogRepository, times(1)).findMostCommonActions(any(LocalDateTime.class));
    }

    @Test
    void testGetLogsWithErrors() {
        // Arrange
        AdminActivityLog errorLog = new AdminActivityLog();
        errorLog.setId(UUID.randomUUID().toString());
        errorLog.setAdminId("admin123");
        errorLog.setActionType("DELETE");
        errorLog.setStatus("ERROR");
        errorLog.setErrorMessage("Resource not found");
        
        List<AdminActivityLog> errorLogs = Arrays.asList(errorLog);
        when(adminActivityLogRepository.findLogsWithErrors()).thenReturn(errorLogs);

        // Act
        List<AdminActivityLog> result = adminService.getLogsWithErrors();

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("ERROR", result.get(0).getStatus());
        assertNotNull(result.get(0).getErrorMessage());
        verify(adminActivityLogRepository, times(1)).findLogsWithErrors();
    }

    @Test
    void testGetSlowOperations() {
        // Arrange
        Long minDurationMs = 1000L;
        AdminActivityLog slowLog = new AdminActivityLog();
        slowLog.setId(UUID.randomUUID().toString());
        slowLog.setAdminId("admin123");
        slowLog.setActionType("REPORT_GENERATION");
        slowLog.setExecutionTimeMs(1500L);
        
        List<AdminActivityLog> slowLogs = Arrays.asList(slowLog);
        when(adminActivityLogRepository.findSlowOperations(minDurationMs)).thenReturn(slowLogs);

        // Act
        List<AdminActivityLog> result = adminService.getSlowOperations(minDurationMs);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertTrue(result.get(0).getExecutionTimeMs() >= minDurationMs);
        verify(adminActivityLogRepository, times(1)).findSlowOperations(minDurationMs);
    }

    @Test
    void testGetActivityStatistics() {
        // Arrange
        when(adminActivityLogRepository.count()).thenReturn(100L);
        when(adminActivityLogRepository.countByStatus("SUCCESS")).thenReturn(80L);
        when(adminActivityLogRepository.countByStatus("ERROR")).thenReturn(10L);
        when(adminActivityLogRepository.countByStatus("FAILED")).thenReturn(10L);
        when(adminActivityLogRepository.findAverageExecutionTime()).thenReturn(250.5);

        // Act
        Map<String, Object> result = adminService.getActivityStatistics();

        // Assert
        assertNotNull(result);
        assertEquals(100L, result.get("totalActivities"));
        assertEquals(80L, result.get("successCount"));
        assertEquals(10L, result.get("errorCount"));
        assertEquals(10L, result.get("failedCount"));
        assertEquals(250.5, result.get("averageExecutionTime"));
        verify(adminActivityLogRepository, times(1)).count();
        verify(adminActivityLogRepository, times(1)).countByStatus("SUCCESS");
        verify(adminActivityLogRepository, times(1)).countByStatus("ERROR");
        verify(adminActivityLogRepository, times(1)).countByStatus("FAILED");
        verify(adminActivityLogRepository, times(1)).findAverageExecutionTime();
    }

    @Test
    void testGetActivityStatistics_WithNullAverageTime() {
        // Arrange
        when(adminActivityLogRepository.count()).thenReturn(100L);
        when(adminActivityLogRepository.countByStatus("SUCCESS")).thenReturn(80L);
        when(adminActivityLogRepository.countByStatus("ERROR")).thenReturn(10L);
        when(adminActivityLogRepository.countByStatus("FAILED")).thenReturn(10L);
        when(adminActivityLogRepository.findAverageExecutionTime()).thenReturn(null);

        // Act
        Map<String, Object> result = adminService.getActivityStatistics();

        // Assert
        assertNotNull(result);
        assertEquals(100L, result.get("totalActivities"));
        assertEquals(80L, result.get("successCount"));
        assertEquals(10L, result.get("errorCount"));
        assertEquals(10L, result.get("failedCount"));
        assertNull(result.get("averageExecutionTime"));
        verify(adminActivityLogRepository, times(1)).count();
        verify(adminActivityLogRepository, times(1)).countByStatus("SUCCESS");
        verify(adminActivityLogRepository, times(1)).countByStatus("ERROR");
        verify(adminActivityLogRepository, times(1)).countByStatus("FAILED");
        verify(adminActivityLogRepository, times(1)).findAverageExecutionTime();
    }
}
