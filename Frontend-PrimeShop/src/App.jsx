import { BrowserRouter as Router, Routes, Route } from "react-router-dom";
import AdminRoutes from "./routes/AdminRoutes";
import UserRoutes from "./routes/UserRoutes";
import DeliveryRoutes from "./routes/DeliveryRoutes";
import { AuthProvider } from "./context/AuthContext";
import { CartProvider } from "./context/CartContext";
import NotFound from "./pages/NotFound";

const App = () => {
  return (
    <AuthProvider>
      <CartProvider>
        <Router>
          <Routes>
            {/* 👨‍💼 Admin luôn đặt trước */}
            <Route path="/admin/*" element={<AdminRoutes />} />

            {/* 👷 Delivery */}
            <Route path="/delivery-dashboard/*" element={<DeliveryRoutes />} />

            {/* 👥 User */}
            <Route path="/*" element={<UserRoutes />} />

            {/* 404 Page */}
            <Route path="*" element={<NotFound />} />
          </Routes>
        </Router>
      </CartProvider>
    </AuthProvider>
  );
};

export default App;
