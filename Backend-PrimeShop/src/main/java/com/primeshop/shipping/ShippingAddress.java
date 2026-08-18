package com.primeshop.shipping;

import lombok.Data;

@Data
public class ShippingAddress {
    private String province; // Tỉnh/thành phố
    private String district; // Quận/huyện
    private String ward;     // Phường/xã
} 