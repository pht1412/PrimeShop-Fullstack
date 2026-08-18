package com.primeshop.bank;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "banks")
@Data
@NoArgsConstructor
public class Bank {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long userId;

    private String bankName = "Vietcombank";

    @Lob
    private byte[] accountNumber;

    @Lob
    private byte[] token;

    private String status = "linked";

    private LocalDateTime createdAt = LocalDateTime.now();
    
    private String cardHolderName;
}
