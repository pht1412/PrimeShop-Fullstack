package com.primeshop.utils;

public class CodeUtils {
    public static String encodeProductId(Long id) {
        return String.format("P%05X", id);
    }

    public static Long decodeProductCode(String code) {
        if (!code.startsWith("P")) throw new IllegalArgumentException("Mã không hợp lệ");
        return Long.parseLong(code.substring(1), 16);
    }
}
