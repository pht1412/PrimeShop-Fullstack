import React, { useState, useEffect, useCallback, useRef } from 'react';
import * as walletApi from '../api/wallet.api';
import { IWalletBalance, ITransaction, IQrResponse } from '../api/wallet.api';
import Swal from 'sweetalert2';
import { Button } from '@mui/material';
import { User } from '../types/user';

interface WalletTabProps {
  user: User | null;
}

// Hàm làm sạch nội dung mô tả, cắt bỏ phần signature
  const cleanDescription = (description: string) => {
    if (!description) return '';
    // Tách chuỗi dựa vào dấu gạch đứng '|' hoặc từ khóa 'signature'
    // Lấy phần đầu tiên và xóa khoảng trắng thừa
    return description.split('|signature:')[0].split('|')[0].trim();
  };

const formatCurrency = (amount: number | null | undefined): string => {
  if (amount === undefined || amount === null) return '0 ₫';
  return amount.toLocaleString('vi-VN', {
    style: 'currency',
    currency: 'VND'
  });
};

const WalletTab: React.FC<WalletTabProps> = ({ user }) => {
  const [balance, setBalance] = useState<number>(0);
  const [points, setPoints] = useState<number>(0);
  const [isActive, setIsActive] = useState<boolean>(false);
  const [isLoading, setIsLoading] = useState<boolean>(true);
  const [transactions, setTransactions] = useState<ITransaction[]>([]);
  const [txLoading, setTxLoading] = useState<boolean>(false);
  const [showTxHistory, setShowTxHistory] = useState<boolean>(false);

  // State polling để tự động refresh khi có tiền (giữ lại để trải nghiệm mượt hơn)
  const [isPolling, setIsPolling] = useState<boolean>(false);
  const prevBalanceRef = useRef<number>(0);

  const fetchWalletData = useCallback(async (userId: number) => {
    try {
      const response = await walletApi.getBalance(userId);
      const data: IWalletBalance = response.data;

      if (data.walletActive) {
        setBalance(prev => {
            prevBalanceRef.current = prev;
            return data.balance;
        });
        setPoints(data.points || 0);
        setIsActive(true);
      } else {
        setIsActive(false);
      }
    } catch (error) {
      console.error("Error fetching balance:", error);
      setIsActive(false);
    } finally {
      setIsLoading(false);
    }
  }, []);

  // Polling ngầm: Nếu Admin duyệt nhanh thì tự cập nhật số dư luôn
  useEffect(() => {
    let intervalId: any;
    if (isPolling && user) {
        intervalId = setInterval(async () => {
            await fetchWalletData(Number(user.id));
        }, 5000); // 5 giây check 1 lần cho đỡ lag
    }
    return () => clearInterval(intervalId);
  }, [isPolling, user, fetchWalletData]);

  // Nếu số dư tăng thì báo thành công (dành cho trường hợp duyệt tự động)
  useEffect(() => {
      if (isPolling && balance > prevBalanceRef.current && prevBalanceRef.current !== 0) {
          setIsPolling(false);
          Swal.close();
          Swal.fire({
              icon: 'success',
              title: 'Tiền đã về ví!',
              text: `Số dư mới: ${formatCurrency(balance)}`,
              timer: 3000
          });
      }
  }, [balance, isPolling]);

  useEffect(() => {
    if (user && user.id) {
      fetchWalletData(Number(user.id));
    } else {
      setIsLoading(true);
    }
  }, [user, fetchWalletData]);

  const handleActivate = async () => {
    if (!user) return;
    setIsLoading(true);
    try {
      await walletApi.activateWallet(Number(user.id));
      await Swal.fire('Thành công!', 'Ví Prime của bạn đã được kích hoạt.', 'success');
      fetchWalletData(Number(user.id));
    } catch (error) {
      Swal.fire('Lỗi', 'Không thể kích hoạt ví. Vui lòng thử lại.', 'error');
      setIsLoading(false);
    }
  };

  // --- 1. HÀM HIỂN THỊ QR CODE (SỬ DỤNG MẠNH API BÊN THỨ 3) ---
  const showQrPopup = (qrData: string) => {
    // Logic tạo ảnh QR "Bất tử":
    // 1. Nếu là URL ảnh (http...) -> Dùng luôn.
    // 2. Nếu là Base64 (dài ngoằng) -> Dùng data URI.
    // 3. Nếu là chuỗi text (ngắn, ví dụ nội dung chuyển khoản) -> Dùng API qrserver để tạo ảnh.
    
    let imageSrc = '';
    if (qrData.startsWith('http')) {
        imageSrc = qrData;
    } else if (qrData.length > 500) { 
        // Giả định base64 sẽ rất dài
        imageSrc = `data:image/png;base64,${qrData}`;
    } else {
        // Fallback mạnh mẽ: Dùng API tạo QR từ text
        imageSrc = `https://api.qrserver.com/v1/create-qr-code/?size=250x250&data=${encodeURIComponent(qrData)}`;
    }

    setIsPolling(true); // Bật chế độ lắng nghe tiền về

    Swal.fire({
      title: 'Quét mã QR để nạp tiền',
      html: `
        <div style="display: flex; justify-content: center; margin-bottom: 15px;"> 
          <img src="${imageSrc}" alt="Mã QR nạp tiền" style="width: 250px; height: 250px; object-fit: contain; border: 1px solid #ddd; border-radius: 8px;" />
        </div>
        <p style="color: #555; font-size: 0.95rem;">Mở App Ngân hàng / Momo để quét mã.</p>
        <p style="color: #888; font-size: 0.85rem; margin-top: 5px;">Hệ thống sẽ tự động cập nhật sau khi nhận được tiền.</p>
      `,
      showConfirmButton: true,
      confirmButtonText: '✅ Tôi đã chuyển tiền', // Nút xác nhận thủ công
      confirmButtonColor: '#2563eb',
      showCloseButton: true,
      allowOutsideClick: false
    }).then((result) => {
        if (result.isConfirmed) {
            // --- 2. POPUP THÔNG BÁO CHỜ DUYỆT (THEO YÊU CẦU) ---
            Swal.fire({
                icon: 'info',
                title: 'Đang xử lý giao dịch',
                text: 'Vui lòng đợi ít phút để quản trị viên kiểm tra và duyệt lệnh nạp tiền của bạn.',
                confirmButtonText: 'Đã hiểu',
                confirmButtonColor: '#3085d6'
            });
            // Vẫn giữ polling chạy ngầm để khi Admin duyệt xong thì số dư tự nhảy
        } else {
            setIsPolling(false); // Nếu tắt popup mà ko bấm xác nhận thì dừng check
        }
    });
  };

  const handleDeposit = async () => {
    if (!user) return;
    
    // --- 3. CHO PHÉP NHẬP SỐ LẺ (BỎ STEP) ---
    const { value: amountStr } = await Swal.fire<string>({
      title: 'Nạp tiền vào Ví Prime',
      input: 'number',
      inputLabel: 'Nhập số tiền muốn nạp (VNĐ)',
      inputPlaceholder: 'Ví dụ: 251104',
      // Bỏ thuộc tính 'step' để nhập số lẻ thoải mái
      inputAttributes: { min: '1000' }, 
      showCancelButton: true,
      confirmButtonText: 'Tạo mã QR',
      cancelButtonText: 'Hủy',
      confirmButtonColor: '#2563eb',
      inputValidator: (value) => {
        if (!value || parseInt(value, 10) < 1000) {
            return 'Số tiền nạp tối thiểu là 1.000đ';
        }
        return null;
      }
    });

    if (amountStr) {
      try {
        Swal.fire({ title: 'Đang tạo mã QR...', didOpen: () => Swal.showLoading() });
        const amount = parseInt(amountStr, 10);
        
        // Gọi API backend
        const response = await walletApi.generateDepositQr(Number(user.id), amount);
        const qrData: IQrResponse = response.data;
        
        // Kiểm tra dữ liệu trả về
        if (qrData && qrData.qrCode) {
            showQrPopup(qrData.qrCode);
        } else {
            // Fallback nếu API không trả về qrCode nhưng thành công (hiếm gặp)
            // Tự tạo QR từ nội dung mặc định nếu cần
            showQrPopup(`NAP TIEN ${user.username} ${amount}`); 
        }
      } catch (error) {
        console.error("Error generating QR:", error);
        Swal.fire('Lỗi', 'Không thể tạo mã QR lúc này. Vui lòng thử lại sau.', 'error');
      }
    }
  };

  const handleToggleTxHistory = async () => {
    if (!user) return;
    if (showTxHistory) {
      setShowTxHistory(false);
      return;
    }
    setShowTxHistory(true);
    
    if (transactions.length === 0) {
      setTxLoading(true);
      try {
        const response = await walletApi.getTransactions(Number(user.id));
        setTransactions(response.data.sort((a: any, b: any) => new Date(b.createdAt).getTime() - new Date(a.createdAt).getTime()));
      } catch (error) {
        Swal.fire('Lỗi', 'Không thể tải lịch sử giao dịch.', 'error');
        setShowTxHistory(false);
      } finally {
        setTxLoading(false);
      }
    }
  };

  const formatTxDate = (dateString: string) => {
    return new Date(dateString).toLocaleString('vi-VN');
  };

  if (isLoading) return <div style={{padding: 20, textAlign: 'center'}}>Đang tải dữ liệu Ví...</div>;

  if (!isActive) {
    return (
      <div style={{textAlign: 'center', padding: '40px', background: '#f8f9fa', borderRadius: '8px'}}>
        <h3 style={{marginBottom: '15px'}}>Kích hoạt Ví Prime</h3>
        <p style={{marginBottom: '20px', color: '#666'}}>Kích hoạt ngay để thanh toán siêu tốc và nhận hoàn tiền!</p>
        <Button variant="contained" color="primary" onClick={handleActivate}>
          Kích hoạt ngay
        </Button>
      </div>
    );
  }

  return (
    <div className="wallet-dashboard" style={{ marginTop: '20px' }}>
      <div className="wallet-card" style={{ 
          background: 'linear-gradient(135deg, #1e3a8a 0%, #3b82f6 100%)', 
          color: 'white', 
          padding: '30px', 
          borderRadius: '16px',
          boxShadow: '0 10px 20px -5px rgba(30, 64, 175, 0.3)',
          display: 'flex',
          justifyContent: 'space-between',
          alignItems: 'center',
          marginBottom: '30px'
      }}>
        <div className="balance-details">
          <div style={{ fontSize: '0.9rem', opacity: 0.9, marginBottom: '5px' }}>Số dư khả dụng</div>
          <div style={{ fontSize: '2.5rem', fontWeight: 'bold', marginBottom: '10px' }}>{formatCurrency(balance)}</div>
          <div style={{ background: 'rgba(255,255,255,0.2)', padding: '4px 12px', borderRadius: '20px', display: 'inline-block', fontSize: '0.85rem' }}>
            💎 {points.toLocaleString('vi-VN')} điểm tích lũy
          </div>
        </div>
        <div className="wallet-card-actions" style={{ display: 'flex', flexDirection: 'column', gap: '10px' }}>
          <Button variant="contained" style={{ background: 'white', color: '#1e3a8a', fontWeight: 'bold' }} onClick={handleDeposit}>
            Nạp tiền
          </Button>
          <Button variant="outlined" style={{ borderColor: 'white', color: 'white' }} onClick={handleToggleTxHistory}>
            {showTxHistory ? 'Ẩn lịch sử' : 'Lịch sử GD'}
          </Button>
        </div>
      </div>

      {showTxHistory && (
        <div className="transaction-history-container" style={{ animation: 'fadeIn 0.3s' }}>
          <h4 style={{ marginBottom: '15px', color: '#333' }}>Lịch sử giao dịch</h4>
          {txLoading ? (
            <div>Đang tải...</div>
          ) : transactions.length > 0 ? (
            <ul style={{ listStyle: 'none', padding: 0 }}>
              {transactions.map(tx => (
                <li key={tx.id} style={{ 
                    padding: '15px', 
                    borderBottom: '1px solid #eee', 
                    display: 'flex', 
                    justifyContent: 'space-between', 
                    alignItems: 'center',
                    background: '#fff'
                }}>
                  <div>
                  <div style={{ fontWeight: 500, color: '#333', marginBottom: '4px' }}>
                      {cleanDescription(tx.description)}
                  </div>                    
                  <div style={{ fontSize: '0.8rem', color: '#888' }}>{formatTxDate(tx.createdAt)}</div>
                  </div>
                  <div style={{ 
                      fontWeight: 'bold', 
                      color: tx.type.includes('DEPOSIT') ? '#10b981' : '#ef4444' 
                  }}>
                    {tx.type.includes('DEPOSIT') ? '+' : '-'} {formatCurrency(tx.amount)}
                  </div>
                </li>
              ))}
            </ul>
          ) : (
            <p style={{ color: '#666', fontStyle: 'italic' }}>Chưa có giao dịch nào.</p>
          )}
        </div>
      )}
    </div>
  );
}
export default WalletTab;