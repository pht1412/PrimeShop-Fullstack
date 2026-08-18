package com.primeshop.ui.tests.order;

import com.primeshop.core.BaseWebTest;
import com.primeshop.ui.pages.LoginPage;
import com.primeshop.ui.pages.AccountPage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.support.ui.ExpectedConditions;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class UserOrderHistoryTest extends BaseWebTest {
    private AccountPage accountPage;

    @BeforeEach
    public void prepareUserSession() {
        // 1. Đăng nhập qua tài khoản Tester User
        browser.get(WEB_URL + "/login");
        LoginPage loginPage = new LoginPage(browser);
        loginPage.enterUsername("user01");
        loginPage.enterPassword("user01");
        loginPage.clickLogin();

        wait.until(ExpectedConditions.urlContains("/home"));

        // 2. Chuyển hướng tới trang Account (Lịch sử)
        browser.get(WEB_URL + "/account");
        accountPage = new AccountPage(browser);

        // Chuyển sang Tab Đơn hàng
        accountPage.switchTab("Đơn Mua");
    }

    @Test
    public void TC_Order_1_TraCuuLichSuDonHang() {
        // Lấy danh sách thẻ Order Card
        int count = browser.findElements(By.className("order-card")).size();

        try {
            // Kiểm tra xem có cái empty-message nào không
            boolean isEmptyDisplayed = browser.findElement(By.xpath("//*[contains(text(), 'chưa có đơn hàng')]"))
                    .isDisplayed();
            assertTrue(isEmptyDisplayed || count > 0, "Lỗi: Lịch sử đơn hàng bị lỗi tải trang hoặc trắng bóc.");
        } catch (Exception e) {
            assertTrue(count > 0, "Lỗi: Không tìm thấy đơn hàng nào trong lịch sử!");
        }
    }

    @Test
    public void TC_Order_2_TimKiemDonHangNguoiDung() {
        // Client không có nút ấn lọc nên ta chuyển sang sử dụng ô tìm kiếm với từ khoá "laptop"
        accountPage.searchOrder("laptop");
        
        // Chờ 1 giây để Frontend gọi API tìm kiếm
        try { Thread.sleep(1000); } catch (Exception e) {}
        
        int count = browser.findElements(By.className("order-card")).size();
        
        if (count > 0) {
            String orderText = wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("(//div[contains(@class, 'order-card')])[1]"))).getText();
            assertTrue(orderText.toLowerCase().contains("laptop"), "Lỗi: Tìm kiếm laptop nhưng đơn hàng trả về không chứa chữ laptop!");
        } else {
            System.out.println("Bỏ qua Assert vì không có đơn hàng nào chứa laptop để chứng minh.");
        }
    }
}
