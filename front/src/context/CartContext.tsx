import { createContext, useContext, useState, useEffect, useCallback, type ReactNode } from 'react';
import { cartApi, type CartDTO } from '../api/cart';
import { useAuth } from './AuthContext';

interface CartContextType {
  cart: CartDTO | null;
  loading: boolean;
  error: string | null;
  itemCount: number;
  addItem: (productId: number, quantity: number) => Promise<void>;
  updateItem: (productId: number, quantity: number) => Promise<void>;
  removeItem: (productId: number) => Promise<void>;
  clearCart: () => Promise<void>;
  refreshCart: () => Promise<void>;
}

const CartContext = createContext<CartContextType | null>(null);

export function CartProvider({ children }: { children: ReactNode }) {
  const { user } = useAuth();
  const [cart, setCart] = useState<CartDTO | null>(null);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);

  const refreshCart = useCallback(async () => {
    setLoading(true);
    setError(null);
    try {
      setCart(await cartApi.get());
    } catch (e) {
      setError(String(e));
    } finally {
      setLoading(false);
    }
  }, []);

  // Re-fetch when auth changes: covers initial load, post-login merge, and logout
  useEffect(() => {
    refreshCart();
  }, [user, refreshCart]);

  const addItem = async (productId: number, quantity: number) => {
    setCart(await cartApi.addItem(productId, quantity));
  };

  const updateItem = async (productId: number, quantity: number) => {
    setCart(await cartApi.updateItem(productId, quantity));
  };

  const removeItem = async (productId: number) => {
    setCart(await cartApi.removeItem(productId));
  };

  const clearCart = async () => {
    await cartApi.clear();
    setCart(prev => prev ? { ...prev, items: [], total: 0 } : null);
  };

  const itemCount = cart?.items.reduce((sum, item) => sum + item.quantity, 0) ?? 0;

  return (
    <CartContext.Provider value={{ cart, loading, error, itemCount, addItem, updateItem, removeItem, clearCart, refreshCart }}>
      {children}
    </CartContext.Provider>
  );
}

export function useCart() {
  const ctx = useContext(CartContext);
  if (!ctx) throw new Error('useCart must be used inside CartProvider');
  return ctx;
}
