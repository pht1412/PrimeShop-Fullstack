package com.primeshop.ui.tests.wallet;

import com.primeshop.core.BaseWebTest;
import com.primeshop.ui.pages.LoginPage;
import com.primeshop.ui.pages.AccountPage;
import com.primeshop.ui.pages.WalletTabComponent;

import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.support.ui.ExpectedConditions;

public class WalletTest extends BaseWebTest {
    private AccountPage accountPage;
    private WalletTabComponent walletTab;

    // Hàm dùng chung để Login và đi tới Tab Ví
    private void loginAndGoToWallet(String username, String password) {
        browser.get(WEB_URL + "/login");
        LoginPage loginPage = new LoginPage(browser);
        loginPage.enterUsername(username);
        loginPage.enterPassword(password);
        loginPage.clickLogin();

        // Chờ Login thành công (chuyển sang /home)
        wait.until(ExpectedConditions.urlContains("/home"));

        // Đi tới trang Account -> Tab Ví
        browser.get(WEB_URL + "/account");
        accountPage = new AccountPage(browser);
        accountPage.goToWalletTab();
        walletTab = new WalletTabComponent(browser);
    }

    @Test
    public void TC_WL_01_KichHoatViPrimeThanhCong() {
        // LƯU Ý: Dùng một tài khoản CHƯA TỪNG kích hoạt ví
        loginAndGoToWallet("user01", "user01");

        // --- THỰC THI ---
        walletTab.activateWallet();

        // --- KIỂM TRA ---
        assertTrue(walletTab.isWalletActivated(), "LỖI: Kích hoạt xong nhưng không hiển thị bảng số dư!");
    }

    @Test
    public void TC_WL_02_NapTienVaoViPrimeThanhCong() {
        // LƯU Ý: Dùng một tài khoản ĐÃ kích hoạt ví
        loginAndGoToWallet("user01", "user01");

        // --- THỰC THI ---
        String depositAmount = "500000"; // Nạp 500k
        walletTab.depositMoney(depositAmount);

        // --- KIỂM TRA ---
        assertTrue(walletTab.isWalletActivated(), "LỖI: Nạp tiền xong giao diện bị lỗi, không thấy bảng số dư!");
    }

    @Test
    public void TC_WL_03_ThanhToanBangViThanhCong() {
        loginAndGoToWallet("user01", "user01");
        accountPage.switchTab("Đơn Mua"); 

        // ĐỔI TỪ MÃ ĐƠN HÀNG SANG TÊN SẢN PHẨM BẠN VỪA MUA
        String productName = "JBL Quantum Qtum50CBLK"; // <-- Đổi thành tên thực tế trong DB
        
        accountPage.clickPayNowForProduct(productName);
        accountPage.selectWalletPayment();
        accountPage.confirmAndFinishPayment();

        assertTrue(browser.getCurrentUrl().contains("/account"), "Lỗi: Không quay lại được trang quản lý đơn hàng sau khi thanh toán!");
    }
}