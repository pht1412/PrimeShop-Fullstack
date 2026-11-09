import api from './api';

export interface Order {
  orderId: number;
  fullName: string;
  phoneNumber: string;
  address: string;
  orderStatus: string;
  paymentType?: string;
  paymentStatus?: string;
  allowedNextStatuses?: string[];
  allowedDeliveryStatuses?: string[];
  estimatedDeliveryDate?: string;
  totalAmount?: number;
  orderDate?: string;
  trackingHistory?: TrackingEvent[];
}

export interface TrackingEvent {
  id: number;
  status: string;
  location: string | null;
  timestamp: string;
  note?: string | null;
}

export interface UpdateStatusRequest {
  status: string;
  location?: string;
  note?: string;
  paymentCollected?: boolean;
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
    inventory: number;
    readyToShip: number;
    shipped: number;
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