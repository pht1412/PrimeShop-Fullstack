export interface OrderStep {
  id: string | number; // Thêm thuộc tính id (kiểu tùy thuộc vào dữ liệu của bạn)
  title: string;
  date?: string; // Optional vì có thể không có ngày
  completed: boolean;
}

export interface OrderDetails { 
  product: string;
  quantity: number;
  total: string; // Hoặc number, tùy thuộc vào dữ liệu
  address: string;
}
  
  export interface OrderTrackingData {
    orderId: string;
    status: string;
    steps: OrderStep[];
    details: OrderDetails;
  }