package com.primeshop.ui.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import java.time.Duration;

public class CheckoutPage {
    private WebDriver browser;
    private WebDriverWait wait;
    private Actions actions;

    private By fullNameInput = By.id("fullName");
    private By addressInput = By.id("address");
    private By phoneInput = By.id("phone");
    private By noteInput = By.id("note");
    private By submitBtn = By.xpath("//button[@type='submit' and contains(@class, 'auth-btn')]");

    public CheckoutPage(WebDriver browser) {
        this.browser = browser;
        this.wait = new WebDriverWait(browser, Duration.ofSeconds(10));
        this.actions = new Actions(browser); // Khởi tạo bộ giả lập chuột vật lý
    }

    // --- MỞ DROPDOWN BẰNG CHUỘT VẬT LÝ ---
    public void selectCity(String cityName) {
        // Tìm đúng thẻ div hiển thị của MUI thay vì thẻ input ẩn
        WebElement cityDropdown = wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//div[@id='city']")));
        
        // Cuộn tới và dùng chuột click thật
        ((JavascriptExecutor) browser).executeScript("arguments[0].scrollIntoView({block: 'center'});", cityDropdown);
        try { Thread.sleep(500); } catch (Exception e) {}
        actions.moveToElement(cityDropdown).click().perform();

        // Chờ menu mở ra
        try { Thread.sleep(1000); } catch (Exception e) {}

        // Chọn Thành phố
        String optionXpath = String.format("//li[@role='option' and contains(text(), '%s')]", cityName);
        WebElement option = wait.until(ExpectedConditions.elementToBeClickable(By.xpath(optionXpath)));
        actions.moveToElement(option).click().perform();
        
        try { Thread.sleep(1000); } catch (Exception e) {} // Chờ React load Quận
    }

    public void selectDistrict(String districtName) {
        WebElement districtDropdown = wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//div[@id='district']")));
        
        ((JavascriptExecutor) browser).executeScript("arguments[0].scrollIntoView({block: 'center'});", districtDropdown);
        try { Thread.sleep(500); } catch (Exception e) {}
        actions.moveToElement(districtDropdown).click().perform();

        try { Thread.sleep(1000); } catch (Exception e) {}

        String optionXpath = String.format("//li[@role='option' and contains(text(), '%s')]", districtName);
        WebElement option = wait.until(ExpectedConditions.elementToBeClickable(By.xpath(optionXpath)));
        actions.moveToElement(option).click().perform();
    }

    public void fillFullName(String name) { fillInput(fullNameInput, name); }
    public void fillAddress(String address) { fillInput(addressInput, address); }
    public void fillPhone(String phone) { fillInput(phoneInput, phone); }
    public void fillNote(String note) { fillInput(noteInput, note); }

    private void fillInput(By locator, String value) {
        WebElement element = wait.until(ExpectedConditions.visibilityOfElementLocated(locator));
        element.sendKeys(Keys.chord(Keys.CONTROL, "a"), Keys.BACK_SPACE);
        element.sendKeys(value);
    }

    public void clickPlaceOrder() {
        // 1. Tuyệt đối KHÔNG dùng 'novalidate' nữa để giữ nguyên tính năng của trình duyệt
        WebElement btn = wait.until(ExpectedConditions.elementToBeClickable(submitBtn));
        
        // 2. Cuộn tới nút
        ((JavascriptExecutor) browser).executeScript("arguments[0].scrollIntoView({block: 'center'});", btn);
        try { Thread.sleep(500); } catch (Exception e) {}

        // 3. DÙNG CLICK CHUẨN (Native Click) CỦA SELENIUM
        // Phải dùng click này thì Chrome mới nhận diện là người dùng bấm và bật Tooltip HTML5 lên
        btn.click(); 
    }

    // Bắt thông báo lỗi của chính Trình duyệt (HTML5)
    public String getNativeValidationMessage() {
        // Mẹo: Material UI luôn giấu một thẻ <input name="city"> ẩn bên dưới cái Select 
        // để hỗ trợ HTML5 Validation
        WebElement hiddenCityInput = browser.findElement(By.name("city"));
        
        // Trích xuất dòng chữ "Please fill out this field" đang kẹt trong đó
        return hiddenCityInput.getAttribute("validationMessage");
    }

    public String getFullNameValue() {
        return browser.findElement(fullNameInput).getAttribute("value");
    }
}