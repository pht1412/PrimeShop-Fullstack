import React, { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import dayjs from 'dayjs';
import api from '../../api/api';
import './delivery.css'; // Import css file cùng folder
import UpdateStatusModal from './UpdateStatusModal';
import TrackingHistoryModal from './TrackingHistoryModal';
import { Button } from '@mui/material';

interface Order {
  id: number;
  orderId: string;
  fullName: string;
  phoneNumber: string;
  address: string;
  orderStatus: string;
  estimatedDeliveryDate?: string;
}

const DeliveryDashboard: React.FC = () => {
  const [orders, setOrders] = useState<Order[]>([]);
  const [selectedOrder, setSelectedOrder] = useState<Order | null>(null);
  const [showUpdateModal, setShowUpdateModal] = useState(false);
  const [showTrackingModal, setShowTrackingModal] = useState(false);
  const navigate = useNavigate();

  // 1. Thêm state cho bộ lọc
  const [filters, setFilters] = useState({
    orderId: "",
    status: "all", // Mặc định là all (sẽ lấy list DELIVERY_STATUSES)
    startDate: "",
    endDate: ""
  });

  // 2. Fetch lại dữ liệu khi filters thay đổi
  useEffect(() => {
    fetchOrders();
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [filters]); // Dependency array theo dõi filters

  const fetchOrders = async () => {
    try {
      // LOGIC SỬA ĐỔI:
      // 1. Nếu chọn "all": Gửi null (hoặc undefined) để BE trả về tất cả.
      // 2. Nếu chọn cụ thể: Gửi chính chuỗi đó (không bọc trong mảng []).
      const statusToSend = filters.status === "all" ? null : filters.status;

      const payload = {
        status: statusToSend, // Truyền String hoặc Null
        orderId: filters.orderId || undefined,
        search: filters.search || undefined, // Thêm search nếu cần
        startDate: filters.startDate ? dayjs(filters.startDate).format("YYYY-MM-DDTHH:mm:ss") : undefined,
        endDate: filters.endDate ? dayjs(filters.endDate).format("YYYY-MM-DDTHH:mm:ss") : undefined
      };

      const response = await api.post('/admin/order/filter', payload);
      
      // LƯU Ý QUAN TRỌNG (CLIENT-SIDE FILTERING):
      // Vì BE trả về "Tất cả" (bao gồm cả Pending, Cancelled...) khi status = null,
      // nhưng đây là trang "Vận chuyển", nên ta có thể lọc bớt ở phía Client để giao diện gọn hơn.
      
      let data = response.data;

      // Nếu đang chọn "Tất cả", ta chỉ giữ lại các trạng thái liên quan đến vận chuyển
      if (filters.status === "all") {
         const DELIVERY_RELATED_STATUSES = [
            'READY_TO_SHIP', 'PROCESSING', 'INVENTORY', 
            'SHIPPING', 'SHIPPED', 'DELIVERED', 
            'RETURNED', 'REFUNDED', 'FAILED_DELIVERY'
         ];
         // Lọc lại data ngay tại đây trước khi setOrders
         data = data.filter((order: Order) => DELIVERY_RELATED_STATUSES.includes(order.orderStatus));
      }

      setOrders(data);
      
    } catch (error) {
      console.error('Lỗi lấy danh sách đơn hàng:', error);
    }
  };

  const handleUpdateStatus = (order: Order) => {
    setSelectedOrder(order);
    setShowUpdateModal(true);
  };

  const handleShowTracking = (order: Order) => {
    setSelectedOrder(order);
    setShowTrackingModal(true);
  };

  const formatDate = (dateString?: string) => {
    if (!dateString) return '-';
    const date = new Date(dateString);
    return date.toLocaleDateString();
  };

  return (
    <div className="delivery-dashboard-container">
      <h2 className="delivery-title">Quản lý vận chuyển đơn hàng</h2>

      <table className="delivery-table">
        <thead>
          <tr>
            <th>Mã đơn</th>
            <th>Khách hàng</th>
            <th>Số điện thoại</th>
            <th>Địa chỉ</th>
            <th>Trạng thái</th>
            <th>Ngày giao dự kiến</th>
            <th>Thao tác</th>
          </tr>
        </thead>
        <tbody>
          {orders.length === 0 ? (
            <tr>
              <td colSpan={7} className="no-orders">
                Không có đơn hàng vận chuyển nào
              </td>
            </tr>
          ) : (
            orders.map(order => (
              <tr key={order.orderId}>
                <td>{order.orderId || order.id}</td>
                <td>{order.fullName}</td>
                <td>{order.phoneNumber}</td>
                <td>{order.address}</td>
                <td>
                  <span className={`status-badge status-${order.orderStatus.toLowerCase()}`}>
                    {order.orderStatus}
                  </span>
                </td>
                <td>{formatDate(order.estimatedDeliveryDate)}</td>
                <td>
                  <Button onClick={() => handleUpdateStatus(order)}>Cập nhật trạng thái</Button>
                  <Button onClick={() => handleShowTracking(order)}>Xem tracking</Button>
                </td>
              </tr>
            ))
          )}
        </tbody>
      </table>

      {/* Modal cập nhật trạng thái */}
      {showUpdateModal && selectedOrder && (
        <UpdateStatusModal 
          order={selectedOrder} 
          open={showUpdateModal} 
          onClose={() => setShowUpdateModal(false)} 
          onSuccess={() => {
            setShowUpdateModal(false);
            fetchOrders();
          }}
        />
      )}

      {/* Modal lịch sử tracking */}
      {showTrackingModal && selectedOrder && (
        <TrackingHistoryModal 
          orderId={selectedOrder?.orderId || null} 
          open={showTrackingModal} 
          onClose={() => setShowTrackingModal(false)} 
        />
      )}
    </div>
  );
};

export default DeliveryDashboard;
