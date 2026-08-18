package com.primeshop.product;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;
import com.primeshop.seller.SellerResponse;
import lombok.Data;
import lombok.NoArgsConstructor;
import com.primeshop.product.Product.ProductStatus;;

@Data
@NoArgsConstructor
public class ProductResponse {
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
    private List<String> images;
    private Integer stock;
    private Integer sold;
    private String category;
    private List<ProductSpecResponse> specs;
    private String description;
    private SellerResponse seller;
    private Long sellerId;
    private String shopName;

    public ProductResponse(Product product) {
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
        this.images = product.getImages().stream()
            .map(ProductImage::getUrl)
            .collect(Collectors.toList());
        this.stock = product.getStock();
        this.sold = product.getSold();
        this.category = product.getCategory().getName();
        this.specs = product.getSpecs().stream()
            .map(ProductSpecResponse::new)
            .collect(Collectors.toList());
        this.description = product.getDescription();
        this.seller = new SellerResponse(product.getSeller());
        this.sellerId = (product.getSeller() != null) ? product.getSeller().getId() : null;
        this.shopName = (product.getSeller() != null) ? product.getSeller().getShopName() : "Unknown Shop";
    }    
}
