package com.primeshop.ui.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import java.time.Duration;

public class CartPage {
    private WebDriver browser;
    private WebDriverWait wait;

    // --- Locators dựa trên CartPage.tsx ---
    private By cartItems = By.className("cart-item");
    private By emptyCartMsg = By.className("cart-empty");
    private By subtotalLabel = By.xpath("//div[contains(text(), 'Tổng tiền hàng')]");
    private By finalTotalLabel = By.xpath("//div[contains(@class, 'font-semibold') and contains(text(), 'Tổng thanh toán')]");
    private By checkoutBtn = By.className("checkout-btn");
    
    // Popup SweetAlert2 cho nút xóa
    private By swalPopup = By.className("swal2-popup");
    private By swalConfirmDeleteBtn = By.cssSelector(".swal2-confirm");

    public CartPage(WebDriver browser) {
        this.browser = browser;
        this.wait = new WebDriverWait(browser, Duration.ofSeconds(10));
    }

    // 1. Kiểm tra sản phẩm có tồn tại trong giỏ không
    public boolean isProductInCart(String productName) {
        try {
            String xpath = String.format("//h3[@class='cart-item-name' and text()='%s']", productName);
            return wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath(xpath))).isDisplayed();
        } catch (Exception e) {
            return false;
        }
    }


    // 3. Giảm số lượng (-)
    public void decreaseQuantity(String productName) {
        String xpath = String.format("//h3[text()='%s']/parent::div//button[text()='-']", productName);
        WebElement btn = wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath(xpath)));
        
        if (btn.isEnabled()) {
            btn.click();
            waitForLoading();
        } else {
            System.out.println("Nút giảm đã bị disabled do số lượng = 1");
        }
    }

    // 4. Xóa sản phẩm (Có xử lý SweetAlert)
    public void removeItem(String productName) {
        // Tìm nút "Xóa khỏi giỏ hàng" (Material UI Button màu đỏ)
        String xpath = String.format("//h3[text()='%s']/parent::div//button[contains(text(), 'Xóa khỏi giỏ hàng')]", productName);
        WebElement removeBtn = wait.until(ExpectedConditions.elementToBeClickable(By.xpath(xpath)));
        
        // Ép click bằng JS cho chắc
        ((JavascriptExecutor) browser).executeScript("arguments[0].click();", removeBtn);

        // Chờ popup Swal hiện ra và bấm xác nhận Xóa
        wait.until(ExpectedConditions.visibilityOfElementLocated(swalPopup));
        browser.findElement(swalConfirmDeleteBtn).click();
        
        waitForLoading();
    }

    // 5. Lấy tổng thanh toán (Final Total)
    public String getFinalTotal() {
        return wait.until(ExpectedConditions.visibilityOfElementLocated(finalTotalLabel)).getText();
    }

    // 6. Hàm bổ trợ: Chờ cho "Đang tải giỏ hàng..." biến mất
    private void waitForLoading() {
        try {
            wait.until(ExpectedConditions.invisibilityOfElementLocated(By.xpath("//p[contains(text(), 'Đang tải giỏ hàng')]")));
            // Nghỉ thêm một nhịp ngắn để React render xong xuôi
            Thread.sleep(500);
        } catch (Exception e) {
            // Nếu không thấy chữ loading thì thôi, chạy tiếp
        }
    }
    public void increaseQuantity(String productName) {
        // XPath: Tìm h3 chứa tên SP -> Tìm ngược lên div cha 'cart-item' -> Tìm nút có chữ '+'
        String xpath = String.format("//h3[contains(text(), '%s')]/ancestor::div[contains(@class, 'cart-item')]//button[text()='+']", productName);
        
        WebElement plusBtn = wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath(xpath)));

        // 1. Cuộn tới nút để đảm bảo nó nằm trong tầm nhìn
        ((JavascriptExecutor) browser).executeScript("arguments[0].scrollIntoView({block: 'center'});", plusBtn);
        
        // 2. Nghỉ một chút cho hiệu ứng cuộn dừng lại
        try { Thread.sleep(500); } catch (Exception e) {}

        // 3. Ép click bằng JavaScript để kích hoạt hàm updateQuantity của React
        ((JavascriptExecutor) browser).executeScript("arguments[0].click();", plusBtn);

        // 4. CHỜ QUAN TRỌNG: Chờ chữ "Đang tải giỏ hàng..." xuất hiện rồi biến mất
        waitForLoading();
    }

    public void clickCheckout() {
        WebElement btn = wait.until(ExpectedConditions.presenceOfElementLocated(checkoutBtn));
        
        // Cuộn xuống cuối trang để thấy nút
        ((JavascriptExecutor) browser).executeScript("arguments[0].scrollIntoView({block: 'center'});", btn);
        try { Thread.sleep(500); } catch (Exception e) {}
        
        // Ép click để React Router truyền State đi chuẩn xác
        ((JavascriptExecutor) browser).executeScript("arguments[0].click();", btn);
    }

    public String getEmptyCartMessage() {
        return wait.until(ExpectedConditions.visibilityOfElementLocated(emptyCartMsg)).getText();
    }

    public boolean isDecreaseButtonDisabled(String productName) {
        String xpath = String.format("//h3[contains(text(), '%s')]/ancestor::div[contains(@class, 'cart-item')]//button[text()='-']", productName);
        WebElement btn = wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath(xpath)));
        return !btn.isEnabled();
    }

    public boolean isCartEmpty() {
        try {
            return browser.findElements(emptyCartMsg).size() > 0 && browser.findElement(emptyCartMsg).isDisplayed();
        } catch (Exception e) {
            return false;
        }
    }

    public int getCartItemCount() {
        return browser.findElements(cartItems).size();
    }

    public void removeAllItems() {
        String xpath = "//button[contains(text(), 'Xóa khỏi giỏ hàng')]";
        while(browser.findElements(By.xpath(xpath)).size() > 0) {
            WebElement btn = wait.until(ExpectedConditions.elementToBeClickable(By.xpath(xpath)));
            ((JavascriptExecutor) browser).executeScript("arguments[0].scrollIntoView({block: 'center'});", btn);
            try { Thread.sleep(500); } catch (Exception e) {}
            ((JavascriptExecutor) browser).executeScript("arguments[0].click();", btn);
            
            wait.until(ExpectedConditions.visibilityOfElementLocated(swalPopup));
            browser.findElement(swalConfirmDeleteBtn).click();
            waitForLoading();
        }
    }
}