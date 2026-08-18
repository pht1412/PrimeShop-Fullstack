package com.primeshop.stock;

import com.primeshop.product.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ExportTransactionRepo extends JpaRepository<ExportTransaction, Long> {
    
    Page<ExportTransaction> findByProduct(Product product, Pageable pageable);
    
    List<ExportTransaction> findByProductOrderByExportDateDesc(Product product);
    
    @Query("SELECT SUM(et.totalRevenue) FROM ExportTransaction et WHERE et.product = :product")
    BigDecimal getTotalRevenueByProduct(@Param("product") Product product);
    
    @Query("SELECT SUM(et.profit) FROM ExportTransaction et WHERE et.product = :product")
    BigDecimal getTotalProfitByProduct(@Param("product") Product product);
    
    @Query("SELECT SUM(et.quantity) FROM ExportTransaction et WHERE et.product = :product")
    Integer getTotalQuantityByProduct(@Param("product") Product product);
    
    List<ExportTransaction> findByExportDateBetween(LocalDateTime startDate, LocalDateTime endDate);
    
    @Query("SELECT SUM(et.totalRevenue) FROM ExportTransaction et WHERE et.exportDate BETWEEN :startDate AND :endDate")
    BigDecimal getTotalRevenueByDateRange(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);
    
    @Query("SELECT SUM(et.profit) FROM ExportTransaction et WHERE et.exportDate BETWEEN :startDate AND :endDate")
    BigDecimal getTotalProfitByDateRange(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);
} 