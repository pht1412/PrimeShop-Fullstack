package com.primeshop.payment.method.vnpayinstallment;

import java.math.BigDecimal;
import java.util.Map;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import lombok.Data;

@Component
@ConfigurationProperties(prefix = "vnpay.installment")
@Data
public class VNPayInstallmentConfig {
    
    // Cấu hình lãi suất theo kỳ hạn (tháng)
    private Map<Integer, BigDecimal> interestRates;
    
    // Số kỳ hạn tối đa
    private Integer maxInstallmentMonths = 24;
    
    // Số kỳ hạn tối thiểu
    private Integer minInstallmentMonths = 3;
    
    // Số tiền tối thiểu để được trả góp (VND)
    private BigDecimal minAmount = new BigDecimal("1000000");
    
    // Số tiền tối đa để được trả góp (VND)
    private BigDecimal maxAmount = new BigDecimal("50000000");
    
    // Thời gian hết hạn thanh toán (phút)
    private Integer paymentTimeoutMinutes = 15;
    
    // Cấu hình VNPay cho trả góp
    private String orderType = "installment";
    private String locale = "vn";
    private String currency = "VND";
    
    /**
     * Lấy lãi suất theo số tháng trả góp
     */
    public BigDecimal getInterestRate(Integer months) {
        if (interestRates == null || interestRates.isEmpty()) {
            // Lãi suất mặc định nếu không có cấu hình
            return new BigDecimal("12.0");
        }
        
        // Tìm lãi suất phù hợp nhất
        BigDecimal rate = interestRates.get(months);
        if (rate != null) {
            return rate;
        }
        
        // Nếu không tìm thấy, lấy lãi suất gần nhất
        for (Map.Entry<Integer, BigDecimal> entry : interestRates.entrySet()) {
            if (entry.getKey() <= months) {
                rate = entry.getValue();
            } else {
                break;
            }
        }
        
        return rate != null ? rate : new BigDecimal("12.0");
    }
    
    /**
     * Kiểm tra số tháng trả góp có hợp lệ không
     */
    public boolean isValidInstallmentMonths(Integer months) {
        return months != null && 
               months >= minInstallmentMonths && 
               months <= maxInstallmentMonths;
    }
    
    /**
     * Kiểm tra số tiền có hợp lệ cho trả góp không
     */
    public boolean isValidAmount(BigDecimal amount) {
        return amount != null && 
               amount.compareTo(minAmount) >= 0 && 
               amount.compareTo(maxAmount) <= 0;
    }
}
