package com.primeshop.product;

import java.math.BigDecimal;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class ProductCardResponse {
    private Long id;
    // private String code;
    private String name;
    private String slug;
    private String brand;
    private BigDecimal price;
    private BigDecimal discountPercent;
    private BigDecimal discountPrice;
    private Boolean isDiscounted;
    private Double rating;
    private String imageUrl;
    private Integer sold;
    private String category;
    private Long sellerId;
    private String shopName;
    private Product.ProductStatus status;

    public ProductCardResponse(Product product) {
        this.id = product.getId();
        // this.code = CodeUtils.encodeProductId(product.getId());
        this.name = product.getName();
        this.slug = product.getSlug();
        this.brand = product.getBrand();      
        this.price = product.getPrice();
        this.discountPercent = product.getDiscountPercent();
        this.discountPrice = product.getDiscountPrice();
        this.isDiscounted = product.getIsDiscounted();
        this.rating = product.getRating();
        this.imageUrl = product.getImageUrl();
        this.sold = product.getSold();
        this.category = product.getCategory().getName();
        this.sellerId = product.getSeller().getId();
        this.shopName = product.getSeller().getShopName();
        this.status = product.getStatus();
    }    
}

