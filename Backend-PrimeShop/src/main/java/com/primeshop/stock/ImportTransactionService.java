package com.primeshop.stock;

import com.primeshop.product.Product;
import com.primeshop.product.ProductRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class ImportTransactionService {
    
    @Autowired
    private ImportTransactionRepo importTransactionRepo;
    
    @Autowired
    private ProductRepo productRepo;
    
    // @Autowired
    // private BusinessRepo businessRepo;
    
    public ImportTransaction createImportTransaction(ImportTransactionRequest request) {
        Product product = productRepo.findById(request.getProductId())
            .orElseThrow(() -> new RuntimeException("Sản phẩm không tồn tại"));
        
        // Kiểm tra xem sản phẩm có thuộc business ID = 2 (hàng từ nguồn khác) không
        // if (product.getBusiness() == null || product.getBusiness().getId() != 2L) {
        //     throw new RuntimeException("Chỉ có thể nhập hàng cho sản phẩm từ nguồn khác (business_id = 2)");
        // }
        
        ImportTransaction importTransaction = new ImportTransaction();
        importTransaction.setProduct(product);
        importTransaction.setQuantity(request.getQuantity());
        importTransaction.setUnitCost(request.getUnitCost());
        importTransaction.setSupplierName(request.getSupplierName());
        importTransaction.setInvoiceNumber(request.getInvoiceNumber());
        importTransaction.setNotes(request.getNotes());
        importTransaction.setImportDate(request.getImportDate() != null ? request.getImportDate() : LocalDateTime.now());
        
        ImportTransaction savedTransaction = importTransactionRepo.save(importTransaction);
        
        // Cập nhật stock của sản phẩm
        product.setStock(product.getStock() + request.getQuantity());
        productRepo.save(product);
        
        return savedTransaction;
    }
    
    public Page<ImportTransaction> getImportTransactionsByProduct(Long productId, Pageable pageable) {
        Product product = productRepo.findById(productId)
            .orElseThrow(() -> new RuntimeException("Sản phẩm không tồn tại"));
        
        return importTransactionRepo.findByProduct(product, pageable);
    }
    
    public List<ImportTransaction> getImportTransactionsByProduct(Long productId) {
        Product product = productRepo.findById(productId)
            .orElseThrow(() -> new RuntimeException("Sản phẩm không tồn tại"));
        
        return importTransactionRepo.findByProductOrderByImportDateDesc(product);
    }
    
    public BigDecimal getTotalCostByProduct(Long productId) {
        Product product = productRepo.findById(productId)
            .orElseThrow(() -> new RuntimeException("Sản phẩm không tồn tại"));
        
        BigDecimal totalCost = importTransactionRepo.getTotalCostByProduct(product);
        return totalCost != null ? totalCost : BigDecimal.ZERO;
    }
    
    public Integer getTotalQuantityByProduct(Long productId) {
        Product product = productRepo.findById(productId)
            .orElseThrow(() -> new RuntimeException("Sản phẩm không tồn tại"));
        
        Integer totalQuantity = importTransactionRepo.getTotalQuantityByProduct(product);
        return totalQuantity != null ? totalQuantity : 0;
    }
    
    public List<ImportTransaction> getImportTransactionsByDateRange(LocalDateTime startDate, LocalDateTime endDate) {
        return importTransactionRepo.findByImportDateBetween(startDate, endDate);
    }
    
    public BigDecimal getTotalCostByDateRange(LocalDateTime startDate, LocalDateTime endDate) {
        BigDecimal totalCost = importTransactionRepo.getTotalCostByDateRange(startDate, endDate);
        return totalCost != null ? totalCost : BigDecimal.ZERO;
    }
    
    public Optional<ImportTransaction> getImportTransactionById(Long id) {
        return importTransactionRepo.findById(id);
    }

    public List<ImportTransaction> getAllImportTransactions() {
        return importTransactionRepo.findAll();
    }
} 