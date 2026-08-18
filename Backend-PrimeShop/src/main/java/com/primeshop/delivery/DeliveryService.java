package com.primeshop.delivery;

import com.primeshop.order.Order;
import com.primeshop.order.OrderRepo;
import com.primeshop.order.OrderStatus;
import com.primeshop.delivery.DeliveryTrackingRepository;
import com.primeshop.order.OrderResponse;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.stream.Collectors;

@Service
public class DeliveryService {
    
    @Autowired
    private OrderRepo orderRepo;
    
    @Autowired
    private DeliveryTrackingRepository trackingRepo;

    /**
     * Lấy danh sách đơn hàng cho đơn vị vận chuyển
     * Chỉ lấy các đơn hàng có trạng thái liên quan đến giao vận
     */
    public List<OrderResponse> getOrdersForDeliveryUnit() {
        // Lấy tất cả đơn hàng đang ở trạng thái liên quan đến giao vận
        List<OrderStatus> deliveryStatuses = List.of(
            OrderStatus.INVENTORY,
            OrderStatus.READY_TO_SHIP, 
            OrderStatus.SHIPPED,
            OrderStatus.DELIVERED,
            OrderStatus.FAILED_DELIVERY
        );
        
        List<Order> orders = orderRepo.findByStatusIn(deliveryStatuses);
        return orders.stream()
                .map(OrderResponse::new)
                .collect(Collectors.toList());
    }

    /**
     * Cập nhật trạng thái đơn hàng và tạo tracking record
     */
    @Transactional
    public void updateOrderStatus(Long orderId, OrderStatus newStatus, String location) {
        // Tìm đơn hàng
        Order order = orderRepo.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found with ID: " + orderId));
        
        // Cập nhật trạng thái đơn hàng
        order.setStatus(newStatus);
        orderRepo.save(order);

        // Tạo tracking record mới
        DeliveryTracking tracking = new DeliveryTracking();
        tracking.setOrder(order);
        tracking.setStatus(newStatus);
        tracking.setTimestamp(LocalDateTime.now());
        tracking.setLocation(location != null ? location : "Unknown location");
        trackingRepo.save(tracking);
    }

    /**
     * Lấy lịch sử tracking của đơn hàng
     */
    public List<DeliveryTracking> getTrackingHistory(Long orderId) {
        return trackingRepo.findByOrderId(orderId);
    }

    /**
     * Lấy thống kê giao vận
     */
    public Map<String, Object> getDeliveryStats() {
        List<Order> allOrders = orderRepo.findAll();
        
        long total = allOrders.size();
        long readyToShip = allOrders.stream()
                .filter(order -> order.getStatus() == OrderStatus.READY_TO_SHIP)
                .count();
        long shipped = allOrders.stream()
                .filter(order -> order.getStatus() == OrderStatus.SHIPPED)
                .count();
        long delivered = allOrders.stream()
                .filter(order -> order.getStatus() == OrderStatus.DELIVERED)
                .count();
        long failed = allOrders.stream()
                .filter(order -> order.getStatus() == OrderStatus.FAILED_DELIVERY)
                .count();
        long inventory = allOrders.stream()
                .filter(order -> order.getStatus() == OrderStatus.INVENTORY)
                .count();
        
        Map<String, Object> stats = new HashMap<>();
        stats.put("total", total);
        stats.put("inventory", inventory);
        stats.put("readyToShip", readyToShip);
        stats.put("shipped", shipped);
        stats.put("delivered", delivered);
        stats.put("failed", failed);
        
        return stats;
    }

    /**
     * Lấy đơn hàng theo trạng thái
     */
    public List<OrderResponse> getOrdersByStatus(OrderStatus status) {
        List<Order> orders = orderRepo.findByStatus(status);
        return orders.stream()
                .map(OrderResponse::new)
                .collect(Collectors.toList());
    }

    /**
     * Tìm kiếm đơn hàng theo từ khóa (tên khách hàng)
     */
    public List<OrderResponse> searchOrders(String keyword) {
        // Lấy tất cả đơn hàng và filter theo keyword
        List<Order> allOrders = orderRepo.findAll();
        return allOrders.stream()
                .filter(order -> 
                    order.getFullName() != null && 
                    order.getFullName().toLowerCase().contains(keyword.toLowerCase()) ||
                    order.getId().toString().contains(keyword)
                )
                .map(OrderResponse::new)
                .collect(Collectors.toList());
    }

