import { useState, useEffect } from 'react';
import { useParams, Link } from 'react-router-dom';
import { ordersApi, type OrderDetailDTO } from '../api/orders';

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
  SHIPPED:   'bg-[#f0faf4] text-[#1DA462]',
  DELIVERED: 'bg-green-100 text-green-700',
  CANCELLED: 'bg-red-100 text-red-700',
};

export default function OrderDetailPage() {
  const { id } = useParams<{ id: string }>();
  const [order, setOrder] = useState<OrderDetailDTO | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    if (!id) return;
    ordersApi.get(Number(id))
      .then(setOrder)
      .catch(e => setError(String(e)))
      .finally(() => setLoading(false));
  }, [id]);

  if (loading) {
    return (
      <div className="max-w-3xl mx-auto animate-pulse space-y-4 mt-8">
        <div className="h-6 bg-gray-200 rounded-full w-56" />
        <div className="h-32 bg-gray-100 rounded-2xl" />
        <div className="h-40 bg-gray-100 rounded-2xl" />
      </div>
    );
  }

  if (error || !order) {
    return (
      <div className="max-w-3xl mx-auto mt-8">
        <div className="bg-red-50 text-red-700 p-4 rounded-xl text-sm">
          {error ?? 'Pedido no encontrado'}
        </div>
        <Link to="/orders" className="text-sm text-[#1DA462] hover:text-[#178a52] mt-4 inline-block">
          ← Volver a pedidos
        </Link>
      </div>
    );
  }

  return (
    <div className="max-w-3xl mx-auto">
      <div className="flex items-center gap-3 mb-6">
        <Link to="/orders" className="text-sm text-gray-400 hover:text-gray-600 transition-colors">← Pedidos</Link>
        <span className="text-gray-300">/</span>
        <span className="font-mono text-sm font-semibold text-gray-700">{order.orderNumber}</span>
        <span className={`text-xs font-medium px-2.5 py-1 rounded-full ${STATUS_COLOR[order.status] ?? 'bg-gray-100 text-gray-600'}`}>
          {STATUS_LABEL[order.status] ?? order.status}
        </span>
      </div>

      <div className="bg-white rounded-2xl shadow-sm border border-gray-200 p-5 mb-4">
        <h2 className="text-xs font-semibold text-gray-400 uppercase tracking-wide mb-2">Dirección de entrega</h2>
        <p className="text-sm text-gray-700">{order.deliveryAddress.street}</p>
        <p className="text-sm text-gray-700">{order.deliveryAddress.postalCode} {order.deliveryAddress.city}</p>
        <p className="text-sm text-gray-700">{order.deliveryAddress.country}</p>
      </div>

      <div className="bg-white rounded-2xl shadow-sm border border-gray-200 overflow-hidden mb-4">
        <table className="w-full">
          <thead>
            <tr className="bg-gray-50 text-xs text-gray-500 uppercase tracking-wide">
              <th className="text-left px-4 py-3 font-medium">Producto</th>
              <th className="text-center px-4 py-3 font-medium">Cant.</th>
              <th className="text-right px-4 py-3 font-medium">P. Unit.</th>
              <th className="text-right px-4 py-3 font-medium">Subtotal</th>
            </tr>
          </thead>
          <tbody>
            {order.items.map(item => (
              <tr key={item.productId} className="border-b border-gray-100 last:border-0">
                <td className="px-4 py-3">
                  <p className="text-sm font-medium text-gray-800">{item.productName}</p>
                  <p className="text-xs text-gray-400 font-mono">{item.sku}</p>
                </td>
                <td className="px-4 py-3 text-center text-sm text-gray-600">{item.quantity}</td>
                <td className="px-4 py-3 text-right text-sm text-gray-600">€{Number(item.unitPrice).toFixed(2)}</td>
                <td className="px-4 py-3 text-right text-sm font-semibold text-[#1DA462]">€{Number(item.lineTotal).toFixed(2)}</td>
              </tr>
            ))}
          </tbody>
        </table>
        <div className="border-t border-gray-100 px-4 py-3 space-y-1.5">
          <SummaryRow label="Subtotal" value={order.subtotal} />
          <SummaryRow label="IVA" value={order.taxTotal} />
          <SummaryRow label="Envío" value={order.shippingFee} />
          <div className="border-t border-gray-100 pt-2">
            <SummaryRow label="Total" value={order.total} bold />
          </div>
        </div>
      </div>

      {order.history.length > 0 && (
        <div className="bg-white rounded-2xl shadow-sm border border-gray-200 p-5">
          <h2 className="text-xs font-semibold text-gray-400 uppercase tracking-wide mb-4">Historial de estado</h2>
          <ol className="relative border-l border-gray-200 space-y-4 ml-2">
            {order.history.map((h, i) => (
              <li key={i} className="ml-4">
                <div className="absolute w-2.5 h-2.5 bg-[#1DA462] rounded-full -left-1.5 border border-white mt-1" />
                <p className="text-sm font-medium text-gray-800">
                  {STATUS_LABEL[h.toStatus] ?? h.toStatus}
                </p>
                {h.note && <p className="text-xs text-gray-400">{h.note}</p>}
                <p className="text-xs text-gray-400">
                  {new Date(h.changedAt).toLocaleString('es-ES')}
                </p>
              </li>
            ))}
          </ol>
        </div>
      )}
    </div>
  );
}

function SummaryRow({ label, value, bold }: { label: string; value: number; bold?: boolean }) {
  return (
    <div className="flex justify-between text-sm">
      <span className={bold ? 'font-bold text-gray-900' : 'text-gray-500'}>{label}</span>
      <span className={bold ? 'font-bold text-[#1DA462]' : 'text-gray-700'}>€{Number(value).toFixed(2)}</span>
    </div>
  );
}
