package com.primeshop.utils;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Map;

public class MoMoSignatureUtils {
    public static boolean isValidSignature(Map<String, String> params, String secretKey, String accessKey) {
        try {
            String receivedSig = params.get("signature");
            if (receivedSig == null) return false;
    
            String rawData = "accessKey=" + accessKey
                    + "&amount=" + safe(params, "amount")
                    + "&extraData=" + safe(params, "extraData")
                    + "&message=" + safe(params, "message")
                    + "&orderId=" + safe(params, "orderId")
                    + "&orderInfo=" + safe(params, "orderInfo")
                    + "&orderType=" + safe(params, "orderType")
                    + "&partnerCode=" + safe(params, "partnerCode")
                    + "&payType=" + safe(params, "payType")
                    + "&requestId=" + safe(params, "requestId")
                    + "&responseTime=" + safe(params, "responseTime")
                    + "&resultCode=" + safe(params, "resultCode")
                    + "&transId=" + safe(params, "transId");
    
            System.out.println("Raw data for signature: " + rawData);
    
            String calcSig = hmacSHA256(rawData, secretKey);
            System.out.println("Received signature:   " + receivedSig);
            System.out.println("Calculated signature: " + calcSig);
    
            return receivedSig.equals(calcSig);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    private static String hmacSHA256(String data, String secretKey) throws Exception {
        Mac hmacSHA256 = Mac.getInstance("HmacSHA256");
        SecretKeySpec secretKeySpec = new SecretKeySpec(secretKey.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
        hmacSHA256.init(secretKeySpec);
        byte[] hash = hmacSHA256.doFinal(data.getBytes(StandardCharsets.UTF_8));
        StringBuilder result = new StringBuilder();
        for (byte b : hash) {
            result.append(String.format("%02x", b));
        }
        return result.toString();
    }
    
    private static String safe(Map<String, String> map, String key) {
        return map.getOrDefault(key, "");
    }
}
