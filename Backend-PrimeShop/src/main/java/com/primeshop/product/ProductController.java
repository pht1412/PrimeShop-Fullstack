package com.primeshop.product;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/product")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:5173")
public class ProductController {
    @Autowired
    private ProductService productService;
    @Autowired
    private ProductRepo productRepo;

    @PersistenceContext
    private EntityManager entityManager;

    @GetMapping("/test")
    public String hello() {
        return "Hello from Render!";
    }

    @GetMapping("/test-product")
    public List<Product> getAllProductsTest() {
        List<?> rows = entityManager
            .createNativeQuery("SELECT * FROM product")
            .getResultList();
        System.out.println(rows);
        return productRepo.findAll();
    }

    @GetMapping
    public ResponseEntity<List<ProductResponse>> getAllProducts() {
        return ResponseEntity.ok(productService.getActiveProducts());
    }

    @GetMapping("/category")
    public ResponseEntity<?> getProductsByCategory(@RequestParam String categorySlug) {
        return ResponseEntity.ok(productService.getProductsByCategory(categorySlug));
    }

    @GetMapping("/slug")
    public ResponseEntity<?> getProductBySlug(@RequestParam String productSlug) {
        return ResponseEntity.ok(productService.getProductBySlug(productSlug));
    }

    @GetMapping("/product-detail/{productSlug}")
    public ResponseEntity<?> getProductDetail(@PathVariable String productSlug) {
        return ResponseEntity.ok(productService.getProductBySlug(productSlug));
    }

    // @GetMapping("/all-products")
    // public ResponseEntity<?> searchProducts(@ModelAttribute ProductFilterRequest request) {
    //     return ResponseEntity.ok(productService.searchProducts(request));
    // }

    @Cacheable("product")
    @GetMapping("/all-products")
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

    @Cacheable("product")
    @GetMapping("/hot-sale")
    public ResponseEntity<List<ProductCardResponse>> getHotSaleProducts() {
        return ResponseEntity.ok(productService.getHotSaleProducts());
    }

    @Cacheable("product")
    @GetMapping("/discount")
    public ResponseEntity<List<ProductCardResponse>> getDiscountProducts() {
        return ResponseEntity.ok(productService.getDiscountProducts());
    }

    @PostMapping("/rate")
    public ResponseEntity<ProductResponse> rateProduct(
            @RequestParam String productSlug,
            @RequestParam Double rating) {
        return ResponseEntity.ok(productService.rateProduct(productSlug, rating));
    }

    @GetMapping("/top-rated")
    public ResponseEntity<List<ProductResponse>> getTopRatedProducts() {
        return ResponseEntity.ok(productService.getTopRatedProducts());
    }

    @PostMapping("/add-images")
    public ResponseEntity<ProductResponse> addImagesToProduct(@RequestParam String productSlug, @RequestParam List<String> imageUrls) {
        productService.addImagesToProduct(productSlug, imageUrls);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/delete-image")
    public ResponseEntity<ProductResponse> deleteProductImage(@RequestParam String productSlug, @RequestParam String imageUrl) {
        productService.deleteProductImage(productSlug, imageUrl);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/images/{productSlug}")
    public ResponseEntity<List<String>> getProductImages(@PathVariable String productSlug) {
        return ResponseEntity.ok(productService.getProductImages(productSlug));
    }

    @GetMapping("/count")
    public ResponseEntity<?> countProduct() {
        return ResponseEntity.ok(productService.countProduct());
    }

    @GetMapping("/rating/{id}")
    public ResponseEntity<?> getAverageRating(@PathVariable Long id) {
        return ResponseEntity.ok(productService.getAverageRating(id));
    }
    
    /* Example Postman request:
     * POST http://localhost:8080/api/products/add-images
     * 
     * Query Params:
     * productSlug: iphone-14-pro-max
     * imageUrls: https://example.com/image1.jpg
     * imageUrls: https://example.com/image2.jpg
     * imageUrls: https://example.com/image3.jpg
     * 
     * Note: Add multiple imageUrls params to send multiple images
     */
}
