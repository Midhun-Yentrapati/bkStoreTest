package com.bookstore2.Bookstore2.Repository;

import com.bookstore2.Bookstore2.Models.DailySalesSummary;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Repository
public interface DailySalesSummaryRepository extends JpaRepository<DailySalesSummary, Long> {

    DailySalesSummary findBySalesDate(LocalDate salesDate);
    List<DailySalesSummary> findBySalesDateBetweenOrderBySalesDateDesc(LocalDate startDate, LocalDate endDate);
    List<DailySalesSummary> findTop10ByOrderBySalesDateDesc();

    @Query("SELECT COALESCE(SUM(d.totalRevenue), 0) FROM DailySalesSummary d WHERE d.salesDate BETWEEN :startDate AND :endDate")
    BigDecimal calculateTotalRevenueInRange(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

    @Query("SELECT COALESCE(SUM(d.totalOrders), 0) FROM DailySalesSummary d WHERE d.salesDate BETWEEN :startDate AND :endDate")
    Integer calculateTotalOrdersInRange(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

    @Query("SELECT COALESCE(SUM(d.totalItemsSold), 0) FROM DailySalesSummary d WHERE d.salesDate BETWEEN :startDate AND :endDate")
    Integer calculateTotalItemsSoldInRange(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

    @Query("SELECT AVG(d.averageOrderValue) FROM DailySalesSummary d WHERE d.salesDate BETWEEN :startDate AND :endDate")
    BigDecimal calculateAverageOrderValueInRange(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

    @Query("SELECT d.topSellingItem, SUM(d.topSellingItemQuantity) as totalQuantity " +
           "FROM DailySalesSummary d " +
           "WHERE d.salesDate BETWEEN :startDate AND :endDate " +
           "GROUP BY d.topSellingItem " +
           "ORDER BY totalQuantity DESC")
    List<Object[]> findTopSellingItemsInRange(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

    @Query("SELECT d.leastSellingItem, SUM(d.leastSellingItemQuantity) as totalQuantity " +
           "FROM DailySalesSummary d " +
           "WHERE d.salesDate BETWEEN :startDate AND :endDate " +
           "GROUP BY d.leastSellingItem " +
           "ORDER BY totalQuantity ASC")
    List<Object[]> findLeastSellingItemsInRange(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);
}
