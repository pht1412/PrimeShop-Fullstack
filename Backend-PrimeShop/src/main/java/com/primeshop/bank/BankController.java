package com.primeshop.bank;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;


@RestController
@RequestMapping("/api/bank")
@CrossOrigin(origins = "http://localhost:5173")
public class BankController {

    @Autowired
    private BankRepository repository;

   @PostMapping("/link")
    public ResponseEntity<?> linkBank(@RequestBody Map<String, String> body) {
        Long userId = Long.valueOf(body.get("userId"));
        String accountNumber = body.get("accountNumber");
        String cardHolderName = body.get("cardHolderName"); // thêm

        // Tạo token giả
        String fakeToken = UUID.randomUUID().toString();

        // Mã hóa (cần implement EncryptionUtil)
        byte[] encAccount = EncryptionUtil.encrypt(accountNumber);
        byte[] encToken = EncryptionUtil.encrypt(fakeToken);

        // Tạo entity Bank
        Bank bank = new Bank();
        bank.setUserId(userId);
        bank.setAccountNumber(encAccount);
        bank.setToken(encToken);
        bank.setCardHolderName(cardHolderName); // set tên chủ thẻ
        bank.setStatus("linked");

        repository.save(bank);

        return ResponseEntity.ok(Map.of("token", fakeToken));
    }

    @PostMapping("/confirm")
    public ResponseEntity<?> confirmBank(@RequestBody Map<String, String> body) {
        String token = body.get("token");
        byte[] encToken = EncryptionUtil.encrypt(token);

        Bank bank = repository.findByToken(encToken)
                .orElseThrow(() -> new RuntimeException("Invalid token"));

        bank.setStatus("confirmed");
        repository.save(bank);

        return ResponseEntity.ok(Map.of("message", "Bank link confirmed"));
    }

    @GetMapping("/list/{userId}")
    public ResponseEntity<List<Map<String, Object>>> getBankLinks(@PathVariable Long userId) {
        List<Bank> banks = repository.findAllByUserId(userId);

        List<Map<String, Object>> result = banks.stream().map(bank -> {
            Map<String, Object> map = new HashMap<>();
            map.put("id", bank.getId());
            map.put("account_number", EncryptionUtil.decrypt(bank.getAccountNumber()));
            map.put("bank_name", bank.getBankName());
            map.put("card_holder_name", bank.getCardHolderName()); // thêm
            map.put("created_at", bank.getCreatedAt());
            map.put("status", bank.getStatus());
            return map; 
        }).toList();

        return ResponseEntity.ok(result);
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<?> deleteBank(@PathVariable Long id) {
        // Kiểm tra xem thẻ có tồn tại không
        Bank bank = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Bank not found with id " + id));

        repository.delete(bank);

        return ResponseEntity.ok(Map.of("message", "Bank card deleted successfully"));
    }

}
