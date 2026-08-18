package com.primeshop.payment.method.vnpayinstallment;

import java.util.Map;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.primeshop.installment.InstallmentAgreement;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/payment/vnpay/installment")
@RequiredArgsConstructor
public class VNPayInstallmentController {
    
    private static final Logger logger = LoggerFactory.getLogger(VNPayInstallmentController.class);
    
    private final VNPayInstallmentService installmentService;
    
    /**
     * Tạo thanh toán trả góp VNPay
     */
    @PostMapping("/create")
    public ResponseEntity<?> createInstallmentPayment(@Valid @RequestBody VNPayInstallmentRequest request) {
        try {
            logger.info("Creating VNPay installment payment for order: {}", request.getOrderId());
            
            VNPayInstallmentResponse response = installmentService.createInstallmentPayment(request);
            
            return ResponseEntity.ok(response);
            
        } catch (IllegalArgumentException e) {
            logger.warn("Invalid request for VNPay installment payment: {}", e.getMessage());
            return ResponseEntity.badRequest().body(Map.of(
                "error", "INVALID_REQUEST",
                "message", e.getMessage()
            ));
            
        } catch (IllegalStateException e) {
            logger.warn("Invalid state for VNPay installment payment: {}", e.getMessage());
            return ResponseEntity.badRequest().body(Map.of(
                "error", "INVALID_STATE",
                "message", e.getMessage()
            ));
            
        } catch (Exception e) {
            logger.error("Error creating VNPay installment payment", e);
            return ResponseEntity.internalServerError().body(Map.of(
                "error", "INTERNAL_ERROR",
                "message", "An error occurred while creating installment payment"
            ));
        }
    }
    
    /**
     * Xử lý callback từ VNPay
     */
    @GetMapping("/callback")
    public ResponseEntity<?> handleCallback(@RequestParam Map<String, String> vnpParams) {
        try {
            logger.info("Received VNPay installment callback: {}", vnpParams);
            
            boolean success = installmentService.handleInstallmentCallback(vnpParams);
            
            if (success) {
                return ResponseEntity.ok(Map.of(
                    "status", "success",
                    "message", "Payment processed successfully"
                ));
            } else {
                return ResponseEntity.badRequest().body(Map.of(
                    "status", "failed",
                    "message", "Payment processing failed"
                ));
            }
            
        } catch (Exception e) {
            logger.error("Error handling VNPay installment callback", e);
            return ResponseEntity.internalServerError().body(Map.of(
                "status", "error",
                "message", "An error occurred while processing callback"
            ));
        }
    }
    
    /**
     * Lấy thông tin trả góp theo order ID
     */
    @GetMapping("/agreement/{orderId}")
    public ResponseEntity<?> getInstallmentAgreement(@PathVariable Long orderId) {
        try {
            logger.info("Getting installment agreement for order: {}", orderId);
            
            Optional<InstallmentAgreement> agreement = installmentService.getInstallmentAgreementByOrderId(orderId);
            
            if (agreement.isPresent()) {
                return ResponseEntity.ok(agreement.get());
            } else {
                return ResponseEntity.notFound().build();
            }
            
        } catch (Exception e) {
            logger.error("Error getting installment agreement for order: {}", orderId, e);
            return ResponseEntity.internalServerError().body(Map.of(
                "error", "INTERNAL_ERROR",
                "message", "An error occurred while getting installment agreement"
            ));
        }
    }
    
    /**
     * Kiểm tra trạng thái thanh toán trả góp
     */
    @GetMapping("/status/{orderId}")
    public ResponseEntity<?> getPaymentStatus(@PathVariable Long orderId) {
        try {
            logger.info("Getting payment status for order: {}", orderId);
            
            Optional<InstallmentAgreement> agreement = installmentService.getInstallmentAgreementByOrderId(orderId);
            
            if (agreement.isPresent()) {
                return ResponseEntity.ok(Map.of(
                    "orderId", orderId,
                    "status", agreement.get().getStatus(),
                    "amount", agreement.get().getAmount(),
                    "months", agreement.get().getMonths(),
                    "interestRate", agreement.get().getAnnualRate(),
                    "createdAt", agreement.get().getCreatedAt(),
                    "updatedAt", agreement.get().getUpdatedAt()
                ));
            } else {
                return ResponseEntity.notFound().build();
            }
            
        } catch (Exception e) {
            logger.error("Error getting payment status for order: {}", orderId, e);
            return ResponseEntity.internalServerError().body(Map.of(
                "error", "INTERNAL_ERROR",
                "message", "An error occurred while getting payment status"
            ));
        }
    }
}
