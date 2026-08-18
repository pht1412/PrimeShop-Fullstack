package com.primeshop.minigame;

import com.primeshop.voucher.Voucher;
import com.primeshop.voucher.VoucherService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api/minigame")
public class MinigameController {
    @Autowired
    private VoucherService voucherService;

    // Danh sách game cấu hình
    private static final List<GameInfo> GAME_LIST = List.of(
        new GameInfo("who_wants_to_be_millionaire", "Ai là triệu phú", "Trả lời đúng 5 câu hỏi để nhận voucher!", "💡"),
        new GameInfo("quiz", "Quiz nhanh", "Trả lời nhanh 3 câu hỏi, nhận voucher hấp dẫn!", "📝"),
        new GameInfo("lucky_wheel", "Vòng Quay May Mắn", "Quay là trúng! Voucher khủng đang chờ.", "🎡"),
        new GameInfo("tai_xiu", "Tài Xỉu Đại Chiến", "Dự đoán Tài hay Xỉu để nhận quà.", "🎲"),
        new GameInfo("memory_card", "Lật Hình Tìm Cặp", "Thử thách trí nhớ siêu phàm.", "🎴")
    );

    // Câu hỏi cho từng gameId
    private static final Map<String, List<Question>> GAME_QUESTIONS = Map.of(
        "who_wants_to_be_millionaire", List.of(
            new Question("Thủ đô của Việt Nam là gì?", List.of("Hà Nội", "Hải Phòng", "Đà Nẵng", "TP.HCM"), 0),
            new Question("2 + 2 = ?", List.of("3", "4", "5", "6"), 1),
            new Question("Màu cờ Việt Nam là?", List.of("Đỏ", "Xanh", "Vàng", "Trắng"), 0),
            new Question("Chữ cái đầu tiên trong bảng chữ cái?", List.of("A", "B", "C", "D"), 0),
            new Question("Sông nào dài nhất Việt Nam?", List.of("Sông Hồng", "Sông Mekong", "Sông Đà", "Sông Đồng Nai"), 1)
        ),
        "quiz", List.of(
            new Question("Trái đất quay quanh gì?", List.of("Mặt trời", "Mặt trăng", "Sao Hỏa", "Sao Kim"), 0),
            new Question("1 + 1 = ?", List.of("1", "2", "3", "4"), 1),
            new Question("Màu lá cây?", List.of("Xanh", "Đỏ", "Vàng", "Tím"), 0)
        ),
        "lucky_wheel", List.of(
            // Index: 0-10k, 1-20k, 2-Chúc may mắn, 3-50k, 4-Thêm lượt, 5-100k
            new Question("Vòng quay Voucher", List.of("Voucher 10k", "Voucher 20k", "Chúc may mắn", "Voucher 50k", "Thêm lượt", "Voucher 100k"), 0)
        ),
        "tai_xiu", List.of(
            new Question("Dự đoán tổng 3 viên xúc xắc", List.of("Tài (11-17)", "Xỉu (4-10)"), 0)
        ),
        "memory_card", List.of(
             new Question("Tìm các cặp hình giống nhau", List.of("🍎", "🍊", "🍇", "🍉", "🍌", "🍒"), 0)
        )
    );

    @GetMapping("/list")
    public ResponseEntity<?> getGameList() {
        List<Map<String, Object>> games = new ArrayList<>();
        for (GameInfo g : GAME_LIST) {
            games.add(Map.of(
                "gameId", g.getGameId(),
                "name", g.getName(),
                "description", g.getDescription(),
                "icon", g.getIcon()
            ));
        }
        return ResponseEntity.ok(Map.of("games", games));
    }

