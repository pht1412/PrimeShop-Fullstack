import React, { useState } from "react";
import { Link } from "react-router-dom";
import "../../components/Login-register/style.css";

const ForgotPasswordPage = () => {
  const [email, setEmail] = useState("");
  const [message, setMessage] = useState<string | null>(null);
  const [loading, setLoading] = useState(false);

  const handleChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    setEmail(e.target.value);
    setMessage(null); // Xóa thông báo khi thay đổi
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!validateEmail(email)) {
      setMessage("Email không hợp lệ!");
      return;
    }
    if (!email.trim()) {
      setMessage("Vui lòng nhập email!");
      return;
    }

    setLoading(true);
    // 👉 Sau này gọi API từ Spring Boot
    // try {
    //   const response = await fetch("http://localhost:8080/api/forgot-password", {
    //     method: "POST",
    //     headers: { "Content-Type": "application/json" },
    //     body: JSON.stringify({ email }),
    //   });
    //   const data = await response.json();
    //   if (!response.ok) throw new Error(data.message || "Gửi yêu cầu thất bại!");
    //   setMessage("Vui lòng kiểm tra email để khôi phục mật khẩu!");
    // } catch (err) {
    //   setMessage(err.message || "Có lỗi xảy ra!");
    // } finally {
    //   setLoading(false);
    // }

    // Giả lập thành công để test
    setTimeout(() => {
      setMessage("Vui lòng kiểm tra email để khôi phục mật khẩu!");
      setLoading(false);
    }, 1000);
  };

  const validateEmail = (email: string) => {
    const re = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
    return re.test(email);
  };

  return (
    <div className="auth-container">
      <h2>Khôi phục mật khẩu</h2>
      <form onSubmit={handleSubmit} className="auth-form">
        {message && <div className={message.includes("thất bại") ? "error-message" : "success-message"}>{message}</div>}
        <div className="input-group">
          <label htmlFor="email">Email</label>
          <input
            type="email"
            id="email"
            value={email}
            onChange={handleChange}
            placeholder="Nhập email của bạn"
            required
            disabled={loading}
          />
        </div>
        <button type="submit" className="auth-btn" disabled={loading}>
          {loading ? "Đang xử lý..." : "Gửi yêu cầu"}
        </button>
      </form>
      <div className="auth-footer">
        <Link to="/login" className="back-link">
          Quay lại đăng nhập
        </Link>
      </div>
    </div>
  );
};

export default ForgotPasswordPage;