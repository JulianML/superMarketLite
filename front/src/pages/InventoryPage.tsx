import { useEffect, useState, useCallback } from 'react';
import { useNavigate } from 'react-router-dom';
import { inventoryApi, type InventoryDTO, type InventorySetDTO, type InventoryAdjustDTO } from '../api/inventory';
import Modal from '../components/Modal';

type ModalMode =
  | { type: 'set'; item: InventoryDTO }
  | { type: 'adjust'; item: InventoryDTO }
  | null;

export default function InventoryPage() {
  const navigate = useNavigate();
  const [items, setItems] = useState<InventoryDTO[]>([]);
  const [filter, setFilter] = useState<'all' | 'below'>('all');
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [modal, setModal] = useState<ModalMode>(null);
  const [stockForm, setStockForm] = useState({ stock: '', safetyStock: '' });
  const [adjustForm, setAdjustForm] = useState({ delta: '', reason: '' });
  const [saving, setSaving] = useState(false);

  const load = useCallback(async () => {
    setLoading(true);
    setError(null);
    try {
      const res = filter === 'all'
        ? await inventoryApi.list()
        : await inventoryApi.belowSafety();
      setItems(res);
    } catch (e) {
      setError(String(e));
    } finally {
      setLoading(false);
    }
  }, [filter]);

  useEffect(() => { load(); }, [load]);

  function openSet(item: InventoryDTO) {
    setStockForm({ stock: String(item.stock), safetyStock: String(item.safetyStock) });
    setModal({ type: 'set', item });
  }

  function openAdjust(item: InventoryDTO) {
    setAdjustForm({ delta: '', reason: '' });
    setModal({ type: 'adjust', item });
  }

  async function handleSet(e: React.FormEvent) {
    e.preventDefault();
    if (!modal || modal.type !== 'set') return;
    setSaving(true);
    try {
      const data: InventorySetDTO = {
        stock: Number(stockForm.stock),
        safetyStock: stockForm.safetyStock ? Number(stockForm.safetyStock) : undefined,
      };
      await inventoryApi.set(modal.item.productId, data);
      setModal(null);
      load();
    } catch (e) {
      alert(String(e));
    } finally {
      setSaving(false);
    }
  }

  async function handleAdjust(e: React.FormEvent) {
    e.preventDefault();
    if (!modal || modal.type !== 'adjust') return;
    setSaving(true);
    try {
      const data: InventoryAdjustDTO = {
        delta: Number(adjustForm.delta),
        reason: adjustForm.reason,
      };
      await inventoryApi.adjust(modal.item.productId, data);
      setModal(null);
      load();
    } catch (e) {
      alert(String(e));
    } finally {
      setSaving(false);
    }
  }

  return (
    <div>
      <div className="flex items-center justify-between mb-4">
        <h1 className="text-2xl font-bold text-gray-800">Inventario</h1>
        <div className="flex gap-1 bg-gray-100 rounded-lg p-1">
          <button
            onClick={() => setFilter('all')}
            className={`px-3 py-1 rounded-md text-sm font-medium transition-colors ${
              filter === 'all'
                ? 'bg-white shadow text-gray-800'
                : 'text-gray-500 hover:text-gray-700'
            }`}
          >
            Todos
          </button>
          <button
            onClick={() => setFilter('below')}
            className={`px-3 py-1 rounded-md text-sm font-medium transition-colors ${
              filter === 'below'
                ? 'bg-white shadow text-red-600'
                : 'text-gray-500 hover:text-gray-700'
            }`}
          >
            Bajo stock
          </button>
        </div>
      </div>

      {error && (
        <div className="bg-red-50 text-red-700 p-3 rounded-lg mb-4 text-sm">{error}</div>
      )}

      <div className="bg-white rounded-lg shadow overflow-hidden">
        <table className="w-full text-sm">
          <thead className="bg-gray-50 text-gray-600 uppercase text-xs">
            <tr>
              <th className="px-4 py-3 text-left">Producto ID</th>
              <th className="px-4 py-3 text-right">Stock actual</th>
              <th className="px-4 py-3 text-right">Stock mínimo</th>
              <th className="px-4 py-3 text-center">Estado</th>
              <th className="px-4 py-3 text-center">Acciones</th>
            </tr>
          </thead>
          <tbody className="divide-y divide-gray-100">
            {loading && (
              <tr>
                <td colSpan={5} className="px-4 py-8 text-center text-gray-400">Cargando...</td>
              </tr>
            )}
            {!loading && items.length === 0 && (
              <tr>
                <td colSpan={5} className="px-4 py-8 text-center text-gray-400">Sin resultados</td>
              </tr>
            )}
            {items.map(item => {
              const isBelowSafety = item.stock < item.safetyStock;
              return (
                <tr key={item.productId} className="hover:bg-gray-50">
                  <td className="px-4 py-3">
                    <button
                      onClick={() => navigate(`/inventory/${item.productId}/movements`)}
                      className="text-indigo-600 hover:underline font-medium"
                    >
                      #{item.productId}
                    </button>
                  </td>
                  <td className="px-4 py-3 text-right font-semibold">
                    <span className={isBelowSafety ? 'text-red-600' : 'text-gray-800'}>
                      {item.stock}
                    </span>
                  </td>
                  <td className="px-4 py-3 text-right text-gray-500">{item.safetyStock}</td>
                  <td className="px-4 py-3 text-center">
                    {isBelowSafety ? (
                      <span className="bg-red-100 text-red-700 text-xs px-2 py-0.5 rounded-full font-medium">
                        Bajo
                      </span>
                    ) : (
                      <span className="bg-green-100 text-green-700 text-xs px-2 py-0.5 rounded-full font-medium">
                        OK
                      </span>
                    )}
                  </td>
                  <td className="px-4 py-3">
                    <div className="flex gap-3 justify-center">
                      <button
                        onClick={() => openSet(item)}
                        className="text-indigo-600 hover:text-indigo-800 text-xs font-medium"
                      >
                        Establecer
                      </button>
                      <button
                        onClick={() => openAdjust(item)}
                        className="text-amber-600 hover:text-amber-800 text-xs font-medium"
                      >
                        Ajustar
                      </button>
                      <button
                        onClick={() => navigate(`/inventory/${item.productId}/movements`)}
                        className="text-gray-500 hover:text-gray-700 text-xs font-medium"
                      >
                        Movimientos
                      </button>
                    </div>
                  </td>
                </tr>
              );
            })}
          </tbody>
        </table>
      </div>

      {modal?.type === 'set' && (
        <Modal
          title={`Establecer stock — Producto #${modal.item.productId}`}
          onClose={() => setModal(null)}
        >
          <form onSubmit={handleSet} className="space-y-3">
            <div>
              <label className="block text-xs font-medium text-gray-600 mb-1">Stock actual *</label>
              <input
                required
                type="number"
                min="0"
                value={stockForm.stock}
                onChange={e => setStockForm(f => ({ ...f, stock: e.target.value }))}
                className="w-full border border-gray-300 rounded px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-indigo-500"
              />
            </div>
            <div>
              <label className="block text-xs font-medium text-gray-600 mb-1">Stock mínimo (safety stock)</label>
              <input
                type="number"
                min="0"
                value={stockForm.safetyStock}
                onChange={e => setStockForm(f => ({ ...f, safetyStock: e.target.value }))}
                className="w-full border border-gray-300 rounded px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-indigo-500"
              />
            </div>
            <div className="flex justify-end gap-2 pt-2">
              <button
                type="button"
                onClick={() => setModal(null)}
                className="px-4 py-2 text-sm text-gray-600 hover:text-gray-800"
              >
                Cancelar
              </button>
              <button
                type="submit"
                disabled={saving}
                className="bg-indigo-600 text-white px-4 py-2 rounded-lg text-sm font-medium hover:bg-indigo-700 disabled:opacity-50"
              >
                {saving ? 'Guardando...' : 'Establecer'}
              </button>
            </div>
          </form>
        </Modal>
      )}

      {modal?.type === 'adjust' && (
        <Modal
          title={`Ajustar stock — Producto #${modal.item.productId}`}
          onClose={() => setModal(null)}
        >
          <p className="text-sm text-gray-500 mb-3">
            Stock actual: <strong className="text-gray-800">{modal.item.stock}</strong>
          </p>
          <form onSubmit={handleAdjust} className="space-y-3">
            <div>
              <label className="block text-xs font-medium text-gray-600 mb-1">
                Delta * (positivo para añadir, negativo para reducir)
              </label>
              <input
                required
                type="number"
                value={adjustForm.delta}
                onChange={e => setAdjustForm(f => ({ ...f, delta: e.target.value }))}
                className="w-full border border-gray-300 rounded px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-indigo-500"
                placeholder="ej: -5 o 10"
              />
            </div>
            <div>
              <label className="block text-xs font-medium text-gray-600 mb-1">Motivo *</label>
              <input
                required
                value={adjustForm.reason}
                onChange={e => setAdjustForm(f => ({ ...f, reason: e.target.value }))}
                className="w-full border border-gray-300 rounded px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-indigo-500"
                placeholder="ej: venta, devolución, ajuste manual"
              />
            </div>
            <div className="flex justify-end gap-2 pt-2">
              <button
                type="button"
                onClick={() => setModal(null)}
                className="px-4 py-2 text-sm text-gray-600 hover:text-gray-800"
              >
                Cancelar
              </button>
              <button
                type="submit"
                disabled={saving}
                className="bg-amber-500 text-white px-4 py-2 rounded-lg text-sm font-medium hover:bg-amber-600 disabled:opacity-50"
              >
                {saving ? 'Guardando...' : 'Ajustar'}
              </button>
            </div>
          </form>
        </Modal>
      )}
    </div>
  );
}
