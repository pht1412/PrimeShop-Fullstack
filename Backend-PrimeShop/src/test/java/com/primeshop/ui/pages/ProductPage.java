package com.primeshop.ui.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;

public class ProductPage {
    private WebDriver browser;
    private WebDriverWait wait;

    // Locator bắt chữ "Đang tải dữ liệu..." để chờ
    private By loadingText = By.xpath("//p[text()='Đang tải dữ liệu...']");

    public ProductPage(WebDriver browser) {
        this.browser = browser;
        this.wait = new WebDriverWait(browser, Duration.ofSeconds(10));
    }

    // Hành động tìm và click vào sản phẩm cụ thể (bắt theo Text, không phân biệt hoa thường)
    public void clickOnProduct(String productName) {
        // Dặn Bot: Chờ cái dòng chữ Loading biến mất hẳn rồi mới tìm sản phẩm
        wait.until(ExpectedConditions.invisibilityOfElementLocated(loadingText));

        // Dùng XPath translate để tìm chữ "iphone 15 màu hồng" (bỏ qua viết hoa/thường)
        String xpathStr = String.format("//*[contains(translate(text(), 'ABCDEFGHIJKLMNOPQRSTUVWXYZ', 'abcdefghijklmnopqrstuvwxyz'), '%s')]", productName.toLowerCase());
        
        WebElement productEl = wait.until(ExpectedConditions.elementToBeClickable(By.xpath(xpathStr)));
        productEl.click();
    }
}