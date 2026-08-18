export interface OrderItem {
  productId: string;
  productName: string;
  quantity: number;
  totalPrice: number;
}

export interface Order {
  orderId: string;
  totalAmount: number; // Tổng tiền hàng (Subtotal)
  finalAmount: number; // Tổng thanh toán thực tế (Sau Voucher + Ship)
  orderStatus: string;
  createdAt: string;
  fullName: string;
  phoneNumber: string;
  address: string;
  note: string;
  items: OrderItem[];
  admin: boolean;
  startDate?: string;
  endDate?: string;
}