    @GetMapping("/questions")
    public ResponseEntity<?> getQuestions(@RequestParam String gameId) {
        List<Question> questions = GAME_QUESTIONS.get(gameId);
        if (questions == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "Game không tồn tại!"));
        }
        List<Map<String, Object>> qList = new ArrayList<>();
        for (Question q : questions) {
            qList.add(Map.of(
                "question", q.getQuestion(),
                "options", q.getOptions()
            ));
        }
        return ResponseEntity.ok(Map.of("questions", qList));
    }

    @PostMapping("/play")
    public ResponseEntity<?> playMinigame(@RequestBody MinigameRequest request,
                                             @RequestParam String gameId) {

        // 1️⃣ --- KIỂM TRA GAME CÓ TỒN TẠI KHÔNG ---
        List<Question> questions = GAME_QUESTIONS.get(gameId);
        if (questions == null) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", "Game không tồn tại!"));
        }

        // Biến lưu dữ liệu phụ để trả về client (ví dụ: kết quả xúc xắc)
        Map<String, Object> extraData = new HashMap<>();

        // 2️⃣ --- SWITCH LOGIC TỪNG GAME ---
        switch (gameId) {

            // -------------------------------------------------------
            // 🧠 GAME 1: AI LÀ TRIỆU PHÚ (QUIZ NHIỀU CÂU)
            // -------------------------------------------------------
            case "who_wants_to_be_millionaire":
            case "quiz": {
                if (request.getAnswers() == null || request.getAnswers().size() != questions.size()) {
                    return ResponseEntity.badRequest().body(Map.of("success", false, "message", "Số lượng đáp án không hợp lệ!"));
                }

                boolean allCorrect = true;
                for (int i = 0; i < questions.size(); i++) {
                    if (!Objects.equals(request.getAnswers().get(i), questions.get(i).getCorrectIndex())) {
                        allCorrect = false;
                        break;
                    }
                }

                if (!allCorrect) {
                    return ResponseEntity.ok(Map.of("success", false, "message", "Rất tiếc! Bạn chưa trả lời đúng hết!"));
                }
                break; // qua tạo voucher
            }

            // -------------------------------------------------------
            // 🎲 GAME 2: TÀI XỈU (3 viên xúc xắc)
            // -------------------------------------------------------
            case "tai_xiu": {
                if (request.getAnswers() == null || request.getAnswers().isEmpty()) {
                    return ResponseEntity.badRequest().body(Map.of("success", false, "message", "Vui lòng chọn Tài hoặc Xỉu!"));
                }

                int userChoice = request.getAnswers().get(0); // 0 = Tài, 1 = Xỉu
                Random r = new Random();

                // Gieo xúc xắc tại server
                int d1 = r.nextInt(6) + 1;
                int d2 = r.nextInt(6) + 1;
                int d3 = r.nextInt(6) + 1;
                int sum = d1 + d2 + d3;

                // Lưu kết quả xúc xắc để trả về frontend
                extraData.put("dice", List.of(d1, d2, d3));
                extraData.put("sum", sum);

                boolean isTai = sum >= 11 && sum <= 17;
                boolean isXiu = sum >= 4 && sum <= 10;

                boolean userWin = (userChoice == 0 && isTai) || (userChoice == 1 && isXiu);

                String resultText = "Kết quả: " + d1 + "-" + d2 + "-" + d3 + " (Tổng " + sum + ")";
                String choiceText = (userChoice == 0 ? "Tài" : "Xỉu");

                // Nếu thua, trả về ngay kết quả xúc xắc để hiển thị
                if (!userWin) {
                    Map<String, Object> response = new HashMap<>();
                    response.put("success", false);
                    response.put("message", resultText + ". Bạn chọn " + choiceText + " — Bạn thua! Hãy thử lại nhé.");
                    response.putAll(extraData); // <-- QUAN TRỌNG: Trả về dice
                    return ResponseEntity.ok(response);
                }

                // Nếu thắng, break để xuống phần tạo voucher (vẫn giữ extraData)
                break; 
            }

            // -------------------------------------------------------
            // 🎡 GAME 3: LUCKY WHEEL (WEIGHTED RANDOM)
            // -------------------------------------------------------
            case "lucky_wheel": {
                int roll = new Random().nextInt(100);
                int resultIndex;

                if (roll < 50) resultIndex = 2;       // Chúc may mắn — trượt
                else if (roll < 70) resultIndex = 4;  // Thêm lượt — không thưởng
                else if (roll < 85) resultIndex = 0;  // 10k
                else if (roll < 95) resultIndex = 1;  // 20k
                else if (roll < 99) resultIndex = 3;  // 50k
                else resultIndex = 5;                 // 100k

                if (resultIndex == 2) {
                    return ResponseEntity.ok(Map.of("success", false, "message", "Chúc may mắn lần sau!"));
                }
                if (resultIndex == 4) {
                    return ResponseEntity.ok(Map.of("success", false, "message", "Bạn nhận được thêm lượt!"));
                }

                // Result 0,1,3,5 đều là trúng thưởng voucher
                break; // qua tạo voucher
            }

            // -------------------------------------------------------
            // 🎴 GAME 4: MEMORY CARD — random win
            // -------------------------------------------------------
            case "memory_card": {
                boolean win = new Random().nextBoolean(); // 50/50 thắng thua
                if (!win) {
                    return ResponseEntity.ok(Map.of(
                        "success", false,
                        "message", "Bạn chưa tìm đúng cặp hình! Hãy thử lại nhé!"
                    ));
                }
                break; // qua tạo voucher
            }

            default:
                return ResponseEntity.badRequest().body(Map.of("success", false, "message", "Game không hỗ trợ!"));
        }

        // -------------------------------------------------------
        // 🏆 3️⃣ TẠO VOUCHER KHI CHIẾN THẮNG
        // -------------------------------------------------------
        try {
            Voucher voucher = voucherService.createMinigameVoucherForUser(request.getUserId());
            
            // Tạo response thành công bao gồm cả voucher và extraData (nếu có)
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Chúc mừng! Bạn đã chiến thắng!");
            response.put("voucher", voucher);
            response.putAll(extraData); // Trả về dice 4-5-6 khi thắng tài xỉu nếu có

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.ok(Map.of(
                "success", false,
                "message", "Bạn đã thắng nhưng hệ thống lỗi khi tạo voucher. Vui lòng liên hệ Admin!"
            ));
        }
    }

    // DTOs
    public static class MinigameRequest {
        private List<Integer> answers;
        private Long userId;
        public List<Integer> getAnswers() { return answers; }
        public void setAnswers(List<Integer> answers) { this.answers = answers; }
        public Long getUserId() { return userId; }
        public void setUserId(Long userId) { this.userId = userId; }
    }
    public static class Question {
        private String question;
        private List<String> options;
        private int correctIndex;
        public Question(String question, List<String> options, int correctIndex) {
            this.question = question;
            this.options = options;
            this.correctIndex = correctIndex;
        }
        public String getQuestion() { return question; }
        public List<String> getOptions() { return options; }
        public int getCorrectIndex() { return correctIndex; }
    }
    public static class GameInfo {
        private String gameId;
        private String name;
        private String description;
        private String icon;
        public GameInfo(String gameId, String name, String description, String icon) {
            this.gameId = gameId;
            this.name = name;
            this.description = description;
            this.icon = icon;
        }
        public String getGameId() { return gameId; }
        public String getName() { return name; }
        public String getDescription() { return description; }
        public String getIcon() { return icon; }
    }
}