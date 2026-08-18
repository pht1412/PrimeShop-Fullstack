package com.primeshop.seller;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SellerRequest {
    private String shopName;
    private String identityCard;
    private String description;
    private String phone;
}
