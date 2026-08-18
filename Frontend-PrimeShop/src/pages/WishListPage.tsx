import React, { useState } from 'react';
import { Link } from 'react-router-dom';
import { FaShoppingCart, FaTrash } from 'react-icons/fa';
import "../assets/css/wishlist.css"; // Import CSS styles for the component

const WishList = () => {
  // Dữ liệu mẫu cho danh sách yêu thích
  // [Kết nối BE]: Gọi API để lấy danh sách yêu thích (GET /api/user/wishlist)
  const [wishlistItems, setWishlistItems] = useState([
    {
      id: 'prod123',
      name: 'iPhone 14 Pro',
      price: 25000000,
      image: 'https://via.placeholder.com/150',
    },
    {
      id: 'prod124',
      name: 'Samsung Galaxy S23',
      price: 20000000,
      image: 'https://via.placeholder.com/150',
    },
    {
      id: 'prod125',
      name: 'MacBook Air M2',
      price: 30000000,
      image: 'https://via.placeholder.com/150',
    },
  ]);

  // Hàm xóa sản phẩm khỏi danh sách yêu thích
  const handleRemoveFromWishlist = (id) => {
    // [Kết nối BE]: Gọi API để xóa sản phẩm khỏi danh sách yêu thích (DELETE /api/user/wishlist/:id)
    setWishlistItems(wishlistItems.filter((item) => item.id !== id));
    console.log(`Xóa sản phẩm ${id} khỏi danh sách yêu thích`);
  };

  // Hàm thêm sản phẩm vào giỏ hàng
  const handleAddToCart = (id) => {
    // [Kết nối BE]: Gọi API để thêm sản phẩm vào giỏ hàng (POST /api/user/cart)
    console.log(`Thêm sản phẩm ${id} vào giỏ hàng`);
  };

  return (
    <section className="wishlist-page">
      <div className="wishlist-container">
        {/* Tiêu đề */}
        <h2 className="wishlist-title">
          Danh sách yêu thích ({wishlistItems.length})
        </h2>

        {/* Danh sách sản phẩm yêu thích */}
        {wishlistItems.length > 0 ? (
          <div className="wishlist-items">
            {wishlistItems.map((item) => (
              <div key={item.id} className="wishlist-item shadow rounded">
                <img
                  src={item.image}
                  alt={item.name}
                  className="wishlist-item-image"
                />
                <div className="wishlist-item-details">
                  <h3 className="wishlist-item-name">{item.name}</h3>
                  <p className="wishlist-item-price">
                    {item.price.toLocaleString('vi-VN', {
                      style: 'currency',
                      currency: 'VND',
                    })}
                  </p>
                </div>
                <div className="wishlist-item-actions">
                  <button
                    className="btn btn-primary add-to-cart-btn"
                    onClick={() => handleAddToCart(item.id)}
                  >
                    <FaShoppingCart className="icon" /> Thêm vào giỏ hàng
                  </button>
                  <button
                    className="btn btn-secondary remove-btn"
                    onClick={() => handleRemoveFromWishlist(item.id)}
                  >
                    <FaTrash className="icon" /> Xóa
                  </button>
                </div>
              </div>
            ))}
          </div>
        ) : (
          <div className="empty-wishlist">
            <p className="empty-message">
              Danh sách yêu thích của bạn đang trống.
            </p>
            <Link to="/products" className="btn btn-primary">
              Tiếp tục mua sắm
            </Link>
          </div>
        )}
      </div>
    </section>
  );
};

export default WishList;