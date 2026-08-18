// src/api/walletApi.ts
import api from './api';
import { AxiosResponse } from 'axios';

// --- Định nghĩa kiểu dữ liệu (Ánh xạ 1:1 từ DTOs Java) ---

export interface IWalletBalance {
    balance: number;
    points: number;
    walletActive: boolean; // <-- Sửa từ 'active' thành 'walletActive'
}

export interface IQrResponse {
    qrCode: string; // <-- Sửa từ 'qrCodeUrl'
    amount: number; // <-- Bổ sung trường 'amount'
    signature: string; // <-- Bổsung trường 'signature' (Xóa 'expiresAt')
}

export interface ITransaction {
    id: number;
    type: string;
    amount: number;
    description: string;
    createdAt: string; // (Date từ Java -> string trong JSON)
}

// DTO cho các hàm POST
export interface IPaymentRequest {
    orderId: number;
    amount: number;
}

// Dùng cho API Admin /pending-deposits
export interface IPendingDeposit {
    id: number;
    userId: number;
    amount: number;
    signature: string;
    createdAt: string;
}

// --- Hết phần định nghĩa kiểu ---


/* * ============================================
 * CÁC API DÀNH CHO USER THÔNG THƯỜNG
 * ============================================
 */

// Lấy số dư và trạng thái ví
export const getBalance = (userId: number): Promise<AxiosResponse<IWalletBalance>> => {
    return api.get('/wallet/balance', {
        params: { userId }
    });
};

// Kích hoạt ví
export const activateWallet = (userId: number): Promise<AxiosResponse<string>> => {
    return api.post('/wallet/activate', null, { 
        params: { userId }
    });
};

// Lấy lịch sử giao dịch
export const getTransactions = (userId: number): Promise<AxiosResponse<ITransaction[]>> => {
    return api.get('/wallet/transactions', {
        params: { userId }
    });
};

// Sinh mã QR nạp tiền
export const generateDepositQr = (userId: number, amount: number): Promise<AxiosResponse<IQrResponse>> => {
    // Body là DepositRequest.java { amount: Double }
    const depositRequest = { amount }; 
    return api.post('/wallet/deposit/qr', depositRequest, {
        params: { userId }
    });
};

// Thanh toán đơn hàng (Thiếu trong file của bạn)
export const payOrder = (userId: number, paymentData: IPaymentRequest): Promise<AxiosResponse<string>> => {
    // Body là PaymentRequest.java { orderId: Long, amount: Double }
    return api.post('/wallet/pay', paymentData, {
        params: { userId }
    });
};


/* * ============================================
 * CÁC API DÀNH CHO ADMIN QUẢN LÝ
 * ============================================
 */

// Admin: Lấy các khoản nạp đang chờ (Thiếu trong file của bạn)
export const getPendingDeposits = (): Promise<AxiosResponse<IPendingDeposit[]>> => {
    return api.get('/wallet/pending-deposits');
};

// Admin: Xác nhận nạp tiền (Thiếu trong file của bạn)
export const confirmDeposit = (userId: number, amount: number, signature: string): Promise<AxiosResponse<string>> => {
    return api.post('/wallet/deposit/confirm', null, {
        params: {
            userId,
            amount,
            signature
        }
    });
};