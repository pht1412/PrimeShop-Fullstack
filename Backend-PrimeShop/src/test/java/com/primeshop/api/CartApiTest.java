package com.primeshop.api;

import com.primeshop.core.BaseTest;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class CartApiTest extends BaseTest {

    @Test
    void TC01_addProductToCart() throws Exception {

        mockMvc.perform(post("/api/cart/add")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                {
                  "productSlug":"iphone",
                  "quantity":1
                }
                """))
                .andExpect(status().isOk());
    }

    @Test
    void TC02_addSameProductTwice() throws Exception {

        mockMvc.perform(post("/api/cart/add")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                {
                  "productSlug":"iphone",
                  "quantity":1
                }
                """))
                .andExpect(status().isOk());

        mockMvc.perform(post("/api/cart/add")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                {
                  "productSlug":"iphone",
                  "quantity":1
                }
                """))
                .andExpect(status().isOk());
    }

    @Test
    void TC03_quantityNegative() throws Exception {

        mockMvc.perform(post("/api/cart/add")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                {
                  "productSlug":"iphone",
                  "quantity":-1
                }
                """))
                .andExpect(status().isBadRequest());
    }

    @Test
    void TC04_quantityZero() throws Exception {

        mockMvc.perform(post("/api/cart/add")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                {
                  "productSlug":"iphone",
                  "quantity":0
                }
                """))
                .andExpect(status().isBadRequest());
    }

    @Test
    void TC05_productSlugInvalid() throws Exception {

        mockMvc.perform(post("/api/cart/add")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                {
                  "productSlug":"abcxyz",
                  "quantity":1
                }
                """))
                .andExpect(status().isNotFound());
    }

    @Test
    void TC06_productSlugNull() throws Exception {

        mockMvc.perform(post("/api/cart/add")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                {
                  "productSlug":null,
                  "quantity":1
                }
                """))
                .andExpect(status().isBadRequest());
    }

    @Test
    void TC07_productSlugEmpty() throws Exception {

        mockMvc.perform(post("/api/cart/add")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                {
                  "productSlug":"",
                  "quantity":1
                }
                """))
                .andExpect(status().isBadRequest());
    }
}