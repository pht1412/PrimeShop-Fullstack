package com.primeshop.shipping;

public enum ShippingZone {
    HCMC_INNER("TPHCM - Nội thành"),
    HCMC_OUTER("TPHCM - Ngoại thành"),
    OTHER_PROVINCES("Các tỉnh/thành khác");

    private final String description;

    ShippingZone(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
} 