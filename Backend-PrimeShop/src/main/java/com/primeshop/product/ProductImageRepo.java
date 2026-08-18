package com.primeshop.product;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductImageRepo extends JpaRepository<ProductImage, Long> {
    List<ProductImage> findByProduct(Product product);
}
