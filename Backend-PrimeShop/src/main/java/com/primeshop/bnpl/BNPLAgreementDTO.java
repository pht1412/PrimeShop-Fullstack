package com.primeshop.bnpl;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class BNPLAgreementDTO {
    private Long id;
    private String provider;
    private String fundiinOrderId;
    private BigDecimal totalAmount;
    private String status;
    private LocalDateTime createdAt;
    private LocalDateTime dueDate;

    private Long orderId;
    private String username;
}
