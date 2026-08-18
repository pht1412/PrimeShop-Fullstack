package com.primeshop.core;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;
import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.PathNotFoundException;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(classes = com.primeshop.Application.class) 
@AutoConfigureMockMvc
@ActiveProfiles("test") // "Vòng kim cô" ép dùng DB Test
@Transactional          // Dọn rác sau mỗi bài test
public abstract class BaseTest {

    @Autowired
    protected MockMvc mockMvc; 

    // Biến lưu trữ Token để tái sử dụng, tránh việc gọi API Login liên tục làm chậm Test
    private String cachedAdminToken;
    private String cachedUserToken;

    /**
     * Tự động lấy Token Admin "Tươi sống" từ API
     */
    protected String getAdminToken() throws Exception {
        if (cachedAdminToken != null) {
            return cachedAdminToken; // Nếu có rồi thì dùng luôn
        }

        System.out.println("🔐 [BASE TEST] Đang tự động cấp phát Token cho ADMIN...");
        String loginPayload = "{\"username\":\"admin\",\"password\":\"admin\"}";
        
        MvcResult authResult = mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(loginPayload))
                .andExpect(status().isOk())
                .andReturn();

        String authResponse = authResult.getResponse().getContentAsString();
        
        // Rút trích Token thông minh như trong MasterE2EFlowTest
        String rawToken;
        try {
            rawToken = JsonPath.read(authResponse, "$.data.token");
        } catch (PathNotFoundException e) {
            rawToken = JsonPath.read(authResponse, "$.token");
        }

        cachedAdminToken = "Bearer " + rawToken;
        return cachedAdminToken;
    }

    /**
     * Tự động lấy Token User "Tươi sống" từ API (Sếp nhớ sửa lại username/pass của User nhé)
     */
    protected String getUserToken() throws Exception {
        if (cachedUserToken != null) {
            return cachedUserToken;
        }

        System.out.println("🔐 [BASE TEST] Đang tự động cấp phát Token cho USER...");
        // Giả sử có tài khoản user là: user01/user01. Sếp xem trong DataInitializer để biết chính xác nhé!
        String loginPayload = "{\"username\":\"user01\",\"password\":\"user01\"}";
        
        MvcResult authResult = mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(loginPayload))
                .andExpect(status().isOk())
                .andReturn();

        String authResponse = authResult.getResponse().getContentAsString();
        
        String rawToken;
        try {
            rawToken = JsonPath.read(authResponse, "$.data.token");
        } catch (PathNotFoundException e) {
            rawToken = JsonPath.read(authResponse, "$.token");
        }

        cachedUserToken = "Bearer " + rawToken;
        return cachedUserToken;
    }
        protected String getSellerToken() {
        return "Bearer eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJ0ZXN0MDQiLCJyb2xlcyI6WyJST0xFX0JVU1NJTkVTUyIsIlJPTEVfVVNFUiJdLCJpYXQiOjE3NzMxNjA4ODQsImV4cCI6MTc3MzI0NzI4NH0.DYk-2nKNkkOd9dpnPI1FD6Q0cIkw-8-W7Olv5UNY6iE";
    }
}