package com.primeshop.voucher;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.primeshop.cart.Cart;
import com.primeshop.cart.CartRepo;
import com.primeshop.voucher.Voucher;
import com.primeshop.voucher.VoucherResponse;
import com.primeshop.voucher.VoucherStatistics;
import com.primeshop.voucher.VoucherValidationResult;
import com.primeshop.voucher.VoucherRepository;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Service
public class VoucherService {
    @Autowired
    private VoucherRepository voucherRepo;
    @Autowired
    private CartRepo cartRepo;

    /**
     * Tìm voucher theo ID
     */
    public Optional<Voucher> findById(Long id) {
        return voucherRepo.findById(id);
    }

    /**
     * Tìm voucher theo mã code
     */
    public Optional<Voucher> findByCode(String code) {
        return voucherRepo.findByCode(code);
    }

    /**
     * Tìm voucher hợp lệ theo mã code
     */
    public Optional<Voucher> findValidVoucherByCode(String code) {
        return voucherRepo.findByCodeAndIsActiveTrue(code);
    }

    /**
     * Kiểm tra voucher có hợp lệ cho đơn hàng không
     */
    public boolean isValidVoucherForOrder(String code, Double orderValue) {
        return voucherRepo.isValidVoucher(code, LocalDateTime.now(), orderValue);
    }

    /**
     * Lấy voucher hợp lệ cho đơn hàng
     */
    public Optional<Voucher> getValidVoucherForOrder(String code, Double orderValue) {
        Optional<Voucher> voucherOpt = voucherRepo.findByCodeAndIsActiveTrue(code);

        if (voucherOpt.isPresent()) {
            Voucher voucher = voucherOpt.get();
            if (voucher.canApplyToOrder(orderValue)) {
                return voucherOpt;
            }
        }

        return Optional.empty();
    }

    /**
     * Tính toán giá trị giảm giá từ voucher
     */
    public Double calculateDiscount(String code, Double orderValue) {
        Optional<Voucher> voucherOpt = getValidVoucherForOrder(code, orderValue);
        return voucherOpt.map(voucher -> voucher.calculateDiscount(orderValue)).orElse(0.0);
    }

    /**
     * Sử dụng voucher (tăng số lượt sử dụng)
     */
    @Transactional
    public boolean useVoucher(Long voucherId) {
        Optional<Voucher> voucherOpt = voucherRepo.findById(voucherId);

        if (voucherOpt.isPresent()) {
            Voucher voucher = voucherOpt.get();
            if (voucher.hasRemainingUsage() && voucher.isValid()) {
                voucher.incrementUsage();
                voucherRepo.save(voucher);
                return true;
            }
        }

        return false;
    }

    /**
     * Sử dụng voucher theo mã code
     */
    @Transactional
    public boolean useVoucherByCode(String code) {
        Optional<Voucher> voucherOpt = voucherRepo.findByCodeAndIsActiveTrue(code);

        if (voucherOpt.isPresent()) {
            Voucher voucher = voucherOpt.get();
            if (voucher.hasRemainingUsage() && voucher.isValid()) {
                voucher.incrementUsage();
                voucherRepo.save(voucher);
                return true;
            }
        }

        return false;
    }

    /**
     * Lấy tất cả voucher đang hoạt động
     */
    public List<Voucher> getAllActiveVouchers() {
        return voucherRepo.findByIsActiveTrue();
    }

    /**
     * Lấy voucher hợp lệ cho đơn hàng
     */
    public List<Voucher> getValidVouchersForOrder(Double orderValue) {
        return voucherRepo.findValidVouchersForOrder(LocalDateTime.now(), orderValue);
    }

