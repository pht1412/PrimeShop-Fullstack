// Vị trí: src/mocks/mockDataC2C.ts

/**
 * Định nghĩa các trạng thái của tin đăng C2C.
 * - 'active': Đang hiển thị, người khác có thể thấy.
 * - 'sold': Đã bán (để hiển thị "ĐÃ BÁN" mờ đi).
 * - 'pending': Chờ duyệt (nếu chúng ta có cơ chế duyệt tin).
 * - 'hidden': Người bán tự ẩn tin.
 */
// Sửa dòng này
export type C2CProductStatus = 'active' | 'sold' | 'pending' | 'hidden' | 'rejected';
/**
 * Định nghĩa tình trạng sản phẩm. Rất quan trọng cho C2C (BẮT BUỘC).
 * - 'new': Mới 100%, chưa qua sử dụng.
 * - 'like_new': Như mới (đã bóc seal, dùng lướt 99%).
 * - 'used': Đã qua sử dụng (hoạt động tốt).
 * - 'for_parts': Bán xác (hỏng, cho linh kiện).
 */
export type C2CProductCondition = 'new' | 'like_new' | 'used' | 'for_parts';

/**
 * Interface (giao diện) chính cho một sản phẩm C2C.
 * Đã "lai" giữa schema [dbo].[product] sếp gửi và các trường C2C-essential.
 */
export interface C2CProduct {
  id: string; // [id]
  name: string; // [name] (Thay cho 'title' cũ)
  description: string; // [description]
  price: number; // [price]
  brand: string | null; // [brand] (Có thể null nếu là hàng "handmade"
  
  /**
   * [UX NOTE]: Trường này BẮT BUỘC là array.
   * Schema sếp gửi là [image_url] (1 ảnh), nhưng C2C
   * yêu cầu nhiều ảnh (ảnh thật, ảnh lỗi) để build trust.
   * Em sẽ gọi đây là 'images' (số nhiều).
   */
  images: string[]; 
  
  category_id: string; // [category_id] (Giả lập dùng string)
  created_at: string; // [created_at]

  // --- Các trường C2C-essential (Bắt buộc phải có) ---
  
  /**
   * [UX NOTE]: Trạng thái tin đăng, rất quan trọng.
   * Dùng 'active' (boolean) của sếp sẽ không đủ,
   * cần biết tin 'Đã bán', 'Đang chờ',...
   */
  status: C2CProductStatus; 
  
  /**
   * [UX NOTE]: Tình trạng sản phẩm. Đây là trái tim của C2C.
   */
  condition: C2CProductCondition;
  
  /**
   * [UX NOTE]: Vị trí. User C2C luôn muốn biết
   * người bán ở đâu để qua xem/lấy hàng.
   */
  location: string;
  
  /**
   * Dùng để biết tin đăng này của ai.
   * (Sẽ map với [user_id] từ bảng seller_profiles)
   */
  sellerId: string; 
}

// === DỮ LIỆU GIẢ LẬP ===

// Giả sử user đang đăng nhập có ID là 'user_123'
const CURRENT_USER_ID = 'user_123';

