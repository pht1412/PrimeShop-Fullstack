package com.primeshop.bnpl;
import jakarta.persistence.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "bnpl_payments")
@Data
public class BNPLPayment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "installment_id")
    private BNPLInstallment installment;

    private LocalDateTime paidAt;
    private BigDecimal amountPaid;
    private String status; // SUCCESS, FAILED, OVERDUE
}
