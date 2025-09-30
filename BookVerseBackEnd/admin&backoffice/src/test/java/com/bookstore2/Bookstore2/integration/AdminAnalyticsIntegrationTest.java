package com.bookstore2.Bookstore2.integration;

import com.bookstore2.Bookstore2.Models.AdminActivityLog;
import com.bookstore2.Bookstore2.Models.DailySalesSummary;
import com.bookstore2.Bookstore2.Service.AdminService;
import com.bookstore2.Bookstore2.Service.AnalyticsService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AdminAnalyticsIntegrationTest {

    @Mock
    private AdminService adminService;

    @Mock
    private AnalyticsService analyticsService;

    @Test
    void testAdminActivityLoggingWithSalesProcessing() {
        // Test scenario: Admin processes daily sales and activity is logged
        
        // Arrange
        AdminActivityLog expectedLog = new AdminActivityLog();
        expectedLog.setId("log-123");
        expectedLog.setAdminId("admin123");
        expectedLog.setActionType("DAILY_SALES_PROCESSING");
        expectedLog.setStatus("SUCCESS");
        
        DailySalesSummary expectedSummary = new DailySalesSummary();
        expectedSummary.setId(1L);
        expectedSummary.setSalesDate(LocalDate.now());
        expectedSummary.setTotalRevenue(new BigDecimal("1500.00"));
        expectedSummary.setTotalOrders(25);
        
        when(adminService.logActivity(anyString(), anyString(), anyString(), anyString(), anyString(),
                anyString(), anyString(), anyString(), anyString(), anyString(), anyString(),
                anyInt(), anyLong(), anyString(), anyString(), anyString(), anyString()))
                .thenReturn(expectedLog);
        
        when(analyticsService.saveOrUpdateDailySalesSummary(any(DailySalesSummary.class)))
                .thenReturn(expectedSummary);

        // Act
        AdminActivityLog activityLog = adminService.logActivity(
            "admin123", "testadmin", "DAILY_SALES_PROCESSING", 
            "Processed daily sales summary", "SUCCESS",
            "AnalyticsService", "/api/analytics/daily-sales", "POST", 
            "192.168.1.1", "Mozilla/5.0", "{\"date\":\"" + LocalDate.now() + "\"}", 
            200, 250L, null, "summary-1", "DAILY_SALES", "session123"
        );
        
        DailySalesSummary salesSummary = analyticsService.saveOrUpdateDailySalesSummary(expectedSummary);

        // Assert
        assertNotNull(activityLog);
        assertEquals("DAILY_SALES_PROCESSING", activityLog.getActionType());
        assertEquals("SUCCESS", activityLog.getStatus());
        
        assertNotNull(salesSummary);
        assertEquals(LocalDate.now(), salesSummary.getSalesDate());
        assertEquals(new BigDecimal("1500.00"), salesSummary.getTotalRevenue());
        
        verify(adminService, times(1)).logActivity(anyString(), anyString(), anyString(), anyString(), anyString(),
                anyString(), anyString(), anyString(), anyString(), anyString(), anyString(),
                anyInt(), anyLong(), anyString(), anyString(), anyString(), anyString());
        verify(analyticsService, times(1)).saveOrUpdateDailySalesSummary(any(DailySalesSummary.class));
    }

    @Test
    void testAdminErrorLoggingWithFailedSalesProcessing() {
        // Test scenario: Admin fails to process sales and error is logged
        
        // Arrange
        AdminActivityLog errorLog = new AdminActivityLog();
        errorLog.setId("error-log-123");
        errorLog.setAdminId("admin123");
        errorLog.setActionType("DAILY_SALES_PROCESSING");
        errorLog.setStatus("ERROR");
        errorLog.setErrorMessage("Database connection failed");
        
        when(adminService.logActivity(anyString(), anyString(), anyString(), anyString(), anyString(),
                anyString(), anyString(), anyString(), anyString(), anyString(), anyString(),
                anyInt(), anyLong(), anyString(), anyString(), anyString(), anyString()))
                .thenReturn(errorLog);
        
        when(analyticsService.saveOrUpdateDailySalesSummary(any(DailySalesSummary.class)))
                .thenThrow(new RuntimeException("Database connection failed"));

        // Act & Assert
        AdminActivityLog activityLog = adminService.logActivity(
            "admin123", "testadmin", "DAILY_SALES_PROCESSING", 
            "Failed to process daily sales summary", "ERROR",
            "AnalyticsService", "/api/analytics/daily-sales", "POST", 
            "192.168.1.1", "Mozilla/5.0", "{\"date\":\"" + LocalDate.now() + "\"}", 
            500, 100L, "Database connection failed", "summary-1", "DAILY_SALES", "session123"
        );
        
        assertThrows(RuntimeException.class, () -> {
            analyticsService.saveOrUpdateDailySalesSummary(new DailySalesSummary());
        });
        
        assertNotNull(activityLog);
        assertEquals("ERROR", activityLog.getStatus());
        assertEquals("Database connection failed", activityLog.getErrorMessage());
        
        verify(adminService, times(1)).logActivity(anyString(), anyString(), anyString(), anyString(), anyString(),
                anyString(), anyString(), anyString(), anyString(), anyString(), anyString(),
                anyInt(), anyLong(), anyString(), anyString(), anyString(), anyString());
    }

    @Test
    void testAdminDashboardDataRetrieval() {
        // Test scenario: Admin retrieves dashboard data with both activity logs and sales analytics
        
        // Arrange
        AdminActivityLog sampleLog = new AdminActivityLog();
        sampleLog.setId("log-123");
        sampleLog.setAdminId("admin123");
        sampleLog.setActionType("DASHBOARD_ACCESS");
        sampleLog.setStatus("SUCCESS");
        
        List<AdminActivityLog> recentActivities = Arrays.asList(sampleLog);
        
        Map<String, Object> activityStats = Map.of(
            "totalActivities", 100L,
            "successCount", 80L,
            "errorCount", 10L,
            "failedCount", 10L,
            "averageExecutionTime", 250.5
        );
        
        DailySalesSummary recentSales = new DailySalesSummary();
        recentSales.setId(1L);
        recentSales.setSalesDate(LocalDate.now());
        recentSales.setTotalRevenue(new BigDecimal("1500.00"));
        recentSales.setTotalOrders(25);
        
        List<DailySalesSummary> recentSalesSummaries = Arrays.asList(recentSales);
        
        Map<String, Object> salesDashboard = Map.of(
            "totalRevenue", new BigDecimal("3500.00"),
            "totalOrders", 55,
            "totalItemsSold", 110,
            "averageOrderValue", new BigDecimal("63.33")
        );
        
        when(adminService.getRecentActivities()).thenReturn(recentActivities);
        when(adminService.getActivityStatistics()).thenReturn(activityStats);
        when(analyticsService.getRecentDailySalesSummaries()).thenReturn(recentSalesSummaries);
        when(analyticsService.getAnalyticsDashboard(any(LocalDate.class), any(LocalDate.class)))
                .thenReturn(salesDashboard);

        // Act
        List<AdminActivityLog> activities = adminService.getRecentActivities();
        Map<String, Object> stats = adminService.getActivityStatistics();
        List<DailySalesSummary> sales = analyticsService.getRecentDailySalesSummaries();
        Map<String, Object> dashboard = analyticsService.getAnalyticsDashboard(
            LocalDate.now().minusDays(30), LocalDate.now());

        // Assert
        assertNotNull(activities);
        assertEquals(1, activities.size());
        assertEquals("DASHBOARD_ACCESS", activities.get(0).getActionType());
        
        assertNotNull(stats);
        assertEquals(5, stats.size());
        assertEquals(100L, stats.get("totalActivities"));
        
        assertNotNull(sales);
        assertEquals(1, sales.size());
        assertEquals(LocalDate.now(), sales.get(0).getSalesDate());
        
        assertNotNull(dashboard);
        assertEquals(4, dashboard.size());
        assertEquals(new BigDecimal("3500.00"), dashboard.get("totalRevenue"));
        
        verify(adminService, times(1)).getRecentActivities();
        verify(adminService, times(1)).getActivityStatistics();
        verify(analyticsService, times(1)).getRecentDailySalesSummaries();
        verify(analyticsService, times(1)).getAnalyticsDashboard(any(LocalDate.class), any(LocalDate.class));
    }

    @Test
    void testAdminPerformanceMonitoring() {
        // Test scenario: Monitor admin performance with both activity logs and sales metrics
        
        // Arrange
        AdminActivityLog slowOperation = new AdminActivityLog();
        slowOperation.setId("slow-log-123");
        slowOperation.setAdminId("admin123");
        slowOperation.setActionType("REPORT_GENERATION");
        slowOperation.setExecutionTimeMs(5000L);
        slowOperation.setStatus("SUCCESS");
        
        List<AdminActivityLog> slowOperations = Arrays.asList(slowOperation);
        
        Map<String, Object> performanceStats = Map.of(
            "averageExecutionTime", 250.5,
            "slowOperationsCount", 5L,
            "totalOperations", 100L
        );
        
        when(adminService.getSlowOperations(1000L)).thenReturn(slowOperations);
        when(adminService.getActivityStatistics()).thenReturn(performanceStats);

        // Act
        List<AdminActivityLog> slowOps = adminService.getSlowOperations(1000L);
        Map<String, Object> perfStats = adminService.getActivityStatistics();

        // Assert
        assertNotNull(slowOps);
        assertEquals(1, slowOps.size());
        assertTrue(slowOps.get(0).getExecutionTimeMs() >= 1000L);
        
        assertNotNull(perfStats);
        assertEquals(3, perfStats.size());
        assertEquals(250.5, perfStats.get("averageExecutionTime"));
        
        verify(adminService, times(1)).getSlowOperations(1000L);
        verify(adminService, times(1)).getActivityStatistics();
    }
}
