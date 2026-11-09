// Vị trí: src/components/C2CFeaturedSection.tsx
import React, { useState, useEffect } from 'react';
import { Link } from 'react-router-dom';
import { C2CProductResponse } from '../../api/c2c.api';
import { getC2CProducts } from '../../api/c2c.api';
import C2CProductCard from './productC2C-card'; // Card "anh em"
import '../../assets/css/c2c-featured-section.css'; // File CSS "tỉ mỉ"
import { Button } from '@mui/material';
import { FaArrowRight } from 'react-icons/fa';

const C2CFeaturedSection: React.FC = () => {
  // State lưu 4 sản phẩm "hot"
  const [featuredProducts, setFeaturedProducts] = useState<C2CProductResponse[]>([]);
  const [isLoading, setIsLoading] = useState(true);

  useEffect(() => {
    const loadData = async () => {
      try {
        setIsLoading(true);
        const allProducts = await getC2CProducts();
        setFeaturedProducts(allProducts.slice(0, 4));

      } catch (error) {
        console.error("Error fetching featured C2C products:", error);
      } finally {
        setIsLoading(false);
      }
    };
    
    loadData();
  }, []); // Chạy 1 lần

  // Nếu đang load hoặc không có sản phẩm nào, không render gì cả
  if (isLoading || featuredProducts.length === 0) {
    // Không render gì cả (hoặc một cái skeleton)
    return null; 
  }

  return (
    <section className="c2c-featured-section">
      {/* 1. Header của Section */}
      <div className="c2c-section-header">
        <h2>Tin đăng C2C nổi bật</h2>
        <Button
          component={Link} // "Pixel-perfect" React Router
          to="/c2c"
          variant="outlined"
          color="primary"
          endIcon={<FaArrowRight />}
        >
          Xem tất cả
        </Button>
      </div>
      
      {/* 2. Grid 4 sản phẩm "mlem" */}
      <div className="c2c-featured-grid">
        {featuredProducts.map(product => (
          <C2CProductCard key={product.id} product={product} />
        ))}
      </div>
    </section>
  );
};

export default C2CFeaturedSection;