package com.primeshop.wallet;

import java.util.List;

public interface WalletService {
    WalletBalanceResponse getBalance(Long userId);
    void payOrder(Long userId, PaymentRequest request);
    QrResponse generateDepositQr(Long userId, DepositRequest request);
    List<TransactionResponse> getTransactionHistory(Long userId);
    void updateBalanceAfterDeposit(Long userId, Double amount);
    void activateWallet(Long userId);
    void confirmDeposit(Long userId, Double amount, String signature);
}