package com.bookstore2.Bookstore2.Controller;

import com.bookstore2.Bookstore2.Models.DailySalesSummary;
import com.bookstore2.Bookstore2.Service.AnalyticsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

@RestController
@RequestMapping("/api/analytics")
public class AnalyticsController {

    @Autowired
    private AnalyticsService analyticsService;
    
    private final RestTemplate restTemplate = new RestTemplate();
    private final String ORDER_SERVICE_URL = "http://localhost:8090/api/orders";
    private final String BOOK_SERVICE_URL = "http://localhost:8090/api/books";
    
    @GetMapping("/health")
    public ResponseEntity<Map<String, String>> health() {
        Map<String, String> status = new HashMap<>();
        status.put("status", "UP");
        status.put("service", "admin-backoffice-service");
        status.put("timestamp", LocalDateTime.now().toString());
        return ResponseEntity.ok(status);
    }

    @GetMapping("/dashboard")
    public ResponseEntity<Map<String, Object>> getDashboard(@RequestParam(required = false) String startDate, @RequestParam(required = false) String endDate) {
        return getDashboardStats();
    }

    @GetMapping("/dashboard-stats")
    public ResponseEntity<Map<String, Object>> getDashboardStats() {
        Map<String, Object> stats = new HashMap<>();
        
        try {
            // Fetch orders from CartOrderManagement service
            Map<String, Object>[] ordersArray = restTemplate.getForObject(ORDER_SERVICE_URL, Map[].class);
            List<Map<String, Object>> orders = ordersArray != null ? Arrays.asList(ordersArray) : new ArrayList<>();
            
            // Calculate stats
            int totalOrders = orders.size();
            int pendingOrders = (int) orders.stream().filter(o -> "Pending".equals(o.get("orderStatus"))).count();
            int deliveredOrders = (int) orders.stream().filter(o -> "Delivered".equals(o.get("orderStatus"))).count();
            
            BigDecimal totalRevenue = orders.stream()
                .filter(o -> "Delivered".equals(o.get("orderStatus")) || "Paid".equals(o.get("paymentStatus")))
                .map(o -> {
                    Object grandTotal = o.get("grandTotal");
                    if (grandTotal != null) {
                        return new BigDecimal(grandTotal.toString());
                    }
                    return BigDecimal.ZERO;
                })
                .reduce(BigDecimal.ZERO, BigDecimal::add);
            
            stats.put("totalOrders", totalOrders);
            stats.put("pendingOrders", pendingOrders);
            stats.put("deliveredOrders", deliveredOrders);
            stats.put("totalRevenue", totalRevenue);
            
        } catch (Exception e) {
            System.out.println("Error fetching dashboard stats: " + e.getMessage());
            e.printStackTrace();
            // Fallback data
            stats.put("totalOrders", 0);
            stats.put("pendingOrders", 0);
            stats.put("deliveredOrders", 0);
            stats.put("totalRevenue", BigDecimal.ZERO);
            stats.put("error", "Failed to fetch data from order service");
        }
        
        return ResponseEntity.ok(stats);
    }

    @GetMapping("/sales-trends")
    public ResponseEntity<Map<String, Object>> getSalesTrends() {
        Map<String, Object> trends = new HashMap<>();
        
        try {
            Map<String, Object>[] ordersArray = restTemplate.getForObject(ORDER_SERVICE_URL, Map[].class);
            List<Map<String, Object>> orders = ordersArray != null ? Arrays.asList(ordersArray) : new ArrayList<>();
            
            // Group by year and calculate revenue
            Map<Integer, BigDecimal> yearlyRevenue = new HashMap<>();
            for (Map<String, Object> order : orders) {
                if ("Delivered".equals(order.get("orderStatus"))) {
                    Object placedAtObj = order.get("placedAt");
                    Object grandTotalObj = order.get("grandTotal");
                    
                    if (placedAtObj != null && grandTotalObj != null) {
                        try {
                            String placedAt = placedAtObj.toString();
                            int year = Integer.parseInt(placedAt.substring(0, 4));
                            BigDecimal amount = new BigDecimal(grandTotalObj.toString());
                            yearlyRevenue.merge(year, amount, BigDecimal::add);
                        } catch (Exception e) {
                            // Skip invalid date/amount entries
                            continue;
                        }
                    }
                }
            }
            
            // If no data, provide current year with zero
            if (yearlyRevenue.isEmpty()) {
                yearlyRevenue.put(2024, BigDecimal.ZERO);
            }
            
            trends.put("yearlyRevenue", yearlyRevenue);
            
        } catch (Exception e) {
            System.out.println("Error fetching sales trends: " + e.getMessage());
            trends.put("yearlyRevenue", Map.of(2024, BigDecimal.ZERO));
        }
        
        return ResponseEntity.ok(trends);
    }

