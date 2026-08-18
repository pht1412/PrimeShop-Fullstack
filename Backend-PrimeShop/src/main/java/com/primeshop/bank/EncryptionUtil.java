package com.primeshop.bank;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

public class EncryptionUtil {

    public static byte[] encrypt(String data) {
        return Base64.getEncoder().encode(data.getBytes(StandardCharsets.UTF_8));
    }

    public static String decrypt(byte[] encrypted) {
        return new String(Base64.getDecoder().decode(encrypted), StandardCharsets.UTF_8);
    }
}
