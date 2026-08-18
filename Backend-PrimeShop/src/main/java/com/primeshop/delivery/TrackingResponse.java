package com.primeshop.delivery;

import com.primeshop.delivery.DeliveryTracking;
import com.primeshop.order.OrderStatus;

public class TrackingResponse {
    private Long id;
    private OrderStatus status;
    private String timestamp;
    private String location;

    public TrackingResponse(DeliveryTracking tracking) {
        this.id = tracking.getId();
        this.status = tracking.getStatus();
        this.timestamp = tracking.getTimestamp().toString();
        this.location = tracking.getLocation();
    }

    // Getters
    public Long getId() { return id; }
    public OrderStatus getStatus() { return status; }
    public String getTimestamp() { return timestamp; }
    public String getLocation() { return location; }
}
