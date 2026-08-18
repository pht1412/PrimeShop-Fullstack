// Vị trí: src/pages/C2CProductPage.tsx
import React, { useState, useEffect } from 'react';
import { C2CProduct } from '../../mocks/mockDataC2C';
import { fetchAllC2CProducts } from '../../mocks/mockDataC2C'; // Import hàm API mới
import C2CProductCard from '../../components/productC2C-card/productC2C-card'; // Import card C2C
import './styles/c2c-product-page.css'; // Import file CSS mới

const C2CProductPage: React.FC = () => {
  // State để lưu danh sách sản phẩm
  const [products, setProducts] = useState<C2CProduct[]>([]);
  // State cho UX/UI "tỉ mỉ"
  const [isLoading, setIsLoading] = useState(true);

  // useEffect để fetch data khi component được mount
  useEffect(() => {
    const loadData = async () => {
      try {
        setIsLoading(true);
        // Call API mock (chỉ lấy các sản phẩm 'active')
        const activeProducts = await fetchAllC2CProducts();
        setProducts(activeProducts);
      } catch (error) {
        console.error("Error fetching all C2C products:", error);
        // Tương lai: hiển thị toast lỗi
      } finally {
        setIsLoading(false);
      }
    };

    loadData();
  }, []); // Chạy 1 lần duy nhất

  // Hàm render "xịn" cho UX
  const renderContent = () => {
    // 1. Trạng thái Loading
    if (isLoading) {
      return <div className="c2c-loading-state">Đang tải gian hàng...</div>;
    }

    // 2. Trạng thái Rỗng
    if (products.length === 0) {
      return <div className="c2c-empty-list-state">Chưa có tin đăng nào được hiển thị.</div>;
    }

    // 3. Render Grid "mlem"
    return (
      <div className="c2c-product-grid">
        {products.map((product) => (
          <C2CProductCard key={product.id} product={product} />
        ))}
      </div>
    );
  };

  return (
    <section className="c2c-page-container">
      <div className="c2c-page-header">
        <h1>Gian hàng C2C</h1>
        <p>Khám phá các mặt hàng "pass" lại từ cộng đồng PrimeShop.</p>
      </div>
      
      {/* Render nội dung (Loading / Rỗng / Grid) */}
      {renderContent()}
    </section>
  );
};

export default C2CProductPage;