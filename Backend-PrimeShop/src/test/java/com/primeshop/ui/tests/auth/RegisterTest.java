package com.primeshop.ui.tests.auth; // Đặt trong folder auth như bạn đã đề xuất

import com.primeshop.core.BaseWebTest;
import com.primeshop.ui.pages.RegisterPage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class RegisterTest extends BaseWebTest {

    private RegisterPage registerPage;

    // Chạy trước mỗi Test Case: Dọn đường vào đúng trang Đăng ký
    @BeforeEach
    public void navigateToRegister() {
        // Vượt qua cảnh báo SSL tự tạo (nếu có)
        browser.get("https://localhost:8080");

        // Bước 1: Mở trình duyệt và truy cập trang đăng ký
        browser.get(WEB_URL + "/register");
        registerPage = new RegisterPage(browser);
    }

    @Test
    public void TC_01AuthTS01_DangKyThanhCong() {
        // --- CHUẨN BỊ DỮ LIỆU (Dựa theo Test Case của bạn) ---
        String username = "Nguyen Van A";
        String email = "test01_" + System.currentTimeMillis() + "@gmail.com";
        String password = "test01";

        // MẸO NHỎ KHI CHẤM ĐỒ ÁN:
        // Nếu chạy TC này lần thứ 2, DB sẽ báo lỗi "Email đã tồn tại".
        // Để đối phó khi demo cho giảng viên, bạn có thể nối thêm thời gian vào email
        // để nó luôn là "Email chưa tồn tại":
        // String email = "test01_" + System.currentTimeMillis() + "@gmail.com";

        // --- THỰC THI CÁC BƯỚC ---
        // Bước 2 & 3: Điền thông tin vào các ô
        registerPage.enterUsername(username);
        registerPage.enterEmail(email);
        registerPage.enterPassword(password);
        registerPage.enterConfirmPassword(password); // UI của bạn có ô nhập lại MK

        // Bước 4: Nhấn nút "Đăng ký"
        registerPage.clickRegister();

        // Bước 5: Quan sát thông báo và quá trình chuyển hướng

        // Kiểm tra 1: Thông báo "Đăng ký thành công!" của SweetAlert hiện lên
        boolean isSuccessPopupDisplayed = wait.until(
                ExpectedConditions.visibilityOfElementLocated(
                        By.xpath("//h2[@id='swal2-title' and text()='Đăng ký thành công!']")))
                .isDisplayed();
        assertTrue(isSuccessPopupDisplayed, "Lỗi: Không hiển thị popup Đăng ký thành công!");

        // Kiểm tra 2: Chờ popup biến mất
        wait.until(ExpectedConditions.invisibilityOfElementLocated(By.className("swal2-container")));

        // Kiểm tra 3: Chuyển hướng về trang "/login"
        boolean isRedirected = wait.until(ExpectedConditions.urlContains("/login"));
        assertTrue(isRedirected, "Lỗi: Không chuyển hướng về trang Đăng nhập sau khi đăng ký thành công!");
    }

@Test
    public void TC_02AuthTS01_DangKyThatBai_DeTrongTruong() {
        // --- THỰC THI ---
        
        // Bước 1 & 2: Mở trang đăng ký và để trống các trường.

        // Bước 3: Nhấn nút "Đăng ký"
        registerPage.clickRegister();

        // --- XỬ LÝ ĐỂ "CHẬM LẠI MỘT NHỊP" ---

        // 1. Tìm ô input đầu tiên bị bắt lỗi (thường là username vì có thuộc tính required)
        WebElement usernameInput = browser.findElement(By.id("username"));

        // 2. Lấy thông báo validation trực tiếp từ trình duyệt
        String validationMsg = usernameInput.getAttribute("validationMessage");
        System.out.println("Nội dung thông báo bắt được: " + validationMsg);

        // 3. Dừng chương trình trong 3 giây để bạn có thể tận mắt nhìn thấy tooltip hiện lên
        try {
            Thread.sleep(3000); 
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        // --- KIỂM TRA (ASSERTION) ---

        // Xác nhận rằng trình duyệt đã kích hoạt cơ chế chặn form của HTML5
        boolean isTooltipActive = !validationMsg.isEmpty();
        assertTrue(isTooltipActive, "Lỗi: Trình duyệt không hiển thị tooltip 'Please fill out this field'!");
        
        // Kiểm tra nội dung thông báo (tùy vào ngôn ngữ trình duyệt là Anh hay Việt)
        assertTrue(validationMsg.contains("Please fill out this field") || validationMsg.contains("Vui lòng"),
                "Nội dung cảnh báo của trình duyệt không khớp mong đợi!");
    }
}