package com.primeshop.payment.method.momo;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class MoMoIpnResponse {
    private String status;
    private String message;
}
