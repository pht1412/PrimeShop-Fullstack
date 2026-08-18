package com.primeshop.user;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Entity
@NoArgsConstructor
public class Role {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, unique = true)
    private RoleName name;    

    public Role(RoleName name) {
        this.name = name;
    }    

    public enum RoleName {
        ROLE_USER,
        ROLE_BUSSINESS,
        ROLE_ADMIN,
        ROLE_SELLER,
        ROLE_DELIVERY_UNIT
    }
}
