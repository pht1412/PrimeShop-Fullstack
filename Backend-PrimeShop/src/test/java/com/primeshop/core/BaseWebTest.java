package com.primeshop.core;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;

public class BaseWebTest {

    // Đây là "Con Bot" điều khiển trình duyệt. Cấp quyền protected.
    protected WebDriver browser; 
    
    // Đây là "Đồng hồ cát". Dùng để bắt con Bot chờ giao diện React xoay loading xong mới click.
    protected WebDriverWait wait; 

    // Biến lưu địa chỉ Web React của dự án 
    protected final String WEB_URL = "http://localhost:5173";

    /**
     * @BeforeEach: Khối lệnh này sẽ TỰ ĐỘNG CHẠY TRƯỚC mỗi khi 1 hàm @Test bắt đầu.
     * Nhiệm vụ: Tự động bật Chrome lên sẵn sàng.
     */
    @BeforeEach
    public void setUpBrowser() {
        ChromeOptions options = new ChromeOptions();
        
        // 🚀 BÍ QUYẾT Ở ĐÂY: Dạy bot lờ đi cảnh báo bảo mật SSL tự tạo (https://localhost)
        options.setAcceptInsecureCerts(true); // Tự động chấp nhận chứng chỉ không an toàn
        options.addArguments("--ignore-certificate-errors"); // Bỏ qua mọi lỗi liên quan đến SSL
        
        // options.addArguments("--headless"); // (Tùy chọn chạy ngầm)
        
        // Khởi động trình duyệt với bộ cấu hình mới
        browser = new ChromeDriver(options);
        
        // Phóng to toàn màn hình
        browser.manage().window().maximize();
        
        // Cài đặt thời gian chờ ngầm định
        browser.manage().timeouts().implicitlyWait(Duration.ofSeconds(5));

        // Khởi tạo đồng hồ chờ rõ ràng
        wait = new WebDriverWait(browser, Duration.ofSeconds(10));
    }

    /**
     * @AfterEach: Khối lệnh này sẽ TỰ ĐỘNG CHẠY SAU khi 1 hàm @Test chạy xong (dù Pass hay Fail).
     * Nhiệm vụ: Tự động tắt Chrome đi để dọn rác, giải phóng RAM.
     */
    @AfterEach					
    public void tearDownBrowser() {
        if (browser != null) {
            browser.quit(); // Đóng toàn bộ trình duyệt
        }
    }
}