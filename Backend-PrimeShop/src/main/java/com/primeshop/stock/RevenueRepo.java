package com.primeshop.stock;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Repository
public interface RevenueRepo extends JpaRepository<Revenue, Long> {
    
    Optional<Revenue> findByPeriod(String period);
    
    List<Revenue> findByPeriodOrderByPeriodDesc(String period);
    
    @Query("SELECT r FROM Revenue r WHERE r.period LIKE :year% ORDER BY r.period DESC")
    List<Revenue> findByYear(@Param("year") String year);
    
    @Query("SELECT SUM(r.totalRevenue) FROM Revenue r WHERE r.period LIKE :year%")
    BigDecimal getTotalRevenueByYear(@Param("year") String year);
    
    @Query("SELECT SUM(r.totalProfit) FROM Revenue r WHERE r.period LIKE :year%")
    BigDecimal getTotalProfitByYear(@Param("year") String year);
    
    @Query("SELECT r FROM Revenue r ORDER BY r.period DESC")
    List<Revenue> findAllOrderByPeriodDesc();
} 