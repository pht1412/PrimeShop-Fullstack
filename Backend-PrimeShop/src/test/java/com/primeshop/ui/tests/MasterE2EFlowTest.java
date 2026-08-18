package com.primeshop.ui.tests;

import com.primeshop.core.BaseWebTest;
import com.primeshop.ui.pages.*;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.support.ui.ExpectedConditions;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class MasterE2EFlowTest extends BaseWebTest {

    // Helper Method: Cho Bot tự xử lý vụ Đăng ký -> Đăng nhập
    private void prepareLoggedInUser(String username, String pass) {
        RegisterPage registerPage = new RegisterPage(browser);
        LoginPage loginPage = new LoginPage(browser);

        browser.get(WEB_URL + "/register");
        registerPage.enterUsername(username);
        registerPage.enterEmail(username + "@test.com");
        registerPage.enterPassword(pass);
        registerPage.enterConfirmPassword(pass);
        registerPage.clickRegister();

        wait.until(ExpectedConditions.urlContains("/login"));
        wait.until(ExpectedConditions.invisibilityOfElementLocated(By.className("swal2-container")));
        wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("button[type='submit'].auth-btn")));

        loginPage.enterUsername(username);
        loginPage.enterPassword(pass);
        loginPage.clickLogin();

        wait.until(ExpectedConditions.urlContains("/home"));
        wait.until(ExpectedConditions.invisibilityOfElementLocated(By.className("swal2-container")));
    }

    @Test
    public void testFullShoppingFlow_SearchAndAddToCart() {
        // --- 1. CHUẨN BỊ DỮ LIỆU ---
        String testUser = "buyer_" + System.currentTimeMillis();
        String testPass = "Pass@123";
        String targetProduct = "iPhone 16 (Hồng)";

        // Khởi tạo các trang
        HeaderPage header = new HeaderPage(browser);
        ProductPage productPage = new ProductPage(browser);
        ProductDetailPage detailPage = new ProductDetailPage(browser);
        CartPage cartPage = new CartPage(browser);

        // --- 2. THỰC THI LUỒNG ---
        // Lái bot dọn đường SSL và đăng nhập
        browser.get("https://localhost:8080");
        prepareLoggedInUser(testUser, testPass);

        // Bước 1: Gõ tìm kiếm "iphone" trên Header
        header.searchForProduct("iphone");

        // Bước 2: Ở trang danh sách, tìm đúng con IP 16 Hồng và click
        productPage.clickOnProduct(targetProduct);

        // Bước 3: Ở trang chi tiết, lấy số lượng giỏ hàng cũ, bấm Thêm và chờ Toastify
        String initialCartCount = header.getCartBadgeNumber(); // Lúc này thường là "0"
        detailPage.clickAddToCart();

        // // --- 3. KIỂM TRA (ASSERTIONS) ---
        // // Kiểm tra 1: Số lượng trên Icon Giỏ hàng phải tăng lên so với ban đầu
        // String newCartCount = header.getCartBadgeNumber();
        // assertTrue(Integer.parseInt(newCartCount) >
        // Integer.parseInt(initialCartCount),
        // "Lỗi: Bấm thêm vào giỏ mà số lượng trên Header không tăng lên!");

        // Bước 4: Click vào icon Giỏ hàng để vào trang Giỏ hàng kiểm tra thực tế
        header.goToCart();
        wait.until(ExpectedConditions.urlContains("/cart"));

        // Kiểm tra 2: Món hàng "iphone 16 màu hồng" phải nằm sờ sờ trong giỏ
        boolean isProductFound = cartPage.isProductInCart(targetProduct);
        assertTrue(isProductFound, "Lỗi chết người: Không tìm thấy sản phẩm [" + targetProduct + "] trong giỏ hàng!");
    }
}