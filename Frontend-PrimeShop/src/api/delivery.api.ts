import api from './api';

export interface Order {
  id: number;
  fullName: string;
  phoneNumber: string;
  address: string;
  status: string;
  estimatedDeliveryDate?: string;
  totalAmount?: number;
  orderDate?: string;
  trackingHistory?: TrackingEvent[];
}

export interface TrackingEvent {
  id: number;
  status: string;
  location: string;
  timestamp: string;
}

export interface UpdateStatusRequest {
  status: string;
  location: string;
}

export const deliveryApi = {
  // Lấy danh sách đơn hàng cho delivery
  getOrders: async (status?: string): Promise<Order[]> => {
    const params = status ? { status } : {};
    const response = await api.get('/delivery/orders', { params });
    return response.data;
  },

  // Cập nhật trạng thái đơn hàng
  updateOrderStatus: async (orderId: number, data: UpdateStatusRequest): Promise<void> => {
    await api.put(`/delivery/orders/${orderId}/status`, data);
  },

  // Lấy lịch sử tracking của đơn hàng
  getTrackingHistory: async (orderId: number): Promise<TrackingEvent[]> => {
    const response = await api.get(`/delivery/orders/${orderId}/tracking`);
    return response.data;
  },

  // Lấy thống kê delivery
  getDeliveryStats: async (): Promise<{
    total: number;
    readyToShip: number;
    shipping: number;
    delivered: number;
    failed: number;
  }> => {
    const response = await api.get('/delivery/stats');
    return response.data;
  },

  // Lấy đơn hàng theo ID
  getOrderById: async (orderId: number): Promise<Order> => {
    const response = await api.get(`/delivery/orders/${orderId}`);
    return response.data;
  }
}; 