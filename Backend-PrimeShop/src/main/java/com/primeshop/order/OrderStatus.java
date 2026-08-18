package com.primeshop.order;

public enum OrderStatus {
    PENDING,       // Đang chờ xác nhận
    CONFIRMED,     // Đã xác nhận, sẵn sàng thanh toán
    PAID,          // Thanh toán thành công
    PROCESSING,    // Đang xử lý
    SHIPPED,       // Đã giao hàng
    DELIVERED,     // Đã nhận hàng
    DELIVERED_FAILED, // Giao hàng không thành công
    PAYMENT_FAILED, // Thanh toán thất bại
    INVENTORY,     // Đang kiểm kho
    READY_TO_SHIP, // Sẵn sàng để giao hàng
    SHIPPING,    // Đã nhận hàng
    FAILED_DELIVERY, // Giao hàng thất bại
    RETURNED,     // Đã trả hàng
    REFUNDED,      // Đã hoàn tiền
    CANCELLED,      // Đã hủy
    COMPLETED;     // Đã hoàn tất
}
