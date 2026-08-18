package com.primeshop.delivery;

import com.primeshop.order.OrderStatus;
import com.primeshop.order.OrderResponse;
import com.primeshop.delivery.UpdateStatusRequest;
import com.primeshop.delivery.TrackingResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import com.primeshop.delivery.DeliveryService;
import com.primeshop.delivery.OrderIdRequest;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/delivery")
@CrossOrigin(origins = "http://localhost:5173")
public class DeliveryController {
    
    @Autowired
    private DeliveryService deliveryService;

    /**
     * Lấy danh sách đơn hàng cho đơn vị vận chuyển
     * Chỉ ADMIN và DELIVERY_UNIT có thể truy cập
     */
    @GetMapping("/orders")
    public ResponseEntity<List<OrderResponse>> getOrdersForDelivery() {
        List<OrderResponse> orders = deliveryService.getOrdersForDeliveryUnit();
        return ResponseEntity.ok(orders);
    }

    /**
     * Cập nhật trạng thái đơn hàng
     * Chỉ ADMIN và DELIVERY_UNIT có thể truy cập
     */
    @PutMapping("/orders/{orderId}/status")
    public ResponseEntity<?> updateOrderStatus(
            @PathVariable Long orderId,
            @RequestBody UpdateStatusRequest request) {
        try {
            deliveryService.updateOrderStatus(orderId, request.getStatus(), request.getLocation());
            return ResponseEntity.ok(Map.of("message", "Order status updated successfully"));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Lấy lịch sử tracking của đơn hàng
     * Chỉ ADMIN và DELIVERY_UNIT có thể truy cập
     */
    @GetMapping("/orders/{orderId}/tracking")
    public ResponseEntity<List<TrackingResponse>> getTrackingHistory(@PathVariable Long orderId) {
    List<DeliveryTracking> trackingHistory = deliveryService.getTrackingHistory(orderId);
    List<TrackingResponse> response = trackingHistory.stream()
            .map(TrackingResponse::new)
            .toList();
    return ResponseEntity.ok(response);
}


    /**
     * Lấy thống kê giao vận
     * Chỉ ADMIN và DELIVERY_UNIT có thể truy cập
     */
    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getDeliveryStats() {
        Map<String, Object> stats = deliveryService.getDeliveryStats();
        return ResponseEntity.ok(stats);
    }

    /**
     * Lấy chi tiết đơn hàng
     * Chỉ ADMIN và DELIVERY_UNIT có thể truy cập
     */
    @GetMapping("/orders/{orderId}")
    public ResponseEntity<OrderResponse> getOrderDetail(@PathVariable Long orderId) {
        // Tìm đơn hàng trong danh sách delivery orders
        List<OrderResponse> deliveryOrders = deliveryService.getOrdersForDeliveryUnit();
        OrderResponse order = deliveryOrders.stream()
                .filter(o -> o.getOrderId().equals(orderId))
                .findFirst()
                .orElse(null);
        
        if (order == null) {
            return ResponseEntity.notFound().build();
        }
        
        return ResponseEntity.ok(order);
    }
    @PostMapping("/delivery/send")
    @PreAuthorize("hasRole('ADMIN') or hasRole('DELIVERY_UNIT')")
    public ResponseEntity<?> sendDeliveryOrder(@RequestBody OrderIdRequest request) {
        deliveryService.sendDeliveryOrder(request.getOrderId());
        return ResponseEntity.ok(Map.of("message", "Delivery order sent successfully"));
    }
}

