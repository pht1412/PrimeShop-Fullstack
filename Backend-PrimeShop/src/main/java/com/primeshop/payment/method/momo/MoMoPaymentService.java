package com.primeshop.payment.method.momo;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.primeshop.config.MoMoConfig;
import com.primeshop.order.Order;
import com.primeshop.order.OrderRepo;
import com.primeshop.order.OrderService;
import com.primeshop.order.OrderStatus;
import com.primeshop.payment.transaction.PaymentTransaction;
import com.primeshop.payment.transaction.PaymentTransactionRepo;
import com.primeshop.utils.MoMoSignatureUtils;
import com.primeshop.utils.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.net.HttpURLConnection;
import java.net.URL;
import java.io.*;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class MoMoPaymentService {
    private final MoMoConfig config;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final OrderRepo orderRepo;
    private final OrderService orderService;
    @Value("${frontend.url}")
    private String frontendBaseUrl;
    private final SecurityUtils securityUtils;
    private final PaymentTransactionRepo paymentTransactionRepo;

    public MoMoPaymentResponse createPayment(MoMoRequest momoRequest) throws Exception {
        Order order = orderRepo.findById(momoRequest.getOrderId())
            .orElseThrow(() -> new IllegalStateException("Không tìm thấy đơn hàng"));

        if (order.getStatus() != OrderStatus.CONFIRMED) {
            throw new IllegalStateException("Đơn hàng chưa đạt điều kiện thanh toán");
        }

        String orderId = generateOrderId(order.getId());
        Long amount = order.getTotalAmount().longValue();
        String orderInfo = buildOrderInfo(orderId, order.getFullName());

        String requestId = UUID.randomUUID().toString();
        String rawSignature = "accessKey=" + config.getAccessKey() +
                "&amount=" + amount +
                "&extraData=" +
                "&ipnUrl=" + config.getNotifyUrl() +
                "&orderId=" + orderId +
                "&orderInfo=" + orderInfo +
                "&partnerCode=" + config.getPartnerCode() +
                "&redirectUrl=" + config.getReturnUrl() +
                "&requestId=" + requestId +
                "&requestType=payWithATM";

        String signature = hmacSHA256(rawSignature, config.getSecretKey());

        MoMoPaymentRequest request = MoMoPaymentRequest.builder()
                .partnerCode(config.getPartnerCode())
                .partnerName("PrimeShop")
                .storeId("PrimeShopStore")
                .requestId(requestId)
                .amount(amount.toString())
                .orderId(orderId)
                .orderInfo(orderInfo)
                .redirectUrl(config.getReturnUrl())
                .ipnUrl(config.getNotifyUrl())
                .requestType("payWithATM")
                .signature(signature)
                .lang("vi")
                .autoCapture(true)
                .extraData("")
                .build();

        try {
            PaymentTransaction tx = new PaymentTransaction();
            Long userId = securityUtils.getCurrentUserId();
            tx.setUserId(userId);
            tx.setOrderId(parseOrderId(orderId).toString());
            tx.setAmount(BigDecimal.valueOf(amount));
            tx.setPaymentMethod("MOMO");
            tx.setStatus("INITIATED");
            tx.setTransactionCode(UUID.randomUUID().toString());

            if (paymentTransactionRepo != null) {
                paymentTransactionRepo.save(tx);
            }
        } catch (Exception e) {
            System.err.println("Failed to create PaymentTransaction: " + e.getMessage());
        }

        URL url = new URL(config.getEndpoint());
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setDoOutput(true);
        conn.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
        conn.setRequestMethod("POST");

        try (OutputStream os = conn.getOutputStream()) { 
            os.write(objectMapper.writeValueAsBytes(request));
        }

        try (BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()))) {
            MoMoPaymentResponse response = objectMapper.readValue(br, MoMoPaymentResponse.class);
            return response;
        }
    }

    public String handleReturn(Map<String, String> params) throws Exception {
        String momoOrderId = params.get("orderId");
        if (momoOrderId == null) {
            throw new IllegalArgumentException("Thiếu orderId");
        }

        Long orderId = parseOrderId(momoOrderId);
        Order order = orderRepo.findById(orderId)
            .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy đơn hàng"));

        boolean isValidSignature = MoMoSignatureUtils.isValidSignature(params, config.getSecretKey(), config.getAccessKey());
        if (!isValidSignature) {
            throw new IllegalArgumentException("Sai chữ ký xác thực");
        }

        String resultCode = params.get("resultCode");
        PaymentTransaction tx = paymentTransactionRepo.findFirstByOrderIdOrderByCreatedAtDesc(order.getId().toString()).orElse(null);
        if ("0".equals(resultCode)) {
            orderService.updateOrderStatus(orderId, OrderStatus.PAID);
            if (tx != null) {
                tx.setStatus("SUCCESS");
                tx.setResponseCode("00");
                paymentTransactionRepo.save(tx);
            }
            return frontendBaseUrl + "/payment/success?orderId=" + orderId;
        } else {
            orderService.updateOrderStatus(orderId, OrderStatus.PAYMENT_FAILED);
            if (tx != null) {
                tx.setStatus("FAILED");
                tx.setResponseCode("FAILED");
                paymentTransactionRepo.save(tx);
            }
            return frontendBaseUrl + "/payment/fail?orderId=" + orderId;
        }
    }

    private String hmacSHA256(String data, String secret) throws Exception {
        Mac hmacSha256 = Mac.getInstance("HmacSHA256");
        SecretKeySpec secretKeySpec = new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
        hmacSha256.init(secretKeySpec);
        byte[] hash = hmacSha256.doFinal(data.getBytes(StandardCharsets.UTF_8));
        StringBuilder sb = new StringBuilder(2 * hash.length);
        for (byte b : hash) sb.append(String.format("%02x", b & 0xff));
        return sb.toString();
    }

    private String generateOrderId(Long baseId) {
        return baseId + "-" + System.currentTimeMillis();
    }

    private String buildOrderInfo(String orderId, String customerName) {
        return "Thanh toán đơn hàng " + orderId + " của " + customerName;
    }

    private Long parseOrderId(String momoOrderId) {
        try {
            if (momoOrderId.contains("-")) {
                return Long.parseLong(momoOrderId.split("-")[0]);
            }
            return Long.parseLong(momoOrderId);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("orderId không hợp lệ");
        }
    }
}
