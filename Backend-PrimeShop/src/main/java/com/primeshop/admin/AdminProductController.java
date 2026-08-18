package com.primeshop.admin;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import com.primeshop.product.ProductCardResponse;
import com.primeshop.product.ProductFilterRequest;
import com.primeshop.product.ProductRepo;
import com.primeshop.product.ProductRequest;
import com.primeshop.product.ProductResponse;
import com.primeshop.product.ProductService;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/admin/product")
@CrossOrigin(origins = "http://localhost:5173", allowCredentials = "true")
@RequiredArgsConstructor
public class AdminProductController {
    @Autowired
    private ProductService productService;
    @Autowired
    private ProductRepo productRepo;

    @PostMapping("/add")
    public ResponseEntity<?> addProduct(@RequestBody ProductRequest request, @RequestParam Long sellerId) {
        return ResponseEntity.ok(productService.addProduct(request, sellerId));
    }

    @PatchMapping("/update")
    public ResponseEntity<?> updateProduct(@RequestParam Long id, @RequestBody ProductRequest request) {
        return ResponseEntity.ok(productService.updateProduct(id, request));
    }

    @PostMapping("/deactive")
    public ResponseEntity<?> deactiveProduct(@RequestParam Long id) {
        productService.deactiveProduct(id);
        return ResponseEntity.ok("Vô hiệu hóa sản phẩm thành công!");
    }

    @PostMapping("/active")
    public ResponseEntity<?> activeProduct(@RequestParam Long id) {
        productService.activeProduct(id);
        return ResponseEntity.ok("Kích hoạt sản phẩm thành công!");
    }

    @GetMapping("/is-active")
    public ResponseEntity<List<ProductResponse>> getActiveProducts() {
        return ResponseEntity.ok(productService.getActiveProducts());
    }

    @GetMapping("/is-deactive")
    public ResponseEntity<List<ProductResponse>> getDeactiveProducts() {
        return ResponseEntity.ok(productService.getDeactiveProducts());
    }

    @GetMapping
    public Page<ProductCardResponse> searchProducts(
        @ModelAttribute ProductFilterRequest request,
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "10") int size
    ) {
        Pageable pageable = PageRequest.of(page, size);
        return productService.searchProducts(request, pageable);
    }

    @GetMapping("/brands")
    public ResponseEntity<?> getBrands() {
        List<String> brands = productRepo.findDistinctBrands();
        return ResponseEntity.ok(brands);
    }
}
