package com.primeshop.stock;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.primeshop.product.Product;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "export_transaction")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ExportTransaction {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @Column(name = "quantity", nullable = false)
    private Integer quantity;

    @Column(name = "unit_price", precision = 19, scale = 2, nullable = false)
    private BigDecimal unitPrice;

    @Column(name = "total_revenue", precision = 19, scale = 2, nullable = false)
    private BigDecimal totalRevenue;

    @Column(name = "unit_cost", precision = 19, scale = 2, nullable = false)
    private BigDecimal unitCost;

    @Column(name = "total_cost", precision = 19, scale = 2, nullable = false)
    private BigDecimal totalCost;

    @Column(name = "profit", precision = 19, scale = 2, nullable = false)
    private BigDecimal profit;

    @Column(name = "profit_margin", precision = 5, scale = 2)
    private BigDecimal profitMargin;

    @Column(name = "customer_name", length = 255)
    private String customerName;

    @Column(name = "invoice_number", length = 100)
    private String invoiceNumber;

    @Column(name = "notes", columnDefinition = "text")
    private String notes;

    @Column(name = "export_date", nullable = false)
    private LocalDateTime exportDate;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    public void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        if (this.exportDate == null) {
            this.exportDate = LocalDateTime.now();
        }
        calculateTotals();
    }
    
    @PreUpdate
    public void onUpdate() {
        this.updatedAt = LocalDateTime.now();
        calculateTotals();
    }

    private void calculateTotals() {
        if (this.quantity != null && this.unitPrice != null) {
            this.totalRevenue = this.unitPrice.multiply(BigDecimal.valueOf(this.quantity));
        }
        
        if (this.quantity != null && this.unitCost != null) {
            this.totalCost = this.unitCost.multiply(BigDecimal.valueOf(this.quantity));
        }
        
        if (this.totalRevenue != null && this.totalCost != null) {
            this.profit = this.totalRevenue.subtract(this.totalCost);
            
            if (this.totalRevenue.compareTo(BigDecimal.ZERO) > 0) {
                this.profitMargin = this.profit.divide(this.totalRevenue, 4, BigDecimal.ROUND_HALF_UP)
                    .multiply(BigDecimal.valueOf(100));
            }
        }
    }
} 