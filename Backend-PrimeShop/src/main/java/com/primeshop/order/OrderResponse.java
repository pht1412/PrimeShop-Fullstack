package com.primeshop.order;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import com.primeshop.seller.SellerProfile;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class OrderResponse {
    private Long orderId;
    private Long userId;
    private BigDecimal totalAmount;
    private BigDecimal finalAmount; // <--- THÊM DÒNG NÀY (Thực thu)
    private BigDecimal discountAmount; // <--- THÊM DÒNG NÀY (Tiền giảm giá - Optional nhưng nên có)
    private OrderStatus orderStatus;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String fullName;
    private String phoneNumber;
    private String address;
    private String note;
    private boolean isAdmin;
    private Long sellerId;
    private String shopName;
    private List<OrderItemResponse> orderItems;

    public OrderResponse(Order order) {
        this.orderId = order.getId();
        this.userId = order.getUser().getId();        
        this.totalAmount = order.getTotalAmount();
        this.finalAmount = order.getFinalAmount(); 
        this.discountAmount = order.getDiscountAmount();
        this.orderStatus = order.getStatus();
        this.createdAt = order.getCreatedAt();
        this.updatedAt = order.getUpdatedAt();
        this.orderItems = order.getOrderItems().stream()
                .map(OrderItemResponse::new)
                .collect(Collectors.toList());
        this.fullName = order.getFullName();
        this.phoneNumber = order.getPhoneNumber();
        this.address = order.getAddress();
        this.note = order.getNote();
        this.isAdmin = order.getUser().getRoles().stream()
                .anyMatch(role -> role.getName().toString().contains("ADMIN"));
        SellerProfile seller = order.getSeller();
        if (seller != null) {
            this.sellerId = seller.getId();
        } else {
            this.sellerId = null;
            System.out.println("WARNING: Order ID " + order.getId() + " không có seller.");
        }
    }
}
