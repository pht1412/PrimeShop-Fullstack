import React from 'react';
import { Link } from 'react-router-dom';
import '../assets/css/footer.css';

// Import các icon mạng xã hội
import facebookIcon from '../assets/images/icons/fb.png';
import instagramIcon from '../assets/images/icons/ins.png';
import tiktokIcon from '../assets/images/icons/tiktok.png';
import zaloIcon from '../assets/images/icons/zalo.png';

// Import icon thanh toán (Đã thêm Momo và Paypal)
import vnpayIcon from '../assets/images/icons/vnpay.png';
import momoIcon from '../assets/images/icons/momo.png';   // Cần file này
import paypalIcon from '../assets/images/icons/paypal.png'; // Cần file này

const Footer = () => {
  return (
    <footer className="footer">
      <div className="footer-container">
        {/* Cột 1: Thông tin liên hệ */}
        <div className="footer-column">
          <h3>PrimeShop</h3>
          <p>Địa chỉ: 10/80c Song Hành Xa Lộ Hà Nội, Phường Tân Phú, Thủ Đức, Hồ Chí Minh</p>
          <p>Điện thoại: 0123456789</p>
          <p>Email: contact@primeshop.com</p>
        </div>

        {/* Cột 2: Chính sách */}
        <div className="footer-column">
          <h3>Chính sách</h3>
          <p><Link to="/warranty-policy">Chính sách bảo hành</Link></p>
          <p><Link to="/return-policy">Chính sách đổi trả</Link></p>
          <p><Link to="/privacy-policy">Chính sách bảo mật</Link></p>
          <p><Link to="/terms-of-use">Điều khoản sử dụng</Link></p>
        </div>

        {/* Cột 3: Hướng dẫn */}
        <div className="footer-column">
          <h3>Hướng dẫn</h3>
          <p><Link to="/shopping-guide">Hướng dẫn mua hàng</Link></p>
          <p><Link to="/payment-guide">Hướng dẫn thanh toán</Link></p>
          <p><Link to="/installment-guide">Hướng dẫn trả góp</Link></p>
          <p><Link to="/voucher-guide">Hướng dẫn sử dụng voucher</Link></p>
        </div>

        {/* Cột 4: Kết nối & Thanh toán */}
        <div className="footer-column">
          <h3>Kết nối với chúng tôi</h3>
          <div className="social-icons">
            <a href="https://facebook.com/primeshop" target="_blank" rel="noopener noreferrer">
              <img src={facebookIcon} alt="Facebook" />
            </a>
            <a href="https://instagram.com/primeshop" target="_blank" rel="noopener noreferrer">
              <img src={instagramIcon} alt="Instagram" />
            </a>
            <a href="https://tiktok.com/@primeshop" target="_blank" rel="noopener noreferrer">
              <img src={tiktokIcon} alt="TikTok" />
            </a>
            <a href="https://zalo.me/primeshop" target="_blank" rel="noopener noreferrer">
              <img src={zaloIcon} alt="Zalo" />
            </a>
          </div>

          <h3>Phương thức thanh toán</h3>
          <div className="payment-icons">
            {/* VNPAY */}
            <a href="https://vnpay.vn" target="_blank" rel="noopener noreferrer" title="Thanh toán qua VNPAY">
              <img src={vnpayIcon} alt="VNPAY" />
            </a>
            
            {/* MOMO */}
            <a href="https://momo.vn" target="_blank" rel="noopener noreferrer" title="Thanh toán qua Ví MoMo">
              <img src={momoIcon} alt="MOMO" />
            </a>

            {/* PAYPAL */}
            <a href="https://www.paypal.com" target="_blank" rel="noopener noreferrer" title="Thanh toán qua PayPal">
              <img src={paypalIcon} alt="PAYPAL" />
            </a>
          </div>
        </div>
      </div>
    </footer>
  );
};

export default Footer;