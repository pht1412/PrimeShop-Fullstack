package com.primeshop.review;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.primeshop.product.ProductReviewRequest;
import com.primeshop.product.ProductReviewResponse;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/review")
@CrossOrigin(origins = "http://localhost:5173")
@RequiredArgsConstructor
public class ReviewController {
    @Autowired
    private ReviewService reviewService;

    @PostMapping
    public ResponseEntity<ProductReviewResponse> addProductReview(@RequestBody ProductReviewRequest request) {
        return ResponseEntity.ok(reviewService.addProductReview(request));
    }

    @GetMapping
    public ResponseEntity<List<ProductReviewResponse>> getProductReviews(@RequestParam String productSlug) {
        return ResponseEntity.ok(reviewService.getProductReviews(productSlug));
    }

    @GetMapping("/rating")
    public ResponseEntity<Double> getProductRating(@RequestParam String productSlug) {
        return ResponseEntity.ok(reviewService.getProductRating(productSlug));
    }
}
