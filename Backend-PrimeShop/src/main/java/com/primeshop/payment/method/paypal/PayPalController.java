package com.primeshop.payment.method.paypal;

import java.io.IOException;
import java.net.URI;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import com.primeshop.payment.method.vnpay.PaymentCallbackResult;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/payment/paypal")
@CrossOrigin(origins = "http://localhost:5173")
@RequiredArgsConstructor
public class PayPalController {

    private final PayPalService payPalService;

    @PostMapping("/create")
    public ResponseEntity<?> createOrder(@RequestBody PayPalRequest request) throws Exception {
        PayPalResponse paypalRes = payPalService.createOrder(request.getOrderId());
        return ResponseEntity.ok(paypalRes);
    }

    @GetMapping("/return")
    public ResponseEntity<?> handleReturn(@RequestParam("token") String orderId, HttpServletResponse response) throws IOException {
        PaymentCallbackResult result = payPalService.handleReturn(orderId);

        String redirectUrl;
        if (result.isSuccess()) {
            redirectUrl = "http://localhost:5173/payment/success?orderId=" + orderId;
        } else {
            redirectUrl = "http://localhost:5173/payment/failed?orderId=" + orderId;
        }

        return ResponseEntity
            .status(HttpStatus.FOUND)
            .location(URI.create(redirectUrl))
            .build();
    }
}
