package com.primeshop.ui.tests.auth;

import com.primeshop.core.BaseWebTest;
import com.primeshop.ui.pages.LoginPage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.support.ui.ExpectedConditions;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class LoginTest extends BaseWebTest {

    private LoginPage loginPage;

    @BeforeEach
    public void navigateToLogin() {
        browser.get("https://localhost:8080");

        // Bước 1: Truy cập trang đăng nhập
        browser.get(WEB_URL + "/login");
        loginPage = new LoginPage(browser);
    }

    @Test
    public void TC_01AuthTS02_DangNhapThanhCong() {
        // --- CHUẨN BỊ DỮ LIỆU ---
        // Lưu ý: Backend của bạn phải cho phép dùng email thay cho username,
        // hoặc bạn phải nhập đúng username là "test01" thay vì email.
        String username = "user01";
        String password = "user01";

        // --- THỰC THI ---
        // Bước 2 & 3: Nhập thông tin
        loginPage.enterUsername(username);
        loginPage.enterPassword(password);

        // Bước 4: Click Đăng nhập
        loginPage.clickLogin();

        // Bước 5: Kiểm tra thông báo và chuyển hướng
        // Kiểm tra 1: Popup SweetAlert "Đăng nhập thành công!"
        boolean isSuccessPopupDisplayed = wait.until(
                ExpectedConditions.visibilityOfElementLocated(
                        By.xpath("//h2[@id='swal2-title' and text()='Đăng nhập thành công!']")))
                .isDisplayed();
        assertTrue(isSuccessPopupDisplayed, "Lỗi: Không hiển thị popup Đăng nhập thành công!");

        // Chờ popup biến mất (1500ms theo code React)
        wait.until(ExpectedConditions.invisibilityOfElementLocated(By.className("swal2-container")));

        // Kiểm tra 2: Hệ thống chuyển hướng về trang chủ (/home)
        boolean isRedirected = wait.until(ExpectedConditions.urlContains("/home"));
        assertTrue(isRedirected, "Lỗi: Không chuyển hướng về trang Chủ sau khi đăng nhập thành công!");
    }

    @Test
    public void TC_04AuthTS02_DangNhapThatBai_SaiMatKhau() {
        // --- CHUẨN BỊ DỮ LIỆU ---
        String username = "test01@gmail.com";
        String wrongPass = "wrong_pass";
        String expectedError = "Đăng nhập thất bại!";

        // --- THỰC THI ---
        loginPage.enterUsername(username);
        loginPage.enterPassword(wrongPass);
        loginPage.clickLogin();
        try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        // --- KIỂM TRA (ASSERTION) ---
        // Trong LoginPage.tsx, lỗi được hiển thị qua {error && <div
        // className="error-message">{error}</div>}
        String actualError = wait.until(
                ExpectedConditions.visibilityOfElementLocated(By.className("error-message"))).getText();

        assertEquals(expectedError, actualError, "Lỗi: Thông báo sai mật khẩu không hiển thị đúng!");
    }

    @Test
    public void TC_05AuthTS02_DangNhapThatBai_TaiKhoanKhongTonTai() {
        // --- CHUẨN BỊ DỮ LIỆU ---
        String randomUser = "random" + System.currentTimeMillis() + "@gmail.com";
        String anyPass = "random";
        String expectedError = "Đăng nhập thất bại!";

        // --- THỰC THI ---
        loginPage.enterUsername(randomUser);
        loginPage.enterPassword(anyPass);
        loginPage.clickLogin();

        try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        // --- KIỂM TRA ---
        // Đợi thông báo lỗi văng ra từ API và hiển thị lên UI
        String actualError = wait.until(
                ExpectedConditions.visibilityOfElementLocated(By.className("error-message"))).getText();

        assertEquals(expectedError, actualError, "Lỗi: Hệ thống không báo đúng lỗi khi tài khoản không tồn tại!");
    }

    @Test
    public void TC_06JWTTS02_KiemTraDuyTriDangNhap_F5() {
        // --- BƯỚC 1: ĐĂNG NHẬP TRƯỚC ---
        String username = "test01"; // Giả sử username của bạn là test01
        String password = "test01";

        loginPage.enterUsername(username);
        loginPage.enterPassword(password);
        loginPage.clickLogin();

        // Đợi vào đến trang chủ
        wait.until(ExpectedConditions.urlContains("/home"));

        // --- BƯỚC 2: REFRESH TRANG (F5) ---
        browser.navigate().refresh();
        try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        // --- BƯỚC 3: KIỂM TRA TRẠNG THÁI ---
        // Chờ Header render lại. Trong header.tsx, nếu có user thì sẽ hiện username
        // trong thẻ span
        // Locator: a[href='/account'] span
        String usernameOnHeader = wait.until(
                ExpectedConditions.visibilityOfElementLocated(By.cssSelector("a[href='/account'] span"))).getText();

        assertEquals(username, usernameOnHeader,
                "Lỗi: Sau khi F5, tài khoản bị đăng xuất hoặc không hiển thị tên người dùng!");

        // Kiểm tra xem nút "Thoát" có còn đó không
        boolean isLogoutVisible = browser.findElement(By.xpath("//span[text()='Thoát']")).isDisplayed();
        assertTrue(isLogoutVisible, "Lỗi: Nút Thoát biến mất sau khi F5!");
    }
}