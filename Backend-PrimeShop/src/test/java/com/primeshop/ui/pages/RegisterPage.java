package com.primeshop.ui.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;

public class RegisterPage {
    private WebDriver browser;

    // 1. Locators (Dựa trên id của file InputField.tsx bạn đã viết)
    private By usernameInput = By.id("username");
    private By emailInput = By.id("email");
    private By passwordInput = By.id("password");
    private By confirmPasswordInput = By.id("confirmPassword");
    // Nút submit trong AuthForm không có ID, dùng type và class
    private By registerButton = By.cssSelector("button[type='submit'].auth-btn");

    // Constructor
    public RegisterPage(WebDriver browser) {
        this.browser = browser;
    }

    // 2. Actions (Hành động)
    public void enterUsername(String username) {
        browser.findElement(usernameInput).sendKeys(username);
    }

    public void enterEmail(String email) {
        browser.findElement(emailInput).sendKeys(email);
    }

    public void enterPassword(String password) {
        browser.findElement(passwordInput).sendKeys(password);
    }

    public void enterConfirmPassword(String confirmPassword) {
        browser.findElement(confirmPasswordInput).sendKeys(confirmPassword);
    }

    public void clickRegister() {
        browser.findElement(registerButton).click();
    }
}