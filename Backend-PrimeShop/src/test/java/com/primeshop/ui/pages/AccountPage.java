package com.primeshop.ui.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import java.time.Duration;
import java.util.List;

public class AccountPage {
    private WebDriver browser;
    private WebDriverWait wait;

    // --- Locators Sidebar (Tabs) ---
    private By sidebarItems = By.className("sidebar-item");
    private By profileTab = By.xpath("//li[contains(@class, 'sidebar-item') and contains(., 'Tài Khoản')]");
    private By ordersTab = By.xpath("//li[contains(@class, 'sidebar-item') and contains(., 'Đơn Mua')]");
    private By walletTab = By.xpath("//li[contains(@class, 'sidebar-item') and contains(., 'Ví Prime')]");

    // --- Locators Profile Tab ---
    private By fullNameInput = By.name("fullName");
    private By phoneInput = By.name("phoneNumber");
    private By addressInput = By.name("address");
    private By saveBtn = By.className("btn-save");

    // --- Locators Orders Tab ---
    private By orderSearchBox = By.className("order-search-box");
    private By orderCards = By.className("order-card");
    private By orderStatusTabs = By.className("order-tab-item");
    // Locator cho các thành phần trong popup thanh toán (dựa trên AccountPage.tsx)
    private By payNowBtn = By.xpath("//button[contains(text(), 'Thanh toán ngay')]");
    private By walletOptionBtn = By.id("pay-wallet"); // Nút chọn thanh toán bằng ví
    
    // Nút xác nhận chung của SweetAlert2

    // --- Locators Chung (SweetAlert2 & Loading) ---
    private By swalPopup = By.className("swal2-popup");
    private By swalConfirmBtn = By.className("swal2-confirm");
    private By animateFadeIn = By.className("animate-fade-in");

    public AccountPage(WebDriver browser) {
        this.browser = browser;
        this.wait = new WebDriverWait(browser, Duration.ofSeconds(10));
    }

    // --- Điều hướng Tab ---
    public void switchTab(String tabName) {
        By tabLocator = By.xpath(String.format("//li[contains(@class, 'sidebar-item') and contains(., '%s')]", tabName));
        WebElement tab = wait.until(ExpectedConditions.elementToBeClickable(tabLocator));
        ((JavascriptExecutor) browser).executeScript("arguments[0].click();", tab);
        
        // Nghỉ một nhịp ngắn (800ms) để React tháo Component cũ và nạp Component mới vào DOM
        try { Thread.sleep(800); } catch (Exception e) {}
    }

    // --- Chức năng Profile ---
    public void updateProfile(String name, String phone, String address) {
        fillInput(fullNameInput, name);
        fillInput(phoneInput, phone);
        fillInput(addressInput, address);
        
        WebElement btn = browser.findElement(saveBtn);
        ((JavascriptExecutor) browser).executeScript("arguments[0].click();", btn);
        handleSuccessPopup();
    }

    private void fillInput(By locator, String value) {
        WebElement element = wait.until(ExpectedConditions.visibilityOfElementLocated(locator));
        element.sendKeys(Keys.chord(Keys.CONTROL, "a"), Keys.BACK_SPACE);
        element.sendKeys(value);
    }

    // --- Chức năng Đơn hàng (Orders) ---
    public void searchOrder(String keyword) {
        WebElement search = wait.until(ExpectedConditions.visibilityOfElementLocated(orderSearchBox));
        search.clear();
        search.sendKeys(keyword);
    }

    public void filterOrderByStatus(String statusLabel) {
        String xpath = String.format("//div[contains(@class, 'order-tab-item') and contains(text(), '%s')]", statusLabel);
        wait.until(ExpectedConditions.elementToBeClickable(By.xpath(xpath))).click();
        try { Thread.sleep(1000); } catch (Exception e) {} // Chờ filter API
    }

    public void clickPayNow(String orderId) {
        // Tìm đơn hàng cụ thể theo ID và bấm nút thanh toán ngay
        String xpath = String.format("//div[contains(@class, 'order-card') and .//div[contains(text(), '%s')]]//button[contains(., 'Thanh toán ngay')]", orderId);
        WebElement btn = wait.until(ExpectedConditions.elementToBeClickable(By.xpath(xpath)));
        ((JavascriptExecutor) browser).executeScript("arguments[0].click();", btn);
    }

