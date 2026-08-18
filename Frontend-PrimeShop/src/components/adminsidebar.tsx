import React from "react";
import { Link } from "react-router-dom";

const AdminSidebar = () => {
  return (
    <aside className="w-64 bg-gray-900 text-white p-4">
      <h2 className="text-xl font-bold mb-4">Admin Panel</h2>
      <nav className="space-y-2">
        <Link to="/admin/dashboard">Dashboard</Link>
        <Link to="/admin/products">Quản lý sản phẩm</Link>
        <Link to="/admin/orders">Quản lý đơn hàng</Link>
        <Link to="/admin/users">Người dùng</Link>
      </nav>
    </aside>
  );
};

export default AdminSidebar;