    /**
     * Tạo voucher mới
     */
    @Transactional
    public Voucher createVoucher(Voucher voucher) {
        // Kiểm tra mã code đã tồn tại chưa
        if (voucherRepo.findByCode(voucher.getCode()).isPresent()) {
            throw new IllegalArgumentException("Mã voucher đã tồn tại: " + voucher.getCode());
        }

        // Kiểm tra thời gian hợp lệ
        if (voucher.getEndDate().isBefore(voucher.getStartDate())) {
            throw new IllegalArgumentException("Ngày kết thúc phải sau ngày bắt đầu");
        }

        // Kiểm tra giá trị giảm
        if (voucher.getDiscountValue() <= 0) {
            throw new IllegalArgumentException("Giá trị giảm phải lớn hơn 0");
        }

        // Kiểm tra số lượt sử dụng
        if (voucher.getMaxUsage() <= 0) {
            throw new IllegalArgumentException("Số lượt sử dụng tối đa phải lớn hơn 0");
        }

        return voucherRepo.save(voucher);
    }

    /**
     * Cập nhật voucher
     */
    @Transactional
    public Voucher updateVoucher(Long id, Voucher voucherDetails) {
        Optional<Voucher> voucherOpt = voucherRepo.findById(id);

        if (voucherOpt.isPresent()) {
            Voucher voucher = voucherOpt.get();

            // Cập nhật thông tin
            voucher.setCode(voucherDetails.getCode());
            voucher.setDiscountType(voucherDetails.getDiscountType());
            voucher.setDiscountValue(voucherDetails.getDiscountValue());
            voucher.setMinOrderValue(voucherDetails.getMinOrderValue());
            voucher.setStartDate(voucherDetails.getStartDate());
            voucher.setEndDate(voucherDetails.getEndDate());
            voucher.setMaxUsage(voucherDetails.getMaxUsage());
            voucher.setIsActive(voucherDetails.getIsActive()); // SỬA Ở ĐÂY

            return voucherRepo.save(voucher);
        }

        throw new IllegalArgumentException("Voucher không tồn tại với ID: " + id);
    }

    /**
     * Xóa voucher (soft delete)
     */
    @Transactional
    public boolean deactivateVoucher(Long id) {
        Optional<Voucher> voucherOpt = voucherRepo.findById(id);

        if (voucherOpt.isPresent()) {
            Voucher voucher = voucherOpt.get();
            voucher.setIsActive(false); // setter vẫn là setIsActive
            voucherRepo.save(voucher);
            return true;
        }

        return false;
    }

    /**
     * Lấy thống kê voucher
     */
    public VoucherStatistics getVoucherStatistics() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime weekFromNow = now.plusWeeks(1);

        List<Voucher> nearUsageLimitVouchers = voucherRepo.findVouchersNearUsageLimit();
        List<VoucherResponse> nearUsageLimitVoucherResponses = nearUsageLimitVouchers.stream()
            .map(VoucherResponse::fromVoucher)
            .collect(java.util.stream.Collectors.toList());

