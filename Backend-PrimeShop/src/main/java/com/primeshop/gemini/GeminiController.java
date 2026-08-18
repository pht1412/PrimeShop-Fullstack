package com.primeshop.gemini;

import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;
import org.springframework.beans.factory.annotation.Autowired;
import com.primeshop.product.ProductRepo;
import java.util.*;

@RestController
@RequestMapping("/api/gemini")
@CrossOrigin(origins = "https://localhost:8080")
public class GeminiController {

    @Autowired
    private GeminiAdvisor geminiAdvisor;

    @Autowired
    private GeminiService geminiService;

    @Autowired
    private ProductRepo productRepo; // ✅ lấy danh sách sản phẩm từ DB

    @PostMapping("/advice")
    public ResponseEntity<Map<String, Object>> getAdvice(@RequestBody Map<String, String> body) {
        String question = body.get("question");
        if (question == null || question.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Câu hỏi không được để trống."));
        }

        String response = geminiAdvisor.askGemini(question);

        Map<String, Object> result = new HashMap<>();
        result.put("question", question);
        result.put("answer", response);
        result.put("timestamp", new Date().toString());
        return ResponseEntity.ok(result);
    }

    @PostMapping("/reset")
    public ResponseEntity<Map<String, String>> resetChat() {
        geminiService.clearUserContext("default_user");
        return ResponseEntity.ok(Map.of("message", "Đã reset hội thoại Gemini."));
    }

}
