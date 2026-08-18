import React, { useState } from "react";
import AuthForm from "../../components/Login-register/AuthForm";
import InputField from "../../components/Login-register/InputField";
import { Link, useNavigate } from "react-router-dom";
import "../../components/Login-register/style.css";
import api from "../../api/api";
import Swal from "sweetalert2";
import { FaUser, FaEnvelope, FaLock } from "react-icons/fa";

const RegisterPage = () => {
  const [registerData, setRegisterData] = useState({
    username: "",
    password: "",
    email: "",
    confirmPassword: "",
    role: "USER"
  });
  const [error, setError] = useState<string | null>(null);
  const [loading, setLoading] = useState(false);
  const navigate = useNavigate();

  const handleChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    setRegisterData({ ...registerData, [e.target.name]: e.target.value });
    setError(null);
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!registerData.username.trim()) {
      setError("Vui lòng nhập tên đăng nhập!");
      return;
    }
    if (!validateEmail(registerData.email)) {
      setError("Email không hợp lệ!");
      return;
    }
    if (!registerData.password.trim()) {
      setError("Vui lòng nhập mật khẩu!");
      return;
    }
    if (registerData.password !== registerData.confirmPassword) {
      setError("Mật khẩu không khớp!");
      return;
    }

    try {
      setLoading(true);
      const { confirmPassword, ...dataToSend } = registerData;
      await api.post("/auth/register", dataToSend);
      Swal.fire({
        icon: 'success',
        title: 'Đăng ký thành công!',
        showConfirmButton: false,
        timer: 1500
      });
      navigate("/login");
    } catch (err: any) {
      setError(err.response?.data?.message || "Đăng ký thất bại!");
    } finally {
      setLoading(false);
    }
  };

  const validateEmail = (email: string) => {
    const re = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
    return re.test(email);
  };

  return (
    <div className="auth-page">
      <AuthForm
        title="Đăng ký tài khoản"
        onSubmit={handleSubmit}
        footer={<p>Đã có tài khoản? <Link to="/login">Đăng nhập</Link></p>}
      >
        {error && <div className="error-message">{error}</div>}
        <InputField
          label="Tên đăng nhập"
          name="username"
          value={registerData.username}
          onChange={handleChange}
          icon={<FaUser />}
        />
        <InputField
          label="Email"
          name="email"
          type="email"
          value={registerData.email}
          onChange={handleChange}
          icon={<FaEnvelope />}
        />
        <InputField
          label="Mật khẩu"
          name="password"
          type="password"
          value={registerData.password}
          onChange={handleChange}
          icon={<FaLock />}
        />
        <InputField
          label="Nhập lại mật khẩu"
          name="confirmPassword"
          type="password"
          value={registerData.confirmPassword}
          onChange={handleChange}
          icon={<FaLock />}
        />
        <button type="submit" className="auth-btn" disabled={loading}>
          {loading ? (
            <span className="loading-spinner">Đang xử lý...</span>
          ) : (
            "Đăng ký"
          )}
        </button>
      </AuthForm>
    </div>
  );
};

export default RegisterPage;