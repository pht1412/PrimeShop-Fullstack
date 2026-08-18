package com.primeshop.cart;

import java.math.BigDecimal;

import lombok.Data;

@Data
public class CartItemResponse {
    private Long id;
    private String productName;
    private String productSlug;
    private Integer quantity;
    private BigDecimal price;
    private BigDecimal totalPrice;
    private String imageUrl;

    public CartItemResponse(CartItem cartItem) {
        this.id = cartItem.getId();
        this.productName = cartItem.getProduct().getName();
        this.quantity = cartItem.getQuantity();
        this.price = cartItem.getPrice();
        this.totalPrice = cartItem.getTotalPrice();
        this.imageUrl = cartItem.getProduct().getImageUrl();
        this.productSlug = cartItem.getProduct().getSlug();
    }
}
