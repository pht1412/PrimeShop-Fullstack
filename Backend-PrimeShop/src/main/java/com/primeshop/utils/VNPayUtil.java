package com.primeshop.utils;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.stream.Collectors;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import org.springframework.stereotype.Component;
import jakarta.xml.bind.DatatypeConverter;

@Component
public class VNPayUtil {
    
    public String hmacSHA512(String key, String data) {
        try {
            Mac hmac = Mac.getInstance("HmacSHA512");
            SecretKeySpec secretKeySpec = new SecretKeySpec(key.getBytes(), "HmacSHA512");
            hmac.init(secretKeySpec);
            byte[] result = hmac.doFinal(data.getBytes(StandardCharsets.UTF_8));
            return DatatypeConverter.printHexBinary(result).toLowerCase();
        } catch (Exception e) {
            throw new RuntimeException("Cannot sign data", e);
        }
    }

    // [VNPayUtil.java]

public String buildQuery(Map<String, String> params) {
    return params.entrySet().stream()
            .sorted(Map.Entry.comparingByKey())
            .map(e -> {
                try {
                    // Encode chuẩn, sau đó đổi dấu + thành %20
                    String encodedValue = URLEncoder.encode(e.getValue(), StandardCharsets.UTF_8)
                                          .replace("+", "%20"); 
                    return e.getKey() + "=" + encodedValue;
                } catch (Exception ex) {
                    return e.getKey() + "=" + e.getValue();
                }
            })
            .collect(Collectors.joining("&"));
    }
}