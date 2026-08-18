package com.primeshop.ui.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;

public class MinigamePlayPage {
    private WebDriver browser;
    private WebDriverWait wait;

    // SweetAlert2 Locators
    private By swalPopup = By.className("swal2-popup");
    private By swalTitle = By.className("swal2-title");
    private By swalHTML = By.className("swal2-html-container");
    private By swalConfirmBtn = By.className("swal2-confirm");

    public MinigamePlayPage(WebDriver browser) {
        this.browser = browser;
        this.wait = new WebDriverWait(browser, Duration.ofSeconds(10));
    }

    // --- GAME 1: AI LÀ TRIỆU PHÚ ---
    public boolean isQuizQuestionDisplayed() {
        try {
            return wait.until(ExpectedConditions.visibilityOfElementLocated(By.className("minigame-question-card"))).isDisplayed();
        } catch (Exception e) {
            return false;
        }
    }

    public boolean isQuizSubmitBtnDisabled() {
        WebElement btn = browser.findElement(By.className("minigame-submit-btn"));
        return !btn.isEnabled();
    }

    // --- GAME 2: VÒNG QUAY MAY MẮN (LUCKY WHEEL) ---
    public boolean isLuckyWheelDisplayed() {
        try {
            return wait.until(ExpectedConditions.visibilityOfElementLocated(By.className("wheel-container"))).isDisplayed();
        } catch (Exception e) {
            return false;
        }
    }

    public void clickSpinWheel() {
        WebElement btn = wait.until(ExpectedConditions.elementToBeClickable(By.className("spin-btn-action")));
        ((JavascriptExecutor) browser).executeScript("arguments[0].click();", btn);
    }

    // --- GAME 3: TÀI XỈU ---
    public boolean isTaiXiuDisplayed() {
        try {
            return wait.until(ExpectedConditions.visibilityOfElementLocated(By.className("taixiu-container"))).isDisplayed();
        } catch (Exception e) {
            return false;
        }
    }

    public void selectTaiXiuOption(String optionText) {
        String xpath = String.format("//div[contains(@class, 'bet-box') and contains(., '%s')]", optionText);
        WebElement box = wait.until(ExpectedConditions.elementToBeClickable(By.xpath(xpath)));
        ((JavascriptExecutor) browser).executeScript("arguments[0].click();", box);
    }

    public boolean isTaiXiuRollBtnDisabled() {
        WebElement btn = browser.findElement(By.className("game-btn"));
        return !btn.isEnabled();
    }

    public void clickMoba() {
        WebElement btn = wait.until(ExpectedConditions.elementToBeClickable(By.className("game-btn")));
        ((JavascriptExecutor) browser).executeScript("arguments[0].click();", btn);
    }

    // --- XỬ LÝ KẾT QUẢ & POPUP REWARD ---
    public boolean isRewardPopupDisplayed() {
        try {
            wait.until(ExpectedConditions.visibilityOfElementLocated(swalPopup));
            WebElement htmlContent = browser.findElement(swalHTML);
            String text = htmlContent.getText().toUpperCase();
            
            // Theo như MinigamePage.tsx, popup code thưởng sẽ có đoạn chữ "CODE: " và "VNĐ"
            return text.contains("CODE:") || text.contains("VNĐ") || text.contains("HSD:");
        } catch (Exception e) {
            return false;
        }
    }

    public boolean isWinOrLoseStatusDisplayed() {
        try {
            wait.until(ExpectedConditions.visibilityOfElementLocated(swalPopup));
            String title = browser.findElement(swalTitle).getText().toUpperCase();
            return title.contains("CHIẾN THẮNG") || title.contains("CHƯA MAY MẮN");
        } catch (Exception e) {
            return false;
        }
    }

    public void closePopupIfAny() {
        try {
            WebDriverWait shortWait = new WebDriverWait(browser, Duration.ofSeconds(2));
            WebElement btn = shortWait.until(ExpectedConditions.elementToBeClickable(swalConfirmBtn));
            ((JavascriptExecutor) browser).executeScript("arguments[0].click();", btn);
        } catch (Exception e) {}
    }

    public void waitForPopupAnimation() {
        try {
            // Popup vòng quay/Tài Xỉu tốn tầm 3 tới 5 giây để Server trả kết quả và animation quay hoàn tất
            WebDriverWait longWait = new WebDriverWait(browser, Duration.ofSeconds(10));
            longWait.until(ExpectedConditions.visibilityOfElementLocated(swalPopup));
        } catch (Exception e) {}
    }
}
