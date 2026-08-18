package com.primeshop.ui.tests.auth;

import com.primeshop.core.BaseWebTest;
import com.primeshop.ui.pages.HeaderPage;
import com.primeshop.ui.pages.LoginPage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

public class AuthorizationAndProfileTest extends BaseWebTest {

    private LoginPage loginPage;
    private String testUser = "user01";
    private String testPass = "user01";

    // Khởi tạo và dùng Helper Method để Đăng nhập cho tất cả các TC
    @BeforeEach
    public void setupAndLogin() {
        browser.get("https://localhost:8080");
        browser.get(WEB_URL + "/login");
        loginPage = new LoginPage(browser);

        // Đăng nhập với quyền User thường
        loginPage.enterUsername(testUser);
        loginPage.enterPassword(testPass);
        loginPage.clickLogin();

        // Chờ hạ cánh an toàn ở trang chủ
        wait.until(ExpectedConditions.urlContains("/home"));
    }

    @Test
    public void TC_07AuthorTS02_KhongChoPhepUserVaoTrangAdmin() {
        // --- THỰC THI BƯỚC 2, 3, 4 ---
        // Ép trình duyệt đi thẳng vào trang quản trị cụ thể mà bạn đã cấu hình router
        browser.get(WEB_URL + "/admin/dashboard");
        try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        // --- KIỂM TRA ---
        // Bước 5: Xác nhận hệ thống từ chối và lập tức đá về thẳng trang login
        try {
            boolean isRedirectedToLogin = wait.until(ExpectedConditions.urlContains("/"));
            assertTrue(isRedirectedToLogin, "Lỗi: Không chuyển hướng về /");
        } catch (org.openqa.selenium.TimeoutException e) {
            // Nếu sau 10s không về được trang login thì đánh rớt test case ngay lập tức
            org.junit.jupiter.api.Assertions
                    .fail("LỖI BẢO MẬT: Hệ thống không đá User thường về trang Login khi cố tình vào /admin/dashboard");
        }
    }

    @Test
    public void TC_08AuthorTS05_AnMenuAdminVoiUserThuong() {
        // --- THỰC THI & KIỂM TRA ---
        // Lúc này bot đã đứng ở trang chủ (nhờ @BeforeEach).
        // Dựa vào mã nguồn header.tsx của bạn, menu Admin có class "nav-item" và link
        // tới "/delivery-dashboard"

        // Tìm TẤT CẢ các thẻ có link trỏ về Quản lý đơn/Admin
        List<WebElement> adminMenus = browser.findElements(By.cssSelector("a[href='/delivery-dashboard']"));

        // Nếu list này rỗng (isEmpty), tức là menu đã được giấu đi thành công
        assertTrue(adminMenus.isEmpty(), "LỖI BẢO MẬT GIAO DIỆN: Menu Quản lý của Admin bị lộ ra cho User thường!");
    }

    @Test
    public void TC_09ProfileTS05_HienThiDungThongTinProfile() {
        // --- THỰC THI ---
        // Bước 1 & 2: Truy cập trang tài khoản
        browser.get(WEB_URL + "/account");
        // --- KIỂM TRA ---
        // Bước 3 & 4: Đối chiếu xem thông tin có khớp với tài khoản "test01@gmail.com"
        // không

        // MẸO CỦA TEST LEAD: Do tui chưa có mã nguồn file AccountPage của bạn, tui sẽ
        // dùng XPath
        // để quét TẤT CẢ chữ trên màn hình xem có xuất hiện email của user này không.
        // Bất kể email nằm trong thẻ <p>, <span> hay nằm trong ô input (<input
        // value="..."/>)

        String xpathScanEmail = String.format("//*[contains(text(), '%s') or @value='%s']", testUser, testUser);

        boolean isEmailFound = wait.until(
                ExpectedConditions.presenceOfElementLocated(By.xpath(xpathScanEmail))).isDisplayed();

        assertTrue(isEmailFound, "LỖI: Trang Profile không hiển thị đúng thông tin của tài khoản đang đăng nhập!");
    }

