package com.primeshop.bnpl;

import jakarta.persistence.*;
import lombok.Data;
import java.math.BigDecimal;

@Data
@Entity
@Table(name = "bnpl_providers")
public class BNPLProvider {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private String apiKey;
    private boolean sandbox;
    private BigDecimal maxLimit;
}
