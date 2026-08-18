package com.primeshop.admin;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import com.primeshop.order.OrderFilterRequest;
import com.primeshop.order.OrderResponse;
import com.primeshop.order.OrderService;
import com.primeshop.order.OrderStatus;

@RestController
@RequestMapping("/api/admin/order")
@CrossOrigin(origins = "http://localhost:5173")
public class AdminOrderController {
    @Autowired
    private OrderService orderService;

    @GetMapping("/get-all")
    public ResponseEntity<?> getAllOrders() {
        return ResponseEntity.ok(orderService.getAllOrders());
    }

    @GetMapping("/get")
    public ResponseEntity<?> getOrderById(@RequestParam Long id) {
        return ResponseEntity.ok(orderService.getOrderById(id));
    }

    @PutMapping("/update-status")
    public ResponseEntity<?> updateOrderStatus(@RequestParam Long id, @RequestParam OrderStatus status) {
        return ResponseEntity.ok(orderService.updateOrderStatus(id, status));
    }

    @PostMapping("/filter")
    public ResponseEntity<List<OrderResponse>> filterOrders(@RequestBody OrderFilterRequest filterRequest) {
        List<OrderResponse> orders = orderService.filterOrders(filterRequest);
        return ResponseEntity.ok(orders);
    }
}
