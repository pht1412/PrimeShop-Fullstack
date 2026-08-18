import React, { useEffect, useState } from "react";
import { useNavigate, useSearchParams } from "react-router-dom";
import api from "../api/api";
import AuthForm from "../components/Login-register/AuthForm";
import { Button } from "@mui/material";

const PaymentResult = () => {
  const [searchParams] = useSearchParams();
  const status = searchParams.get("vnp_ResponseCode"); // Đây sẽ là "00" nếu thanh toán thành công
  const rawOrderId = searchParams.get("vnp_OrderInfo");
  const orderId = rawOrderId?.split("#")[1];
  const amount = searchParams.get("vnp_Amount");
  const date = searchParams.get("vnp_PayDate");
  const [message, setMessage] = useState("");
  const navigate = useNavigate();

  const handlePaymentResult = async () => {
    try {
      const response = await api.get(`/payment/callback?${searchParams.toString()}`);
      console.log(response);
    } catch (error) {
      console.log(error);
    }
  };

  function formatVNPayDate(raw) {
    if (!raw || raw.length !== 14) return "Ngày không hợp lệ";
  
    const year = raw.slice(0, 4);
    const month = raw.slice(4, 6);
    const day = raw.slice(6, 8);
    const hour = raw.slice(8, 10);
    const minute = raw.slice(10, 12);
    const second = raw.slice(12, 14);
  
    return `${day}/${month}/${year} ${hour}:${minute}:${second}`;
  }
  

  useEffect(() => {
    if (status === "00") {
      setMessage("Thanh toán thành công 🎉");
    } else {
      setMessage("Thanh toán thất bại ❌");
    }

    handlePaymentResult();
  }, [status]);

  return (
    <AuthForm title={""} onSubmit={() => {}}>
        <h2 className="text-2xl font-bold">{message}</h2>
        <p>Mã đơn hàng: {orderId}</p>
        <p>Số tiền: {(Number(amount) / 100).toLocaleString('vi-VN')} VNĐ</p>
        <p>Ngày thanh toán: {formatVNPayDate(date)}</p>
        <Button variant="contained" className="mt-4 text-blue-500 underline" onClick={() => navigate("/")}>Quay về trang chủ</Button>
    </AuthForm>
  );
};

export default PaymentResult;
