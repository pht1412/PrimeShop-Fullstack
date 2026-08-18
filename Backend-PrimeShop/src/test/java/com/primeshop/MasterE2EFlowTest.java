package com.primeshop;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import com.jayway.jsonpath.JsonPath;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest
@AutoConfigureMockMvc
public class MasterE2EFlowTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    public void testXuyenTamLien_TuDangNhapDenThanhToan_HoanThien() throws Exception {
        Object orderId = null; 
        String activeToken = null;

        // --- CHẶNG 1: LOGIN ---
        String loginPayload = "{\"username\":\"admin\",\"password\":\"admin\"}";
        MvcResult authResult = mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(loginPayload))
                .andExpect(status().isOk())
                .andReturn();

        String authResponse = authResult.getResponse().getContentAsString();
        String rawToken = JsonPath.read(authResponse, authResponse.contains("data") ? "$.data.token" : "$.token");
        activeToken = "Bearer " + rawToken;
        System.out.println("✅ [AUTH] Đăng nhập thành công.");



        // --- CHẶNG 2: THÊM HÀNG VÀO GIỎ ---
        String cartPayload = "{\"productSlug\": \"iphone-15-hong-1\", \"quantity\": 1}";
        mockMvc.perform(post("/api/cart/add")
                .header("Authorization", activeToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(cartPayload))
                .andExpect(status().isOk());
        System.out.println("✅ [CART] Đã thêm sản phẩm mới vào giỏ sạch.");

        // --- CHẶNG 3: TẠO ĐƠN HÀNG ---
        String orderPayload = "{" +
                "\"fullName\": \"Tổng Tester\"," +
                "\"phoneNumber\": \"0901234567\"," +
                "\"address\": \"Dinh Thống Nhất, Quận 1\"," +
                "\"voucherCodes\": []," + 
                "\"note\": \"Báo cáo giữa kỳ PrimeShop\"" +
                "}";

        MvcResult orderResult = mockMvc.perform(post("/api/order/create")
                .header("Authorization", activeToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(orderPayload))
                .andReturn();

        String orderResponse = orderResult.getResponse().getContentAsString();
        
        if (orderResult.getResponse().getStatus() != 200) {
            System.err.println("❌ LỖI TẠI CHẶNG 3: " + orderResponse);
            throw new AssertionError("Dừng test do lỗi Server 500.");
        }
        try {
            orderId = JsonPath.read(orderResponse, "$.data.orderId");
        } catch (Exception e1) {
            try {
                orderId = JsonPath.read(orderResponse, "$.orderId");
            } catch (Exception e2) {
                try {
                    orderId = JsonPath.read(orderResponse, "$.data.id");
                } catch (Exception e3) {
                    orderId = JsonPath.read(orderResponse, "$.id");
                }
            }
        }

        assertNotNull(orderId, "❌ Không thể rút trích mã đơn hàng từ JSON của Dev!");
        System.out.println("✅ [ORDER] Đơn hàng #" + orderId + " đã được tạo.");

        // --- CHẶNG 3.1: ADMIN DUYỆT ĐƠN ---
        mockMvc.perform(put("/api/order/update-status")
                .header("Authorization", activeToken)
                .param("id", orderId.toString())
                .param("status", "CONFIRMED"))
                .andExpect(status().isOk());
        System.out.println("✅ [APPROVE] Admin đã duyệt đơn hàng.");

        // --- CHẶNG 4: THANH TOÁN ---
        mockMvc.perform(put("/api/order/update-status")
                .header("Authorization", activeToken)
                .param("id", orderId.toString())
                .param("status", "PAID"))
                .andExpect(status().isOk());

        System.out.println("🏆 [MISSION ACCOMPLISHED] VƯỢT 4 TẦNG THÀNH CÔNG!");
    }
}