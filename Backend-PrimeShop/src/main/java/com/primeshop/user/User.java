package com.primeshop.user;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import com.primeshop.cart.Cart;
import java.math.BigDecimal;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "users")
public class User implements UserDetails {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String username;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(columnDefinition = "text")
    private String fullName;

    private String phoneNumber;

    @Column(columnDefinition = "text")
    private String address;

    @Column(columnDefinition = "text")
    private String avatar;

    @Column(precision = 18, scale = 2)
    private BigDecimal balance = BigDecimal.ZERO; // Default 0.00
    private Integer points = 0; // Điểm thưởng
    private Double walletBalance;
    private Double point;
    private Boolean walletActive = false; // Trạng thái kích hoạt ví

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
        name = "users_roles",
        joinColumns = @JoinColumn(name = "user_id"),
        inverseJoinColumns = @JoinColumn(name = "role_id")
    )
    private Set<Role> roles = new HashSet<>();

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Cart> carts;

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        // return Collections.emptyList(); // <-- BỎ DÒNG CŨ
        
        // DÙNG DÒNG NÀY:
        // Nó sẽ "biến" Set<Role> của anh thành Set<SimpleGrantedAuthority>
        // (Giả sử file Role.java của anh có hàm 'getName()' trả về "ROLE_USER")
        return roles.stream()
                .map(role -> new SimpleGrantedAuthority(role.getName().name()))
                .collect(Collectors.toList());
    }

    @Override
    public boolean isAccountNonExpired() { return true; }
    @Override
    public boolean isAccountNonLocked() { return true; }
    @Override
    public boolean isCredentialsNonExpired() { return true; }
    @Override
    public boolean isEnabled() { return true; }
}