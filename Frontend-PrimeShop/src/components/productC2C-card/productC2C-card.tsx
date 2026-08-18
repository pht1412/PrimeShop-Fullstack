// Vị trí: src/components/C2CProductCard.tsx
import React from 'react';
import { Link } from 'react-router-dom';
// BƯỚC 1: Loại bỏ Mock, Import type "xịn"
import { IProductCardResponse, ProductStatus } from '../../types/seller'; 
// BƯỚC 2: Import file CSS "Shopee" mới
import styles from './styles/ProductCard.module.css'; 
import { FaMapMarkerAlt, FaToolbox } from 'react-icons/fa';

// Định nghĩa Props: Nhận type "xịn"
interface C2CProductCardProps {
  product: IProductCardResponse;
}

// === Các hàm Helpers "tỉ mỉ" ===

// Hàm format tiền (giữ nguyên)
const formatCurrency = (amount: number): string => {
  return amount.toLocaleString('vi-VN', {
    style: 'currency',
    currency: 'VND'
  });
};

// BƯỚC 3: Xử lý type "condition" (có thể là undefined)
type C2CCondition = 'new' | 'like_new' | 'used' | 'for_parts';

const renderCondition = (condition?: C2CCondition) => {
  switch (condition) {
    case 'new': return 'Mới 100%';
    case 'like_new': return 'Như mới 99%';
    case 'used': return 'Đã qua sử dụng';
    case 'for_parts': return 'Bán linh kiện';
    default: return 'Không rõ'; // Xử lý khi 'condition' là undefined
  }
};

// --- Component Chính ---

const C2CProductCard: React.FC<C2CProductCardProps> = ({ product }) => {
  
  // BƯỚC 4: Dùng trường "xịn" từ IProductCardResponse
  const { slug, name, price, imageUrl, brand, location, condition, status } = product;

  // BƯỚC 5: Cập nhật logic "Đã bán/Ẩn"
  // API thật không có 'sold', ta dùng 'DISABLED' hoặc 'REJECTED'
  const isSoldOrHidden = status === 'DISABLED' || status === 'REJECTED';

  /*
    LƯU Ý QUAN TRỌNG CHO SẾP (VỀ BỐ CỤC GRID "KHÍT"):
    Để các card này "khít" nhau thành 1 hàng (như ảnh Shopee),
    sếp cần bọc chúng trong một <div> cha ở trang danh sách
    và dùng CSS Grid:
    
    .product-list-container {
      display: grid;
      grid-template-columns: repeat(5, 1fr); // (Hiện 5 card 1 hàng)
      gap: 10px; // (Khoảng hở 10px)
    }
  */

  return (
    // BƯỚC 6: Dùng class CSS "xịn"
    <div className={`${styles.productCard} ${isSoldOrHidden ? styles.c2cSold : ''}`}>
      
      {/* BƯỚC 7: Sửa Link trỏ về "slug" (tốt cho SEO) */}
      <Link to={`/c2c-product-detail/${slug}`} className={styles.productLink}>
        
        {/* Hình ảnh sản phẩm */}
        <div className={styles.imageWrapper}>
          <img 
            // BƯỚC 8: Dùng "imageUrl" (API thật)
            src={imageUrl || 'https://via.placeholder.com/300'} 
            alt={name} 
            className={styles.productImage} 
          />
          {/* BỔ SUNG: Overlay "ĐÃ BÁN / ẨN" */}
          {isSoldOrHidden && <div className={styles.soldOverlay}>ĐÃ BÁN/ẨN</div>}
        </div>

        {/* Thông tin sản phẩm */}
        <div className={styles.productInfo}>
          
          {/* Tên sản phẩm */}
          <Link to={`/c2c-product-detail/${slug}`} className={styles.productName}>
            {name}
          </Link>
          
          {/* Giá (Đơn giản hóa) */}
          <div className={styles.priceInfo}>
            <span className={styles.currentPrice}>
              {formatCurrency(price)}
            </span>
          </div>

          {/* Thông tin C2C (Tình trạng & Vị trí) */}
          <div className={styles.c2cMetaInfo}>
            <span className={styles.c2cCondition}>
              <FaToolbox /> {renderCondition(condition as C2CCondition)}
            </span>
            <span className={styles.c2cLocation}>
              <FaMapMarkerAlt /> {location || 'Toàn quốc'}
            </span>
          </div>
          
        </div>
      </Link>
    </div>
  );
};

export default C2CProductCard;