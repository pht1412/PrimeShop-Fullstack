import React, { useState } from "react";
import AuthForm from "../../components/Login-register/AuthForm";
import InputField from "../../components/Login-register/InputField";
import { Link, useNavigate, useLocation } from "react-router-dom";
import "../../components/Login-register/style.css";
import api from "../../api/api";
import { useAuth } from "../../context/AuthContext";
import { useCart } from "../../context/CartContext";
import { toast, ToastContainer } from "react-toastify";
import "react-toastify/dist/ReactToastify.css";
import Swal from "sweetalert2";
import { FaUser, FaLock } from "react-icons/fa";

const LoginPage = () => {
  const [loginData, setLoginData] = useState({ username: "", password: "" });
  const [error, setError] = useState<string | null>(null);
  const [loading, setLoading] = useState(false);
  const navigate = useNavigate();
  const location = useLocation();
  const { login } = useAuth();

  const handleChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    setLoginData({ ...loginData, [e.target.name]: e.target.value });
    setError(null);
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();

    if (!loginData.username.trim()) {
      setError("Vui lòng nhập tên đăng nhập!");
      return;
    }
    if (!loginData.password.trim()) {
      setError("Vui lòng nhập mật khẩu!");
      return;
    }

    try {
      setLoading(true);
      const res = await api.post("/auth/login", loginData);
      const { token, user } = res.data;
      
      // Get cart item count if available
      let cartItemCount = 0;
      try {
        const cartRes = await api.get("/cart/count");
        cartItemCount = cartRes.data || 0;
      } catch (cartError) {
        console.log("Could not fetch cart count:", cartError);
      }
      
      login(user, token, cartItemCount);
      Swal.fire({
        icon: 'success',
        title: 'Đăng nhập thành công!',
        showConfirmButton: false,
        timer: 1500
      });
      const from = location.state?.from?.pathname || "/home";
      setTimeout(() => {
        navigate(from, { replace: true });
      }, 1000);
    } catch (err: any) {
      setError(err.response?.data?.message || "Đăng nhập thất bại!");
    } finally {
      setLoading(false);
    }
  };

  return (
    <>
      <ToastContainer />
      <div className="auth-page">
        <AuthForm
          title="Đăng nhập tài khoản"
          onSubmit={handleSubmit}
          footer={<p>Chưa có tài khoản? <Link to="/register">Đăng ký</Link></p>}
        >
          {error && <div className="error-message">{error}</div>}
          <InputField
            label="Tên đăng nhập"
            name="username"
            type="text"
            value={loginData.username}
            onChange={handleChange}
            icon={<FaUser />}
          />
          <InputField
            label="Mật khẩu"
            name="password"
            type="password"
            value={loginData.password}
            onChange={handleChange}
            icon={<FaLock />}
          />
          <Link to="/forgot-password" className="forgot-password">
            Quên mật khẩu?
          </Link>
          <button type="submit" className="auth-btn" disabled={loading}>
            {loading ? (
              <span className="loading-spinner">Đang xử lý...</span>
            ) : (
              "Đăng nhập"
            )}
          </button>
        </AuthForm>
      </div>
    </>
  );
};

export default LoginPage;