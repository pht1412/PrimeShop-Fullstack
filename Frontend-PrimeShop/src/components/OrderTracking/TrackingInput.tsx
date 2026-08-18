import React from "react";
import styles from "./styles/OrderTrackingPages.module.css";

type TrackingInputProps = {
  orderId: string;
  onOrderIdChange: (orderId: string) => void;
  onTrack: () => void;
  onReset: () => void;
};

const TrackingInput: React.FC<TrackingInputProps> = ({
  orderId,
  onOrderIdChange,
  onTrack,
  onReset,
}) => {
  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    onTrack();
  };

  return (
    <form onSubmit={handleSubmit} className={styles.trackingInput}>
      <input
        type="text"
        value={orderId}
        onChange={(e) => onOrderIdChange(e.target.value)}
        placeholder="Nhập mã đơn hàng..."
        className={styles.input}
        aria-label="Nhập mã đơn hàng để theo dõi"
      />
      <div className={styles.buttonGroup}>
        <button type="submit" className={styles.searchButton}>
          Tìm kiếm
        </button>
        <button
          type="button"
          onClick={onReset}
          className={styles.resetButton}
        >
          Làm mới
        </button>
      </div>
    </form>
  );
};

export default TrackingInput;