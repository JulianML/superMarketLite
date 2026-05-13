import { useState, useEffect } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { useCart } from '../context/CartContext';
import { useAuth } from '../context/AuthContext';
import type { CartItemDTO } from '../api/cart';

function CartItemRow({
  item,
  onUpdate,
  onRemove,
}: {
  item: CartItemDTO;
  onUpdate: (productId: number, qty: number) => Promise<void>;
  onRemove: (productId: number) => Promise<void>;
}) {
  const [qty, setQty] = useState(item.quantity);
  const [saving, setSaving] = useState(false);
  const [removing, setRemoving] = useState(false);

  // Sync when the cart refreshes from server
  useEffect(() => setQty(item.quantity), [item.quantity]);

  const commit = async () => {
    const clamped = Math.min(99, Math.max(1, qty));
    if (clamped === item.quantity) return;
    setSaving(true);
    try {
      await onUpdate(item.productId, clamped);
    } finally {
      setSaving(false);
    }
  };

  const handleRemove = async () => {
    setRemoving(true);
    try {
      await onRemove(item.productId);
    } finally {
      setRemoving(false);
    }
  };

  return (
    <tr className="border-b border-gray-100 last:border-0">
      <td className="py-4 pr-4">
        <p className="font-medium text-gray-800 text-sm">{item.productName}</p>
        <p className="text-xs text-gray-400 font-mono mt-0.5">{item.sku}</p>
      </td>
      <td className="py-4 pr-4 text-sm text-gray-600 text-right whitespace-nowrap">
        {Number(item.unitPrice).toFixed(2)}
      </td>
      <td className="py-4 pr-4 text-center">
        <input
          type="number"
          min={1}
          max={99}
          value={qty}
          disabled={saving}
          onChange={e => setQty(Number(e.target.value))}
          onBlur={commit}
          onKeyDown={e => e.key === 'Enter' && commit()}
          className="w-16 border border-gray-300 rounded-lg px-2 py-1 text-sm text-center focus:outline-none focus:ring-2 focus:ring-indigo-500 disabled:opacity-50"
        />
      </td>
      <td className="py-4 pr-4 text-sm font-semibold text-indigo-600 text-right whitespace-nowrap">
        {Number(item.lineTotal).toFixed(2)}
      </td>
      <td className="py-4 text-right">
        <button
          onClick={handleRemove}
          disabled={removing}
          className="text-xs text-red-500 hover:text-red-700 disabled:opacity-40 transition-colors"
        >
          {removing ? '...' : 'Eliminar'}
        </button>
      </td>
    </tr>
  );
}

export default function CartPage() {
  const { cart, loading, error, updateItem, removeItem, clearCart } = useCart();
  const { user } = useAuth();
  const navigate = useNavigate();
  const [clearing, setClearing] = useState(false);
  const [mutationError, setMutationError] = useState<string | null>(null);

  const handleUpdate = async (productId: number, qty: number) => {
    setMutationError(null);
    try {
      await updateItem(productId, qty);
    } catch (e) {
      setMutationError(String(e));
    }
  };

  const handleRemove = async (productId: number) => {
    setMutationError(null);
    try {
      await removeItem(productId);
    } catch (e) {
      setMutationError(String(e));
    }
  };

  const handleClear = async () => {
    setClearing(true);
    setMutationError(null);
    try {
      await clearCart();
    } catch (e) {
      setMutationError(String(e));
    } finally {
      setClearing(false);
    }
  };

  if (loading) {
    return (
      <div className="max-w-3xl mx-auto animate-pulse space-y-4 mt-8">
        <div className="h-6 bg-gray-200 rounded w-32" />
        {[1, 2, 3].map(i => (
          <div key={i} className="h-14 bg-gray-100 rounded-xl" />
        ))}
      </div>
    );
  }

  return (
    <div className="max-w-3xl mx-auto">
      <div className="mb-6">
        <h1 className="text-2xl font-bold text-gray-800 mb-1">Carrito</h1>
        <p className="text-sm text-gray-500">Revisá tu selección antes de continuar</p>
      </div>

      {(error || mutationError) && (
        <div className="bg-red-50 text-red-700 p-3 rounded-lg mb-4 text-sm">
          {error ?? mutationError}
        </div>
      )}

      {!cart || cart.items.length === 0 ? (
        <div className="text-center py-20 text-gray-400">
          <svg className="w-14 h-14 mx-auto mb-4 text-gray-300" fill="none" stroke="currentColor" strokeWidth={1.5} viewBox="0 0 24 24">
            <path strokeLinecap="round" strokeLinejoin="round" d="M2.25 3h1.386c.51 0 .955.343 1.087.835l.383 1.437M7.5 14.25a3 3 0 00-3 3h15.75m-12.75-3h11.218c1.121-2.3 2.1-4.684 2.924-7.138a60.114 60.114 0 00-16.536-1.84M7.5 14.25L5.106 5.272M6 20.25a.75.75 0 11-1.5 0 .75.75 0 011.5 0zm12.75 0a.75.75 0 11-1.5 0 .75.75 0 011.5 0z" />
          </svg>
          <p className="text-sm mb-4">Tu carrito está vacío</p>
          <Link
            to="/catalog"
            className="inline-block bg-indigo-600 text-white text-sm font-medium px-5 py-2 rounded-lg hover:bg-indigo-700 transition-colors"
          >
            Ver catálogo
          </Link>
        </div>
      ) : (
        <div className="bg-white rounded-xl shadow-sm border border-gray-100 overflow-hidden">
          <table className="w-full">
            <thead>
              <tr className="bg-gray-50 text-xs text-gray-500 uppercase tracking-wide">
                <th className="text-left px-4 py-3 font-medium">Producto</th>
                <th className="text-right px-4 py-3 font-medium">Precio unit.</th>
                <th className="text-center px-4 py-3 font-medium">Cantidad</th>
                <th className="text-right px-4 py-3 font-medium">Subtotal</th>
                <th className="px-4 py-3" />
              </tr>
            </thead>
            <tbody className="px-4">
              {cart.items.map(item => (
                <CartItemRow
                  key={item.productId}
                  item={item}
                  onUpdate={handleUpdate}
                  onRemove={handleRemove}
                />
              ))}
            </tbody>
          </table>

          <div className="border-t border-gray-100 px-6 py-4 flex items-center justify-between">
            <button
              onClick={handleClear}
              disabled={clearing}
              className="text-sm text-gray-400 hover:text-red-500 disabled:opacity-40 transition-colors"
            >
              {clearing ? 'Vaciando...' : 'Vaciar carrito'}
            </button>
            <div className="flex items-center gap-6">
              <div className="text-right">
                <p className="text-xs text-gray-500 mb-0.5">Total</p>
                <p className="text-2xl font-bold text-indigo-600">
                  {Number(cart.total).toFixed(2)}
                </p>
              </div>
              {user ? (
                <button
                  onClick={() => navigate('/checkout/address')}
                  className="bg-indigo-600 text-white text-sm font-medium px-5 py-2.5 rounded-lg hover:bg-indigo-700 transition-colors"
                >
                  Finalizar compra
                </button>
              ) : (
                <Link
                  to="/login"
                  className="bg-indigo-600 text-white text-sm font-medium px-5 py-2.5 rounded-lg hover:bg-indigo-700 transition-colors"
                >
                  Iniciar sesión para comprar
                </Link>
              )}
            </div>
          </div>
        </div>
      )}
    </div>
  );
}
