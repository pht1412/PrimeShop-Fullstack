package com.primeshop.delivery;

import org.springframework.data.jpa.repository.JpaRepository;

import com.primeshop.delivery.DeliveryTracking;

import java.util.List;

public interface DeliveryTrackingRepository extends JpaRepository<DeliveryTracking, Long> {
    List<DeliveryTracking> findByOrderId(Long orderId);
} 