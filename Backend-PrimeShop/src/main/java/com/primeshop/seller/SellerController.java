package com.primeshop.seller;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.primeshop.order.OrderService;
import com.primeshop.product.ProductCardResponse;
import com.primeshop.product.ProductFilterRequest;
import com.primeshop.product.ProductRequest;
import com.primeshop.product.ProductService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/seller")
@RequiredArgsConstructor
public class SellerController {
    private final SellerService sellerService;
    private final ProductService productService;
    private final OrderService orderService;

    @PostMapping("/apply")
    @PreAuthorize("hasRole('USER')")
    public SellerResponse applyForSeller(@RequestBody SellerRequest request) {
        return sellerService.registerSeller(request);
    }

    @PostMapping("/add-product")
    @PreAuthorize("hasRole('BUSSINESS')")
    public ResponseEntity<?> addProduct(@RequestBody ProductRequest request, @RequestParam Long sellerId) {
        return ResponseEntity.ok(productService.addProduct(request, sellerId));
    }

    @PreAuthorize("hasRole('BUSSINESS')")
    @GetMapping("/products")
    public Page<ProductCardResponse> getSellerProducts(
            @ModelAttribute ProductFilterRequest request,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam Long sellerId
    ) {
        Pageable pageable = PageRequest.of(page, size);
        request.setSellerView(true);
        request.setSellerId(sellerId);
        return productService.searchProducts(request, pageable);
    }

    @PreAuthorize("hasRole('BUSSINESS')")
    @PatchMapping("/update-product")
    public ResponseEntity<?> updateProduct(@RequestParam Long id, @RequestBody ProductRequest request) {
        return ResponseEntity.ok(productService.updateProduct(id, request));
    }

    @PreAuthorize("hasRole('BUSSINESS')")
    @GetMapping("/orders")
    public ResponseEntity<?> getSellerOrders() {
        return ResponseEntity.ok(orderService.getOrdersBySeller());
    }

    @PreAuthorize("hasRole('BUSSINESS')")
    @GetMapping("/me")
    public ResponseEntity<?> getSellerProfile() {
        return ResponseEntity.ok(sellerService.getSellerProfile());
    }
}