export const mockC2CProducts: C2CProduct[] = [
  {
    id: 'c2c-001',
    name: 'iPhone 13 Pro Max 256GB Xanh Sierra', // Dùng 'name'
    description: 'Máy dùng kỹ, còn mới 99%. Pin 92%. Fullbox, tặng kèm ốp lưng. Chỉ giao dịch trực tiếp tại TP.HCM.',
    price: 17500000,
    brand: 'Apple', // Thêm 'brand'
    images: [ // 'images' (array)
      'https://cdn.tgdd.vn/Products/Images/42/250280/iphone-13-pro-max-sierra-blue-600x600.jpg',
      'https://cdn.tgdd.vn/Products/Images/42/250280/iphone-13-pro-max-sierra-blue-1-600x600.jpg',
      'https://cdn.tgdd.vn/Products/Images/42/250280/iphone-13-pro-max-sierra-blue-2-600x600.jpg',
    ],
    category_id: 'dien-thoai', // Thêm 'category_id'
    created_at: '2025-10-28T10:30:00Z',
    
    // C2C-essential fields
    status: 'active',
    condition: 'like_new',
    location: 'Quận 1, TP.HCM',
    sellerId: CURRENT_USER_ID,
  },
  {
    id: 'c2c-002',
    name: 'Sách "Lập trình React" - NXB Khoa Học', // Dùng 'name'
    description: 'Sách đã đọc 1 lần, còn rất mới, không quăn góc. Pass lại cho bạn nào đam mê React.',
    price: 120000,
    brand: null, // Sách không có brand
    images: [
      'https://salt.tikicdn.com/cache/w1200/ts/product/5e/2f/a1/bce0f0805c8d0c273a7102e3b97b055d.png',
    ],
    category_id: 'sach', // Thêm 'category_id'
    created_at: '2025-10-27T15:00:00Z',
    
    // C2C-essential fields
    status: 'active',
    condition: 'used',
    location: 'Quận Cầu Giấy, Hà Nội',
    sellerId: CURRENT_USER_ID,
  },
  {
    id: 'c2c-003',
    name: 'Bàn phím cơ Keychron Q1 (Đã bán)', // Dùng 'name'
    description: 'Bàn phím đã mod, switch Akko. Gõ rất sướng tay. Đã bán cho một bạn nhiệt tình.',
    price: 2800000,
    brand: 'Keychron', // Thêm 'brand'
    images: [
      'https://file.hstatic.net/1000171032/file/keychron-q1- knob-chinh-hang_730f2ee60d974e649033327d92c7300c.png',
    ],
    category_id: 'phu-kien-pc', // Thêm 'category_id'
    created_at: '2025-10-26T11:20:00Z',
    
    // C2C-essential fields
    status: 'sold',
    condition: 'like_new',
    location: 'Quận 3, TP.HCM',
    sellerId: CURRENT_USER_ID,
  },
  {
    id: 'c2c-004',
    name: 'Máy ảnh Sony A6000 (Của người khác)', // Dùng 'name'
    description: 'Máy ảnh mirrorless quốc dân, không phải của user 123. Lens kit 16-50mm. Đầy đủ pin sạc.',
    price: 6000000,
    brand: 'Sony', // Thêm 'brand'
    images: ['https://hoanghamobile.com/tin-tuc/wp-content/uploads/2022/08/may-anh-sony-a6000-1.jpg'],
    category_id: 'may-anh', // Thêm 'category_id'
    created_at: '2025-10-25T09:00:00Z',
    
    // C2C-essential fields
    status: 'active',
    condition: 'used',
    location: 'Quận Hải Châu, Đà Nẵng',
    sellerId: 'user_456', // ID người bán khác
  },
  // --- BỔ SUNG 6 SẢN PHẨM MỚI ---
  {
    id: 'c2c-005',
    name: 'Tai nghe Sony WH-1000XM4 (Like New)',
    description: 'Tai nghe chống ồn đỉnh cao. Mua tại CellphoneS còn bảo hành 3 tháng. Fullbox không thiếu gì.',
    price: 4500000,
    brand: 'Sony',
    images: [
      '/c2c-images/thao(1).jpg',
      '/c2c-images/thao(2).jpg'
    ],
    category_id: 'am-thanh',
    created_at: '2025-10-29T08:00:00Z',
    status: 'active',
    condition: 'like_new',
    location: 'Quận 10, TP.HCM',
    sellerId: 'user_789',
  },
  {
    id: 'c2c-006',
    name: 'VGA RTX 3070 Gigabyte (Đã dùng 1 năm)',
    description: 'Card còn khỏe, chiến game mượt. Chỉ dùng chơi game, không trâu cày. Hết bảo hành. Bao test 3 ngày.',
    price: 7000000,
    brand: 'Gigabyte',
    images: [
      'https://cdn.tgdd.vn/Products/Images/42/250280/iphone-13-pro-max-sierra-blue-600x600.jpg', // Tái sử dụng ảnh
    ],
    category_id: 'linh-kien-pc',
    created_at: '2025-10-29T11:00:00Z',
    status: 'active',
    condition: 'used',
    location: 'Quận Hoàng Mai, Hà Nội',
    sellerId: CURRENT_USER_ID, // Thêm 1 cái của user hiện tại
  },
  {
    id: 'c2c-007',
    name: 'Giày Adidas Ultraboost 22 (Mới 100%) - CHỜ DUYỆT',
    description: 'Được tặng sinh nhật nhưng không vừa size. Mới 100% fullbox, size 42. Hàng real bao check.',
    price: 2500000,
    brand: 'Adidas',
    images: [
      '/c2c-images/nguyenhuynhthanhthao.jpg',
    ],
    category_id: 'thoi-trang-nam',
    created_at: '2025-10-30T14:15:00Z',
    status: 'pending', // Trạng thái Chờ duyệt
    condition: 'new',
    location: 'Quận 5, TP.HCM',
    sellerId: CURRENT_USER_ID,
  },
  {
    id: 'c2c-008',
    name: 'Bán xác Mainboard B450 Tomahawk (Cho thợ)',
    description: 'Main bị chập nguồn, không lên. Bán cho anh em thợ về lấy linh kiện. Không bao test.',
    price: 200000,
    brand: 'MSI',
    images: [
      '/c2c-images/thanhthao.jpg'
    ],
    category_id: 'linh-kien-pc',
    created_at: '2025-10-28T18:00:00Z',
    status: 'active',
    condition: 'for_parts', // Bán xác
    location: 'Quận Thanh Xuân, Hà Nội',
    sellerId: 'user_abc',
  },
  {
    id: 'c2c-009',
    name: 'Bàn làm việc gỗ thông 1m2 (pass lại)',
    description: 'Do chuyển nhà nên pass lại bàn làm việc còn tốt. Kích thước 1m2 x 60cm. Chân sắt chắc chắn.',
    price: 800000,
    brand: null,
    images: [
      '/c2c-images/nguyenhuynhthanhthao.jpg', // Ảnh local giả lập
    ],
    category_id: 'noi-that',
    created_at: '2025-10-29T16:30:00Z',
    status: 'active',
    condition: 'used',
    location: 'TP. Thủ Đức, TP.HCM',
    sellerId: 'user_789',
  },
  {
    id: 'c2c-010',
    name: 'Nintendo Switch Oled (Tạm ẩn)',
    description: 'Máy mới 99%, ít chơi. Đã hack. Full phụ kiện. Do bận đi làm nên tạm ẩn, qua tuần bán tiếp.',
    price: 5500000,
    brand: 'Nintendo',
    images: [
      '/c2c-images/nguyenhuynhthanhthao.jpg', // Tái sử dụng ảnh
    ],
    category_id: 'may-choi-game',
    created_at: '2025-10-20T10:00:00Z',
    status: 'hidden', // Trạng thái Ẩn
    condition: 'like_new',
    location: 'Quận 1, TP.HCM',
    sellerId: CURRENT_USER_ID,
  }
];

