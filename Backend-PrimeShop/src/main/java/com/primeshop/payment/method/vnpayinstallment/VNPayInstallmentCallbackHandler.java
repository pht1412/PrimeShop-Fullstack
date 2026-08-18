package com.primeshop.payment.method.vnpayinstallment;

import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

/**
 * Handler chuyên dụng để xử lý callback từ VNPay cho trả góp
 * Tách biệt logic callback để dễ maintain và test
 */
@Component
@RequiredArgsConstructor
public class VNPayInstallmentCallbackHandler {
    
    private static final Logger logger = LoggerFactory.getLogger(VNPayInstallmentCallbackHandler.class);
    
    private final VNPayInstallmentService installmentService;
    
    /**
     * Xử lý callback từ VNPay cho trả góp
     * @param request HTTP request chứa parameters từ VNPay
     * @param response HTTP response để redirect user
     * @return true nếu xử lý thành công, false nếu có lỗi
     */
    public boolean handleCallback(HttpServletRequest request, HttpServletResponse response) {
        try {
            logger.info("Processing VNPay installment callback");
            
            // Extract parameters từ request
            Map<String, String> vnpParams = extractVnPayParams(request);
            
            // Log parameters để debug (không log sensitive data)
            logCallbackParams(vnpParams);
            
            // Xử lý callback thông qua service
            boolean success = installmentService.handleInstallmentCallback(vnpParams);
            
            if (success) {
                logger.info("VNPay installment callback processed successfully");
                // Redirect user đến trang thành công
                response.sendRedirect("/payment-success?orderId=" + vnpParams.get("vnp_TxnRef"));
                return true;
            } else {
                logger.warn("VNPay installment callback processing failed");
                // Redirect user đến trang thất bại
                response.sendRedirect("/payment-failed?orderId=" + vnpParams.get("vnp_TxnRef"));
                return false;
            }
            
        } catch (Exception e) {
            logger.error("Error processing VNPay installment callback", e);
            try {
                response.sendRedirect("/payment-error");
            } catch (Exception redirectException) {
                logger.error("Error redirecting to error page", redirectException);
            }
            return false;
        }
    }
    
    /**
     * Extract VNPay parameters từ HTTP request
     */
    private Map<String, String> extractVnPayParams(HttpServletRequest request) {
        Map<String, String> params = new java.util.HashMap<>();
        
        // Lấy tất cả parameters từ request
        request.getParameterMap().forEach((key, values) -> {
            if (values.length > 0) {
                params.put(key, values[0]);
            }
        });
        
        return params;
    }
    
    /**
     * Log callback parameters (ẩn sensitive data)
     */
    private void logCallbackParams(Map<String, String> params) {
        Map<String, String> safeParams = new java.util.HashMap<>(params);
        
        // Ẩn sensitive parameters
        safeParams.remove("vnp_SecureHash");
        safeParams.remove("vnp_SecureHashType");
        
        logger.info("VNPay callback parameters: {}", safeParams);
    }
    
    /**
     * Validate callback parameters
     */
    public boolean validateCallbackParams(Map<String, String> params) {
        // Kiểm tra các parameters bắt buộc
        String[] requiredParams = {
            "vnp_TxnRef",
            "vnp_ResponseCode",
            "vnp_TransactionNo",
            "vnp_SecureHash"
        };
        
        for (String param : requiredParams) {
            if (!params.containsKey(param) || params.get(param) == null || params.get(param).isEmpty()) {
                logger.warn("Missing required parameter: {}", param);
                return false;
            }
        }
        
        return true;
    }
    
    /**
     * Xử lý callback với validation
     */
    public boolean handleCallbackWithValidation(HttpServletRequest request, HttpServletResponse response) {
        try {
            Map<String, String> vnpParams = extractVnPayParams(request);
            
            // Validate parameters
            if (!validateCallbackParams(vnpParams)) {
                logger.error("Invalid callback parameters");
                response.sendRedirect("/payment-error?reason=invalid_params");
                return false;
            }
            
            // Xử lý callback
            return handleCallback(request, response);
            
        } catch (Exception e) {
            logger.error("Error in callback validation", e);
            try {
                response.sendRedirect("/payment-error?reason=validation_error");
            } catch (Exception redirectException) {
                logger.error("Error redirecting to error page", redirectException);
            }
            return false;
        }
    }
}
