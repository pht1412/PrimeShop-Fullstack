// src/routes/UserRoutes.jsx
import React from "react";
import { Routes, Route } from "react-router-dom";

import ProductPage from "../pages/product/ProductPage";
import HomePage from "../pages/HomePage";
import PageAbout from "../pages/AboutPage";
import PageCart from "../pages/cart/CartPage";
// import News from "../pages/news/News";
import QAPage from "../pages/QAPage";
import LoginPage from "../pages/auth/LoginPage";
import RegisterPage from "../pages/auth/RegisterPage";
import ForgotPasswordPage from "../pages/auth/ForgotPasswordPage";
import OrderTrackingPages from "../pages/OrderTrackingPage";
import AccountPage from "../pages/AccountPage";
import WishList from "../pages/WishListPage";
import ProductDetailPage from "../pages/product/ProductDetail";
// import NewsDetail from "../pages/news/NewsDetail";
import CheckoutPage from "../pages/checkout/CheckoutPage";
import NotFound from "../pages/NotFound";
import Header from "../components/header";
import Footer from "../components/footer";
import PaymentResult from "../pages/PaymentResult";
import News from "../pages/news/News";
import NewsDetail from "../pages/news/NewsDetail";
import MinigamePage from "../pages/games/MinigamePage";
import GameListPage from "../pages/games/GameListPage";
import C2CProduct from "../pages/productC2C/C2CProductPage";
import C2CProductDetailPage from "../pages/productC2C/C2CProductDetailPage";
// import Chatbot from "../components/chat/Chatbot";
import PrimeShopChat from "../components/chat/ChatC2C";
import ContactWidget from "../components/contact-widget/ContactWidget";
import PaymentSuccessPage from "../pages/PaymentSuccessPage";

const UserRoutes = () => {
  return (
    <div className="app-container">
      <Header />
      <Routes>
        <Route path="/" element={<HomePage />} />
        <Route path="/home" element={<HomePage />} />
        <Route path="/about" element={<PageAbout />} />
        <Route path="/cart" element={<PageCart />} />
        <Route path="/checkout" element={<CheckoutPage />} />
        <Route path="/all-products" element={<ProductPage />} />
        <Route path="/product-detail/:slug" element={<ProductDetailPage />} />
        <Route path="/news" element={<News />} />
        <Route path="/news/:id" element={<NewsDetail />} />
        <Route path="/faq" element={<QAPage />} />
        <Route path="/login" element={<LoginPage />} />
        <Route path="/register" element={<RegisterPage />} />
        <Route path="/order-tracking" element={<OrderTrackingPages />} />
        <Route path="/account" element={<AccountPage />} />
        <Route path="/forgot-password" element={<ForgotPasswordPage />} />
        <Route path="/wishlist" element={<WishList />} />
        <Route path="*" element={<NotFound />} />
        <Route path="/payment-result" element={<PaymentResult />} />
        <Route path="/minigame-list" element={<GameListPage />} />
        <Route path="/minigame" element={<MinigamePage />} />
        <Route path="/minigame/:gameId" element={<MinigamePage />} />
        <Route path="/c2c" element={<C2CProduct />} />
        <Route path="/c2c-product-detail/:id" element={<C2CProductDetailPage />} />
        <Route path="/payment/success" element={<PaymentSuccessPage />} />
      </Routes>
      <Footer />
      {/* <Chatbot /> */}
      <ContactWidget 
        onOpenChatC2C={() => console.log("Mở Chat C2C")} 
        onOpenChatBot={() => console.log("Mở Chat AI")} 
      />
    </div>
  );
};

export default UserRoutes;
