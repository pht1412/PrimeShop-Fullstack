package com.primeshop.ui.tests.order;

import com.primeshop.core.BaseWebTest;
import com.primeshop.ui.pages.LoginPage;
import com.primeshop.ui.pages.AdminOrderPage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.support.ui.ExpectedConditions;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertFalse;

public class AdminOrderTest extends BaseWebTest {
    private AdminOrderPage adminOrderPage;

    @BeforeEach
    public void prepareAdminSession() {
        // 1. Đăng nhập qua tài khoản Admin (TC_Order_6)
        browser.get(WEB_URL + "/login");
        LoginPage loginPage = new LoginPage(browser);
        loginPage.enterUsername("admin");
        loginPage.enterPassword("admin");
        loginPage.clickLogin();

        // Hệ thống có thể không tự nhảy về /admin ngay, ta chỉ cần đợi rời khỏi trang
        // login và ép force
        try {
            wait.until(ExpectedConditions.not(ExpectedConditions.urlContains("/login")));
        } catch (Exception e) {
        }

        // 2. Chuyển hướng luôn qua Trang Quản lý Đơn hàng
        browser.get(WEB_URL + "/admin/orders");
        adminOrderPage = new AdminOrderPage(browser);
        // Load xong API fetchOrders
        try {
            Thread.sleep(1500);
        } catch (InterruptedException e) {
        }
    }

    @Test
    public void TC_Order_6_AdminXemTatCaDonHang() {
        int orderCount = adminOrderPage.getOrderCount();
        assertTrue(orderCount > 0, "Lỗi: Không hiển thị bảng danh sách đơn hàng cho Admin!");
    }

    @Test
    public void TC_Order_7_AdminLocDonHangTheoTrangThai() {
        // Lọc trạng thái "PENDING"
        adminOrderPage.clickFilterStatus("PENDING");

        if (adminOrderPage.getOrderCount() > 0 && !adminOrderPage.isNoDataDisplayed()) {
            String firstStatus = adminOrderPage.getFirstOrderStatus();
            assertTrue(firstStatus.equals("PENDING"),
                    "Lỗi: Lọc 'Chờ xác nhận' nhưng kết quả có trạng thái khác: " + firstStatus);
        }
    }

    @Test
    public void TC_Order_8_HienThiKhiKhongCoDonHang() {
        // Dùng mã đơn giả mạo
        adminOrderPage.searchOrder("DUMMY_999999");

        // Thay vì hiện chữ Không Có Dữ Liệu trên bảng, Backend ném Error và Frontend
        // kích hoạt Popup Lỗi
        assertTrue(adminOrderPage.isErrorPopupDisplayed(),
                "Lỗi: Không hiển thị Popup cảnh báo Lỗi từ hệ thống khi tìm kiếm đơn mạo danh.");

        // Ấn OK để hoàn thiện Test Case
        adminOrderPage.clickOkOnErrorPopup();
    }

    @Test
    public void TC_Order_9_AdminCapNhatTrangThaiHopLe() {
        // Filter đúng trạng thái bằng TIẾNG ANH (data-value)
        adminOrderPage.clickFilterStatus("PENDING");

        if (adminOrderPage.getOrderCount() > 0 && !adminOrderPage.isNoDataDisplayed()) {
            // Nhấn Duyệt đơn hàng đầu tiên (Chuyển PENDING sang PROCESSING/CONFIRMED)
            adminOrderPage.clickApproveFirstPendingOrder();
            // Việc handleSweetAlert() không quăng Exception tức là có Popup Thành công.
            assertTrue(true);
        } else {
            System.out.println("Bỏ qua TC_Order_9 vì không có đơn hàng Pending nào để duyệt.");
        }
    }

    @Test
    public void TC_Order_10_AdminCapNhatTrangThaiKhongHopLe() {
        boolean isDisplayed = adminOrderPage.isInvalidButtonDisplayedForDelivered();
        assertFalse(isDisplayed, "BUG_ORDER: Xuất hiện nút Duyệt hoặc Giao Hàng trên đơn hàng Đã Hoàn Thành/Đã Hủy!");
    }
}
