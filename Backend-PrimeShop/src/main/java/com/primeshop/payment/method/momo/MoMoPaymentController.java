package com.primeshop.payment.method.momo;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/payment/momo")
@CrossOrigin(origins =  "http://locahost:5173")
@RequiredArgsConstructor
public class MoMoPaymentController {
    
    private final MoMoPaymentService moMoPaymentService;
    @Value("${frontend.url}")
    private String frontendBaseUrl;

    @PostMapping("/create")
    public ResponseEntity<MoMoPaymentResponse> createPayment(@RequestBody MoMoRequest request) throws Exception {
        try {
            MoMoPaymentResponse response = moMoPaymentService.createPayment(request);
            return ResponseEntity.ok(response);
        } catch (IllegalStateException e) {
            return ResponseEntity.badRequest().body(null);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(null);
        }
    }

    @GetMapping("/return")
    public void handleReturn(@RequestParam Map<String, String> params, HttpServletResponse response) throws IOException {
        try {
            String redirectUrl = moMoPaymentService.handleReturn(params);
            response.sendRedirect(redirectUrl);
        } catch (IllegalArgumentException e) {
            response.sendRedirect(frontendBaseUrl + "/payment/fail?reason=" + URLEncoder.encode(e.getMessage(), StandardCharsets.UTF_8));
        } catch (Exception e) {
            response.sendRedirect(frontendBaseUrl + "/payment/fail?reason=server_error");
        }
    }
}
