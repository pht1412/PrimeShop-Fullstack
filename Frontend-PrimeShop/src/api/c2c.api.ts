import api from "./api";
export interface C2CProductResponse {
  id: number;
  name: string;
  slug: string;
  brand: string | null;
  price: number;
  imageUrl: string;
  images: string[];
  stock: number;
  category: string;
  sellerId: number;
  shopName: string;
  description: string;
  owner: boolean;
  status: "PENDING" | "APPROVED" | "REJECTED" | "DISABLED";
  condition?: "new" | "like_new" | "used" | "for_parts";
  location?: string;
}

export const getC2CProducts = async (): Promise<C2CProductResponse[]> => {
  const response = await api.get<C2CProductResponse[]>("/product/c2c");
  return response.data;
};

export const getC2CProduct = async (productId: string): Promise<C2CProductResponse> => {
  const response = await api.get<C2CProductResponse>(`/product/c2c/${productId}`);
  return response.data;
};
