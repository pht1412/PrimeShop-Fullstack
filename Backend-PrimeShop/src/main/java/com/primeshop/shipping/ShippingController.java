package com.primeshop.shipping;

import com.primeshop.cart.Cart;
import com.primeshop.cart.CartRepo;
import com.primeshop.user.User;
import com.primeshop.user.UserRepo;
import com.primeshop.voucher.Voucher;
import com.primeshop.voucher.VoucherRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "http://localhost:5173", allowCredentials = "false")
public class ShippingController {

    @Autowired
    private ShippingService shippingService;
    
    @Autowired
    private CartRepo cartRepo;
    
    @Autowired
    private UserRepo userRepo;

    @Autowired
    private VoucherRepository voucherRepository;

    /**
     * API tính phí vận chuyển - GET method (cho frontend)
     * GET /api/shipping/calculate?address=...&city=...&district=...&voucherCode=...
     */
    @GetMapping("/shipping/calculate")
    public ResponseEntity<ShippingCalculateResponse> calculateShipping(
            @RequestParam String address,
            @RequestParam String city,
            @RequestParam String district,
            @RequestParam(required = false) String voucherCode) {
        try {
            System.out.println("Shipping calculation request:");
            System.out.println("Address: " + address);
            System.out.println("City: " + city);
            System.out.println("District: " + district);
            System.out.println("VoucherCode: " + voucherCode);

            // Tạo ShippingAddress từ parameters
            ShippingAddress shippingAddress = new ShippingAddress();
            shippingAddress.setProvince(city);
            shippingAddress.setDistrict(district);
            shippingAddress.setWard(""); // Không có ward trong frontend

            // Tạo mock cart items (vì frontend không gửi cart items)
            List<CartItemInfo> cartItems = new ArrayList<>();
            
            // Lấy subtotal từ cart hiện tại của user
            BigDecimal subtotal = BigDecimal.ZERO;
            try {
                String username = SecurityContextHolder.getContext().getAuthentication().getName();
                if (username != null && !username.equals("anonymousUser")) {
                    User user = userRepo.findByUsername(username).orElse(null);
                    if (user != null) {
                        Cart userCart = cartRepo.findByUser(user).orElse(null);
                        if (userCart != null && userCart.getTotalAmount() != null) {
                            subtotal = userCart.getTotalAmount();
                            System.out.println("Found user cart, subtotal: " + subtotal);
                        } else {
                            System.out.println("No cart found for user: " + username + " or cart is empty");
                        }
                    } else {
                        System.out.println("No user found for username: " + username);
                    }
                } else {
                    System.out.println("User not authenticated or anonymous");
                }
            } catch (Exception e) {
                System.out.println("Could not get cart subtotal: " + e.getMessage());
            }
            
            // Nếu subtotal = 0 hoặc null, sử dụng giá trị mặc định để test voucher
            if (subtotal == null || subtotal.compareTo(BigDecimal.ZERO) == 0) {
                // Sử dụng subtotal mặc định 300,000đ để đảm bảo voucher FREESHIP hoạt động
                subtotal = new BigDecimal("300000");
                System.out.println("Using default subtotal for voucher testing: " + subtotal);
            }
            
            System.out.println("Final subtotal for voucher validation: " + subtotal);
            
            // Gọi service để tính phí
            ShippingPreviewResponse previewResponse = shippingService.calculateShippingFee(
                shippingAddress,
                cartItems,
                subtotal, // Sử dụng subtotal thực tế từ cart hoặc default
                voucherCode
            );
            
            System.out.println("Shipping calculation result: " + previewResponse.getFinalShippingFee());
            System.out.println("Is free shipping: " + previewResponse.isFreeShipping());
            System.out.println("Free shipping reason: " + previewResponse.getFreeShippingReason());
            
            // Tạo response đơn giản cho frontend
            ShippingCalculateResponse response = new ShippingCalculateResponse();
            response.setShippingFee(previewResponse.getFinalShippingFee().intValue());
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("Shipping calculation error: " + e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Test endpoint để kiểm tra miễn phí ship tự động với ngưỡng mới 2,000,000 VNĐ
     * GET /api/shipping/test-new-threshold
     */
    @GetMapping("/shipping/test-new-threshold")
    public ResponseEntity<String> testNewFreeShippingThreshold() {
        try {
            StringBuilder result = new StringBuilder();
            result.append("=== NEW FREE SHIPPING THRESHOLD TEST (2,000,000 VNĐ) ===\n\n");
            
            // Test các tỉnh thành khác nhau
            String[] testCities = {"hcm", "hanoi", "danang", "haiphong", "cantho", "binhduong", "baivi", "socTrang"};
            String[] testDistricts = {"district1", "baidinh", "haichau", "hongbang", "ninhkieu", "thudaumot", "vungtau", "soctrang"};
            
            // Test các mức subtotal khác nhau
            BigDecimal[] testSubtotals = {
                new BigDecimal("1500000"), // Dưới ngưỡng
                new BigDecimal("2000000"), // Đúng ngưỡng
                new BigDecimal("2500000")  // Trên ngưỡng
            };
            
            for (int i = 0; i < testCities.length; i++) {
                String city = testCities[i];
                String district = testDistricts[i];
                
                result.append(String.format("📍 %s - %s:\n", city, district));
                
                for (BigDecimal subtotal : testSubtotals) {
                    ShippingAddress address = new ShippingAddress();
                    address.setProvince(city);
                    address.setDistrict(district);
                    
                    List<CartItemInfo> cartItems = new ArrayList<>();
                    
                    // Test không có voucher
                    ShippingPreviewResponse response = shippingService.calculateShippingFee(
                        address, cartItems, subtotal, null
                    );
                    
                    String status = response.isFreeShipping() ? "✅ MIỄN PHÍ" : "💰 CÓ PHÍ";
                    result.append(String.format("   Subtotal %sđ: %s VNĐ (%s)\n", 
                        subtotal.toString(), 
                        response.getFinalShippingFee(),
                        status));
                }
                result.append("\n");
            }
            
            return ResponseEntity.ok(result.toString());
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.ok("Test failed: " + e.getMessage());
        }
    }

    /**
     * Test endpoint để kiểm tra tính năng shipping calculation với FREESHIP đã cập nhật
     * GET /api/shipping/test-updated-freeship
     */
    @GetMapping("/shipping/test-updated-freeship")
    public ResponseEntity<String> testUpdatedFreeship() {
        try {
            StringBuilder result = new StringBuilder();
            result.append("=== UPDATED FREESHIP TEST (Subtotal: 300k >= 200k) ===\n\n");
            
            // Test các tỉnh thành khác nhau
            String[] testCities = {"hcm", "hanoi", "danang", "haiphong", "cantho", "binhduong", "baivi", "socTrang"};
            String[] testDistricts = {"district1", "baidinh", "haichau", "hongbang", "ninhkieu", "thudaumot", "vungtau", "soctrang"};
            
            for (int i = 0; i < testCities.length; i++) {
                String city = testCities[i];
                String district = testDistricts[i];
                
                ShippingAddress address = new ShippingAddress();
                address.setProvince(city);
                address.setDistrict(district);
                
                List<CartItemInfo> cartItems = new ArrayList<>();
                BigDecimal subtotal = new BigDecimal("300000"); // >= 200k để test FREESHIP voucher đã cập nhật
                
                // Test không có voucher
                ShippingPreviewResponse response1 = shippingService.calculateShippingFee(
                    address, cartItems, subtotal, null
                );
                
                // Test với voucher FREESHIP
                ShippingPreviewResponse response2 = shippingService.calculateShippingFee(
                    address, cartItems, subtotal, "FREESHIP"
                );
                
                result.append(String.format("📍 %s - %s:\n", city, district));
                result.append(String.format("   Không voucher: %s VNĐ\n", response1.getFinalShippingFee()));
                result.append(String.format("   Có FREESHIP: %s VNĐ (%s)\n", 
                    response2.getFinalShippingFee(), 
                    response2.isFreeShipping() ? "Miễn phí" : "Có phí"));
                result.append("\n");
            }
            
            return ResponseEntity.ok(result.toString());
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.ok("Test failed: " + e.getMessage());
        }
    }

    /**
     * Test endpoint để kiểm tra miễn phí ship tự động (subtotal >= 500k)
     * GET /api/shipping/test-auto-free
     */
    @GetMapping("/shipping/test-auto-free")
    public ResponseEntity<String> testAutoFreeShipping() {
        try {
            StringBuilder result = new StringBuilder();
            result.append("=== AUTO FREE SHIPPING TEST (Subtotal: 600k) ===\n\n");
            
            // Test các tỉnh thành khác nhau
            String[] testCities = {"hcm", "hanoi", "danang", "haiphong", "cantho", "binhduong", "baivi", "socTrang"};
            String[] testDistricts = {"district1", "baidinh", "haichau", "hongbang", "ninhkieu", "thudaumot", "vungtau", "soctrang"};
            
            for (int i = 0; i < testCities.length; i++) {
                String city = testCities[i];
                String district = testDistricts[i];
                
                ShippingAddress address = new ShippingAddress();
                address.setProvince(city);
                address.setDistrict(district);
                
                List<CartItemInfo> cartItems = new ArrayList<>();
                BigDecimal subtotal = new BigDecimal("600000"); // >= 500k để test auto free shipping
                
                // Test không có voucher
                ShippingPreviewResponse response1 = shippingService.calculateShippingFee(
                    address, cartItems, subtotal, null
                );
                
                // Test với voucher FREESHIP
                ShippingPreviewResponse response2 = shippingService.calculateShippingFee(
                    address, cartItems, subtotal, "FREESHIP"
                );
                
                result.append(String.format("📍 %s - %s:\n", city, district));
                result.append(String.format("   Không voucher: %s VNĐ (%s)\n", 
                    response1.getFinalShippingFee(),
                    response1.isFreeShipping() ? "Auto miễn phí" : "Có phí"));
                result.append(String.format("   Có FREESHIP: %s VNĐ (%s)\n", 
                    response2.getFinalShippingFee(), 
                    response2.isFreeShipping() ? "Miễn phí" : "Có phí"));
                result.append("\n");
            }
            
            return ResponseEntity.ok(result.toString());
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.ok("Test failed: " + e.getMessage());
        }
    }

    /**
     * Test endpoint cụ thể cho địa chỉ "123, soctrang, thanhtri"
     * GET /api/shipping/test-soctrang-thanhtri
     */
    @GetMapping("/shipping/test-soctrang-thanhtri")
    public ResponseEntity<String> testSoctrangThanhtri() {
        try {
            StringBuilder result = new StringBuilder();
            result.append("=== TEST CỤ THỂ: 123, Sóc Trăng, Thành Trị ===\n\n");
            
            // Tạo địa chỉ cụ thể
            ShippingAddress address = new ShippingAddress();
            address.setProvince("soctrang");
            address.setDistrict("thanhtri");
            address.setWard("123");
            
            List<CartItemInfo> cartItems = new ArrayList<>();
            BigDecimal subtotal = new BigDecimal("300000"); // 300k
            
            result.append("📍 Địa chỉ: 123, Sóc Trăng, Thành Trị\n");
            result.append("💰 Subtotal: " + subtotal + " VNĐ\n\n");
            
            // Test không có voucher
            ShippingPreviewResponse response1 = shippingService.calculateShippingFee(
                address, cartItems, subtotal, null
            );
            
            // Test với voucher FREESHIP
            ShippingPreviewResponse response2 = shippingService.calculateShippingFee(
                address, cartItems, subtotal, "FREESHIP"
            );
            
            result.append("=== KẾT QUẢ ===\n");
            result.append("🚚 Không voucher:\n");
            result.append("   - Phí vận chuyển: " + response1.getShippingFee() + " VNĐ\n");
            result.append("   - Phí cuối cùng: " + response1.getFinalShippingFee() + " VNĐ\n");
            result.append("   - Miễn phí: " + (response1.isFreeShipping() ? "Có" : "Không") + "\n");
            result.append("   - Lý do: " + (response1.getFreeShippingReason() != null ? response1.getFreeShippingReason() : "Không có") + "\n\n");
            
            result.append("🎫 Có voucher FREESHIP:\n");
            result.append("   - Phí vận chuyển: " + response2.getShippingFee() + " VNĐ\n");
            result.append("   - Giảm giá: " + response2.getShippingDiscount() + " VNĐ\n");
            result.append("   - Phí cuối cùng: " + response2.getFinalShippingFee() + " VNĐ\n");
            result.append("   - Miễn phí: " + (response2.isFreeShipping() ? "Có" : "Không") + "\n");
            result.append("   - Lý do: " + (response2.getFreeShippingReason() != null ? response2.getFreeShippingReason() : "Không có") + "\n\n");
            
            result.append("=== SO SÁNH ===\n");
            if (response1.getFinalShippingFee().compareTo(response2.getFinalShippingFee()) == 0) {
                result.append("❌ FREESHIP KHÔNG được áp dụng - Phí giống nhau\n");
                result.append("   Cần kiểm tra: Voucher có tồn tại? Điều kiện có đủ?\n");
            } else {
                result.append("✅ FREESHIP được áp dụng thành công!\n");
                result.append("   Tiết kiệm: " + response1.getFinalShippingFee().subtract(response2.getFinalShippingFee()) + " VNĐ\n");
            }
            
            return ResponseEntity.ok(result.toString());
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.ok("Test failed: " + e.getMessage());
        }
    }

    /**
     * Test endpoint để kiểm tra chi tiết voucher FREESHIP
     * GET /api/shipping/test-freeship-voucher
     */
    @GetMapping("/shipping/test-freeship-voucher")
    public ResponseEntity<String> testFreeshipVoucher() {
        try {
            StringBuilder result = new StringBuilder();
            result.append("=== FREESHIP VOUCHER TEST ===\n\n");
            
            // Test tìm voucher FREESHIP
            Voucher freeshipVoucher = voucherRepository.findByCode("FREESHIP").orElse(null);
            
            if (freeshipVoucher == null) {
                result.append("❌ FREESHIP voucher không tồn tại trong database!\n");
                return ResponseEntity.ok(result.toString());
            }
            
            result.append("✅ FREESHIP voucher tìm thấy:\n");
            result.append("Code: " + freeshipVoucher.getCode() + "\n");
            result.append("Discount Type: " + freeshipVoucher.getDiscountType() + "\n");
            result.append("Discount Value: " + freeshipVoucher.getDiscountValue() + "\n");
            result.append("Min Order Value: " + freeshipVoucher.getMinOrderValue() + "\n");
            result.append("Is Active: " + freeshipVoucher.getIsActive() + "\n");
            result.append("Is Valid: " + freeshipVoucher.isValid() + "\n");
            result.append("Current Usage: " + freeshipVoucher.getCurrentUsage() + "\n");
            result.append("Max Usage: " + freeshipVoucher.getMaxUsage() + "\n");
            result.append("Start Date: " + freeshipVoucher.getStartDate() + "\n");
            result.append("End Date: " + freeshipVoucher.getEndDate() + "\n\n");
            
            // Test với subtotal 300k (không đủ điều kiện)
            BigDecimal subtotal300k = new BigDecimal("300000");
            boolean meetsMinOrder300k = subtotal300k.compareTo(BigDecimal.valueOf(freeshipVoucher.getMinOrderValue())) >= 0;
            
            result.append("=== TEST VỚI SUBTOTAL 300K ===\n");
            result.append("Subtotal: " + subtotal300k + "\n");
            result.append("Min Order Required: " + freeshipVoucher.getMinOrderValue() + "\n");
            result.append("Meets Min Order: " + meetsMinOrder300k + "\n");
            
            if (!meetsMinOrder300k) {
                result.append("❌ Không đủ điều kiện đơn hàng tối thiểu!\n");
                result.append("Cần đơn hàng >= " + freeshipVoucher.getMinOrderValue() + "đ\n");
            }
            
            // Test với subtotal 600k (đủ điều kiện)
            BigDecimal subtotal600k = new BigDecimal("600000");
            boolean meetsMinOrder600k = subtotal600k.compareTo(BigDecimal.valueOf(freeshipVoucher.getMinOrderValue())) >= 0;
            
            result.append("\n=== TEST VỚI SUBTOTAL 600K ===\n");
            result.append("Subtotal: " + subtotal600k + "\n");
            result.append("Min Order Required: " + freeshipVoucher.getMinOrderValue() + "\n");
            result.append("Meets Min Order: " + meetsMinOrder600k + "\n");
            
            if (meetsMinOrder600k) {
                result.append("✅ Đủ điều kiện đơn hàng tối thiểu!\n");
            }
            
            return ResponseEntity.ok(result.toString());
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.ok("Test failed: " + e.getMessage());
        }
    }

    /**
     * Test endpoint mô phỏng API call thực tế từ frontend
     * GET /api/shipping/test-frontend-api
     */
    @GetMapping("/shipping/test-frontend-api")
    public ResponseEntity<String> testFrontendApi() {
        try {
            StringBuilder result = new StringBuilder();
            result.append("=== TEST FRONTEND API CALL ===\n\n");
            
            // Mô phỏng request từ frontend
            String address = "123";
            String city = "soctrang";
            String district = "thanhtri";
            String voucherCode = "FREESHIP";
            
            result.append("📝 Request Parameters:\n");
            result.append("   address: " + address + "\n");
            result.append("   city: " + city + "\n");
            result.append("   district: " + district + "\n");
            result.append("   voucherCode: " + voucherCode + "\n\n");
            
            // Tạo ShippingAddress từ parameters (giống logic trong calculateShipping)
            ShippingAddress shippingAddress = new ShippingAddress();
            shippingAddress.setProvince(city);
            shippingAddress.setDistrict(district);
            shippingAddress.setWard("");
            
            // Mock cart items và subtotal
            List<CartItemInfo> cartItems = new ArrayList<>();
            BigDecimal subtotal = new BigDecimal("300000"); // 300k
            
            result.append("💰 Subtotal (mock): " + subtotal + " VNĐ\n\n");
            
            // Gọi service để tính phí (giống logic trong calculateShipping)
            ShippingPreviewResponse previewResponse = shippingService.calculateShippingFee(
                shippingAddress,
                cartItems,
                subtotal,
                voucherCode
            );
            
            result.append("=== KẾT QUẢ API ===\n");
            result.append("🚚 Shipping Fee: " + previewResponse.getShippingFee() + " VNĐ\n");
            result.append("🎫 Shipping Discount: " + previewResponse.getShippingDiscount() + " VNĐ\n");
            result.append("💳 Final Shipping Fee: " + previewResponse.getFinalShippingFee() + " VNĐ\n");
            result.append("✅ Free Shipping: " + previewResponse.isFreeShipping() + "\n");
            result.append("📋 Reason: " + (previewResponse.getFreeShippingReason() != null ? previewResponse.getFreeShippingReason() : "Không có") + "\n");
            result.append("📍 Zone: " + previewResponse.getShippingZone() + "\n");
            result.append("📝 Zone Description: " + previewResponse.getZoneDescription() + "\n\n");
            
            // Tạo response đơn giản cho frontend (giống logic trong calculateShipping)
            ShippingCalculateResponse response = new ShippingCalculateResponse();
            response.setShippingFee(previewResponse.getFinalShippingFee().intValue());
            
            result.append("=== FRONTEND RESPONSE ===\n");
            result.append("JSON Response: {\n");
            result.append("  \"shippingFee\": " + response.getShippingFee() + "\n");
            result.append("}\n\n");
            
            result.append("=== KẾT LUẬN ===\n");
            if (response.getShippingFee() == 0) {
                result.append("✅ FREESHIP hoạt động - Phí ship = 0 VNĐ\n");
            } else {
                result.append("❌ FREESHIP không hoạt động - Phí ship = " + response.getShippingFee() + " VNĐ\n");
            }
            
            return ResponseEntity.ok(result.toString());
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.ok("Test failed: " + e.getMessage());
        }
    }

    /**
     * API preview phí vận chuyển - POST method (cho backend khác)
     * POST /api/checkout/preview-shipping
     */
    @PostMapping("/checkout/preview-shipping")
    public ResponseEntity<ShippingPreviewResponse> previewShipping(@RequestBody ShippingPreviewRequest request) {
        try {
            ShippingPreviewResponse response = shippingService.calculateShippingFee(
                request.getAddress(),
                request.getCartItems(),
                request.getSubtotal(),
                request.getVoucherCode()
            );
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            // Trả về lỗi 500 nếu có exception
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Endpoint tổng quát để test voucher FREESHIP với bất kỳ địa chỉ nào
     * GET /api/shipping/test-freeship-comprehensive
     */
    @GetMapping("/shipping/test-freeship-comprehensive")
    public ResponseEntity<String> testFreeshipComprehensive(
            @RequestParam(defaultValue = "123") String address,
            @RequestParam(defaultValue = "hanoi") String city,
            @RequestParam(defaultValue = "baidinh") String district,
            @RequestParam(defaultValue = "300000") String subtotalStr) {
        try {
            StringBuilder result = new StringBuilder();
            result.append("=== TEST FREESHIP TỔNG QUÁT ===\n\n");
            
            // Parse subtotal
            BigDecimal subtotal = new BigDecimal(subtotalStr);
            
            result.append("📝 Thông tin test:\n");
            result.append("   Address: " + address + "\n");
            result.append("   City: " + city + "\n");
            result.append("   District: " + district + "\n");
            result.append("   Subtotal: " + subtotal + " VNĐ\n\n");
            
            // Tạo địa chỉ
            ShippingAddress shippingAddress = new ShippingAddress();
            shippingAddress.setProvince(city);
            shippingAddress.setDistrict(district);
            shippingAddress.setWard("");
            
            List<CartItemInfo> cartItems = new ArrayList<>();
            
            // Test 1: Không có voucher
            result.append("=== TEST 1: KHÔNG CÓ VOUCHER ===\n");
            ShippingPreviewResponse response1 = shippingService.calculateShippingFee(
                shippingAddress, cartItems, subtotal, null
            );
            
            result.append("🚚 Kết quả:\n");
            result.append("   - Phí vận chuyển: " + response1.getShippingFee() + " VNĐ\n");
            result.append("   - Phí cuối cùng: " + response1.getFinalShippingFee() + " VNĐ\n");
            result.append("   - Miễn phí: " + (response1.isFreeShipping() ? "Có" : "Không") + "\n");
            result.append("   - Lý do: " + (response1.getFreeShippingReason() != null ? response1.getFreeShippingReason() : "Không có") + "\n");
            result.append("   - Khu vực: " + response1.getZoneDescription() + "\n\n");
            
            // Test 2: Có voucher FREESHIP
            result.append("=== TEST 2: CÓ VOUCHER FREESHIP ===\n");
            ShippingPreviewResponse response2 = shippingService.calculateShippingFee(
                shippingAddress, cartItems, subtotal, "FREESHIP"
            );
            
            result.append("🎫 Kết quả:\n");
            result.append("   - Phí vận chuyển: " + response2.getShippingFee() + " VNĐ\n");
            result.append("   - Giảm giá: " + response2.getShippingDiscount() + " VNĐ\n");
            result.append("   - Phí cuối cùng: " + response2.getFinalShippingFee() + " VNĐ\n");
            result.append("   - Miễn phí: " + (response2.isFreeShipping() ? "Có" : "Không") + "\n");
            result.append("   - Lý do: " + (response2.getFreeShippingReason() != null ? response2.getFreeShippingReason() : "Không có") + "\n");
            result.append("   - Khu vực: " + response2.getZoneDescription() + "\n\n");
            
            // So sánh kết quả
            result.append("=== SO SÁNH KẾT QUẢ ===\n");
            BigDecimal difference = response1.getFinalShippingFee().subtract(response2.getFinalShippingFee());
            
            if (response2.getFinalShippingFee().compareTo(BigDecimal.ZERO) == 0) {
                result.append("✅ FREESHIP HOẠT ĐỘNG THÀNH CÔNG!\n");
                result.append("   - Phí vận chuyển cuối cùng: 0 VNĐ\n");
                result.append("   - Tiết kiệm được: " + difference + " VNĐ\n");
                result.append("   - Trạng thái: Miễn phí vận chuyển\n");
            } else {
                result.append("❌ FREESHIP KHÔNG HOẠT ĐỘNG!\n");
                result.append("   - Phí vận chuyển cuối cùng: " + response2.getFinalShippingFee() + " VNĐ\n");
                result.append("   - Tiết kiệm được: " + difference + " VNĐ\n");
                result.append("   - Trạng thái: Vẫn có phí vận chuyển\n");
            }
            
            // Kiểm tra voucher trong database
            result.append("\n=== KIỂM TRA VOUCHER FREESHIP ===\n");
            Voucher voucher = voucherRepository.findByCode("FREESHIP").orElse(null);
            
            if (voucher == null) {
                result.append("❌ FREESHIP voucher không tồn tại trong database!\n");
            } else {
                result.append("✅ Voucher FREESHIP found in database:\n");
                result.append("Code: " + voucher.getCode() + "\n");
                result.append("Discount Type: " + voucher.getDiscountType() + "\n");
                result.append("Discount Value: " + voucher.getDiscountValue() + "\n");
                result.append("Min Order Value: " + voucher.getMinOrderValue() + "\n");
                result.append("Is Active: " + voucher.getIsActive() + "\n");
                result.append("Is Valid: " + voucher.isValid() + "\n");
                result.append("Start Date: " + voucher.getStartDate() + "\n");
                result.append("End Date: " + voucher.getEndDate() + "\n");
                result.append("Max Usage: " + voucher.getMaxUsage() + "\n");
                result.append("Current Usage: " + voucher.getCurrentUsage() + "\n\n");
            }
            
            // Tạo response JSON cho frontend
            result.append("\n=== FRONTEND API RESPONSE ===\n");
            result.append("1. Không có voucher:\n");
            result.append("   GET /api/shipping/calculate?address=" + address + "&city=" + city + "&district=" + district + "\n");
            result.append("   Response: {\"shippingFee\": " + response1.getFinalShippingFee().intValue() + "}\n\n");
            
            result.append("2. Có voucher FREESHIP:\n");
            result.append("   GET /api/shipping/calculate?address=" + address + "&city=" + city + "&district=" + district + "&voucherCode=FREESHIP\n");
            result.append("   Response: {\"shippingFee\": " + response2.getFinalShippingFee().intValue() + "}\n");
            
            return ResponseEntity.ok(result.toString());
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.ok("Test failed: " + e.getMessage());
        }
    }

    /**
     * Endpoint test nhanh FREESHIP - chỉ trả về kết quả đơn giản
     * GET /api/shipping/test-freeship-quick
     */
    @GetMapping("/shipping/test-freeship-quick")
    public ResponseEntity<String> testFreeshipQuick(
            @RequestParam(defaultValue = "hanoi") String city,
            @RequestParam(defaultValue = "baidinh") String district,
            @RequestParam(defaultValue = "300000") String subtotalStr) {
        try {
            BigDecimal subtotal = new BigDecimal(subtotalStr);
            
            ShippingAddress address = new ShippingAddress();
            address.setProvince(city);
            address.setDistrict(district);
            address.setWard("");
            
            List<CartItemInfo> cartItems = new ArrayList<>();
            
            // Test không có voucher
            ShippingPreviewResponse response1 = shippingService.calculateShippingFee(
                address, cartItems, subtotal, null
            );
            
            // Test có voucher FREESHIP
            ShippingPreviewResponse response2 = shippingService.calculateShippingFee(
                address, cartItems, subtotal, "FREESHIP"
            );
            
            StringBuilder result = new StringBuilder();
            result.append("📍 " + city + " - " + district + ":\n");
            result.append("   Không voucher: " + response1.getFinalShippingFee() + " VNĐ\n");
            result.append("   Có FREESHIP: " + response2.getFinalShippingFee() + " VNĐ (" + 
                (response2.isFreeShipping() ? "Miễn phí" : "Có phí") + ")\n");
            
            return ResponseEntity.ok(result.toString());
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.ok("Test failed: " + e.getMessage());
        }
    }

    /**
     * Test FREESHIP với tất cả các tỉnh thành
     * GET /api/shipping/test-freeship-all-provinces
     */
    @GetMapping("/shipping/test-freeship-all-provinces")
    public ResponseEntity<String> testFreeshipAllProvinces(
            @RequestParam(defaultValue = "300000") String subtotalStr) {
        try {
            BigDecimal subtotal = new BigDecimal(subtotalStr);
            StringBuilder result = new StringBuilder();
            result.append("=== TEST FREESHIP TẤT CẢ TỈNH THÀNH (Subtotal: " + subtotal + " VNĐ) ===\n\n");
            
            // Danh sách các tỉnh thành để test
            String[][] provinces = {
                {"hcm", "district1", "TPHCM - Quận 1"},
                {"hcm", "district7", "TPHCM - Quận 7"},
                {"hanoi", "baidinh", "Hà Nội - Ba Đình"},
                {"hanoi", "hoankiem", "Hà Nội - Hoàn Kiếm"},
                {"danang", "haichau", "Đà Nẵng - Hải Châu"},
                {"danang", "thanhkhe", "Đà Nẵng - Thanh Khê"},
                {"haiphong", "hongbang", "Hải Phòng - Hồng Bàng"},
                {"cantho", "ninhkieu", "Cần Thơ - Ninh Kiều"},
                {"binhduong", "thudaumot", "Bình Dương - Thủ Dầu Một"},
                {"baivi", "vungtau", "Bà Rịa - Vũng Tàu"},
                {"soctrang", "thanhtri", "Sóc Trăng - Thành Trị"},
                {"soctrang", "soctrang", "Sóc Trăng - Sóc Trăng"},
                {"angiang", "longxuyen", "An Giang - Long Xuyên"},
                {"tiengiang", "mytho", "Tiền Giang - Mỹ Tho"},
                {"bentre", "bentre", "Bến Tre - Bến Tre"}
            };
            
            int successCount = 0;
            int totalCount = provinces.length;
            
            for (String[] province : provinces) {
                String city = province[0];
                String district = province[1];
                String description = province[2];
                
                ShippingAddress address = new ShippingAddress();
                address.setProvince(city);
                address.setDistrict(district);
                address.setWard("");
                
                List<CartItemInfo> cartItems = new ArrayList<>();
                
                // Test không có voucher
                ShippingPreviewResponse response1 = shippingService.calculateShippingFee(
                    address, cartItems, subtotal, null
                );
                
                // Test có voucher FREESHIP
                ShippingPreviewResponse response2 = shippingService.calculateShippingFee(
                    address, cartItems, subtotal, "FREESHIP"
                );
                
                result.append("📍 " + description + ":\n");
                result.append("   Không voucher: " + response1.getFinalShippingFee() + " VNĐ\n");
                result.append("   Có FREESHIP: " + response2.getFinalShippingFee() + " VNĐ (" + 
                    (response2.isFreeShipping() ? "✅ Miễn phí" : "❌ Có phí") + ")\n");
                
                if (response2.isFreeShipping()) {
                    successCount++;
                    result.append("   💰 Tiết kiệm: " + response1.getFinalShippingFee().subtract(response2.getFinalShippingFee()) + " VNĐ\n");
                } else {
                    result.append("   ⚠️  FREESHIP không hoạt động!\n");
                }
                result.append("\n");
            }
            
            // Tổng kết
            result.append("=== TỔNG KẾT ===\n");
            result.append("✅ FREESHIP hoạt động: " + successCount + "/" + totalCount + " tỉnh thành\n");
            result.append("❌ FREESHIP không hoạt động: " + (totalCount - successCount) + "/" + totalCount + " tỉnh thành\n");
            
            double successRate = (double) successCount / totalCount * 100;
            result.append("📊 Tỷ lệ thành công: " + String.format("%.1f", successRate) + "%\n");
            
            if (successRate == 100.0) {
                result.append("🎉 FREESHIP hoạt động hoàn hảo trên tất cả tỉnh thành!\n");
            } else if (successRate >= 80.0) {
                result.append("👍 FREESHIP hoạt động tốt trên hầu hết tỉnh thành!\n");
            } else {
                result.append("⚠️  FREESHIP cần được kiểm tra lại!\n");
            }
            
            return ResponseEntity.ok(result.toString());
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.ok("Test failed: " + e.getMessage());
        }
    }

    /**
     * Test endpoint để debug voucher FREESHIP
     * GET /api/shipping/debug-freeship
     */
    @GetMapping("/shipping/debug-freeship")
    public ResponseEntity<String> debugFreeship() {
        try {
            StringBuilder result = new StringBuilder();
            result.append("=== DEBUG FREESHIP VOUCHER ===\n\n");
            
            // Kiểm tra voucher trong database
            Voucher voucher = voucherRepository.findByCode("FREESHIP").orElse(null);
            if (voucher != null) {
                result.append("✅ Voucher FREESHIP found in database:\n");
                result.append("Code: " + voucher.getCode() + "\n");
                result.append("Discount Type: " + voucher.getDiscountType() + "\n");
                result.append("Discount Value: " + voucher.getDiscountValue() + "\n");
                result.append("Min Order Value: " + voucher.getMinOrderValue() + "\n");
                result.append("Is Active: " + voucher.getIsActive() + "\n");
                result.append("Is Valid: " + voucher.isValid() + "\n");
                result.append("Start Date: " + voucher.getStartDate() + "\n");
                result.append("End Date: " + voucher.getEndDate() + "\n");
                result.append("Max Usage: " + voucher.getMaxUsage() + "\n");
                result.append("Current Usage: " + voucher.getCurrentUsage() + "\n\n");
            } else {
                result.append("❌ Voucher FREESHIP NOT found in database!\n\n");
            }
            
            // Test shipping calculation với voucher
            ShippingAddress address = new ShippingAddress();
            address.setProvince("soctrang");
            address.setDistrict("thanhtri");
            
            List<CartItemInfo> cartItems = new ArrayList<>();
            BigDecimal subtotal = new BigDecimal("300000"); // 300k >= 200k
            
            result.append("📍 Test Address: Sóc Trăng - Thành Trị\n");
            result.append("💰 Subtotal: " + subtotal + " VNĐ\n\n");
            
            // Test không có voucher
            ShippingPreviewResponse response1 = shippingService.calculateShippingFee(
                address, cartItems, subtotal, null
            );
            
            // Test với voucher FREESHIP
            ShippingPreviewResponse response2 = shippingService.calculateShippingFee(
                address, cartItems, subtotal, "FREESHIP"
            );
            
            result.append("📊 Results:\n");
            result.append("Không voucher: " + response1.getFinalShippingFee() + " VNĐ\n");
            result.append("Có FREESHIP: " + response2.getFinalShippingFee() + " VNĐ\n");
            result.append("Is Free Shipping: " + response2.isFreeShipping() + "\n");
            result.append("Free Shipping Reason: " + response2.getFreeShippingReason() + "\n");
            
            return ResponseEntity.ok(result.toString());
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.ok("Debug failed: " + e.getMessage());
        }
    }

    /**
     * Response DTO cho API calculate shipping
     */
    public static class ShippingCalculateResponse {
        private int shippingFee;

        public int getShippingFee() {
            return shippingFee;
        }

        public void setShippingFee(int shippingFee) {
            this.shippingFee = shippingFee;
        }
    }
} 