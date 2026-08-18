package com.primeshop.order;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class OrderItemRequest {
    private String productSlug;
    private Integer quantity;
}
