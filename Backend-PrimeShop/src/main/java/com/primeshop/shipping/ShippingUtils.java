package com.primeshop.shipping;

import java.math.BigDecimal;
import java.util.List;

public class ShippingUtils {

    /**
     * Tính tổng số lượng sản phẩm trong giỏ hàng
     */
    public static int calculateTotalQuantity(List<CartItemInfo> cartItems) {
        if (cartItems == null || cartItems.isEmpty()) {
            return 0;
        }
        
        return cartItems.stream()
                .mapToInt(CartItemInfo::getQuantity)
                .sum();
    }

    /**
     * Tính tổng giá trị giỏ hàng
     */
    public static BigDecimal calculateSubtotal(List<CartItemInfo> cartItems) {
        if (cartItems == null || cartItems.isEmpty()) {
            return BigDecimal.ZERO;
        }
        
        return cartItems.stream()
                .map(CartItemInfo::getTotalPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    /**
     * Kiểm tra xem có phải địa chỉ TPHCM không
     */
    public static boolean isHCMCAddress(String province) {
        if (province == null) {
            return false;
        }
        
        String normalizedProvince = province.trim().toLowerCase();
        return normalizedProvince.equals("tphcm") || 
               normalizedProvince.equals("thành phố hồ chí minh") ||
               normalizedProvince.equals("hồ chí minh");
    }

    /**
     * Format số tiền theo định dạng VND
     */
    public static String formatCurrency(BigDecimal amount) {
        if (amount == null) {
            return "0đ";
        }
        
        return String.format("%,.0fđ", amount.doubleValue());
    }

    /**
     * Kiểm tra xem có đủ điều kiện miễn phí ship không
     */
    public static boolean isEligibleForFreeShipping(BigDecimal subtotal, BigDecimal threshold) {
        return subtotal != null && threshold != null && 
               subtotal.compareTo(threshold) >= 0;
    }
} 