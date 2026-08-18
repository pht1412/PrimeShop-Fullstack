package com.primeshop.product;

import java.math.BigDecimal;
import java.util.List;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class ProductRequest {
    private String name;
    private String brand;
    private BigDecimal price;
    private BigDecimal discountPercent;
    private Double rating;
    private String imageUrl;
    private Integer stock;
    private Integer sold;
    private Long categoryId;
    private List<ProductSpecResponse> specs;
    private String description;
    // private Long businessId;
}


