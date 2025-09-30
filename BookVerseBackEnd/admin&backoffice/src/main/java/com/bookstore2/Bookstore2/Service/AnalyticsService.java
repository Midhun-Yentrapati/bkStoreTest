package com.bookstore2.Bookstore2.Service;

import com.bookstore2.Bookstore2.Models.DailySalesSummary;
import com.bookstore2.Bookstore2.Repository.DailySalesSummaryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class AnalyticsService {

    @Autowired
    private DailySalesSummaryRepository dailySalesSummaryRepository;

    public DailySalesSummary saveOrUpdateDailySalesSummary(DailySalesSummary dailySalesSummary) {
        Optional<DailySalesSummary> existingSummary = Optional.ofNullable(dailySalesSummaryRepository.findBySalesDate(dailySalesSummary.getSalesDate()));
        if (existingSummary.isPresent()) {
            DailySalesSummary updatedSummary = existingSummary.get();
            updatedSummary.setTotalRevenue(dailySalesSummary.getTotalRevenue());
            updatedSummary.setTotalOrders(dailySalesSummary.getTotalOrders());
            updatedSummary.setTotalItemsSold(dailySalesSummary.getTotalItemsSold());
            updatedSummary.setAverageOrderValue(dailySalesSummary.getAverageOrderValue());
            updatedSummary.setTopSellingItem(dailySalesSummary.getTopSellingItem());
            updatedSummary.setTopSellingItemQuantity(dailySalesSummary.getTopSellingItemQuantity());
            updatedSummary.setLeastSellingItem(dailySalesSummary.getLeastSellingItem());
            updatedSummary.setLeastSellingItemQuantity(dailySalesSummary.getLeastSellingItemQuantity());
            updatedSummary.setUpdatedAt(java.time.LocalDateTime.now());
            return dailySalesSummaryRepository.save(updatedSummary);
        } else {
            return dailySalesSummaryRepository.save(dailySalesSummary);
        }
    }

    public DailySalesSummary getDailySalesSummaryByDate(LocalDate salesDate) {
        return dailySalesSummaryRepository.findBySalesDate(salesDate);
    }

    public List<DailySalesSummary> getDailySalesSummariesByDateRange(LocalDate startDate, LocalDate endDate) {
        return dailySalesSummaryRepository.findBySalesDateBetweenOrderBySalesDateDesc(startDate, endDate);
    }

    public List<DailySalesSummary> getRecentDailySalesSummaries() {
        return dailySalesSummaryRepository.findTop10ByOrderBySalesDateDesc();
    }

    public BigDecimal calculateTotalRevenueInRange(LocalDate startDate, LocalDate endDate) {
        return dailySalesSummaryRepository.calculateTotalRevenueInRange(startDate, endDate);
    }

    public Integer calculateTotalOrdersInRange(LocalDate startDate, LocalDate endDate) {
        return dailySalesSummaryRepository.calculateTotalOrdersInRange(startDate, endDate);
    }

    public Integer calculateTotalItemsSoldInRange(LocalDate startDate, LocalDate endDate) {
        return dailySalesSummaryRepository.calculateTotalItemsSoldInRange(startDate, endDate);
    }

    public BigDecimal calculateAverageOrderValueInRange(LocalDate startDate, LocalDate endDate) {
        return dailySalesSummaryRepository.calculateAverageOrderValueInRange(startDate, endDate);
    }

    public List<Object[]> getTopSellingItemsInRange(LocalDate startDate, LocalDate endDate) {
        return dailySalesSummaryRepository.findTopSellingItemsInRange(startDate, endDate);
    }

    public List<Object[]> getLeastSellingItemsInRange(LocalDate startDate, LocalDate endDate) {
        return dailySalesSummaryRepository.findLeastSellingItemsInRange(startDate, endDate);
    }

    public Map<String, Object> getAnalyticsDashboard(LocalDate startDate, LocalDate endDate) {
        Map<String, Object> dashboard = new HashMap<>();

        dashboard.put("totalRevenue", calculateTotalRevenueInRange(startDate, endDate));
        dashboard.put("totalOrders", calculateTotalOrdersInRange(startDate, endDate));
        dashboard.put("totalItemsSold", calculateTotalItemsSoldInRange(startDate, endDate));
        dashboard.put("averageOrderValue", calculateAverageOrderValueInRange(startDate, endDate));
        dashboard.put("topSellingItems", getTopSellingItemsInRange(startDate, endDate));
        dashboard.put("leastSellingItems", getLeastSellingItemsInRange(startDate, endDate));

        return dashboard;
    }
}
