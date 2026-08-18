package com.primeshop.ui.tests;

import com.primeshop.core.BaseWebTest;
import com.primeshop.ui.pages.HomePage;
import com.primeshop.ui.pages.LoginPage;
import com.primeshop.ui.pages.RegisterPage;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.support.ui.ExpectedConditions;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.openqa.selenium.By;

public class AuthE2ETest extends BaseWebTest {

    @Test
    public void testEndToEndRegisterAndLogin() {
        // --- 1. SETUP DỮ LIỆU TEST ---
        // Tạo username và email ngẫu nhiên dựa trên thời gian thực để tránh trùng lặp
        // DB
        String randomSuffix = String.valueOf(System.currentTimeMillis());
        String testUser = "auto_user_" + randomSuffix;
        String testEmail = testUser + "@gmail.com";
        String testPass = "Password@123";

        // Khởi tạo các Page Objects
        RegisterPage registerPage = new RegisterPage(browser);
        LoginPage loginPage = new LoginPage(browser);
        HomePage homePage = new HomePage(browser);

        // --- 2. THỰC THI LUỒNG ĐĂNG KÝ ---
        browser.get(WEB_URL + "/register");

        registerPage.enterUsername(testUser);
        registerPage.enterEmail(testEmail);
        registerPage.enterPassword(testPass);
        registerPage.enterConfirmPassword(testPass);
        registerPage.clickRegister();

        // [QUAN TRỌNG] Xử lý SweetAlert trong React
        // File RegisterPage.tsx cài đặt timer: 1500ms trước khi navigate("/login")
        // Ta dùng "Đồng hồ cát" (wait) đã định nghĩa trong BaseWebTest để chờ URL
        // chuyển sang /login
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        wait.until(ExpectedConditions.urlContains("/login"));

        // 🚀 BÍ QUYẾT: Dặn Bot đứng chờ cho cái popup SweetAlert2 biến mất hẳn
        // (invisibility)
        wait.until(ExpectedConditions.invisibilityOfElementLocated(By.className("swal2-container")));

        // Cũng có thể dặn Bot chờ cho đến khi cái nút Đăng nhập thực sự an toàn để
        // click
        wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("button[type='submit'].auth-btn")));

        // --- 3. THỰC THI LUỒNG ĐĂNG NHẬP ---
        // Lúc này Bot đã đứng ở trang Đăng nhập
        loginPage.enterUsername(testUser);
        loginPage.enterPassword(testPass);
        loginPage.clickLogin();

        // Chờ SweetAlert Đăng nhập thành công và navigate về trang home hoặc previous
        // path
        // Chờ đến khi URL không còn là /login nữa
        wait.until(ExpectedConditions.not(ExpectedConditions.urlContains("/login")));

        // Đảm bảo Bot đã thực sự vào được Home
        wait.until(ExpectedConditions.urlContains("/home"));

        // --- 4. ASSERTION (Xác nhận kết quả thực tế) ---
        // Chờ Header React render lại (state user được cập nhật)
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("a[href='/account'] span")));

        // Kiểm tra 1: Tên hiển thị trên Header có khớp với tên vừa đăng ký không?
        String actualUsernameOnHeader = homePage.getLoggedInUsername();
        assertEquals(testUser, actualUsernameOnHeader, "Tên hiển thị trên Header không khớp với user vừa đăng nhập!");

        // Kiểm tra 2: Nút Thoát có xuất hiện không?
        assertTrue(homePage.isLogoutButtonDisplayed(),
                "Không tìm thấy nút Thoát, đăng nhập có thể chưa hoàn toàn thành công!");

        // --- 5. BỔ SUNG: DUYỆT QUA TẤT CẢ CÁC MỤC TRÊN HEADER ---
        System.out.println("Bắt đầu duyệt qua các mục trên Header...");
        String[] navItems = { "Giới thiệu", "Sản Phẩm", "Tin Công Nghệ", "Hỏi Đáp", "Mini Game", "Trang Chủ" };
        for (String itemText : navItems) {
            System.out.println("Đang chuyển tới trang: " + itemText);
            org.openqa.selenium.WebElement navItem = wait.until(ExpectedConditions.elementToBeClickable(
                    By.xpath("//a[contains(@class, 'nav-item') and contains(text(), '" + itemText + "')]")));

            // Đảm bảo đưa vào tầm nhìn trước khi click và tạo hiệu ứng Delay cho người quan
            // sát
            ((org.openqa.selenium.JavascriptExecutor) browser)
                    .executeScript("arguments[0].scrollIntoView({block: 'center'});", navItem);
            try {
                Thread.sleep(500);
            } catch (Exception e) {
            }

            // Cú click
            ((org.openqa.selenium.JavascriptExecutor) browser).executeScript("arguments[0].click();", navItem);

            // Dừng 2 giây ở mỗi màn hình để quan sát thành quả (như yêu cầu)
            try {
                Thread.sleep(2000);
            } catch (Exception e) {
            }
        }
    }
}