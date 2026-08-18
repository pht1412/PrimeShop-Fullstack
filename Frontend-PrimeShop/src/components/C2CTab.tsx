import React, { useState, useEffect, useCallback } from 'react';
import { Button } from '@mui/material';
import { FaPlus, FaEdit } from 'react-icons/fa';
import '../assets/css/c2c-tab.css';
import Swal from 'sweetalert2';
import { toast } from 'react-toastify';
import C2CProductFormModal from './C2CProductFormModal';
import SellerApplyModal from './SellerApplyModal'; // ✅ NEW
import { User } from '../types/user';
import * as sellerApi from '../api/seller.api';
import { ISellerProfile, IProductCardResponse, IProductRequest, ProductStatus, ISellerRequest } from '../types/seller';

interface C2CTabProps {
  user: User | null;
}

// --- [HELPER MỚI] ĐÀO DỮ LIỆU TRONG SPECS ---
// Hàm này giúp tìm kiếm giá trị trong mảng specs nếu field chính bị thiếu
const getSpecValue = (product: any, keyName: string): string | null => {
  // 1. Nếu có field trực tiếp thì dùng luôn
  if (product[keyName]) return product[keyName];

  // 2. Nếu không, đi lục trong mảng specs
  if (product.specs && Array.isArray(product.specs)) {
    // Tìm spec có name trùng với keyName (không phân biệt hoa thường)
    const found = product.specs.find((s: any) => 
      s.name?.toLowerCase() === keyName.toLowerCase() || 
      // Mapping tiếng Việt/Anh phòng hờ
      (keyName === 'condition' && s.name === 'Tình trạng') ||
      (keyName === 'location' && s.name === 'Vị trí')
    );
    return found ? found.value : null;
  }
  return null;
};
// ----------------------------------------------

const renderProductStatus = (status: ProductStatus | string | undefined) => {
  const normalizedStatus = status ? String(status).toUpperCase() : '';
  switch (normalizedStatus) {
    case 'APPROVED': 
    case 'VERIFIED': return { text: 'Đang hiển thị', className: 'status-active' };
    case 'PENDING': 
    case 'PENDING_REVIEW': return { text: 'Chờ duyệt', className: 'status-pending' };
    case 'REJECTED': return { text: 'Bị từ chối', className: 'status-sold' };
    case 'DISABLED': 
    case 'HIDDEN': return { text: 'Đang ẩn', className: 'status-hidden' };
    default: return { text: `Khác (${status || 'Null'})`, className: 'status-hidden' };
  }
};

const renderCondition = (condition: any) => {
  // Chuẩn hóa input đầu vào để switch case bắt dính
  const val = condition ? String(condition).toLowerCase() : '';
  switch (val) {
    case 'new': return 'Mới 100%';
    case 'like_new': return 'Như mới 99%';
    case 'used': return 'Đã qua sử dụng';
    case 'for_parts': return 'Bán linh kiện';
    default: return 'Không rõ'; // Nếu vẫn không khớp thì chịu
  }
};

const formatCurrency = (amount: number): string => {
  if (amount === undefined || amount === null) return '0 ₫';
  return amount.toLocaleString('vi-VN', { style: 'currency', currency: 'VND' });
};

