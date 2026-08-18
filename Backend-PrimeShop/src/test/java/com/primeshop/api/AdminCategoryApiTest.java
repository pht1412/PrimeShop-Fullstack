package com.primeshop.api;

import com.primeshop.core.BaseTest;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvFileSource;
import org.springframework.http.MediaType;


import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;


public class AdminCategoryApiTest extends BaseTest {

        @ParameterizedTest(name = "{0}")
        @CsvFileSource(resources = "/data/admin/category_test.csv", numLinesToSkip = 1)
        void testCategoryApi(String testCase,
                        String name,
                        String slug,
                        int expectedStatus) throws Exception {

                if (testCase.equals("createCategoryValid")) {

                        String json = """
                                        {
                                          "name":"%s",
                                          "specs":["RAM","CPU"]
                                        }
                                        """.formatted(name);

                        mockMvc.perform(post("/api/admin/category/add")
                                        .header("Authorization", getAdminToken())
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(json))
                                        .andDo(print())
                                        .andExpect(status().is(expectedStatus));
                }

                if (testCase.equals("getAllCategories")) {

                        mockMvc.perform(get("/api/admin/category/all")
                                        .header("Authorization", getAdminToken()))
                                        .andDo(print())
                                        .andExpect(status().is(expectedStatus));
                }

                if (testCase.equals("updateCategoryNotExist")) {

                        String json = """
                                        {
                                          "name":"Test",
                                          "slug":"not-exist",
                                          "specs":[]
                                        }
                                        """;

                        mockMvc.perform(post("/api/admin/category/update")
                                        .header("Authorization", getAdminToken())
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(json))
                                        .andDo(print())
                                        .andExpect(status().is(expectedStatus));
                }
        }
}