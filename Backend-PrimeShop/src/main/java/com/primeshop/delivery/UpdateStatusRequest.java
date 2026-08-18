package com.primeshop.delivery;

import com.primeshop.order.OrderStatus;

public class UpdateStatusRequest {
    private OrderStatus status;
    private String location;

    // Getters and Setters
    public OrderStatus getStatus() { return status; }
    public void setStatus(OrderStatus status) { this.status = status; }
    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }
}
