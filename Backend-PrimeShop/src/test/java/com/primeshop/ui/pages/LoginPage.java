package com.primeshop.ui.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

public class LoginPage {
    private WebDriver browser;

    private By usernameInput = By.id("username");
    private By passwordInput = By.id("password");
    private By loginButton = By.cssSelector("button[type='submit'].auth-btn");

    public LoginPage(WebDriver browser) {
        this.browser = browser;
    }

    public void enterUsername(String username) {
        WebElement input = browser.findElement(usernameInput);
        input.clear();
        input.sendKeys(username);
    }

    public void enterPassword(String password) {
        WebElement input = browser.findElement(passwordInput);
        input.clear();
        input.sendKeys(password);
    }

    public void clickLogin() {
        browser.findElement(loginButton).click();
    }
}