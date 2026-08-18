package com.primeshop.stock;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/revenue")
@CrossOrigin(origins = "http://localhost:5173")
public class RevenueController {
    
    @Autowired
    private RevenueService revenueService;
    
    @GetMapping("/period/{period}")
    public ResponseEntity<Revenue> getRevenueByPeriod(@PathVariable String period) {
        Revenue revenue = revenueService.getRevenueByPeriod(period);
        return revenue != null ? ResponseEntity.ok(revenue) : ResponseEntity.notFound().build();
    }
    
    @GetMapping("/year/{year}")
    public ResponseEntity<List<Revenue>> getRevenueByYear(@PathVariable String year) {
        List<Revenue> revenues = revenueService.getRevenueByYear(year);
        return ResponseEntity.ok(revenues);
    }
    
    @GetMapping("/all")
    public ResponseEntity<List<Revenue>> getAllRevenue() {
        List<Revenue> revenues = revenueService.getAllRevenueOrderByPeriod();
        return ResponseEntity.ok(revenues);
    }
    
    @GetMapping("/year/{year}/total-revenue")
    public ResponseEntity<BigDecimal> getTotalRevenueByYear(@PathVariable String year) {
        BigDecimal totalRevenue = revenueService.getTotalRevenueByYear(year);
        return ResponseEntity.ok(totalRevenue);
    }
    
    @GetMapping("/year/{year}/total-profit")
    public ResponseEntity<BigDecimal> getTotalProfitByYear(@PathVariable String year) {
        BigDecimal totalProfit = revenueService.getTotalProfitByYear(year);
        return ResponseEntity.ok(totalProfit);
    }
    
    @PostMapping("/calculate/{period}")
    public ResponseEntity<Revenue> calculateMonthlyRevenue(@PathVariable String period) {
        Revenue revenue = revenueService.calculateMonthlyRevenue(period);
        return ResponseEntity.ok(revenue);
    }
} 