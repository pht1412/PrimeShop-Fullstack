// Vị trí: src/pages/C2CProductPage.tsx
import React, { useState, useEffect } from 'react';
import { C2CProductResponse, getC2CProducts } from '../../api/c2c.api';
import C2CProductCard from '../../components/productC2C-card/productC2C-card'; // Import card C2C
import './styles/c2c-product-page.css'; // Import file CSS mới

const C2CProductPage: React.FC = () => {
  // State để lưu danh sách sản phẩm
  const [products, setProducts] = useState<C2CProductResponse[]>([]);
  // State cho UX/UI "tỉ mỉ"
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  // useEffect để fetch data khi component được mount
  useEffect(() => {
    const loadData = async () => {
      try {
        setIsLoading(true);
        setProducts(await getC2CProducts());
      } catch (error) {
        const status = (error as { response?: { status?: number } }).response?.status;
        console.error("Fetching C2C products failed with status:", status);
        setError("Không thể tải sản phẩm từ người dùng khác. Vui lòng thử lại.");
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

    if (error) {
      return <div className="c2c-empty-list-state">{error}</div>;
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
        <h1>Sản phẩm từ người dùng khác</h1>
        <p>Khám phá sản phẩm do cộng đồng PrimeShop đăng bán.</p>
      </div>
      
      {/* Render nội dung (Loading / Rỗng / Grid) */}
      {renderContent()}
    </section>
  );
};

export default C2CProductPage;