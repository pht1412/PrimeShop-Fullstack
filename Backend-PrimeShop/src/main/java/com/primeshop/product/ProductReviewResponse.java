package com.primeshop.product;

import java.time.LocalDateTime;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class ProductReviewResponse {
    private Long id;
    private String content;
    private Integer rating;
    private String username;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Boolean isApproved;
    private Boolean isDeleted;

    public ProductReviewResponse(ProductReview review) {
        this.id = review.getId();
        this.content = review.getContent();
        this.rating = review.getRating();
        this.username = review.getUser().getFullName();
        this.createdAt = review.getCreatedAt();
    }
}