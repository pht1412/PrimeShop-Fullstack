import React, { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import { mockCart } from '../../mocks/mockData';
import '../../assets/css/cart.css'; // Import CSS cho trang giỏ hàng
import api from '../../api/api';
import { Cart } from '../../types/cart';

const CartPage: React.FC = () => {
  // Dùng state để quản lý giỏ hàng động
  const [cart, setCart] = useState<Cart | null>(null);
  const [loading, setLoading] = useState(true);

  const fetchCart = async () => {
    try {
      const res = await api.get("/cart");
      setCart(res.data);
    } catch (err) {
      console.error("Lỗi khi tải giỏ hàng:", err);
    } finally {
      setLoading(false);
    }
  };

  const updateQuantity = async (productSlug: string, delta: number) => {
    try {
      await api.post("/cart/add", { productSlug, quantity: delta });
    } catch (err) {
      console.error("Không thể cập nhật số lượng", err);
    }
  }

  useEffect(() => {
    fetchCart();
    console.log("Giỏ hàng trả về:", cart);
  }, []);

  if (!cart) return <p>Đang tải giỏ hàng...</p>;

  // Logic tăng số lượng
  // const increaseQuantity = (productId: string) => {
  //   setCart((prevCart) =>
  //     prevCart.map((item) =>
  //       item.cartItems.productSlug === productId
  //         ? { ...item, quantity: item.cartItems.quantity + 1 }
  //         : item
  //     )
  //   );
  // };

  // const increaseQuantity = async (productSlug: string) => {
  //   try {
  //     const res = await api.post("/cart", {
  //       productSlug,
  //       quantity: 1
  //     });
  //     setCart(res.data)
  //   } catch (err) {
  //     console.error("Không thể tăng số lượng:", err);
  //   }
  // };

  // const decreaseQuantity = async (productSlug: string) => {
  //   try {
  //     const res = await api.post("/cart", {
  //       productSlug,
  //       quantity: -1
  //     });
  //     setCart(res.data);
  //   } catch (err) {
  //     console.error("Không thể giảm số lượng:", err);
  //   }
  // }

  // Logic giảm số lượng (không cho giảm dưới 1)
  // const decreaseQuantity = (productId: string) => {
  //   setCart((prevCart) =>
  //     prevCart.map((item) =>
  //       item.cartItems.productSlug === productId && item.cartItems.quantity > 1
  //         ? { ...item, quantity: item.cartItems.quantity - 1 }
  //         : item
  //     )
  //   );
  // };

  // Tính tổng tiền
  // const total = cart.reduce((sum, item) => sum + item.cartItems.price * item.cartItems.quantity, 0);

  return (
    <div className="cart-page">
      <h1>Giỏ hàng</h1>
      {cart?.cartItems.length === 0 ? (
        <p className="cart-empty">Giỏ hàng trống</p>
      ) : (
        <div className="cart-content">
          <div className="cart-items">
            {cart?.cartItems.map((item) => (
              <div key={item.id} className="cart-item">
                <img src={item.imageUrl} alt={item.productName} className="cart-item-image" />
                <div className="cart-item-details">
                  <h3 className="cart-item-name">{item.productName}</h3>
                  <p className="cart-item-price">
                    Đơn giá: {item.price.toLocaleString('vi-VN')} VND
                  </p>
                  <div className="cart-item-quantity">
                    <button
                      onClick={() => decreaseQuantity(item.productSlug)}
                      className="quantity-btn"
                    >
                      -
                    </button>
                    <span>{item.quantity}</span>
                    <button
                      onClick={() => increaseQuantity(item.productSlug)}
                      className="quantity-btn"
                    >
                      +
                    </button>
                  </div>
                  <p className="cart-item-subtotal">
                    Tổng: {(item.price * item.quantity).toLocaleString('vi-VN')} VND
                  </p>
                </div>
              </div>
            ))}
          </div>
          <div className="cart-summary">
            <h3 className="cart-total">
              Tổng tiền: {cart?.totalPrice.toLocaleString('vi-VN')} VND
            </h3>
            <Link to="/checkout" className="checkout-btn">
              Tiến hành thanh toán
            </Link>
          </div>
        </div>
      )}
    </div>
  );
  
};

export default CartPage;