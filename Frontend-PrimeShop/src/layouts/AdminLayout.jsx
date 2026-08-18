import React, { useState } from 'react';
import { NavLink } from 'react-router-dom';
import PropTypes from 'prop-types';
// import '../assets/css/admin.css';
import '../pages/admin/admin.css'

const AdminLayout = ({ children }) => {
  const [isSidebarOpen, setIsSidebarOpen] = useState(false);

  const toggleSidebar = () => {
    setIsSidebarOpen(!isSidebarOpen);
  };

  return (
    <div className="admin-layout">
      <aside className={`admin-sidebar ${isSidebarOpen ? 'open' : ''}`} aria-label="Admin navigation">
        <div className="sidebar-header">
          <h2>PrimeShop Admin</h2>
        </div>
        <nav className="sidebar-nav">
          <ul>
            <li>
              <NavLink to="/admin/dashboard" className={({ isActive }) => (isActive ? 'active' : '')}>
                Tổng quan
              </NavLink>
            </li>
            <li>
              <NavLink to="/admin/orders" className={({ isActive }) => (isActive ? 'active' : '')}>
                Đơn hàng
              </NavLink>
            </li>
            <li>
              <NavLink to="/admin/products" className={({ isActive }) => (isActive ? 'active' : '')}>
                Sản phẩm
              </NavLink>
            </li>
            <li>
              <NavLink to="/admin/users" className={({ isActive }) => (isActive ? 'active' : '')}>
                Người dùng
              </NavLink>
            </li>
            <li>
              <NavLink to="/admin/news" className={({ isActive }) => (isActive ? 'active' : '')}>
                Tin tức
              </NavLink>
            </li>
            <li>
              <NavLink to="/admin/categories" className={({ isActive }) => (isActive ? 'active' : '')}>
                Danh mục
              </NavLink>
              </li>
              <li>
                <NavLink to="/admin/vouchers" className={({ isActive }) => (isActive ? 'active' : '')}>
                  Voucher
                </NavLink>
              </li>
              <li>
                <NavLink to="/admin/wallets" className={({ isActive }) => (isActive ? 'active' : '')}>
                  Ví tiền
                </NavLink>
              </li>
              <li>
                <NavLink to="/admin/c2c-management" className={({ isActive }) => (isActive ? 'active' : '')}>
                  Quản lý đăng bán
                </NavLink>
              </li>
              <li>
              <NavLink to="/admin/revenues" className={({ isActive }) => (isActive ? 'active' : '')}>
                Doanh thu
              </NavLink>
            </li>
            <li>
              <NavLink to="/admin/import-transactions" className={({ isActive }) => (isActive ? 'active' : '')}>
                Nhập hàng
              </NavLink>
            </li>
            <li>
              <NavLink to="/admin/business-manager" className={({ isActive }) => (isActive ? 'active' : '')}>
                Quản lý tài khoản đăng bán
              </NavLink>
            </li>

          </ul>
        </nav>
      </aside>
      
      <div className="admin-content">
        <button
          className="toggle-btn"
          onClick={toggleSidebar}
          aria-label={isSidebarOpen ? 'Đóng menu' : 'Mở menu'}
        >
          {isSidebarOpen ? '✕' : '☰'}
        </button>
        <main>{children}</main>
      </div>
    </div>
  );
};

AdminLayout.propTypes = {
  children: PropTypes.node.isRequired,
};

export default AdminLayout;