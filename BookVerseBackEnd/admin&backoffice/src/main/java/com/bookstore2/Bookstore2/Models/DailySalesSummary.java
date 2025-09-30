package com.bookstore2.Bookstore2.Models;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "daily_sales_summary")
public class DailySalesSummary {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "sales_date", nullable = false, unique = true)
    private LocalDate salesDate;

    @Column(name = "total_revenue", precision = 10, scale = 2)
    private BigDecimal totalRevenue;

    @Column(name = "total_orders")
    private Integer totalOrders;

    @Column(name = "total_items_sold")
    private Integer totalItemsSold;

    @Column(name = "average_order_value", precision = 10, scale = 2)
    private BigDecimal averageOrderValue;

    @Column(name = "top_selling_item")
    private String topSellingItem;

    @Column(name = "top_selling_item_quantity")
    private Integer topSellingItemQuantity;

    @Column(name = "least_selling_item")
    private String leastSellingItem;

    @Column(name = "least_selling_item_quantity")
    private Integer leastSellingItemQuantity;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public DailySalesSummary() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    public DailySalesSummary(LocalDate salesDate, BigDecimal totalRevenue, Integer totalOrders,
                             Integer totalItemsSold, BigDecimal averageOrderValue, String topSellingItem,
                             Integer topSellingItemQuantity, String leastSellingItem, Integer leastSellingItemQuantity) {
        this.salesDate = salesDate;
        this.totalRevenue = totalRevenue;
        this.totalOrders = totalOrders;
        this.totalItemsSold = totalItemsSold;
        this.averageOrderValue = averageOrderValue;
        this.topSellingItem = topSellingItem;
        this.topSellingItemQuantity = topSellingItemQuantity;
        this.leastSellingItem = leastSellingItem;
        this.leastSellingItemQuantity = leastSellingItemQuantity;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public LocalDate getSalesDate() { return salesDate; }
    public void setSalesDate(LocalDate salesDate) { this.salesDate = salesDate; }
    public BigDecimal getTotalRevenue() { return totalRevenue; }
    public void setTotalRevenue(BigDecimal totalRevenue) { this.totalRevenue = totalRevenue; }
    public Integer getTotalOrders() { return totalOrders; }
    public void setTotalOrders(Integer totalOrders) { this.totalOrders = totalOrders; }
    public Integer getTotalItemsSold() { return totalItemsSold; }
    public void setTotalItemsSold(Integer totalItemsSold) { this.totalItemsSold = totalItemsSold; }
    public BigDecimal getAverageOrderValue() { return averageOrderValue; }
    public void setAverageOrderValue(BigDecimal averageOrderValue) { this.averageOrderValue = averageOrderValue; }
    public String getTopSellingItem() { return topSellingItem; }
    public void setTopSellingItem(String topSellingItem) { this.topSellingItem = topSellingItem; }
    public Integer getTopSellingItemQuantity() { return topSellingItemQuantity; }
    public void setTopSellingItemQuantity(Integer topSellingItemQuantity) { this.topSellingItemQuantity = topSellingItemQuantity; }
    public String getLeastSellingItem() { return leastSellingItem; }
    public void setLeastSellingItem(String leastSellingItem) { this.leastSellingItem = leastSellingItem; }
    public Integer getLeastSellingItemQuantity() { return leastSellingItemQuantity; }
    public void setLeastSellingItemQuantity(Integer leastSellingItemQuantity) { this.leastSellingItemQuantity = leastSellingItemQuantity; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
