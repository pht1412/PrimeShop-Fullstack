import React, { useEffect, useState } from "react";
import { Link, useNavigate } from "react-router-dom";
import { 
  FaPhone, FaStore, FaShoppingCart, FaUser, FaBars, 
  FaSearch, FaSignOutAlt 
} from "react-icons/fa";
import "../assets/css/header.css";
import logo from "../assets/images/P.png";
import { useAuth } from "../context/AuthContext";
import Swal from "sweetalert2";


const Header = () => {
  const [isMenuOpen, setIsMenuOpen] = useState(false);
  const [isScrolled, setIsScrolled] = useState(false); 
  const { user, logout } = useAuth();
  const [searchTerm, setSearchTerm] = useState(""); 
  const [cartItemCount, setCartItemCount] = useState(0); 
  const navigate = useNavigate();

  // Logic phát hiện cuộn chuột
  useEffect(() => {
    const handleScroll = () => {
      // Cuộn quá 50px thì đổi màu header
      setIsScrolled(window.scrollY > 50);
    };
    window.addEventListener("scroll", handleScroll);
    return () => window.removeEventListener("scroll", handleScroll);
  }, []);

  const handleSearch = () => {
    if (searchTerm.trim()) {
      setIsMenuOpen(false);
      navigate(`/all-products?search=${encodeURIComponent(searchTerm.trim())}`);
    }
  };

  const handleKeyDown = (e: React.KeyboardEvent<HTMLInputElement>) => {
    if (e.key === 'Enter') handleSearch();
  };

  const isAdmin = user && (user.role === 'ADMIN' || user.username === 'admin');

  return (
    // Class 'scrolled' sẽ tự động thêm vào khi cuộn
    <header className={`header ${isScrolled ? 'scrolled' : ''}`}>
      
      {/* Top Bar - Thông tin liên hệ */}
      <section className="top-bar">
        <div className="welcome-text">👋 Chào mừng đến với PrimeShop - Nơi công nghệ thăng hoa!</div>
        <div className="top-right">
          <button className="btn"><FaPhone/> 1900 9099</button>
          <button className="btn"><FaStore/> Hệ thống cửa hàng</button>
        </div>
      </section>

      {/* Main Header */}
      <section className="main-header">
        {/* Hamburger Mobile */}
        <button className="hamburger" onClick={() => setIsMenuOpen(!isMenuOpen)}>
          <FaBars />
        </button>

        {/* Logo */}
        <Link to="/home" className="logo-link">
          <img src={logo} alt="PrimeShop" className="logo-image" />
          {/* Logic đổi màu chữ Logo: Mặc định trắng, Cuộn xuống thì xanh */}
          <span>PRIME<span style={{color: isScrolled ? '#2563EB' : '#FFD700'}}>SHOP</span></span>
        </Link>

        {/* Search */}
        <div className="search-box">
          <div className="search-wrapper">
            <FaSearch className="search-icon" onClick={handleSearch}/>
            <input 
              type="text" 
              placeholder="Bạn muốn tìm gì hôm nay? (iPhone 15, Laptop...)" 
              value={searchTerm}
              onChange={(e) => setSearchTerm(e.target.value)}
              onKeyDown={handleKeyDown}
            />
          </div>
        </div>

        {/* Actions */}
        <div className="user-actions">
          {!user ? (
            <Link to="/login" className="action-btn">
              <FaUser className="icon"/>
              <span>Đăng nhập</span>
            </Link>
          ) : (
            <>
              <Link to="/account" className="action-btn">
                <FaUser className="icon"/>
                <span>{user.username}</span>
              </Link>
              <button onClick={() => {
                  Swal.fire({
                    title: 'Đăng xuất?', 
                    text: 'Bạn có chắc muốn đăng xuất không?',
                    icon: 'warning', 
                    showCancelButton: true, 
                    confirmButtonText: 'Đăng xuất',
                    cancelButtonText: 'Hủy',
                    confirmButtonColor: '#d33'
                  }).then((res) => res.isConfirmed && logout());
                }} className="action-btn">
                <FaSignOutAlt className="icon"/>
                <span>Thoát</span>
              </button>
            </>
          )}

          <Link to="/cart" className="action-btn">
            <div style={{position: 'relative'}}>
               <FaShoppingCart className="icon"/>
               {/* Badge số lượng (Giả lập số 2 cho đẹp nếu chưa có logic cart) */}
               <span className="cart-badge">{cartItemCount > 0 ? cartItemCount : 0}</span>
            </div>
            <span>Giỏ hàng</span>
          </Link>
        </div>
      </section>

      {/* Navigation - Thanh menu */}
      <nav className="navigation-container">
        <div className="nav-box">
          <Link to="/home" className="nav-item">Trang Chủ</Link>
          <Link to="/about" className="nav-item">Giới thiệu </Link>
          <Link to="/all-products" className="nav-item">Sản Phẩm</Link>
          <Link to="/news" className="nav-item">Tin Công Nghệ</Link>
          <Link to="/faq" className="nav-item">Hỏi Đáp</Link>
          <Link to="/minigame-list" className="nav-item">Mini Game</Link>
          {isAdmin && <Link to="/delivery-dashboard" className="nav-item" style={{color: isScrolled ? '#DC2626' : '#FFA500'}}>Quản Lý Đơn</Link>}
        </div>
      </nav>

      {/* Mobile Menu */}
      {/* <div className={`mobile-menu ${isMenuOpen ? "open" : ""}`}>
        <div className="mobile-menu-content">
           <Link to="/home" className="mobile-menu-item" onClick={()=>setIsMenuOpen(false)}>Trang Chủ</Link>
           <Link to="/all-products" className="mobile-menu-item" onClick={()=>setIsMenuOpen(false)}>Sản Phẩm</Link>
           <Link to="/news" className="mobile-menu-item" onClick={()=>setIsMenuOpen(false)}>Tin Tức</Link>
           <Link to="/cart" className="mobile-menu-item" onClick={()=>setIsMenuOpen(false)}>Giỏ Hàng</Link>
        </div>
      </div> */}
    </header>
  );
};

export default Header;