package com.primeshop.product;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class ProductSpecRequest {
    private String name;
    private String value;
}
