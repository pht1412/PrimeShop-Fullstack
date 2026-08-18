package com.primeshop.payment.method.vnpay;

import org.springframework.stereotype.Component;

@Component
public class VNPayConfig {
    // Cấu hình cứng để đảm bảo không sai sót khi demo
    public static final String VNP_VERSION = "2.1.0";
    public static final String VNP_COMMAND = "pay";
    
    // Terminal ID (Mã Website) - Đã kiểm tra không có khoảng trắng
    public static final String VNP_TMN_CODE = "P8WZY8S4"; 
    
    // Secret Key (Chuỗi bí mật)
    public static final String VNP_HASH_SECRET = "AYAZFPB4UZMSBE23W110JC1WPVMG0DOV";
    
    // URL Sandbox VNPAY
    public static final String VNP_URL = "https://sandbox.vnpayment.vn/paymentv2/vpcpay.html";
    
    // URL Backend xử lý kết quả (Quan trọng: Phải trỏ về Controller của Backend)
    public static final String VNP_RETURN_URL = "https://localhost:8080/api/payment/vnpay/return";
    
    public static final String VNP_LOCALE = "vn";
    public static final String VNP_CURR_CODE = "VND";
    public static final String VNP_IP_ADDR = "127.0.0.1"; // Default cho local
}