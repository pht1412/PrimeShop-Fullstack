package com.primeshop.ui.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;

public class MinigameListPage {
    private WebDriver browser;
    private WebDriverWait wait;

    private By gameCards = By.className("game-card");
    private By playButtons = By.xpath("//button[contains(., 'Chơi ngay')]");

    public MinigameListPage(WebDriver browser) {
        this.browser = browser;
        this.wait = new WebDriverWait(browser, Duration.ofSeconds(10));
    }

    public boolean isGameListDisplayed() {
        try {
            wait.until(ExpectedConditions.visibilityOfElementLocated(gameCards));
            return browser.findElements(gameCards).size() > 0;
        } catch (Exception e) {
            return false;
        }
    }

    public void clickPlayFirstGame() {
        WebElement btn = wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath("(//button[contains(., 'Chơi ngay')])[1]")));
        ((JavascriptExecutor) browser).executeScript("arguments[0].scrollIntoView({block: 'center'});", btn);
        ((JavascriptExecutor) browser).executeScript("arguments[0].click();", btn);
    }

    public void clickPlayGameByTitle(String titleContains) {
        String xpath = String.format("//div[contains(@class, 'game-card') and .//h3[contains(., '%s')]]//button[contains(., 'Chơi ngay')]", titleContains);
        WebElement btn = wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath(xpath)));
        ((JavascriptExecutor) browser).executeScript("arguments[0].scrollIntoView({block: 'center'});", btn);
        ((JavascriptExecutor) browser).executeScript("arguments[0].click();", btn);
    }
}
