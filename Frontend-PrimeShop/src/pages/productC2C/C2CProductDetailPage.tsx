// Vị trí: src/pages/C2CProductDetailPage.tsx
import React, { useState, useEffect } from 'react';
import { useParams, Link } from 'react-router-dom'; // Dùng useParams để lấy ID
import { C2CProduct } from '../../mocks/mockDataC2C';
import { fetchC2CProductById } from '../../mocks/mockDataC2C'; // Import API
import './styles/c2c-product-detail-page.css'; // File CSS "tỉ mỉ"
import { FaMapMarkerAlt, FaToolbox, FaShieldAlt, FaComments } from 'react-icons/fa';
import { Button } from '@mui/material';

// Hàm helper (tái sử dụng từ C2CTab)
const formatCurrency = (amount: number): string => {
  return amount.toLocaleString('vi-VN', {
    style: 'currency',
    currency: 'VND'
  });
};

const renderCondition = (condition: C2CProduct['condition']) => {
  switch (condition) {
    case 'new': return 'Mới 100% (chưa qua sử dụng)';
    case 'like_new': return 'Như mới 99% (dùng lướt)';
    case 'used': return 'Đã qua sử dụng (hoạt động tốt)';
    case 'for_parts': return 'Bán linh kiện / Hỏng (cho thợ)';
    default: return 'Không rõ';
  }
};

const C2CProductDetailPage: React.FC = () => {
  // 1. Lấy ID từ URL
  const { id } = useParams<{ id: string }>(); 
  
  // 2. States
  const [product, setProduct] = useState<C2CProduct | null>(null);
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  // State "tỉ mỉ" cho gallery ảnh
  const [selectedImage, setSelectedImage] = useState<string>('');

  // 3. Fetch Data
  useEffect(() => {
    if (!id) {
      setError('Không tìm thấy ID sản phẩm.');
      setIsLoading(false);
      return;
    }

    const loadData = async () => {
      try {
        setIsLoading(true);
        const fetchedProduct = await fetchC2CProductById(id);
        
        if (fetchedProduct) {
          setProduct(fetchedProduct);
          // Set ảnh đầu tiên làm ảnh chính
          setSelectedImage(fetchedProduct.images[0]); 
        } else {
          setError('Không tìm thấy tin đăng này (có thể đã bị xóa).');
        }
      } catch (err) {
        setError('Lỗi khi tải dữ liệu.');
      } finally {
        setIsLoading(false);
      }
    };

    loadData();
  }, [id]); // Chạy lại mỗi khi ID trên URL thay đổi

  // 4. Render States
  if (isLoading) {
    return <div className="c2c-detail-loading">Đang tải chi tiết...</div>;
  }

  if (error) {
    return <div className="c2c-detail-error">{error}</div>;
  }

  if (!product) {
    return <div className="c2c-detail-error">Không có dữ liệu sản phẩm.</div>;
  }

  // 5. Render "Pixel-perfect" Layout
  return (
    <section className="c2c-detail-container">
      {/* (A) Layout 2 Cột Chính */}
      <div className="c2c-detail-main">
        
        {/* === CỘT TRÁI: GALLERY ẢNH (Quan trọng) === */}
        <div className="c2c-gallery">
          <div className="c2c-gallery-main-image">
            <img src={selectedImage || 'https://via.placeholder.com/600'} alt={product.name} />
          </div>
          <div className="c2c-gallery-thumbnails">
            {product.images.map((imgUrl, index) => (
              <img
                key={index}
                src={imgUrl}
                alt={`thumbnail ${index + 1}`}
                className={selectedImage === imgUrl ? 'active' : ''}
                onClick={() => setSelectedImage(imgUrl)}
                onMouseOver={() => setSelectedImage(imgUrl)} // UX "mlem"
              />
            ))}
          </div>
        </div>

        {/* === CỘT PHẢI: THÔNG TIN (Chốt đơn) === */}
        <div className="c2c-info">
          {/* Tên & Brand */}
          <span className="c2c-info-brand">{product.brand || 'Không có thương hiệu'}</span>
          <h1 className="c2c-info-name">{product.name}</h1>
          
          {/* Giá */}
          <div className="c2c-info-price">
            {formatCurrency(product.price)}
          </div>
          
          {/* Trạng thái (Đã bán?) */}
          {product.status === 'sold' && (
            <div className="c2c-info-sold-badge">ĐÃ BÁN</div>
          )}

          {/* "Trái tim" C2C: Tình trạng & Vị trí */}
          <div className="c2c-info-meta">
            <div className="meta-item">
              <FaToolbox />
              <strong>Tình trạng:</strong> {renderCondition(product.condition)}
            </div>
            <div className="meta-item">
              <FaMapMarkerAlt />
              <strong>Vị trí:</strong> {product.location}
            </div>
          </div>
          
          {/* Nút Call-to-Action (CTA) */}
          <div className="c2c-info-actions">
            <Button 
              variant="contained" 
              color="primary"
              size="large"
              startIcon={<FaComments />}
              disabled={product.status === 'sold'}
              onClick={() => alert(`Sếp ơi! Mở khung chat với Seller ID: ${product.sellerId}`)}
            >
              Chat với người bán
            </Button>
            <Button 
              variant="outlined" 
              color="secondary"
              size="large"
              disabled={product.status === 'sold'}
            >
              Thêm vào giỏ (nếu có)
            </Button>
          </div>
          
          {/* Box "Tin tưởng" */}
          <div className="c2c-info-trust-box">
            <FaShieldAlt />
            <div className="trust-box-content">
              <strong>Giao dịch an toàn</strong>
              <p>Chỉ thanh toán khi đã gặp mặt và kiểm tra hàng. Không đặt cọc trước!</p>
            </div>
          </div>
        </div>
      </div>

      {/* (B) Khu vực Mô tả chi tiết */}
      <div className="c2c-detail-description">
        <h2>Mô tả chi tiết</h2>
        {/* Dùng pre-wrap để giữ nguyên các dấu xuống dòng của người bán */}
        <p style={{ whiteSpace: 'pre-wrap' }}>
          {product.description}
        </p>
      </div>

      {/* (C) Khu vực Thông tin Người bán (Mock) */}
      <div className="c2c-detail-seller">
        <h2>Thông tin người bán</h2>
        <div className="seller-info-card">
          <img src="https://cdn.pixabay.com/photo/2015/10/05/22/37/blank-profile-picture-973460_1280.png" alt="avatar" />
          <div className="seller-info-details">
            <strong>ID Người bán: {product.sellerId}</strong>
            <span>Đã tham gia 2 tháng trước</span>
            {/* Tương lai: Lấy từ 'o.seller_profiles' */}
            <span>Tỉ lệ phản hồi: 95%</span> 
          </div>
          <Button variant="outlined">Xem cửa hàng</Button>
        </div>
      </div>
    </section>
  );
};

export default C2CProductDetailPage;