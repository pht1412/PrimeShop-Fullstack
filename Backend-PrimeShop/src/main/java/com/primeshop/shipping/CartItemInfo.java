package com.primeshop.shipping;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class CartItemInfo {
    private Long productId;
    private String productName;
    private Integer quantity;
    private BigDecimal price;
    private BigDecimal totalPrice;
} 