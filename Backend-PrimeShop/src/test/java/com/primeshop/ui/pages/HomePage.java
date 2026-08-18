package com.primeshop.ui.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;

public class HomePage {
    private WebDriver browser;

    // Locator cho nút chứa Tên User sau khi đăng nhập thành công
    private By loggedInUserLink = By.cssSelector("a[href='/account'] span");
    // Locator cho nút Đăng xuất
    private By logoutButton = By.xpath("//button[contains(@class, 'action-btn')]//span[text()='Thoát']");

    public HomePage(WebDriver browser) {
        this.browser = browser;
    }

    public String getLoggedInUsername() {
        return browser.findElement(loggedInUserLink).getText();
    }

    public boolean isLogoutButtonDisplayed() {
        return browser.findElement(logoutButton).isDisplayed();
    }
}