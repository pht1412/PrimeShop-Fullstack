package com.primeshop.payment.method.paypal;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor

public class PayPalRequest {
    private Long orderId;
}
