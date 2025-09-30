package com.bookstore2.Bookstore2.Service;

import com.bookstore2.Bookstore2.Models.DailySalesSummary;
import com.bookstore2.Bookstore2.Repository.DailySalesSummaryRepository;
import org.junit.jupiter.api.BeforeEach;
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
import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AnalyticsServiceTest {

    @Mock
    private DailySalesSummaryRepository dailySalesSummaryRepository;

    @InjectMocks
    private AnalyticsService analyticsService;

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
    void testSaveOrUpdateDailySalesSummary_NewSummary() {
        when(dailySalesSummaryRepository.findBySalesDate(any(LocalDate.class))).thenReturn(null);
        when(dailySalesSummaryRepository.save(any(DailySalesSummary.class))).thenReturn(sampleSalesSummary);

        DailySalesSummary result = analyticsService.saveOrUpdateDailySalesSummary(sampleSalesSummary);

        assertNotNull(result);
        assertEquals(LocalDate.of(2024, 1, 15), result.getSalesDate());
        assertEquals(new BigDecimal("1500.00"), result.getTotalRevenue());
        assertEquals(25, result.getTotalOrders());
        verify(dailySalesSummaryRepository, times(1)).findBySalesDate(any(LocalDate.class));
        verify(dailySalesSummaryRepository, times(1)).save(any(DailySalesSummary.class));
    }

    @Test
    void testGetDailySalesSummaryByDate() {
        LocalDate salesDate = LocalDate.of(2024, 1, 15);
        when(dailySalesSummaryRepository.findBySalesDate(salesDate)).thenReturn(sampleSalesSummary);

        DailySalesSummary result = analyticsService.getDailySalesSummaryByDate(salesDate);

        assertNotNull(result);
        assertEquals(salesDate, result.getSalesDate());
        assertEquals(new BigDecimal("1500.00"), result.getTotalRevenue());
        verify(dailySalesSummaryRepository, times(1)).findBySalesDate(salesDate);
    }

    @Test
    void testGetRecentDailySalesSummaries() {
        when(dailySalesSummaryRepository.findTop10ByOrderBySalesDateDesc()).thenReturn(sampleSalesSummaries);

        List<DailySalesSummary> result = analyticsService.getRecentDailySalesSummaries();

        assertNotNull(result);
        assertEquals(2, result.size());
        verify(dailySalesSummaryRepository, times(1)).findTop10ByOrderBySalesDateDesc();
    }

    @Test
    void testCalculateTotalRevenueInRange() {
        LocalDate startDate = LocalDate.of(2024, 1, 1);
        LocalDate endDate = LocalDate.of(2024, 1, 31);
        BigDecimal expectedRevenue = new BigDecimal("3500.00");
        when(dailySalesSummaryRepository.calculateTotalRevenueInRange(startDate, endDate))
            .thenReturn(expectedRevenue);

        BigDecimal result = analyticsService.calculateTotalRevenueInRange(startDate, endDate);

        assertNotNull(result);
        assertEquals(expectedRevenue, result);
        verify(dailySalesSummaryRepository, times(1)).calculateTotalRevenueInRange(startDate, endDate);
    }

    @Test
    void testGetAnalyticsDashboard() {
        LocalDate startDate = LocalDate.of(2024, 1, 1);
        LocalDate endDate = LocalDate.of(2024, 1, 31);
        
        BigDecimal totalRevenue = new BigDecimal("3500.00");
        Integer totalOrders = 55;
        Integer totalItems = 110;
        BigDecimal avgOrderValue = new BigDecimal("63.33");
        
        List<Object[]> topItems = new ArrayList<>();
        topItems.add(new Object[]{"The Great Gatsby", 25L});
        
        List<Object[]> leastItems = new ArrayList<>();
        leastItems.add(new Object[]{"Moby Dick", 3L});

        when(dailySalesSummaryRepository.calculateTotalRevenueInRange(startDate, endDate))
            .thenReturn(totalRevenue);
        when(dailySalesSummaryRepository.calculateTotalOrdersInRange(startDate, endDate))
            .thenReturn(totalOrders);
        when(dailySalesSummaryRepository.calculateTotalItemsSoldInRange(startDate, endDate))
            .thenReturn(totalItems);
        when(dailySalesSummaryRepository.calculateAverageOrderValueInRange(startDate, endDate))
            .thenReturn(avgOrderValue);
        when(dailySalesSummaryRepository.findTopSellingItemsInRange(startDate, endDate))
            .thenReturn(topItems);
        when(dailySalesSummaryRepository.findLeastSellingItemsInRange(startDate, endDate))
            .thenReturn(leastItems);

        Map<String, Object> result = analyticsService.getAnalyticsDashboard(startDate, endDate);

        assertNotNull(result);
        assertEquals(6, result.size());
        assertEquals(totalRevenue, result.get("totalRevenue"));
        assertEquals(totalOrders, result.get("totalOrders"));
        assertEquals(totalItems, result.get("totalItemsSold"));
        assertEquals(avgOrderValue, result.get("averageOrderValue"));
        assertEquals(topItems, result.get("topSellingItems"));
        assertEquals(leastItems, result.get("leastSellingItems"));
        
        verify(dailySalesSummaryRepository, times(1)).calculateTotalRevenueInRange(startDate, endDate);
        verify(dailySalesSummaryRepository, times(1)).calculateTotalOrdersInRange(startDate, endDate);
        verify(dailySalesSummaryRepository, times(1)).calculateTotalItemsSoldInRange(startDate, endDate);
        verify(dailySalesSummaryRepository, times(1)).calculateAverageOrderValueInRange(startDate, endDate);
        verify(dailySalesSummaryRepository, times(1)).findTopSellingItemsInRange(startDate, endDate);
        verify(dailySalesSummaryRepository, times(1)).findLeastSellingItemsInRange(startDate, endDate);
    }
}
