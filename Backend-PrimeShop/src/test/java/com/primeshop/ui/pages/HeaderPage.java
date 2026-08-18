package com.primeshop.ui.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;

public class HeaderPage {
    private WebDriver browser;
    private WebDriverWait wait;

    // --- Locators ---
    private By searchInput = By.cssSelector("input[placeholder*='Bạn muốn tìm gì']");
    
    // NÂNG CẤP: Dùng XPath chỉ đích danh thẻ <a> chứa chữ 'Giỏ hàng' để né Mobile Menu ẩn
    private By cartIconLink = By.xpath("//a[@href='/cart' and .//span[text()='Giỏ hàng']]");
    
    private By cartBadge = By.className("cart-badge");
    private By logoutBtn = By.xpath("//button[contains(@class, 'action-btn') and .//span[text()='Thoát']]");
    private By swalConfirmBtn = By.xpath("//button[contains(@class, 'swal2-confirm') and text()='Đăng xuất']");
    private By minigameNav = By.xpath("//a[@href='/minigame-list' and contains(., 'Mini Game')]");

    public HeaderPage(WebDriver browser) {
        this.browser = browser;
        this.wait = new WebDriverWait(browser, Duration.ofSeconds(10));
    }

    // --- Actions ---

    public void goToCart() {
        // Bước 1: Đợi phần tử xuất hiện trong DOM
        WebElement link = wait.until(ExpectedConditions.presenceOfElementLocated(cartIconLink));
        
        // Bước 2: Ép click bằng JavaScript - Bỏ qua mọi lớp đệm và tọa độ sai lệch
        ((JavascriptExecutor) browser).executeScript("arguments[0].click();", link);
    }

    public void clickLogout() {
        WebElement btnLogout = wait.until(ExpectedConditions.presenceOfElementLocated(logoutBtn));
        // Cuộn tới vị trí nút trước khi bấm
        ((JavascriptExecutor) browser).executeScript("arguments[0].scrollIntoView({block: 'center'});", btnLogout);
        
        try { Thread.sleep(500); } catch (InterruptedException e) {}
        
        ((JavascriptExecutor) browser).executeScript("arguments[0].click();", btnLogout);

        // Xử lý SweetAlert
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.className("swal2-container")));
        try { Thread.sleep(500); } catch (InterruptedException e) {}

        WebElement confirmBtn = wait.until(ExpectedConditions.presenceOfElementLocated(swalConfirmBtn));
        ((JavascriptExecutor) browser).executeScript("arguments[0].click();", confirmBtn);

        wait.until(ExpectedConditions.invisibilityOfElementLocated(By.className("swal2-container")));
    }

    public void searchForProduct(String keyword) {
        WebElement input = wait.until(ExpectedConditions.elementToBeClickable(searchInput));
        input.clear();
        input.sendKeys(keyword);
        input.sendKeys(Keys.ENTER);
    }

    public String getCartBadgeNumber() {
        return wait.until(ExpectedConditions.visibilityOfElementLocated(cartBadge)).getText();
    }

    public void goToMinigameList() {
        WebElement link = wait.until(ExpectedConditions.presenceOfElementLocated(minigameNav));
        ((JavascriptExecutor) browser).executeScript("arguments[0].scrollIntoView({block: 'center'});", link);
        ((JavascriptExecutor) browser).executeScript("arguments[0].click();", link);
    }
}