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
public class ExportTransactionService {
    
    @Autowired
    private ExportTransactionRepo exportTransactionRepo;
    
    @Autowired
    private ProductRepo productRepo;
    
    // @Autowired
    // private BusinessRepo businessRepo;
    
    public ExportTransaction createExportTransaction(ExportTransactionRequest request) {
        Product product = productRepo.findById(request.getProductId())
            .orElseThrow(() -> new RuntimeException("Sản phẩm không tồn tại"));
        
        // Kiểm tra xem sản phẩm có thuộc business ID = 1 (hàng tự sản xuất) không
        // if (product.getBusiness() == null || product.getBusiness().getId() != 1L) {
        //     throw new RuntimeException("Chỉ có thể xuất hàng cho sản phẩm tự sản xuất (business_id = 1)");
        // }
        
        // Kiểm tra stock
        if (product.getStock() < request.getQuantity()) {
            throw new RuntimeException("Số lượng xuất vượt quá tồn kho");
        }
        
        ExportTransaction exportTransaction = new ExportTransaction();
        exportTransaction.setProduct(product);
        exportTransaction.setQuantity(request.getQuantity());
        exportTransaction.setUnitPrice(request.getUnitPrice());
        exportTransaction.setUnitCost(request.getUnitCost());
        exportTransaction.setCustomerName(request.getCustomerName());
        exportTransaction.setInvoiceNumber(request.getInvoiceNumber());
        exportTransaction.setNotes(request.getNotes());
        exportTransaction.setExportDate(request.getExportDate() != null ? request.getExportDate() : LocalDateTime.now());
        
        ExportTransaction savedTransaction = exportTransactionRepo.save(exportTransaction);
        
        // Cập nhật stock và sold của sản phẩm
        product.setStock(product.getStock() - request.getQuantity());
        product.setSold(product.getSold() + request.getQuantity());
        productRepo.save(product);
        
        return savedTransaction;
    }
    
    public Page<ExportTransaction> getExportTransactionsByProduct(Long productId, Pageable pageable) {
        Product product = productRepo.findById(productId)
            .orElseThrow(() -> new RuntimeException("Sản phẩm không tồn tại"));
        
        return exportTransactionRepo.findByProduct(product, pageable);
    }
    
    public List<ExportTransaction> getExportTransactionsByProduct(Long productId) {
        Product product = productRepo.findById(productId)
            .orElseThrow(() -> new RuntimeException("Sản phẩm không tồn tại"));
        
        return exportTransactionRepo.findByProductOrderByExportDateDesc(product);
    }
    
    public BigDecimal getTotalRevenueByProduct(Long productId) {
        Product product = productRepo.findById(productId)
            .orElseThrow(() -> new RuntimeException("Sản phẩm không tồn tại"));
        
        BigDecimal totalRevenue = exportTransactionRepo.getTotalRevenueByProduct(product);
        return totalRevenue != null ? totalRevenue : BigDecimal.ZERO;
    }
    
    public BigDecimal getTotalProfitByProduct(Long productId) {
        Product product = productRepo.findById(productId)
            .orElseThrow(() -> new RuntimeException("Sản phẩm không tồn tại"));
        
        BigDecimal totalProfit = exportTransactionRepo.getTotalProfitByProduct(product);
        return totalProfit != null ? totalProfit : BigDecimal.ZERO;
    }
    
    public Integer getTotalQuantityByProduct(Long productId) {
        Product product = productRepo.findById(productId)
            .orElseThrow(() -> new RuntimeException("Sản phẩm không tồn tại"));
        
        Integer totalQuantity = exportTransactionRepo.getTotalQuantityByProduct(product);
        return totalQuantity != null ? totalQuantity : 0;
    }
    
    public List<ExportTransaction> getExportTransactionsByDateRange(LocalDateTime startDate, LocalDateTime endDate) {
        return exportTransactionRepo.findByExportDateBetween(startDate, endDate);
    }
    
    public BigDecimal getTotalRevenueByDateRange(LocalDateTime startDate, LocalDateTime endDate) {
        BigDecimal totalRevenue = exportTransactionRepo.getTotalRevenueByDateRange(startDate, endDate);
        return totalRevenue != null ? totalRevenue : BigDecimal.ZERO;
    }
    
    public BigDecimal getTotalProfitByDateRange(LocalDateTime startDate, LocalDateTime endDate) {
        BigDecimal totalProfit = exportTransactionRepo.getTotalProfitByDateRange(startDate, endDate);
        return totalProfit != null ? totalProfit : BigDecimal.ZERO;
    }
    
    public Optional<ExportTransaction> getExportTransactionById(Long id) {
        return exportTransactionRepo.findById(id);
    }
} 