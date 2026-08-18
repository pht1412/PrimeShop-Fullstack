package com.primeshop.payment.method.momo;

import lombok.Data;

@Data
public class MoMoIpnRequest {
    private String partnerCode;
    private Long orderId;
    private String requestId;
    private String amount;
    private String orderInfo;
    private String orderType;
    private String transId;
    private Integer resultCode;
    private String message;
    private String payType;
    private Long responseTime;
    private String extraData;
    private String signature;
}
