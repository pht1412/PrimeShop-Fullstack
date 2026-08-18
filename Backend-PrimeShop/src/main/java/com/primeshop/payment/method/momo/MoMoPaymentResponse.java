package com.primeshop.payment.method.momo;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class MoMoPaymentResponse {
    private String payUrl;
    private String deeplink;
    private String qrCodeUrl;
    private String errorCode;
    private String message;
    private Integer resultCode;
}
