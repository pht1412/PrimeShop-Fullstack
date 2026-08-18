package com.primeshop.shipping;

import lombok.Data;
import java.math.BigDecimal;
import java.util.List;

@Data
public class ShippingPreviewRequest {
    private ShippingAddress address;
    private List<CartItemInfo> cartItems;
    private BigDecimal subtotal;
    private String voucherCode; // Mã giảm giá (có thể null)
} 