package com.primeshop.gemini;

import com.primeshop.product.Product;
import com.primeshop.product.ProductRepo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
public class GeminiAdvisor {

    private final GeminiService geminiService;
    private final ProductRepo productRepo;

    public GeminiAdvisor(GeminiService geminiService, ProductRepo productRepo) {
        this.geminiService = geminiService;
        this.productRepo = productRepo;
    }

    public String askGemini(String question) {
        long start = System.currentTimeMillis();
        String lowerQ = question.toLowerCase();

        // 1. Lấy toàn bộ sản phẩm (Lưu ý: Nếu DB quá lớn >1000sp thì nên dùng Query Limit)
        List<Product> allProducts = productRepo.findAll();

        // 2. Chiến thuật 1: Lọc theo từ khóa (Keyword Matching)
        List<Product> candidates = allProducts.stream()
                .filter(p -> {
                    String name = Optional.ofNullable(p.getName()).orElse("").toLowerCase();
                    String brand = Optional.ofNullable(p.getBrand()).orElse("").toLowerCase();
                    String cat = (p.getCategory() != null && p.getCategory().getName() != null)
                            ? p.getCategory().getName().toLowerCase() : "";
                    
                    // So sánh: Tên SP chứa từ khóa HOẶC Câu hỏi chứa tên Brand/Cat
                    return name.contains(lowerQ) || lowerQ.contains(name) 
                           || lowerQ.contains(brand) || lowerQ.contains(cat);
                })
                .limit(5)
                .collect(Collectors.toList());

        // 3. Chiến thuật 2 (Dự phòng): Nếu không tìm thấy theo từ khóa, lấy Top sản phẩm tiêu biểu
        // Để Gemini tự suy luận dựa trên mô tả
        if (candidates.isEmpty()) {
            log.info("🔍 Không tìm thấy sản phẩm theo từ khóa, chuyển sang chế độ gợi ý thông minh...");
            candidates = allProducts.stream()
                    .filter(p -> p.getPrice() != null) // Chỉ lấy sp có giá
                    .limit(15) // Lấy 15 sản phẩm đầu bảng (hoặc random nếu muốn)
                    .collect(Collectors.toList());
        }

        // 4. Chuyển đổi sang Map để gửi cho Gemini (Giảm tải dữ liệu thừa)
        List<Map<String, Object>> productList = candidates.stream()
                .map(p -> {
                    Map<String, Object> item = new HashMap<>();
                    item.put("id", p.getId());
                    item.put("name", p.getName());
                    item.put("price", p.getPrice()); // Gemini cần giá để tư vấn ngân sách
                    item.put("brand", p.getBrand());
                    item.put("category", p.getCategory() != null ? p.getCategory().getName() : "");
                    // Gửi thêm mô tả ngắn nếu có để Gemini hiểu tính năng
                    item.put("desc", p.getDescription() != null && p.getDescription().length() > 100 
                            ? p.getDescription().substring(0, 100) + "..." 
                            : p.getDescription());
                    return item;
                })
                .collect(Collectors.toList());
        
        // 5. Gọi Gemini
        String response = geminiService.getProductAdvice(question, productList);

        long end = System.currentTimeMillis();
        log.info("⚡ GeminiAdvisor xong trong {} ms. Gửi {} sản phẩm cho AI.", (end - start), productList.size());

        return response;
    }
}