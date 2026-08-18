package com.primeshop.ui.tests.order;

import com.primeshop.core.BaseWebTest;
import com.primeshop.ui.pages.LoginPage;
import com.primeshop.ui.pages.CheckoutPage;
import com.primeshop.ui.pages.CartPage;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertFalse;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.support.ui.ExpectedConditions;

public class OrderCheckoutTest extends BaseWebTest {
    private CheckoutPage checkoutPage;

    @BeforeEach
    public void prepareCheckout() {
        browser.get(WEB_URL + "/login");
        LoginPage loginPage = new LoginPage(browser);
        loginPage.enterUsername("user01");
        loginPage.enterPassword("user01");
        loginPage.clickLogin();
        wait.until(ExpectedConditions.urlContains("/home"));

        // Đi từ Giỏ hàng sang để có State
        browser.get(WEB_URL + "/cart");
        CartPage cartPage = new CartPage(browser);
        cartPage.clickCheckout();
        wait.until(ExpectedConditions.urlContains("/checkout"));

        checkoutPage = new CheckoutPage(browser);
    }

    @Test
    public void TC_Order_3_TaoDonHangHopLeThanhCong() {
        checkoutPage.fillFullName("Nguyễn Văn Kiệt");
        checkoutPage.fillPhone("0123456789");
        checkoutPage.fillAddress("123 Đường ABC");

        // CHÚ Ý: Chữ phải khớp tuyệt đối 100% với mảng cities
        checkoutPage.selectCity("TP. Hồ Chí Minh");
        checkoutPage.selectDistrict("Quận 1");

        checkoutPage.fillNote("Giao giờ hành chính");
        checkoutPage.clickPlaceOrder();

        boolean isSuccess = false;
        try {
            wait.until(ExpectedConditions.visibilityOfElementLocated(
                    By.xpath("//h2[contains(@class, 'swal2-title') and contains(text(), 'Đặt hàng thành công')]")));
            isSuccess = true;
        } catch (Exception e) {
        }

        assertTrue(isSuccess, "Lỗi: Không hiển thị popup Đặt hàng thành công!");
    }

    @Test
    public void TC_Order_4_TaoDonHangThieuThanhPho() {
        checkoutPage.fillFullName("Nguyễn Văn Kiệt");
        checkoutPage.fillPhone("0123456789");
        checkoutPage.fillAddress("123 Đường ABC");
            
        // Cố tình bỏ qua bước chọn Thành phố
        checkoutPage.clickPlaceOrder();

        // Tạm dừng 3 giây để ngắm cái popup "Please fill out this field" bật ra
        try { Thread.sleep(3000); } catch (InterruptedException e) {}

        // Lấy thông báo HTML5 thực tế
        String errorMsg = checkoutPage.getNativeValidationMessage();
        System.out.println("Cảnh báo từ Chrome: " + errorMsg);

        // Kiểm tra xem trình duyệt có thực sự văng ra câu chửi nào không
        // Tui dùng assert không rỗng vì lỡ Chrome của bạn cài tiếng Việt nó sẽ hiện "Vui lòng điền trường này"
        assertFalse(errorMsg.isEmpty(), "Lỗi: Trình duyệt không chặn form lại!");
    }

    @Test
    public void TC_Order_5_KiemTraDuLieuSauKhiReload() {
        checkoutPage.fillFullName("Nguyễn Văn Kiệt");
        checkoutPage.fillAddress("123 Đường ABC");

        browser.navigate().refresh();
        checkoutPage = new CheckoutPage(browser);
        try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        String nameAfterReload = checkoutPage.getFullNameValue();
        assertFalse(nameAfterReload.isEmpty(), "BUG_ORD_01: Dữ liệu form không được lưu trữ khi reload trang!");
    }
}