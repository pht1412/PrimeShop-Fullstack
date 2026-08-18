package com.primeshop.seller;

import lombok.Data;

@Data
public class SellerResponse {
    private Long id;
    private String shopName;
    private String identityCard;
    private String description;
    private String phone;
    private String status;
    private Long userId;

    public SellerResponse(SellerProfile sellerProfile) {
        this.id = sellerProfile.getId();
        this.shopName = sellerProfile.getShopName();
        this.identityCard = sellerProfile.getIdentityCard();
        this.description = sellerProfile.getDescription();
        this.phone = sellerProfile.getPhone();
        this.status = sellerProfile.getStatus().name();
        if (sellerProfile.getUser() != null) {
            this.userId = sellerProfile.getUser().getId();
        }
    }
}
