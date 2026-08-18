export interface CartItem {
  id: number;
  productSlug: string;
  productName: string;
  price: number;
  totalPrice: number;
  quantity: number;
  imageUrl: string;
}

export interface Cart {
  id: number;
  totalPrice: number;
  items: CartItem[];
}