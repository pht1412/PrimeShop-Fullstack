// src/pages/AccountPage.tsx
import React, { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import '../assets/css/account.css';
import api from '../api/api';
import * as walletApi from '../api/wallet.api';
import { IPaymentRequest } from '../api/wallet.api';
import { User } from '../types/user'; 
import { Order } from '../types/order';
import { toast, ToastContainer } from "react-toastify";
import "react-toastify/dist/ReactToastify.css";
import Swal from 'sweetalert2';
import VoucherList from '../components/VoucherList';
import WalletTab from '../components/WalletTab';
import C2CTab from '../components/C2CTab'; 

// Icons
import { 
  FaUser, FaClipboardList, FaTicketAlt, FaWallet, FaStore, 
  FaSearch, FaStoreAlt, FaTruck, FaCommentDots, FaMoneyBillWave, FaCheckCircle, FaTimesCircle, FaBoxOpen 
} from "react-icons/fa";

const AccountPage = () => {
  const navigate = useNavigate();
  const [activeTab, setActiveTab] = useState('profile');
  const [activeOrderStatus, setActiveOrderStatus] = useState('ALL'); 
  const [user, setUser] = useState<User | null>(null);
  const [orders, setOrders] = useState<Order[]>([]);
  const [formData, setFormData] = useState({ fullName: '', phoneNumber: '', address: '' });
  const [searchOrderTerm, setSearchOrderTerm] = useState('');

  // --- UTILS ---
  const formatCurrency = (amount: number | null | undefined) => {
    return (amount || 0).toLocaleString('vi-VN', { style: 'currency', currency: 'VND' });
  };

  // --- HELPER: MAP STATUS TO BADGE ---
  const getStatusInfo = (status: string) => {
    switch(status) {
      // 1. Nhóm Chờ (Màu Cam)
      case 'PENDING': return { label: 'Chờ xác nhận', class: 'status-pending', icon: <FaBoxOpen/> };
      case 'PROCESSING': return { label: 'Đang xử lý', class: 'status-pending', icon: <FaBoxOpen/> };
      case 'INVENTORY': return { label: 'Đang kiểm kho', class: 'status-pending', icon: <FaBoxOpen/> };
      
      // 2. Nhóm Xác Nhận (Màu Xanh Dương Đậm)
      case 'CONFIRMED': return { label: 'Đã xác nhận', class: 'status-confirmed', icon: <FaCheckCircle/> };
      case 'PAID': return { label: 'Đã thanh toán', class: 'status-paid', icon: <FaMoneyBillWave/> };
      
      // 3. Nhóm Vận Chuyển (Màu Xanh Trời)
      case 'READY_TO_SHIP': return { label: 'Sẵn sàng giao', class: 'status-ready', icon: <FaTruck/> };
      case 'SHIPPING': return { label: 'Đang giao hàng', class: 'status-shipping', icon: <FaTruck/> };
      case 'SHIPPED': return { label: 'Đã giao cho ĐVVC', class: 'status-shipped', icon: <FaTruck/> };
      
      // 4. Nhóm Thành Công (Màu Xanh Lá)
      case 'DELIVERED': return { label: 'Giao thành công', class: 'status-success', icon: <FaCheckCircle/> };
      case 'COMPLETED': return { label: 'Hoàn tất', class: 'status-success', icon: <FaCheckCircle/> };
      
      // 5. Nhóm Thất Bại (Màu Đỏ)
      case 'CANCELLED': return { label: 'Đã hủy', class: 'status-danger', icon: <FaTimesCircle/> };
      case 'PAYMENT_FAILED': return { label: 'Thanh toán lỗi', class: 'status-danger', icon: <FaTimesCircle/> };
      case 'DELIVERED_FAILED': return { label: 'Giao thất bại', class: 'status-danger', icon: <FaTimesCircle/> };
      case 'FAILED_DELIVERY': return { label: 'Giao thất bại', class: 'status-danger', icon: <FaTimesCircle/> };
      
      // 6. Nhóm Hoàn Trả (Màu Xám)
      case 'RETURNED': return { label: 'Đã trả hàng', class: 'status-neutral', icon: <FaBoxOpen/> };
      case 'REFUNDED': return { label: 'Đã hoàn tiền', class: 'status-neutral', icon: <FaMoneyBillWave/> };
      
      default: return { label: status, class: 'status-neutral', icon: null };
    }
  };

  // --- FETCH DATA ---
  const fetchUser = async () => {
    try {
      const response = await api.get('/auth/me');
      setUser(response.data);
      setFormData({ 
        fullName: response.data.fullName || '', 
        phoneNumber: response.data.phoneNumber || '', 
        address: response.data.address || '' 
      });
    } catch (e) { console.error(e) }
  };

  const fetchOrders = async () => {
    try {
      const response = await api.get('/order/get');
      const sortedOrders = response.data.sort((a:Order, b:Order) => 
        new Date(b.createdAt).getTime() - new Date(a.createdAt).getTime()
      );
      setOrders(sortedOrders);
    } catch (e) { console.error(e) }
  };

  useEffect(() => {
    fetchUser();
    fetchOrders();
  }, []);

  // --- LOGIC CHAT ---
  const handleChatWithShop = async (order: any) => {
    const token = localStorage.getItem('token');
    if (!token) {
        toast.info("Vui lòng đăng nhập để chat với Shop!");
        return navigate('/login');
    }
    const sellerId = order.orderItems?.[0]?.sellerId || order.shopId;
    if (!sellerId) {
        toast.error("Không xác định được chủ shop.");
        return;
    }
    try {
        const res = await api.post('/chat/create', null, { params: { sellerId: sellerId } });
        const chatEvent = new CustomEvent('PRIMESHOP_OPEN_CHAT', { detail: res.data });
        window.dispatchEvent(chatEvent);
    } catch (error) {
        console.error(error);
        toast.error("Không thể kết nối với Shop.");
    }
  };

  // --- LOGIC THANH TOÁN (PAYMENT) ---
  const handleFundiinPayment = async (orderId: string) => { /* Code cũ */ };
  const handleVnPayInstallment = async (orderId: string, amount: number) => { /* Code cũ */ };
  const handlePayWithWallet = async (orderId: string, amount: number) => { 
      if (!user) return toast.error("Vui lòng đăng nhập lại."); 
      try { 
          Swal.fire({ title: 'Đang thanh toán...', didOpen: () => Swal.showLoading() }); 
          const paymentData: IPaymentRequest = { orderId: Number(orderId), amount: amount }; 
          await walletApi.payOrder(Number(user.id), paymentData); 
          Swal.close(); 
          await Swal.fire('Thành công!', 'Thanh toán thành công.', 'success'); 
          fetchOrders(); 
      } catch (error: any) { 
          const msg = error?.response?.data || 'Ví không đủ số dư hoặc lỗi hệ thống.'; 
          Swal.fire('Thất bại', String(msg), 'error'); 
      } 
  };
  const handleVnPayPayOrder = async (orderId: string, amount: number) => { 
      try { 
          const response = await api.post("/payment/vnpay/create", { orderId: orderId, amount: amount }); 
          window.location.href = response.data.paymentUrl; 
      } catch (error) { toast.error('Lỗi khởi tạo thanh toán VNPay'); } 
  };
  const handlePaypalPayOrder = async (orderId: any, amount: number) => { 
      try { 
          const res = await api.post("/payment/paypal/create", { orderId, amount }); 
          window.location.href = res.data.links[0]; 
      } catch (error) { toast.error("Lỗi thanh toán PayPal"); } 
  };
  const handleMoMoPayOrder = async (orderId: any, amount: number) => { 
      try { 
          const res = await api.post("/payment/momo/create", { orderId, amount }); 
          window.location.href = res.data.payUrl; 
      } catch (error) { toast.error("Lỗi thanh toán MoMo"); } 
  }

  // --- PHƯƠNG THỨC THANH TOÁN (GIAO DIỆN MỚI LUNG LINH) ---
  const showPaymentMethod = (order: any) => {
    const amountToPay = order.finalAmount ?? order.totalAmount;
    
    Swal.fire({
      title: '', // Tắt title mặc định
      width: 500, // Tăng độ rộng modal
      html: `
        <div class="payment-modal-container">
          <div class="payment-header">
            <p class="payment-label">Tổng thanh toán</p>
            <h2 class="payment-amount">${formatCurrency(amountToPay)}</h2>
            <div class="payment-divider"></div>
            <p class="payment-sub">Vui lòng chọn phương thức bên dưới</p>
          </div>

          <div class="payment-options">
            
            <button id="pay-vnpay" class="payment-btn btn-vnpay">
              <div class="btn-icon">
                <svg viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg"><path d="M2 10C2 7.17157 2 5.75736 2.87868 4.87868C3.75736 4 5.17157 4 8 4H16C18.8284 4 20.2426 4 21.1213 4.87868C22 5.75736 22 7.17157 22 10H2ZM2 14H22C22 16.8284 22 18.2426 21.1213 19.1213C20.2426 20 18.8284 20 16 20H8C5.17157 20 3.75736 20 2.87868 19.1213C2 18.2426 2 16.8284 2 14Z" fill="currentColor" fill-opacity="0.5"/><path d="M6 16H10" stroke="currentColor" stroke-width="2" stroke-linecap="round"/></svg>
              </div>
              <div class="btn-info">
                <span class="btn-title">VNPAY QR</span>
                <span class="btn-desc">Quét mã nhanh chóng</span>
              </div>
            </button>

            <button id="pay-wallet" class="payment-btn btn-wallet">
              <div class="btn-icon">
                <svg viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg"><path d="M20 12V17C20 18.8856 20 19.8284 19.4142 20.4142C18.8284 21 17.8856 21 16 21H8C6.11438 21 5.17157 21 4.58579 20.4142C4 19.8284 4 18.8856 4 17V7C4 5.11438 4 4.17157 4.58579 3.58579C5.17157 3 6.11438 3 8 3H16C17.8856 3 18.8284 3 19.4142 3.58579C20 4.17157 20 5.11438 20 7V9" stroke="currentColor" stroke-width="2"/><path d="M18 12H22" stroke="currentColor" stroke-width="2" stroke-linecap="round"/><path d="M18 9H22V15H18V9Z" fill="currentColor"/></svg>
              </div>
              <div class="btn-info">
                <span class="btn-title">Ví PrimeShop</span>
                <span class="btn-desc">Thanh toán 1 chạm</span>
              </div>
              <span class="badge-discount">Khuyên dùng</span>
            </button>

            <button id="pay-momo" class="payment-btn btn-momo">
              <div class="btn-icon">
                <svg viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg"><path d="M12 22C17.5228 22 22 17.5228 22 12C22 6.47715 17.5228 2 12 2C6.47715 2 2 6.47715 2 12C2 17.5228 6.47715 22 12 22Z" stroke="currentColor" stroke-width="2"/><path d="M8 12H16M12 8V16" stroke="currentColor" stroke-width="2" stroke-linecap="round"/></svg>
              </div>
              <div class="btn-info">
                <span class="btn-title">MoMo</span>
                <span class="btn-desc">Ví điện tử quốc dân</span>
              </div>
            </button>

            <button id="pay-paypal" class="payment-btn btn-paypal">
              <div class="btn-icon">
                <svg viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg"><path d="M3 10C3 6.22876 3 4.34315 4.17157 3.17157C5.34315 2 7.22876 2 11 2H13C16.7712 2 18.6569 2 19.8284 3.17157C21 4.34315 21 6.22876 21 10V14C21 17.7712 21 19.6569 19.8284 20.8284C18.6569 22 16.7712 22 13 22H11C7.22876 22 5.34315 22 4.17157 20.8284C3 19.6569 3 17.7712 3 14V10Z" stroke="currentColor" stroke-width="2"/><path d="M8 12H16M16 12L13 9M16 12L13 15" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/></svg>
              </div>
              <div class="btn-info">
                <span class="btn-title">PayPal / Visa</span>
                <span class="btn-desc">Thẻ quốc tế</span>
              </div>
            </button>

          </div>
        </div>
      `,
      showConfirmButton: false,
      showCloseButton: true,
      focusConfirm: false,
      // Logic xử lý sự kiện click
      didOpen: () => {
        const orderId = order.orderId;
        document.getElementById('pay-vnpay')?.addEventListener('click', () => { handleVnPayPayOrder(orderId, amountToPay); Swal.close(); });
        document.getElementById('pay-wallet')?.addEventListener('click', () => { handlePayWithWallet(orderId, amountToPay); Swal.close(); });
        document.getElementById('pay-momo')?.addEventListener('click', () => { handleMoMoPayOrder(orderId, amountToPay); Swal.close(); });
        document.getElementById('pay-paypal')?.addEventListener('click', () => { handlePaypalPayOrder(orderId, amountToPay); Swal.close(); });
      }
    });
  };

  const handleReceiveOrder = async (orderId: string) => {
    try {
      await api.put(`/order/update-status?id=${orderId}&status=DELIVERED`);
      fetchOrders();
      toast.success("Đã xác nhận nhận hàng!");
    } catch (error) { toast.error('Lỗi cập nhật trạng thái'); }
  };

  const handleUpdateProfile = async (e: React.FormEvent) => {
    e.preventDefault();
    try {
      await api.patch('/auth/update', formData);
      Swal.fire('Thành công', 'Cập nhật thông tin thành công!', 'success').then(() => window.location.reload());
    } catch (error) { toast.error(`Có lỗi xảy ra khi cập nhật thông tin!`); }
  };

  const handleInputChange = (e: React.ChangeEvent<HTMLInputElement | HTMLTextAreaElement>) => {
    const { name, value } = e.target;
    setFormData(prev => ({ ...prev, [name]: value }));
  };

  // --- FILTER ---
  const getFilteredOrders = () => {
    let filtered = orders;
    if (activeOrderStatus !== 'ALL') {
      filtered = filtered.filter(order => order.orderStatus === activeOrderStatus);
    }
    if (searchOrderTerm) {
      const term = searchOrderTerm.toLowerCase();
      filtered = filtered.filter(order => 
        String(order.orderId).includes(term) || 
        order.orderItems.some(item => item.productName.toLowerCase().includes(term))
      );
    }
    return filtered;
  };

  return (
    <section className="account-page">
      <ToastContainer position="top-right" autoClose={2000} />
      <div className="account-container">
        
        {/* SIDEBAR */}
        <aside className="account-sidebar">
          <div className="sidebar-user">
            <img src={user?.avatar || "https://cdn.pixabay.com/photo/2015/10/05/22/37/blank-profile-picture-973460_1280.png"} alt="Avatar" className="sidebar-avatar" />
            <div className="sidebar-info">
              <h3>{user?.username}</h3>
              <p className="edit-profile"><FaUser size={10}/> Sửa hồ sơ</p>
            </div>
          </div>
          <ul className="sidebar-menu">
            <li className={`sidebar-item ${activeTab === 'profile' ? 'active' : ''}`} onClick={() => setActiveTab('profile')}>
              <FaUser className="sidebar-icon"/> Tài Khoản
            </li>
            <li className={`sidebar-item ${activeTab === 'orders' ? 'active' : ''}`} onClick={() => setActiveTab('orders')}>
              <FaClipboardList className="sidebar-icon"/> Đơn Mua
            </li>
            <li className={`sidebar-item ${activeTab === 'wallet' ? 'active' : ''}`} onClick={() => setActiveTab('wallet')}>
              <FaWallet className="sidebar-icon"/> Ví Prime
            </li>
            <li className={`sidebar-item ${activeTab === 'vouchers' ? 'active' : ''}`} onClick={() => setActiveTab('vouchers')}>
              <FaTicketAlt className="sidebar-icon"/> Kho Voucher
            </li>
            <li className={`sidebar-item ${activeTab === 'c2c' ? 'active' : ''}`} onClick={() => setActiveTab('c2c')}>
              <FaStore className="sidebar-icon"/> Kênh Người Bán
            </li>
          </ul>
        </aside>

        {/* CONTENT */}
        <div className="account-content">
          {activeTab === 'profile' && (
            <div className="animate-fade-in">
              <div className="section-title">Hồ Sơ Của Tôi <span className="section-subtitle">Quản lý thông tin hồ sơ để bảo mật tài khoản</span></div>
              <div className="profile-layout">
                <form className="profile-form-col" onSubmit={handleUpdateProfile}>
                  <div className="form-group-row">
                    <label className="form-label">Tên hiển thị</label>
                    <input type="text" className="form-input" name="fullName" value={formData.fullName} onChange={handleInputChange} />
                  </div>
                  <div className="form-group-row">
                    <label className="form-label">Số điện thoại</label>
                    <input type="text" className="form-input" name="phoneNumber" value={formData.phoneNumber} onChange={handleInputChange} />
                  </div>
                  <div className="form-group-row">
                    <label className="form-label">Địa chỉ</label>
                    <input type="text" className="form-input" name="address" value={formData.address} onChange={handleInputChange} />
                  </div>
                  <div className="form-group-row">
                    <label className="form-label">Email</label>
                    <div style={{flex:1, padding:'12px 15px', color:'#555'}}>{user?.email}</div>
                  </div>
                  <button type="submit" className="btn-save">Lưu</button>
                </form>
                <div className="profile-avatar-col">
                  <img src={user?.avatar || "https://cdn.pixabay.com/photo/2015/10/05/22/37/blank-profile-picture-973460_1280.png"} alt="Avatar" className="avatar-preview" />
                  <button className="btn-outline" style={{fontSize:'0.85rem'}}>Chọn Ảnh</button>
                </div>
              </div>
            </div>
          )}

          {activeTab === 'orders' && (
            <div className="animate-fade-in">
              <div className="order-tabs-header">
                {['ALL', 'PENDING', 'CONFIRMED', 'SHIPPED', 'DELIVERED', 'CANCELLED'].map(status => (
                  <div key={status} className={`order-tab-item ${activeOrderStatus === status ? 'active' : ''}`} onClick={() => setActiveOrderStatus(status)}>
                    {status === 'ALL' ? 'Tất cả' : getStatusInfo(status).label}
                  </div>
                ))}
              </div>

              {/* SEARCH BAR MỚI ĐẸP HƠN */}
              <div className="order-search-container">
                <FaSearch className="search-icon-absolute" />
                <input 
                  type="text" 
                  className="order-search-box"
                  placeholder="Tìm kiếm theo Mã đơn hàng hoặc Tên sản phẩm..." 
                  value={searchOrderTerm} 
                  onChange={(e) => setSearchOrderTerm(e.target.value)} 
                />
              </div>

              <div className="order-list-container">
                {getFilteredOrders().length > 0 ? (
                  getFilteredOrders().map(order => {
                    const firstItem = order.orderItems[0];
                    const otherItemsCount = order.orderItems.length - 1;
                    const displayTotal = order.finalAmount ?? order.totalAmount;
                    const statusInfo = getStatusInfo(order.orderStatus);

                    return (
                      <div key={order.orderId} className="order-card">
                        <div className="card-header">
                          <div className="shop-name"><FaStoreAlt /> PrimeShop Mall</div>
                          {/* BADGE TRẠNG THÁI MỚI */}
                          <div className={`status-badge ${statusInfo.class}`}>
                            {statusInfo.icon} {statusInfo.label}
                          </div>
                        </div>

                        <div className="card-body">
                          <img 
                            src={firstItem?.imageUrl || "https://via.placeholder.com/80?text=No+Img"} 
                            alt={firstItem?.productName} 
                            className="product-thumb"
                            onError={(e) => e.currentTarget.src = "https://via.placeholder.com/80?text=Error"}
                          />
                          <div className="product-info">
                            <div className="product-name">{firstItem?.productName}</div>
                            <div className="product-variant">Số lượng: x{firstItem?.quantity}</div>
                            {otherItemsCount > 0 && (
                              <div style={{fontSize:'0.8rem', color:'#6B7280', marginTop:'5px'}}>
                                + {otherItemsCount} sản phẩm khác
                              </div>
                            )}
                          </div>
                        </div>

                        <div className="card-footer">
                          <div className="total-section">
                            <span>Thành tiền:</span>
                            <span className="total-price">{formatCurrency(displayTotal)}</span>
                          </div>
                          
                          <div className="action-buttons">
                            {order.orderStatus === 'CONFIRMED' && (
                                <button className="btn-action btn-fill-primary" onClick={() => showPaymentMethod(order)}>
                                    <FaMoneyBillWave /> Thanh toán ngay
                                </button>
                            )}

                            {order.orderStatus === 'DELIVERED' && (
                              <>
                                <button className="btn-action btn-fill-blue" onClick={() => handleReceiveOrder(order.orderId)}>
                                    Đã nhận hàng
                                </button>
                                <button className="btn-action btn-outline">Mua Lại</button>
                              </>
                            )}

                            {/* {(order.orderStatus !== 'DELIVERED' && order.orderStatus !== 'CANCELLED') && (
                                <button className="btn-action btn-chat" onClick={() => handleChatWithShop(order)}>
                                    <FaCommentDots /> Chat với Shop
                                </button>
                            )} */}
                          </div>
                        </div>
                      </div>
                    );
                  })
                ) : (
                  <div className="no-orders-found">
                    <img src="https://deo.shopeemobile.com/shopee/shopee-pcmall-live-sg/5fafbb923393b712b96488590b8f781d.png" alt="No Order" style={{width:100, marginBottom:20, opacity:0.5}}/>
                    <p>Chưa có đơn hàng nào</p>
                  </div>
                )}
              </div>
            </div>
          )}

          {activeTab === 'vouchers' && <div className="animate-fade-in"><VoucherList showAllVouchers={true} /></div>}
          {activeTab === 'wallet' && <WalletTab user={user} />}
          {activeTab === 'c2c' && <C2CTab user={user} />}
        </div>
      </div>
    </section>
  );
};

export default AccountPage;