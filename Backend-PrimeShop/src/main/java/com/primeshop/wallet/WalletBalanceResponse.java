package com.primeshop.wallet;

public class WalletBalanceResponse {
    private double balance;
    private double points;
    private boolean walletActive;

    public WalletBalanceResponse(double balance, double points, boolean walletActive) {
        this.balance = balance;
        this.points = points;
        this.walletActive = walletActive;
    }

    public double getBalance() {
        return balance;
    }

    public void setBalance(double balance) {
        this.balance = balance;
    }

    public double getPoints() {
        return points;
    }

    public void setPoints(double points) {
        this.points = points;
    }

    public boolean isWalletActive() {
        return walletActive;
    }

    public void setWalletActive(boolean walletActive) {
        this.walletActive = walletActive;
    }
}