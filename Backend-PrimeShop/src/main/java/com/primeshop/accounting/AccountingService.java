// package com.primeshop.accounting;

// import java.math.BigDecimal;
// import java.util.Date;

// import org.springframework.beans.factory.annotation.Autowired;
// import org.springframework.stereotype.Service;

// import com.primeshop.order.OrderRepo;


// @Service
// public class AccountingService {
//     @Autowired
//     private OrderRepo orderRepo;
//     @Autowired

//     public AccountingResponse getAccountingData(Date startDate, Date endDate) {
//         BigDecimal totalIncome = orderRepo.getTotalIncome(startDate, endDate);
//         return new AccountingResponse(startDate, endDate, totalIncome);
//     }
// }
