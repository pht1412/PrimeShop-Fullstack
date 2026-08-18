package com.primeshop.api;

import com.primeshop.core.BaseTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test; // Dùng cho TC04 (Test thường)
import org.junit.jupiter.params.ParameterizedTest; // Dùng cho CSV (Test tham số)
import org.junit.jupiter.params.provider.CsvFileSource; // Để đọc file .csv
import org.junit.jupiter.params.provider.CsvSource; // Để dùng @CsvSource trực tiếp
import org.springframework.http.MediaType;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

public class AuthApiTesttemp extends BaseTest {

    @ParameterizedTest
    @CsvFileSource(resources = "/data/auth/auth_test.csv", numLinesToSkip = 1)
    @DisplayName("Kiểm thử Đăng nhập hàng loạt từ file CSV (Data-Driven)")
    public void testLoginWithCsv(String username, String password, int expectedStatus) throws Exception {

        // 1. Chuẩn bị JSON Body từ dữ liệu CSV
        String loginJson = String.format("{\"username\":\"%s\", \"password\":\"%s\"}",
                username == null ? "" : username,
                password);

        // 2. Bắn API và kiểm tra Status Code linh hoạt theo cột expectedStatus
        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(loginJson))
                .andDo(print()) // In ra log để dễ debug nếu có lỗi
                .andExpect(status().is(expectedStatus));
    }

    @ParameterizedTest
    @DisplayName("TC03: Đăng ký user trùng lặp (Giữ nguyên logic đặc thù)")
    @CsvSource({
            "admin, password123, admin@gmail.com, 400"
    })
    public void registerFail_DuplicateUsername(String username, String pass, String email, int status)
            throws Exception {
        String registerJson = String.format(
                "{\"username\":\"%s\",\"password\":\"%s\",\"email\":\"%s\",\"role\":\"USER\"}",
                username, pass, email);

        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(registerJson))
                .andExpect(status().is(status))
                .andDo(print())
                .andExpect(jsonPath("$.error").exists());
    }

    @Test
    @DisplayName("TC04: Kiểm tra quyền Admin thành công")
    public void checkRole_Success() throws Exception {
        // Đăng nhập lấy token trước
        String loginJson = "{\"username\":\"admin\", \"password\":\"admin\"}";
        String response = mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(loginJson))
                .andReturn().getResponse().getContentAsString();
        String token = response.split("\"token\":\"")[1].split("\"")[0];

        mockMvc.perform(get("/api/auth/check-role")
                .header("Authorization", "Bearer " + token))
                .andDo(print())
                .andExpect(status().isOk());
    }
    @ParameterizedTest
    @CsvFileSource(resources = "/data/auth/register_test.csv", numLinesToSkip = 1)
    @DisplayName("Kiểm thử Đăng ký hàng loạt từ file CSV (Data-Driven)")
    public void testRegisterWithCsv(String username, String password, String email, String role, int expectedStatus, String description) throws Exception {
        
        // Tạo JSON body linh hoạt (xử lý trường hợp null để bẫy lỗi 400)
        String registerJson = String.format(
                "{\"username\":%s, \"password\":%s, \"email\":%s, \"role\":\"%s\"}",
                username == null ? null : "\"" + username + "\"",
                password == null ? null : "\"" + password + "\"",
                email == null ? null : "\"" + email + "\"",
                role);

        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(registerJson))
                .andDo(print()) // Quan trọng: In bằng chứng ra Console
                .andExpect(status().is(expectedStatus));
    }
}