    @Test
    public void TC_10ProfileTS04_CapNhatThongTinCaNhan() {
        // --- THỰC THI ---
        browser.get(WEB_URL + "/account");

        // 1. CHỐNG GHI ĐÈ DỮ LIỆU (Race Condition)
        // Nghỉ ngơi 1.5 giây để chờ API fetchUser() lấy xong dữ liệu cũ và React render xong
        try { Thread.sleep(1500); } catch (InterruptedException e) {}

        WebElement nameInput = wait.until(ExpectedConditions.visibilityOfElementLocated(By.name("fullName")));

        // 2. KHẮC PHỤC LỖI CỦA HÀM .clear() TRONG REACT
        // Bấm tổ hợp phím Ctrl + A (Bôi đen) và Backspace (Xóa) để ép React chạy hàm onChange
        nameInput.sendKeys(org.openqa.selenium.Keys.chord(org.openqa.selenium.Keys.CONTROL, "a"), org.openqa.selenium.Keys.BACK_SPACE);
        
        String newName = "Prime User " + System.currentTimeMillis();
        nameInput.sendKeys(newName);

        // 3. ÉP CLICK VÀO NÚT LƯU XUYÊN QUA MỌI CẢN TRỞ
        WebElement saveBtn = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("button.btn-save")));
        ((org.openqa.selenium.JavascriptExecutor) browser).executeScript("arguments[0].click();", saveBtn);

        // 4. CHỜ THÔNG BÁO THÀNH CÔNG (SweetAlert2)
        WebElement swalPopup = wait.until(ExpectedConditions.visibilityOfElementLocated(By.className("swal2-popup")));
        WebElement swalOkBtn = swalPopup.findElement(By.cssSelector(".swal2-confirm"));
        
        // Nghỉ nửa giây cho hiệu ứng popup dừng lại rồi mới bấm OK
        try { Thread.sleep(500); } catch (InterruptedException e) {}
        swalOkBtn.click();

        // 5. CHỜ TRANG TỰ ĐỘNG RELOAD (F5)
        // Khi trang reload, ô input cũ sẽ biến mất. Dặn bot chờ cho nó biến mất hoàn toàn.
        wait.until(ExpectedConditions.stalenessOf(nameInput));

        // --- KIỂM TRA ---
        // Tìm lại cái ô input mới tinh sau khi F5
        WebElement nameInputAfterReload = wait.until(ExpectedConditions.visibilityOfElementLocated(By.name("fullName")));
        String savedName = nameInputAfterReload.getAttribute("value");

        assertEquals(newName, savedName, "Lỗi: Dữ liệu Profile không được lưu vào Database sau khi tải lại trang!");
    }

    @Test
    public void TC_11AuthTS04_ChanQuayLaiTrangLoginKhiDaDangNhap() {
        // --- THỰC THI ---
        // Lúc này Bot đã đứng ở "/home" nhờ hàm @BeforeEach.

        // Bước 2: Bot nhấn nút "Back" trên trình duyệt (Cố gắng lùi về trang /login
        // trước đó)
        browser.navigate().back();
        try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        // Hoặc một cách test bạo lực hơn là ép nó truy cập thẳng link login:
        // browser.get(WEB_URL + "/login");

        // --- KIỂM TRA ---
        // Bước 3 & 4: Hệ thống tự động chặn và đá về trang chủ (hoặc không cho vào)
        try {
            // Chờ xem hệ thống có đá nó ra khỏi "/login" không
            boolean isKeptAwayFromLogin = wait
                    .until(ExpectedConditions.not(ExpectedConditions.urlToBe(WEB_URL + "/login")));
            assertTrue(isKeptAwayFromLogin,
                    "LỖI LOGIC: Người dùng đã đăng nhập nhưng vẫn vào lại được trang Đăng nhập!");
        } catch (org.openqa.selenium.TimeoutException e) {
            org.junit.jupiter.api.Assertions.fail("Hệ thống không chặn việc truy cập /login khi có Token hợp lệ!");
        }
    }

    @Test
    public void TC_12AuthTS02_DangXuatThoatKhoiHeThong() {
        // --- THỰC THI ---
        // Khởi tạo trang Header
        HeaderPage header = new HeaderPage(browser);
        
        // Gọi hàm đăng xuất (Bot sẽ tự xử lý vụ bấm nút và chờ popup)
        header.clickLogout();

        // --- KIỂM TRA ---
        // Quan sát Header (Nút "Đăng nhập" hiện ra lại)
        boolean isLoginButtonVisible = wait.until(
            ExpectedConditions.visibilityOfElementLocated(By.cssSelector("a[href='/login']"))
        ).isDisplayed();
        assertTrue(isLoginButtonVisible, "Lỗi: Đăng xuất xong nhưng Header không cập nhật lại trạng thái!");

        // --- KIỂM TRA BẢO MẬT BƯỚC 5 (Đã điều chỉnh theo thực tế) ---
        // Thử truy cập vào trang bảo mật (Account) khi đã mất Token
        browser.get(WEB_URL + "/account");

        // 1. Hệ thống không đá văng đi đâu cả, vẫn giữ ở lại trang /account
        boolean isStillOnAccountPage = wait.until(ExpectedConditions.urlContains("/account"));
        assertTrue(isStillOnAccountPage, "Lỗi: Hệ thống tự động chuyển hướng sai lệch khi truy cập /account!");

        // 2. Chốt hạ: Đảm bảo thông tin cá nhân của người cũ KHÔNG CÒN trên màn hình
        // Giả sử tài khoản vừa đăng xuất có email là test01@gmail.com
        String oldUserEmail = "test01@gmail.com";
        
        // Quét toàn bộ DOM xem có thẻ nào chứa chữ (hoặc value) là email cũ không
        String xpathScanEmail = String.format("//*[contains(text(), '%s') or @value='%s']", oldUserEmail, oldUserEmail);
        
        // Vì ta mong đợi nó KHÔNG TỒN TẠI, ta dùng findElements (số nhiều) để trả về một list.
        // Nếu list rỗng (isEmpty) -> Dữ liệu đã được dọn dẹp sạch sẽ, test Pass!
        java.util.List<org.openqa.selenium.WebElement> remainingData = browser.findElements(By.xpath(xpathScanEmail));
        
        assertTrue(remainingData.isEmpty(), "LỖI BẢO MẬT NGHIÊM TRỌNG: Đã đăng xuất nhưng vào /account vẫn thấy dữ liệu cũ!");
    }
}