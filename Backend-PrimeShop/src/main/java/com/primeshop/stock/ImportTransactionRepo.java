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
public interface ImportTransactionRepo extends JpaRepository<ImportTransaction, Long> {
    
    Page<ImportTransaction> findByProduct(Product product, Pageable pageable);
    
    List<ImportTransaction> findByProductOrderByImportDateDesc(Product product);
    
    @Query("SELECT SUM(it.totalCost) FROM ImportTransaction it WHERE it.product = :product")
    BigDecimal getTotalCostByProduct(@Param("product") Product product);
    
    @Query("SELECT SUM(it.quantity) FROM ImportTransaction it WHERE it.product = :product")
    Integer getTotalQuantityByProduct(@Param("product") Product product);
    
    List<ImportTransaction> findByImportDateBetween(LocalDateTime startDate, LocalDateTime endDate);
    
    @Query("SELECT SUM(it.totalCost) FROM ImportTransaction it WHERE it.importDate BETWEEN :startDate AND :endDate")
    BigDecimal getTotalCostByDateRange(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);
} 