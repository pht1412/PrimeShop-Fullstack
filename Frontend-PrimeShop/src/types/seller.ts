// Vị trí: src/types/seller.ts
// (Em đã xóa IProductCardResponse bị trùng và sửa lại cái xịn)

export type ProductStatus = 'PENDING' | 'APPROVED' | 'REJECTED' | 'DISABLED';

// ... (ISellerProfile giữ nguyên) ...
export interface ISellerRequest {
  shopName: string;
  phone: string;           // ✅ NEW
  identityCard: string;    // ✅ NEW (CCCD/CMND)
  description: string;
  address?: string;        // optional
}

export interface ISellerProfile {
  id: number;
  shopName: string;
  identityCard: string;
  description: string;
  phone: string;
  status: 'PENDING_REVIEW' | 'VERIFIED_SELLER' | 'BANNED_SELLER';
  // Bổ sung optional user info
  user?: {
      id?: number;
      username: string;
      email?: string;
  };
}


// ✅ SỬA LỖI 1: ĐÂY LÀ INTERFACE "XỊN" DUY NHẤT
// (ĐÃ BỔ SUNG categoryId ĐỂ FIX LỖI)
export interface IProductCardResponse {
  id: number;
  name: string;
  slug: string;
  brand: string | null;
  price: number;
  discountPercent: number;
  discountPrice: number;
  isDiscounted: boolean;
  rating: number;
  imageUrl: string;
  sold: number;
  category: string; 
  sellerId: number;
  shopName: string;
  stock: number;
  status: ProductStatus; 
  description?: string;
  condition?: 'new' | 'like_new' | 'used' | 'for_parts';
  location?: string;
  
  // ▼▼▼ BỔ SUNG TRƯỜNG NÀY ĐỂ FIX LỖI TRONG C2CTab.tsx ▼▼▼
  categoryId: number; 
}

// ❌ EM ĐÃ XÓA KHỐI "IProductCardResponse" BỊ TRÙNG Ở ĐÂY ❌


// ... (IPage, IProductRequest giữ nguyên) ...
export interface IPage<T> {
  content: T[];
  totalPages: number;
  totalElements: number;
  size: number;
  number: number; // trang hiện tại
}

export interface IProductRequest {
  name: string;
  description: string;
  price: number;
  brand: string | null;
  imageUrl: string; 
  stock: number;
  categoryId: number; 
  specs: {
    name: string;
    value: string;
  }[];
}