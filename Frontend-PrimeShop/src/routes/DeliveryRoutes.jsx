import { Routes, Route } from "react-router-dom";
import DeliveryLayout from "../layouts/DeliveryLayout";
import DeliveryDashboard from "../pages/delivery/DeliveryDashboard";

const DeliveryRoutes = () => {
  return (
    <Routes>
      <Route path="/" element={<DeliveryLayout />}>
        <Route index element={<DeliveryDashboard />} />
        <Route path="dashboard" element={<DeliveryDashboard />} />
        <Route path="orders" element={<DeliveryDashboard />} />
        <Route path="history" element={<DeliveryDashboard />} />
      </Route>
    </Routes>
  );
};

export default DeliveryRoutes;