package com.primeshop.wallet;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.MediaType;

import java.util.*;
import java.util.stream.Collectors;
import com.primeshop.wallet.WalletTransactionRepository;

@RestController
@RequestMapping(value = "/api/wallet", produces = MediaType.APPLICATION_JSON_VALUE + ";charset=UTF-8")
public class WalletController {

    @Autowired
    private WalletService walletService;

    @Autowired
    private WalletTransactionRepository walletTransactionRepository; // <-- Thêm dòng này

    // Xem số dư
    @GetMapping("/balance")
    public ResponseEntity<WalletBalanceResponse> getBalance(@RequestParam Long userId) {
        return ResponseEntity.ok(walletService.getBalance(userId));
    }

    // Thanh toán đơn hàng + tích điểm
    @PostMapping("/pay")
    public ResponseEntity<String> payOrder(@RequestParam Long userId, @RequestBody PaymentRequest request) {
        walletService.payOrder(userId, request);
        return ResponseEntity.ok("Payment successful, 10% value added to your points");
    }

    // Sinh QR nạp tiền
    @PostMapping("/deposit/qr")
    public ResponseEntity<QrResponse> generateDepositQr(@RequestParam Long userId, @RequestBody DepositRequest request) {
        return ResponseEntity.ok(walletService.generateDepositQr(userId, request));
    }

    // Lịch sử giao dịch
    @GetMapping("/transactions")
    public ResponseEntity<List<TransactionResponse>> getTransactions(@RequestParam Long userId) {
        return ResponseEntity.ok(walletService.getTransactionHistory(userId));
    }

    // Webhook cho payment gateway callback
    @PostMapping("/deposit/callback")
    public ResponseEntity<String> depositCallback(@RequestParam Long userId, @RequestParam Double amount, @RequestParam String signature) {
        // Validate signature nếu cần
        walletService.updateBalanceAfterDeposit(userId, amount);
        return ResponseEntity.ok("Deposit updated");
    }

    // Xác nhận nạp tiền
    @PostMapping("/deposit/confirm")
    public ResponseEntity<String> confirmDeposit(@RequestParam Long userId, @RequestParam Double amount, @RequestParam String signature) {
        walletService.confirmDeposit(userId, amount, signature);
        return ResponseEntity.ok("Nạp tiền thành công");
    }

    // Kích hoạt ví
    @PostMapping("/activate")
    public ResponseEntity<String> activateWallet(@RequestParam Long userId) {
        walletService.activateWallet(userId);
        return ResponseEntity.ok("Wallet activated successfully");
    }

    // Lấy các khoản nạp tiền đang chờ xác nhận
    @GetMapping("/pending-deposits")
    public ResponseEntity<List<Map<String, Object>>> getPendingDeposits() {
        List<WalletTransaction> pending = walletTransactionRepository.findByType("DEPOSIT_PENDING");
        List<Map<String, Object>> result = new ArrayList<>();
        for (WalletTransaction tx : pending) {
            Map<String, Object> map = new HashMap<>();
            map.put("id", tx.getId());
            map.put("userId", tx.getUserId());
            map.put("amount", tx.getAmount());
            // Tách signature từ description nếu có
            String sig = "";
            if (tx.getDescription() != null && tx.getDescription().contains("signature:")) {
                sig = tx.getDescription().split("signature:")[1];
            }
            map.put("signature", sig);
            map.put("createdAt", tx.getCreatedAt());
            result.add(map);
        }
        return ResponseEntity.ok(result);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<?> handleException(Exception ex) {
        return ResponseEntity.badRequest().body(
            ex.getMessage() != null ? ex.getMessage() : "Đã xảy ra lỗi không xác định"
        );
    }
}