package com.primeshop.voucher;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import com.primeshop.cart.Cart;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "vouchers")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Voucher {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String code;

    @Enumerated(EnumType.STRING)
    @Column(name = "discount_type", nullable = false)
    private DiscountType discountType; // PERCENT, FIXED

    @Column(name = "discount_value", nullable = false)
    private Double discountValue;

    @Column(name = "min_order_value")
    private Double minOrderValue;

    @Column(name = "start_date")
    private LocalDateTime startDate;

    @Column(name = "end_date")
    private LocalDateTime endDate;

    @Column(name = "max_usage")
    private Integer maxUsage; // null nếu không giới hạn

    @Column(name = "used_count")
    private Integer currentUsage = 0;

    @Column(name = "is_active")
    private Boolean isActive = true;

    private Long userId;

    @CreationTimestamp
    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @ManyToMany(mappedBy = "vouchers")
    private List<Cart> carts = new ArrayList<>();

    public enum DiscountType { PERCENT, FIXED, FREESHIP; }

    public Double calculateDiscount(Double orderValue) {
        if (!Boolean.TRUE.equals(isActive) || orderValue == null || orderValue <= 0) {
            return 0.0;
        }
        if (minOrderValue != null && orderValue < minOrderValue) {
            return 0.0;
        }
        if (maxUsage != null && currentUsage != null && currentUsage >= maxUsage) {
            return 0.0;
        }
        if (endDate != null && LocalDateTime.now().isAfter(endDate)) {
            return 0.0;
        }
        Double discountAmount = 0.0;
        if (discountType == DiscountType.PERCENT) {
            discountAmount = orderValue * (discountValue / 100.0);
        } else if (discountType == DiscountType.FIXED) {
            discountAmount = Math.min(discountValue, orderValue);
        } else if (discountType == DiscountType.FREESHIP) {
            discountAmount = 0.0;
        }
        return discountAmount;
    }

    public boolean isValid() {
        if (!Boolean.TRUE.equals(isActive)) {
            return false;
        }
        LocalDateTime now = LocalDateTime.now();
        if (startDate != null && now.isBefore(startDate)) {
            return false;
        }
        if (endDate != null && now.isAfter(endDate)) {
            return false;
        }
        if (maxUsage != null && currentUsage != null && currentUsage >= maxUsage) {
            return false;
        }
        return true;
    }

    public boolean hasRemainingUsage() {
        if (maxUsage == null) {
            return true;
        }
        return currentUsage == null || currentUsage < maxUsage;
    }

    public Integer getRemainingUsage() {
        if (maxUsage == null) {
            return null;
        }
        if (currentUsage == null) {
            return maxUsage;
        }
        return Math.max(0, maxUsage - currentUsage);
    }

    public boolean canApplyToOrder(Double orderValue) {
        if (!isValid()) {
            return false;
        }
        if (minOrderValue != null && orderValue < minOrderValue) {
            return false;
        }
        return true;
    }

    public void incrementUsage() {
        if (currentUsage == null) {
            currentUsage = 0;
        }
        currentUsage++;
    }

    public LocalDateTime getExpirationDate() {
        return this.endDate;
    }

    public Double getMaxDiscountValue() {
        return null; // Nếu có trường maxDiscountValue thì thay thế dòng này
    }
}