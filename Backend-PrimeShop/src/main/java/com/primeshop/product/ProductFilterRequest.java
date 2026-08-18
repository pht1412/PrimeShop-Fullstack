package com.primeshop.product;

import java.math.BigDecimal;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class ProductFilterRequest {
    private Long sellerId;
    private Boolean sellerView;
    private String search;
    private String category;
    private String brand;
    private BigDecimal minPrice;
    private BigDecimal maxPrice;
}
