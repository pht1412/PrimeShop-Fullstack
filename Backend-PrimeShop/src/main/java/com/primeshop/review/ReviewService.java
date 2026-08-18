package com.primeshop.review;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import com.primeshop.product.Product;
import com.primeshop.product.ProductRepo;
import com.primeshop.product.ProductReview;
import com.primeshop.product.ProductReviewRepo;
import com.primeshop.product.ProductReviewRequest;
import com.primeshop.product.ProductReviewResponse;
import com.primeshop.user.User;
import com.primeshop.user.UserRepo;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ReviewService {
    @Autowired
    private ProductReviewRepo productReviewRepo;
    @Autowired
    private ProductRepo productRepo;
    @Autowired
    private UserRepo userRepo;

    public ProductReviewResponse addProductReview(ProductReviewRequest request) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepo.findByUsername(username)
            .orElseThrow(() -> new RuntimeException("Không tìm thấy tài khoản!"));
        Product product = productRepo.findBySlugAndActiveTrue(request.getProductSlug())
            .orElseThrow(() -> new RuntimeException("Không tìm thấy sản phẩm!"));
        ProductReview review = new ProductReview(request, product, user);
        productReviewRepo.save(review);
        return new ProductReviewResponse(review);
    }

    public List<ProductReviewResponse> getProductReviews(String productSlug) {
        Product product = productRepo.findBySlugAndActiveTrue(productSlug)
            .orElseThrow(() -> new RuntimeException("Không tìm thấy sản phẩm!"));
        Long productId = product.getId();
        List<ProductReview> reviews = productReviewRepo.findByProductId(productId);
        return reviews.stream()
            .map(ProductReviewResponse::new)
            .collect(Collectors.toList());
    }

    public Double getProductRating(String productSlug) {
        Product product = productRepo.findBySlugAndActiveTrue(productSlug)
            .orElseThrow(() -> new RuntimeException("Không tìm thấy sản phẩm!"));
        Long productId = product.getId();
        return productReviewRepo.findAverageRatingByProductId(productId);
    }
}
