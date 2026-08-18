package com.primeshop.api;

import com.primeshop.core.BaseTest;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvFileSource;


import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;


public class AdminSellerApiTest extends BaseTest {

        @ParameterizedTest(name = "{0}")
        @CsvFileSource(resources = "/data/admin/seller_test.csv", numLinesToSkip = 1)
        void testSellerApi(String testCase,
                        Long sellerId,
                        Long productId,
                        int expectedStatus) throws Exception {

                if (testCase.equals("getAllSellers")) {

                        mockMvc.perform(get("/api/admin/seller/all")
                                        .header("Authorization", getAdminToken()))
                                        .andDo(print())
                                        .andExpect(status().is(expectedStatus));
                }

                if (testCase.equals("getPendingSellers")) {

                        mockMvc.perform(get("/api/admin/seller/pending-registrations")
                                        .header("Authorization", getAdminToken()))
                                        .andDo(print())
                                        .andExpect(status().is(expectedStatus));
                }

                if (testCase.equals("approveSellerInvalid")) {

                        mockMvc.perform(patch("/api/admin/seller/approve-registration")
                                        .param("sellerId", String.valueOf(sellerId))
                                        .header("Authorization", getAdminToken()))
                                        .andDo(print())
                                        .andExpect(status().is(expectedStatus));
                }

                if (testCase.equals("banSeller")) {

                        mockMvc.perform(patch("/api/admin/seller/ban-seller")
                                        .param("sellerId", String.valueOf(sellerId))
                                        .header("Authorization", getAdminToken()))
                                        .andDo(print())
                                        .andExpect(status().is(expectedStatus));
                }
        }
}       