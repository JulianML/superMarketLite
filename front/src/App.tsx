import { BrowserRouter, Routes, Route, Navigate, Outlet } from 'react-router-dom';
import { AuthProvider } from './context/AuthContext';
import { CartProvider } from './context/CartContext';
import { CheckoutProvider } from './context/CheckoutContext';
import UserRoute from './components/UserRoute';
import AdminLayout from './components/AdminLayout';
import Navbar from './components/Navbar';
import LoginPage from './pages/LoginPage';
import AdminLoginPage from './pages/AdminLoginPage';
import ProductsPage from './pages/ProductsPage';
import InventoryPage from './pages/InventoryPage';
import MovementsPage from './pages/MovementsPage';
import CatalogPage from './pages/CatalogPage';
import ProductDetailPage from './pages/ProductDetailPage';
import CartPage from './pages/CartPage';
import CheckoutAddressPage from './pages/CheckoutAddressPage';
import CheckoutSummaryPage from './pages/CheckoutSummaryPage';
import CheckoutConfirmationPage from './pages/CheckoutConfirmationPage';
import OrdersPage from './pages/OrdersPage';
import OrderDetailPage from './pages/OrderDetailPage';

function AppLayout() {
  return (
    <div className="min-h-screen bg-[#f5f5f5]">
      <Navbar />
      <main className="px-6 py-6 max-w-7xl mx-auto">
        <Outlet />
      </main>
    </div>
  );
}

export default function App() {
  return (
    <AuthProvider>
      <CartProvider>
        <CheckoutProvider>
          <BrowserRouter>
            <Routes>
              {/* Public login for buyers */}
              <Route path="/login" element={<LoginPage />} />

              {/* Admin section */}
              <Route path="/admin/login" element={<AdminLoginPage />} />
              <Route path="/admin" element={<AdminLayout />}>
                <Route index element={<Navigate to="products" replace />} />
                <Route path="products" element={<ProductsPage />} />
                <Route path="inventory" element={<InventoryPage />} />
                <Route path="inventory/:productId/movements" element={<MovementsPage />} />
              </Route>

              {/* Buyer-facing store */}
              <Route element={<AppLayout />}>
                <Route path="/" element={<Navigate to="/catalog" replace />} />
                <Route path="/catalog" element={<CatalogPage />} />
                <Route path="/catalog/:id" element={<ProductDetailPage />} />
                <Route path="/cart" element={<CartPage />} />

                <Route path="/checkout/address" element={
                  <UserRoute><CheckoutAddressPage /></UserRoute>
                } />
                <Route path="/checkout/summary" element={
                  <UserRoute><CheckoutSummaryPage /></UserRoute>
                } />
                <Route path="/checkout/confirmation" element={
                  <UserRoute><CheckoutConfirmationPage /></UserRoute>
                } />

                <Route path="/orders" element={
                  <UserRoute><OrdersPage /></UserRoute>
                } />
                <Route path="/orders/:id" element={
                  <UserRoute><OrderDetailPage /></UserRoute>
                } />
              </Route>
            </Routes>
          </BrowserRouter>
        </CheckoutProvider>
      </CartProvider>
    </AuthProvider>
  );
}
