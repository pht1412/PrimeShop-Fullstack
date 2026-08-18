package com.primeshop.cart;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.primeshop.voucher.Voucher;
import com.primeshop.voucher.VoucherRepository;
import com.primeshop.voucher.VoucherService;

import java.util.List;

@RestController
@RequestMapping("/api/cart")
@CrossOrigin(origins = "http://localhost:5173")
public class CartController {
    @Autowired
    private CartService cartService;

    @Autowired
    private CartRepo cartRepo;
    @Autowired
    private VoucherRepository voucherRepository;
    @Autowired
    private VoucherService voucherService;

    @PostMapping("/add")
    public ResponseEntity<?> addToCart(@RequestBody CartItemRequest request) {
        return ResponseEntity.ok(cartService.addToCart(request));
    }

    @PostMapping("/remove")
    public ResponseEntity<?> removeFromCart(@RequestBody CartItemRequest request) {
        cartService.removeFromCart(request);
        return ResponseEntity.ok("Xóa sản phẩm khỏi giỏ hàng thành công!");
    }

    @GetMapping
    public ResponseEntity<?> getCart() {
        return ResponseEntity.ok(cartService.getCart());
    }

    @GetMapping("/count")
    public ResponseEntity<?> getCartItemCount() {
        return ResponseEntity.ok(cartService.getCartItemCount());
    }

    /* @PostMapping("/apply-voucher")
    public ResponseEntity<?> applyVoucherToCart(@RequestBody ApplyVoucherRequest request) {
        Cart cart = cartRepo.findById(request.getCartId())
            .orElseThrow(() -> new RuntimeException("Cart not found"));
        Voucher voucher = voucherRepository.findByCode(request.getVoucherCode())
            .orElseThrow(() -> new RuntimeException("Voucher not found"));
        cart.setVoucher(voucher);
        cart.setTotalPrice();
        cartRepo.save(cart);
        return ResponseEntity.ok(cart);
    } */

    @PostMapping("/apply-multi-voucher")
    public ResponseEntity<?> applyMultiVoucherToCart(@RequestBody ApplyMultiVoucherRequest request) {
        System.out.println("🛒 Applying vouchers to cart (NO USED_COUNT INCREASE): " + request.getVoucherCodes());
        
        Cart cart = cartRepo.findById(request.getCartId())
            .orElseThrow(() -> new RuntimeException("Cart not found"));

        // Xóa voucher cũ nếu có
        cart.setVouchers(null);

        // Áp dụng nhiều voucher - CHỈ LƯU VÀO CART, KHÔNG TĂNG USED_COUNT
        List<Voucher> vouchers = voucherRepository.findAllByCodeIn(request.getVoucherCodes());
        
        // Log thông tin voucher (KHÔNG TĂNG USED_COUNT)
        for (Voucher voucher : vouchers) {
            System.out.println("📝 Voucher added to cart: " + voucher.getCode() + 
                             " (current_usage: " + voucher.getCurrentUsage() + 
                             ", max_usage: " + voucher.getMaxUsage() + 
                             ") - NO USED_COUNT INCREASE");
        }
        
        cart.setVouchers(vouchers);

        // Tính lại tổng tiền, discount, phí ship, v.v.
        cart.setTotalPrice();
        cartRepo.save(cart);
        
        System.out.println("🎉 Cart updated with " + vouchers.size() + " vouchers (used_count unchanged)");
        return ResponseEntity.ok(cart);
    }

    // DTO cho request
    public static class ApplyVoucherRequest {
        private Long cartId;
        private String voucherCode;
        // getters/setters
        public Long getCartId() { return cartId; }
        public void setCartId(Long cartId) { this.cartId = cartId; }
        public String getVoucherCode() { return voucherCode; }
        public void setVoucherCode(String voucherCode) { this.voucherCode = voucherCode; }
    }

    // DTO cho request apply-multi-voucher
    public static class ApplyMultiVoucherRequest {
        private Long cartId;
        private List<String> voucherCodes;
        // getters/setters
        public Long getCartId() { return cartId; }
        public void setCartId(Long cartId) { this.cartId = cartId; }
        public List<String> getVoucherCodes() { return voucherCodes; }
        public void setVoucherCodes(List<String> voucherCodes) { this.voucherCodes = voucherCodes; }
    }
}
