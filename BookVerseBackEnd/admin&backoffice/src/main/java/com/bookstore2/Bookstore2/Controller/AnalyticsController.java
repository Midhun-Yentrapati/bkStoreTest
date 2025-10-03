package com.bookstore2.Bookstore2.Controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

@RestController
@RequestMapping("/api/analytics")
public class AnalyticsController {

    private final RestTemplate restTemplate = new RestTemplate();
    private final String ORDER_SERVICE_URL = "http://localhost:8090/api/orders";
    private final String BOOK_SERVICE_URL = "http://localhost:8090/api/books";

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
            // Fallback data
            stats.put("totalOrders", 0);
            stats.put("pendingOrders", 0);
            stats.put("deliveredOrders", 0);
            stats.put("totalRevenue", BigDecimal.ZERO);
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
}