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
@RequestMapping("/api/import-transactions")
@CrossOrigin(origins = "http://localhost:5173")
public class ImportTransactionController {
    
    @Autowired
    private ImportTransactionService importTransactionService;
    
    @GetMapping("/all")
    public ResponseEntity<List<ImportTransaction>> getAllImportTransactions() {
        List<ImportTransaction> transactions = importTransactionService.getAllImportTransactions();
        return ResponseEntity.ok(transactions);
    }

    @PostMapping
    public ResponseEntity<ImportTransaction> createImportTransaction(@Valid @RequestBody ImportTransactionRequest request) {
        try {
            ImportTransaction transaction = importTransactionService.createImportTransaction(request);
            return ResponseEntity.ok(transaction);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }
    
    @GetMapping("/product/{productId}")
    public ResponseEntity<Page<ImportTransaction>> getImportTransactionsByProduct(
            @PathVariable Long productId, 
            Pageable pageable) {
        try {
            Page<ImportTransaction> transactions = importTransactionService.getImportTransactionsByProduct(productId, pageable);
            return ResponseEntity.ok(transactions);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }
    
    @GetMapping("/product/{productId}/all")
    public ResponseEntity<List<ImportTransaction>> getAllImportTransactionsByProduct(@PathVariable Long productId) {
        try {
            List<ImportTransaction> transactions = importTransactionService.getImportTransactionsByProduct(productId);
            return ResponseEntity.ok(transactions);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }
    
    @GetMapping("/product/{productId}/total-cost")
    public ResponseEntity<BigDecimal> getTotalCostByProduct(@PathVariable Long productId) {
        try {
            BigDecimal totalCost = importTransactionService.getTotalCostByProduct(productId);
            return ResponseEntity.ok(totalCost);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }
    
    @GetMapping("/product/{productId}/total-quantity")
    public ResponseEntity<Integer> getTotalQuantityByProduct(@PathVariable Long productId) {
        try {
            Integer totalQuantity = importTransactionService.getTotalQuantityByProduct(productId);
            return ResponseEntity.ok(totalQuantity);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }
    
    @GetMapping("/date-range")
    public ResponseEntity<List<ImportTransaction>> getImportTransactionsByDateRange(
            @RequestParam LocalDateTime startDate,
            @RequestParam LocalDateTime endDate) {
        List<ImportTransaction> transactions = importTransactionService.getImportTransactionsByDateRange(startDate, endDate);
        return ResponseEntity.ok(transactions);
    }
    
    @GetMapping("/date-range/total-cost")
    public ResponseEntity<BigDecimal> getTotalCostByDateRange(
            @RequestParam LocalDateTime startDate,
            @RequestParam LocalDateTime endDate) {
        BigDecimal totalCost = importTransactionService.getTotalCostByDateRange(startDate, endDate);
        return ResponseEntity.ok(totalCost);
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<ImportTransaction> getImportTransactionById(@PathVariable Long id) {
        return importTransactionService.getImportTransactionById(id)
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }
} 