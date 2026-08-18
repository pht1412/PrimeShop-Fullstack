import { Routes, Route } from "react-router-dom";
import AdminLayout from "../layouts/AdminLayout";
import Dashboard from "../pages/admin/DashBoard";
import OrderManager from "../pages/admin/OrderManager";
import ProductManager from "../pages/admin/ProductManager";
import UserManager from "../pages/admin/UserManager";
import AdminRoute from "../components/AdminRoute";
import CategoryManager from "../pages/admin/CategoryManager";
import NewsManager from "../pages/admin/NewsManager";
import VouchersManager from "../pages/admin/VouchersManager";
import WalletManager from "../pages/admin/WalletsManager";
import C2CManagement from "../pages/admin/C2CManagement";
import RevenueManager from "../pages/admin/RevenueManager";
import ImportTransactionManager from "../pages/admin/ImportTransactionManager";
import BusinessManager from "../pages/admin/BusinessManager";

const AdminRoutes = () => {
  return (
    <Routes>
      <Route
        path="dashboard"
        element={
          <AdminRoute>
            <AdminLayout>
              <Dashboard />
            </AdminLayout>
          </AdminRoute>
        }
      />
      <Route
        path="orders"
        element={
          <AdminRoute>
            <AdminLayout>
              <OrderManager />
            </AdminLayout>
          </AdminRoute>
        }
      />
      <Route
        path="products"
        element={
          <AdminRoute>
            <AdminLayout>
              <ProductManager />
            </AdminLayout>
          </AdminRoute>
        }
      />
      <Route
        path="users"
        element={
          <AdminRoute>
            <AdminLayout>
              <UserManager />
            </AdminLayout>
          </AdminRoute>
        }
      />
      <Route
        path="categories"
        element={
          <AdminRoute>
            <AdminLayout>
              <CategoryManager />
            </AdminLayout>
          </AdminRoute>
        }
      />

      <Route
        path="news"
        element={
          <AdminRoute>
            <AdminLayout>
              <NewsManager />
            </AdminLayout>
          </AdminRoute>
        }
      />

      <Route
        path="vouchers"
        element={
          <AdminRoute>
            <AdminLayout>
              <VouchersManager />
            </AdminLayout>
          </AdminRoute>
        }
      />
      <Route
        path="wallets"
        element={
          <AdminRoute>
            <AdminLayout>
              <WalletManager />
            </AdminLayout>
          </AdminRoute>
        }
      />
      <Route
        path="c2c-management"
        element={
          <AdminRoute>
            <AdminLayout>
              <C2CManagement />
            </AdminLayout>
          </AdminRoute>
        }
      />
      <Route  
        path="revenues"
        element={
          <AdminRoute>
            <AdminLayout>
              <RevenueManager />
            </AdminLayout>
          </AdminRoute>
        }
      />
      <Route
        path="import-transactions"
        element={
          <AdminRoute>
            <AdminLayout>
              <ImportTransactionManager />
            </AdminLayout>
          </AdminRoute>
        }
      />
      <Route
        path="business-manager"
        element={
          <AdminRoute>
            <AdminLayout>
              <BusinessManager />
            </AdminLayout>
          </AdminRoute>
        }
      />
    </Routes>
  );
};

export default AdminRoutes;
