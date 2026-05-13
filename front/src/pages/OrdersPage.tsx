import { useState, useEffect } from 'react';
import { Link } from 'react-router-dom';
import { ordersApi, type OrderSummaryDTO } from '../api/orders';

const STATUS_LABEL: Record<string, string> = {
  PENDING:   'Pendiente',
  CONFIRMED: 'Confirmado',
  PREPARING: 'Preparando',
  SHIPPED:   'En camino',
  DELIVERED: 'Entregado',
  CANCELLED: 'Cancelado',
};

const STATUS_COLOR: Record<string, string> = {
  PENDING:   'bg-gray-100 text-gray-600',
  CONFIRMED: 'bg-blue-100 text-blue-700',
  PREPARING: 'bg-yellow-100 text-yellow-700',
  SHIPPED:   'bg-indigo-100 text-indigo-700',
  DELIVERED: 'bg-green-100 text-green-700',
  CANCELLED: 'bg-red-100 text-red-700',
};

export default function OrdersPage() {
  const [orders, setOrders] = useState<OrderSummaryDTO[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    ordersApi.list()
      .then(setOrders)
      .catch(e => setError(String(e)))
      .finally(() => setLoading(false));
  }, []);

  if (loading) {
    return (
      <div className="max-w-3xl mx-auto animate-pulse space-y-3 mt-8">
        <div className="h-6 bg-gray-200 rounded w-40" />
        {[1, 2, 3].map(i => <div key={i} className="h-16 bg-gray-100 rounded-xl" />)}
      </div>
    );
  }

  return (
    <div className="max-w-3xl mx-auto">
      <div className="mb-6">
        <h1 className="text-2xl font-bold text-gray-800 mb-1">Mis pedidos</h1>
        <p className="text-sm text-gray-500">Historial de compras</p>
      </div>

      {error && (
        <div className="bg-red-50 text-red-700 p-3 rounded-lg mb-4 text-sm">{error}</div>
      )}

      {orders.length === 0 ? (
        <div className="text-center py-20 text-gray-400">
          <p className="text-sm mb-4">No tienes pedidos aún</p>
          <Link
            to="/catalog"
            className="inline-block bg-indigo-600 text-white text-sm font-medium px-5 py-2 rounded-lg hover:bg-indigo-700 transition-colors"
          >
            Ver catálogo
          </Link>
        </div>
      ) : (
        <div className="space-y-3">
          {orders.map(order => (
            <Link
              key={order.id}
              to={`/orders/${order.id}`}
              className="block bg-white rounded-xl shadow-sm border border-gray-100 px-5 py-4 hover:border-indigo-200 transition-colors"
            >
              <div className="flex items-center justify-between">
                <div>
                  <p className="font-mono text-sm font-semibold text-gray-800">{order.orderNumber}</p>
                  <p className="text-xs text-gray-400 mt-0.5">
                    {new Date(order.placedAt).toLocaleDateString('es-ES', {
                      day: '2-digit', month: 'short', year: 'numeric',
                    })}
                  </p>
                </div>
                <div className="flex items-center gap-4">
                  <p className="text-sm font-bold text-indigo-600">€{Number(order.total).toFixed(2)}</p>
                  <span className={`text-xs font-medium px-2.5 py-1 rounded-full ${STATUS_COLOR[order.status] ?? 'bg-gray-100 text-gray-600'}`}>
                    {STATUS_LABEL[order.status] ?? order.status}
                  </span>
                </div>
              </div>
            </Link>
          ))}
        </div>
      )}
    </div>
  );
}
