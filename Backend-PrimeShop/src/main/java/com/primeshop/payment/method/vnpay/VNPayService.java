package com.primeshop.payment.method.vnpay;

import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.*;
import org.springframework.stereotype.Service;

import com.primeshop.order.OrderRepo;
import com.primeshop.order.OrderStatus;
import com.primeshop.payment.transaction.PaymentTransaction;
import com.primeshop.payment.transaction.PaymentTransactionRepo;
import com.primeshop.utils.SecurityUtils;
import com.primeshop.utils.VNPayUtil;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class VNPayService {
    
    private final VNPayUtil vnPayUtil;
    private final PaymentTransactionRepo paymentTransactionRepo;
    // Đã loại bỏ Environment env vì không cần thiết nữa
    private final OrderRepo orderRepo;
    private final SecurityUtils securityUtils;

    public String createPaymentUrl(PaymentRequest request) throws UnsupportedEncodingException {
        // Lấy trực tiếp từ Config Class để tránh lỗi "Invisible Space"
        String vnp_TmnCode = VNPayConfig.VNP_TMN_CODE;
        String vnp_Url = VNPayConfig.VNP_URL;
        String vnp_ReturnUrl = VNPayConfig.VNP_RETURN_URL;
        String secretKey = VNPayConfig.VNP_HASH_SECRET;

        Map<String, String> vnpParams = new HashMap<>();
        vnpParams.put("vnp_Version", VNPayConfig.VNP_VERSION);
        vnpParams.put("vnp_Command", VNPayConfig.VNP_COMMAND);
        vnpParams.put("vnp_TmnCode", vnp_TmnCode);
        
        // Tính toán số tiền (VNPAY yêu cầu nhân 100)
        vnpParams.put("vnp_Amount", String.valueOf(request.getAmount().multiply(new BigDecimal(100)).longValue()));
        
        vnpParams.put("vnp_CurrCode", VNPayConfig.VNP_CURR_CODE);
        vnpParams.put("vnp_TxnRef", String.valueOf(request.getOrderId()));
        vnpParams.put("vnp_OrderInfo", "Thanh toan don hang #" + request.getOrderId());
        vnpParams.put("vnp_OrderType", "billpayment");
        vnpParams.put("vnp_Locale", VNPayConfig.VNP_LOCALE);
        vnpParams.put("vnp_ReturnUrl", vnp_ReturnUrl);
        vnpParams.put("vnp_IpAddr", VNPayConfig.VNP_IP_ADDR);
        
        // Thời gian tạo
        vnpParams.put("vnp_CreateDate", LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss")));

        // Build Query & Hash
        String query = vnPayUtil.buildQuery(vnpParams);
        String hashData = vnPayUtil.hmacSHA512(secretKey, query);

        String paymentUrl = vnp_Url + "?" + query + "&vnp_SecureHash=" + hashData;

        // Lưu log giao dịch vào DB
        Long userId = securityUtils.getCurrentUserId();
        PaymentTransaction tx = new PaymentTransaction();
        tx.setUserId(userId);
        tx.setOrderId(String.valueOf(request.getOrderId()));
        tx.setAmount(request.getAmount());
        tx.setPaymentMethod("VNPAY");
        tx.setStatus("INITIATED");
        paymentTransactionRepo.save(tx);

        return paymentUrl;
    }
    
    public PaymentCallbackResult handleReturn(Map<String, String> vnpParams) {
        String receivedHash = vnpParams.get("vnp_SecureHash");
        if (receivedHash == null) {
             return PaymentCallbackResult.invalid("Thiếu chữ ký xác thực");
        }

        // 1. Tạo TreeMap để sắp xếp tham số theo A-Z
        Map<String, String> fields = new TreeMap<>(vnpParams);
        
        // 2. Xóa các trường không tham gia tạo hash
        fields.remove("vnp_SecureHash");
        fields.remove("vnp_SecureHashType");

        // 3. Tái tạo chuỗi dữ liệu (Standard VNPAY Logic)
        StringBuilder hashData = new StringBuilder();
        Iterator<String> itr = fields.keySet().iterator();
        
        try {
            while (itr.hasNext()) {
                String fieldName = itr.next();
                String fieldValue = fields.get(fieldName);
                if ((fieldValue != null) && (fieldValue.length() > 0)) {
                    // Build: key=encodedValue
                    hashData.append(fieldName);
                    hashData.append('=');
                    
                    // 🔴 QUAN TRỌNG: Phải dùng UTF-8 chuẩn thay vì ASCII
                    hashData.append(URLEncoder.encode(fieldValue, StandardCharsets.UTF_8.toString()));
                    
                    if (itr.hasNext()) {
                        hashData.append('&');
                    }
                }
            }
        } catch (UnsupportedEncodingException e) {
            return PaymentCallbackResult.failed("Lỗi encode dữ liệu: " + e.getMessage());
        }

        // 4. Hash và so sánh
        String secretKey = VNPayConfig.VNP_HASH_SECRET;
        String expectedHash = vnPayUtil.hmacSHA512(secretKey, hashData.toString());

        // DEBUG: In ra để kiểm tra nếu vẫn lỗi (Xóa sau khi fix xong)
        // System.out.println("--- DEBUG CHECK ---");
        // System.out.println("Hash String: " + hashData.toString());
        // System.out.println("My Hash    : " + expectedHash);
        // System.out.println("VNP Hash   : " + receivedHash);

        if (!expectedHash.equals(receivedHash)) {
            // Logic xử lý thất bại giữ nguyên...
            String orderId = vnpParams.get("vnp_TxnRef");
            PaymentTransaction tx = paymentTransactionRepo.findFirstByOrderIdOrderByCreatedAtDesc(orderId).orElse(null);
            if (tx != null) {
                tx.setStatus("FAILED");
                tx.setResponseCode("INVALID_HASH");
                paymentTransactionRepo.save(tx);
                orderRepo.findById(Long.parseLong(orderId)).ifPresent(order -> {
                    order.setStatus(OrderStatus.PAYMENT_FAILED);
                    orderRepo.save(order);
                });
            }
            return PaymentCallbackResult.invalid("Chữ ký không hợp lệ");
        }

        // ... Phần logic xử lý thành công (vnp_ResponseCode == 00) ở dưới giữ nguyên ...
        String responseCode = vnpParams.get("vnp_ResponseCode");
        String orderId = vnpParams.get("vnp_TxnRef");
        PaymentTransaction tx = paymentTransactionRepo.findFirstByOrderIdOrderByCreatedAtDesc(orderId).orElse(null);
        
        // (Copy lại đoạn logic save DB cũ của bạn vào đây cho đầy đủ)
        if ("00".equals(responseCode)) {
            if (tx != null) {
                tx.setStatus("SUCCESS");
                tx.setTransactionCode(vnpParams.get("vnp_TransactionNo"));
                tx.setResponseCode("00");
                paymentTransactionRepo.save(tx);
            }
            orderRepo.findById(Long.parseLong(orderId)).ifPresent(order -> {
                order.setStatus(OrderStatus.PAID);
                orderRepo.save(order);
            });
            return PaymentCallbackResult.success("Thanh toán thành công");
        } else {
            // Xử lý lỗi từ ngân hàng
            if (tx != null) {
                tx.setStatus("FAILED");
                tx.setResponseCode(responseCode);
                paymentTransactionRepo.save(tx);
            }
             orderRepo.findById(Long.parseLong(orderId)).ifPresent(order -> {
                order.setStatus(OrderStatus.PAYMENT_FAILED);
                orderRepo.save(order);
            });
            return PaymentCallbackResult.failed("Thanh toán thất bại");
        }
    }
}