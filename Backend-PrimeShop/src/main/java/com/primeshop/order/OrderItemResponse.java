package com.primeshop.order;

import java.math.BigDecimal;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class OrderItemResponse {
    private Long productId;
    private String productName;
    private Integer quantity;
    private BigDecimal totalPrice;
    
    // ✅ FIX 1: Thêm trường imageUrl vào DTO
    private String imageUrl;

    public OrderItemResponse(OrderItem orderItem) {
        this.productId = orderItem.getProduct().getId();
        this.productName = orderItem.getProduct().getName();
        this.quantity = orderItem.getQuantity();
        this.totalPrice = orderItem.getTotalPrice();
        
        // ✅ FIX 2: Logic lấy ảnh thông minh
        // Ưu tiên 1: Lấy ảnh snapshot đã lưu trong orderItem (lúc mua)
        if (orderItem.getProductImageUrl() != null && !orderItem.getProductImageUrl().isEmpty()) {
            this.imageUrl = orderItem.getProductImageUrl();
        } 
        // Ưu tiên 2: Nếu không có snapshot (đơn cũ), lấy ảnh hiện tại của sản phẩm
        else if (orderItem.getProduct() != null) {
            this.imageUrl = orderItem.getProduct().getImageUrl();
        }
    }
}