import { BrowserRouter, Routes, Route, Navigate, Outlet } from 'react-router-dom';
import { AuthProvider } from './context/AuthContext';
import { CartProvider } from './context/CartContext';
import PrivateRoute from './components/PrivateRoute';
import Navbar from './components/Navbar';
import LoginPage from './pages/LoginPage';
import ProductsPage from './pages/ProductsPage';
import InventoryPage from './pages/InventoryPage';
import MovementsPage from './pages/MovementsPage';
import CatalogPage from './pages/CatalogPage';
import ProductDetailPage from './pages/ProductDetailPage';
import CartPage from './pages/CartPage';
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
      <BrowserRouter>
        <Routes>
          <Route path="/login" element={<LoginPage />} />
          <Route element={<AppLayout />}>
            <Route path="/" element={<Navigate to="/catalog" replace />} />
            <Route path="/catalog" element={<CatalogPage />} />
            <Route path="/catalog/:id" element={<ProductDetailPage />} />
            <Route path="/cart" element={<CartPage />} />
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
      </CartProvider>
    </AuthProvider>
  );
}
