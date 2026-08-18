package com.primeshop.api;

import com.primeshop.core.BaseTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvFileSource;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.http.MediaType;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * MinigameApiTest - Sprint 2: Kiểm thử tự động module Minigame
 *
 * Dữ liệu đọc từ 2 file CSV:
 * → minigame_success_test.csv : TC đúng, kỳ vọng PASS ✅
 * → minigame_fail_test.csv : TC phát hiện BUG, kỳ vọng FAIL ❌
 */
@DisplayName("Minigame API Tests")
public class MinigameApiTest extends BaseTest {

    // ===================================================================
    // ✅ NHÓM PASS - Đọc từ minigame_success_test.csv
    // ===================================================================

    @Test
    @DisplayName("testGetGameList_ThanhCong_TraVeDanhSachGame")
    public void testGetGameList_ThanhCong_TraVeDanhSachGame() throws Exception {
        mockMvc.perform(get("/api/minigame/list")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.games").isArray());
    }

    @Test
    @DisplayName("testGetGameList_ThanhCong_GameCoEduTruong")
    public void testGetGameList_ThanhCong_GameCoEduTruong() throws Exception {
        mockMvc.perform(get("/api/minigame/list")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.games[0].gameId").exists())
                .andExpect(jsonPath("$.games[0].name").exists());
    }

    @ParameterizedTest(name = "[{index}] TC={0} | gameId={1} | {6}")
    @CsvFileSource(resources = "/data/minigame/minigame_success_test.csv", numLinesToSkip = 1)
    @DisplayName("testPlayMinigame_ThanhCong_DuLieuHopLe")
    public void testPlayMinigame_ThanhCong_DuLieuHopLe(
            String testCase, String gameId, int userId,
            String answers, int expectedStatus,
            String expectedSuccess, String description) throws Exception {

        String answersJson = (answers == null || answers.equals("null")) ? "null" : answers;
        String payload = "{ \"userId\": " + userId + ", \"answers\": " + answersJson + " }";

        var request = mockMvc.perform(post("/api/minigame/play")
                .param("gameId", gameId.trim())
                .contentType(MediaType.APPLICATION_JSON)
                .content(payload));

        if (expectedStatus == 200) {
            request.andExpect(status().isOk());
        } else {
            request.andExpect(status().isBadRequest());
        }
    }

    @ParameterizedTest(name = "[{index}] gameId={0}, expectedCount={1}")
    @CsvSource({
            "who_wants_to_be_millionaire, 5, Lay dung 5 cau hoi Ai La Trieu Phu",
            "quiz,                        3, Lay dung 3 cau hoi Quiz",
            "tai_xiu,                     1, Lay dung 1 lua chon Tai Xiu"
    })
    @DisplayName("testGetQuestions_ThanhCong_GameHopLe_TraVeDungSoCauHoi")
    public void testGetQuestions_ThanhCong_GameHopLe_TraVeDungSoCauHoi(
            String gameId, int expectedCount, String description) throws Exception {
        mockMvc.perform(get("/api/minigame/questions")
                .param("gameId", gameId.trim())
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.questions").isArray())
                .andExpect(jsonPath("$.questions.length()").value(expectedCount));
    }

    @ParameterizedTest(name = "[{index}] gameId={0} | {1}")
    @CsvSource({
            "fake_game_xyz, GameId khong ton tai - ky vong 400",
            "MILLIONAIRE,   GameId sai chu hoa - ky vong 400"
    })
    @DisplayName("testGetQuestions_ThanhCong_GameKhongTonTai_Loi400")
    public void testGetQuestions_ThanhCong_GameKhongTonTai_Loi400(
            String gameId, String description) throws Exception {
        mockMvc.perform(get("/api/minigame/questions")
                .param("gameId", gameId.trim())
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @ParameterizedTest(name = "[{index}] choice={0} | {2}")
    @CsvSource({
            "0, Tai, Chon Tai - xuc xac phai la mang",
            "1, Xiu, Chon Xiu - xuc xac phai la mang"
    })
    @DisplayName("testPlayTaiXiu_ThanhCong_TraVeKetQuaXucXac")
    public void testPlayTaiXiu_ThanhCong_TraVeKetQuaXucXac(
            int choice, String label, String description) throws Exception {
        String payload = "{ \"userId\": 1, \"answers\": [" + choice + "] }";
        mockMvc.perform(post("/api/minigame/play")
                .param("gameId", "tai_xiu")
                .contentType(MediaType.APPLICATION_JSON)
                .content(payload))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.dice").isArray())
                .andExpect(jsonPath("$.sum").isNumber());
    }

    // ===================================================================
    // ❌ NHÓM FAIL - Đọc từ minigame_fail_test.csv (Phát hiện BUG)
    // ===================================================================

    // ❌ BUG: API không yêu cầu xác thực → Lỗ hổng bảo mật
    @Test
    @DisplayName("testGetGameList_ThatBai_KhongToken_KyVong401_BaoMatYeu")
    public void testGetGameList_ThatBai_KhongToken_KyVong401_BaoMatYeu() throws Exception {
        mockMvc.perform(get("/api/minigame/list")
                .contentType(MediaType.APPLICATION_JSON))
                // BUG bảo mật: Phải yêu cầu JWT token, nhưng API cho phép anonymous
                .andExpect(status().isUnauthorized());
    }

    // ❌ BUG: userId âm phải bị từ chối
    @Test
    @DisplayName("testPlayMillionaire_ThatBai_UserIdAm_KhongBiTuChoi")
    public void testPlayMillionaire_ThatBai_UserIdAm_KhongBiTuChoi() throws Exception {
        String payload = "{ \"userId\": -1, \"answers\": [0, 1, 0, 0, 1] }";
        mockMvc.perform(post("/api/minigame/play")
                .param("gameId", "who_wants_to_be_millionaire")
                .contentType(MediaType.APPLICATION_JSON)
                .content(payload))
                // BUG: userId âm phải trả về 400, nhưng API xử lý bình thường
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("userId không hợp lệ!"));
    }

    // ❌ BUG: Khi thắng phải trao voucher có code và giá trị giảm
    @Test
    @DisplayName("testPlayMillionaire_ThatBai_ThangKhongCoVoucher_ChuaImplement")
    public void testPlayMillionaire_ThatBai_ThangKhongCoVoucher_ChuaImplement() throws Exception {
        String payload = "{ \"userId\": 1, \"answers\": [0, 1, 0, 0, 1] }";
        mockMvc.perform(post("/api/minigame/play")
                .param("gameId", "who_wants_to_be_millionaire")
                .contentType(MediaType.APPLICATION_JSON)
                .content(payload))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                // BUG: Thiếu trường voucher.code và voucher.discountPercent
                .andExpect(jsonPath("$.voucher.code").isString())
                .andExpect(jsonPath("$.voucher.discountPercent").isNumber());
    }

    // ❌ BUG: Lucky Wheel phải trả về giải thưởng cụ thể
    @Test
    @DisplayName("testPlayLuckyWheel_ThatBai_KetQuaThieuTruongPrize")
    public void testPlayLuckyWheel_ThatBai_KetQuaThieuTruongPrize() throws Exception {
        String payload = "{ \"userId\": 1, \"answers\": [] }";
        mockMvc.perform(post("/api/minigame/play")
                .param("gameId", "lucky_wheel")
                .contentType(MediaType.APPLICATION_JSON)
                .content(payload))
                .andExpect(status().isOk())
                // BUG: Phải có trường prize chứa tên và giá trị giải thưởng
                .andExpect(jsonPath("$.prize.name").isString())
                .andExpect(jsonPath("$.prize.value").isNumber());
    }

    // ❌ BUG: Câu hỏi thiếu trường difficulty
    @Test
    @DisplayName("testGetQuestions_ThatBai_CauHoiThieuTruongDifficulty")
    public void testGetQuestions_ThatBai_CauHoiThieuTruongDifficulty() throws Exception {
        mockMvc.perform(get("/api/minigame/questions")
                .param("gameId", "who_wants_to_be_millionaire")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                // BUG: Câu hỏi thiếu trường difficulty (easy/medium/hard)
                .andExpect(jsonPath("$.questions[0].difficulty").isString());
    }

    // ❌ BUG: Kết quả Quiz thiếu trường score
    @Test
    @DisplayName("testPlayQuiz_ThatBai_KetQuaThieuTruongScore")
    public void testPlayQuiz_ThatBai_KetQuaThieuTruongScore() throws Exception {
        String payload = "{ \"userId\": 1, \"answers\": [0, 1, 0] }";
        mockMvc.perform(post("/api/minigame/play")
                .param("gameId", "quiz")
                .contentType(MediaType.APPLICATION_JSON)
                .content(payload))
                .andExpect(status().isOk())
                // BUG: Kết quả phải có trường score là số nguyên
                .andExpect(jsonPath("$.score").isNumber());
    }

    // ❌ BUG: Tài Xỉu thiếu trường isValid xác nhận tính hợp lệ
    @Test
    @DisplayName("testPlayTaiXiu_ThatBai_KetQuaThieuTruongIsValid")
    public void testPlayTaiXiu_ThatBai_KetQuaThieuTruongIsValid() throws Exception {
        String payload = "{ \"userId\": 1, \"answers\": [0] }";
        mockMvc.perform(post("/api/minigame/play")
                .param("gameId", "tai_xiu")
                .contentType(MediaType.APPLICATION_JSON)
                .content(payload))
                .andExpect(status().isOk())
                // BUG: Phải có trường isValid xác nhận kết quả
                .andExpect(jsonPath("$.isValid").value(true))
                .andExpect(jsonPath("$.dice.length()").value(3));
    }

    // ❌ BUG: Game list phải có 10 game, thực tế chỉ có 5
    @Test
    @DisplayName("testGetGameList_ThatBai_SoLuongGameKhongDu10")
    public void testGetGameList_ThatBai_SoLuongGameKhongDu10() throws Exception {
        mockMvc.perform(get("/api/minigame/list")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                // BUG: Spec yêu cầu 10 game nhưng API chỉ trả về 5
                .andExpect(jsonPath("$.games.length()").value(10));
    }

    // ❌ BUG: Đọc từ fail CSV - test toàn bộ danh sách bug
    @ParameterizedTest(name = "[{index}] {0} | {1} | {7}")
    @CsvFileSource(resources = "/data/minigame/minigame_fail_test.csv", numLinesToSkip = 1)
    @DisplayName("testMinigame_ThatBai_PhatHienBug_TuFile")
    public void testMinigame_ThatBai_PhatHienBug_TuFile(
            String testCase, String gameId, int userId,
            String answers, int expectedStatus,
            String expectedField, String expectedValue, String description) throws Exception {

        // Tất cả các test này đều FAIL vì API chưa implement đúng
        String answersJson = (answers == null || answers.equals("null")) ? "null" : answers;
        String payload = "{ \"userId\": " + userId + ", \"answers\": " + answersJson + " }";

        mockMvc.perform(post("/api/minigame/play")
                .param("gameId", gameId.trim())
                .contentType(MediaType.APPLICATION_JSON)
                .content(payload))
                .andExpect(status().isOk())
                // Kỳ vọng trường bug tồn tại - sẽ FAIL vì API chưa có
                .andExpect(jsonPath("$." + expectedField).exists());
    }
}
