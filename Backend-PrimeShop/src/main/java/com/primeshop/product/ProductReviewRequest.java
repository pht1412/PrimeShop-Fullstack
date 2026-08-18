package com.primeshop.product;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class ProductReviewRequest {
    private String productSlug;
    private Integer rating;
    private String content;   

    public ProductReviewRequest(String productSlug,Integer rating, String content) {
        this.productSlug = productSlug;
        this.rating = rating;
        this.content = content;
    }
}

