package com.primeshop.shipping;

import com.primeshop.voucher.Voucher;
import com.primeshop.voucher.VoucherRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class ShippingService {

    @Autowired
    private VoucherRepository voucherRepository;

    // Danh sách các quận nội thành TPHCM
    private static final Set<String> HCMC_INNER_DISTRICTS = Set.of(
        "Quận 1", "Quận 2", "Quận 3", "Quận 4", "Quận 5", "Quận 6", "Quận 7", "Quận 8", "Quận 9", "Quận 10", "Quận 11", "Quận 12",
        "Quận Bình Tân", "Quận Bình Thạnh", "Quận Gò Vấp", "Quận Phú Nhuận", "Quận Tân Bình", "Quận Tân Phú", "Quận Thủ Đức"
    );

    // Các tỉnh/thành phố khác TPHCM
    private static final Set<String> OTHER_PROVINCES = Set.of(
        "An Giang", "Bà Rịa - Vũng Tàu", "Bắc Giang", "Bắc Kạn", "Bạc Liêu", "Bắc Ninh", "Bến Tre", "Bình Định", "Bình Dương", 
        "Bình Phước", "Bình Thuận", "Cà Mau", "Cần Thơ", "Cao Bằng", "Đà Nẵng", "Đắk Lắk", "Đắk Nông", "Điện Biên", "Đồng Nai", 
        "Đồng Tháp", "Gia Lai", "Hà Giang", "Hà Nam", "Hà Nội", "Hà Tĩnh", "Hải Dương", "Hải Phòng", "Hậu Giang", "Hòa Bình", 
        "Hưng Yên", "Khánh Hòa", "Kiên Giang", "Kon Tum", "Lai Châu", "Lâm Đồng", "Lạng Sơn", "Lào Cai", "Long An", "Nam Định", 
        "Nghệ An", "Ninh Bình", "Ninh Thuận", "Phú Thọ", "Phú Yên", "Quảng Bình", "Quảng Nam", "Quảng Ngãi", "Quảng Ninh", 
        "Quảng Trị", "Sóc Trăng", "Sơn La", "Tây Ninh", "Thái Bình", "Thái Nguyên", "Thanh Hóa", "Thừa Thiên Huế", "Tiền Giang", 
        "Trà Vinh", "Tuyên Quang", "Vĩnh Long", "Vĩnh Phúc", "Yên Bái"
    );

    // Phí vận chuyển cơ bản
    private static final BigDecimal HCMC_INNER_FEE = new BigDecimal("20000");
    private static final BigDecimal HCMC_OUTER_FEE = new BigDecimal("35000");
    private static final BigDecimal OTHER_PROVINCES_FEE = new BigDecimal("50000");
    
    // Phí phụ thu khi có nhiều sản phẩm
    private static final BigDecimal EXTRA_ITEMS_FEE = new BigDecimal("10000");
    private static final int EXTRA_ITEMS_THRESHOLD = 5;
    
    // Ngưỡng miễn phí ship - CẬP NHẬT LÊN 2,000,000 VNĐ
    private static final BigDecimal FREE_SHIPPING_THRESHOLD = new BigDecimal("2000000");

    /**
     * Tính phí vận chuyển dựa trên địa chỉ và thông tin giỏ hàng
     */
    public ShippingPreviewResponse calculateShippingFee(ShippingAddress address, 
                                                       List<CartItemInfo> cartItems, 
                                                       BigDecimal subtotal, 
                                                       String voucherCode) {
        
        System.out.println("=== SHIPPING CALCULATION DEBUG ===");
        System.out.println("Address: " + (address != null ? address.getProvince() + " - " + address.getDistrict() : "null"));
        System.out.println("Subtotal: " + subtotal);
        System.out.println("VoucherCode: " + voucherCode);
        
        // Xác định khu vực giao hàng
        ShippingZone zone = determineShippingZone(address);
        System.out.println("Shipping Zone: " + zone);
        
        // Tính phí vận chuyển cơ bản
        BigDecimal baseFee = getBaseShippingFee(zone);
        System.out.println("Base Fee: " + baseFee);
        
        // Tính phí phụ thu cho số lượng sản phẩm
        BigDecimal extraItemsFee = calculateExtraItemsFee(cartItems);
        System.out.println("Extra Items Fee: " + extraItemsFee);
        
        // Tổng phí vận chuyển trước giảm
        BigDecimal totalShippingFee = baseFee.add(extraItemsFee);
        System.out.println("Total Shipping Fee (before discount): " + totalShippingFee);
        
        // Kiểm tra điều kiện miễn phí ship
        boolean isFreeShipping = false;
        String freeShippingReason = null;
        BigDecimal shippingDiscount = BigDecimal.ZERO;
        
        // Kiểm tra subtotal >= 2,000,000đ (chỉ khi có subtotal > 0)
        if (subtotal != null && subtotal.compareTo(BigDecimal.ZERO) > 0 && 
            subtotal.compareTo(FREE_SHIPPING_THRESHOLD) >= 0) {
            isFreeShipping = true;
            freeShippingReason = "Đơn hàng từ " + FREE_SHIPPING_THRESHOLD + "đ";
            shippingDiscount = totalShippingFee;
            System.out.println("Free shipping due to subtotal >= " + FREE_SHIPPING_THRESHOLD);
        }
        
        // Kiểm tra voucher FREESHIP (ưu tiên sau cùng)
        if (!isFreeShipping && voucherCode != null && !voucherCode.trim().isEmpty()) {
            try {
                Voucher voucher = voucherRepository.findByCode(voucherCode.trim()).orElse(null);
                if (voucher != null && voucher.isValid()) {
                    // Kiểm tra loại voucher (chỉ áp dụng FIXED cho FREESHIP)
                    if (voucher.getDiscountType() == Voucher.DiscountType.FIXED) {
                        // Kiểm tra điều kiện đơn hàng tối thiểu (nếu có)
                        boolean meetsMinOrder = true;
                        if (voucher.getMinOrderValue() != null && subtotal != null) {
                            meetsMinOrder = subtotal.compareTo(BigDecimal.valueOf(voucher.getMinOrderValue())) >= 0;
                        }
                        
                        // Nếu đáp ứng điều kiện, áp dụng FREESHIP
                        if (meetsMinOrder) {
                            isFreeShipping = true;
                            freeShippingReason = "Mã giảm giá FREESHIP";
                            shippingDiscount = totalShippingFee;
                            
                            System.out.println("FREESHIP voucher applied successfully:");
                            System.out.println("Voucher code: " + voucher.getCode());
                            System.out.println("Discount value: " + voucher.getDiscountValue());
                            System.out.println("Shipping fee: " + totalShippingFee);
                            System.out.println("Province: " + address.getProvince());
                            System.out.println("District: " + address.getDistrict());
                        } else {
                            System.out.println("FREESHIP voucher not applied - minimum order not met:");
                            System.out.println("Required: " + voucher.getMinOrderValue());
                            System.out.println("Current: " + subtotal);
                        }
                    } else {
                        System.out.println("FREESHIP voucher not applied - wrong discount type: " + voucher.getDiscountType());
                    }
                } else {
                    System.out.println("FREESHIP voucher not found or invalid: " + voucherCode);
                }
            } catch (Exception e) {
                // Log error nhưng không làm crash
                System.err.println("Error checking voucher: " + e.getMessage());
                e.printStackTrace();
            }
        }
        
        // Tính phí vận chuyển cuối cùng
        BigDecimal finalShippingFee = isFreeShipping ? BigDecimal.ZERO : totalShippingFee;
        System.out.println("Final Shipping Fee: " + finalShippingFee);
        System.out.println("Is Free Shipping: " + isFreeShipping);
        System.out.println("Free Shipping Reason: " + freeShippingReason);
        System.out.println("=== END DEBUG ===\n");
        
        // Tạo response
        ShippingPreviewResponse response = new ShippingPreviewResponse();
        response.setShippingFee(totalShippingFee);
        response.setShippingDiscount(shippingDiscount);
        response.setFinalShippingFee(finalShippingFee);
        response.setShippingZone(zone);
        response.setZoneDescription(zone.getDescription());
        response.setFreeShipping(isFreeShipping);
        response.setFreeShippingReason(freeShippingReason);
        
        return response;
    }

    /**
     * Xác định khu vực giao hàng dựa trên địa chỉ
     */
    private ShippingZone determineShippingZone(ShippingAddress address) {
        if (address == null || address.getProvince() == null) {
            return ShippingZone.OTHER_PROVINCES;
        }
        
        String province = address.getProvince().trim();
        String district = address.getDistrict() != null ? address.getDistrict().trim() : "";
        
        // Kiểm tra có phải TPHCM không (hỗ trợ nhiều cách viết)
        if (province.equalsIgnoreCase("TPHCM") || 
            province.equalsIgnoreCase("Thành phố Hồ Chí Minh") ||
            province.equalsIgnoreCase("Hồ Chí Minh") ||
            province.equalsIgnoreCase("hcm")) {
            
            // Kiểm tra có phải quận nội thành không
            if (HCMC_INNER_DISTRICTS.stream()
                    .anyMatch(innerDistrict -> district.equalsIgnoreCase(innerDistrict) || 
                                             district.equalsIgnoreCase("district" + innerDistrict.replace("Quận ", "")))) {
                return ShippingZone.HCMC_INNER;
            } else {
                return ShippingZone.HCMC_OUTER;
            }
        }
        
        // Kiểm tra các thành phố lớn khác (có thể áp dụng phí thấp hơn)
        if (province.equalsIgnoreCase("Hà Nội") || 
            province.equalsIgnoreCase("hanoi") ||
            province.equalsIgnoreCase("Đà Nẵng") ||
            province.equalsIgnoreCase("danang") ||
            province.equalsIgnoreCase("Hải Phòng") ||
            province.equalsIgnoreCase("haiphong") ||
            province.equalsIgnoreCase("Cần Thơ") ||
            province.equalsIgnoreCase("cantho") ||
            province.equalsIgnoreCase("Bình Dương") ||
            province.equalsIgnoreCase("binhduong") ||
            province.equalsIgnoreCase("Bà Rịa - Vũng Tàu") ||
            province.equalsIgnoreCase("baivi") ||
            province.equalsIgnoreCase("Sóc Trăng") ||
            province.equalsIgnoreCase("socTrang")) {
            // Có thể áp dụng phí trung bình cho các thành phố lớn
            return ShippingZone.OTHER_PROVINCES;
        }
        
        return ShippingZone.OTHER_PROVINCES;
    }

    /**
     * Lấy phí vận chuyển cơ bản theo khu vực
     */
    private BigDecimal getBaseShippingFee(ShippingZone zone) {
        switch (zone) {
            case HCMC_INNER:
                return HCMC_INNER_FEE;
            case HCMC_OUTER:
                return HCMC_OUTER_FEE;
            case OTHER_PROVINCES:
                return OTHER_PROVINCES_FEE;
            default:
                return OTHER_PROVINCES_FEE;
        }
    }

    /**
     * Tính phí phụ thu cho số lượng sản phẩm
     */
    private BigDecimal calculateExtraItemsFee(List<CartItemInfo> cartItems) {
        if (cartItems == null || cartItems.isEmpty()) {
            return BigDecimal.ZERO;
        }
        
        int totalQuantity = cartItems.stream()
                .mapToInt(CartItemInfo::getQuantity)
                .sum();
        
        if (totalQuantity > EXTRA_ITEMS_THRESHOLD) {
            return EXTRA_ITEMS_FEE;
        }
        
        return BigDecimal.ZERO;
    }
} 