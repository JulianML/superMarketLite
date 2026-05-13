import { BrowserRouter, Routes, Route, Navigate, Outlet } from 'react-router-dom';
import { AuthProvider } from './context/AuthContext';
import { CartProvider } from './context/CartContext';
import { CheckoutProvider } from './context/CheckoutContext';
import PrivateRoute from './components/PrivateRoute';
import UserRoute from './components/UserRoute';
import Navbar from './components/Navbar';
import LoginPage from './pages/LoginPage';
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
import { BUSINESS_ID } from './api/client';

function AppLayout() {
  return (
    <div className="min-h-screen bg-gray-50">
      <Navbar />
      <main className="p-6 max-w-6xl mx-auto">
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
              <Route path="/login" element={<LoginPage />} />
              <Route element={<AppLayout />}>
                <Route path="/" element={<Navigate to="/catalog" replace />} />
                <Route path="/catalog" element={<CatalogPage />} />
                <Route path="/catalog/:id" element={<ProductDetailPage />} />
                <Route path="/cart" element={<CartPage />} />

                {/* Checkout flow — requires login */}
                <Route path="/checkout/address" element={
                  <UserRoute><CheckoutAddressPage /></UserRoute>
                } />
                <Route path="/checkout/summary" element={
                  <UserRoute><CheckoutSummaryPage /></UserRoute>
                } />
                <Route path="/checkout/confirmation" element={
                  <UserRoute><CheckoutConfirmationPage /></UserRoute>
                } />

                {/* Order history — requires login */}
                <Route path="/orders" element={
                  <UserRoute><OrdersPage /></UserRoute>
                } />
                <Route path="/orders/:id" element={
                  <UserRoute><OrderDetailPage /></UserRoute>
                } />

                <Route
                  path="/products"
                  element={
                    <PrivateRoute requiredBusinessId={BUSINESS_ID}>
                      <ProductsPage />
                    </PrivateRoute>
                  }
                />
                <Route
                  path="/inventory"
                  element={
                    <PrivateRoute requiredBusinessId={BUSINESS_ID}>
                      <InventoryPage />
                    </PrivateRoute>
                  }
                />
                <Route
                  path="/inventory/:productId/movements"
                  element={
                    <PrivateRoute requiredBusinessId={BUSINESS_ID}>
                      <MovementsPage />
                    </PrivateRoute>
                  }
                />
              </Route>
            </Routes>
          </BrowserRouter>
        </CheckoutProvider>
      </CartProvider>
    </AuthProvider>
  );
}
