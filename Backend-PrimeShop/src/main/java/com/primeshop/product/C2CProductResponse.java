package com.primeshop.product;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;
import com.primeshop.seller.SellerProfile;
import lombok.Data;

@Data
public class C2CProductResponse {
    private Long id;
    private String name;
    private String slug;
    private String brand;
    private BigDecimal price;
    private String imageUrl;
    private List<String> images;
    private Integer stock;
    private String category;
    private Long sellerId;
    private String shopName;
    private String description;
    private Product.ProductStatus status;
    private boolean owner;

    public C2CProductResponse(Product product, boolean owner) {
        this.id = product.getId();
        this.name = product.getName();
        this.slug = product.getSlug();
        this.brand = product.getBrand();
        this.price = product.getPrice();
        this.imageUrl = product.getImageUrl();
        this.images = product.getImages().stream()
            .map(ProductImage::getUrl)
            .collect(Collectors.toList());
        this.stock = product.getStock();
        this.category = product.getCategory().getName();
        this.sellerId = product.getSeller().getId();
        this.shopName = product.getSeller().getShopName();
        this.description = product.getDescription();
        this.status = product.getStatus();
        this.owner = owner;
    }
}
