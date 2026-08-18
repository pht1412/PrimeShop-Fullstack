package com.primeshop.api;

import com.primeshop.core.BaseTest;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvFileSource;


import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;

public class AdminOrderApiTest extends BaseTest {

        @ParameterizedTest(name = "{0}")
        @CsvFileSource(resources = "/data/admin/order_test.csv", numLinesToSkip = 1)
        void testOrderApi(String testCase,
                        Long orderId,
                        String status,
                        int expectedStatus) throws Exception {

                if (testCase.equals("getAllOrders")) {

                        mockMvc.perform(get("/api/admin/order/get-all")
                                        .header("Authorization", getAdminToken()))
                                        .andDo(print())
                                        .andExpect(status().is(expectedStatus));
                }

                if (testCase.equals("getOrderValid") || testCase.equals("getOrderInvalid")) {

                        mockMvc.perform(get("/api/admin/order/get")
                                        .param("id", String.valueOf(orderId))
                                        .header("Authorization", getAdminToken()))
                                        .andDo(print())
                                        .andExpect(status().is(expectedStatus));
                }

                if (testCase.equals("updateStatusValid")) {

                        mockMvc.perform(put("/api/admin/order/update-status")
                                        .param("id", String.valueOf(orderId))
                                        .param("status", status)
                                        .header("Authorization", getAdminToken()))
                                        .andDo(print())
                                        .andExpect(status().is(expectedStatus));
                }

                if (testCase.equals("filterOrders")) {

                        String body = """
                                        {
                                          "userId": 1
                                        }
                                        """;

                        mockMvc.perform(post("/api/admin/order/filter")
                                        .header("Authorization", getAdminToken())
                                        .contentType("application/json")
                                        .content(body))
                                        .andDo(print())
                                        .andExpect(status().is(expectedStatus));
                }
        }
}