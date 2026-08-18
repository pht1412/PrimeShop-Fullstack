package com.primeshop.voucher;

import com.primeshop.cart.Cart;
import com.primeshop.cart.CartRepo;
import com.primeshop.user.User;
import com.primeshop.user.UserRepo;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.primeshop.voucher.Voucher;
import com.primeshop.voucher.VoucherResponse;
import com.primeshop.voucher.VoucherStatistics;
import com.primeshop.voucher.VoucherValidationResult;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/vouchers")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:5173", allowCredentials = "false")
public class VoucherController {
    
    private final VoucherService voucherService;
    private final UserRepo userRepo;
    private final CartRepo cartRepo;
    private final VoucherRepository voucherRepository;
    
    /**
     * Lấy tất cả voucher đang hoạt động
     */
    @GetMapping
    public ResponseEntity<List<VoucherResponse>> getAllActiveVouchers() {
        List<Voucher> vouchers = voucherService.getAllActiveVouchers();
        List<VoucherResponse> responses = vouchers.stream()
                .map(VoucherResponse::fromVoucher)
                .collect(java.util.stream.Collectors.toList());
        return ResponseEntity.ok(responses);
    }
    
    /**
     * Lấy voucher theo ID
     * 
     */
    @GetMapping("/{id}")
    public ResponseEntity<VoucherResponse> getVoucherById(@PathVariable Long id) {
        Optional<Voucher> voucherOpt = voucherService.findById(id);
        if (voucherOpt.isPresent()) {
            VoucherResponse response = VoucherResponse.fromVoucher(voucherOpt.get());
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.notFound().build();
        }
    }
    /**
     * Kiểm tra voucher có hợp lệ cho đơn hàng không
     */
    @PostMapping("/validate")
    public ResponseEntity<VoucherValidationResponse> validateVoucher(
            @RequestBody VoucherValidationRequest request) {
        
        VoucherValidationResult result = voucherService.validateVoucherWithDetails(
            request.getCode(), request.getOrderValue());
        
        VoucherValidationResponse response = VoucherValidationResponse.builder()
                .valid(result.isValid())
                .discountAmount(result.getDiscountAmount())
                .message(result.getMessage())
                .build();
        
        return ResponseEntity.ok(response);
    }

