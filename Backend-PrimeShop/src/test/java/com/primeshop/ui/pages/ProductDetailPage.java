package com.primeshop.ui.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.openqa.selenium.JavascriptExecutor; // Thêm import này

import java.time.Duration;

public class ProductDetailPage {
    private WebDriver browser;
    private WebDriverWait wait;

    // Bắt nút qua Text thay vì class name CSS Modules
    private By addToCartBtn = By.xpath("//button[contains(., 'Thêm vào giỏ')]");

    // Bắt popup thông báo của thư viện React Toastify (Khác với SweetAlert nhé)
    private By toastMessage = By.className("Toastify__toast");

    public ProductDetailPage(WebDriver browser) {
        this.browser = browser;
        this.wait = new WebDriverWait(browser, Duration.ofSeconds(10));
    }

    public void clickAddToCart() {
        // 1. Tìm nút bấm trước
        WebElement btn = wait.until(ExpectedConditions.presenceOfElementLocated(addToCartBtn));

        // 🚀 BÍ QUYẾT: Dùng JavaScript để cuộn trang sao cho nút nằm ở giữa màn hình
        // (center)
        ((JavascriptExecutor) browser).executeScript("arguments[0].scrollIntoView({block: 'center'});", btn);

        // Chờ nửa giây để hiệu ứng cuộn mượt mà của trình duyệt dừng hẳn lại
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        WebElement btnClick = wait.until(ExpectedConditions.elementToBeClickable(addToCartBtn));
        ((JavascriptExecutor) browser).executeScript("arguments[0].click();", btnClick);

        // [CỰC KỲ QUAN TRỌNG] Chờ popup báo "Đã thêm vào giỏ hàng!" hiện lên rồi biến
        // mất (có trycatch)
        try {
            WebDriverWait shortWait = new WebDriverWait(browser, Duration.ofSeconds(3));
            shortWait.until(ExpectedConditions.visibilityOfElementLocated(toastMessage));
            shortWait.until(ExpectedConditions.invisibilityOfElementLocated(toastMessage));
        } catch (Exception e) {
            System.out.println("Toast không hiện hoặc ẩn quá nhanh");
            try {
                Thread.sleep(2000);
            } catch (Exception ignore) {
            }
        }
    }
}