/**
 * Hàm giả lập gọi API để lấy *chỉ* các sản phẩm C2C của user hiện tại.
 * @param userId ID của user đang đăng nhập
 * @returns Promise chứa danh sách sản phẩm C2C của user đó
 */
export const fetchMyC2CProducts = (userId: string): Promise<C2CProduct[]> => {
  console.log(`[Mock API] Fetching C2C products for user: ${userId}`);
  return new Promise((resolve) => {
    setTimeout(() => {
      const myProducts = mockC2CProducts.filter(
        (product) => product.sellerId === userId
      );
      resolve(myProducts);
    }, 500); // Giả lập độ trễ mạng 500ms
  });
};

// --- BỔ SUNG CÁC HÀM API MỚI ---

/**
 * BỔ SUNG: Hàm giả lập gọi API lấy TẤT CẢ sản phẩm C2C
 * (Chỉ lấy các tin đang 'active' để hiển thị công khai)
 * @returns Promise chứa danh sách TẤT CẢ sản phẩm C2C đang active
 */
export const fetchAllC2CProducts = (): Promise<C2CProduct[]> => {
  console.log("[Mock API] Fetching ALL active C2C products");
  return new Promise((resolve) => {
    setTimeout(() => {
      const activeProducts = mockC2CProducts.filter(
        (product) => product.status === 'active'
      );
      resolve(activeProducts);
    }, 700); // Giả lập mạng chậm hơn
  });
};

/**
 * BỔ SUNG: Hàm giả lập gọi API lấy MỘT sản phẩm C2C theo ID
 * @param productId ID của sản phẩm cần lấy
 * @returns Promise chứa sản phẩm đó, hoặc undefined nếu không tìm thấy
 */
export const fetchC2CProductById = (productId: string): Promise<C2CProduct | undefined> => {
  console.log(`[Mock API] Fetching C2C product by ID: ${productId}`);
  return new Promise((resolve) => {
    setTimeout(() => {
      const product = mockC2CProducts.find(
        (p) => p.id === productId
      );
      resolve(product);
    }, 300); // Giả lập mạng nhanh
  });
};

// ... (Sau hàm fetchC2CProductById)

/**
 * BỔ SUNG: Hàm giả lập gọi API lấy CHỈ các sản phẩm C2C đang "chờ duyệt"
 * @returns Promise chứa danh sách sản phẩm C2C có status 'pending'
 */
export const fetchPendingC2CProducts = (): Promise<C2CProduct[]> => {
  console.log("[Mock API] Fetching ALL PENDING C2C products for Admin");
  return new Promise((resolve) => {
    setTimeout(() => {
      const pendingProducts = mockC2CProducts.filter(
        (product) => product.status === 'pending'
      );
      resolve(pendingProducts);
    }, 500);
  });
};

/**
 * BỔ SUNG: Hàm giả lập "Duyệt" tin
 * @param productId ID của tin cần duyệt
 */
export const mockApproveC2CProduct = (productId: string): Promise<boolean> => {
  console.log(`[Mock API] Approving product ID: ${productId}`);
  return new Promise((resolve) => {
    setTimeout(() => {
      const index = mockC2CProducts.findIndex(p => p.id === productId);
      if (index !== -1) {
        mockC2CProducts[index].status = 'active'; // Chuyển sang 'active'
      }
      resolve(true);
    }, 300);
  });
};

/**
 * BỔ SUNG: Hàm giả lập "Từ chối" tin
 * @param productId ID của tin cần từ chối
 * @param reason Lý do từ chối
 */
export const mockRejectC2CProduct = (productId: string, reason: string): Promise<boolean> => {
  console.log(`[Mock API] Rejecting product ID: ${productId} with REASON: ${reason}`);
  return new Promise((resolve) => {
    setTimeout(() => {
      const index = mockC2CProducts.findIndex(p => p.id === productId);
      if (index !== -1) {
        mockC2CProducts[index].status = 'rejected'; // Chuyển sang 'rejected'
      }
      // Tương lai: Gửi lý do này về cho user (ví dụ: tạo 1 'notification')
      resolve(true);
    }, 300);
  });
};