const C2CTab: React.FC<C2CTabProps> = ({ user }) => {
  const [isLoading, setIsLoading] = useState(true);
  const [isSubmitting, setIsSubmitting] = useState(false);
  const [sellerProfile, setSellerProfile] = useState<ISellerProfile | null>(null);
  const [products, setProducts] = useState<IProductCardResponse[]>([]);
  const [isModalOpen, setIsModalOpen] = useState(false);
  const [editingProduct, setEditingProduct] = useState<any | null>(null);
  const [isApplyModalOpen, setIsApplyModalOpen] = useState(false); // ✅ NEW

  const fetchSellerStatus = useCallback(async () => {
    if (!user) {
      setSellerProfile(null);
      setIsLoading(false);
      return;
    }
    setIsLoading(true);
    try {
      const response = await sellerApi.getMyBusinessProfile();
      setSellerProfile(response.data);
    } catch (error: any) {
      if (error.response?.status === 404) setSellerProfile(null);
      else {
        // Log chi tiết để dễ debug và hiển thị message cụ thể nếu có
        console.error('Lỗi tải Business Profile:', error);
        toast.error(error.response?.data?.message || error.message || 'Lỗi tải Business Profile.');
      }
    } finally {
      setIsLoading(false);
    }
  }, [user]);

  const fetchSellerProducts = useCallback(async (currentSellerId: number) => {
    try {
      const response = await sellerApi.getSellerProducts(currentSellerId, 0, 50); 
      let allProducts: IProductCardResponse[] = [];
      if (response.data && Array.isArray(response.data.content)) {
          allProducts = response.data.content;
      } else if (Array.isArray(response.data)) {
          allProducts = response.data;
      }
      setProducts(allProducts);
    } catch (err: any) {
      console.error('Lỗi tải sản phẩm:', err);
    }
  }, []);

  useEffect(() => { fetchSellerStatus(); }, [fetchSellerStatus]);
  
  useEffect(() => {
    if (sellerProfile && sellerProfile.status === 'VERIFIED_SELLER' && sellerProfile.id) {
      fetchSellerProducts(sellerProfile.id);
    }
  }, [sellerProfile, fetchSellerProducts]);
  
  useEffect(() => {
    if (sellerProfile?.status === 'PENDING_REVIEW') {
      const interval = setInterval(fetchSellerStatus, 10000);
      return () => clearInterval(interval);
    }
  }, [sellerProfile, fetchSellerStatus]);

  // === [FIXED] HÀM ĐĂNG KÝ SELLER (cập nhật để dùng SellerApplyModal) ===
  const handleApply = () => {
    if (!user) {
      toast.error('Vui lòng đăng nhập trước.');
      return;
    }
    setIsApplyModalOpen(true);
  };

  const handleApplySubmit = async (formData: {
    shopName: string;
    phone: string;
    identityCard: string;
    description: string;
    address: string;
  }) => {
    setIsSubmitting(true);
    try {
      const applyRequest: ISellerRequest = {
        shopName: formData.shopName,
        phone: formData.phone,
        identityCard: formData.identityCard,
        description: formData.description,
        address: formData.address,
      };

      await sellerApi.applyForSeller(applyRequest);

      toast.success('Đơn đăng ký của bạn đã được gửi. Vui lòng chờ admin duyệt.');
      await fetchSellerStatus();
    } catch (err: any) {
      console.error('Lỗi gửi đơn:', err);
      toast.error(err.response?.data?.message || 'Lỗi gửi đơn đăng ký.');
      throw err; // re-throw để component biết xử lý error
    } finally {
      setIsSubmitting(false);
    }
  };

  // === [FIXED] HÀM XỬ LÝ LƯU (CREATE/UPDATE) ===
  const handleModalSave = async (formData: any) => {
    if (!sellerProfile) return;
    
    // Chuẩn bị payload gửi về Backend
    // Backend cần nhận thông tin trong 'specs' list
    const requestData: IProductRequest = {
      name: formData.name,
      description: formData.description,
      price: formData.price,
      brand: formData.brand || null,
      imageUrl: formData.images[0] || '', // Lấy ảnh đầu tiên làm ảnh đại diện
      stock: Number(formData.stock || 1),
      categoryId: Number(formData.category_id || '1'),
      
      // [QUAN TRỌNG] Map dữ liệu từ Form vào cấu trúc Specs mà Backend cần
      specs: [
        { name: 'Tình trạng', value: formData.condition }, // Backend lưu cái này
        { name: 'Vị trí', value: formData.location }       // Backend lưu cái này
      ]
    };

    try {
      setIsLoading(true);
      if (editingProduct) {
        await sellerApi.updateProduct(editingProduct.id, requestData);
        toast.success('Cập nhật thành công!');
      } else {
        await sellerApi.addProduct(requestData, sellerProfile.id); 
        await Swal.fire('Thành công!', 'Đã đăng tin mới.', 'success');
      }
      setIsModalOpen(false);
      if (sellerProfile?.id) fetchSellerProducts(sellerProfile.id);
      
    } catch (err: any) {
      console.error('Lỗi lưu SP:', err);
      toast.error(err.response?.data?.message || 'Lỗi hệ thống.');
    } finally {
      setIsLoading(false);
    }
  };

  // === [FIXED] HÀM MỞ FORM SỬA (LẤY DỮ LIỆU TỪ SPECS RA) ===
  const handleEdit = (product: IProductCardResponse) => {
    // 1. Dùng hàm helper để móc dữ liệu từ specs ra
    const realCondition = getSpecValue(product, 'condition') || 'used'; 
    const realLocation = getSpecValue(product, 'location') || '';

    // 2. Fill vào form data
    const fullProductData = {
      ...product,
      description: product.description || '',
      category_id: String(product.categoryId || '1'), 
      images: [product.imageUrl],
      stock: Number(product.stock || 1),
      
      // Gán giá trị thực tế đã tìm được
      condition: realCondition, 
      location: realLocation
    };
    
    setEditingProduct(fullProductData);
    setIsModalOpen(true);
  };
  
  const handlePostNew = () => { setEditingProduct(null); setIsModalOpen(true); };
  const handleToggleHide = () => Swal.fire('Thông báo', 'Tính năng đang phát triển', 'info');
  const handleDelete = () => Swal.fire('Thông báo', 'Tính năng đang phát triển', 'info');

  if (isLoading) return <div className="wallet-loading">Đang tải dữ liệu...</div>;

  // ===== CASE 1: Chưa là Seller =====
  if (!sellerProfile) {
    return (
      <>
        <div className="activate-wallet-container">
          <h3>Bạn chưa là Seller</h3>
          <p>Đăng ký để bán hàng trên nền tảng</p>
          <button 
            onClick={handleApply}
            className="px-4 py-2 bg-blue-600 text-white rounded hover:bg-blue-700"
          >
            📝 Đăng ký Business
          </button>
        </div>

        {/* Modal form */}
        <SellerApplyModal
          open={isApplyModalOpen}
          onClose={() => setIsApplyModalOpen(false)}
          onSubmit={handleApplySubmit}
          isSubmitting={isSubmitting}
        />
      </>
    );
  }

  // ===== CASE 2: Đang chờ duyệt =====
  if (sellerProfile.status === 'PENDING_REVIEW') {
    return (
      <div className="activate-wallet-container">
        <h3>⏳ Đơn của bạn đang chờ duyệt</h3>
        <p>Admin sẽ kiểm tra và phê duyệt trong vòng 24-48 giờ</p>
        <p style={{ fontSize: '12px', color: '#999' }}>Shop: {sellerProfile.shopName}</p>
      </div>
    );
  }

  // ===== CASE 3: Bị từ chối =====
  if (sellerProfile.status === 'REJECTED') {
    return (
      <>
        <div className="activate-wallet-container">
          <h3>❌ Đơn của bạn bị từ chối</h3>
          <p>Lý do: {sellerProfile.description || 'Không rõ'}</p>
          <button 
            onClick={handleApply}
            className="px-4 py-2 bg-blue-600 text-white rounded hover:bg-blue-700"
          >
            Gửi lại đơn
          </button>
        </div>

        <SellerApplyModal
          open={isApplyModalOpen}
          onClose={() => setIsApplyModalOpen(false)}
          onSubmit={handleApplySubmit}
          isSubmitting={isSubmitting}
        />
      </>
    );
  }

  // ===== CASE 4: Đã duyệt - Hiển thị gian hàng =====
  if (sellerProfile.status === 'VERIFIED_SELLER') {
    return (
      <div className="c2c-tab-container">
        <div className="c2c-header">
          <h3 className="mb-3">Gian hàng: {sellerProfile.shopName}</h3>
          <button 
            onClick={handlePostNew}
            className="px-4 py-2 bg-blue-600 text-white rounded hover:bg-blue-700 flex items-center gap-2"
          >
            <FaPlus /> Đăng tin mới
          </button>
        </div>

        <div className="c2c-product-list">
          {products.length === 0 ? (
            <div className="c2c-empty-state"><p>Chưa có tin đăng nào</p></div>
          ) : (
            products.map(product => {
              const statusInfo = renderProductStatus(product.status);
              const displayCondition = getSpecValue(product, 'condition');

              return (
                <div key={product.id} className="c2c-product-item shadow-sm">
                  <img src={product.imageUrl || 'https://via.placeholder.com/150'} alt={product.name} className="c2c-product-image" />
                  <div className="c2c-product-info">
                    <h5 className="product-name">{product.name}</h5>
                    <p className="product-price">{formatCurrency(product.price)}</p>
                    <p className="product-meta">
                      <span>{renderCondition(displayCondition)}</span> | <span>{product.category}</span>
                    </p>
                    <span className={`product-status-badge ${statusInfo.className}`}>{statusInfo.text}</span>
                  </div>
                  <div className="c2c-product-actions">
                    <button onClick={() => handleEdit(product)} className="btn-outlined">✏️ Sửa</button>
                    <button onClick={handleToggleHide} className="btn-outlined">👁️ Ẩn</button>
                    <button onClick={handleDelete} className="btn-danger">🗑️ Xóa</button>
                  </div>
                </div>
              );
            })
          )}
        </div>

        <C2CProductFormModal
          isOpen={isModalOpen}
          onClose={() => setIsModalOpen(false)}
          productToEdit={editingProduct}
          onSave={handleModalSave}
        />
      </div>
    );
  }

  // Default: status không xác định
  return <div className="activate-wallet-container">Trạng thái không xác định. Vui lòng liên hệ support.</div>;
};

export default C2CTab;