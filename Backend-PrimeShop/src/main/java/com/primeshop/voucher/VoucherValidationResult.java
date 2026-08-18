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
public class VoucherValidationResult {
    private boolean valid;
    private String message;
    private Double discountAmount;
    private Voucher voucher; // Giữ lại cho backward compatibility
    private List<Voucher> vouchers; // Thêm field mới cho multiple vouchers
} 