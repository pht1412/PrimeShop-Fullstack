package com.primeshop.api;

import com.primeshop.core.BaseTest;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvFileSource;
import org.springframework.http.MediaType;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

public class AuthApiTest extends BaseTest {

    // ============ REGISTER TESTS ============

    // Nạp dữ liệu từ file CSV cho các case đăng ký thành công
    @ParameterizedTest
    @CsvFileSource(resources = "/data/auth/testcase_register_success.csv", numLinesToSkip = 1)
    public void testRegister_ThanhCong(String username, String password, String email, String fullName, String phoneNumber) throws Exception {
        
        // Lắp ráp Body JSON
        String payloadJson = String.format(
            "{\"username\": \"%s\", \"password\": \"%s\", \"email\": \"%s\", \"fullName\": \"%s\", \"phoneNumber\": \"%s\", \"role\": \"USER\"}",
            username, password, email, fullName, phoneNumber
        );

        // Bắn API đăng ký
        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(payloadJson))
        
        // Kiểm tra kết quả thành công
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.username").value(username))
                .andExpect(jsonPath("$.email").value(email));
    }

    // Nạp dữ liệu từ file CSV cho các case đăng ký thất bại
    @ParameterizedTest
    @CsvFileSource(resources = "/data/auth/testcase_register_fail.csv", numLinesToSkip = 1)
    public void testRegister_ThatBai_DuLieuKhongHopLe(String username, String password, String email, int expectedHttpCode, String expectedMessage) throws Exception {
        
        // Lắp ráp Body JSON
        String payloadJson = String.format(
            "{\"username\": \"%s\", \"password\": \"%s\", \"email\": \"%s\", \"role\": \"USER\"}",
            username, password, email
        );

        // Bắn API đăng ký
        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(payloadJson))
        
        // Kiểm tra lỗi
                .andExpect(status().is(expectedHttpCode))
                .andExpect(jsonPath("$.error").value(expectedMessage));
    }

    @Test
    public void testRegister_ThieuBody_BaoLoi400() throws Exception {
        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void testRegister_SaiKieuDuLieu_BaoLoi400() throws Exception {
        String payloadJson = "{\"username\": 123, \"password\": true, \"email\": null}";

        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(payloadJson))
                .andExpect(status().isBadRequest());
    }

    // ============ LOGIN TESTS ============

    // Nạp dữ liệu từ file CSV cho các case đăng nhập thành công
    @ParameterizedTest
    @CsvFileSource(resources = "/data/auth/testcase_login_success.csv", numLinesToSkip = 1)
    public void testLogin_ThanhCong(String username, String password) throws Exception {
        
        // Lắp ráp Body JSON
        String payloadJson = String.format(
            "{\"username\": \"%s\", \"password\": \"%s\"}",
            username, password
        );

        // Bắn API đăng nhập
        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(payloadJson))
        
        // Kiểm tra kết quả thành công
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").exists())
                .andExpect(jsonPath("$.user.username").value(username));
    }

    // Nạp dữ liệu từ file CSV cho các case đăng nhập thất bại
    @ParameterizedTest
    @CsvFileSource(resources = "/data/auth/testcase_login_fail.csv", numLinesToSkip = 1)
    public void testLogin_ThatBai_ThongTinKhongHopLe(String username, String password, int expectedHttpCode, String expectedMessage) throws Exception {
        
        // Lắp ráp Body JSON
        String payloadJson = String.format(
            "{\"username\": \"%s\", \"password\": \"%s\"}",
            username, password
        );

        // Bắn API đăng nhập
        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(payloadJson))
        
        // Kiểm tra lỗi
                .andExpect(status().is(expectedHttpCode))
                .andExpect(jsonPath("$.error").value(expectedMessage));
    }

    @Test
    public void testLogin_KhongCoToken_BiChan() throws Exception {
        String payloadJson = "{\"username\": \"testuser\", \"password\": \"password123\"}";

        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(payloadJson))
                .andExpect(status().isUnauthorized());
    }

    @Test
    public void testLogin_ThieuBody_BaoLoi400() throws Exception {
        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    // ============ CHECK-ROLE TESTS ============

    @Test
    public void testCheckRole_ThanhCong() throws Exception {
        mockMvc.perform(get("/api/auth/check-role")
                .header("Authorization", getUserToken())
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    public void testCheckRole_KhongCoToken_BiChan() throws Exception {
        mockMvc.perform(get("/api/auth/check-role")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());
    }
}