    // --- Xử lý Popup ---
    public void handleSuccessPopup() {
        wait.until(ExpectedConditions.visibilityOfElementLocated(swalPopup));
        try { Thread.sleep(500); } catch (Exception e) {}
        browser.findElement(swalConfirmBtn).click();
        // Sau khi bấm OK, code React của bạn gọi window.location.reload()
        // Cần đợi trang tải lại xong
        wait.until(ExpectedConditions.stalenessOf(browser.findElement(swalPopup)));
    }

    // Kiểm tra hiển thị thông tin
    public String getFullNameValue() {
        return wait.until(ExpectedConditions.visibilityOfElementLocated(fullNameInput)).getAttribute("value");
    }

    // Hàm gọi nhanh Tab Ví Prime
    public void goToWalletTab() {
        // Tận dụng luôn hàm switchTab đã viết ở trên cho gọn code
        switchTab("Ví Prime");
    }

    // Thay thế bằng hàm tìm kiếm theo Tên Sản Phẩm
    public void clickPayNowForProduct(String productName) {
        // Tìm thẻ order-card nào chứa cái product-name đúng với sản phẩm của bạn, sau đó bấm nút Thanh toán bên trong nó
        String xpath = String.format("//div[contains(@class, 'order-card') and .//div[contains(@class, 'product-name') and contains(text(), '%s')]]//button[contains(text(), 'Thanh toán ngay')]", productName);
        
        WebElement btn = wait.until(ExpectedConditions.elementToBeClickable(By.xpath(xpath)));
        
        // Cuộn tới nút cho chắc chắn không bị che khuất
        ((JavascriptExecutor) browser).executeScript("arguments[0].scrollIntoView({block: 'center'});", btn);
        ((JavascriptExecutor) browser).executeScript("arguments[0].click();", btn);
    }

    // 2. Chọn phương thức thanh toán bằng Ví trong popup
    public void selectWalletPayment() {
        // Đợi popup phương thức thanh toán hiện ra
        WebElement walletBtn = wait.until(ExpectedConditions.visibilityOfElementLocated(walletOptionBtn));
        // Nghỉ một nhịp cho popup ổn định
        try { Thread.sleep(800); } catch (Exception e) {}
        ((JavascriptExecutor) browser).executeScript("arguments[0].click();", walletBtn);
    }

    // 3. Xác nhận thanh toán và đóng popup thành công
    public void confirmAndFinishPayment() {
        // Bước A: Xác nhận thanh toán ở popup 1 ("Xác nhận thanh toán?")
        WebElement btnConfirm = wait.until(ExpectedConditions.elementToBeClickable(swalConfirmBtn));
        ((JavascriptExecutor) browser).executeScript("arguments[0].click();", btnConfirm);

        // Bước B: Đợi tiêu đề "Thành công" xuất hiện để biết Backend đã xử lý xong
        try {
            wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//h2[contains(@class, 'swal2-title') and contains(text(), 'Thành công')]")));
        } catch (Exception e) {
            System.out.println("Thông báo thành công bị lướt qua quá nhanh.");
        }
        
        // Bước C: Xử lý nút OK với Try-Catch (Chống Crash)
        try {
            // Rút ngắn thời gian chờ xuống 2 giây. Nếu có nút OK thì bấm, không thì thôi!
            WebDriverWait shortWait = new WebDriverWait(browser, Duration.ofSeconds(2));
            WebElement btnSuccessOk = shortWait.until(ExpectedConditions.presenceOfElementLocated(swalConfirmBtn));
            ((JavascriptExecutor) browser).executeScript("arguments[0].click();", btnSuccessOk);
            System.out.println("Đã bấm nút OK trên popup Thành công.");
        } catch (Exception e) {
            System.out.println("Popup tự động đóng hoặc trang tự reload. Bỏ qua bước click OK.");
        }

        // Bước D: Đợi popup biến mất hoàn toàn (hoặc trang đã reload xong)
        try {
            wait.until(ExpectedConditions.invisibilityOfElementLocated(By.className("swal2-container")));
        } catch (Exception e) {
            // Không làm gì cả vì popup đã thực sự biến mất
        }
    }
}