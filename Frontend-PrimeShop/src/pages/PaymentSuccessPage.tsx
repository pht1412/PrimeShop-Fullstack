// src/pages/PaymentSuccessPage.tsx
import React, { useEffect, useState } from 'react';
import { useSearchParams, useNavigate } from 'react-router-dom';
import { 
  CheckCircleFilled, 
  ShoppingOutlined, 
  HomeOutlined, 
  CopyOutlined, 
  CheckOutlined,
  FileTextOutlined
} from '@ant-design/icons';

const PaymentSuccessPage: React.FC = () => {
  const [searchParams] = useSearchParams();
  const navigate = useNavigate();
  const orderId = searchParams.get('orderId');

  const [loading, setLoading] = useState(true);
  const [isValidOrder, setIsValidOrder] = useState(false);
  const [isCopied, setIsCopied] = useState(false);

  // --- 1. Logic API (Giữ nguyên từ code cũ của bạn) ---
  useEffect(() => {
    if (!orderId) {
      setLoading(false);
      return;
    }

    const verifyOrder = async () => {
      try {
        // Giả lập call API check đơn hàng
        setTimeout(() => {
          setIsValidOrder(true);
          setLoading(false);
        }, 1200); // Delay chút để hiện hiệu ứng loading đẹp
      } catch (error) {
        console.error("Lỗi xác thực đơn hàng", error);
        setIsValidOrder(false);
        setLoading(false);
      }
    };
    verifyOrder();
  }, [orderId]);

  // --- 2. Xử lý Copy ---
  const handleCopyCode = () => {
    if (orderId) {
      navigator.clipboard.writeText(orderId);
      setIsCopied(true);
      setTimeout(() => setIsCopied(false), 2000);
    }
  };

  // --- 3. CSS Animation & Styles ---
  // Tôi thêm class .layout-center-fix để ép buộc căn giữa bất chấp layout cha
  const customStyles = `
    .layout-center-fix {
      display: flex;
      justify-content: center;
      align-items: center;
      width: 100%;
      min-height: 80vh; /* Chiếm tối thiểu 80% màn hình nhìn thấy */
    }
    
    @keyframes slideInUp {
      from { transform: translateY(30px); opacity: 0; }
      to { transform: translateY(0); opacity: 1; }
    }
    @keyframes pulse-green {
      0% { box-shadow: 0 0 0 0 rgba(16, 185, 129, 0.7); }
      70% { box-shadow: 0 0 0 15px rgba(16, 185, 129, 0); }
      100% { box-shadow: 0 0 0 0 rgba(16, 185, 129, 0); }
    }
    .animate-card-entry {
      animation: slideInUp 0.6s cubic-bezier(0.2, 0.8, 0.2, 1) forwards;
    }
    .icon-pulse {
      animation: pulse-green 2s infinite;
    }
    /* Tạo hiệu ứng răng cưa hóa đơn */
    .receipt-cut {
      position: relative;
      height: 20px;
      background: radial-gradient(circle, transparent 8px, #ffffff 9px);
      background-size: 24px 20px;
      background-position: -12px 0;
      transform: rotate(180deg);
      margin-top: -1px;
    }
  `;

  // --- RENDER: LOADING ---
  if (loading) {
    return (
      <div className="layout-center-fix bg-gray-50">
        <style>{customStyles}</style>
        <div className="flex flex-col items-center">
          <div className="w-16 h-16 border-4 border-emerald-100 border-t-emerald-500 rounded-full animate-spin"></div>
          <p className="mt-4 text-emerald-600 font-medium tracking-wide">Đang xác thực giao dịch...</p>
        </div>
      </div>
    );
  }

  // --- RENDER: ERROR ---
  if (!orderId || !isValidOrder) {
    return (
      <div className="layout-center-fix bg-gray-50 p-4">
        <style>{customStyles}</style>
        <div className="bg-white p-10 rounded-3xl shadow-xl max-w-md w-full text-center animate-card-entry">
          <div className="w-20 h-20 bg-red-50 rounded-full flex items-center justify-center mx-auto mb-6">
             <span className="text-red-500 text-4xl font-bold">✕</span> 
          </div>
          <h2 className="text-2xl font-bold text-gray-800 mb-2">Giao dịch thất bại</h2>
          <p className="text-gray-500 mb-8">Chúng tôi không tìm thấy thông tin thanh toán hoặc giao dịch bị từ chối.</p>
          <button 
            onClick={() => navigate('/')}
            className="w-full py-3.5 bg-gray-900 text-white rounded-xl hover:bg-black transition font-medium shadow-lg hover:shadow-xl"
          >
            Quay về trang chủ
          </button>
        </div>
      </div>
    );
  }

  // --- RENDER: SUCCESS (GIAO DIỆN MỚI) ---
  return (
    // Wrapper ngoài cùng: Sử dụng class 'layout-center-fix' tự định nghĩa ở trên để căn giữa
    <div className="layout-center-fix bg-slate-50 p-4">
      <style>{customStyles}</style>

      {/* CARD CHÍNH: Thiết kế dạng Hóa đơn */}
      <div className="w-full max-w-[480px] animate-card-entry">
        
        {/* Phần Header xanh lá */}
        <div className="bg-gradient-to-br from-emerald-500 to-teal-600 rounded-t-3xl p-8 text-center text-white relative overflow-hidden shadow-lg z-10">
          {/* Background decoration circles */}
          <div className="absolute top-[-20px] left-[-20px] w-24 h-24 bg-white opacity-10 rounded-full"></div>
          <div className="absolute bottom-[-10px] right-[-10px] w-20 h-20 bg-white opacity-10 rounded-full"></div>

          <div className="mb-4 relative z-10">
            <div className="bg-white text-emerald-500 w-20 h-20 rounded-full flex items-center justify-center mx-auto shadow-md icon-pulse">
              <CheckCircleFilled style={{ fontSize: '48px' }} />
            </div>
          </div>
          <h1 className="text-2xl font-bold mb-1 tracking-tight">Thanh toán thành công!</h1>
          <p className="text-emerald-100 text-sm">Cảm ơn bạn đã mua sắm tại PrimeShop</p>
        </div>

        {/* Phần Thân trắng (Body) */}
        <div className="bg-white px-8 pb-8 pt-6 rounded-b-3xl shadow-2xl relative">
          
          {/* Chi tiết đơn hàng */}
          <div className="text-center mb-8">
            <p className="text-gray-400 text-xs uppercase tracking-widest font-semibold mb-3">Hóa đơn điện tử</p>
            
            {/* Box chứa Mã đơn hàng */}
            <div className="bg-gray-50 border-2 border-dashed border-gray-200 rounded-xl p-4 flex flex-col items-center justify-center group hover:border-emerald-300 transition-colors cursor-default">
              <span className="text-xs text-gray-500 mb-1">Mã đơn hàng (Order ID)</span>
              <div className="flex items-center gap-2">
                <span className="text-xl font-mono font-bold text-gray-800 tracking-wide select-all">
                  {orderId}
                </span>
                <button 
                  onClick={handleCopyCode}
                  className="w-8 h-8 flex items-center justify-center rounded-full bg-white border border-gray-200 hover:border-emerald-500 hover:text-emerald-500 transition-all shadow-sm active:scale-90"
                  title="Copy mã"
                >
                  {isCopied ? <CheckOutlined /> : <CopyOutlined />}
                </button>
              </div>
              {isCopied && <span className="text-[10px] text-emerald-600 font-bold mt-1 animate-pulse">Đã sao chép!</span>}
            </div>
          </div>

          {/* Thông tin bổ sung (Grid 2 cột) */}
          <div className="grid grid-cols-2 gap-4 mb-8">
             <div className="bg-blue-50 p-3 rounded-lg text-center">
                <p className="text-blue-400 text-xs mb-1">Trạng thái</p>
                <p className="text-blue-700 font-bold text-sm">Đã thanh toán</p>
             </div>
             <div className="bg-purple-50 p-3 rounded-lg text-center">
                <p className="text-purple-400 text-xs mb-1">Thời gian</p>
                <p className="text-purple-700 font-bold text-sm">{new Date().toLocaleDateString('vi-VN')}</p>
             </div>
          </div>

          {/* Các nút bấm hành động */}
          <div className="space-y-3">
            <button 
              onClick={() => navigate('/all-products')}
              className="w-full flex items-center justify-center gap-3 bg-emerald-600 hover:bg-emerald-700 text-white font-bold py-4 rounded-xl shadow-lg shadow-emerald-200 transform transition hover:-translate-y-1 active:translate-y-0"
            >
              <ShoppingOutlined className="text-lg"/> Tiếp tục mua sắm
            </button>
            
            <div className="grid grid-cols-2 gap-3">
                
                <button 
                  onClick={() => navigate('/')} 
                  className="flex items-center justify-center gap-2 bg-white text-gray-700 font-medium py-3 border border-gray-200 rounded-xl hover:bg-gray-50 hover:border-gray-300 transition"
                >
                  <HomeOutlined /> Trang chủ
                </button>
            </div>
          </div>
          
          {/* Footer nhỏ */}
          <div className="mt-8 pt-4 border-t border-gray-100 text-center">
            <p className="text-xs text-gray-400">Cần hỗ trợ? Gọi <a href="tel:19009999" className="text-emerald-600 font-semibold hover:underline">1900 9999</a></p>
          </div>

        </div>
        {/* Phần trang trí răng cưa dưới đáy */}
        {/* <div className="receipt-cut w-full"></div> */} 
      </div>
    </div>
  );
};

export default PaymentSuccessPage;