package com.primeshop.seller;

import java.util.Optional;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.primeshop.seller.SellerProfile.SellerStatus;

public interface SellerRepo extends JpaRepository<SellerProfile, Long> {
    Optional<SellerProfile> findByUserId(Long userId);
    List<SellerProfile> findByStatus(SellerStatus status);
}