    /**
     * Kiểm tra nhiều voucher có hợp lệ cho đơn hàng không
     */
    @PostMapping("/validate-multiple")
    public ResponseEntity<MultipleVoucherValidationResponse> validateMultipleVouchers(
            @RequestBody MultipleVoucherValidationRequest request) {
        
        VoucherValidationResult result = voucherService.validateMultipleVouchers(
            request.getVoucherCodes(), request.getOrderValue());
        
        MultipleVoucherValidationResponse response = MultipleVoucherValidationResponse.builder()
                .valid(result.isValid())
                .discountAmount(result.getDiscountAmount())
                .message(result.getMessage())
                .vouchers(result.getVouchers() != null ? 
                    result.getVouchers().stream()
                        .map(VoucherResponse::fromVoucher)
                        .collect(java.util.stream.Collectors.toList()) : 
                    new ArrayList<>())
                .build();
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * Lấy voucher hợp lệ cho đơn hàng
     */
    @GetMapping("/valid-for-order")
    public ResponseEntity<List<VoucherResponse>> getValidVouchersForOrder(
            @RequestParam Double orderValue) {
        List<Voucher> vouchers = voucherService.getValidVouchersForOrder(orderValue);
        List<VoucherResponse> responses = vouchers.stream()
                .map(v -> VoucherResponse.fromVoucherWithOrderValue(v, orderValue))
                .collect(java.util.stream.Collectors.toList());
        return ResponseEntity.ok(responses);
    }
    
    /**
     * Tạo voucher mới (Admin only)
     */
    @PostMapping
    public ResponseEntity<VoucherResponse> createVoucher(@RequestBody Voucher voucher) {
        try {
            Voucher createdVoucher = voucherService.createVoucher(voucher);
            return ResponseEntity.ok(VoucherResponse.fromVoucher(createdVoucher));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }
    
    /**
     * Cập nhật voucher (Admin only)
     */
    @PutMapping("/{id}")
    public ResponseEntity<VoucherResponse> updateVoucher(
            @PathVariable Long id, 
            @RequestBody Voucher voucherDetails) {
        try {
            Voucher updatedVoucher = voucherService.updateVoucher(id, voucherDetails);
            return ResponseEntity.ok(VoucherResponse.fromVoucher(updatedVoucher));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }
    
    /**
     * Xóa voucher (Admin only)
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deactivateVoucher(@PathVariable Long id) {
        boolean success = voucherService.deactivateVoucher(id);
        return success ? ResponseEntity.ok().build() : ResponseEntity.notFound().build();
    }
    
    /**
     * Sử dụng voucher
     */
    /* @PostMapping("/{id}/use")
    public ResponseEntity<Void> useVoucher(@PathVariable Long id) {
        boolean success = voucherService.useVoucher(id);
        return success ? ResponseEntity.ok().build() : ResponseEntity.badRequest().build();
    } */
    
    /**
     * Sử dụng voucher theo mã code
     */
    @PostMapping("/use-by-code")
    public ResponseEntity<Void> useVoucherByCode(@RequestParam String code) {
        boolean success = voucherService.useVoucherByCode(code);
        return success ? ResponseEntity.ok().build() : ResponseEntity.badRequest().build();
    }
    
    /**
     * Sử dụng nhiều voucher theo mã code
     */
    @PostMapping("/use-multi-by-code")
    public ResponseEntity<Map<String, Boolean>> useVouchersByCodes(@RequestBody List<String> codes) {
        Map<String, Boolean> result = new HashMap<>();
        for (String code : codes) {
            boolean success = voucherService.useVoucherByCode(code);
            result.put(code, success);
        }
        return ResponseEntity.ok(result);
    }
    
    /**
     * Lấy thống kê voucher (Admin only)
     */
    @GetMapping("/statistics")
    public ResponseEntity<VoucherStatistics> getVoucherStatistics() {
        VoucherStatistics statistics = voucherService.getVoucherStatistics();
        return ResponseEntity.ok(statistics);
    }
    
    /**
     * Lấy voucher theo loại giảm giá
     */
    @GetMapping("/by-type")
    public ResponseEntity<List<VoucherResponse>> getVouchersByDiscountType(
            @RequestParam Voucher.DiscountType discountType) {
        List<Voucher> vouchers = voucherService.getVouchersByDiscountType(discountType);
        List<VoucherResponse> responses = vouchers.stream()
                .map(VoucherResponse::fromVoucher)
                .collect(java.util.stream.Collectors.toList());
        return ResponseEntity.ok(responses);
    }
    
    /**
     * Kiểm tra voucher có hết hạn không
     */
    @GetMapping("/{code}/expired")
    public ResponseEntity<Boolean> isVoucherExpired(@PathVariable String code) {
        boolean isExpired = voucherService.isVoucherExpired(code);
        return ResponseEntity.ok(isExpired);
    }
    
    /**
     * Áp dụng voucher cho giỏ hàng
     */
    @PostMapping("/apply")
    public ResponseEntity<?> applyVoucher(@RequestBody ApplyVoucherRequest req) {
        try {
            Cart cart = voucherService.applyVoucherToCart(req.getCartId(), req.getVoucherCode());
            return ResponseEntity.ok(cart);
        } catch (RuntimeException ex) {
            return ResponseEntity.badRequest().body(Map.of("error", ex.getMessage()));
        }
    }

    /**
     * Xóa voucher khỏi giỏ hàng
     */
    @PostMapping("/remove")
    public ResponseEntity<?> removeVoucher(@RequestBody RemoveVoucherRequest req) {
        try {
            Cart cart = voucherService.removeVoucherFromCart(req.getCartId());
            return ResponseEntity.ok(cart);
        } catch (RuntimeException ex) {
            return ResponseEntity.badRequest().body(Map.of("error", ex.getMessage()));
        }
    }

    /**
     * Lấy tất cả vouchers
     */
    @GetMapping("/all")
    public ResponseEntity<List<VoucherResponse>> getAllVouchers() {
        List<Voucher> vouchers = voucherService.getAllVouchers();
        List<VoucherResponse> responses = vouchers.stream()
                .map(VoucherResponse::fromVoucher)
                .collect(java.util.stream.Collectors.toList());
        return ResponseEntity.ok(responses);
    }

    /**
     * Đếm tất cả vouchers
     */
    @GetMapping("/count")
    public ResponseEntity<Map<String, Long>> countAllVouchers() {
        long count = voucherService.countAllVouchers();
        return ResponseEntity.ok(Map.of("count", count));
    }

    @GetMapping("/test-cart-voucher")
    public ResponseEntity<Map<String, Object>> testCartVoucher(@RequestParam String voucherCode, @RequestParam(required = false) String username) {
        try {
            Map<String, Object> result = new HashMap<>();
            
            // Lấy cart của user
            User user = null;
            if (username != null && !username.isEmpty()) {
                user = userRepo.findByUsername(username).orElse(null);
            }
            
            if (user == null) {
                result.put("error", "User not found");
                return ResponseEntity.badRequest().body(result);
            }
            
            Cart cart = cartRepo.findByUser(user).orElse(null);
            if (cart == null) {
                result.put("error", "Cart not found for user: " + username);
                return ResponseEntity.badRequest().body(result);
            }
            
            // Tính toán lại cart total
            cart.setTotalPrice();
            BigDecimal cartTotal = cart.getTotalAmount();
            
            // Lấy voucher
            Optional<Voucher> voucherOpt = voucherRepository.findByCode(voucherCode);
            if (voucherOpt.isEmpty()) {
                result.put("error", "Voucher not found: " + voucherCode);
                return ResponseEntity.badRequest().body(result);
            }
            
            Voucher voucher = voucherOpt.get();
            
            // Kiểm tra điều kiện voucher
            Map<String, Object> validation = new HashMap<>();
            validation.put("voucherCode", voucherCode);
            validation.put("cartTotal", cartTotal.doubleValue());
            validation.put("voucherMinOrderValue", voucher.getMinOrderValue());
            validation.put("voucherIsActive", voucher.getIsActive());
            validation.put("voucherIsValid", voucher.isValid());
            validation.put("voucherCurrentUsage", voucher.getCurrentUsage());
            validation.put("voucherMaxUsage", voucher.getMaxUsage());
            
            // Kiểm tra từng điều kiện
            List<String> errors = new ArrayList<>();
            
            if (!Boolean.TRUE.equals(voucher.getIsActive())) {
                errors.add("Voucher is inactive");
            }
            
            if (voucher.getExpirationDate() != null && voucher.getExpirationDate().isBefore(LocalDateTime.now())) {
                errors.add("Voucher expired");
            }
            
            if (voucher.getMaxUsage() != null && voucher.getCurrentUsage() >= voucher.getMaxUsage()) {
                errors.add("Voucher usage limit reached");
            }
            
            if (voucher.getMinOrderValue() != null && cartTotal.doubleValue() < voucher.getMinOrderValue()) {
                errors.add("Order value too low for voucher. Required: " + voucher.getMinOrderValue() + ", Current: " + cartTotal.doubleValue());
            }
            
            validation.put("errors", errors);
            validation.put("canApply", errors.isEmpty());
            
            // Tính discount nếu có thể áp dụng
            if (errors.isEmpty()) {
                double discount = 0;
                if (voucher.getDiscountType() == Voucher.DiscountType.PERCENT) {
                    discount = cartTotal.doubleValue() * voucher.getDiscountValue() / 100.0;
                    if (voucher.getMaxDiscountValue() != null) {
                        discount = Math.min(discount, voucher.getMaxDiscountValue());
                    }
                } else {
                    discount = voucher.getDiscountValue();
                }
                discount = Math.min(discount, cartTotal.doubleValue());
                
                validation.put("discountAmount", discount);
                validation.put("finalAmount", cartTotal.doubleValue() - discount);
            }
            
            result.put("cartInfo", Map.of(
                "cartId", cart.getId(),
                "username", username,
                "cartTotal", cartTotal.doubleValue(),
                "itemCount", cart.getCartItems().size()
            ));
            result.put("voucherValidation", validation);
            
            return ResponseEntity.ok(result);
            
        } catch (Exception e) {
            Map<String, Object> result = new HashMap<>();
            result.put("error", "Test failed: " + e.getMessage());
            return ResponseEntity.badRequest().body(result);
        }
    }

    /**
     * Test endpoint để kiểm tra voucher usage count
     */
    @GetMapping("/test-usage/{code}")
    public ResponseEntity<Map<String, Object>> testVoucherUsage(@PathVariable String code) {
        Optional<Voucher> voucherOpt = voucherService.findByCode(code);
        
        Map<String, Object> response = new HashMap<>();
        
        if (voucherOpt.isPresent()) {
            Voucher voucher = voucherOpt.get();
            response.put("code", voucher.getCode());
            response.put("currentUsage", voucher.getCurrentUsage());
            response.put("maxUsage", voucher.getMaxUsage());
            response.put("isActive", voucher.getIsActive());
            response.put("isValid", voucher.isValid());
            response.put("hasRemainingUsage", voucher.hasRemainingUsage());
            response.put("remainingUsage", voucher.getRemainingUsage());
            return ResponseEntity.ok(response);
        } else {
            response.put("error", "Voucher not found");
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Test endpoint để tăng usage count của voucher
     */
    @PostMapping("/test-increment/{code}")
    public ResponseEntity<Map<String, Object>> testIncrementUsage(@PathVariable String code) {
        Optional<Voucher> voucherOpt = voucherService.findByCode(code);
        
        Map<String, Object> response = new HashMap<>();
        
        if (voucherOpt.isPresent()) {
            Voucher voucher = voucherOpt.get();
            int beforeUsage = voucher.getCurrentUsage();
            
            voucher.incrementUsage();
            voucherService.saveAndFlush(voucher);
            
            response.put("code", voucher.getCode());
            response.put("beforeUsage", beforeUsage);
            response.put("afterUsage", voucher.getCurrentUsage());
            response.put("incremented", true);
            return ResponseEntity.ok(response);
        } else {
            response.put("error", "Voucher not found");
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping("/test-process")
    public ResponseEntity<?> testVoucherProcessing(@RequestBody TestVoucherRequest request) {
        try {
            System.out.println("🧪 Testing voucher processing for codes: " + request.getVoucherCodes());
            
            List<Voucher> processedVouchers = voucherService.processVouchersForOrder(
                request.getVoucherCodes(), 
                request.getOrderValue()
            );
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Voucher processing test completed");
            response.put("processedVouchers", processedVouchers.size());
            response.put("vouchers", processedVouchers.stream()
                .map(v -> Map.of(
                    "code", v.getCode(),
                    "currentUsage", v.getCurrentUsage(),
                    "maxUsage", v.getMaxUsage(),
                    "discountType", v.getDiscountType(),
                    "discountValue", v.getDiscountValue()
                ))
                .collect(Collectors.toList()));
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            System.err.println("❌ Voucher processing test failed: " + e.getMessage());
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    @GetMapping("/check-usage/{code}")
    public ResponseEntity<?> checkVoucherUsage(@PathVariable String code) {
        try {
            Optional<Voucher> voucherOpt = voucherService.findByCode(code);
            if (voucherOpt.isEmpty()) {
                return ResponseEntity.notFound().build();
            }
            
            Voucher voucher = voucherOpt.get();
            Map<String, Object> response = new HashMap<>();
            response.put("code", voucher.getCode());
            response.put("currentUsage", voucher.getCurrentUsage());
            response.put("maxUsage", voucher.getMaxUsage());
            response.put("remainingUsage", voucher.getMaxUsage() - voucher.getCurrentUsage());
            response.put("isActive", voucher.getIsActive());
            response.put("isValid", voucher.isValid());
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    // DTO classes cho request/response
    public static class VoucherValidationRequest {
        private String code;
        private Double orderValue;
        
        // Getters and setters
        public String getCode() { return code; }
        public void setCode(String code) { this.code = code; }
        public Double getOrderValue() { return orderValue; }
        public void setOrderValue(Double orderValue) { this.orderValue = orderValue; }
    }
    
    public static class VoucherValidationResponse {
        private boolean valid;
        private Double discountAmount;
        private String message;
        
        // Builder pattern
        public static VoucherValidationResponseBuilder builder() {
            return new VoucherValidationResponseBuilder();
        }
        
        public static class VoucherValidationResponseBuilder {
            private boolean valid;
            private Double discountAmount;
            private String message;
            
            public VoucherValidationResponseBuilder valid(boolean valid) {
                this.valid = valid;
                return this;
            }
            
            public VoucherValidationResponseBuilder discountAmount(Double discountAmount) {
                this.discountAmount = discountAmount;
                return this;
            }
            
            public VoucherValidationResponseBuilder message(String message) {
                this.message = message;
                return this;
            }
            
            public VoucherValidationResponse build() {
                VoucherValidationResponse response = new VoucherValidationResponse();
                response.valid = this.valid;
                response.discountAmount = this.discountAmount;
                response.message = this.message;
                return response;
            }
        }
        
        // Getters and setters
        public boolean isValid() { return valid; }
        public void setValid(boolean valid) { this.valid = valid; }
        public Double getDiscountAmount() { return discountAmount; }
        public void setDiscountAmount(Double discountAmount) { this.discountAmount = discountAmount; }
        public String getMessage() { return message; }
        public void setMessage(String message) { this.message = message; }
    }

    @Data
    public static class ApplyVoucherRequest {
        private Long cartId;
        private String voucherCode;
    }

    @Data
    public static class RemoveVoucherRequest {
        private Long cartId;
    }

    @PostMapping("/apply-multi")
    public ResponseEntity<?> applyVouchers(@RequestBody ApplyVouchersRequest req) {
        try {
            Cart cart = voucherService.applyVouchersToCart(req.getCartId(), req.getVoucherCodes());
            return ResponseEntity.ok(cart);
        } catch (RuntimeException ex) {
            return ResponseEntity.badRequest().body(Map.of("error", ex.getMessage()));
        }
    }

    // DTO
    @Data
    public static class ApplyVouchersRequest {
        private Long cartId;
        private List<String> voucherCodes;
    }

    @Data
    public static class MultipleVoucherValidationRequest {
        private List<String> voucherCodes;
        private Double orderValue;
    }

    @Data
    public static class MultipleVoucherValidationResponse {
        private boolean valid;
        private Double discountAmount;
        private String message;
        private List<VoucherResponse> vouchers;

        public static MultipleVoucherValidationResponseBuilder builder() {
            return new MultipleVoucherValidationResponseBuilder();
        }

        public static class MultipleVoucherValidationResponseBuilder {
            private boolean valid;
            private Double discountAmount;
            private String message;
            private List<VoucherResponse> vouchers;

            public MultipleVoucherValidationResponseBuilder valid(boolean valid) {
                this.valid = valid;
                return this;
            }

            public MultipleVoucherValidationResponseBuilder discountAmount(Double discountAmount) {
                this.discountAmount = discountAmount;
                return this;
            }

            public MultipleVoucherValidationResponseBuilder message(String message) {
                this.message = message;
                return this;
            }

            public MultipleVoucherValidationResponseBuilder vouchers(List<VoucherResponse> vouchers) {
                this.vouchers = vouchers;
                return this;
            }

            public MultipleVoucherValidationResponse build() {
                MultipleVoucherValidationResponse response = new MultipleVoucherValidationResponse();
                response.valid = this.valid;
                response.discountAmount = this.discountAmount;
                response.message = this.message;
                response.vouchers = this.vouchers;
                return response;
            }
        }
    }

    // DTO cho test request
    public static class TestVoucherRequest {
        private List<String> voucherCodes;
        private Double orderValue;
        
        public List<String> getVoucherCodes() { return voucherCodes; }
        public void setVoucherCodes(List<String> voucherCodes) { this.voucherCodes = voucherCodes; }
        public Double getOrderValue() { return orderValue; }
        public void setOrderValue(Double orderValue) { this.orderValue = orderValue; }
    }

    @PutMapping("unactivate/{id}")
    public ResponseEntity<VoucherResponse> unactivateVoucher(@PathVariable Long id) {
        try {
            Voucher updatedVoucher = voucherService.unactivateVoucher(id);
            return ResponseEntity.ok(VoucherResponse.fromVoucher(updatedVoucher));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }
    @GetMapping("/my")
    public List<Voucher> getMyVouchers(@RequestParam Long userId) {
        return voucherService.getVouchersByUserId(userId);
    }

}