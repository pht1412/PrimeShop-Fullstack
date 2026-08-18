package com.primeshop.api;

import com.primeshop.core.BaseTest;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvFileSource;
import org.springframework.http.MediaType;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

public class PaymentApiTest extends BaseTest {

    // Khai báo chạy test với data từ file CSV, bỏ qua dòng 1 (tiêu đề)
    @ParameterizedTest
    @CsvFileSource(resources = "/data/payment/testcase_create_success.csv", numLinesToSkip = 1)
    public void testCreateVNPayUrl_ThanhCong(Long orderId, Long amount, String expectedUrlStart) throws Exception {
        
        // 1. Lắp ráp Body JSON từ file CSV (Khớp với class PaymentRequest của em)
        String payloadJson = String.format("{\"orderId\": %d, \"amount\": %d}", orderId, amount);

        // 2. Bắn API tạo link thanh toán (Sửa lại đúng endpoint của em)
        mockMvc.perform(post("/api/payment/vnpay/create")
                .header("Authorization", getUserToken()) // Tự động lấy quyền User
                .contentType(MediaType.APPLICATION_JSON)
                .content(payloadJson))
                
        // 3. Kiểm tra (Assert)
                .andExpect(status().isOk())
                // Tuyệt chiêu của Senior: Chỉ kiểm tra URL bắt đầu bằng domain VNPay
                .andExpect(jsonPath("$.paymentUrl", Matchers.startsWith(expectedUrlStart)));
    }

    // Nạp dữ liệu từ file CSV chứa các case thất bại
    @ParameterizedTest
    @CsvFileSource(resources = "/data/payment/testcase_create_fail.csv", numLinesToSkip = 1)
    public void testCreateVNPayUrl_ThatBai_LoiDuLieu(Long orderId, Long amount, int expectedHttpCode, String expectedMessage) throws Exception {
        
        // 1. Lắp ráp Body JSON từ file CSV
        String payloadJson = String.format("{\"orderId\": %d, \"amount\": %d}", orderId, amount);

        // 2. Bắn API tạo link thanh toán
        mockMvc.perform(post("/api/payment/vnpay/create")
                .header("Authorization", getUserToken()) // Tự động lấy quyền User
                .contentType(MediaType.APPLICATION_JSON)
                .content(payloadJson))
                
        // 3. Kiểm tra (Assert) kết quả thất bại
                .andExpect(status().is(expectedHttpCode)) // Kỳ vọng trả về lỗi (Ví dụ: 400 Bad Request)
                .andExpect(jsonPath("$.message").value(expectedMessage)); // Kỳ vọng Backend phải báo lỗi chuẩn
    }

    @Test
    public void testCreateVNPayUrl_KhongCoToken_BiChan() throws Exception {
        String payloadJson = "{\"orderId\": 10058, \"amount\": 30990000}";

        mockMvc.perform(post("/api/payment/vnpay/create")
               .contentType(MediaType.APPLICATION_JSON)
               .content(payloadJson))
               .andExpect(status().isUnauthorized());
    }

    @Test
    public void testCreateVNPayUrl_ThieuBody_BaoLoi400() throws Exception {
        mockMvc.perform(post("/api/payment/vnpay/create")
                .header("Authorization", getUserToken())
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void testCreateVNPayUrl_SaiKieuDuLieu_BaoLoi400() throws Exception {
        String payloadJson = "{\"orderId\": \"10058\", \"amount\": \"ba_muoi_trieu\"}";

        mockMvc.perform(post("/api/payment/vnpay/create")
                .header("Authorization", getUserToken())
                .contentType(MediaType.APPLICATION_JSON)
                .content(payloadJson))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void testCallback_ThanhCong_RedirectVeTrangSuccess() throws Exception {
        String validHash = "chu_ky_hop_le";

        mockMvc.perform(get("/api/payment/vnpay/return")
                .param("vnp_TxnRef", "10058")
                .param("vnp_Amount", "3099000000")
                .param("vnp_ResponseCode", "00")
                .param("vnp_SecureHash", validHash))
                .andExpect(status().isFound())
                .andExpect(header().string("Location", Matchers.containsString("/payment/success")));
    }

    @Test
    public void testCallback_DonHangKhongTonTai_RedirectVeTrangLoi() throws Exception {
        mockMvc.perform(get("/api/payment/vnpay/return")
                .param("vnp_TxnRef", "9999999")
                .param("vnp_ResponseCode", "00") 
                .param("vnp_SecureHash", "hash_gia_mao_cho_le_nhanh"))
                .andExpect(status().isFound())
                .andExpect(header().string("Location", Matchers.containsString("/payment/failed")));
    }

    @Test
    public void testCallback_SaiChuKy_RedirectVeTrangLoi() throws Exception {
        mockMvc.perform(get("/api/payment/vnpay/return")
               .param("vnp_TxnRef", "10058")
               .param("vnp_Amount", "30990000")
               .param("vnp_ResponseCode", "00")
               .param("vnp_SecureHash", "chu_ky_gia_mao_cua_hacker_12345"))
               .andExpect(status().isFound())
               .andExpect(header().string("Location", Matchers.containsString("/payment/failed")));
    }

    @Test
    public void testCallback_KhachHangHuy_RedirectVeTrangLoi() throws Exception {
        mockMvc.perform(get("/api/payment/vnpay/return")
                .param("vnp_TxnRef", "10059")
                .param("vnp_ResponseCode", "24") 
                .param("vnp_SecureHash", "hash_tam_thoi"))
                .andExpect(status().isFound())
                .andExpect(header().string("Location", Matchers.containsString("/payment/failed")));
    }

    @Test
    public void testCallback_SQLInjection_BiChan() throws Exception {
        mockMvc.perform(get("/api/payment/vnpay/return")
                .param("vnp_TxnRef", "10058")
                .param("vnp_OrderInfo", "'; DROP TABLE payment_transaction;--")
                .param("vnp_ResponseCode", "00")
                .param("vnp_SecureHash", "hash_bat_ky"))         
                .andExpect(status().isFound())
                .andExpect(header().string("Location", Matchers.containsString("/payment/failed")));
    }
}