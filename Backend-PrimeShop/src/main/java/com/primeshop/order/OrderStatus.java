package com.primeshop.order;

import java.util.List;

public enum OrderStatus {
    PENDING,       // Đang chờ xác nhận
    CONFIRMED,     // Đã xác nhận, sẵn sàng thanh toán
    PAID,          // Thanh toán thành công
    PROCESSING,    // Đang xử lý
    SHIPPED,       // Đã bàn giao cho đơn vị vận chuyển
    DELIVERED,     // Giao hàng thành công
    DELIVERED_FAILED, // Trạng thái cũ, giữ để đọc dữ liệu đã lưu
    PAYMENT_FAILED, // Thanh toán thất bại
    INVENTORY,     // Đang kiểm kho
    READY_TO_SHIP, // Sẵn sàng để giao hàng
    SHIPPING,    // Trạng thái cũ, giữ để đọc dữ liệu đã lưu
    FAILED_DELIVERY, // Giao hàng thất bại, có thể giao lại hoặc hoàn hàng
    RETURNED,     // Đã trả hàng
    REFUNDED,      // Đã hoàn tiền
    CANCELLED,      // Đã hủy
    COMPLETED;     // Khách hàng xác nhận đã nhận hàng

    public List<OrderStatus> getAllowedTransitions() {
        return switch (this) {
            case PENDING -> List.of(CONFIRMED, PAID, PAYMENT_FAILED, CANCELLED);
            case CONFIRMED -> List.of(PAID, PAYMENT_FAILED, CANCELLED);
            case PAYMENT_FAILED -> List.of(CONFIRMED, PAID, CANCELLED);
            case PAID -> List.of(PROCESSING, CANCELLED);
            case PROCESSING -> List.of(INVENTORY, CANCELLED);
            case INVENTORY -> List.of(READY_TO_SHIP, CANCELLED);
            case READY_TO_SHIP -> List.of(SHIPPED, CANCELLED);
            case SHIPPED -> List.of(DELIVERED, FAILED_DELIVERY);
            case FAILED_DELIVERY -> List.of(READY_TO_SHIP, RETURNED);
            case RETURNED -> List.of(REFUNDED);
            case SHIPPING -> List.of(SHIPPED);
            case DELIVERED_FAILED -> List.of(READY_TO_SHIP, RETURNED);
            case DELIVERED -> List.of(COMPLETED);
            case REFUNDED, CANCELLED, COMPLETED -> List.of();
        };
    }

    public boolean canTransitionTo(OrderStatus nextStatus) {
        return nextStatus != null && getAllowedTransitions().contains(nextStatus);
    }

    public boolean canBeChangedByDeliveryUnitTo(OrderStatus nextStatus) {
        return switch (this) {
            case READY_TO_SHIP -> nextStatus == SHIPPED;
            case SHIPPED -> nextStatus == DELIVERED || nextStatus == FAILED_DELIVERY;
            case FAILED_DELIVERY, DELIVERED_FAILED -> nextStatus == READY_TO_SHIP || nextStatus == RETURNED;
            case SHIPPING -> nextStatus == SHIPPED;
            default -> false;
        };
    }
}
