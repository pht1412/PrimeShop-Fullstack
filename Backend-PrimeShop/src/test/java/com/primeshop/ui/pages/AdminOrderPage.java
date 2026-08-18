package com.primeshop.ui.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;

public class AdminOrderPage {
    private WebDriver browser;
    private WebDriverWait wait;

    private By tableRows = By.cssSelector("tbody tr");
    private By noDataMessage = By.xpath("//td[contains(., 'Không có dữ liệu')]");

    public AdminOrderPage(WebDriver browser) {
        this.browser = browser;
        this.wait = new WebDriverWait(browser, Duration.ofSeconds(10));
    }

    public void clickFilterStatus(String statusValue) {
        // Mở dropdown Select của MUI
        By dropdownBox = By.xpath("//div[contains(@class, 'MuiSelect-select')]");
        WebElement selectDiv = wait.until(ExpectedConditions.elementToBeClickable(dropdownBox));
        // QUAN TRỌNG: Dùng click() nguyên bản của Selenium để nhại lại cú click chuột thật. 
        // MUI sử dụng React MouseEvents, dùng JSExecutor click ở đây sẽ KHÔNG trổ xuống được.
        selectDiv.click();
        
        // Chọn option tương ứng (dùng attribute data-value để tránh lỗi encoding font tiếng Việt)
        By optionLocator = By.xpath("//li[@data-value='" + statusValue + "']");
        WebElement option = wait.until(ExpectedConditions.elementToBeClickable(optionLocator));
        ((JavascriptExecutor) browser).executeScript("arguments[0].click();", option);
        
        // Nghỉ xíu để form gọi API fetchOrders (giống useEffect)
        try { Thread.sleep(1000); } catch (InterruptedException e) {}
    }

    public void searchOrder(String orderIdText) {
        // Tìm ô input tìm kiếm mã đơn (dựa trên thẻ input type text của MUI)
        By searchInput = By.xpath("//label[text()='Mã đơn']/following-sibling::div/input");
        WebElement input = wait.until(ExpectedConditions.visibilityOfElementLocated(searchInput));
        input.clear();
        input.sendKeys(orderIdText);
        try { Thread.sleep(1000); } catch (Exception e) {}
    }

    public int getOrderCount() {
        return browser.findElements(tableRows).size();
    }

    // --- Cập nhật kiểm tra Popup Lỗi (TC_Order_8) ---
    public boolean isErrorPopupDisplayed() {
        try {
            // Popup báo lỗi tải danh sách đơn hàng
            By swalTitle = By.xpath("//h2[contains(@class, 'swal2-title') and text()='Lỗi']");
            return wait.until(ExpectedConditions.visibilityOfElementLocated(swalTitle)).isDisplayed();
        } catch (Exception e) {
            return false;
        }
    }

    public void clickOkOnErrorPopup() {
        handleSweetAlert(); // Dùng chung hàm click swal2-confirm đã viết sẵn
    }

    public boolean isNoDataDisplayed() {
        try {
            return wait.until(ExpectedConditions.visibilityOfElementLocated(noDataMessage)).isDisplayed();
        } catch (Exception e) {
            return false;
        }
    }

    // Lấy trạng thái của đơn hàng ĐẦU TIÊN (nếu có)
    public String getFirstOrderStatus() {
        By statusBadge = By.xpath("//tbody/tr[1]//td[position()=5]/span");
        return wait.until(ExpectedConditions.visibilityOfElementLocated(statusBadge)).getText();
    }

    // Tìm dòng đầu tiên có chữ PENDING và nhấn nút Duyệt
    public void clickApproveFirstPendingOrder() {
        By approveBtn = By.xpath("//tbody/tr[.//span[text()='PENDING']]//button[contains(., 'Duyệt')]");
        WebElement btn = wait.until(ExpectedConditions.elementToBeClickable(approveBtn));
        ((JavascriptExecutor) browser).executeScript("arguments[0].scrollIntoView({block: 'center'});", btn);
        ((JavascriptExecutor) browser).executeScript("arguments[0].click();", btn);
        
        // Xử lý thông báo SweetAlert của Admin (Đã duyệt đơn hàng)
        handleSweetAlert();
    }

    // Kiểm tra xem hệ thống có hiển thị nút Duyệt cho các đơn hàng có trạng thái KHÁC chờ xác nhận không (VD: DELIVERED, CANCELLED)
    public boolean isInvalidButtonDisplayedForDelivered() {
        try {
            // Tìm các hàng ĐÃ GIAO (DELIVERED) hoặc ĐÃ HUỶ (CANCELLED), coi có nhét nút Duyệt vô không
            By invalidApproveBtn = By.xpath("//tbody/tr[.//span[text()='DELIVERED' or text()='CANCELLED']]//button[contains(., 'Duyệt') or contains(., 'Giao hàng')]");
            // Set wait ngắn lại vì thường là nó sẽ không có
            WebDriverWait shortWait = new WebDriverWait(browser, Duration.ofSeconds(2));
            return shortWait.until(ExpectedConditions.presenceOfElementLocated(invalidApproveBtn)).isDisplayed();
        } catch (Exception e) {
            return false; // Nghĩa là không có nút Duyệt (Đây là kết quả MONG MUỐN PASSED)
        }
    }

    private void handleSweetAlert() {
        By confirmBtn = By.className("swal2-confirm");
        try {
            WebElement btn = wait.until(ExpectedConditions.elementToBeClickable(confirmBtn));
            btn.click();
            wait.until(ExpectedConditions.invisibilityOfElementLocated(By.className("swal2-container")));
        } catch (Exception e) {}
    }
}
