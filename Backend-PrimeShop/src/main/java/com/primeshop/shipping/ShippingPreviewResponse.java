package com.primeshop.shipping;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class ShippingPreviewResponse {
    private BigDecimal shippingFee;        // Phí vận chuyển trước giảm
    private BigDecimal shippingDiscount;   // Số tiền giảm (nếu có mã FREESHIP)
    private BigDecimal finalShippingFee;   // Phí vận chuyển sau khi giảm
    private ShippingZone shippingZone;     // Khu vực giao hàng
    private String zoneDescription;        // Mô tả khu vực
    private boolean isFreeShipping;        // Có miễn phí ship không
    private String freeShippingReason;     // Lý do miễn phí ship (nếu có)
} 