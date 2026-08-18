package com.primeshop.stock;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "revenue")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Revenue {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "period", nullable = false, length = 10)
    private String period; // Format: YYYY-MM

    @Column(name = "order_revenue", precision = 19, scale = 2, nullable = false)
    private BigDecimal orderRevenue = BigDecimal.ZERO;

    @Column(name = "order_profit", precision = 19, scale = 2, nullable = false)
    private BigDecimal orderProfit = BigDecimal.ZERO;

    @Column(name = "export_revenue", precision = 19, scale = 2, nullable = false)
    private BigDecimal exportRevenue = BigDecimal.ZERO;

    @Column(name = "export_profit", precision = 19, scale = 2, nullable = false)
    private BigDecimal exportProfit = BigDecimal.ZERO;

    @Column(name = "total_revenue", precision = 19, scale = 2, nullable = false)
    private BigDecimal totalRevenue = BigDecimal.ZERO;

    @Column(name = "total_profit", precision = 19, scale = 2, nullable = false)
    private BigDecimal totalProfit = BigDecimal.ZERO;

    @Column(name = "profit_margin", precision = 5, scale = 2)
    private BigDecimal profitMargin = BigDecimal.ZERO;

    @Column(name = "order_count", nullable = false)
    private Integer orderCount = 0;

    @Column(name = "export_count", nullable = false)
    private Integer exportCount = 0;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    public void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        calculateTotals();
    }
    
    @PreUpdate
    public void onUpdate() {
        this.updatedAt = LocalDateTime.now();
        calculateTotals();
    }

    @SuppressWarnings("deprecation")
    private void calculateTotals() {
        // Tính tổng doanh thu
        this.totalRevenue = this.orderRevenue.add(this.exportRevenue);
        
        // Tính tổng lợi nhuận
        this.totalProfit = this.orderProfit.add(this.exportProfit);
        
        // Tính profit margin
        if (this.totalRevenue.compareTo(BigDecimal.ZERO) > 0) {
            this.profitMargin = this.totalProfit.divide(this.totalRevenue, 4, BigDecimal.ROUND_HALF_UP)
                .multiply(BigDecimal.valueOf(100));
        }
    }

    public void addOrderRevenue(BigDecimal revenue, BigDecimal profit) {
        this.orderRevenue = this.orderRevenue.add(revenue);
        this.orderProfit = this.orderProfit.add(profit);
        this.orderCount++;
        calculateTotals();
    }

    public void addExportRevenue(BigDecimal revenue, BigDecimal profit) {
        this.exportRevenue = this.exportRevenue.add(revenue);
        this.exportProfit = this.exportProfit.add(profit);
        this.exportCount++;
        calculateTotals();
    }
} 