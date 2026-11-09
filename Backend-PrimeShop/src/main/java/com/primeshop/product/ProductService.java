package com.primeshop.product;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import com.primeshop.category.Category;
import com.primeshop.category.CategoryRepo;
import com.primeshop.product.Product.ProductStatus;
import com.primeshop.seller.SellerProfile;
import com.primeshop.seller.SellerRepo;
import com.primeshop.user.User;
import com.primeshop.utils.SecurityUtils;
import com.primeshop.utils.SlugUtils;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ProductService {   
    @Autowired
    private ProductRepo productRepo;
    @Autowired
    private CategoryRepo categoryRepo;
    @Autowired
    private ProductSpecRepo productSpecRepo;
    @Autowired
    private ProductImageRepo productImageRepo;
    @Autowired
    private ProductReviewRepo productReviewRepo;
    @Autowired
    private SellerRepo sellerRepo;
    @Autowired
    private SecurityUtils securityUtils;


    public ProductResponse addProduct(ProductRequest request, Long sellerId) {
        return saveProduct(request, sellerId, ProductStatus.PENDING);
    }

    @Transactional
    public ProductResponse addAdminProduct(ProductRequest request, Long sellerId) {
        return saveProduct(request, sellerId, ProductStatus.APPROVED);
    }

    private ProductResponse saveProduct(
            ProductRequest request,
            Long sellerId,
            ProductStatus status
    ) {
        Category category = categoryRepo.findById(request.getCategoryId())
            .orElseThrow(() -> new RuntimeException("Không tìm thấy danh mục!"));
        SellerProfile seller = sellerRepo.findById(sellerId)
            .orElseThrow(() -> new RuntimeException("Không tìm thấy người bán!"));
        Product product = new Product(request, category);
        product.setSeller(seller);
        product.setStatus(status);
        String baseSlug = SlugUtils.toSlug(product.getName());
        String slug = baseSlug;
        int counter = 1;
        while (productRepo.existsBySlug(slug)) {
            slug = baseSlug + "-" + counter;
            counter++;
        }
        product.setSlug(slug);
        Product saved = productRepo.save(product);
        List<ProductSpec> specs = request.getSpecs() == null ? List.of() : request.getSpecs().stream()
            .filter(spec -> spec.getName() != null && !spec.getName().isBlank()
                && spec.getValue() != null && !spec.getValue().isBlank())
            .map(spec -> new ProductSpec(null, spec.getName(), spec.getValue(), product))
            .collect(Collectors.toList());
        if (!specs.isEmpty()) {
            productSpecRepo.saveAll(specs);
        }

        Product finalProduct = productRepo.findById(saved.getId())
            .orElseThrow(() -> new RuntimeException("Không tìm thấy sản phẩm!"));

        return new ProductResponse(finalProduct);
    }

    @Transactional
    public ProductResponse updateProduct(Long id, ProductRequest request) {
        User user = securityUtils.getCurrentUser();
        SellerProfile seller = sellerRepo.findByUserId(user.getId())
            .orElseThrow(() -> new RuntimeException("Tài khoản hiện tại không phải là người bán!"));
        Product product = productRepo.findById(id)
            .orElseThrow(() -> new RuntimeException("Không tìm thấy sản phẩm!"));
        if (!product.getSeller().getId().equals(seller.getId())) {
            throw new RuntimeException("Bạn không có quyền cập nhật sản phẩm này!");
        }
        if (request.getName() != null) {
            product.setName(request.getName());
            String baseSlug = SlugUtils.toSlug(request.getName());
            String slug = baseSlug;
            int counter = 1;
            while (productRepo.existsBySlug(slug)) {
                slug = baseSlug + "-" + counter;
                counter++;
            }
            product.setSlug(slug);
        }

        if (request.getBrand() != null) product.setBrand(request.getBrand());
        if (request.getImageUrl() != null) product.setImageUrl(request.getImageUrl());
        if (request.getPrice() != null) {
            product.setPrice(request.getPrice());
            if (product.getDiscountPercent() != null) {
                product.setDiscountPrice(product.calculateDiscountPrice(request.getPrice(), product.getDiscountPercent()));
            }
        }

        if (request.getDiscountPercent() != null) {
            product.setDiscountPercent(request.getDiscountPercent());
            product.setDiscountPrice(product.calculateDiscountPrice(product.getPrice(), request.getDiscountPercent()));
            product.setIsDiscounted(true);
        }
        
        if (request.getDiscountPercent() == null || request.getDiscountPercent().compareTo(BigDecimal.ZERO) == 0) {
            product.setDiscountPercent(BigDecimal.ZERO);
            product.setDiscountPrice(product.getPrice());
            product.setIsDiscounted(false);
        }

        if (request.getRating() != null) {
            product.addRating(request.getRating());
        }

        if (request.getStock() != null) product.setStock((request.getStock()));

        if (request.getCategoryId() != null) {
            Category category = categoryRepo.findById(request.getCategoryId())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy danh mục"));
            product.setCategory(category);
        }

        if (request.getDescription() != null) product.setDescription(request.getDescription());

        if (request.getSpecs() != null && !request.getSpecs().isEmpty()) {
            productSpecRepo.deleteByProduct(product);
            List<ProductSpec> specs = request.getSpecs().stream()
                .map(spec -> new ProductSpec(null, spec.getName(), spec.getValue(), product))
                .collect(Collectors.toList());
            productSpecRepo.saveAll(specs);
        }

        product.setStatus(ProductStatus.PENDING);
        productRepo.save(product);
        return new ProductResponse(product);
    }

    public void deactiveProduct(Long id) {
        Product product = productRepo.findById(id)
            .orElseThrow(() -> new RuntimeException("Không tìm thấy sản phẩm!"));
        product.setActive(false);
        productRepo.save(product);
    }

    public void activeProduct(Long id) {
        Product product = productRepo.findById(id)
            .orElseThrow(() -> new RuntimeException("Không tìm thấy sản phẩm!"));
        product.setActive(true);
        productRepo.save(product);
    }

    public List<ProductResponse> getActiveProducts() {
        List<Product> products = productRepo.findByActiveTrue().stream()
            .filter(this::isOfficialProduct)
            .filter(product -> product.getStatus() == ProductStatus.APPROVED)
            .collect(Collectors.toList());
        return products.stream()
                .map(ProductResponse::new)
                .collect(Collectors.toList());
    }

    public List<ProductResponse> getDeactiveProducts() {
        List<Product> products = productRepo.findByActiveFalse();
        return products.stream()
                .map(ProductResponse::new)
                .collect(Collectors.toList());
    }

    public List<ProductResponse> getAllProducts() {
        List<Product> products = productRepo.findAll();
        return products.stream()
                .map(ProductResponse::new)
                .collect(Collectors.toList());
    }

    // @Cacheable(value = "product", key = "#request.toCacheKey() + '-' + #pageable.pageNumber + '-' + #pageable.pageSize")
    public Page<ProductCardResponse> searchProducts(ProductFilterRequest request, Pageable pageable) {
        Specification<Product> spec = ProductSpecification.filter(request);
        Page<Product> products = productRepo.findAll(spec, pageable);
        return products.map(ProductCardResponse::new);
    }

    public ProductResponse getProductBySlug(String slug) {
        Product product = productRepo.findBySlugAndActiveTrue(slug)
            .orElseThrow(() -> new RuntimeException("Không tìm thấy sản phẩm!"));
        if (!isOfficialProduct(product) || product.getStatus() != ProductStatus.APPROVED) {
            throw new RuntimeException("Không tìm thấy sản phẩm!");
        }
        return new ProductResponse(product);
    }

    public List<C2CProductResponse> getC2CProducts() {
        return productRepo.findByStatusAndActiveTrueAndSeller_ShopNameNotOrderByCreatedAtDesc(
                ProductStatus.APPROVED,
                SellerProfile.OFFICIAL_SHOP_NAME
            ).stream()
            .map(product -> new C2CProductResponse(product, isCurrentUserSeller(product)))
            .collect(Collectors.toList());
    }

    public C2CProductResponse getC2CProduct(Long productId) {
        Product product = productRepo.findByIdAndActiveTrue(productId)
            .filter(item -> item.getStatus() == ProductStatus.APPROVED && !isOfficialProduct(item))
            .orElseThrow(() -> new RuntimeException("Không tìm thấy sản phẩm C2C!"));
        return new C2CProductResponse(product, isCurrentUserSeller(product));
    }

    public Product getProductById(Long id) {
        return productRepo.findByIdAndActiveTrue(id)
            .orElseThrow(() -> new RuntimeException("Không tìm thấy sản phẩm!"));
    }

    public List<ProductResponse> getProductsByCategory(String categorySlug) {
        return productRepo.findByCategorySlugAndActiveTrue(categorySlug)
            .stream()
            .filter(product -> product.getStatus() == ProductStatus.APPROVED)
            .filter(this::isOfficialProduct)
            .map(ProductResponse::new)
            .collect(Collectors.toList());
    }

    public List<ProductCardResponse> getHotSaleProducts() {
        List<Product> products = productRepo.findByActiveTrueOrderBySoldDesc();
        return products.stream()
            .filter(product -> product.getStatus() == ProductStatus.APPROVED)
            .filter(this::isOfficialProduct)
            .map(ProductCardResponse::new)
            .collect(Collectors.toList());
    }
   
    public List<ProductCardResponse> getDiscountProducts() {
        List<Product> products = productRepo.findByIsDiscountedTrueAndActiveTrueOrderByDiscountPercentDesc();
        return products.stream()
            .filter(product -> product.getStatus() == ProductStatus.APPROVED)
            .filter(this::isOfficialProduct)
            .map(ProductCardResponse::new)
            .collect(Collectors.toList());
    }

    public ProductResponse rateProduct(String productSlug, Double rating) {
        Product product = productRepo.findBySlugAndActiveTrue(productSlug)
            .orElseThrow(() -> new RuntimeException("Không tìm thấy sản phẩm!"));
        
        product.addRating(rating);
        productRepo.save(product);
        
        return new ProductResponse(product);
    }

    public List<ProductResponse> getTopRatedProducts() {
        return productRepo.findByActiveTrue().stream()
            .filter(product -> product.getStatus() == ProductStatus.APPROVED)
            .filter(this::isOfficialProduct)
            .sorted((p1, p2) -> p2.getRating().compareTo(p1.getRating()))
            .map(ProductResponse::new)
            .collect(Collectors.toList());
    }

    private boolean isOfficialProduct(Product product) {
        return product.getSeller() != null
            && SellerProfile.OFFICIAL_SHOP_NAME.equals(product.getSeller().getShopName());
    }

    private boolean isCurrentUserSeller(Product product) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication != null
            && authentication.isAuthenticated()
            && !"anonymousUser".equals(authentication.getName())
            && product.getSeller().getUser() != null
            && authentication.getName().equals(product.getSeller().getUser().getUsername());
    }

    public void addImagesToProduct(String productSlug, List<String> imageUrls) {
        Product product = productRepo.findBySlugAndActiveTrue(productSlug)
            .orElseThrow(() -> new RuntimeException("Không tìm thấy sản phẩm!"));

        for (String url : imageUrls) {
            ProductImage image = new ProductImage();
            image.setUrl(url);
            image.setProduct(product);
            productImageRepo.save(image);
            product.getImages().add(image);
        }

        productRepo.save(product);
    }

    public void deleteProductImage(String productSlug, String imageUrl) {
        Product product = productRepo.findBySlugAndActiveTrue(productSlug)
            .orElseThrow(() -> new RuntimeException("Không tìm thấy sản phẩm!"));
        ProductImage image = product.getImages().stream()
            .filter(i -> i.getUrl().equals(imageUrl))
            .findFirst()
            .orElseThrow(() -> new RuntimeException("Không tìm thấy hình ảnh!"));
        product.getImages().remove(image);
        productImageRepo.delete(image);
        productRepo.save(product);
    }

    public List<String> getProductImages(String productSlug) {
        Product product = productRepo.findBySlugAndActiveTrue(productSlug)
            .orElseThrow(() -> new RuntimeException("Không tìm thấy sản phẩm!"));
        return product.getImages().stream()
            .map(ProductImage::getUrl)
            .collect(Collectors.toList());
    }

    public Long countProduct() {
        return productRepo.countByActiveTrue();
    }

    public Double getAverageRating(Long productId) {
        return productReviewRepo.findAverageRatingByProductId(productId);
    }
}
