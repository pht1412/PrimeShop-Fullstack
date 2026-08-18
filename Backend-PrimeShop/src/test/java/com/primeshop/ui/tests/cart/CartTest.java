package com.primeshop.ui.tests.cart;

import com.primeshop.core.BaseWebTest;
import com.primeshop.ui.pages.LoginPage;
import com.primeshop.ui.pages.ProductDetailPage;
import com.primeshop.ui.pages.HeaderPage;
import com.primeshop.ui.pages.CartPage;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.support.ui.ExpectedConditions;

public class CartTest extends BaseWebTest {
    private CartPage cartPage;
    private HeaderPage headerPage;
    private LoginPage loginPage;

    private static final String P1_SLUG = "may-tinh-bang-samsung-galaxy-tab-a9-4g-4gb64gb-1";
    private static final String P1_NAME = "Máy tính bảng Samsung Galaxy Tab A9 4G 4GB/64GB";

    private static final String P2_SLUG = "may-tinh-bang-ipad-pro-m2-11-inch-wifi-2tb-1";
    private static final String P2_NAME = "Máy tính bảng iPad Pro M2 11 inch WiFi 2TB";

    @BeforeEach
    public void prepareCart() {
        // 1. Đăng nhập để có quyền dùng Giỏ hàng lưu trữ trên DB
        browser.get(WEB_URL + "/login");
        loginPage = new LoginPage(browser);
        loginPage.enterUsername("user01");
        loginPage.enterPassword("user01");
        loginPage.clickLogin();

        wait.until(ExpectedConditions.urlContains("/home"));

        headerPage = new HeaderPage(browser);
        cartPage = new CartPage(browser);

        // 2. Clear Cart trước mỗi Test để đảm bảo dữ liệu sạch
        browser.get(WEB_URL + "/cart");
        if (!cartPage.isCartEmpty()) {
            cartPage.removeAllItems();
        }
    }

    private void addProductToCart(String productSlug) {
        browser.get(WEB_URL + "/product-detail/" + productSlug);
        new ProductDetailPage(browser).clickAddToCart();
    }

    // ================== CÁC TESTCASE ================== //

    @Test
    public void TC_CC01_ThemNhieuSanPhamKhacNhau() {
        addProductToCart(P1_SLUG);
        addProductToCart(P2_SLUG);

        headerPage.goToCart();

        assertTrue(cartPage.isProductInCart(P2_NAME), "Lỗi: Thiếu " + P2_NAME);
        assertTrue(cartPage.isProductInCart(P1_NAME), "Lỗi: Thiếu " + P1_NAME);
        assertEquals(2, cartPage.getCartItemCount(), "Lỗi: Tổng số loại SP trong giỏ ảo phải là 2!");
    }

    @Test
    public void TC_CC02_CapNhatSoLuongHopLe() {
        addProductToCart(P2_SLUG);
        headerPage.goToCart();

        String priceBefore = cartPage.getFinalTotal();
        cartPage.increaseQuantity(P2_NAME); // Nâng lên 2

        String priceAfter = cartPage.getFinalTotal();
        assertNotEquals(priceBefore, priceAfter, "Lỗi: Tổng tiền không đổi sau khi tăng SL!");
    }

    @Test
    public void TC_CC03_XoaSanPhamKhoiGioHang() {
        addProductToCart(P1_SLUG);
        addProductToCart(P2_SLUG);
        headerPage.goToCart();

        cartPage.removeItem(P1_NAME);

        assertFalse(cartPage.isProductInCart(P1_NAME), "Lỗi: SP 1 vẫn còn trong giỏ!");
        assertTrue(cartPage.isProductInCart(P2_NAME), "Lỗi: SP 2 bị mất!");
    }

    @Test
    public void TC_CC04_KiemTraGioHangTrong() {
        browser.get(WEB_URL + "/cart");

        assertTrue(cartPage.isCartEmpty(), "Lỗi: Giỏ hàng chưa trống hệt lúc Init!");
        String msg = cartPage.getEmptyCartMessage();
        assertTrue(msg.contains("Giỏ hàng trống"), "Lỗi: Sai thông báo giỏ hàng trống!");
    }

    @Test
    public void TC_CC05_ThemTrungSanPham() {
        addProductToCart(P1_SLUG);
        addProductToCart(P1_SLUG); // Add lần 2

        headerPage.goToCart();

        assertEquals(1, cartPage.getCartItemCount(), "Lỗi: Không được duplicate dòng SP!");
        assertTrue(cartPage.isProductInCart(P1_NAME));
    }

    @Test
    public void TC_CC06_KhongChoGiamSoLuongDuoiMot() {
        addProductToCart(P1_SLUG);
        headerPage.goToCart();

        // Mới add thì SL = 1. Nút "-" phải bị disable.
        assertTrue(cartPage.isDecreaseButtonDisabled(P1_NAME), "Lỗi: Nút giảm số lượng không bị khoá khi SL = 1");
    }

    @Test
    public void TC_CC07_TinhTongTienVoiNhieuSanPham() {
        addProductToCart(P1_SLUG);
        addProductToCart(P2_SLUG);
        headerPage.goToCart();

        String total = cartPage.getFinalTotal();
        assertNotNull(total, "Lỗi: Không hiển thị tổng tiền");
        assertTrue(total.length() > 0);
    }

    @Test
    public void TC_CC08_LuuTrangThaiGioHangSauKhiReload() {
        addProductToCart(P1_SLUG);
        headerPage.goToCart();

        browser.navigate().refresh();

        assertTrue(cartPage.isProductInCart(P1_NAME), "Lỗi: Mất sản phẩm sau khi F5!");
    }

    @Test
    public void TC_CC09_XoaToanBoGioHang() {
        addProductToCart(P1_SLUG);
        addProductToCart(P2_SLUG);
        headerPage.goToCart();

        cartPage.removeAllItems();

        assertTrue(cartPage.isCartEmpty(), "Lỗi: Chức năng xoá giỏ hàng không làm rỗng mảng!");
        assertTrue(cartPage.getEmptyCartMessage().contains("Giỏ hàng trống"));
    }

    @Test
    public void TC_CC12_KiemTraHienThiGiaSanPham() {
        addProductToCart(P1_SLUG);
        headerPage.goToCart();

        assertTrue(cartPage.isProductInCart(P1_NAME), "Lỗi: SP không nằm trong cart!");
        assertDoesNotThrow(() -> cartPage.getFinalTotal());
    }
}