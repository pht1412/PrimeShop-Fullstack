package com.primeshop.ui.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;

public class WalletTabComponent {
    private WebDriver browser;
    private WebDriverWait wait;

    // --- Locators (Điều chỉnh class theo đúng WalletTab.tsx của bạn nếu cần) ---
    private By activateBtn = By.xpath("//button[contains(text(), 'Kích hoạt ngay')]");
    private By openDepositBtn = By.xpath("//button[contains(text(), 'Nạp tiền')]");
    private By depositBtn = By.xpath("//button[contains(text(), 'Nạp tiền') or contains(@class, 'btn-deposit')]");

    // Khung hiển thị số dư (Dùng để xác nhận ví đã kích hoạt thành công)
    private By balanceDisplay = By.xpath("//div[contains(text(), 'Số dư') or contains(@class, 'balance')]");

    // Xử lý Popup thông báo (SweetAlert2)
    private By swalPopup = By.className("swal2-popup");
    private By swalConfirmBtn = By.cssSelector(".swal2-confirm");
    private By swalInput = By.className("swal2-input");

    public WalletTabComponent(WebDriver browser) {
        this.browser = browser;
        this.wait = new WebDriverWait(browser, Duration.ofSeconds(10));
    }

    // Hành động: Kích hoạt ví
    public void activateWallet() {
        // Tìm nút Kích hoạt ngay bằng XPath mới
        WebElement btn = wait.until(ExpectedConditions.presenceOfElementLocated(activateBtn));

        // Cuộn tới nút và nghỉ một nhịp ngắn
        ((JavascriptExecutor) browser).executeScript("arguments[0].scrollIntoView({block: 'center'});", btn);
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
        }

        // ÉP CLICK: Dùng JavaScript để kích hoạt hàm của React
        ((JavascriptExecutor) browser).executeScript("arguments[0].click();", btn);

        // Xử lý popup xác nhận sau khi bấm kích hoạt
        handleSweetAlert();
    }

    // Hành động: Nạp tiền
    public void depositMoney(String amount) {
        // BƯỚC 1: Nhấn nút "Nạp tiền" ở ngoài màn hình chính để mở Popup
        WebElement btnOpen = wait.until(ExpectedConditions.elementToBeClickable(openDepositBtn));
        ((JavascriptExecutor) browser).executeScript("arguments[0].click();", btnOpen);

        // BƯỚC 2: Chờ Popup nhập tiền hiện ra và điền số tiền
        WebElement input = wait.until(ExpectedConditions.visibilityOfElementLocated(swalInput));
        input.clear();
        input.sendKeys(amount);

        // Dùng JS ép click nút "Tạo mã QR"
        WebElement btnCreateQr = browser.findElement(swalConfirmBtn);
        ((JavascriptExecutor) browser).executeScript("arguments[0].click();", btnCreateQr);

        // BƯỚC 3: Chờ Popup "Quét mã QR" xuất hiện
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//img[@alt='Mã QR nạp tiền']")));
        
        // BÍ QUYẾT: Dừng 1 giây chờ cái popup ảnh QR phóng to xong và đứng im
        try { Thread.sleep(1000); } catch (Exception e) {}

        // Dùng JS ép click nút "Tôi đã chuyển tiền"
        WebElement btnDone = wait.until(ExpectedConditions.presenceOfElementLocated(swalConfirmBtn));
        ((JavascriptExecutor) browser).executeScript("arguments[0].click();", btnDone);

        // BƯỚC 4: Chờ Popup "Đang xử lý giao dịch" xuất hiện
        // Thẻ tiêu đề của SweetAlert luôn là thẻ <h2> với class swal2-title
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//h2[contains(@class, 'swal2-title') and contains(text(), 'Đang xử lý')]")));
        
        // BÍ QUYẾT: Dừng 1 giây chờ popup thông báo này ổn định
        try { Thread.sleep(1000); } catch (Exception e) {}
        
        // Dùng JS ép click nút "Đã hiểu"
        WebElement btnGotIt = wait.until(ExpectedConditions.presenceOfElementLocated(swalConfirmBtn));
        ((JavascriptExecutor) browser).executeScript("arguments[0].click();", btnGotIt);
        
        // Đợi popup cuối cùng biến mất hoàn toàn
        wait.until(ExpectedConditions.invisibilityOfElementLocated(By.className("swal2-container")));
    }

    // Hành động: Xử lý các bảng thông báo SweetAlert2
    private void handleSweetAlert() {
        wait.until(ExpectedConditions.visibilityOfElementLocated(swalPopup));
        try {
            Thread.sleep(500);
        } catch (Exception e) {
        } // Chờ animation kết thúc
        browser.findElement(swalConfirmBtn).click();
        wait.until(ExpectedConditions.invisibilityOfElementLocated(swalPopup));
    }

    // Kiểm tra: Ví đã kích hoạt chưa? (Dựa vào việc có nhìn thấy số dư không)
    public boolean isWalletActivated() {
        try {
            return wait.until(ExpectedConditions.visibilityOfElementLocated(balanceDisplay)).isDisplayed();
        } catch (Exception e) {
            return false;
        }
    }
}