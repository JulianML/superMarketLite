import { useEffect, useState } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { inventoryApi, type InventoryMovementDTO } from '../api/inventory';

export default function MovementsPage() {
  const { productId } = useParams<{ productId: string }>();
  const navigate = useNavigate();
  const [movements, setMovements] = useState<InventoryMovementDTO[]>([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    if (!productId) return;
    setLoading(true);
    inventoryApi
      .movements(Number(productId))
      .then(setMovements)
      .catch(e => setError(String(e)))
      .finally(() => setLoading(false));
  }, [productId]);

  return (
    <div>
      <div className="flex items-center gap-3 mb-4">
        <button
          onClick={() => navigate('/inventory')}
          className="text-gray-500 hover:text-gray-700 text-sm"
        >
          ← Volver
        </button>
        <h1 className="text-2xl font-bold text-gray-800">
          Movimientos — Producto #{productId}
        </h1>
      </div>

      {error && (
        <div className="bg-red-50 text-red-700 p-3 rounded-lg mb-4 text-sm">{error}</div>
      )}

      <div className="bg-white rounded-lg shadow overflow-hidden">
        <table className="w-full text-sm">
          <thead className="bg-gray-50 text-gray-600 uppercase text-xs">
            <tr>
              <th className="px-4 py-3 text-left">ID</th>
              <th className="px-4 py-3 text-right">Cantidad</th>
              <th className="px-4 py-3 text-left">Motivo</th>
              <th className="px-4 py-3 text-left">Referencia</th>
              <th className="px-4 py-3 text-left">Fecha</th>
            </tr>
          </thead>
          <tbody className="divide-y divide-gray-100">
            {loading && (
              <tr>
                <td colSpan={5} className="px-4 py-8 text-center text-gray-400">Cargando...</td>
              </tr>
            )}
            {!loading && movements.length === 0 && (
              <tr>
                <td colSpan={5} className="px-4 py-8 text-center text-gray-400">Sin movimientos</td>
              </tr>
            )}
            {movements.map(m => (
              <tr key={m.id} className="hover:bg-gray-50">
                <td className="px-4 py-3 text-gray-500">{m.id}</td>
                <td className="px-4 py-3 text-right font-semibold">
                  <span className={m.quantityChange >= 0 ? 'text-green-600' : 'text-red-600'}>
                    {m.quantityChange >= 0 ? '+' : ''}{m.quantityChange}
                  </span>
                </td>
                <td className="px-4 py-3 text-gray-700">{m.reason}</td>
                <td className="px-4 py-3 text-gray-500">{m.referenceId ?? '—'}</td>
                <td className="px-4 py-3 text-gray-500">
                  {new Date(m.createdAt).toLocaleString('es-ES')}
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>
    </div>
  );
}
