import { create } from 'zustand';
import { CartItem } from '../types/cart';

type CartState = {
  cart: CartItem[];
  addToCart: (product: CartItem) => void;
  increaseQuantity: (productId: string) => void;
  decreaseQuantity: (productId: string) => void;
};

export const useCartStore = create<CartState>((set) => ({
  cart: [],
  addToCart: (product) =>
    set((state) => {
      const existingItem = state.cart.find((item) => item.productId === product.productId);
      if (existingItem) {
        return {
          cart: state.cart.map((item) =>
            item.productId === product.productId
              ? { ...item, quantity: item.quantity + 1 }
              : item
          ),
        };
      }
      return { cart: [...state.cart, { ...product, quantity: 1 }] };
    }),
  increaseQuantity: (productId) =>
    set((state) => ({
      cart: state.cart.map((item) =>
        item.productId === productId ? { ...item, quantity: item.quantity + 1 } : item
      ),
    })),
  decreaseQuantity: (productId) =>
    set((state) => ({
      cart: state.cart.map((item) =>
        item.productId === productId && item.quantity > 1
          ? { ...item, quantity: item.quantity - 1 }
          : item
      ),
    })),
}));