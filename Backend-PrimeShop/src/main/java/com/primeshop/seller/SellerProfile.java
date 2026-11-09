package com.primeshop.seller;

import com.primeshop.user.User;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "seller_profiles")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SellerProfile {
    public static final String OFFICIAL_SHOP_NAME = "PrimeShop Official";

    @Id
    @GeneratedValue
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false, length = 255)
    private String shopName;

    private String identityCard;

    @Column(length = 1000)
    private String description;

    private String phone;

    @Enumerated(EnumType.STRING)
    private SellerStatus status;

    public enum SellerStatus {
        PENDING_REVIEW, VERIFIED_SELLER, BANNED_SELLER
    }
}
