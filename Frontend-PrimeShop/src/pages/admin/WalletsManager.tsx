// src/components/WalletManager.tsx
import React, { useState, useEffect } from 'react';
import * as walletApi from '../../api/wallet.api';
import { IPendingDeposit } from '../../api/wallet.api';
import Swal from 'sweetalert2';
import { Button } from '@mui/material';


// --- (Helper Functions - Tái sử dụng từ WalletTab) ---
const formatCurrency = (amount: number | null | undefined): string => {
 if (amount === undefined || amount === null) return '0 ₫';
 return amount.toLocaleString('vi-VN', {
  style: 'currency',
  currency: 'VND'
 });
};

const formatTxDate = (dateString: string) => {
  const date = new Date(dateString);
  return `${date.toLocaleDateString('vi-VN')} ${date.toLocaleTimeString('vi-VN')}`;
};
// --- (Hết Helper Functions) ---


// --- (Component chính) ---
const WalletsManager = () => { 
 // --- (State) ---
 const [pendingDeposits, setPendingDeposits] = useState<IPendingDeposit[]>([]);
 const [isLoading, setIsLoading] = useState<boolean>(true);
 const [error, setError] = useState<string | null>(null);

 // --- (Data Fetching) ---
 const fetchPendingDeposits = async () => {
  setIsLoading(true);
  setError(null);
  try {
   const response = await walletApi.getPendingDeposits();
   // Sắp xếp cho giao dịch mới nhất lên đầu
   const sortedData = response.data.sort((a, b) => 
    new Date(b.createdAt).getTime() - new Date(a.createdAt).getTime()
   );
   setPendingDeposits(sortedData);
  } catch (err) {
   console.error("Error fetching pending deposits:", err);
   setError("Không thể tải danh sách chờ. Vui lòng thử lại.");
   Swal.fire('Lỗi', 'Không thể tải danh sách chờ. Vui lòng thử lại.', 'error');
  } finally {
   setIsLoading(false);
  }
 };

 // Tải dữ liệu khi component mount
 useEffect(() => {
  fetchPendingDeposits();
 }, []);

 // --- (Event Handlers) ---
 const handleConfirmDeposit = async (deposit: IPendingDeposit) => {
  // 1. Hỏi xác nhận từ Admin
  const result = await Swal.fire({
   title: 'Xác nhận Nạp tiền?',
   text: `Bạn có chắc muốn cộng ${formatCurrency(deposit.amount)} cho User ID: ${deposit.userId}?`,
   icon: 'warning',
   showCancelButton: true,
   confirmButtonColor: '#28a745', // Màu xanh lá
   cancelButtonColor: '#dc3545',
   confirmButtonText: 'Xác nhận!',
   cancelButtonText: 'Hủy'
  });
  

  if (result.isConfirmed) {
   try {
    // 2. Gọi API
    await walletApi.confirmDeposit(deposit.userId, deposit.amount, deposit.signature);
    
    // 3. Thông báo thành công
    Swal.fire(
     'Thành công!',
     `Đã nạp ${formatCurrency(deposit.amount)} vào ví User ${deposit.userId}.`,
     'success'
    );

   // 4. Cập nhật UI: Xóa khoản đã xác nhận khỏi danh sách
    setPendingDeposits(prevDeposits => 
     prevDeposits.filter(d => d.id !== deposit.id)
    );

   } catch (err) {
     console.error("Error confirming deposit:", err);
     Swal.fire('Lỗi', 'Xác nhận thất bại. Vui lòng kiểm tra console.', 'error');
   }
 };

 // --- (Render) ---
 
 // 1. Trạng thái Loading
 if (isLoading) {
  return <div className="wallet-loading">Đang tải danh sách chờ xác nhận...</div>;
 }

 // 2. Trạng thái Lỗi
 if (error) {
  return <div className="wallet-loading" style={{ color: 'red' }}>{error}</div>;
 }

 // 3. Trạng thái Trống
 if (pendingDeposits.length === 0) {
  return <p className="no-transactions">Không có khoản nạp tiền nào đang chờ xác nhận.</p>;
 }
}

 // 4. Trạng thái có dữ liệu
 return (
  <div className="admin-wallet-manager">
   <h3>Quản lý Nạp tiền Chờ Xác nhận</h3>
   
   {/* Tái sử dụng container và list style từ WalletTab */}
   <div className="transaction-history-container">
    <ul className="transaction-list">
     
    {pendingDeposits.map(deposit => (
      <li key={deposit.id} className="transaction-item">
       
       {/* (Thông tin giao dịch - bên trái) */}
       <div className="tx-info">
        <span className="tx-description" style={{ fontWeight: 'bold' }}>
         User ID: {deposit.userId}
        </span>
        <span className="tx-date" style={{ fontSize: '0.9rem' }}>
         Ngày tạo: {formatTxDate(deposit.createdAt)}
        </span>
        <span className="tx-date" style={{ fontSize: '0.8rem', color: '#777' }}>
         Signature: {deposit.signature.substring(0, 20)}...
        </span>
       </div>
       
       {/* (Hành động - bên phải) */}
       <div className="admin-tx-action">
        <span className="tx-amount positive">
         + {formatCurrency(deposit.amount)}
        </span>
        <Button
          variant="contained"
          color="success"
          size="small"
          onClick={() => handleConfirmDeposit(deposit)}
          style={{ marginLeft: '20px' }}
        >
         Xác nhận
        </Button>
       </div>
      </li>
     ))}
    </ul>
   </div>
  </div>
 );
};

export default WalletsManager;