    /**
     * Lấy đơn hàng cần giao hôm nay
     */
    public List<OrderResponse> getTodayDeliveryOrders() {
        LocalDate today = LocalDate.now();
        
        // Lấy tất cả đơn hàng và filter theo ngày giao dự kiến
        List<Order> allOrders = orderRepo.findAll();
        return allOrders.stream()
                .filter(order -> 
                    order.getEstimatedDeliveryDate() != null &&
                    order.getEstimatedDeliveryDate().equals(today) &&
                    (order.getStatus() == OrderStatus.READY_TO_SHIP || 
                     order.getStatus() == OrderStatus.SHIPPED)
                )
                .map(OrderResponse::new)
                .collect(Collectors.toList());
    }

    /**
     * Lấy đơn hàng bị delay (quá hạn giao)
     */

    
    public List<OrderResponse> getDelayedOrders() {
        LocalDate today = LocalDate.now();
        LocalDateTime todayStart = today.atStartOfDay();

        // Lấy tất cả đơn hàng và filter theo ngày giao dự kiến
        List<Order> allOrders = orderRepo.findAll();
        return allOrders.stream()
                .filter(order -> 
                    order.getEstimatedDeliveryDate() != null &&
                    order.getEstimatedDeliveryDate().isBefore(todayStart) &&
                    (order.getStatus() == OrderStatus.READY_TO_SHIP || 
                     order.getStatus() == OrderStatus.SHIPPED)
                )
                .map(OrderResponse::new)
                .collect(Collectors.toList());
    }

    /**
     * Cập nhật ngày giao hàng dự kiến
     */
    @Transactional
    public void updateEstimatedDeliveryDate(Long orderId, LocalDate newEstimatedDate) {
        Order order = orderRepo.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found with ID: " + orderId));
        
        LocalDateTime estimatedDateTime = newEstimatedDate.atStartOfDay();
        order.setEstimatedDeliveryDate(estimatedDateTime);
        orderRepo.save(order);
    }

    /**
     * Lấy thống kê theo ngày
     */
    public Map<String, Object> getDailyStats() {
        LocalDate today = LocalDate.now();
        
        // Lấy tất cả đơn hàng và filter theo ngày tạo
        List<Order> allOrders = orderRepo.findAll();
        List<Order> todayOrders = allOrders.stream()
                .filter(order -> 
                    order.getCreatedAt() != null &&
                    order.getCreatedAt().toLocalDate().equals(today)
                )
                .collect(Collectors.toList());
        
        long deliveredToday = todayOrders.stream()
                .filter(order -> order.getStatus() == OrderStatus.DELIVERED)
                .count();
        
        long failedToday = todayOrders.stream()
                .filter(order -> order.getStatus() == OrderStatus.FAILED_DELIVERY)
                .count();
        
        Map<String, Object> dailyStats = new HashMap<>();
        dailyStats.put("totalOrdersToday", todayOrders.size());
        dailyStats.put("deliveredToday", deliveredToday);
        dailyStats.put("failedToday", failedToday);
        dailyStats.put("successRate", todayOrders.size() > 0 ? 
            (double) deliveredToday / todayOrders.size() * 100 : 0);
        
        return dailyStats;
    }

    /*Kiểm tra đơn hàng đạt trạng thái PAID và chuyển sang đơn vị vận chuyển */
    public void sendDeliveryOrder(Long orderId) {
        // 1. Lấy đơn hàng
        Order order = orderRepo.findById(orderId)
            .orElseThrow(() -> new RuntimeException("Order not found"));

        // 2. Kiểm tra trạng thái PAID
        if (order.getStatus() != OrderStatus.PAID) {
            throw new RuntimeException("Order must be in PAID status to send to delivery");
        }

        // 3. Tạo bản ghi vận chuyển (Delivery) nếu cần
        // Delivery delivery = new Delivery(order, ...);
        // deliveryRepo.save(delivery);

        // 4. Cập nhật trạng thái đơn hàng sang SHIPPED
        order.setStatus(OrderStatus.PROCESSING);
        orderRepo.save(order);

        // 5. (Tùy chọn) Ghi log, gửi notification, v.v.
    }
} 