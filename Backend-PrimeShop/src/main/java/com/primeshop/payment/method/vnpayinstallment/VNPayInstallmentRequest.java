package com.primeshop.payment.method.vnpayinstallment;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class VNPayInstallmentRequest {
    @NotNull(message = "Order ID is required")
    private Long orderId;
    
    @NotNull(message = "Installment months is required")
    @Min(value = 1, message = "Installment months must be at least 1")
    private Integer installmentMonths;
}
