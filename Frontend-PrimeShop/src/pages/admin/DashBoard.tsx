import React, { useEffect, useState } from "react";
import api from "../../api/api";
import "../../pages/admin/admin.css"
import { useNavigate } from "react-router-dom";
import { useAuth } from "../../context/AuthContext";

const DashBoard = () => {
  const [productCount, setProductCount] = useState<number | null>(null);
  const [orderCount, setOrderCount] = useState<number | null>(null);
  const [userCount, setUserCount] = useState<number | null>(null);
  const [newsCount, setNewsCount] = useState<number | null>(null);
  const [categoryCount, setCategoryCount] = useState<number | null>(null);
  const [voucherCount, setVoucherCount] = useState<number | null>(null);
  const navigate = useNavigate();
  const { user } = useAuth();

  const getTotalProductsCount = async () => {
    try {
      const response = await api.get('/product/count');
      setProductCount(response.data);
    } catch (error) {
      console.error('Error fetching product count:', error);
      setProductCount(0);
    }
  }
  
  const getTotalOrdersCount = async () => {
    try {
      const response = await api.get('/order/count');
      setOrderCount(response.data);
    } catch (error) {
      console.error('Error fetching order count:', error);
      setOrderCount(0);
    }
  }

  const getTotalUsersCount = async () => {
    try {
      const response = await api.get('/auth/count');
      setUserCount(response.data);
    } catch (error) {
      console.error('Error fetching user count:', error);
      setUserCount(0);
    }
  }
  
  const getTotalNewsCount = async () => {
    try {
      const response = await api.get('/news/count');
      setNewsCount(response.data.count);
    } catch (error) {
      console.error('Error fetching news count:', error);
      setNewsCount(0);
    }
  }
  
  const getTotalCategoriesCount = async () => {
    try {
      const response = await api.get('/category/count');
      setCategoryCount(response.data.count);
    } catch (error) {
      console.error('Error fetching category count:', error);
      setCategoryCount(0);
    }
  }
  
  const getTotalVouchersCount = async () => {
    try {
      const response = await api.get('/vouchers/count');
      setVoucherCount(response.data.count);
    } catch (error) {
      console.error('Error fetching voucher count:', error);
      setVoucherCount(0);
    }
  }

  useEffect(() => {
    getTotalProductsCount();
    getTotalOrdersCount();
    getTotalUsersCount();
    getTotalNewsCount();
    getTotalCategoriesCount();
    getTotalVouchersCount();
  }, []);
  
  return (
    <div className="admin-content">
      <h1>Tổng quan hệ thống</h1>
      <div className="grid grid-cols-1 md:grid-cols-5 gap-4">
        <div className="card" onClick={() => navigate('/admin/products')} style={{ cursor: 'pointer' }}>
          <span className="card-icon">📦</span>
          <div className="card-content">
            <p>{productCount ?? 0} sản phẩm</p>
            <p>Tổng sản phẩm trong kho</p>
          </div>
        </div>
        <div className="card" onClick={() => navigate('/admin/orders')} style={{ cursor: 'pointer' }}>
          <span className="card-icon">🛒</span>
          <div className="card-content">
            <p>{orderCount ?? 0} đơn hàng</p>
            <p>Tổng đơn hàng đã đặt</p>
          </div>
        </div>
        <div className="card" onClick={() => navigate('/admin/users')} style={{ cursor: 'pointer' }}>
          <span className="card-icon">👤</span>
          <div className="card-content">
            <p>{userCount ?? 0} người dùng</p>
            <p>Tổng số người dùng</p>
          </div>
        </div>
        <div className="card" onClick={() => navigate('/admin/news')} style={{ cursor: 'pointer' }}>
          <span className="card-icon">📰</span>
          <div className="card-content">
            <p>{newsCount ?? 0} tin tức</p>
            <p>Tổng số tin tức</p>
          </div>
        </div>
        <div className="card" onClick={() => navigate('/admin/categories')} style={{ cursor: 'pointer' }}>
          <span className="card-icon">📚</span>
          <div className="card-content">
            <p>{categoryCount ?? 0} danh mục</p>
            <p>Tổng số danh mục</p>
          </div>
        </div>
        {/* Voucher Manager Card */}
        <div className="card" onClick={() => navigate('/admin/vouchers')} style={{ cursor: 'pointer' }}>
          <span className="card-icon">🎟️</span>
          <div className="card-content">
            <p>{voucherCount ?? 0} voucher</p>
            <p>Tổng số voucher</p>
          </div>
        </div>
        {/* Wallet Manager Card */}
        <div className="card" onClick={() => navigate('/admin/wallets')} style={{ cursor: 'pointer' }}>
          <span className="card-icon">👛</span>
          <div className="card-content">
            <p>Quản lý ví</p>
            <p>Quản lý các giao dịch ví người dùng</p>
          </div>
        </div>

        {/*C2C Manager Card */}
        <div className="card" onClick={() => navigate('/admin/c2c-management')} style={{ cursor: 'pointer' }}>
          <span className="card-icon">🏪</span>
          <div className="card-content">
            <p>Quản lý C2C</p>
            <p>Duyệt tin đăng C2C của người dùng</p>
          </div>
        </div>
        
      </div>
    </div>
  );
};

export default DashBoard;
