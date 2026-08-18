package com.primeshop.stock;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ExportTransactionRequest {
    
    @NotNull(message = "Product ID không được để trống")
    private Long productId;
    
    @NotNull(message = "Số lượng không được để trống")
    @Positive(message = "Số lượng phải lớn hơn 0")
    private Integer quantity;
    
    @NotNull(message = "Giá bán đơn vị không được để trống")
    @Positive(message = "Giá bán đơn vị phải lớn hơn 0")
    private BigDecimal unitPrice;
    
    @NotNull(message = "Giá vốn đơn vị không được để trống")
    @Positive(message = "Giá vốn đơn vị phải lớn hơn 0")
    private BigDecimal unitCost;
    
    @Size(max = 255, message = "Tên khách hàng không được vượt quá 255 ký tự")
    private String customerName;
    
    @Size(max = 100, message = "Số hóa đơn không được vượt quá 100 ký tự")
    private String invoiceNumber;
    
    @Size(max = 1000, message = "Ghi chú không được vượt quá 1000 ký tự")
    private String notes;
    
    private LocalDateTime exportDate;
} 