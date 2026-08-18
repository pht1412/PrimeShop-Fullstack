package com.primeshop.product;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class ProductSpecResponse {
    private String name;
    private String value;

    public ProductSpecResponse(ProductSpec spec) {
        this.name = spec.getName();
        this.value = spec.getValue();
    }
}