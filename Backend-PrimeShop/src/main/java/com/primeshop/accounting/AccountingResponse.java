package com.primeshop.accounting;

import java.math.BigDecimal;
import java.util.Date;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class AccountingResponse {
    private Date startDate;
    private Date endDate;
    private String orderId;
    private String customer;
    private String product;
    private Integer quantity;
    private BigDecimal totalPrice;
    private BigDecimal totalIncome;
    private BigDecimal totalProfit;

    public AccountingResponse(Date startDate, Date endDate, BigDecimal totalIncome) {
        this.startDate = startDate;
        this.endDate = endDate;
        this.totalIncome = totalIncome;
    }
}
