package com.primeshop.api;

import com.primeshop.core.BaseTest;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvFileSource;
import org.springframework.http.MediaType;


import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;


public class AdminProductApiTest extends BaseTest {

        @ParameterizedTest(name = "{0}")
        @CsvFileSource(resources = "/data/admin/product_test.csv", numLinesToSkip = 1)
        void testProductApi(String testCase,
                        Long sellerId,
                        Long productId,
                        int expectedStatus) throws Exception {

                if (testCase.equals("addProductValid")) {

                        String json = """
                                        {
                                          "name":"Test Product",
                                          "brand":"Apple",
                                          "price":1000
                                        }
                                        """;

                        mockMvc.perform(post("/api/admin/product/add")
                                        .param("sellerId", String.valueOf(sellerId))
                                        .header("Authorization", getAdminToken())
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(json))
                                        .andDo(print())
                                        .andExpect(status().is(expectedStatus));
                }

                if (testCase.equals("deactiveProduct")) {

                        mockMvc.perform(post("/api/admin/product/deactive")
                                        .param("id", String.valueOf(productId))
                                        .header("Authorization", getAdminToken()))
                                        .andDo(print())
                                        .andExpect(status().is(expectedStatus));
                }

                if (testCase.equals("getActiveProducts")) {

                        mockMvc.perform(get("/api/admin/product/is-active")
                                        .header("Authorization", getAdminToken()))
                                        .andDo(print())
                                        .andExpect(status().is(expectedStatus));
                }

                if (testCase.equals("getBrands")) {

                        mockMvc.perform(get("/api/admin/product/brands")
                                        .header("Authorization", getAdminToken()))
                                        .andDo(print())
                                        .andExpect(status().is(expectedStatus));
                }
        }
}