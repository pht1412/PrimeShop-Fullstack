package com.primeshop.bnpl;

import com.primeshop.order.Order;
import com.primeshop.user.User;
import jakarta.persistence.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "bnpl_agreements")
public class BNPLAgreement {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", unique = true)
    private Order order;

    private String provider; // e.g. "Fundiin"
    private String fundiinOrderId; // ID trả về từ Fundiin
    private BigDecimal totalAmount;
    private String status; // PENDING, APPROVED, REJECTED
    private LocalDateTime createdAt = LocalDateTime.now();
    private LocalDateTime dueDate;
}
