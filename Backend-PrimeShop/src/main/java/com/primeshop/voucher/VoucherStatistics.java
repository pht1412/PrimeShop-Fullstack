package com.primeshop.voucher;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VoucherStatistics {
    
    private Long totalActiveVouchers;
    private Long expiringSoonCount;
    private List<VoucherResponse> nearUsageLimitVouchers;
    
    // Thêm các thống kê khác nếu cần
    private Long totalVouchers;
    private Long expiredVouchers;
    private Long usedVouchers;
    private Double totalDiscountAmount;
} 