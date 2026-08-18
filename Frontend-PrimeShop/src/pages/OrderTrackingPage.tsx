import React, { useState } from "react";
import { ToastContainer, toast } from "react-toastify";
import "react-toastify/dist/ReactToastify.css"; // Import CSS cho Toastify
import styles from "../components/OrderTracking/styles/OrderTrackingPages.module.css";
import TrackingInput from "../components/OrderTracking/TrackingInput";
import TrackingTimeline from "../components/OrderTracking/TrackingTimeLine";
import OrderDetails from "../components/OrderTracking/OrderDetails";
import { fetchOrderTracking } from "../services/orderService";
import { OrderTrackingData } from "../services/order";

// Định nghĩa type cho component
const OrderTrackingPages: React.FC = () => {
  // State quản lý mã đơn hàng, dữ liệu theo dõi, trạng thái loading và lỗi
  const [orderId, setOrderId] = useState<string>("");
  const [trackingData, setTrackingData] = useState<OrderTrackingData | null>(null);
  const [isLoading, setIsLoading] = useState<boolean>(false);
  const [error, setError] = useState<string | null>(null);

  // Xử lý tìm kiếm đơn hàng
  const handleTrackOrder = async () => {
    if (!orderId.trim()) {
      toast.error("Vui lòng nhập mã đơn hàng!");
      return;
    }

    setIsLoading(true);
    setError(null);
    setTrackingData(null);

    try {
      const data = await fetchOrderTracking(orderId);
      setTrackingData(data);
      toast.success("Đã tìm thấy đơn hàng!");
    } catch (err: any) {
      const errorMessage = err.message ?? "Có lỗi xảy ra khi tìm kiếm đơn hàng";
      setError(errorMessage);
      toast.error(errorMessage);
    } finally {
      setIsLoading(false);
    }
  };

  // Xử lý reset form
  const handleReset = () => {
    setOrderId("");
    setTrackingData(null);
    setError(null);
  };

  return (
    <div className={styles.page}>
      {/* Thông báo Toastify */}
      <ToastContainer position="top-right" autoClose={3000} hideProgressBar={false} />

      {/* Header */}
      <header className={styles.header}>
        <h1>Theo dõi đơn hàng của bạn</h1>
      </header>

      {/* Form nhập mã đơn hàng */}
      <TrackingInput
        orderId={orderId}
        onOrderIdChange={setOrderId}
        onTrack={handleTrackOrder}
        onReset={handleReset}
      />

      {/* Trạng thái loading */}
      {isLoading && (
        <div className={styles.loading}>
          <p>Đang tìm kiếm đơn hàng...</p>
        </div>
      )}

      {/* Thông báo lỗi */}
      {error && !isLoading && (
        <div className={styles.error}>
          <p>{error}</p>
        </div>
      )}

      {/* Hiển thị kết quả với hiệu ứng fade-in */}
      {trackingData && !isLoading && !error && (
        <div className={`${styles.content} ${styles.fadeIn}`}>
          <TrackingTimeline steps={trackingData.steps} />
          <OrderDetails
            orderId={trackingData.orderId}
            details={trackingData.details}
          />
        </div>
      )}
    </div>
  );
};

export default OrderTrackingPages;