    @GetMapping("/top-selling-books")
    public ResponseEntity<List<Map<String, Object>>> getTopSellingBooks() {
        try {
            // Get all orders and aggregate book sales
            Map<String, Object>[] ordersArray = restTemplate.getForObject(ORDER_SERVICE_URL, Map[].class);
            List<Map<String, Object>> orders = ordersArray != null ? Arrays.asList(ordersArray) : new ArrayList<>();
            
            Map<String, Integer> bookSales = new HashMap<>();
            Map<String, String> bookTitles = new HashMap<>();
            
            for (Map<String, Object> order : orders) {
                List<Map<String, Object>> orderItems = (List<Map<String, Object>>) order.get("orderItems");
                if (orderItems != null) {
                    for (Map<String, Object> item : orderItems) {
                        String bookId = (String) item.get("bookId");
                        String title = (String) item.get("title");
                        Integer quantity = (Integer) item.get("quantity");
                        
                        if (bookId != null && quantity != null) {
                            bookSales.merge(bookId, quantity, Integer::sum);
                            if (title != null) {
                                bookTitles.put(bookId, title);
                            }
                        }
                    }
                }
            }
            
            // Convert to list and sort by sales
            List<Map<String, Object>> topBooks = bookSales.entrySet().stream()
                .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
                .limit(6)
                .map(entry -> {
                    Map<String, Object> book = new HashMap<>();
                    book.put("bookId", entry.getKey());
                    book.put("title", bookTitles.getOrDefault(entry.getKey(), "Book #" + entry.getKey()));
                    book.put("quantitySold", entry.getValue());
                    return book;
                })
                .collect(ArrayList::new, (list, item) -> list.add(item), (list1, list2) -> list1.addAll(list2));
            
            return ResponseEntity.ok(topBooks);
        } catch (Exception e) {
            System.out.println("Error fetching top selling books: " + e.getMessage());
            return ResponseEntity.ok(new ArrayList<>());
        }
    }

    @GetMapping("/least-selling-books")
    public ResponseEntity<List<Map<String, Object>>> getLeastSellingBooks() {
        try {
            // Get all orders and aggregate book sales
            Map<String, Object>[] ordersArray = restTemplate.getForObject(ORDER_SERVICE_URL, Map[].class);
            List<Map<String, Object>> orders = ordersArray != null ? Arrays.asList(ordersArray) : new ArrayList<>();
            
            Map<String, Integer> bookSales = new HashMap<>();
            Map<String, String> bookTitles = new HashMap<>();
            
            for (Map<String, Object> order : orders) {
                List<Map<String, Object>> orderItems = (List<Map<String, Object>>) order.get("orderItems");
                if (orderItems != null) {
                    for (Map<String, Object> item : orderItems) {
                        String bookId = (String) item.get("bookId");
                        String title = (String) item.get("title");
                        Integer quantity = (Integer) item.get("quantity");
                        
                        if (bookId != null && quantity != null) {
                            bookSales.merge(bookId, quantity, Integer::sum);
                            if (title != null) {
                                bookTitles.put(bookId, title);
                            }
                        }
                    }
                }
            }
            
            // Convert to list and sort by sales (ascending for least sold)
            List<Map<String, Object>> leastBooks = bookSales.entrySet().stream()
                .sorted(Map.Entry.<String, Integer>comparingByValue())
                .limit(6)
                .map(entry -> {
                    Map<String, Object> book = new HashMap<>();
                    book.put("bookId", entry.getKey());
                    book.put("title", bookTitles.getOrDefault(entry.getKey(), "Book #" + entry.getKey()));
                    book.put("quantitySold", entry.getValue());
                    return book;
                })
                .collect(ArrayList::new, (list, item) -> list.add(item), (list1, list2) -> list1.addAll(list2));
            
            return ResponseEntity.ok(leastBooks);
        } catch (Exception e) {
            System.out.println("Error fetching least selling books: " + e.getMessage());
            return ResponseEntity.ok(new ArrayList<>());
        }
    }

    // Daily Sales Summary Management Methods
    
