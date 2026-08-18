package com.primeshop.payment.method.momo;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class MoMoPaymentRequest {
    private String partnerCode;
    private String partnerName;
    private String storeId;
    private String requestId;
    private String amount;
    private String orderId;
    private String orderInfo;
    private String redirectUrl;
    private String ipnUrl;
    private String requestType;
    private String signature;
    private String lang;
    private boolean autoCapture;
    private String extraData;
}
