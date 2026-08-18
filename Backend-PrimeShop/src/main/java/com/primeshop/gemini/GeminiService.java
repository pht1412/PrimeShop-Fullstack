package com.primeshop.gemini;

import io.github.cdimascio.dotenv.Dotenv;
import lombok.extern.slf4j.Slf4j;
import com.google.gson.*;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Service
@Slf4j
public class GeminiService {
    private final String geminiApiKey;
    private final RestTemplate restTemplate = new RestTemplate();
    private final Gson gson = new Gson();
    private final GeminiPromptBuilder promptBuilder;

    // 🧠 Cache để tránh gọi lại cùng câu hỏi
    private final Map<String, String> cache = new ConcurrentHashMap<>();

    // 🧩 Bộ nhớ hội thoại (mỗi user có 1 danh sách lịch sử)
    private final Map<String, List<Map<String, String>>> userContexts = new ConcurrentHashMap<>();

    public GeminiService(GeminiPromptBuilder promptBuilder) {
        this.promptBuilder = promptBuilder;
        Dotenv dotenv = Dotenv.load();
        this.geminiApiKey = dotenv.get("GEMINI_API_KEY");
    }

    // ✅ Lấy context (hoặc tạo mới nếu chưa có)
    private List<Map<String, String>> getUserContext(String userId) {
        return userContexts.computeIfAbsent(userId, k -> new ArrayList<>());
    }

    // ✅ Xóa context của user (reset chat)
    public void clearUserContext(String userId) {
        userContexts.remove(userId);
        log.info("🧹 Đã xóa ngữ cảnh hội thoại cho user {}", userId);
    }

    // ✅ Hàm chính: Gửi câu hỏi đến Gemini, có giữ ngữ cảnh
    public String getProductAdvice(String question, List<Map<String, Object>> products) {
        String userId = "default_user"; // 🔧 Tạm thời cố định (nếu có login thì truyền ID người dùng)
        List<Map<String, String>> chatHistory = getUserContext(userId);

        // Nếu câu hỏi rỗng
        if (question == null || question.isBlank()) {
            return "{\"error\":\"Câu hỏi không được để trống.\"}";
        }

        // 🔍 Kiểm tra cache
        if (cache.containsKey(question)) {
            log.info("⚡ Cache hit for question: {}", question);
            return cache.get(question);
        }

        // 🧠 Thêm câu hỏi mới vào lịch sử hội thoại
        chatHistory.add(Map.of("role", "user", "content", question));

        // 🧩 Tạo prompt có tính tư vấn (nằm trong GeminiPromptBuilder)
        String prompt = promptBuilder.buildPrompt(question, products);
        System.out.println("Generated Prompt: " + prompt);

        // 🧾 Dựng JSON request có toàn bộ hội thoại (ngữ cảnh)
        Map<String, Object> body = Map.of(
                "contents", chatHistory.stream()
                        .map(msg -> Map.of(
                                "role", msg.get("role"),
                                "parts", List.of(Map.of("text", msg.get("content")))
                        ))
                        .collect(Collectors.toList())
        );

        // tốc độ chậm, chất lượng cao, chính xác cao
         String url = "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-pro:generateContent?key=" + geminiApiKey;
        
        // tốc độ nhanh, chất lượng trung bình, chính xác trung bình thích hợp với tư vấn sản phẩm
        //String url = "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash:generateContent?key=" + geminiApiKey;
        
        // tốc độ nhanh nhất, chất lượng thấp, chính xác thấp thích hợp với chat nhanh, tóm tắt
        // String url = "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash-lite:generateContent?key=" + geminiApiKey;

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<String> entity = new HttpEntity<>(gson.toJson(body), headers);

        try {
            // 🚀 Gọi API Gemini
            ResponseEntity<String> response = restTemplate.postForEntity(url, entity, String.class);

            // 🧩 Parse kết quả JSON
            JsonObject root = JsonParser.parseString(response.getBody()).getAsJsonObject();
            String text = root.getAsJsonArray("candidates")
                    .get(0).getAsJsonObject()
                    .getAsJsonObject("content")
                    .getAsJsonArray("parts")
                    .get(0).getAsJsonObject()
                    .get("text").getAsString()
                    .replaceAll("(?s)```json|```", "")
                    .trim();

            // 🧠 Lưu phản hồi vào lịch sử hội thoại
            chatHistory.add(Map.of("role", "model", "content", text));

            // 🧱 Cache kết quả (để lần sau khỏi gọi lại)
            cache.put(question, text);

            log.info("✅ Gemini response: {}", text);
            return text;

        } catch (Exception e) {
            log.error("❌ Lỗi khi gọi Gemini: ", e);
            System.out.println("❌ Lỗi khi gọi Gemini: " + e.getMessage());
            return "{\"error\":\"Lỗi khi gọi Gemini API hoặc phân tích kết quả.\"}";
        }
    }
}