    @PostMapping("/daily-sales-summary")
    public ResponseEntity<DailySalesSummary> processDailySalesSummary(@RequestBody DailySalesSummary dailySalesSummary) {
        try {
            DailySalesSummary savedSummary = analyticsService.saveOrUpdateDailySalesSummary(dailySalesSummary);
            return ResponseEntity.ok(savedSummary);
        } catch (Exception e) {
            System.out.println("Error processing daily sales summary: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    @GetMapping("/daily-sales-summaries/recent")
    public ResponseEntity<List<DailySalesSummary>> getRecentDailySalesSummaries() {
        try {
            List<DailySalesSummary> summaries = analyticsService.getRecentDailySalesSummaries();
            return ResponseEntity.ok(summaries);
        } catch (Exception e) {
            System.out.println("Error fetching recent daily sales summaries: " + e.getMessage());
            return ResponseEntity.ok(new ArrayList<>());
        }
    }
    
    @GetMapping("/daily-sales-summary/{date}")
    public ResponseEntity<DailySalesSummary> getDailySalesSummaryByDate(@PathVariable LocalDate date) {
        try {
            DailySalesSummary summary = analyticsService.getDailySalesSummaryByDate(date);
            if (summary != null) {
                return ResponseEntity.ok(summary);
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            System.out.println("Error fetching daily sales summary by date: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    @GetMapping("/daily-sales-summaries")
    public ResponseEntity<List<DailySalesSummary>> getDailySalesSummariesByDateRange(
            @RequestParam LocalDate startDate, 
            @RequestParam LocalDate endDate) {
        try {
            List<DailySalesSummary> summaries = analyticsService.getDailySalesSummariesByDateRange(startDate, endDate);
            return ResponseEntity.ok(summaries);
        } catch (Exception e) {
            System.out.println("Error fetching daily sales summaries by date range: " + e.getMessage());
            return ResponseEntity.ok(new ArrayList<>());
        }
    }
    
    // Analytics Calculation Methods
    
    @GetMapping("/total-revenue")
    public ResponseEntity<BigDecimal> getTotalRevenueInRange(
            @RequestParam LocalDate startDate, 
            @RequestParam LocalDate endDate) {
        try {
            BigDecimal totalRevenue = analyticsService.calculateTotalRevenueInRange(startDate, endDate);
            return ResponseEntity.ok(totalRevenue);
        } catch (Exception e) {
            System.out.println("Error calculating total revenue: " + e.getMessage());
            return ResponseEntity.ok(BigDecimal.ZERO);
        }
    }
    
    @GetMapping("/total-orders")
    public ResponseEntity<Integer> getTotalOrdersInRange(
            @RequestParam LocalDate startDate, 
            @RequestParam LocalDate endDate) {
        try {
            Integer totalOrders = analyticsService.calculateTotalOrdersInRange(startDate, endDate);
            return ResponseEntity.ok(totalOrders);
        } catch (Exception e) {
            System.out.println("Error calculating total orders: " + e.getMessage());
            return ResponseEntity.ok(0);
        }
    }
    
    @GetMapping("/total-items-sold")
    public ResponseEntity<Integer> getTotalItemsSoldInRange(
            @RequestParam LocalDate startDate, 
            @RequestParam LocalDate endDate) {
        try {
            Integer totalItems = analyticsService.calculateTotalItemsSoldInRange(startDate, endDate);
            return ResponseEntity.ok(totalItems);
        } catch (Exception e) {
            System.out.println("Error calculating total items sold: " + e.getMessage());
            return ResponseEntity.ok(0);
        }
    }
    
    @GetMapping("/average-order-value")
    public ResponseEntity<BigDecimal> getAverageOrderValueInRange(
            @RequestParam LocalDate startDate, 
            @RequestParam LocalDate endDate) {
        try {
            BigDecimal avgOrderValue = analyticsService.calculateAverageOrderValueInRange(startDate, endDate);
            return ResponseEntity.ok(avgOrderValue);
        } catch (Exception e) {
            System.out.println("Error calculating average order value: " + e.getMessage());
            return ResponseEntity.ok(BigDecimal.ZERO);
        }
    }
    
    @GetMapping("/top-selling-items")
    public ResponseEntity<List<Object[]>> getTopSellingItemsInRange(
            @RequestParam LocalDate startDate, 
            @RequestParam LocalDate endDate) {
        try {
            List<Object[]> topItems = analyticsService.getTopSellingItemsInRange(startDate, endDate);
            return ResponseEntity.ok(topItems);
        } catch (Exception e) {
            System.out.println("Error fetching top selling items: " + e.getMessage());
            return ResponseEntity.ok(new ArrayList<>());
        }
    }
    
    @GetMapping("/least-selling-items")
    public ResponseEntity<List<Object[]>> getLeastSellingItemsInRange(
            @RequestParam LocalDate startDate, 
            @RequestParam LocalDate endDate) {
        try {
            List<Object[]> leastItems = analyticsService.getLeastSellingItemsInRange(startDate, endDate);
            return ResponseEntity.ok(leastItems);
        } catch (Exception e) {
            System.out.println("Error fetching least selling items: " + e.getMessage());
            return ResponseEntity.ok(new ArrayList<>());
        }
    }
    
    @GetMapping("/analytics-dashboard")
    public ResponseEntity<Map<String, Object>> getAnalyticsDashboard(
            @RequestParam LocalDate startDate, 
            @RequestParam LocalDate endDate) {
        try {
            Map<String, Object> dashboard = analyticsService.getAnalyticsDashboard(startDate, endDate);
            return ResponseEntity.ok(dashboard);
        } catch (Exception e) {
            System.out.println("Error fetching analytics dashboard: " + e.getMessage());
            Map<String, Object> errorDashboard = new HashMap<>();
            errorDashboard.put("error", "Failed to fetch analytics data");
            return ResponseEntity.ok(errorDashboard);
        }
    }
}