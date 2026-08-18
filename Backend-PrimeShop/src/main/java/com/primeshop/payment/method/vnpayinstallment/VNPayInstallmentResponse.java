package com.primeshop.payment.method.vnpayinstallment;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VNPayInstallmentResponse {
    private String paymentUrl;
    private String transactionId;
    private String orderId;
    private BigDecimal totalAmount;
    private BigDecimal monthlyPayment;
    private Integer installmentMonths;
    private BigDecimal interestRate;
    private String status;
    private LocalDateTime createdAt;
    private LocalDateTime expiresAt;
    private String message;
}
