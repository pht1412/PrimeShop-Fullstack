package com.primeshop.api;

import com.primeshop.core.BaseTest;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@Transactional
public class SellerApiTest extends BaseTest {

    // =============================
    // APPLY SELLER
    // =============================

    // TC-S01 Apply seller success
    @Test
    void TC_S01_applySeller_success() throws Exception {

        String json = """
        {
          "shopName":"Tech Shop",
          "identityCard":"123456789",
          "phone":"0900000000",
          "description":"Test seller"
        }
        """;

        mockMvc.perform(post("/api/seller/apply")
                .header("Authorization", getUserToken())
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
                .andExpect(status().isOk());
    }

    // TC-S02 Apply seller duplicate
    @Test
    void TC_S02_applySeller_duplicate() throws Exception {

        String json = """
        {
          "shopName":"Tech Shop",
          "identityCard":"123456789"
        }
        """;

        mockMvc.perform(post("/api/seller/apply")
                .header("Authorization", getUserToken())
                .contentType(MediaType.APPLICATION_JSON)
                .content(json));

        mockMvc.perform(post("/api/seller/apply")
                .header("Authorization", getUserToken())
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
                .andExpect(status().isBadRequest());
    }

    // TC-S03 Apply seller without login
    @Test
    void TC_S03_applySeller_noLogin() throws Exception {

        mockMvc.perform(post("/api/seller/apply"))
                .andExpect(status().isUnauthorized());
    }

    // =============================
    // ADD PRODUCT
    // =============================

    // TC-S04 Add product success
    @Test
    void TC_S04_addProduct_success() throws Exception {

        String json = """
        {
          "name":"Laptop",
          "price":10000,
          "description":"Gaming laptop"
        }
        """;

        mockMvc.perform(post("/api/seller/add-product")
                .param("sellerId","1")
                .header("Authorization", getSellerToken())
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
                .andExpect(status().isOk());
    }

    // TC-S05 Add product without seller role
    @Test
    void TC_S05_addProduct_wrongRole() throws Exception {

        String json = """
        {
          "name":"Laptop",
          "price":10000
        }
        """;

        mockMvc.perform(post("/api/seller/add-product")
                .param("sellerId","1")
                .header("Authorization", getUserToken())
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
                .andExpect(status().isForbidden());
    }

    // TC-S06 Add product invalid data
    @Test
    void TC_S06_addProduct_invalidData() throws Exception {

        String json = """
        {
          "name":"",
          "price":-100
        }
        """;

        mockMvc.perform(post("/api/seller/add-product")
                .param("sellerId","1")
                .header("Authorization", getSellerToken())
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
                .andExpect(status().isBadRequest());
    }

    // =============================
    // GET SELLER PRODUCTS
    // =============================

    // TC-S07 Get seller products
    @Test
    void TC_S07_getSellerProducts() throws Exception {

        mockMvc.perform(get("/api/seller/products")
                .param("sellerId","1")
                .header("Authorization", getSellerToken()))
                .andExpect(status().isOk());
    }

    // TC-S08 Pagination test
    @Test
    void TC_S08_getSellerProducts_pagination() throws Exception {

        mockMvc.perform(get("/api/seller/products")
                .param("sellerId","1")
                .param("page","0")
                .param("size","10")
                .header("Authorization", getSellerToken()))
                .andExpect(status().isOk());
    }

    // =============================
    // UPDATE PRODUCT
    // =============================

    // TC-S09 Update product success
    @Test
void TC09_adminApproveProduct() throws Exception {

    mockMvc.perform(patch("/api/admin/seller/approve-products")
            .param("sellerId", "3002")
            .param("productId", "182")
            .header("Authorization", getAdminToken()))
            .andExpect(status().isOk());
}

    // TC-S10 Update product not found
    @Test
    void TC_S10_updateProduct_notFound() throws Exception {

        String json = """
        {
          "name":"Updated Laptop"
        }
        """;

        mockMvc.perform(patch("/api/seller/update-product")
                .param("id","999")
                .header("Authorization", getSellerToken())
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
                .andExpect(status().isBadRequest());
    }

    // =============================
    // SELLER PROFILE
    // =============================

    // TC-S11 Get seller profile
    @Test
    void TC_S11_getSellerProfile() throws Exception {

        mockMvc.perform(get("/api/seller/me")
                .header("Authorization", getSellerToken()))
                .andExpect(status().isOk());
    }

    // TC-S12 Get seller profile without login
    @Test
    void TC_S12_getSellerProfile_noLogin() throws Exception {

        mockMvc.perform(get("/api/seller/me"))
                .andExpect(status().isUnauthorized());
    }

}