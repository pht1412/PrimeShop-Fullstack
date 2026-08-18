package com.primeshop.bnpl;

import com.primeshop.order.Order;
import com.primeshop.order.OrderRepo;
import com.primeshop.user.UserRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/bnpl")
@RequiredArgsConstructor
public class BnplController {

    private final BNPLAgreementRepository agreementRepo;
    private final BNPLProviderRepository providerRepo;
    private final BNPLBlacklistRepository blacklistRepo;
    private final OrderRepo orderRepo;
    private final UserRepo userRepo;

    // ========== 1️⃣ Khởi tạo BNPL (Trả sau) ==========
    @PostMapping("/init")
    public ResponseEntity<?> initBnpl(@RequestBody BnplInitRequest req) {
        Order order = orderRepo.findById(req.getOrderId())
                .orElseThrow(() -> new RuntimeException("Order not found"));

        // 🔹 Kiểm tra xem đã có BNPLAgreement cho order này chưa
        BNPLAgreement existing = agreementRepo.findByOrderId(req.getOrderId()).orElse(null);
        if (existing != null) {
            return ResponseEntity.ok(Map.of(
                    "status", existing.getStatus(),
                    "consentUrl", "https://sandbox.fundiin.vn/checkout?order=" + existing.getId()));
        }

        // 🔹 Nếu chưa có thì tạo mới
        BNPLAgreement agr = new BNPLAgreement();
        agr.setUser(order.getUser());
        agr.setOrder(order);
        agr.setProvider("Fundiin");
        agr.setStatus("PENDING");
        agr.setTotalAmount(order.getFinalAmount());
        agreementRepo.save(agr);

        return ResponseEntity.ok(Map.of(
                "status", "PENDING",
                "consentUrl", "https://sandbox.fundiin.vn/checkout?order=" + agr.getId()));
    }

    // ========== 2️⃣ Xác nhận BNPL (sau khi Fundiin callback) ==========
    @PostMapping("/confirm")
    public ResponseEntity<?> confirmBnpl(@RequestBody Map<String, Object> body) {
        Long orderId = Long.valueOf(body.get("orderId").toString());
        String status = body.get("status").toString();

        BNPLAgreement agreement = agreementRepo.findByOrderId(orderId)
                .orElseThrow(() -> new RuntimeException("Agreement not found"));

        agreement.setStatus(status.toUpperCase());

        // Nếu Fundiin xác nhận thành công thì cập nhật đơn hàng
        if ("APPROVED".equalsIgnoreCase(status)) {
            Order order = agreement.getOrder();
            order.setStatus(com.primeshop.order.OrderStatus.CONFIRMED);
            orderRepo.save(order);
        }

        agreementRepo.save(agreement);

        return ResponseEntity.ok(Map.of(
                "message", "BNPL agreement updated",
                "agreementStatus", agreement.getStatus()));
    }

    // ========== 3️⃣ Danh sách giao dịch BNPL ==========
    @GetMapping("/orders")
    public List<BNPLAgreement> getAllAgreements() {
        return agreementRepo.findAll();
    }

    // ========== 4️⃣ Chi tiết 1 giao dịch ==========
    @GetMapping("/orders/{orderId}")
    public ResponseEntity<?> getAgreementDetail(@PathVariable Long orderId) {
        return agreementRepo.findByOrderId(orderId)
                .map(a -> {
                    BNPLAgreementDTO dto = new BNPLAgreementDTO();
                    dto.setId(a.getId());
                    dto.setProvider(a.getProvider());
                    dto.setFundiinOrderId(a.getFundiinOrderId());
                    dto.setTotalAmount(a.getTotalAmount());
                    dto.setStatus(a.getStatus());
                    dto.setCreatedAt(a.getCreatedAt());
                    dto.setDueDate(a.getDueDate());
                    dto.setOrderId(a.getOrder().getId());
                    dto.setUsername(a.getUser().getUsername());
                    return ResponseEntity.ok(dto);
                })
                .orElse(ResponseEntity.notFound().build());
    }

    // ========== 5️⃣ Gửi nhắc trả góp ==========
    @PostMapping("/reminder/{installmentId}")
    public ResponseEntity<?> sendReminder(@PathVariable Long installmentId) {
        return ResponseEntity.ok(Map.of("message", "Reminder sent for installment " + installmentId));
    }

    // ========== 6️⃣ Blacklist người dùng ==========
    @PostMapping("/blacklist/{userId}")
    public ResponseEntity<?> addToBlacklist(@PathVariable Long userId, @RequestBody Map<String, String> body) {
        var user = userRepo.findById(userId).orElseThrow();
        var entry = new BNPLBlacklist();
        entry.setUser(user);
        entry.setReason(body.getOrDefault("reason", "Overdue payment"));
        blacklistRepo.save(entry);
        return ResponseEntity.ok(Map.of("message", "User blacklisted"));
    }

    // ========== 7️⃣ Danh sách blacklist ==========
    @GetMapping("/blacklist")
    public List<BlacklistDTO> getBlacklist() {
        return blacklistRepo.findAll().stream()
                .map(BlacklistDTO::from)
                .toList();
    }

    // ========== 8️⃣ Báo cáo tổng hợp ==========
    @GetMapping("/reports")
    public ResponseEntity<?> getReports() {
        long total = agreementRepo.count();
        long approved = agreementRepo.countByStatus("APPROVED");
        return ResponseEntity.ok(Map.of(
                "totalAgreements", total,
                "approved", approved));
    }

    // ========== 9️⃣ Xuất CSV ==========
    @GetMapping("/export")
    public void exportToCSV(HttpServletResponse response) throws IOException {
        response.setContentType("text/csv");
        response.setHeader("Content-Disposition", "attachment; filename=bnpl_orders.csv");
        var agreements = agreementRepo.findAll();
        try (PrintWriter writer = response.getWriter()) {
            writer.println("id,user,provider,status,totalAmount,dueDate");
            for (BNPLAgreement a : agreements) {
                writer.println(a.getId() + "," +
                        a.getUser().getUsername() + "," +
                        a.getProvider() + "," +
                        a.getStatus() + "," +
                        a.getTotalAmount() + "," +
                        a.getDueDate());
            }
        }
    }

    // ========== 🔟 Cấu hình API Fundiin ==========
    @PutMapping("/config")
    public ResponseEntity<?> updateConfig(@RequestBody Map<String, String> body) {
        var config = providerRepo.findByName("Fundiin").orElse(new BNPLProvider());
        config.setName("Fundiin");
        config.setApiKey(body.get("apiKey"));
        config.setSandbox(Boolean.parseBoolean(body.getOrDefault("sandbox", "true")));
        config.setMaxLimit(new BigDecimal(body.getOrDefault("maxLimit", "10000000")));
        providerRepo.save(config);
        return ResponseEntity.ok(Map.of("message", "Config updated"));
    }
}
