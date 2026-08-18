package com.primeshop.utils;

import java.text.Normalizer;

public class SlugUtils {
    public static String toSlug(String input) {
        return Normalizer.normalize(input, Normalizer.Form.NFD)
        .replaceAll("\\p{InCombiningDiacriticalMarks}+", "")
        .replaceAll("[^a-zA-Z0-9\\s]", "")
        .trim()
        .replaceAll("\\s+", "-")
        .toLowerCase();
    }
}
