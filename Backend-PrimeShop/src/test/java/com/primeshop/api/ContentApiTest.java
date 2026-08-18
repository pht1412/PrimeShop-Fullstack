package com.primeshop.api;

import com.primeshop.core.BaseTest;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * ContentApiTest - Test API cho module Content (Tin tức/Bài viết)
 * Sử dụng 3 kiểu gửi API từ BaseTest:
 * - Kiểu 1: POST/PUT với JSON + Token
 * - Kiểu 2: GET với URL Params
 * - Kiểu 3: Upload File
 */
public class ContentApiTest extends BaseTest {

    // ============================================================
    // KIỂU 2: GET VỚI URL PARAMS (Không cần token)
    // Dùng cho: Xem tin tức, tìm kiếm, lọc
    // ============================================================

    /**
     * Test: Lấy tất cả tin tức với phân trang
     */
    @Test
    public void kieu2_GetAllContent() throws Exception {
        mockMvc.perform(get("/api/news")
                .param("page", "0")
                .param("size", "10")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    /**
     * Test: Lấy tin tức theo danh mục
     */
    @Test
    public void kieu2_GetContentByCategory() throws Exception {
        mockMvc.perform(get("/api/news")
                .param("categoryId", "1")
                .param("page", "0")
                .param("size", "10")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    /**
     * Test: Tìm kiếm tin tức
     */
    @Test
    public void kieu2_SearchContent() throws Exception {
        mockMvc.perform(get("/api/news")
                .param("search", "iPhone")
                .param("page", "0")
                .param("size", "10")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    /**
     * Test: Lấy tin tức theo ID
     */
    @Test
    public void kieu2_GetContentById() throws Exception {
        mockMvc.perform(get("/api/news/1")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    /**
     * Test: Lấy tin tức theo slug
     */
    @Test
    public void kieu2_GetContentBySlug() throws Exception {
        mockMvc.perform(get("/api/news/slug/tin-cong-nghe-2024")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    /**
     * Test: Đếm tổng số tin tức
     */
    @Test
    public void kieu2_CountContent() throws Exception {
        mockMvc.perform(get("/api/news/count")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    // ============================================================
    // BẪY LỖI (Error Cases) - Kiểu 2: GET với URL Params
    // ============================================================

    /**
     * Test: Lấy tin tức không tồn tại -> 404
     */
    @Test
    public void kieu2_GetContentNotFound() throws Exception {
        mockMvc.perform(get("/api/news/99999")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    /**
     * Test: Lấy tin tức theo slug không tồn tại -> 404
     */
    @Test
    public void kieu2_GetContentByNonExistentSlug() throws Exception {
        mockMvc.perform(get("/api/news/slug/tin-khong-ton-tai-xyz")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    /**
     * Test: Phân trang với page âm -> 400
     */
    @Test
    public void kieu2_NegativePageError() throws Exception {
        mockMvc.perform(get("/api/news")
                .param("page", "-1")
                .param("size", "10")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    /**
     * Test: Phân trang với size = 0 -> 400
     */
    @Test
    public void kieu2_ZeroSizeError() throws Exception {
        mockMvc.perform(get("/api/news")
                .param("page", "0")
                .param("size", "0")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    // ============================================================
    // KIỂU 1: POST VỚI JSON + TOKEN (Cần token ADMIN)
    // Dùng cho: Tạo, cập nhật, xóa tin tức
    // ============================================================

    /**
     * Test: Tạo tin tức mới (cần token ADMIN)
     */
    @Test
    public void kieu1_CreateContent() throws Exception {
        String payloadJson = """
            {
                "title": "Tin công nghệ mới",
                "textUrl": "https://example.com/article",
                "excerpt": "Tóm tắt tin tức",
                "imageUrl": "https://example.com/image.jpg",
                "categoryId": 1,
                "status": "ACTIVE"
            }
            """;

        mockMvc.perform(post("/api/news")
                .header("Authorization", getAdminToken())
                .contentType(MediaType.APPLICATION_JSON)
                .content(payloadJson))
                .andExpect(status().isCreated());
    }

    /**
     * Test: Cập nhật tin tức (cần token ADMIN)
     */
    @Test
    public void kieu1_UpdateContent() throws Exception {
        String payloadJson = """
            {
                "title": "Tin đã cập nhật",
                "textUrl": "https://example.com/updated-article",
                "excerpt": "Tóm tắt đã cập nhật",
                "imageUrl": "https://example.com/updated-image.jpg",
                "categoryId": 1,
                "status": "ACTIVE"
            }
            """;

        mockMvc.perform(put("/api/news/1")
                .header("Authorization", getAdminToken())
                .contentType(MediaType.APPLICATION_JSON)
                .content(payloadJson))
                .andExpect(status().isOk());
    }

    /**
     * Test: Xóa tin tức (cần token ADMIN)
     */
    @Test
    public void kieu1_DeleteContent() throws Exception {
        mockMvc.perform(delete("/api/news/1")
                .header("Authorization", getAdminToken()))
                .andExpect(status().isNoContent());
    }

    /**
     * Test: Tạo tin tức với dữ liệu thiếu -> 400
     */
    @Test
    public void kieu1_CreateContentWithMissingData() throws Exception {
        String payloadJson = """
            {
                "title": ""
            }
            """;

        mockMvc.perform(post("/api/news")
                .header("Authorization", getAdminToken())
                .contentType(MediaType.APPLICATION_JSON)
                .content(payloadJson))
                .andExpect(status().isBadRequest());
    }

    /**
     * Test: Tạo tin tức với status không hợp lệ -> 400
     */
    @Test
    public void kieu1_CreateContentWithInvalidStatus() throws Exception {
        String payloadJson = """
            {
                "title": "Tin công nghệ",
                "textUrl": "https://example.com/article",
                "status": "INVALID_STATUS"
            }
            """;

        mockMvc.perform(post("/api/news")
                .header("Authorization", getAdminToken())
                .contentType(MediaType.APPLICATION_JSON)
                .content(payloadJson))
                .andExpect(status().isBadRequest());
    }

    /**
     * Test: Cập nhật tin tức không tồn tại -> 404
     */
    @Test
    public void kieu1_UpdateNonExistentContent() throws Exception {
        String payloadJson = """
            {
                "title": "Tin đã cập nhật",
                "textUrl": "https://example.com/updated",
                "status": "ACTIVE"
            }
            """;

        mockMvc.perform(put("/api/news/99999")
                .header("Authorization", getAdminToken())
                .contentType(MediaType.APPLICATION_JSON)
                .content(payloadJson))
                .andExpect(status().isNotFound());
    }

    /**
     * Test: Xóa tin tức không tồn tại -> 404
     */
    @Test
    public void kieu1_DeleteNonExistentContent() throws Exception {
        mockMvc.perform(delete("/api/news/99999")
                .header("Authorization", getAdminToken()))
                .andExpect(status().isNotFound());
    }
}
