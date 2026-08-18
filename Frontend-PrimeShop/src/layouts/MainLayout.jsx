// src/routes/UserRoutes.jsx
import React from "react";
import { Routes, Route } from "react-router-dom";

import ProductsPage from "../pages/product/ProductsPage";
import HomePage from "../pages/HomePage";
import PageAbout from "../pages/AboutPage";
import PageCart from "../pages/cart/CartPage";
import News from "../pages/News";
import QAPage from "../pages/QAPage";
import LoginPage from "../pages/auth/LoginPage";
import RegisterPage from "../pages/auth/RegisterPage";
import OrderTrackingPages from "../pages/OrderTrackingPage";
import AccountPage from "../pages/AccountPage";
import WishList from "../pages/WishListPage";
import ProductsDetailsPage from "../pages/product/ProductsDetails";
import Header from "../components/header";
import Footer from "../components/footer";

const UserRoutes = () => {
  return (
    <div className="app-container">
      <Header />
      <Routes>
        <Route path="/home" element={<HomePage />} />
        <Route path="/about" element={<PageAbout />} />
        <Route path="/cart" element={<PageCart />} />
        <Route path="/products" element={<ProductsPage />} />
        <Route path="/product-details/:id" element={<ProductsDetailsPage />} />
        <Route path="/news" element={<News />} />
        <Route path="/faq" element={<QAPage />} />
        <Route path="/login" element={<LoginPage />} />
        <Route path="/register" element={<RegisterPage />} />
        <Route path="/order-tracking" element={<OrderTrackingPages />} />
        <Route path="/account" element={<AccountPage />} />
        <Route path="/wishlist" element={<WishList />} />
      </Routes>
      <Footer />
    </div>
  );
};

export default UserRoutes;
