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
@Table(name = "import_transaction")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ImportTransaction {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @Column(name = "quantity", nullable = false)
    private Integer quantity;

    @Column(name = "unit_cost", precision = 19, scale = 2, nullable = false)
    private BigDecimal unitCost;

    @Column(name = "total_cost", precision = 19, scale = 2, nullable = false)
    private BigDecimal totalCost;

    @Column(name = "supplier_name", length = 255)
    private String supplierName;

    @Column(name = "invoice_number", length = 100)
    private String invoiceNumber;

    @Column(name = "notes", columnDefinition = "text")
    private String notes;

    @Column(name = "import_date", nullable = false)
    private LocalDateTime importDate;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    public void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        if (this.importDate == null) {
            this.importDate = LocalDateTime.now();
        }
        if (this.totalCost == null && this.unitCost != null && this.quantity != null) {
            this.totalCost = this.unitCost.multiply(BigDecimal.valueOf(this.quantity));
        }
    }
    
    @PreUpdate
    public void onUpdate() {
        this.updatedAt = LocalDateTime.now();
        if (this.totalCost == null && this.unitCost != null && this.quantity != null) {
            this.totalCost = this.unitCost.multiply(BigDecimal.valueOf(this.quantity));
        }
    }
} 