package com.primeshop.order;

import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.primeshop.payment.method.vnpayinstallment.VNPayInstallmentRequest;
import com.primeshop.payment.method.vnpayinstallment.VNPayInstallmentResponse;
import com.primeshop.payment.method.vnpayinstallment.VNPayInstallmentService;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestParam;

@RestController
@RequestMapping("/api/order")
@CrossOrigin(origins = "http://localhost:5173")
public class OrderController {
    @Autowired
    private OrderService orderService;
    
    @Autowired
    private VNPayInstallmentService vnPayInstallmentService;

    @PostMapping("/create")
    public ResponseEntity<?> createOrder(@RequestBody OrderRequest request) {
        return ResponseEntity.ok(orderService.createOrder(request));
    }

    @GetMapping("/get")
    public ResponseEntity<List<OrderResponse>> getOrder() {
        return ResponseEntity.ok(orderService.getOrdersByUser());
    }

    @GetMapping("/all-orders")
    public ResponseEntity<?> getAllOrders(@ModelAttribute OrderFilterRequest request) {
        return ResponseEntity.ok(orderService.searchOrders(request));
    }

    @GetMapping("/count")
    public ResponseEntity<?> countOrder() {
        return ResponseEntity.ok(orderService.countOrder());
    }
    
    /**
     * Tạo thanh toán trả góp VNPay cho đơn hàng
     */
    @PostMapping("/{orderId}/installment/payment")
    public ResponseEntity<?> createInstallmentPayment(@PathVariable Long orderId, 
                                                     @RequestBody VNPayInstallmentRequest request) {
        try {
            // Set orderId từ path variable
            request.setOrderId(orderId);
            
            VNPayInstallmentResponse response = vnPayInstallmentService.createInstallmentPayment(request);
            
            return ResponseEntity.ok(response);
            
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of(
                "error", "INVALID_REQUEST",
                "message", e.getMessage()
            ));
            
        } catch (IllegalStateException e) {
            return ResponseEntity.badRequest().body(Map.of(
                "error", "INVALID_STATE", 
                "message", e.getMessage()
            ));
            
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of(
                "error", "INTERNAL_ERROR",
                "message", "An error occurred while creating installment payment"
            ));
        }
    }
    
    /**
     * Lấy thông tin trả góp của đơn hàng
     */
    @GetMapping("/{orderId}/installment")
    public ResponseEntity<?> getOrderInstallmentInfo(@PathVariable Long orderId) {
        try {
            var agreement = vnPayInstallmentService.getInstallmentAgreementByOrderId(orderId);
            
            if (agreement.isPresent()) {
                return ResponseEntity.ok(agreement.get());
            } else {
                return ResponseEntity.notFound().build();
            }
            
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of(
                "error", "INTERNAL_ERROR",
                "message", "An error occurred while getting installment information"
            ));
        }
    }

    @PutMapping("/update-status")
    public ResponseEntity<?> updateStatus(
            @RequestParam Long id, 
            @RequestParam String status) {

        try {
            boolean updated = orderService.updateOrderStatus(id, status);

            if (updated) {
                return ResponseEntity.ok(Map.of("message", "Cập nhật trạng thái thành công"));
            } else {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(Map.of("message", "Không tìm thấy đơn hàng hoặc trạng thái không hợp lệ"));
            }
        } catch (Exception e) {
            // Log lỗi chi tiết ở đây nếu cần
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("message", "Lỗi hệ thống khi cập nhật trạng thái"));
        }
    }

}
