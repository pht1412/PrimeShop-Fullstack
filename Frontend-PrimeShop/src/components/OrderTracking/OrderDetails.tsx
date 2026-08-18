import React from "react";
import { OrderDetails as OrderDetailsType } from "../../services/order";
import styles from "./styles/OrderTrackingPages.module.css"; // Import CSS Modules

interface OrderDetailsProps {
  orderId: string;
  details: OrderDetailsType;
}

const OrderDetails: React.FC<OrderDetailsProps> = ({ orderId, details }) => {
  return (
    <div className={styles.orderDetails}>
      <h2>Chi tiết đơn hàng #{orderId}</h2>
      <div className={styles.detailsCard}>
        <p>
          <strong>Sản phẩm:</strong> {details.product}
        </p>
        <p>
          <strong>Số lượng:</strong> {details.quantity}
        </p>
        <p>
          <strong>Tổng tiền:</strong> {details.total}
        </p>
        <p>
          <strong>Địa chỉ giao:</strong> {details.address}
        </p>
        <button className={styles.supportButton}>Liên hệ hỗ trợ</button>
      </div>
    </div>
  );
};

export default OrderDetails;