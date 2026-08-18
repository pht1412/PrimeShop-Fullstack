import React, { createContext, useContext, useEffect, useState } from 'react';
import api from '../api/api';

interface CartContextType {
  cartItemCount: number;
  updateCartCount: () => Promise<void>;
}

export const CartContext = createContext<CartContextType | null>(null);

export const CartProvider = ({ children }) => {
  const [cartItemCount, setCartItemCount] = useState(0);

  const updateCartCount = async () => {
    try {
      const token = localStorage.getItem('token');
      if (!token) {
        setCartItemCount(0);
        return;
      }

      const response = await api.get('/cart/count');
      const { cartItemCount: count } = response.data;
      setCartItemCount(count);
    } catch (error) {
      console.error('Error fetching cart count:', error);
      setCartItemCount(0);
    }
  };

  // Update cart count when component mounts and when token changes
  useEffect(() => {
    const token = localStorage.getItem('token');
    if (token) {
      updateCartCount();
    } else {
      setCartItemCount(0);
    }
  }, [localStorage.getItem('token')]);

  return (
    <CartContext.Provider value={{ cartItemCount, updateCartCount }}>
      {children}
    </CartContext.Provider>
  );
};

export const useCart = (CartContext: any) => {
  const context = useContext(CartContext);
  if (!context) {
    throw new Error('useCart must be used within a CartProvider');
  }
  return context;
};
