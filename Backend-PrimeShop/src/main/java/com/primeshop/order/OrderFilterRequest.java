package com.primeshop.order;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderFilterRequest {
    private Long userId;
    private Long orderId;
    private OrderStatus status;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private Boolean isDeleted;
    private BigDecimal minTotalAmount;
    private BigDecimal maxTotalAmount;
    // public List<OrderStatus> getStatus() { return status; }
    // public void setStatus(List<OrderStatus> status) { this.status = status; }
}
