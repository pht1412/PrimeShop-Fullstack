package com.primeshop.order;

import java.util.List;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class OrderRequest {
    private List<OrderItem> orderItems;
    private String fullName;
    private String phoneNumber;
    private String address;
    private String note;
    private List<String> voucherCodes; // <-- BỔ SUNG DÒNG NÀY
}
