package com.primeshop.wallet;

public class QrResponse {
    private String qrCode;
    private Double amount;
    private String signature;

    public QrResponse(String qrCode, Double amount, String signature) {
        this.qrCode = qrCode;
        this.amount = amount;
        this.signature = signature;
    }

    public String getQrCode() { return qrCode; }
    public void setQrCode(String qrCode) { this.qrCode = qrCode; }
    public Double getAmount() { return amount; }
    public void setAmount(Double amount) { this.amount = amount; }
    public String getSignature() { return signature; }
    public void setSignature(String signature) { this.signature = signature; }
}