package com.bookstore2.Bookstore2.Controller;

import com.bookstore2.Bookstore2.Models.DailySalesSummary;
import com.bookstore2.Bookstore2.Service.AnalyticsService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AnalyticsControllerTest {

    @Mock
    private AnalyticsService analyticsService;

    @InjectMocks
    private AnalyticsController analyticsController;

    private DailySalesSummary sampleSalesSummary;
    private List<DailySalesSummary> sampleSalesSummaries;

    @BeforeEach
    void setUp() {
        sampleSalesSummary = new DailySalesSummary();
        sampleSalesSummary.setId(1L);
        sampleSalesSummary.setSalesDate(LocalDate.of(2024, 1, 15));
        sampleSalesSummary.setTotalRevenue(new BigDecimal("1500.00"));
        sampleSalesSummary.setTotalOrders(25);
        sampleSalesSummary.setTotalItemsSold(50);
        sampleSalesSummary.setAverageOrderValue(new BigDecimal("60.00"));
        sampleSalesSummary.setTopSellingItem("The Great Gatsby");
        sampleSalesSummary.setTopSellingItemQuantity(10);
        sampleSalesSummary.setLeastSellingItem("Moby Dick");
        sampleSalesSummary.setLeastSellingItemQuantity(2);
        sampleSalesSummary.setCreatedAt(LocalDateTime.now());
        sampleSalesSummary.setUpdatedAt(LocalDateTime.now());

        DailySalesSummary summary2 = new DailySalesSummary();
        summary2.setId(2L);
        summary2.setSalesDate(LocalDate.of(2024, 1, 14));
        summary2.setTotalRevenue(new BigDecimal("2000.00"));
        summary2.setTotalOrders(30);
        summary2.setTotalItemsSold(60);
        summary2.setAverageOrderValue(new BigDecimal("66.67"));
        summary2.setTopSellingItem("To Kill a Mockingbird");
        summary2.setTopSellingItemQuantity(15);
        summary2.setLeastSellingItem("1984");
        summary2.setLeastSellingItemQuantity(1);
        summary2.setCreatedAt(LocalDateTime.now());
        summary2.setUpdatedAt(LocalDateTime.now());

        sampleSalesSummaries = Arrays.asList(sampleSalesSummary, summary2);
    }

    @Test
    void testProcessDailySalesSummary_Success() {
        when(analyticsService.saveOrUpdateDailySalesSummary(any(DailySalesSummary.class)))
            .thenReturn(sampleSalesSummary);

        ResponseEntity<DailySalesSummary> response = analyticsController.processDailySalesSummary(sampleSalesSummary);

        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(LocalDate.of(2024, 1, 15), response.getBody().getSalesDate());
        assertEquals(new BigDecimal("1500.00"), response.getBody().getTotalRevenue());
        verify(analyticsService, times(1)).saveOrUpdateDailySalesSummary(sampleSalesSummary);
    }

    @Test
    void testGetRecentDailySalesSummaries() {
        when(analyticsService.getRecentDailySalesSummaries()).thenReturn(sampleSalesSummaries);

        ResponseEntity<List<DailySalesSummary>> response = analyticsController.getRecentDailySalesSummaries();

        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(2, response.getBody().size());
        assertEquals(LocalDate.of(2024, 1, 15), response.getBody().get(0).getSalesDate());
        verify(analyticsService, times(1)).getRecentDailySalesSummaries();
    }

    @Test
    void testGetDailySalesSummaryByDate_Found() {
        LocalDate salesDate = LocalDate.of(2024, 1, 15);
        when(analyticsService.getDailySalesSummaryByDate(salesDate)).thenReturn(sampleSalesSummary);

        ResponseEntity<DailySalesSummary> response = analyticsController.getDailySalesSummaryByDate(salesDate);

        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(salesDate, response.getBody().getSalesDate());
        verify(analyticsService, times(1)).getDailySalesSummaryByDate(salesDate);
    }

    @Test
    void testGetDailySalesSummaryByDate_NotFound() {
        LocalDate salesDate = LocalDate.of(2024, 1, 20);
        when(analyticsService.getDailySalesSummaryByDate(salesDate)).thenReturn(null);

        ResponseEntity<DailySalesSummary> response = analyticsController.getDailySalesSummaryByDate(salesDate);

        assertNotNull(response);
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNull(response.getBody());
        verify(analyticsService, times(1)).getDailySalesSummaryByDate(salesDate);
    }

    @Test
    void testGetDailySalesSummariesByDateRange() {
        LocalDate startDate = LocalDate.of(2024, 1, 1);
        LocalDate endDate = LocalDate.of(2024, 1, 31);
        when(analyticsService.getDailySalesSummariesByDateRange(startDate, endDate))
            .thenReturn(sampleSalesSummaries);

        ResponseEntity<List<DailySalesSummary>> response = analyticsController.getDailySalesSummariesByDateRange(startDate, endDate);

        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(2, response.getBody().size());
        verify(analyticsService, times(1)).getDailySalesSummariesByDateRange(startDate, endDate);
    }

    @Test
    void testGetTotalRevenueInRange() {
        LocalDate startDate = LocalDate.of(2024, 1, 1);
        LocalDate endDate = LocalDate.of(2024, 1, 31);
        BigDecimal expectedRevenue = new BigDecimal("3500.00");
        when(analyticsService.calculateTotalRevenueInRange(startDate, endDate))
            .thenReturn(expectedRevenue);

        ResponseEntity<BigDecimal> response = analyticsController.getTotalRevenueInRange(startDate, endDate);

        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(expectedRevenue, response.getBody());
        verify(analyticsService, times(1)).calculateTotalRevenueInRange(startDate, endDate);
    }

    @Test
    void testGetTotalOrdersInRange() {
        LocalDate startDate = LocalDate.of(2024, 1, 1);
        LocalDate endDate = LocalDate.of(2024, 1, 31);
        Integer expectedOrders = 55;
        when(analyticsService.calculateTotalOrdersInRange(startDate, endDate))
            .thenReturn(expectedOrders);

        ResponseEntity<Integer> response = analyticsController.getTotalOrdersInRange(startDate, endDate);

        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(expectedOrders, response.getBody());
        verify(analyticsService, times(1)).calculateTotalOrdersInRange(startDate, endDate);
    }

    @Test
    void testGetTotalItemsSoldInRange() {
        LocalDate startDate = LocalDate.of(2024, 1, 1);
        LocalDate endDate = LocalDate.of(2024, 1, 31);
        Integer expectedItems = 110;
        when(analyticsService.calculateTotalItemsSoldInRange(startDate, endDate))
            .thenReturn(expectedItems);

        ResponseEntity<Integer> response = analyticsController.getTotalItemsSoldInRange(startDate, endDate);

        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(expectedItems, response.getBody());
        verify(analyticsService, times(1)).calculateTotalItemsSoldInRange(startDate, endDate);
    }

    @Test
    void testGetAverageOrderValueInRange() {
        LocalDate startDate = LocalDate.of(2024, 1, 1);
        LocalDate endDate = LocalDate.of(2024, 1, 31);
        BigDecimal expectedAvg = new BigDecimal("63.33");
        when(analyticsService.calculateAverageOrderValueInRange(startDate, endDate))
            .thenReturn(expectedAvg);

        ResponseEntity<BigDecimal> response = analyticsController.getAverageOrderValueInRange(startDate, endDate);

        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(expectedAvg, response.getBody());
        verify(analyticsService, times(1)).calculateAverageOrderValueInRange(startDate, endDate);
    }

    @Test
    void testGetTopSellingItemsInRange() {
        LocalDate startDate = LocalDate.of(2024, 1, 1);
        LocalDate endDate = LocalDate.of(2024, 1, 31);
        List<Object[]> topItems = Arrays.asList(
            new Object[]{"The Great Gatsby", 25L},
            new Object[]{"To Kill a Mockingbird", 20L}
        );
        when(analyticsService.getTopSellingItemsInRange(startDate, endDate))
            .thenReturn(topItems);

        ResponseEntity<List<Object[]>> response = analyticsController.getTopSellingItemsInRange(startDate, endDate);

        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(2, response.getBody().size());
        assertEquals("The Great Gatsby", response.getBody().get(0)[0]);
        verify(analyticsService, times(1)).getTopSellingItemsInRange(startDate, endDate);
    }

    @Test
    void testGetLeastSellingItemsInRange() {
        LocalDate startDate = LocalDate.of(2024, 1, 1);
        LocalDate endDate = LocalDate.of(2024, 1, 31);
        List<Object[]> leastItems = Arrays.asList(
            new Object[]{"Moby Dick", 3L},
            new Object[]{"1984", 1L}
        );
        when(analyticsService.getLeastSellingItemsInRange(startDate, endDate))
            .thenReturn(leastItems);

        ResponseEntity<List<Object[]>> response = analyticsController.getLeastSellingItemsInRange(startDate, endDate);

        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(2, response.getBody().size());
        assertEquals("Moby Dick", response.getBody().get(0)[0]);
        verify(analyticsService, times(1)).getLeastSellingItemsInRange(startDate, endDate);
    }

    @Test
    void testGetAnalyticsDashboard() {
        LocalDate startDate = LocalDate.of(2024, 1, 1);
        LocalDate endDate = LocalDate.of(2024, 1, 31);
        
        Map<String, Object> dashboard = new HashMap<>();
        dashboard.put("totalRevenue", new BigDecimal("3500.00"));
        dashboard.put("totalOrders", 55);
        dashboard.put("totalItemsSold", 110);
        dashboard.put("averageOrderValue", new BigDecimal("63.33"));
        dashboard.put("topSellingItems", Arrays.asList(new Object[]{"The Great Gatsby", 25L}));
        dashboard.put("leastSellingItems", Arrays.asList(new Object[]{"Moby Dick", 3L}));

        when(analyticsService.getAnalyticsDashboard(startDate, endDate)).thenReturn(dashboard);

        ResponseEntity<Map<String, Object>> response = analyticsController.getAnalyticsDashboard(startDate, endDate);

        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(6, response.getBody().size());
        assertEquals(new BigDecimal("3500.00"), response.getBody().get("totalRevenue"));
        assertEquals(55, response.getBody().get("totalOrders"));
        verify(analyticsService, times(1)).getAnalyticsDashboard(startDate, endDate);
    }
}
