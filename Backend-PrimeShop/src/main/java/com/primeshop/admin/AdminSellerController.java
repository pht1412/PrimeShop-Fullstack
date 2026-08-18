package com.primeshop.admin;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import java.util.List;
import com.primeshop.product.ProductResponse;
import com.primeshop.seller.SellerResponse;
import com.primeshop.seller.SellerService;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/admin/seller")
@PreAuthorize("hasRole('ADMIN')")
public class AdminSellerController {
    private final SellerService sellerService;

    @PatchMapping("/approve-registration")
    public String approveSeller(@RequestParam Long sellerId) {
        return "Đã phê duyệt người bán thành công: " + sellerService.approveSeller(sellerId).getShopName();
    }

    @PatchMapping("/approve-products")
    public ProductResponse approveSellerProducts(@RequestParam Long sellerId, @RequestParam Long productId) {
        return sellerService.approveSellerProducts(sellerId, productId);
    }

    @GetMapping("/pending-registrations")
    public List<SellerResponse> getPendingSellers() {
        return sellerService.getPendingSellers();
    }

    // [ADD] API lấy danh sách sản phẩm đang chờ duyệt
    @GetMapping("/pending-products")
    public List<ProductResponse> getPendingProducts() {
        return sellerService.getPendingProducts();
    }

    @PatchMapping("/ban-seller")
    public String banSeller(@RequestParam Long sellerId) {
        SellerResponse response = sellerService.banSeller(sellerId);
        return "Đã chặn người bán thành công: " + response.getShopName();
    }

    // [ADD] API Từ chối sản phẩm
    @PatchMapping("/reject-product")
    public ProductResponse rejectProduct(@RequestParam Long sellerId, @RequestParam Long productId) {
        return sellerService.rejectProduct(sellerId, productId);
    }

    // [ADD] API Lấy danh sách TOÀN BỘ người bán (Verified, Pending, Banned)
    @GetMapping("/all")
    public List<SellerResponse> getAllSellers() {
        return sellerService.getAllSellers();
    }
}
