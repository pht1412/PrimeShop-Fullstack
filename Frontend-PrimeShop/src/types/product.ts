<<<<<<< HEAD
import { components } from "./api-types";

// export type Product = components["schemas"]["Product"];
=======

export interface ProductCardType {
  slug: string;
  name: string;
  brand: string;
  price: number;
  discountPrice: number;
  isDiscounted: boolean;
  discountPercent: number;
  imageUrl: string;
  sold: number;
}
>>>>>>> tphat-unmerged

export interface Product {
  id: string;
  slug: string;
  name: string;
  price: number;
<<<<<<< HEAD
  originalPrice: number;
  discountPercent: number;
  images: string[];
  description: string;
  category: string;
  brand: string;
  // screenType: string;
  // screenSize: string;
  // storageOptions: string[];
  rating: number;
  stock: number;
  sold: number;
=======
  discountPrice: number;
  isDiscounted: boolean;
  discountPercent: number;
  imageUrl: string[];
  imageUrls: string[];
  description: string;
  category: string;
  brand: string;
  rating: number;
  stock: number;
  sold: number;
  specs: ProductSpecs[];
  seller: SellerProfile;
>>>>>>> tphat-unmerged
}

export interface ProductFilter {
  category?: string;
  brand?: string;
  minPrice?: number;
  maxPrice?: number;
}

<<<<<<< HEAD
export interface ProductSpec {
  name: string;
  value: string;
}
=======
export interface ProductSpecs {
  name: string;
  value: string;
}

export interface SellerProfile {
  id: string;
  shopName: string;
  identityCard: string;
  phone: string;
  description: string;
  status: String;
}
>>>>>>> tphat-unmerged
