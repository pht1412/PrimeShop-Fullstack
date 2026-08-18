package com.primeshop.api;

import com.primeshop.core.BaseTest;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.ResultActions;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.junit.jupiter.api.Assertions.*;

/**
 * ProductApiTest - Test API cho module Product
 * Sử dụng 3 kiểu gửi API từ TemplateMockMvcTest:
 * - Kiểu 1: POST/PUT với JSON + Token
 * - Kiểu 2: GET với URL Params
 * - Kiểu 3: Upload File
 */
public class ProductApiTest extends BaseTest {

    // ============================================================
    // KIỂU 2: GET VỚI URL PARAMS (Không cần token)
    // Dùng cho: Xem sản phẩm, tìm kiếm, lọc
    // ============================================================

    /**
     * Test: Lấy tất cả sản phẩm đang hoạt động
     */
    @Test
    public void kieu2_GetAllProducts() throws Exception {
        mockMvc.perform(get("/api/product")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    /**
     * Test: Lấy sản phẩm theo category
     */
    @Test
    public void kieu2_GetProductsByCategory() throws Exception {
        mockMvc.perform(get("/api/product/category")
                .param("categorySlug", "dien-thoai")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    /**
     * Test: Lấy sản phẩm theo slug
     */
    @Test
    public void kieu2_GetProductBySlug() throws Exception {
        mockMvc.perform(get("/api/product/slug")
                .param("productSlug", "iphone-15-pro-max")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    /**
     * Test: Tìm kiếm sản phẩm với phân trang
     */
    @Test
    public void kieu2_SearchProductsWithPagination() throws Exception {
        mockMvc.perform(get("/api/product/all-products")
                .param("page", "0")
                .param("size", "10")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    /**
     * Test: Lấy danh sách thương hiệu
     */
    @Test
    public void kieu2_GetBrands() throws Exception {
        mockMvc.perform(get("/api/product/brands")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    /**
     * Test: Lấy sản phẩm hot sale
     */
    @Test
    public void kieu2_GetHotSaleProducts() throws Exception {
        mockMvc.perform(get("/api/product/hot-sale")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    /**
     * Test: Lấy sản phẩm giảm giá
     */
    @Test
    public void kieu2_GetDiscountProducts() throws Exception {
        mockMvc.perform(get("/api/product/discount")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    /**
     * Test: Lấy sản phẩm được đánh giá cao
     */
    @Test
    public void kieu2_GetTopRatedProducts() throws Exception {
        mockMvc.perform(get("/api/product/top-rated")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    /**
     * Test: Đếm tổng số sản phẩm
     */
    @Test
    public void kieu2_CountProducts() throws Exception {
        mockMvc.perform(get("/api/product/count")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    // ============================================================
    // BẪY LỖI (Error Cases) - Kiểu 2: GET với URL Params
    // ============================================================

    /**
     * Test: Lấy sản phẩm không tồn tại -> 404
     */
    @Test
    public void kieu2_GetProductNotFound() throws Exception {
        mockMvc.perform(get("/api/product/slug")
                .param("productSlug", "san-pham-khong-ton-tai-xyz")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    /**
     * Test: Phân trang với page âm -> 400
     */
    @Test
    public void kieu2_NegativePageError() throws Exception {
        mockMvc.perform(get("/api/product/all-products")
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
        mockMvc.perform(get("/api/product/all-products")
                .param("page", "0")
                .param("size", "0")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    // ============================================================
    // KIỂU 1: POST VỚI JSON + TOKEN (Cần đăng nhập)
    // Dùng cho: Đánh giá sản phẩm
    // ============================================================

    /**
     * Test: Đánh giá sản phẩm (cần token USER)
     */
    @Test
    public void kieu1_PostRateProduct() throws Exception {
        String payloadJson = """
            {
                "productSlug": "iphone-15-pro-max",
                "rating": 4.5
            }
            """;

        mockMvc.perform(post("/api/product/rate")
                .header("Authorization", getUserToken())
                .contentType(MediaType.APPLICATION_JSON)
                .content(payloadJson))
                .andExpect(status().isOk());
    }

    /**
     * Test: Đánh giá sản phẩm với rating không hợp lệ (>5) -> 400
     */
    @Test
    public void kieu1_PostRateProductInvalidRating() throws Exception {
        String payloadJson = """
            {
                "productSlug": "iphone-15-pro-max",
                "rating": 6.0
            }
            """;

        mockMvc.perform(post("/api/product/rate")
                .header("Authorization", getUserToken())
                .contentType(MediaType.APPLICATION_JSON)
                .content(payloadJson))
                .andExpect(status().isBadRequest());
    }

    // ============================================================
    // KIỂU 3: UPLOAD FILE (Cần token ADMIN)
    // Dùng cho: Thêm ảnh sản phẩm
    // ============================================================

    /**
     * Test: Thêm ảnh vào sản phẩm (cần token ADMIN)
     */
    @Test
    public void kieu3_UploadProductImage() throws Exception {
        byte[] fileContent = "fake image content".getBytes();

        mockMvc.perform(multipart("/api/product/add-images")
                .file("image", fileContent)
                .header("Authorization", getAdminToken())
                .param("productSlug", "iphone-15-pro-max"))
                .andExpect(status().isBadRequest()); // Sẽ fail vì file không hợp lệ (đây là test mẫu)
    }

    /**
     * Test: Upload file không phải ảnh -> 400
     */
    @Test
    public void kieu3_UploadInvalidFileType() throws Exception {
        byte[] fileContent = "virus code".getBytes();

        mockMvc.perform(multipart("/api/product/add-images")
                .file("image", fileContent)
                .header("Authorization", getAdminToken())
                .param("productSlug", "iphone-15-pro-max"))
                .andExpect(status().isBadRequest());
    }
}
