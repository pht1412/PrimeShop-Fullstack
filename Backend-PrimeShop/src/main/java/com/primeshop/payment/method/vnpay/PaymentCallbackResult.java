package com.primeshop.payment.method.vnpay;

import lombok.Data;

@Data
public class PaymentCallbackResult {
    private final boolean success;
    private final int httpStatus;
    private final String message;

    private PaymentCallbackResult(boolean success, int hpptStatus, String message) {
        this.success = success;
        this.httpStatus = hpptStatus;
        this.message = message;
    }

    public static PaymentCallbackResult success(String message) {
        return new PaymentCallbackResult(true, 200, message);
    }

    public static PaymentCallbackResult failed(String msg) {
        return new PaymentCallbackResult(false, 400, msg);
    }

    public static PaymentCallbackResult invalid(String msg) {
        return new PaymentCallbackResult(false, 400, msg);
    }

    public boolean isSuccess() {
        return success;
    }

    @Override
    public String toString() {
        return "CallbackResult{success=" + success + ", message='" + message + "'}";
    }
}
