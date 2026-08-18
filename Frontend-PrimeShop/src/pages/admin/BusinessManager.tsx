// Vị trí: src/pages/admin/BusinessManagement.tsx
import React, { useEffect, useState } from 'react';
import { ISellerProfile } from '../../types/seller'; // Import Type chuẩn
import { 
    adminGetPendingSellers, 
    adminApproveSeller, 
    adminBanSeller,
    adminGetAllSellers
} from '../../api/seller.api'; // Import API Service chuẩn

const BusinessManagement: React.FC = () => {
  // Sử dụng ISellerProfile thay vì interface tự định nghĩa
  const [sellers, setSellers] = useState<ISellerProfile[]>([]);
  const [loading, setLoading] = useState<boolean>(true);
  const [error, setError] = useState<string | null>(null);
  const [activeTab, setActiveTab] = useState<'PENDING' | 'ALL'>('PENDING');
  

  // Hàm Fetch dữ liệu được rút gọn tối đa
  const fetchSellers = async () => {
    setLoading(true);
    setError(null);
    try {
      let response;
      // Logic chọn API dựa trên Tab
      if (activeTab === 'PENDING') {
          // Tab Chờ duyệt: Gọi API cũ
          response = await adminGetPendingSellers();
      } else {
          // Tab Tất cả: Gọi API mới vừa viết
          response = await adminGetAllSellers(); 
      }
      
      // Axios trả về dữ liệu trong property .data
      setSellers(Array.isArray(response.data) ? response.data : []);
      
    } catch (err: any) {
      console.error(err);
      setError('Không thể tải dữ liệu. Vui lòng kiểm tra kết nối.');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchSellers();
  }, [activeTab]);

  // Xử lý Duyệt
  const handleApprove = async (sellerId: number) => {
    if (!window.confirm('Bạn có chắc chắn muốn duyệt cửa hàng này?')) return;

    try {
      await adminApproveSeller(sellerId);
      alert('Duyệt thành công!');
      fetchSellers(); // Refresh lại danh sách
    } catch (error) {
      console.error(error);
      alert('Có lỗi xảy ra khi duyệt.');
    }
  };

  // Xử lý Ban
  const handleBan = async (sellerId: number) => {
    const reason = window.prompt('Nhập lý do chặn (để lưu log - Optional):', 'Vi phạm chính sách');
    if (reason === null) return;

    try {
      await adminBanSeller(sellerId);
      alert('Đã chặn (Ban) cửa hàng thành công!');
      fetchSellers();
    } catch (error) {
      console.error(error);
      alert('Không thể chặn cửa hàng này.');
    }
  };

  // Helper Render Badge (Giữ nguyên logic hiển thị)
  const renderStatusBadge = (status: string) => {
    switch (status) {
      case 'PENDING_REVIEW':
        return <span className="px-3 py-1 text-xs font-semibold text-yellow-700 bg-yellow-100 rounded-full border border-yellow-200">Chờ duyệt</span>;
      case 'VERIFIED_SELLER':
        return <span className="px-3 py-1 text-xs font-semibold text-green-700 bg-green-100 rounded-full border border-green-200">Đã xác thực</span>;
      case 'BANNED_SELLER':
        return <span className="px-3 py-1 text-xs font-semibold text-red-700 bg-red-100 rounded-full border border-red-200">Đã chặn</span>;
      default:
        return <span className="px-3 py-1 text-xs text-gray-600 bg-gray-100 rounded-full">{status}</span>;
    }
  };

  return (
    <div className="p-6 bg-gray-50 min-h-screen font-sans">
      {/* Header */}
      <div className="flex justify-between items-center mb-8">
        <div>
          <h1 className="text-2xl font-bold text-gray-800">Quản lý Doanh Nghiệp (Seller)</h1>
          <p className="text-sm text-gray-500 mt-1">Duyệt đăng ký và kiểm soát chất lượng nhà bán hàng.</p>
        </div>
        <button 
            onClick={fetchSellers} 
            className="px-4 py-2 bg-white border border-gray-300 rounded-lg text-sm text-gray-700 hover:bg-gray-50 shadow-sm transition-all"
        >
            Làm mới dữ liệu
        </button>
      </div>

      {/* Tabs */}
      <div className="mb-6 border-b border-gray-200">
        <nav className="-mb-px flex space-x-8">
          <button
            onClick={() => setActiveTab('PENDING')}
            className={`whitespace-nowrap pb-4 px-1 border-b-2 font-medium text-sm transition-colors ${
              activeTab === 'PENDING' ? 'border-blue-600 text-blue-600' : 'border-transparent text-gray-500 hover:text-gray-700'
            }`}
          >
            Đơn chờ duyệt
          </button>
          <button
            onClick={() => setActiveTab('ALL')}
            className={`whitespace-nowrap pb-4 px-1 border-b-2 font-medium text-sm transition-colors ${
              activeTab === 'ALL' ? 'border-blue-600 text-blue-600' : 'border-transparent text-gray-500 hover:text-gray-700'
            }`}
          >
            Tất cả doanh nghiệp
          </button>
        </nav>
      </div>

      {/* Table */}
      <div className="bg-white rounded-xl shadow-sm border border-gray-200 overflow-hidden">
        {loading ? (
          <div className="p-8 text-center text-gray-500">Đang tải dữ liệu...</div>
        ) : error ? (
          <div className="p-8 text-center text-red-500">Lỗi: {error}</div>
        ) : sellers.length === 0 ? (
          <div className="p-12 text-center">
            <div className="text-gray-400 text-5xl mb-4">📭</div>
            <p className="text-gray-500 text-lg">Không có dữ liệu nào cần xử lý.</p>
          </div>
        ) : (
          <div className="overflow-x-auto">
            <table className="w-full text-left border-collapse">
              <thead>
                <tr className="bg-gray-50 text-gray-600 text-xs uppercase tracking-wider border-b border-gray-200">
                  <th className="px-6 py-4 font-semibold">ID</th>
                  <th className="px-6 py-4 font-semibold">Thông tin Shop</th>
                  <th className="px-6 py-4 font-semibold">Liên hệ / CCCD</th>
                  <th className="px-6 py-4 font-semibold">Mô tả</th>
                  <th className="px-6 py-4 font-semibold">Trạng thái</th>
                  <th className="px-6 py-4 font-semibold text-right">Hành động</th>
                </tr>
              </thead>
              <tbody className="divide-y divide-gray-100">
                {sellers.map((seller) => (
                  <tr key={seller.id} className="hover:bg-gray-50 transition-colors">
                    <td className="px-6 py-4 text-sm text-gray-500">#{seller.id}</td>
                    <td className="px-6 py-4">
                      <div className="font-medium text-gray-900">{seller.shopName}</div>
                      {/* Lấy thông tin user an toàn nhờ Optional Chaining (?.) */}
                      <div className="text-xs text-gray-500 mt-0.5">Owner: {seller.user?.username || 'N/A'}</div>
                    </td>
                    <td className="px-6 py-4">
                        <div className="text-sm text-gray-700">Phone: {seller.phone}</div>
                        <div className="text-xs text-gray-500">CCCD: {seller.identityCard}</div>
                    </td>
                    <td className="px-6 py-4 text-sm text-gray-600 max-w-xs truncate" title={seller.description}>
                      {seller.description}
                    </td>
                    <td className="px-6 py-4">
                      {renderStatusBadge(seller.status)}
                    </td>
                    <td className="px-6 py-4 text-right space-x-2">
                      {seller.status === 'PENDING_REVIEW' && (
                        <button
                          onClick={() => handleApprove(seller.id)}
                          className="inline-flex items-center px-3 py-1.5 bg-green-600 text-white text-xs font-medium rounded-md hover:bg-green-700 focus:outline-none transition-all shadow-sm"
                        >
                          ✓ Duyệt
                        </button>
                      )}
                      
                      {seller.status !== 'BANNED_SELLER' && (
                        <button
                          onClick={() => handleBan(seller.id)}
                          className="inline-flex items-center px-3 py-1.5 bg-white border border-red-200 text-red-600 text-xs font-medium rounded-md hover:bg-red-50 hover:border-red-300 focus:outline-none transition-all"
                        >
                          ✕ Chặn
                        </button>
                      )}
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}
      </div>
    </div>
  );
};

export default BusinessManagement;