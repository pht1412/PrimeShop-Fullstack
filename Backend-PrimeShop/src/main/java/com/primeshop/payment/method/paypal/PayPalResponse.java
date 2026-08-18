package com.primeshop.payment.method.paypal;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class PayPalResponse {
    private String id;
    private String status;
    private String[] links;
}
