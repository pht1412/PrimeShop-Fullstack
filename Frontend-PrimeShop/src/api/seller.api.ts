// Vị trí: src/api/seller.api.ts
import api from './api';
import { ISellerRequest, ISellerProfile, IProductRequest } from '../types/seller';
import { IPage, IProductCardResponse } from '../types/seller';

/**
 * [LUỒNG 1] "Xin" làm Seller - gửi đơn đăng ký
 */
export const applyForSeller = (request: ISellerRequest) => {
  return api.post<ISellerProfile>('/seller/apply', request);
};

/**
 * [LUỒNG 2.1] Lấy hồ sơ Seller của chính mình (alias)
 */
export const getMySellerProfile = () => {
  return api.get<ISellerProfile>('/seller/me'); // ✅ ensure leading '/'
};

/**
 * [LUỒNG 2.3] Thêm sản phẩm
 */
export const addProduct = (request: IProductRequest, sellerId: number) => {
  return api.post('/seller/add-product', request, {
    params: { sellerId }
  });
};

/**
 * [LUỒNG 2.4] Cập nhật sản phẩm
 */
export const updateProduct = (productId: number, request: IProductRequest) => {
  return api.patch('/seller/update-product', request, { // ✅ ensure leading '/'
    params: { id: productId }
  });
};

/**
 * [LUỒNG 2.5] Hồ sơ Business (chính thức, alias của getMySellerProfile)
 */
export const getMyBusinessProfile = () => {
  return api.get<ISellerProfile>('/seller/me'); // ✅ ensure leading '/'
};

/**
 * [LUỒNG 2.2] Lấy danh sách sản phẩm theo sellerId
 */
export const getSellerProducts = (sellerId: number, page: number = 0, size: number = 10) => {
  return api.get<any>('/seller/products', {
    params: {
      sellerId,
      page,
      size,
      sort: 'createdAt,desc',
    }
  });
};

// ==========================================
// KHU VỰC ADMIN (Mới bổ sung)
// ==========================================

/**
 * [ADMIN] Lấy danh sách Seller đang chờ duyệt (Pending)
 */
export const adminGetPendingSellers = () => {
    return api.get<ISellerProfile[]>('/admin/seller/pending-registrations');
};

/**
 * [ADMIN] Duyệt đơn đăng ký Seller
 * @param sellerId 
 */
export const adminApproveSeller = (sellerId: number) => {
    return api.patch(`/admin/seller/approve-registration`, null, {
        params: { sellerId }
    });
};

/**
 * [ADMIN] Chặn (Ban) Seller
 * @param sellerId 
 */
export const adminBanSeller = (sellerId: number) => {
    return api.patch(`/admin/seller/ban-seller`, null, {
        params: { sellerId }
    });
};

/**
 * [ADMIN] Lấy danh sách Sản phẩm đang chờ duyệt (Pending)
 * Đã xây dựng ở bước trước: /api/admin/seller/pending-products
 */
export const adminGetPendingProducts = () => {
    return api.get<IProductCardResponse[]>('/admin/seller/pending-products');
};

/**
 * [ADMIN] Duyệt sản phẩm
 */
export const adminApproveProduct = (sellerId: number, productId: number) => {
    return api.patch(`/admin/seller/approve-products`, null, {
        params: { sellerId, productId }
    });
};

/**
 * [ADMIN] Từ chối sản phẩm (Reject)
 */
export const adminRejectProduct = (sellerId: number, productId: number) => {
    return api.patch(`/admin/seller/reject-product`, null, {
        params: { sellerId, productId }
    });
};

export const adminGetAllSellers = () => {
    return api.get<ISellerProfile[]>('/admin/seller/all');
};