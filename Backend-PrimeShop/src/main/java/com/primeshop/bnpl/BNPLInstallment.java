package com.primeshop.bnpl;
import jakarta.persistence.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "bnpl_installments")
@Data
public class BNPLInstallment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "agreement_id")
    private BNPLAgreement agreement;

    private int installmentNumber;
    private BigDecimal amount;
    private LocalDateTime dueDate;
    private boolean paid = false;
}
