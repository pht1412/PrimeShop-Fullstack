package com.primeshop.cart;

import java.math.BigDecimal;

import com.primeshop.product.Product;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Entity
@NoArgsConstructor
public class CartItem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cart_id", nullable = false)
    private Cart cart;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_slug", nullable = false)
    private Product product;

    private Integer quantity;
    private BigDecimal price;
    private BigDecimal totalPrice;
    
    public CartItem(Cart cart, Product product, Integer quantity) {
        this.cart = cart;
        this.product = product;
        this.quantity = quantity;
        this.price = product.getDiscountPrice();
        this.totalPrice = product.getDiscountPrice().multiply(BigDecimal.valueOf(quantity));
    }

    public void updateQuantity(Integer quantity) {
        this.quantity = quantity;
        this.totalPrice = product.getDiscountPrice().multiply(BigDecimal.valueOf(quantity));
    }
}
