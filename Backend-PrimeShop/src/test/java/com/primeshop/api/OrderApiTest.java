package com.primeshop.api;

import org.junit.jupiter.api.Test;
import org.springframework.security.test.context.support.WithMockUser;
import com.primeshop.core.BaseTest;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import org.springframework.http.MediaType;

public class OrderApiTest extends BaseTest{

    // =========================
    // GET ORDERS BY USER
    // =========================

    @Test
    void TC01_GetOrdersByUser() throws Exception {

        mockMvc.perform(get("/api/order/get")
                .header("Authorization", getUserToken()))
                .andExpect(status().isOk());
    }

    // =========================
    // CREATE ORDER
    // =========================

    @Test
    void TC02_CreateOrder_InvalidBody() throws Exception {

        String cartJson = """
        {
            "productSlug": "sony-inzone-h9-whg900n",
            "quantity": 1
        }
        """;

        // add cart trước
        mockMvc.perform(post("/api/cart/add")
                .header("Authorization", getUserToken())
                .contentType(MediaType.APPLICATION_JSON)
                .content(cartJson))
                .andExpect(status().isOk());

        // body rỗng
        mockMvc.perform(post("/api/order/create")
                .header("Authorization", getUserToken())
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void TC03_CreateOrder_EmptyCart() throws Exception {

        String json = """
        {
            "fullName": "Nguyen Van A",
            "phoneNumber": "0123456789",
            "address": "Hanoi"
        }
        """;

        mockMvc.perform(post("/api/order/create")
                .header("Authorization", getUserToken())
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
                .andExpect(status().isBadRequest());
    }

    @Test
    void TC04_CreateOrder_Success() throws Exception {

        String cartJson = """
        {
            "productSlug": "sony-inzone-h9-whg900n",
            "quantity": 1
        }
        """;

        // Add product vào cart
        mockMvc.perform(post("/api/cart/add")
                .header("Authorization", getUserToken())
                .contentType(MediaType.APPLICATION_JSON)
                .content(cartJson))
                .andExpect(status().isOk());

        String orderJson = """
        {
            "fullName": "Nguyen Van A",
            "phoneNumber": "0123456789",
            "address": "Hanoi"
        }
        """;

        // Create order
        mockMvc.perform(post("/api/order/create")
                .header("Authorization", getUserToken())
                .contentType(MediaType.APPLICATION_JSON)
                .content(orderJson))
                .andExpect(status().isOk());
    }

    // =========================
    // ADMIN GET ALL ORDERS
    // =========================

    @Test
    void TC05_GetAllOrders() throws Exception {

        mockMvc.perform(get("/api/order/all-orders")
                .header("Authorization", getAdminToken()))
                .andExpect(status().isOk());
    }

    @Test
    void TC06_FilterOrdersByStatus() throws Exception {

        mockMvc.perform(get("/api/order/all-orders")
                .header("Authorization", getAdminToken())
                .param("status", "PENDING"))
                .andExpect(status().isOk());
    }

    // =========================
    // COUNT ORDERS
    // =========================

    @Test
    void TC07_CountOrders() throws Exception {

        mockMvc.perform(get("/api/order/count"))
                .andExpect(status().isOk());
    }

    // =========================
    // UPDATE ORDER STATUS
    // =========================

    @Test
    void TC08_UpdateStatus_Valid() throws Exception {

        mockMvc.perform(put("/api/order/update-status")
                .header("Authorization", getAdminToken())
                .param("id", "1")
                .param("status", "SHIPPED"))
                .andExpect(status().isOk());
    }

    @Test
    void TC09_UpdateStatus_OrderNotFound() throws Exception {

        mockMvc.perform(put("/api/order/update-status")
                .header("Authorization", getAdminToken())
                .param("id", "999999")
                .param("status", "SHIPPED"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void TC10_UpdateStatus_InvalidStatus() throws Exception {

        mockMvc.perform(put("/api/order/update-status")
                .header("Authorization", getAdminToken())
                .param("id", "1")
                .param("status", "INVALID_STATUS"))
                .andExpect(status().isBadRequest());
    }

    // =========================
    // INSTALLMENT PAYMENT
    // =========================

    @Test
    void TC11_CreateInstallmentPayment() throws Exception {

        String json = """
        {
            "installmentMonths": 6
        }
        """;

        mockMvc.perform(post("/api/order/20080/installment/payment")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
                .andExpect(status().isOk());
    }

    // =========================
    // GET INSTALLMENT INFO
    // =========================

    @Test
    void TC12_GetInstallmentInfo() throws Exception {

        mockMvc.perform(get("/api/order/20078/installment"))
                .andExpect(status().isOk());
    }

    @Test
    void TC13_GetInstallmentInfo_NotFound() throws Exception {

        mockMvc.perform(get("/api/order/999999/installment"))
                .andExpect(status().isNotFound());
    }
}