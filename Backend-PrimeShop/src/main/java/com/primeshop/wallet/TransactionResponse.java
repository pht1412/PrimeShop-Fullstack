package com.primeshop.wallet;

import java.util.Date;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor @AllArgsConstructor
public class TransactionResponse {
    private Long id;
    private Double amount;
    private String type;
    private String description;
    private Date createdAt;

    // public TransactionResponse(Long id, Double amount, String type, String description, Date createdAt) {
    //     this.id = id;
    //     this.amount = amount;
    //     this.type = type;
    //     this.description = description;
    //     this.createdAt = createdAt;
    // }
    // Getters & Setters
    // ...
}