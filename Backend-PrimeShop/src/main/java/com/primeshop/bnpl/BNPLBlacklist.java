package com.primeshop.bnpl;

import com.primeshop.user.User;
import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "bnpl_blacklist")
public class BNPLBlacklist {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    private User user;

    private String reason;
    private LocalDateTime createdAt = LocalDateTime.now();
}
