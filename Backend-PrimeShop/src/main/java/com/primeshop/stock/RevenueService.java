package com.primeshop.stock;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
public class RevenueService {
    
    @Autowired
    private RevenueRepo revenueRepo;
    
    @Autowired
    private ExportTransactionService exportTransactionService;
    
    public Revenue getRevenueByPeriod(String period) {
        return revenueRepo.findByPeriod(period).orElse(null);
    }
    
    public List<Revenue> getRevenueByYear(String year) {
        return revenueRepo.findByYear(year);
    }
    
    public List<Revenue> getAllRevenueOrderByPeriod() {
        return revenueRepo.findAllOrderByPeriodDesc();
    }
    
    public BigDecimal getTotalRevenueByYear(String year) {
        BigDecimal totalRevenue = revenueRepo.getTotalRevenueByYear(year);
        return totalRevenue != null ? totalRevenue : BigDecimal.ZERO;
    }
    
    public BigDecimal getTotalProfitByYear(String year) {
        BigDecimal totalProfit = revenueRepo.getTotalProfitByYear(year);
        return totalProfit != null ? totalProfit : BigDecimal.ZERO;
    }
    
    public Revenue updateRevenueFromExportTransaction(ExportTransaction exportTransaction) {
        String period = exportTransaction.getExportDate().format(DateTimeFormatter.ofPattern("yyyy-MM"));
        
        Revenue revenue = revenueRepo.findByPeriod(period)
            .orElseGet(() -> {
                Revenue newRevenue = new Revenue();
                newRevenue.setPeriod(period);
                return newRevenue;
            });
        
        revenue.addExportRevenue(exportTransaction.getTotalRevenue(), exportTransaction.getProfit());
        
        return revenueRepo.save(revenue);
    }
    
    public Revenue updateRevenueFromOrder(BigDecimal orderRevenue, BigDecimal orderProfit) {
        String period = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM"));
        return updateRevenueFromOrder(orderRevenue, orderProfit, period);
    }
    
    public Revenue updateRevenueFromOrder(BigDecimal orderRevenue, BigDecimal orderProfit, String period) {
        Revenue revenue = revenueRepo.findByPeriod(period)
            .orElseGet(() -> {
                Revenue newRevenue = new Revenue();
                newRevenue.setPeriod(period);
                return newRevenue;
            });
        
        revenue.addOrderRevenue(orderRevenue, orderProfit);
        
        return revenueRepo.save(revenue);
    }
    
    public Revenue calculateMonthlyRevenue(String period) {
        // Tính toán doanh thu từ xuất hàng trong tháng
        LocalDateTime startOfMonth = LocalDateTime.parse(period + "-01T00:00:00");
        LocalDateTime endOfMonth = startOfMonth.plusMonths(1).minusSeconds(1);
        
        BigDecimal exportRevenue = exportTransactionService.getTotalRevenueByDateRange(startOfMonth, endOfMonth);
        BigDecimal exportProfit = exportTransactionService.getTotalProfitByDateRange(startOfMonth, endOfMonth);
        
        Revenue revenue = revenueRepo.findByPeriod(period)
            .orElseGet(() -> {
                Revenue newRevenue = new Revenue();
                newRevenue.setPeriod(period);
                return newRevenue;
            });
        
        revenue.setExportRevenue(exportRevenue);
        revenue.setExportProfit(exportProfit);
        
        return revenueRepo.save(revenue);
    }
} 