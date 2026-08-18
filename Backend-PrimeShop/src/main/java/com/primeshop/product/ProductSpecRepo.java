package com.primeshop.product;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ProductSpecRepo extends JpaRepository<ProductSpec, Long> {
    @Modifying
    @Query("DELETE FROM ProductSpec p WHERE p.product = :product")
    void deleteByProduct(@Param("product") Product product);
}
