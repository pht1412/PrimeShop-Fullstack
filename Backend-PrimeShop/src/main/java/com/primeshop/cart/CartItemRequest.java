package com.primeshop.cart;

import lombok.Data;

@Data
public class CartItemRequest {
    private String productSlug;
    private Integer quantity;
}
