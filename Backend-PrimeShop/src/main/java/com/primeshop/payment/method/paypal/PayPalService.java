package com.primeshop.payment.method.paypal;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import com.primeshop.order.Order;
import com.primeshop.order.OrderRepo;
import com.primeshop.order.OrderService;
import com.primeshop.order.OrderStatus;
import com.primeshop.payment.method.vnpay.PaymentCallbackResult;
import com.primeshop.payment.transaction.PaymentTransaction;
import com.primeshop.payment.transaction.PaymentTransactionRepo;
import com.primeshop.utils.SecurityUtils;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PayPalService {
    
    @Value("${paypal.client-id}")
    private String clientId;

    @Value("${paypal.client-secret}")
    private String clientSecret;

    @Value("${paypal.mode}")
    private String mode;

    @Value("${paypal.base-url}")
    private String baseUrl;

    private final OrderRepo orderRepo;
    private final OrderService orderService;
    private static final RestTemplate restTemplate = new RestTemplate();
    private final PaymentTransactionRepo paymentTransactionRepo;
    private final SecurityUtils securityUtils;

    public PayPalResponse createOrder(Long orderId) {
        Optional<Order> orderOptional = orderRepo.findById(orderId);
        Order order = orderOptional.get();

        BigDecimal vndAmount = order.getTotalAmount();
        BigDecimal exchangeRate = new BigDecimal("24000");
        BigDecimal amount = vndAmount.divide(exchangeRate, 0, BigDecimal.ROUND_UP);
        String currency = "USD";

        RestTemplate restTemplate = new RestTemplate();

        HttpHeaders headers = new HttpHeaders();
        headers.setBasicAuth(clientId, clientSecret);
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("grant_type", "client_credentials");

        String accessToken = getAccessToken();

        headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);
        headers.setContentType(MediaType.APPLICATION_JSON);

        Map<String, Object> orderRequest = Map.of(
            "intent", "CAPTURE",
            "purchase_units", List.of(Map.of(
                "amount", Map.of(
                    "currency_code", currency,
                    "value", amount.toString()
                )
            )),
            "application_context", Map.of(
                "return_url", "https://localhost:8080/api/payment/paypal/return",
                "cancel_url", "https://localhost:8080/api/payment/paypal/cancel"
            )
        );

        ResponseEntity<Map> orderRes = restTemplate.postForEntity(
            baseUrl + "/v2/checkout/orders",
            new HttpEntity<>(orderRequest, headers),
            Map.class
        );

        List<Map<String, Object>> links = (List<Map<String, Object>>) orderRes.getBody().get("links");
        String approveUrl = links.stream()
                .filter(link -> "approve".equals(link.get("rel")))
                .findFirst()
                .map(link -> (String) link.get("href"))
                .orElseThrow();

        PayPalResponse payPalResponse = new PayPalResponse();
        payPalResponse.setStatus((String) orderRes.getBody().get("status"));
        payPalResponse.setLinks(new String[]{approveUrl});
        Object idObj = orderRes.getBody().get("id");
        if (idObj != null) {
            payPalResponse.setId(idObj.toString());
            order.setPaypalPaymentId(idObj.toString());
            orderRepo.save(order);
        }

        PaymentTransaction tx = new PaymentTransaction();
        Long userId = securityUtils.getCurrentUserId();
        tx.setUserId(userId);
        tx.setOrderId(orderId.toString());
        tx.setAmount(amount);
        tx.setPaymentMethod("PAYPAL");
        tx.setStatus("INITIATED");
        tx.setTransactionCode(payPalResponse.getId());

        if (paymentTransactionRepo != null) {
            paymentTransactionRepo.save(tx);
        }

        return payPalResponse;
    }

    public PaymentCallbackResult handleReturn(String orderId) {
        try {
            String accessToken = getAccessToken();
            String captureUrl = "https://api-m.sandbox.paypal.com/v2/checkout/orders/" + orderId + "/capture";

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(accessToken);

            HttpEntity<String> entity = new HttpEntity<>(null, headers);
            ResponseEntity<String> paypalResponse =
                    restTemplate.exchange(captureUrl, HttpMethod.POST, entity, String.class);

            Order order = orderRepo.findByPaypalPaymentId(orderId);
            if (order == null) {
                return PaymentCallbackResult.invalid("Order not found for PayPal orderId: " + orderId);
            }

            PaymentTransaction tx = paymentTransactionRepo.findFirstByOrderIdOrderByCreatedAtDesc(order.getId().toString()).orElse(null);

            if (paypalResponse.getStatusCode().is2xxSuccessful()
                    && paypalResponse.getBody() != null
                    && paypalResponse.getBody().contains("\"status\":\"COMPLETED\"")) {
                orderService.updateOrderStatus(order.getId(), OrderStatus.PAID);
                if (tx != null) {
                    tx.setStatus("SUCCESS");
                    tx.setResponseCode("00");
                    paymentTransactionRepo.save(tx);
                }
                return PaymentCallbackResult.success("Payment successful");
            } else {
                orderService.updateOrderStatus(order.getId(), OrderStatus.PAYMENT_FAILED);
                if (tx != null) {
                    tx.setStatus("FAILED");
                    tx.setResponseCode("FAILED");
                    paymentTransactionRepo.save(tx);
                }
                return PaymentCallbackResult.failed("Payment failed with status: " + paypalResponse.getStatusCode());
            }
        } catch (Exception e) {
            e.printStackTrace();
            return PaymentCallbackResult.failed("Exception occurred: " + e.getMessage());
        }
    }

    public String getAccessToken() {
        String url = baseUrl + "/v1/oauth2/token";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        headers.setBasicAuth(clientId, clientSecret);

        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("grant_type", "client_credentials");

        HttpEntity<MultiValueMap<String, String>> entity = new HttpEntity<>(body, headers);

        ResponseEntity<Map> response = restTemplate.exchange(
                url, HttpMethod.POST, entity, Map.class);

        Map<String, Object> responseBody = response.getBody();
        if (responseBody != null && responseBody.containsKey("access_token")) {
            return (String) responseBody.get("access_token");
        } else {
            throw new RuntimeException("Failed to retrieve access token from PayPal");
        }
    }
}