        return VoucherStatistics.builder()
                .totalActiveVouchers(voucherRepo.countByIsActiveTrue())
                .expiringSoonCount(voucherRepo.countExpiringSoon(now, weekFromNow))
                .nearUsageLimitVouchers(nearUsageLimitVoucherResponses)
                .build();
    }

    /**
     * Lấy voucher theo loại giảm giá
     */
    public List<Voucher> getVouchersByDiscountType(Voucher.DiscountType discountType) {
        return voucherRepo.findByDiscountTypeAndIsActiveTrue(discountType);
    }

    /**
     * Kiểm tra voucher có còn hiệu lực không
     */
    public boolean isVoucherExpired(String code) {
        Optional<Voucher> voucherOpt = voucherRepo.findByCode(code);

        if (voucherOpt.isPresent()) {
            Voucher voucher = voucherOpt.get();
            LocalDateTime now = LocalDateTime.now();
            return now.isAfter(voucher.getEndDate());
        }

        return true; // Không tìm thấy voucher = đã hết hạn
    }

    public Cart applyVoucherToCart(Long cartId, String voucherCode) {
        Cart cart = cartRepo.findById(cartId)
            .orElseThrow(() -> new RuntimeException("Cart not found"));
        Voucher voucher = voucherRepo.findByCode(voucherCode)
            .orElseThrow(() -> new RuntimeException("Voucher not found"));

        // Tính toán lại tổng tiền cart trước khi kiểm tra voucher
        cart.setTotalPrice();
        BigDecimal cartTotal = cart.getTotalAmount();
        
        System.out.println("=== VOUCHER APPLICATION DEBUG ===");
        System.out.println("Cart ID: " + cartId);
        System.out.println("Voucher Code: " + voucherCode);
        System.out.println("Cart Total: " + cartTotal);
        System.out.println("Voucher Min Order Value: " + voucher.getMinOrderValue());
        System.out.println("Voucher Is Active: " + voucher.getIsActive()); // SỬA Ở ĐÂY
        System.out.println("Voucher Is Valid: " + voucher.isValid());

        // Kiểm tra điều kiện
        if (!Boolean.TRUE.equals(voucher.getIsActive())) { // SỬA Ở ĐÂY
            System.out.println("❌ Voucher is inactive");
            throw new RuntimeException("Voucher is inactive");
        }
        
        if (voucher.getExpirationDate() != null && voucher.getExpirationDate().isBefore(LocalDateTime.now())) {
            System.out.println("❌ Voucher expired");
            throw new RuntimeException("Voucher expired");
        }
        
        if (voucher.getMaxUsage() != null && voucher.getCurrentUsage() >= voucher.getMaxUsage()) {
            System.out.println("❌ Voucher usage limit reached");
            throw new RuntimeException("Voucher usage limit reached");
        }
        
        if (voucher.getMinOrderValue() != null && cartTotal.doubleValue() < voucher.getMinOrderValue()) {
            System.out.println("❌ Order value too low for voucher");
            System.out.println("Required: " + voucher.getMinOrderValue());
            System.out.println("Current: " + cartTotal.doubleValue());
            throw new RuntimeException("Order value too low for voucher. Required: " + voucher.getMinOrderValue() + ", Current: " + cartTotal.doubleValue());
        }

        // TODO: kiểm tra applicable_products, applicable_categories nếu cần

        // Tính giảm giá
        double discount = 0;
        if (voucher.getDiscountType() == Voucher.DiscountType.PERCENT) {
            discount = cartTotal.doubleValue() * voucher.getDiscountValue() / 100.0;
            if (voucher.getMaxDiscountValue() != null) {
                discount = Math.min(discount, voucher.getMaxDiscountValue());
            }
        } else {
            discount = voucher.getDiscountValue();
        }
        discount = Math.min(discount, cartTotal.doubleValue()); // Không giảm quá tổng tiền

        System.out.println("✅ Voucher applied successfully");
        System.out.println("Discount Amount: " + discount);
        System.out.println("Final Amount: " + (cartTotal.doubleValue() - discount));
        System.out.println("=== END DEBUG ===");

        // Lưu vào cart
        cart.setVouchers(List.of(voucher));
        cart.setDiscountAmount(BigDecimal.valueOf(discount));
        cart.setFinalAmount(cartTotal.subtract(BigDecimal.valueOf(discount)));
        cartRepo.save(cart);

        return cart;
    }

    public Cart removeVoucherFromCart(Long cartId) {
        Cart cart = cartRepo.findById(cartId)
            .orElseThrow(() -> new RuntimeException("Cart not found"));
        cart.setVouchers(null);
        cart.setDiscountAmount(BigDecimal.ZERO);
        cart.setFinalAmount(cart.getTotalAmount());
        cartRepo.save(cart);
        return cart;
    }

    public VoucherValidationResult validateVoucherWithDetails(String code, Double orderValue) {
        Optional<Voucher> voucherOpt = voucherRepo.findByCodeAndIsActiveTrue(code);
        if (voucherOpt.isEmpty()) {
            return VoucherValidationResult.builder()
                .valid(false)
                .message("Voucher không tồn tại hoặc không hoạt động")
                .discountAmount(0.0)
                .voucher(null)
                .build();
        }
        Voucher voucher = voucherOpt.get();
        if (!voucher.canApplyToOrder(orderValue)) {
            return VoucherValidationResult.builder()
                .valid(false)
                .message("Voucher không áp dụng cho giá trị đơn hàng này")
                .discountAmount(0.0)
                .voucher(voucher)
                .build();
        }
        double discount = voucher.calculateDiscount(orderValue);
        return VoucherValidationResult.builder()
            .valid(true)
            .message("Voucher hợp lệ")
            .discountAmount(discount)
            .voucher(voucher)
            .build();
    }

    public List<Voucher> getAllVouchers() {
        return voucherRepo.findAll();
    }

    public long countAllVouchers() {
        return voucherRepo.count();
    }

    public Voucher save(Voucher voucher) {
        return voucherRepo.save(voucher);
    }

    public Voucher saveAndFlush(Voucher voucher) {
        return voucherRepo.saveAndFlush(voucher);
    }

    @Transactional
    public Map<String, Boolean> useVouchersByCodes(List<String> codes) {
        Map<String, Boolean> result = new HashMap<>();
        for (String code : codes) {
            boolean success = useVoucherByCode(code);
            result.put(code, success);
        }
        return result;
    }

    @Transactional
    public Cart applyVouchersToCart(Long cartId, List<String> voucherCodes) {
        Cart cart = cartRepo.findById(cartId)
            .orElseThrow(() -> new RuntimeException("Cart not found"));

        List<Voucher> vouchers = voucherRepo.findAllByCodeIn(voucherCodes);

        // Kiểm tra hợp lệ từng voucher, tính tổng giảm giá
        double totalDiscount = 0.0;
        for (Voucher voucher : vouchers) {
            if (voucher.isValid() && voucher.canApplyToOrder(cart.getTotalAmount().doubleValue())) {
                totalDiscount += voucher.calculateDiscount(cart.getTotalAmount().doubleValue());
            }
        }
        totalDiscount = Math.min(totalDiscount, cart.getTotalAmount().doubleValue());

        cart.setVouchers(vouchers);
        cart.setDiscountAmount(BigDecimal.valueOf(totalDiscount));
        cart.setFinalAmount(cart.getTotalAmount().subtract(BigDecimal.valueOf(totalDiscount)));
        cartRepo.save(cart);
        return cart;
    }

    /**
     * Xử lý voucher cho đơn hàng mới - LUÔN TĂNG USED_COUNT
     * Method này được gọi từ OrderService khi tạo order
     */
    @Transactional
    public List<Voucher> processVouchersForOrder(List<String> voucherCodes, Double orderValue) {
        List<Voucher> appliedVouchers = new ArrayList<>();
        
        if (voucherCodes == null || voucherCodes.isEmpty()) {
            System.out.println("ℹ️ No voucher codes provided");
            return appliedVouchers;
        }

        System.out.println("🔄 Starting voucher processing for order value: " + orderValue);

        for (String code : voucherCodes) {
            if (code == null || code.trim().isEmpty()) {
                System.out.println("⚠️ Skipping empty voucher code");
                continue;
            }

            String trimmedCode = code.trim();
            System.out.println("🔍 Processing voucher code: " + trimmedCode);

            Optional<Voucher> voucherOpt = voucherRepo.findByCodeAndIsActiveTrue(trimmedCode);
            
            if (voucherOpt.isEmpty()) {
                System.err.println("❌ Voucher not found or inactive: " + trimmedCode);
                throw new RuntimeException("Voucher không tồn tại hoặc không hoạt động: " + trimmedCode);
            }

            Voucher voucher = voucherOpt.get();
            System.out.println("📋 Found voucher: " + voucher.getCode() + 
                             " (current_usage: " + voucher.getCurrentUsage() + 
                             ", max_usage: " + voucher.getMaxUsage() + ")");

            // Validate voucher
            if (!voucher.isValid()) {
                System.err.println("❌ Voucher is not valid: " + trimmedCode);
                throw new RuntimeException("Voucher không hợp lệ: " + trimmedCode);
            }

            if (!voucher.hasRemainingUsage()) {
                System.err.println("❌ Voucher has no remaining usage: " + trimmedCode);
                throw new RuntimeException("Voucher đã hết lượt sử dụng: " + trimmedCode);
            }

            if (!voucher.canApplyToOrder(orderValue)) {
                System.err.println("❌ Voucher cannot be applied to order: " + trimmedCode + 
                                 " (order_value: " + orderValue + ", min_required: " + voucher.getMinOrderValue() + ")");
                throw new RuntimeException("Đơn hàng không đủ điều kiện áp dụng voucher: " + trimmedCode);
            }

            // LUÔN TĂNG USED_COUNT KHI ĐẶT HÀNG
            int beforeUsage = voucher.getCurrentUsage();
            voucher.incrementUsage();
            voucherRepo.saveAndFlush(voucher); // Đảm bảo cập nhật ngay lập tức
            
            System.out.println("✅ Voucher used_count increased for order: " + trimmedCode + 
                             " (" + beforeUsage + " -> " + voucher.getCurrentUsage() + ")");
            
            appliedVouchers.add(voucher);
        }

        System.out.println("🎉 Voucher processing completed. Applied: " + appliedVouchers.size() + " vouchers");
        return appliedVouchers;
    }

    /**
     * Validate và tính toán discount cho nhiều voucher
     */
    public VoucherValidationResult validateMultipleVouchers(List<String> voucherCodes, Double orderValue) {
        List<Voucher> validVouchers = new ArrayList<>();
        double totalDiscount = 0.0;
        List<String> errors = new ArrayList<>();

        for (String code : voucherCodes) {
            if (code == null || code.trim().isEmpty()) {
                continue;
            }

            Optional<Voucher> voucherOpt = voucherRepo.findByCodeAndIsActiveTrue(code.trim());
            
            if (voucherOpt.isEmpty()) {
                errors.add("Voucher không tồn tại: " + code);
                continue;
            }

            Voucher voucher = voucherOpt.get();

            if (!voucher.isValid()) {
                errors.add("Voucher không hợp lệ: " + code);
                continue;
            }

            if (!voucher.hasRemainingUsage()) {
                errors.add("Voucher đã hết lượt sử dụng: " + code);
                continue;
            }

            if (!voucher.canApplyToOrder(orderValue)) {
                errors.add("Đơn hàng không đủ điều kiện áp dụng voucher: " + code);
                continue;
            }

            validVouchers.add(voucher);
            totalDiscount += voucher.calculateDiscount(orderValue);
        }

        boolean isValid = validVouchers.size() == voucherCodes.size() && errors.isEmpty();
        String message = isValid ? "Tất cả voucher hợp lệ" : String.join("; ", errors);

        return VoucherValidationResult.builder()
            .valid(isValid)
            .message(message)
            .discountAmount(totalDiscount)
            .voucher(validVouchers.isEmpty() ? null : validVouchers.get(0)) // Backward compatibility
            .vouchers(validVouchers) // New field for multiple vouchers
            .build();
    }

    /**
     * Sinh voucher minigame cho user (có thể mở rộng gán userId nếu muốn)
     */
    public Voucher createMinigameVoucherForUser(Long userId) {
        String code = "MINIGAME-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        Voucher voucher = new Voucher();
        voucher.setCode(code);
        voucher.setDiscountType(Voucher.DiscountType.FIXED);
        voucher.setDiscountValue(50000.0); // 50k
        voucher.setMinOrderValue(200000.0);
        voucher.setStartDate(java.time.LocalDateTime.now());
        voucher.setEndDate(java.time.LocalDateTime.now().plusDays(7));
        voucher.setMaxUsage(1);
        voucher.setCurrentUsage(0);
        voucher.setIsActive(true);
        voucher.setUserId(userId);
        // Nếu muốn gán cho user, có thể mở rộng thêm trường userId hoặc bảng voucher_user
        return voucherRepo.save(voucher);
    }
    @Transactional
    public Voucher unactivateVoucher(Long id) {
        Optional<Voucher> voucherOpt = voucherRepo.findById(id);

        if (voucherOpt.isPresent()) {
            Voucher voucher = voucherOpt.get();
            voucher.setIsActive(false); // setter vẫn là setIsActive
            return voucherRepo.save(voucher);
        }

        throw new IllegalArgumentException("Voucher không tồn tại với ID: " + id);
    }
    public List<Voucher> getVouchersByUserId(Long userId) {
        return voucherRepo.findByUserId(userId);
    }
}