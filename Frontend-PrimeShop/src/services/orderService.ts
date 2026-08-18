import { OrderTrackingData } from './order';

const useMock = true; // Đặt thành false khi BE sẵn sàng

export const fetchOrderTracking = async (orderId: string): Promise<OrderTrackingData> => {
  if (useMock) {
    // Mock data
    const mockData: OrderTrackingData = {
      orderId,
      status: "Đang giao",
      steps: [
        { id: "1", title: "Đã đặt hàng", date: "05/04/2025 10:00", completed: true },
        { id: "2", title: "Đang xử lý", date: "05/04/2025 12:00", completed: true },
        { id: "3", title: "Đang giao", date: "05/04/2025 14:00", completed: true },
        { id: "4", title: "Đã giao", date: "", completed: false },
      ],
      details: {
        product: "Máy tính xách tay XYZ",
        quantity: 1,
        total: "25.000.000 VNĐ",
        address: "123 Đường Công Nghệ, TP.HCM",
      },
    };
    if (!orderId) throw new Error("Vui lòng nhập mã đơn hàng");
    return mockData;
  }

  // Gọi API thật
  const response = await fetch(`/api/order-tracking/${orderId}`);
  if (!response.ok) throw new Error("Không tìm thấy đơn hàng");
  return response.json();
};