import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { useCart } from '../context/CartContext';
import { useCheckout } from '../context/CheckoutContext';
import { ordersApi } from '../api/orders';

export default function CheckoutSummaryPage() {
  const navigate = useNavigate();
  const { cart, clearCart } = useCart();
  const { address, setConfirmedOrder } = useCheckout();
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);

  if (!address) {
    navigate('/checkout/address', { replace: true });
    return null;
  }
  if (!cart || cart.items.length === 0) {
    navigate('/cart', { replace: true });
    return null;
  }

  const handleConfirm = async () => {
    setLoading(true);
    setError(null);
    try {
      const order = await ordersApi.checkout(address);
      setConfirmedOrder(order);
      await clearCart();
      navigate('/checkout/confirmation');
    } catch (e) {
      setError(String(e));
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="max-w-lg mx-auto">
      <div className="mb-6">
        <p className="text-xs text-gray-400 uppercase tracking-wide mb-1">Paso 2 de 3</p>
        <h1 className="text-2xl font-bold text-gray-900">Resumen del pedido</h1>
      </div>

      {error && (
        <div className="bg-red-50 text-red-700 p-3 rounded-xl mb-4 text-sm">{error}</div>
      )}

      <div className="bg-white rounded-2xl shadow-sm border border-gray-200 p-5 mb-4">
        <h2 className="text-xs font-semibold text-gray-400 uppercase tracking-wide mb-3">Dirección de entrega</h2>
        <p className="text-sm text-gray-800">{address.street}</p>
        <p className="text-sm text-gray-800">{address.postalCode} {address.city}</p>
        <p className="text-sm text-gray-800">{address.country}</p>
        <button
          onClick={() => navigate('/checkout/address')}
          className="text-xs text-[#1DA462] hover:text-[#178a52] mt-2 transition-colors"
        >
          Cambiar dirección
        </button>
      </div>

      <div className="bg-white rounded-2xl shadow-sm border border-gray-200 overflow-hidden mb-4">
        <table className="w-full">
          <thead>
            <tr className="bg-gray-50 text-xs text-gray-500 uppercase tracking-wide">
              <th className="text-left px-4 py-3 font-medium">Producto</th>
              <th className="text-center px-4 py-3 font-medium">Cant.</th>
              <th className="text-right px-4 py-3 font-medium">Subtotal</th>
            </tr>
          </thead>
          <tbody>
            {cart.items.map(item => (
              <tr key={item.productId} className="border-b border-gray-100 last:border-0">
                <td className="px-4 py-3">
                  <p className="text-sm font-medium text-gray-800">{item.productName}</p>
                  <p className="text-xs text-gray-400 font-mono">{item.sku}</p>
                </td>
                <td className="px-4 py-3 text-center text-sm text-gray-600">{item.quantity}</td>
                <td className="px-4 py-3 text-right text-sm font-semibold text-[#1DA462]">
                  {Number(item.lineTotal).toFixed(2)}
                </td>
              </tr>
            ))}
          </tbody>
        </table>

        <div className="border-t border-gray-100 px-4 py-4 space-y-1.5">
          <Row label="Subtotal" value={Number(cart.total).toFixed(2)} />
          <Row label="Envío" value="0.00" />
          <div className="border-t border-gray-100 pt-2 mt-2">
            <Row label="Total" value={Number(cart.total).toFixed(2)} bold />
          </div>
        </div>
      </div>

      <div className="flex justify-between">
        <button
          onClick={() => navigate('/checkout/address')}
          className="text-sm text-gray-500 hover:text-gray-700 transition-colors"
        >
          ← Volver
        </button>
        <button
          onClick={handleConfirm}
          disabled={loading}
          className="bg-[#1DA462] hover:bg-[#178a52] text-white text-sm font-semibold px-6 py-2.5 rounded-full disabled:opacity-50 transition-colors"
        >
          {loading ? 'Procesando...' : 'Confirmar pedido'}
        </button>
      </div>
    </div>
  );
}

function Row({ label, value, bold }: { label: string; value: string; bold?: boolean }) {
  return (
    <div className="flex justify-between text-sm">
      <span className={bold ? 'font-bold text-gray-900' : 'text-gray-500'}>{label}</span>
      <span className={bold ? 'font-bold text-[#1DA462]' : 'text-gray-700'}>€{value}</span>
    </div>
  );
}
