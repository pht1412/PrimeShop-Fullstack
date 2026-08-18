package com.primeshop.cart;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;
import lombok.Data;
import com.primeshop.voucher.VoucherResponse;

@Data
public class CartResponse {
    private Long id;
    private BigDecimal totalPrice;
    private BigDecimal discount;
    private List<VoucherResponse> vouchers;
    private List<CartItemResponse> items;

    public CartResponse(Cart cart) {
        this.id = cart.getId();
        this.totalPrice = cart.getTotalPrice();
        this.discount = cart.getDiscount();
        
        // Handle vouchers safely
        if (cart.getVouchers() != null) {
            this.vouchers = VoucherResponse.fromVouchers(cart.getVouchers());
        }
        
        this.items = cart.getCartItems().stream()
                .map(CartItemResponse::new)
                .collect(Collectors.toList());
    }
}
