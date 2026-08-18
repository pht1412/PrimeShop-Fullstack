package com.primeshop.chatbot;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import com.primeshop.product.Product;
import com.primeshop.product.ProductRepo;

@Service
public class ChatbotService {
    
    @Autowired
    private ProductRepo productRepo;

    public String handleQuestion(String question) {
        String lower = question.toLowerCase();

        if (lower.contains("iphone") || lower.contains("apple")) {
            // Không dùng findByBrand, dùng Specification để lọc brand = "Apple"
            Specification<Product> spec = (root, query, cb) -> cb.equal(cb.lower(root.get("brand")), "apple");
            List<Product> products = productRepo.findAll(spec);
            return formatProducts(products);
        }

        // 2. Nếu nhắc đến "dưới X triệu"
        if (lower.contains("dưới")) {
            double price = extractPrice(lower); // ví dụ 20000000
            Specification<Product> spec = (root, query, cb) -> cb.lessThanOrEqualTo(root.get("price"), price);
            List<Product> products = productRepo.findAll(spec);
            return formatProducts(products);
        }

        return "Xin lỗi, tôi chưa tìm thấy sản phẩm phù hợp.";
    }

    private String formatProducts(List<Product> products) {
        if (products.isEmpty()) return "Không có sản phẩm phù hợp.";
        return products.stream()
                .map(p -> p.getName() + " | " + p.getBrand() + " | " + p.getPrice() + " VND | " + "http://localhost:5173/product-detail/" + p.getSlug())
                .collect(Collectors.joining("\n"));
    }

    private double extractPrice(String text) {
        // ví dụ: "dưới 20 triệu" -> 20000000
        Pattern pattern = Pattern.compile("(\\d+)");
        Matcher matcher = pattern.matcher(text);
        if (matcher.find()) {
            return Double.parseDouble(matcher.group(1)) * 1_000_000;
        }
        return Double.MAX_VALUE;
    }
}
