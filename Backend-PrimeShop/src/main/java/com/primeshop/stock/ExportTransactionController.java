package com.primeshop.stock;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/export-transactions")
@CrossOrigin(origins = "http://localhost:5173")
public class ExportTransactionController {
    
    @Autowired
    private ExportTransactionService exportTransactionService;
    
    @PostMapping
    public ResponseEntity<ExportTransaction> createExportTransaction(@Valid @RequestBody ExportTransactionRequest request) {
        try {
            ExportTransaction transaction = exportTransactionService.createExportTransaction(request);
            return ResponseEntity.ok(transaction);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }
    
    @GetMapping("/product/{productId}")
    public ResponseEntity<Page<ExportTransaction>> getExportTransactionsByProduct(
            @PathVariable Long productId, 
            Pageable pageable) {
        try {
            Page<ExportTransaction> transactions = exportTransactionService.getExportTransactionsByProduct(productId, pageable);
            return ResponseEntity.ok(transactions);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }
    
    @GetMapping("/product/{productId}/all")
    public ResponseEntity<List<ExportTransaction>> getAllExportTransactionsByProduct(@PathVariable Long productId) {
        try {
            List<ExportTransaction> transactions = exportTransactionService.getExportTransactionsByProduct(productId);
            return ResponseEntity.ok(transactions);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }
    
    @GetMapping("/product/{productId}/total-revenue")
    public ResponseEntity<BigDecimal> getTotalRevenueByProduct(@PathVariable Long productId) {
        try {
            BigDecimal totalRevenue = exportTransactionService.getTotalRevenueByProduct(productId);
            return ResponseEntity.ok(totalRevenue);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }
    
    @GetMapping("/product/{productId}/total-profit")
    public ResponseEntity<BigDecimal> getTotalProfitByProduct(@PathVariable Long productId) {
        try {
            BigDecimal totalProfit = exportTransactionService.getTotalProfitByProduct(productId);
            return ResponseEntity.ok(totalProfit);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }
    
    @GetMapping("/product/{productId}/total-quantity")
    public ResponseEntity<Integer> getTotalQuantityByProduct(@PathVariable Long productId) {
        try {
            Integer totalQuantity = exportTransactionService.getTotalQuantityByProduct(productId);
            return ResponseEntity.ok(totalQuantity);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }
    
    @GetMapping("/date-range")
    public ResponseEntity<List<ExportTransaction>> getExportTransactionsByDateRange(
            @RequestParam LocalDateTime startDate,
            @RequestParam LocalDateTime endDate) {
        List<ExportTransaction> transactions = exportTransactionService.getExportTransactionsByDateRange(startDate, endDate);
        return ResponseEntity.ok(transactions);
    }
    
    @GetMapping("/date-range/total-revenue")
    public ResponseEntity<BigDecimal> getTotalRevenueByDateRange(
            @RequestParam LocalDateTime startDate,
            @RequestParam LocalDateTime endDate) {
        BigDecimal totalRevenue = exportTransactionService.getTotalRevenueByDateRange(startDate, endDate);
        return ResponseEntity.ok(totalRevenue);
    }
    
    @GetMapping("/date-range/total-profit")
    public ResponseEntity<BigDecimal> getTotalProfitByDateRange(
            @RequestParam LocalDateTime startDate,
            @RequestParam LocalDateTime endDate) {
        BigDecimal totalProfit = exportTransactionService.getTotalProfitByDateRange(startDate, endDate);
        return ResponseEntity.ok(totalProfit);
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<ExportTransaction> getExportTransactionById(@PathVariable Long id) {
        return exportTransactionService.getExportTransactionById(id)
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }
} 