package com.primeshop.delivery;

import com.primeshop.order.OrderStatus;

public class UpdateStatusRequest {
    private OrderStatus status;
    private String location;
    private String note;
    private boolean paymentCollected;

    // Getters and Setters
    public OrderStatus getStatus() { return status; }
    public void setStatus(OrderStatus status) { this.status = status; }
    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }
    public String getNote() { return note; }
    public void setNote(String note) { this.note = note; }
    public boolean isPaymentCollected() { return paymentCollected; }
    public void setPaymentCollected(boolean paymentCollected) { this.paymentCollected = paymentCollected; }
}
