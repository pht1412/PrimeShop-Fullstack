// Vị trí: src/pages/C2CProductDetailPage.tsx
import React, { useState, useEffect } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import { C2CProductResponse, getC2CProduct } from '../../api/c2c.api';
import api from '../../api/api';
import { useAuth } from '../../context/AuthContext';
import './styles/c2c-product-detail-page.css'; // File CSS "tỉ mỉ"
import { FaMapMarkerAlt, FaToolbox, FaShieldAlt, FaComments } from 'react-icons/fa';
import { Button } from '@mui/material';
import Swal from 'sweetalert2';

const formatCurrency = (amount: number): string =>
  amount.toLocaleString('vi-VN', { style: 'currency', currency: 'VND' });

const C2CProductDetailPage: React.FC = () => {
  // 1. Lấy ID từ URL
  const { id } = useParams<{ id: string }>();
  const { user } = useAuth();
  const navigate = useNavigate();
  
  // 2. States
  const [product, setProduct] = useState<C2CProductResponse | null>(null);
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
        const fetchedProduct = await getC2CProduct(id);
        setProduct(fetchedProduct);
        setSelectedImage(fetchedProduct.images?.[0] || fetchedProduct.imageUrl || '');
      } catch {
        setError('Không tìm thấy tin đăng này hoặc tin đã bị ẩn.');
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

  const isOwner = product.owner;
  const addToCart = async () => {
    if (!user) {
      navigate('/login');
      return;
    }
    try {
      await api.post('/cart/add', { productSlug: product.slug, quantity: 1 });
      await Swal.fire('Đã thêm vào giỏ', 'Sản phẩm đã được thêm vào giỏ hàng.', 'success');
      navigate('/cart');
    } catch (error) {
      const response = (error as { response?: { data?: { message?: string } } }).response;
      await Swal.fire(
        'Không thể thêm vào giỏ',
        response?.data?.message || 'Vui lòng thử lại sau.',
        'error'
      );
    }
  };

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
            {(product.images || []).map((imgUrl, index) => (
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
          {/* "Trái tim" C2C: Tình trạng & Vị trí */}
          <div className="c2c-info-meta">
            <div className="meta-item">
              <FaToolbox />
              <strong>Tình trạng:</strong> {product.stock > 0 ? 'Còn hàng' : 'Hết hàng'}
            </div>
            <div className="meta-item">
              <FaMapMarkerAlt />
              <strong>Vị trí:</strong> {product.location || 'Người bán chưa cập nhật'}
            </div>
          </div>
          
          {/* Nút Call-to-Action (CTA) */}
          {isOwner ? (
            <p role="status">Đây là sản phẩm bạn đã đăng.</p>
          ) : (
            <div className="c2c-info-actions">
              <Button
                variant="contained"
                color="primary"
                size="large"
                startIcon={<FaComments />}
                onClick={() => alert(`Mở khung chat với Seller ID: ${product.sellerId}`)}
              >
                Chat với người bán
              </Button>
              <Button
                variant="outlined"
                color="secondary"
                size="large"
                disabled={!product.stock || product.stock < 1}
                onClick={addToCart}
              >
                Thêm vào giỏ hàng
              </Button>
            </div>
          )}
          
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
          {product.description || 'Người bán chưa thêm mô tả.'}
        </p>
      </div>

      {/* (C) Khu vực Thông tin Người bán (Mock) */}
      <div className="c2c-detail-seller">
        <h2>Thông tin người bán</h2>
        <div className="seller-info-card">
          <img src="https://cdn.pixabay.com/photo/2015/10/05/22/37/blank-profile-picture-973460_1280.png" alt="avatar" />
          <div className="seller-info-details">
            <strong>{product.shopName || 'Người bán PrimeShop'}</strong>
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