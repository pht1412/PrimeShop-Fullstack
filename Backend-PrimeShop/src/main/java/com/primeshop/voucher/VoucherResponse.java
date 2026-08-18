package com.primeshop.voucher;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import com.primeshop.voucher.Voucher;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VoucherResponse {
    private Long id;
    private String code;
    private Voucher.DiscountType discountType;
    private Double discountValue;
    private Double minOrderValue;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private Integer maxUsage;
    private Integer currentUsage;
    private Boolean isActive;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Business logic fields
    private Boolean isValid;
    private Integer remainingUsage;
    private Double calculatedDiscount;

    public VoucherResponse(Voucher voucher) {
        this.id = voucher.getId();
        this.code = voucher.getCode();
        this.discountType = voucher.getDiscountType();
        this.discountValue = voucher.getDiscountValue();
        this.minOrderValue = voucher.getMinOrderValue();
        this.startDate = voucher.getStartDate();
        this.endDate = voucher.getEndDate();
        this.maxUsage = voucher.getMaxUsage();
        this.currentUsage = voucher.getCurrentUsage();
        this.isActive = voucher.getIsActive();
        this.createdAt = voucher.getCreatedAt();
        this.updatedAt = voucher.getUpdatedAt();
        this.isValid = voucher.isValid();
        this.remainingUsage = voucher.getRemainingUsage();
        this.calculatedDiscount = null;
    }

    public static VoucherResponse fromVoucher(Voucher voucher) {
        return VoucherResponse.builder()
                .id(voucher.getId())
                .code(voucher.getCode())
                .discountType(voucher.getDiscountType())
                .discountValue(voucher.getDiscountValue())
                .minOrderValue(voucher.getMinOrderValue())
                .startDate(voucher.getStartDate())
                .endDate(voucher.getEndDate())
                .maxUsage(voucher.getMaxUsage())
                .currentUsage(voucher.getCurrentUsage())
                .isActive(voucher.getIsActive())
                .createdAt(voucher.getCreatedAt())
                .updatedAt(voucher.getUpdatedAt())
                .isValid(voucher.isValid())
                .remainingUsage(voucher.getRemainingUsage())
                .build();
    }

    public static VoucherResponse fromVoucherWithOrderValue(Voucher voucher, Double orderValue) {
        VoucherResponse response = fromVoucher(voucher);
        response.setCalculatedDiscount(voucher.calculateDiscount(orderValue));
        return response;
    }

    public static List<VoucherResponse> fromVouchers(List<Voucher> vouchers) {
        if (vouchers == null || vouchers.isEmpty()) return new ArrayList<>();
        return vouchers.stream().map(VoucherResponse::new).collect(Collectors.toList());
    }
}