package com.primeshop.payment.method.vnpayinstallment;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Component;
import com.primeshop.order.Order;
import com.primeshop.order.OrderRepo;
import com.primeshop.order.OrderStatus;
import com.primeshop.user.User;

import lombok.RequiredArgsConstructor;

/**
 * Validator cho VNPay Installment
 * Tách biệt logic validation để dễ test và maintain
 */
@Component
@RequiredArgsConstructor
public class VNPayInstallmentValidator {
    
    private final VNPayInstallmentConfig installmentConfig;
    private final OrderRepo orderRepo;
    
    /**
     * Validate VNPay Installment Request
     */
    public ValidationResult validateInstallmentRequest(VNPayInstallmentRequest request) {
        List<String> errors = new ArrayList<>();
        
        Order order = orderRepo.findById(request.getOrderId())
            .orElseThrow(() -> new IllegalArgumentException("Order not found: " + request.getOrderId()));

        // Validate basic fields
        if (request.getOrderId() == null || request.getOrderId() <= 0) {
            errors.add("Order ID must be a positive number");
        }
        
        if (order.getTotalAmount() == null || order.getTotalAmount().compareTo(BigDecimal.ZERO) <= 0) {
            errors.add("Amount must be greater than 0");
        }
        
        if (request.getInstallmentMonths() == null || request.getInstallmentMonths() <= 0) {
            errors.add("Installment months must be greater than 0");
        }
        
        // Validate installment months
        if (request.getInstallmentMonths() != null && 
            !installmentConfig.isValidInstallmentMonths(request.getInstallmentMonths())) {
            errors.add(String.format("Installment months must be between %d and %d",
                installmentConfig.getMinInstallmentMonths(),
                installmentConfig.getMaxInstallmentMonths()));
        }
        
        // Validate amount
        if (order.getTotalAmount() != null && 
            !installmentConfig.isValidAmount(order.getTotalAmount())) {
            errors.add(String.format("Amount must be between %s and %s VND",
                installmentConfig.getMinAmount(),
                installmentConfig.getMaxAmount()));
        }
        
        // Validate order
        if (request.getOrderId() != null) {
            ValidationResult orderValidation = validateOrder(request.getOrderId());
            if (!orderValidation.isValid()) {
                errors.addAll(orderValidation.getErrors());
            }
        }
        
        return new ValidationResult(errors.isEmpty(), errors);
    }
    
    /**
     * Validate order for installment
     */
    public ValidationResult validateOrder(Long orderId) {
        List<String> errors = new ArrayList<>();
        
        if (orderId == null) {
            errors.add("Order ID is required");
            return new ValidationResult(false, errors);
        }
        
        // Check if order exists
        if (!orderRepo.existsById(orderId)) {
            errors.add("Order not found: " + orderId);
            return new ValidationResult(false, errors);
        }
        
        // Get order details
        Order order = orderRepo.findById(orderId).orElse(null);
        if (order == null) {
            errors.add("Order not found: " + orderId);
            return new ValidationResult(false, errors);
        }
        
        // Check order status
        if (order.getStatus() != OrderStatus.CONFIRMED) {
            errors.add("Order must be confirmed before creating installment payment. Current status: " + order.getStatus());
        }
        
        // Check if order is deleted
        if (order.isDeleted()) {
            errors.add("Cannot create installment payment for deleted order");
        }
        
        // Check if order already has installment agreement
        // This would need to be implemented based on your business logic
        
        return new ValidationResult(errors.isEmpty(), errors);
    }
    
    /**
     * Validate user eligibility for installment
     */
    public ValidationResult validateUserEligibility(User user, BigDecimal amount) {
        List<String> errors = new ArrayList<>();
        
        if (user == null) {
            errors.add("User is required");
            return new ValidationResult(false, errors);
        }
        
        // Check if user is enabled
        if (!user.isEnabled()) {
            errors.add("User account is not enabled");
        }
        
        // Check user's installment history
        // This would need to be implemented based on your business logic
        // For example: check if user has too many active installments
        
        // Check user's credit limit
        // This would need to be implemented based on your business logic
        
        return new ValidationResult(errors.isEmpty(), errors);
    }
    
    /**
     * Validate installment amount calculation
     */
    public ValidationResult validateAmountCalculation(BigDecimal principal, BigDecimal interestRate, 
                                                    Integer months, BigDecimal calculatedMonthly) {
        List<String> errors = new ArrayList<>();
        
        if (principal == null || principal.compareTo(BigDecimal.ZERO) <= 0) {
            errors.add("Principal amount must be greater than 0");
        }
        
        if (interestRate == null || interestRate.compareTo(BigDecimal.ZERO) < 0) {
            errors.add("Interest rate must be non-negative");
        }
        
        if (months == null || months <= 0) {
            errors.add("Installment months must be greater than 0");
        }
        
        if (calculatedMonthly == null || calculatedMonthly.compareTo(BigDecimal.ZERO) <= 0) {
            errors.add("Calculated monthly payment must be greater than 0");
        }
        
        // Validate calculation logic
        if (principal != null && interestRate != null && months != null && calculatedMonthly != null) {
            BigDecimal totalPayment = calculatedMonthly.multiply(BigDecimal.valueOf(months));
            BigDecimal maxAllowedTotal = principal.multiply(new BigDecimal("2.0")); // Max 200% of principal
            
            if (totalPayment.compareTo(maxAllowedTotal) > 0) {
                errors.add("Calculated total payment exceeds maximum allowed amount");
            }
        }
        
        return new ValidationResult(errors.isEmpty(), errors);
    }
    
    /**
     * Validation result class
     */
    public static class ValidationResult {
        private final boolean valid;
        private final List<String> errors;
        
        public ValidationResult(boolean valid, List<String> errors) {
            this.valid = valid;
            this.errors = errors != null ? errors : new ArrayList<>();
        }
        
        public boolean isValid() {
            return valid;
        }
        
        public List<String> getErrors() {
            return errors;
        }
        
        public String getErrorMessage() {
            return String.join(", ", errors);
        }
    }
}
