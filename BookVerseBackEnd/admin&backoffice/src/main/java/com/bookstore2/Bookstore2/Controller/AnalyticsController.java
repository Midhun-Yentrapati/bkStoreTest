package com.bookstore2.Bookstore2.Controller;

import com.bookstore2.Bookstore2.Models.DailySalesSummary;
import com.bookstore2.Bookstore2.Service.AnalyticsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/analytics")
@CrossOrigin(origins = "*")
public class AnalyticsController {

    @Autowired
    private AnalyticsService analyticsService;

    @PostMapping("/daily-sales/process")
    public ResponseEntity<DailySalesSummary> processDailySalesSummary(@RequestBody DailySalesSummary dailySalesSummary) {
        DailySalesSummary savedSummary = analyticsService.saveOrUpdateDailySalesSummary(dailySalesSummary);
        return ResponseEntity.ok(savedSummary);
    }

    @GetMapping("/daily-sales/recent")
    public ResponseEntity<List<DailySalesSummary>> getRecentDailySalesSummaries() {
        List<DailySalesSummary> recentSummaries = analyticsService.getRecentDailySalesSummaries();
        return ResponseEntity.ok(recentSummaries);
    }

    @GetMapping("/daily-sales/{salesDate}")
    public ResponseEntity<DailySalesSummary> getDailySalesSummaryByDate(@PathVariable LocalDate salesDate) {
        DailySalesSummary summary = analyticsService.getDailySalesSummaryByDate(salesDate);
        return summary != null ? ResponseEntity.ok(summary) : ResponseEntity.notFound().build();
    }

    @GetMapping("/daily-sales/date-range")
    public ResponseEntity<List<DailySalesSummary>> getDailySalesSummariesByDateRange(
            @RequestParam LocalDate startDate, @RequestParam LocalDate endDate) {
        List<DailySalesSummary> summaries = analyticsService.getDailySalesSummariesByDateRange(startDate, endDate);
        return ResponseEntity.ok(summaries);
    }

    @GetMapping("/stats/total-revenue")
    public ResponseEntity<BigDecimal> getTotalRevenueInRange(
            @RequestParam LocalDate startDate, @RequestParam LocalDate endDate) {
        BigDecimal totalRevenue = analyticsService.calculateTotalRevenueInRange(startDate, endDate);
        return ResponseEntity.ok(totalRevenue);
    }

    @GetMapping("/stats/total-orders")
    public ResponseEntity<Integer> getTotalOrdersInRange(
            @RequestParam LocalDate startDate, @RequestParam LocalDate endDate) {
        Integer totalOrders = analyticsService.calculateTotalOrdersInRange(startDate, endDate);
        return ResponseEntity.ok(totalOrders);
    }

    @GetMapping("/stats/total-items-sold")
    public ResponseEntity<Integer> getTotalItemsSoldInRange(
            @RequestParam LocalDate startDate, @RequestParam LocalDate endDate) {
        Integer totalItemsSold = analyticsService.calculateTotalItemsSoldInRange(startDate, endDate);
        return ResponseEntity.ok(totalItemsSold);
    }

    @GetMapping("/stats/average-order-value")
    public ResponseEntity<BigDecimal> getAverageOrderValueInRange(
            @RequestParam LocalDate startDate, @RequestParam LocalDate endDate) {
        BigDecimal averageOrderValue = analyticsService.calculateAverageOrderValueInRange(startDate, endDate);
        return ResponseEntity.ok(averageOrderValue);
    }

    @GetMapping("/stats/top-selling-items")
    public ResponseEntity<List<Object[]>> getTopSellingItemsInRange(
            @RequestParam LocalDate startDate, @RequestParam LocalDate endDate) {
        List<Object[]> topSellingItems = analyticsService.getTopSellingItemsInRange(startDate, endDate);
        return ResponseEntity.ok(topSellingItems);
    }

    @GetMapping("/stats/least-selling-items")
    public ResponseEntity<List<Object[]>> getLeastSellingItemsInRange(
            @RequestParam LocalDate startDate, @RequestParam LocalDate endDate) {
        List<Object[]> leastSellingItems = analyticsService.getLeastSellingItemsInRange(startDate, endDate);
        return ResponseEntity.ok(leastSellingItems);
    }

    @GetMapping("/dashboard")
    public ResponseEntity<Map<String, Object>> getAnalyticsDashboard(
            @RequestParam LocalDate startDate, @RequestParam LocalDate endDate) {
        Map<String, Object> dashboard = analyticsService.getAnalyticsDashboard(startDate, endDate);
        return ResponseEntity.ok(dashboard);
    }
}
