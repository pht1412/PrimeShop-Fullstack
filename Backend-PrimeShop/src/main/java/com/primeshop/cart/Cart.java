package com.primeshop.cart;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import com.primeshop.user.User;
import com.primeshop.voucher.Voucher;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Entity
@NoArgsConstructor
public class Cart {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToMany(mappedBy = "cart", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<CartItem> cartItems;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;   

    private BigDecimal totalPrice;

    @ManyToMany
    @JoinTable(
        name = "cart_voucher",
        joinColumns = @JoinColumn(name = "cart_id"),
        inverseJoinColumns = @JoinColumn(name = "voucher_id")
    )
    private List<Voucher> vouchers = new ArrayList<>();

    private BigDecimal discount = BigDecimal.ZERO;

    // Getter cho totalAmount (totalPrice)
    public BigDecimal getTotalAmount() {
        return this.totalPrice;
    }

    // Setter cho discountAmount (discount)
    public void setDiscountAmount(BigDecimal discountAmount) {
        this.discount = discountAmount;
    }

    // Setter/getter cho finalAmount (totalPrice - discount)
    private BigDecimal finalAmount;
    public void setFinalAmount(BigDecimal finalAmount) {
        this.finalAmount = finalAmount;
    }
    public BigDecimal getFinalAmount() {
        return this.finalAmount;
    }

    public Cart(User user) {
        this.user = user;
        this.totalPrice = BigDecimal.ZERO;
        this.cartItems = new ArrayList<>();
    }

    public void setTotalPrice() {
        if (this.cartItems == null) {
            this.cartItems = new ArrayList<>();
        }

        BigDecimal sum = this.cartItems.stream()
            .map(CartItem::getTotalPrice)
            .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalDiscount = BigDecimal.ZERO;
        if (vouchers != null && !vouchers.isEmpty()) {
            for (Voucher voucher : vouchers) {
                try {
                    boolean validMinOrder = voucher.getMinOrderValue() == null ||
                        sum.compareTo(BigDecimal.valueOf(voucher.getMinOrderValue())) >= 0;
                    if (validMinOrder) {
                        Double discountAmount = voucher.calculateDiscount(sum.doubleValue());
                        totalDiscount = totalDiscount.add(BigDecimal.valueOf(discountAmount));
                    }
                } catch (Exception e) {
                    System.err.println("Error calculating voucher discount: " + e.getMessage());
                }
            }
        }

        this.discount = totalDiscount;
        this.totalPrice = sum.subtract(totalDiscount).max(BigDecimal.ZERO);
    }

    // Add setter methods for direct field assignment
    public void setTotalPrice(BigDecimal totalPrice) {
        this.totalPrice = totalPrice;
    }

    public void setDiscount(BigDecimal discount) {
        this.discount = discount;
    }

    // Getter & Setter cho danh sách voucher
    public List<Voucher> getVouchers() {
        return vouchers;
    }
    public void setVouchers(List<Voucher> vouchers) {
        this.vouchers = vouchers;
    }
}
